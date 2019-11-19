package com.atguigu.gmall.cart.service;

import com.alibaba.fastjson.JSON;
import com.atguigu.core.bean.Resp;
import com.atguigu.core.bean.UserInfo;
import com.atguigu.gmall.cart.feign.GmallPmsFeign;
import com.atguigu.gmall.cart.feign.GmallSmsFeign;
import com.atguigu.gmall.cart.interceptor.LoginInterceptor;
import com.atguigu.gmall.cart.vo.Cart;
import com.atguigu.gmall.cart.vo.CartItemVO;
import com.atguigu.gmall.pms.entity.SkuInfoEntity;
import com.atguigu.gmall.pms.entity.SkuSaleAttrValueEntity;
import com.atguigu.sms.vo.ItemSaleVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CartService {

    @Autowired
    private GmallPmsFeign gmallPmsFeign;

    @Autowired
    private GmallSmsFeign gmallSmsClient;

    @Autowired
    private StringRedisTemplate redisTemplate;

    private static final String CART_PREFIX = "cart:uid:";

    public static final String CURRENT_PRICE_PRFIX = "cart:price:";

    public void addCart(Cart cart) {

        // 获取userInfo
        UserInfo userInfo = LoginInterceptor.get();

        // 获取redis的key
        String key = getKey();

        // 查询用户购物车
        BoundHashOperations<String, Object, Object> hashOps = this.redisTemplate.boundHashOps(key);
        // 判断购物车是否存在
        Long skuId = cart.getSkuId();
        Integer count = cart.getCount();
        // 注意这里的skuId要转化成String，因为redis中保存的都是String
        if (hashOps.hasKey(skuId.toString())){
            // 购物车已存在该记录，更新数量
            String cartJson = hashOps.get(skuId.toString()).toString();
            cart = JSON.parseObject(cartJson, Cart.class);
            cart.setCount(cart.getCount() + count);
        } else {
            // 购物车不存在该记录，新增记录
            Resp<SkuInfoEntity> skuResp = this.gmallPmsFeign.querySkuById(skuId);
            SkuInfoEntity skuInfoEntity = skuResp.getData();
            cart.setCount(count);
            cart.setDefaultImage(skuInfoEntity.getSkuDefaultImg());
            cart.setPrice(skuInfoEntity.getPrice());
            cart.setTitle(skuInfoEntity.getSkuTitle());
            Resp<List<SkuSaleAttrValueEntity>> saleAttrValueResp = this.gmallPmsFeign.querySkuSaleAttrValueBySkuId(skuId);
            cart.setSkuAttrValue(saleAttrValueResp.getData());
            // 查询营销信息
            Resp<List<ItemSaleVO>> listResp1 = this.gmallSmsClient.queryItemSaleVOs(cart.getSkuId());
            cart.setSales(listResp1.getData());
            this.redisTemplate.opsForValue().set(CURRENT_PRICE_PRFIX + skuId, skuInfoEntity.getPrice().toString());
        }
        // 将购物车记录写入redis
        hashOps.put(skuId.toString(), JSON.toJSONString(cart));
    }

    public List<Cart> queryCarts() {

        // 查询未登录状态的购物车
        UserInfo userInfo = LoginInterceptor.get();
        String key1 = CART_PREFIX + userInfo.getUserKey();

        BoundHashOperations<String, Object, Object> userKeyOps = this.redisTemplate.boundHashOps(key1);
        List<Object> cartJsonList = userKeyOps.values();
        List<Cart> userKeyCarts = null;
        if (!CollectionUtils.isEmpty(cartJsonList)) {
            userKeyCarts = cartJsonList.stream().map(cartJson -> {
                Cart cart = JSON.parseObject(cartJson.toString(), Cart.class);
                cart.setCurrentPrice(new BigDecimal(this.redisTemplate.opsForValue().get(CURRENT_PRICE_PRFIX + cart.getSkuId())));
                return cart;
            }).collect(Collectors.toList());
        }

        // 判断登录状态
        if (userInfo.getUserId() == null) {
            // 未登录直接返回
            return userKeyCarts;
        }

        // 登录，查询登录状态的购物车
        String key2 = CART_PREFIX + userInfo.getUserId();
        BoundHashOperations<String, Object, Object> userIdOps = this.redisTemplate.boundHashOps(key2);
        // 判断未登录的购物车是否为空
        if (!CollectionUtils.isEmpty(userKeyCarts)) {
            // 不为空，合并
            userKeyCarts.forEach(cart -> {
                // 有更新数量
                if (userIdOps.hasKey(cart.getSkuId().toString())){
                    String cartJson = userIdOps.get(cart.getSkuId().toString()).toString();
                    Cart idCart = JSON.parseObject(cartJson, Cart.class);
                    // 更新数量
                    idCart.setCount(idCart.getCount() + cart.getCount());
                    userIdOps.put(cart.getSkuId().toString(), JSON.toJSONString(idCart));
                } else {
                    // 没有新增记录
                    userIdOps.put(cart.getSkuId().toString(), JSON.toJSONString(cart));
                }
            });
            this.redisTemplate.delete(key1);
        }
        // 查询返回
        List<Object> userIdCartJsonList = userIdOps.values();
        if (CollectionUtils.isEmpty(userIdCartJsonList)){
            return null;
        }
        return userIdCartJsonList.stream().map(userIdCartJson -> {
            Cart cart = JSON.parseObject(userIdCartJson.toString(), Cart.class);
            cart.setCurrentPrice(new BigDecimal(this.redisTemplate.opsForValue().get(CURRENT_PRICE_PRFIX + cart.getSkuId())));
            return cart;
        }).collect(Collectors.toList());
    }

    public void updateCart(Cart cart) {

        String key = getKey();

        Integer count = cart.getCount();

        BoundHashOperations<String, Object, Object> hashOps = this.redisTemplate.boundHashOps(key);

        if (hashOps.hasKey(cart.getSkuId().toString())) {
            // 获取购物车中的更新数量的购物记录
            String cartJson = hashOps.get(cart.getSkuId().toString()).toString();
            cart = JSON.parseObject(cartJson, Cart.class);
            cart.setCount(count);
            hashOps.put(cart.getSkuId().toString(), JSON.toJSONString(cart));
        }

    }
    private String getKey() {
        String key = CART_PREFIX;
        // 判断登录状态
        UserInfo userInfo = LoginInterceptor.get();
        if (userInfo.getUserId() != null) {
            key += userInfo.getUserId();
        } else {
            key += userInfo.getUserKey();
        }
        return key;
    }

    public void deleteCart(Long skuId) {
        String key = getKey();

        BoundHashOperations<String, Object, Object> hashOps = this.redisTemplate.boundHashOps(key);
        if (hashOps.hasKey(skuId.toString())) {

            hashOps.delete(skuId.toString());
        }

    }

    public void checkCart(List<Cart> carts) {
        String key = getKey();
        BoundHashOperations<String, Object, Object> hashOps = this.redisTemplate.boundHashOps(key);
        carts.forEach(cart -> {
            Boolean check = cart.getCheck();
            if (hashOps.hasKey(cart.getSkuId().toString())) {
                // 获取购物车中的更新数量的购物记录
                String cartJson = hashOps.get(cart.getSkuId().toString()).toString();
                cart = JSON.parseObject(cartJson, Cart.class);
                cart.setCheck(check);
                hashOps.put(cart.getSkuId().toString(), JSON.toJSONString(cart));
            }
        });
    }

    public List<CartItemVO> queryCartItemVO(Long userId) {
        // 登录，查询登录状态的购物车
        String key = CART_PREFIX + userId;
        BoundHashOperations<String, Object, Object> userIdOps = this.redisTemplate.boundHashOps(key);
        // 查询返回
        List<Object> userIdCartJsonList = userIdOps.values();
        if (CollectionUtils.isEmpty(userIdCartJsonList)){
            return null;
        }
        // 获取所有的购物车记录
        return userIdCartJsonList.stream().map(userIdCartJson -> {
            Cart cart = JSON.parseObject(userIdCartJson.toString(), Cart.class);
            cart.setCurrentPrice(new BigDecimal(this.redisTemplate.opsForValue().get(CURRENT_PRICE_PRFIX + cart.getSkuId())));
            return cart;
        }).filter(cart -> cart.getCheck()).map(cart -> {
            CartItemVO cartItemVO = new CartItemVO();
            cartItemVO.setSkuId(cart.getSkuId());
            cartItemVO.setCount(cart.getCount());
            return cartItemVO;
        }).collect(Collectors.toList());
    }
}