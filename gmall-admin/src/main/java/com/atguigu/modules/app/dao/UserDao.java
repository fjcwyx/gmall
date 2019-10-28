/**
 * Copyright (c) 2016-2019 谷粒开源 All rights reserved.
 * <p>
 * https://www.guli.cloud
 * <p>
 * 版权所有，侵权必究！
 */

package com.atguigu.modules.app.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.atguigu.modules.app.entity.UserEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户
 *
 * @author Mark sunlightcs@gmail.com
 */
@Mapper
public interface UserDao extends BaseMapper<UserEntity> {

}
