package com.atguigu.gmall.service;

import com.atguigu.gmall.bean.SkuLsInfo;
import com.atguigu.gmall.bean.SkuLsParams;
import com.atguigu.gmall.bean.SkuLsResult;

public interface ListService {

    /**
     * 将sku数据保存到es中
     * @param skuLsInfo
     */
    public void saveSkuInfo(SkuLsInfo skuLsInfo);


    /**
     * 通过skuParam从es中查询结果并返回结果
     */
    public SkuLsResult search(SkuLsParams skuLsParam);

    /**
     * 通过hotscore更新热度评分
     * @param skuId
     */
    public void incrHotScore(String skuId);

}


