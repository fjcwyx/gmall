package com.atguigu.gmall.wms.dao;

import com.atguigu.gmall.wms.entity.WareSkuEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 商品库存
 * 
 * @author fjc
 * @email 1134118878@qq.com
 * @date 2019-10-29 09:12:08
 */
@Mapper
public interface WareSkuDao extends BaseMapper<WareSkuEntity> {

    List<WareSkuEntity> checkStock(@Param("skuId") long skuId,@Param("count") Integer count);

    int lockStock(@Param("id") long id,@Param("count") Integer count);

    int unLockStock(@Param("id") long skuWareId,@Param("count") Integer count);

	
}
