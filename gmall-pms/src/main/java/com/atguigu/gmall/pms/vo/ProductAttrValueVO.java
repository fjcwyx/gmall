package com.atguigu.gmall.pms.vo;

import com.atguigu.gmall.pms.entity.ProductAttrValueEntity;
import org.apache.commons.lang.StringUtils;

import java.util.List;

/**
 * Created by FJC on 2019-10-31.
 */
public class ProductAttrValueVO extends ProductAttrValueEntity {

    public void setValueSelected(List<String> valueSelected){
        this.setAttrValue(StringUtils.join(valueSelected, ","));
    }
}
