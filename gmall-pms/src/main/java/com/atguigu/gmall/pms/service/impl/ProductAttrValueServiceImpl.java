package com.atguigu.gmall.pms.service.impl;

import com.atguigu.core.bean.PageVo;
import com.atguigu.core.bean.Query;
import com.atguigu.core.bean.QueryCondition;
import com.atguigu.gmall.pms.dao.ProductAttrValueDao;
import com.atguigu.gmall.pms.entity.ProductAttrValueEntity;
import com.atguigu.gmall.pms.service.ProductAttrValueService;
import com.atguigu.gmall.pms.vo.SpuAttributeValueVO;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;


@Service("productAttrValueService")
public class ProductAttrValueServiceImpl extends ServiceImpl<ProductAttrValueDao, ProductAttrValueEntity> implements ProductAttrValueService {

    @Autowired
    private ProductAttrValueDao productAttrValueDao;

    @Override
    public PageVo queryPage(QueryCondition params) {
        IPage<ProductAttrValueEntity> page = this.page(
                new Query<ProductAttrValueEntity>().getPage(params),
                new QueryWrapper<ProductAttrValueEntity>()
        );

        return new PageVo(page);
    }

    @Override
    public List<SpuAttributeValueVO> querySearchAttrValue(Long spuId) {
        List<ProductAttrValueEntity> productAttrValueEntitys = this.productAttrValueDao.querySearchAttrValue(spuId);
        return productAttrValueEntitys.stream().map(productAttrValueEntity->{
            SpuAttributeValueVO attributeValueVO = new SpuAttributeValueVO();
            attributeValueVO.setName(productAttrValueEntity.getAttrName());
            attributeValueVO.setProductAttributeId(productAttrValueEntity.getAttrId());
            attributeValueVO.setValue(productAttrValueEntity.getAttrValue());
            return attributeValueVO;
        }).collect(Collectors.toList());
    }

}