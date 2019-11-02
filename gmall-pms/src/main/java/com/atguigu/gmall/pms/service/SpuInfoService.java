package com.atguigu.gmall.pms.service;

import com.atguigu.core.bean.PageVo;
import com.atguigu.core.bean.QueryCondition;
import com.atguigu.gmall.pms.entity.SpuInfoEntity;
import com.atguigu.gmall.pms.vo.SpuInfoVO;
import com.baomidou.mybatisplus.extension.service.IService;


/**
 * spu信息
 *
 * @author fjc
 * @email 1134118878@qq.com
 * @date 2019-10-28 23:08:48
 */
public interface SpuInfoService extends IService<SpuInfoEntity> {

    PageVo queryPage(QueryCondition params);

    PageVo querySpuInfo(QueryCondition queryCondition, Long catId);

    void bigSave(SpuInfoVO spuInfoVO);

}

