package com.atguigu.gmall.pms.service.impl;

import com.atguigu.core.bean.PageVo;
import com.atguigu.core.bean.Query;
import com.atguigu.core.bean.QueryCondition;
import com.atguigu.gmall.pms.dao.AttrAttrgroupRelationDao;
import com.atguigu.gmall.pms.dao.AttrDao;
import com.atguigu.gmall.pms.entity.AttrAttrgroupRelationEntity;
import com.atguigu.gmall.pms.entity.AttrEntity;
import com.atguigu.gmall.pms.service.AttrService;
import com.atguigu.gmall.pms.vo.AttrVO;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service("attrService")
public class AttrServiceImpl extends ServiceImpl<AttrDao, AttrEntity> implements AttrService {

    @Autowired
    private AttrAttrgroupRelationDao attrAttrgroupRelationDao;

    @Override
    public PageVo queryPage(QueryCondition params) {
        IPage<AttrEntity> page = this.page(
                new Query<AttrEntity>().getPage(params),
                new QueryWrapper<AttrEntity>()
        );

        return new PageVo(page);
    }

    @Override
    public PageVo queryAttrByTypeAndCid(Integer type, Long cid, QueryCondition queryCondition) {

        QueryWrapper<AttrEntity> wrapper = new QueryWrapper<>();
        if (type != null) {
            wrapper.eq("attr_type", type);
        }
        if (type != null) {
            wrapper.eq("catelog_id", cid);
        }

        IPage<AttrEntity> page = this.page(
                new Query<AttrEntity>().getPage(queryCondition),
                wrapper
        );
        return new PageVo(page);
    }

    @Override
    public void saveAttrAndRelation(AttrVO attrVO) {
        //向属性表中添加数据
        this.save(attrVO);
        //向属性和组的关系表中添加数据
        AttrAttrgroupRelationEntity entity = new AttrAttrgroupRelationEntity();
        entity.setAttrId(attrVO.getAttrId());
        entity.setAttrGroupId(attrVO.getAttrGroupId());
        this.attrAttrgroupRelationDao.insert(entity);
    }

}