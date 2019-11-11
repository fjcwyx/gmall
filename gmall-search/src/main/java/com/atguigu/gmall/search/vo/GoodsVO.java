package com.atguigu.gmall.search.vo;

import com.atguigu.gmall.pms.vo.SpuAttributeValueVO;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * Created by FJC on 2019-11-10.
 */
@Data
public class GoodsVO {

    //搜索页面上部的聚合显示字段
    private Long brandId; //品牌id
    private String brandName;  //品牌名
    private Long productCategoryId;  //sku的分类id
    private String productCategoryName; //sku的名字

    /**
     *  保存当前sku所有需要检索的属性；(每类商品都有不同的检索属性)list集合中的每个vo代表一个检索属性
     *  根据vo中的id聚合、名称作为索引名，值作为索引范围
     *  检索属性来源于spu的基本属性中的search_type=1（销售属性都已经拼接在标题中了）
     */
    private List<SpuAttributeValueVO> attrValueList;//检索属性

    //展示的每个商品所需要的字段
    private Long id;  //skuId
    private String pic; //sku的默认图片
    private String name;//这是需要检索的sku的标题
    private BigDecimal price;//sku-price；
    private Integer sale;//sku-sale 销量
    private Long stock;//sku-stock 库存
    private Integer sort;//排序分 热度分

}
