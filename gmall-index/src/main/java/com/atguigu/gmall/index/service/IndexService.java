package com.atguigu.gmall.index.service;

import com.atguigu.gmall.pms.entity.CategoryEntity;

import java.util.List;

/**
 * Created by FJC on 2019-11-13.
 */
public interface IndexService {
    List<CategoryEntity> queryCategoryLevel1();

    List<CategoryEntity> querySubCategories(Long pid);
}
