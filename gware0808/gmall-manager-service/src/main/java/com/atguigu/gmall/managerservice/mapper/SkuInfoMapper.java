package com.atguigu.gmall.managerservice.mapper;

import com.atguigu.gmall.bean.SkuInfo;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

public interface SkuInfoMapper extends Mapper<SkuInfo> {


    /**
     * 指定查询方法
     * @param spuId
     * @return
     */
    public List<SkuInfo> selectSkuInfoListBySpu(long spuId);

}
