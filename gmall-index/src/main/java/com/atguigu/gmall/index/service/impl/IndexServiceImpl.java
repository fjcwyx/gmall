package com.atguigu.gmall.index.service.impl;

import com.atguigu.core.bean.Resp;
import com.atguigu.gmall.index.feign.GmallPmsFeign;
import com.atguigu.gmall.index.service.IndexService;
import com.atguigu.gmall.pms.entity.CategoryEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created by FJC on 2019-11-13.
 */
@Service
public class IndexServiceImpl implements IndexService {

    @Autowired
    private GmallPmsFeign gmallPmsFeign;

    @Override
    public List<CategoryEntity> queryCategoryLevel1() {
        Resp<List<CategoryEntity>> categoryResp = this.gmallPmsFeign.queryCategories(1,0l);
        return categoryResp.getData();
    }

    @Override
    public List<CategoryEntity> querySubCategories(Long pid) {
        Resp<List<CategoryEntity>> subCategoryResp = this.gmallPmsFeign.querySubCategory(pid);
        return subCategoryResp.getData();
    }
}
