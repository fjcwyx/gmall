package com.atguigu.gmall.wms.vo;

import lombok.Data;

/**
 * Created by FJC on 2019-11-18.
 */
@Data
public class SkuLockVO {

    private Long skuId;

    private Integer count;

    private Boolean  lock; //锁定成功为true，失败为false

    private Long skuWareId; //锁定库存的id

    private String orderToken; //订单号

}
