package com.atguigu.gmall.managerservice.mapper;

import com.atguigu.gmall.bean.SkuSaleAttrValue;

import tk.mybatis.mapper.common.Mapper;

import java.util.List;

public interface SkuSaleAttrValueMapper extends Mapper<SkuSaleAttrValue> {

     public List<SkuSaleAttrValue> selectSkuSaleAttrValueListBySpu(String spuId);

}
