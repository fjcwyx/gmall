package com.atguigu.gmall.wms.service.impl;

import com.atguigu.core.bean.PageVo;
import com.atguigu.core.bean.Query;
import com.atguigu.core.bean.QueryCondition;
import com.atguigu.gmall.wms.dao.WareSkuDao;
import com.atguigu.gmall.wms.entity.WareSkuEntity;
import com.atguigu.gmall.wms.service.WareSkuService;
import com.atguigu.gmall.wms.vo.SkuLockVO;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.stream.Collectors;


@Service("wareSkuService")
public class WareSkuServiceImpl extends ServiceImpl<WareSkuDao, WareSkuEntity> implements WareSkuService {

    @Autowired
    private RedissonClient redissonClient;

    @Autowired
    private WareSkuDao wareSkuDao;

    @Override
    public PageVo queryPage(QueryCondition params) {
        IPage<WareSkuEntity> page = this.page(
                new Query<WareSkuEntity>().getPage(params),
                new QueryWrapper<WareSkuEntity>()
        );

        return new PageVo(page);
    }

    @Override
    public String chechAndLock(List<SkuLockVO> skuLockVOS) {

        //边遍历边锁库存(验证通过一个商品，就需要锁定一个商品的库存)
        //只要有一个验库存过程中，出现库存不足，则所有的库存都不能锁定
        skuLockVOS.forEach(skuLockVO -> {
            //遍历锁库存
            lockSku(skuLockVO);
        });

        //获取锁定成功的商品集合
        List<SkuLockVO> success = skuLockVOS.stream().filter(skuLockVO -> skuLockVO.getLock()).collect(Collectors.toList());
        //获取锁定失败的商品集合
        List<SkuLockVO> error = skuLockVOS.stream().filter(skuLockVO -> !skuLockVO.getLock()).collect(Collectors.toList());

        if (!CollectionUtils.isEmpty(error)){
            //如果锁库失败商品集合大于0，则回滚锁定成功的商品
            success.forEach(sku -> {
                this.wareSkuDao.unLockStock(sku.getSkuWareId(),sku.getCount());
            });
        return "锁定失败："+ error.stream().map(sku -> sku.getSkuId()).collect(Collectors.toList()).toString();
        }
            return null;
    }

    private void lockSku(SkuLockVO skuLockVO) {
        RLock lock = this.redissonClient.getLock("sku:lock:" + skuLockVO.getSkuId());
        lock.lock();
        skuLockVO.setLock(false);
        //验库存
        List<WareSkuEntity> wareSkuEntities = this.wareSkuDao.checkStock(skuLockVO.getSkuId(), skuLockVO.getCount());
        //如果没有仓库库存符合条件，则标记锁库存失败
        if (!CollectionUtils.isEmpty(wareSkuEntities)){
            //锁库存
            if(this.wareSkuDao.lockStock(wareSkuEntities.get(0).getId(), skuLockVO.getCount())==1){
                skuLockVO.setLock(true);
                skuLockVO.setSkuWareId(wareSkuEntities.get(0).getId());
            }
        }
        lock.unlock();
    }

}