package com.atguigu.gmall.pms.service.impl;

import com.atguigu.core.bean.PageVo;
import com.atguigu.core.bean.Query;
import com.atguigu.core.bean.QueryCondition;
import com.atguigu.gmall.pms.dao.*;
import com.atguigu.gmall.pms.entity.SkuImagesEntity;
import com.atguigu.gmall.pms.entity.SkuInfoEntity;
import com.atguigu.gmall.pms.entity.SkuSaleAttrValueEntity;
import com.atguigu.gmall.pms.entity.SpuInfoEntity;
import com.atguigu.gmall.pms.feign.GmallSmsFeign;
import com.atguigu.gmall.pms.service.SpuInfoDescService;
import com.atguigu.gmall.pms.service.SpuInfoService;
import com.atguigu.gmall.pms.vo.ProductAttrValueVO;
import com.atguigu.gmall.pms.vo.SkuInfoVO;
import com.atguigu.gmall.pms.vo.SpuInfoVO;
import com.atguigu.sms.vo.SaleVO;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.*;


@Service("spuInfoService")
public class SpuInfoServiceImpl extends ServiceImpl<SpuInfoDao, SpuInfoEntity> implements SpuInfoService {

    @Override
    public PageVo queryPage(QueryCondition params) {
        IPage<SpuInfoEntity> page = this.page(
                new Query<SpuInfoEntity>().getPage(params),
                new QueryWrapper<SpuInfoEntity>()
        );

        return new PageVo(page);
    }

    @Override
    public PageVo querySpuInfo(QueryCondition queryCondition, Long catId) {

        QueryWrapper<SpuInfoEntity> wrapper = new QueryWrapper<>();
        if (catId != 0) {
            wrapper.eq("catalog_id", catId);
        }
        String key = queryCondition.getKey();
        if (!StringUtils.isEmpty(key)){
           wrapper.and(t -> t.eq("id", key).or().like("spu_name", key));
        }
        IPage<SpuInfoEntity> page = this.page(
                new Query<SpuInfoEntity>().getPage(queryCondition),
                wrapper
        );
        return new PageVo(page);
    }

    @Autowired
    private SpuInfoDescDao spuInfoDescDao;

    @Autowired
    private ProductAttrValueDao productAttrValueDao;

    @Autowired
    private SkuInfoDao skuInfoDao;

    @Autowired
    private SkuImagesDao skuImagesDao;

    @Autowired
    private SkuSaleAttrValueDao skuSaleAttrValueDao;

    @Autowired
    private GmallSmsFeign gmallSmsFeign;

    @Autowired
    private SpuInfoDescService spuInfoDescService;

    @Autowired
    private AmqpTemplate amqpTemplate;


    @Override
    @Transactional
    public void bigSave(SpuInfoVO spuInfoVO) {
        //1 保存spu相关信息
        //1.1 保存pms_spu_info表信息
        Long spu_id = saveSpuInfo(spuInfoVO);

        //1.2 保存pms_spu_info_desc表信息
        this.spuInfoDescService.saveSpuDesc(spuInfoVO, spu_id);

        //1.3 保存pms_product_attr_value表信息
        saveBaseAttrs(spuInfoVO, spu_id);

        //2 保存sku相关信息
        //2.1保存pms_sku_info表信息
        saveSkuInfoWithSaleInfo(spuInfoVO, spu_id);

        sendMsg(spu_id,"insert");
    }

    private void sendMsg(Long spu_id,String type) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", spu_id);
        map.put("type", type);
        this.amqpTemplate.convertAndSend("GMALL-ITEM-EXCHANGE","item."+type,map);
    }

    public void saveSkuInfoWithSaleInfo(SpuInfoVO spuInfoVO, Long spu_id) {
        List<SkuInfoVO> skus = spuInfoVO.getSkus();
        if (CollectionUtils.isEmpty(skus)){
            return;
        }
        skus.forEach(sku -> {
            SkuInfoEntity skuInfoEntity = new SkuInfoEntity();
            BeanUtils.copyProperties(sku, skuInfoEntity);
            skuInfoEntity.setSpuId(spu_id);
            skuInfoEntity.setSkuCode(UUID.randomUUID().toString());
            skuInfoEntity.setCatalogId(spuInfoVO.getCatalogId());
            skuInfoEntity.setBrandId(spuInfoVO.getBrandId());
            List<String> images = sku.getImages();
            if (!CollectionUtils.isEmpty(images)) {
                skuInfoEntity.setSkuDefaultImg(!StringUtils.isEmpty(skuInfoEntity.getSkuDefaultImg()) ? skuInfoEntity.getSkuDefaultImg() : images.get(0));
            }
            this.skuInfoDao.insert(skuInfoEntity);

            Long sku_Id = skuInfoEntity.getSkuId();
        //2.2保存pms_sku_images表信息
            if (!CollectionUtils.isEmpty(images)) {
                images.forEach(image -> {
                    SkuImagesEntity skuImagesEntity = new SkuImagesEntity();
                    skuImagesEntity.setSkuId(sku_Id);
                    skuImagesEntity.setImgUrl(image);
                    skuImagesEntity.setImgSort(0);
                    skuImagesEntity.setDefaultImg(org.apache.commons.lang.StringUtils.equals(image, skuInfoEntity.getSkuDefaultImg())? 1 : 0);
                    this.skuImagesDao.insert(skuImagesEntity);
                });
            }
        //2.3保存pms_sku_sale_attr_value表信息
            List<SkuSaleAttrValueEntity> saleAttrs = sku.getSaleAttrs();
            if(!CollectionUtils.isEmpty(saleAttrs)){
                saleAttrs.forEach(saleAttr -> {
                    saleAttr.setSkuId(sku_Id);
                    saleAttr.setAttrSort(0);
                    this.skuSaleAttrValueDao.insert(saleAttr);
                });
            }
        //3 保存营销相关信息
            SaleVO saleVO = new SaleVO();
            BeanUtils.copyProperties(sku, saleVO);
            saleVO.setSkuId(sku_Id);
            this.gmallSmsFeign.saveSaleVo(saleVO);
        });
    }

    public void saveBaseAttrs(SpuInfoVO spuInfoVO, Long spu_id) {
        List<ProductAttrValueVO> baseAttrs = spuInfoVO.getBaseAttrs();
        baseAttrs.forEach(baseAttr -> {
            baseAttr.setSpuId(spu_id);

            baseAttr.setAttrSort(0);
            baseAttr.setQuickShow(1);
            this.productAttrValueDao.insert(baseAttr);

        });
    }

    public Long saveSpuInfo(SpuInfoVO spuInfoVO) {
        spuInfoVO.setCreateTime(new Date());
        spuInfoVO.setUodateTime(spuInfoVO.getCreateTime());
        this.save(spuInfoVO);
        return spuInfoVO.getId();
    }
}
















