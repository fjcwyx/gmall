package com.atguigu.gmall.pms.service;

import com.atguigu.core.bean.PageVo;
import com.atguigu.core.bean.QueryCondition;
import com.atguigu.gmall.pms.entity.AttrGroupEntity;
import com.atguigu.gmall.pms.vo.AttrGroupVO;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;


/**
 * 属性分组
 *
 *
 * @author fjc
 * @email 1134118878@qq.com
 * @date 2019-10-28 23:08:48
 */
public interface AttrGroupService extends IService<AttrGroupEntity> {

    PageVo queryPage(QueryCondition params);

    PageVo queryByCidPage(Long catId, QueryCondition queryCondition);

    AttrGroupVO queryAttrByGroupId(Long gid);

    List<AttrGroupVO> queryAttrByCId(Long catId);
}

