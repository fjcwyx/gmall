package com.atguigu.gmall.sms.service;

import com.atguigu.core.bean.PageVo;
import com.atguigu.core.bean.QueryCondition;
import com.atguigu.gmall.sms.entity.SkuBoundsEntity;
import com.atguigu.sms.vo.ItemSaleVO;
import com.atguigu.sms.vo.SaleVO;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;


/**
 * 商品sku积分设置
 *
 * @author fjc
 * @email 1134118878@qq.com
 * @date 2019-10-29 08:59:14
 */
public interface SkuBoundsService extends IService<SkuBoundsEntity> {

    PageVo queryPage(QueryCondition params);

    void saveSaleVo(SaleVO saleVO);

    List<ItemSaleVO> queryItemSaleVOs(Long skuId);
}

