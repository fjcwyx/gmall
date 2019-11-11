package com.atguigu.gmall.pms.service;

import com.atguigu.core.bean.PageVo;
import com.atguigu.core.bean.QueryCondition;
import com.atguigu.gmall.pms.entity.ProductAttrValueEntity;
import com.atguigu.gmall.pms.vo.SpuAttributeValueVO;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;


/**
 * spu属性值
 *
 * @author fjc
 * @email 1134118878@qq.com
 * @date 2019-10-28 23:08:48
 */
public interface ProductAttrValueService extends IService<ProductAttrValueEntity> {

    PageVo queryPage(QueryCondition params);

    List<SpuAttributeValueVO> querySearchAttrValue(Long spuId);
}

