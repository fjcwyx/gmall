package com.atguigu.gmall.pms.service.impl;

import com.atguigu.core.bean.PageVo;
import com.atguigu.core.bean.Query;
import com.atguigu.core.bean.QueryCondition;
import com.atguigu.gmall.pms.dao.*;
import com.atguigu.gmall.pms.entity.*;
import com.atguigu.gmall.pms.feign.GmallSmsFeign;
import com.atguigu.gmall.pms.service.SpuInfoService;
import com.atguigu.gmall.pms.vo.ProductAttrValueVO;
import com.atguigu.gmall.pms.vo.SkuInfoVO;
import com.atguigu.gmall.pms.vo.SpuInfoVO;
import com.atguigu.sms.vo.SaleVO;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.List;
import java.util.UUID;


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

    @Override
    public void bigSave(SpuInfoVO spuInfoVO) {
        //1 保存spu相关信息
        //1.1 保存pms_spu_info表信息
        spuInfoVO.setCreateTime(new Date());
        spuInfoVO.setUodateTime(spuInfoVO.getCreateTime());
        this.save(spuInfoVO);

        Long spu_id = spuInfoVO.getId();
        //1.2 保存pms_spu_info_desc表信息
        List<String> spuImages = spuInfoVO.getSpuImages();
        String desc = org.apache.commons.lang.StringUtils.join(spuImages, ",");
        SpuInfoDescEntity spuInfoDescEntity = new SpuInfoDescEntity();
        spuInfoDescEntity.setSpuId(spu_id);
        spuInfoDescEntity.setDecript(desc);
        this.spuInfoDescDao.insert(spuInfoDescEntity);
        //1.3 保存pms_product_attr_value表信息
        List<ProductAttrValueVO> baseAttrs = spuInfoVO.getBaseAttrs();
        baseAttrs.forEach(baseAttr -> {
            baseAttr.setSpuId(spu_id);

            baseAttr.setAttrSort(0);
            baseAttr.setQuickShow(1);
            this.productAttrValueDao.insert(baseAttr);

        });
        //2 保存sku相关信息
        //2.1保存pms_sku_info表信息
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


}
















