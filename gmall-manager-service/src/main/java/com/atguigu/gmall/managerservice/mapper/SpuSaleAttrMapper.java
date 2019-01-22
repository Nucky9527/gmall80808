package com.atguigu.gmall.managerservice.mapper;

import com.atguigu.gmall.bean.SpuSaleAttr;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

public interface SpuSaleAttrMapper extends Mapper<SpuSaleAttr> {
    /**
     * 根据spuId查询销售属性列表
     * @param spuId
     * @return
     */
    List<SpuSaleAttr> selectSpuSaleAttrList(String spuId);

    /**
     * 查询售属性集合
     * @param spuId
     * @param skuId
     * @return
     */
    List<SpuSaleAttr> selectSpuSaleAttrListCheckBySku(String spuId, String skuId);

}
