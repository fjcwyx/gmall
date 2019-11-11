package com.atguigu.gmall.pms.vo;

import lombok.Data;

/**
 * Created by FJC on 2019-11-11.
 */
@Data
public class SpuAttributeValueVO {

    private Long productAttributeId; //当前sku对应的属性的attr_id
    private String name;//属性名  电池
    private String value;//3G   3000mah
}
