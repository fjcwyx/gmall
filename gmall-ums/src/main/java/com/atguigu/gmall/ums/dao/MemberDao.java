package com.atguigu.gmall.ums.dao;

import com.atguigu.gmall.ums.entity.MemberEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 会员
 * 
 * @author fjc
 * @email 1134118878@qq.com
 * @date 2019-10-29 09:06:37
 */
@Mapper
public interface MemberDao extends BaseMapper<MemberEntity> {
	
}
