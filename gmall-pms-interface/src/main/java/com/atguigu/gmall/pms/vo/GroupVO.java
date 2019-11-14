package com.atguigu.gmall.pms.vo;

import com.atguigu.gmall.pms.entity.ProductAttrValueEntity;
import lombok.Data;

import java.util.List;

/**
 * Created by FJC on 2019-11-14.
 */
@Data
public class GroupVO {

    private String groupName;

    private List<ProductAttrValueEntity> baseAttrValues;
}
