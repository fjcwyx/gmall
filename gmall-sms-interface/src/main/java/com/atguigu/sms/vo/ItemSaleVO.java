package com.atguigu.sms.vo;

import lombok.Data;

/**
 * Created by FJC on 2019-11-14.
 */
@Data
public class ItemSaleVO {

    // 0-优惠券    1-满减    2-阶梯
    private String type;

    private String desc;//促销信息/优惠券的名字
}
