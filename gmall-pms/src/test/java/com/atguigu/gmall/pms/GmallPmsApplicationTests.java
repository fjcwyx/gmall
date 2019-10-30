package com.atguigu.gmall.pms;

import com.atguigu.gmall.pms.controller.BrandController;
import com.atguigu.gmall.pms.dao.BrandDao;
import com.atguigu.gmall.pms.entity.BrandEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class GmallPmsApplicationTests {

    @Autowired
    private BrandDao brandDao;

    @Autowired
    private BrandController brandController;


    @Test
    void contextLoads() {
    }

    @Test
    public void test(){
//        IPage<BrandEntity> page = this.brandDao.selectPage(new Page<>(2, 2), new QueryWrapper<BrandEntity>());
//        System.out.println(page.getRecords());
//        System.out.println(page.getTotal());
//        System.out.println(page.getPages());

        BrandEntity brandEntity = new BrandEntity();
        brandEntity.setName("小米手机");
        brandEntity.setLogo("https://fjc-scw.oss-cn-shanghai.aliyuncs.com/2019-10-30/f49af2e4-9486-4998-8da4-24ffd328e6c6_2.png");
        brandEntity.setDescript("国民手机");
        brandEntity.setFirstLetter("H");
        brandEntity.setShowStatus(1);
        this.brandController.save(brandEntity);

    }

}
