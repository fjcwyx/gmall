package com.atguigu.gmall.order.service;

import com.atguigu.core.bean.Resp;
import com.atguigu.core.bean.UserInfo;
import com.atguigu.gmall.cart.vo.CartItemVO;
import com.atguigu.gmall.order.feign.*;
import com.atguigu.gmall.order.interceptor.LoginInterceptor;
import com.atguigu.gmall.order.vo.OrderConfirmVO;
import com.atguigu.gmall.order.vo.OrderItemVO;
import com.atguigu.gmall.pms.entity.SkuInfoEntity;
import com.atguigu.gmall.pms.entity.SkuSaleAttrValueEntity;
import com.atguigu.gmall.ums.entity.MemberEntity;
import com.atguigu.gmall.ums.entity.MemberReceiveAddressEntity;
import com.atguigu.gmall.wms.entity.WareSkuEntity;
import com.atguigu.sms.vo.ItemSaleVO;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;
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
    private GmallCartFeign gmallCartClient;

    @Autowired
    private ThreadPoolExecutor threadPoolExecutor;


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
        }, threadPoolExecutor);

        CompletableFuture.allOf(adressCompletableFuture,cartCompletableFuture,boundCompletableFuture,idCompletableFuture).join();
        return orderConfirmVO;
    }
}
