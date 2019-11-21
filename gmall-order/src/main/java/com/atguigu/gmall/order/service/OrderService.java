package com.atguigu.gmall.order.service;

import com.atguigu.core.bean.Resp;
import com.atguigu.core.bean.UserInfo;
import com.atguigu.gmall.cart.vo.CartItemVO;
import com.atguigu.gmall.oms.entity.OrderEntity;
import com.atguigu.gmall.oms.vo.OrderItemVO;
import com.atguigu.gmall.oms.vo.OrderSubmitVO;
import com.atguigu.gmall.order.feign.*;
import com.atguigu.gmall.order.interceptor.LoginInterceptor;
import com.atguigu.gmall.order.vo.OrderConfirmVO;
import com.atguigu.gmall.pms.entity.SkuInfoEntity;
import com.atguigu.gmall.pms.entity.SkuSaleAttrValueEntity;
import com.atguigu.gmall.ums.entity.MemberEntity;
import com.atguigu.gmall.ums.entity.MemberReceiveAddressEntity;
import com.atguigu.gmall.wms.entity.WareSkuEntity;
import com.atguigu.gmall.wms.vo.SkuLockVO;
import com.atguigu.sms.vo.ItemSaleVO;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

/**
 * Created by FJC on 2019-11-16.
 */
@Service
public class OrderService {

    @Autowired
    private GmallUmsFeign gmallUmsClient;
    @Autowired
    private GmallPmsFeign gmallPmsClient;
    @Autowired
    private GmallSmsFeign gmallSmsClient;
    @Autowired
    private GmallWmsFeign gmallWmsClient;

    @Autowired
    private GmallOmsFeign gmallOmsClient;



    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private AmqpTemplate amqpTemplate;

    @Autowired
    private GmallCartFeign gmallCartClient;

    @Autowired
    private ThreadPoolExecutor threadPoolExecutor;

    private static final String TOKEN_PREFIX = "order:token:";

    public OrderConfirmVO confirm() {

        UserInfo userInfo = LoginInterceptor.get();
        OrderConfirmVO orderConfirmVO = new OrderConfirmVO();
        //设置收货地址
        CompletableFuture<Void> adressCompletableFuture = CompletableFuture.runAsync(() -> {
            Resp<List<MemberReceiveAddressEntity>> addresses = this.gmallUmsClient.queryAddressesByUserId(userInfo.getUserId());
            orderConfirmVO.setAddresses(addresses.getData());
        }, threadPoolExecutor);
        //设置订单中的商品信息
        CompletableFuture<Void> cartCompletableFuture = CompletableFuture.supplyAsync(() -> {
            Resp<List<CartItemVO>> cartItemVOS = this.gmallCartClient.queryCartItemVO(userInfo.getUserId());
            List<CartItemVO> itemVOS = cartItemVOS.getData();
            return itemVOS;
        }, threadPoolExecutor).thenAcceptAsync(itemVOS -> {
            if (CollectionUtils.isEmpty(itemVOS)) {
                return;
            }
            List<OrderItemVO> orderItemVOS = itemVOS.stream().map(cartItemVO -> {
                OrderItemVO orderItemVO = new OrderItemVO();
                // 根据s'kuId查询sku
                Resp<SkuInfoEntity> skuInfoEntityResp = this.gmallPmsClient.querySkuById(cartItemVO.getSkuId());
                SkuInfoEntity skuInfoEntity = skuInfoEntityResp.getData();
                // 根据skuId查询销售属性
                Resp<List<SkuSaleAttrValueEntity>> skuSaleResp = this.gmallPmsClient.querySkuSaleAttrValueBySkuId(cartItemVO.getSkuId());
                List<SkuSaleAttrValueEntity> skuSaleAttrValueEntities = skuSaleResp.getData();

                orderItemVO.setSkuAttrValue(skuSaleAttrValueEntities);
                orderItemVO.setTitle(skuInfoEntity.getSkuTitle());
                orderItemVO.setSkuId(cartItemVO.getSkuId());
                orderItemVO.setPrice(skuInfoEntity.getPrice());
                orderItemVO.setDefaultImage(skuInfoEntity.getSkuDefaultImg());
                orderItemVO.setCount(cartItemVO.getCount());
                // 根据skuId获取营销信息
                Resp<List<ItemSaleVO>> saleResp = this.gmallSmsClient.queryItemSaleVOs(cartItemVO.getSkuId());
                List<ItemSaleVO> itemSaleVOS = saleResp.getData();
                orderItemVO.setSales(itemSaleVOS);
                // 根据skuId获取库存信息
                Resp<List<WareSkuEntity>> storeResp = this.gmallWmsClient.queryWareSkuBySkuId(cartItemVO.getSkuId());
                List<WareSkuEntity> wareSkuEntities = storeResp.getData();
                orderItemVO.setStore(wareSkuEntities.stream().anyMatch(wareSkuEntity -> wareSkuEntity.getStock() > 0));
                orderItemVO.setWeight(skuInfoEntity.getWeight());
                return orderItemVO;
            }).collect(Collectors.toList());
            orderConfirmVO.setOrderItems(orderItemVOS);
        }, threadPoolExecutor);

        //设置积分信息
        CompletableFuture<Void> boundCompletableFuture = CompletableFuture.runAsync(() -> {
            Resp<MemberEntity> memberEntity = this.gmallUmsClient.queryUserById(userInfo.getUserId());
            orderConfirmVO.setBounds(memberEntity.getData().getIntegration());
        }, threadPoolExecutor);
        //设置订单唯一标志
        CompletableFuture<Void> idCompletableFuture = CompletableFuture.runAsync(() -> {
            String timeId = IdWorker.getTimeId();
            orderConfirmVO.setOrderToken(timeId);
            this.redisTemplate.opsForValue().set(TOKEN_PREFIX+timeId, timeId);
        }, threadPoolExecutor);

        CompletableFuture.allOf(adressCompletableFuture,cartCompletableFuture,boundCompletableFuture,idCompletableFuture).join();
        return orderConfirmVO;
    }

    public void submit(OrderSubmitVO orderSubmitVO) {
        //1. 验证令牌防止重复提交
        String orderToken = orderSubmitVO.getOrderToken();
        String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
        Long flag = this.redisTemplate.execute(new DefaultRedisScript<>(script, Long.class), Arrays.asList(TOKEN_PREFIX + orderToken), orderToken);
        if (flag == 0l) {
            throw new RuntimeException("请不要重复提交！");
        }
        //2. 验证价格
        BigDecimal totalPrice = orderSubmitVO.getTotalPrice();
        List<OrderItemVO> orderItemVOS = orderSubmitVO.getOrderItemVOS();
        if (CollectionUtils.isEmpty(orderItemVOS)){
            throw new RuntimeException("请求添加购物清单！");
        }
        BigDecimal currentPrice = orderItemVOS.stream().map(orderItemVO -> {
            Resp<SkuInfoEntity> skuInfoEntityResp = this.gmallPmsClient.querySkuById(orderItemVO.getSkuId());
            SkuInfoEntity skuInfoEntity = skuInfoEntityResp.getData();
            return skuInfoEntity.getPrice().multiply(new BigDecimal(orderItemVO.getCount()));
        }).reduce((a, b) -> a.add(b)).get();
        if (totalPrice.compareTo(currentPrice) != 0) {
            throw new RuntimeException("请刷新页面后重试！");
        }
        //3. 验证库存，并锁定库存
        List<SkuLockVO> skuLockVOS = orderItemVOS.stream().map(orderItemVO -> {
            SkuLockVO skuLockVO = new SkuLockVO();
            skuLockVO.setSkuId(orderItemVO.getSkuId());
            skuLockVO.setCount(orderItemVO.getCount());
            //skuLockVO.setOrderToken(orderToken);
            return skuLockVO;
        }).collect(Collectors.toList());

        Resp<Object> objectResp = this.gmallWmsClient.chechAndLock(skuLockVOS);

        if (objectResp.getCode() == 1) {
            throw new RuntimeException(objectResp.getMsg());
        }
        //4. 生成订单
        UserInfo userInfo = LoginInterceptor.get();
        Resp<OrderEntity> orderResp = null;

        try {
            orderSubmitVO.setUserId(userInfo.getUserId());
            Resp<MemberEntity> memberEntityResp = this.gmallUmsClient.queryUserById(userInfo.getUserId());
            MemberEntity memberEntity = memberEntityResp.getData();
            orderSubmitVO.setUserName(memberEntity.getUsername());

            orderResp = this.gmallOmsClient.createOrder(orderSubmitVO);

        } catch (Exception e) {
            e.printStackTrace();
//          this.amqpTemplate.convertAndSend("WMS-EXCHANGE", "wms.ttl", orderToken);
            throw new RuntimeException("订单创建失败！服务器异常！");
        }

        //5. 删购物车中对应的记录（消息队列）
        Map<String, Object> map = new HashMap<>();
        map.put("userId", userInfo.getUserId());
        List<Long> skuIds = orderItemVOS.stream().map(orderItemVO -> orderItemVO.getSkuId()).collect(Collectors.toList());
        map.put("skuIds", skuIds);
        this.amqpTemplate.convertAndSend("GMALL-ORDER-EXCHANGE", "cart.delete", map);

//        if (orderResp != null) {
//            return orderResp.getData();
//        }
//        return null;
    }
}
