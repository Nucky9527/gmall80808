package com.atguigu.gmall.service;


import com.atguigu.gmall.bean.*;

import java.util.List;

/*
* 平台管理--接口
* */

public interface ManagerService {

    public List<BaseCatalog1> getCatalog1List();        //获取一级目录

    public List<BaseCatalog2> getCatalog2List(String catalog1Id);        //获取二级目录

    public List<BaseCatalog3> getCatalog3List(String catalog2Id);        //获取三级目录

    public List<BaseAttrInfo> getBaseAttrInfoList(String catalog3Id);    //获取属性基本信息列表

    public void saveAttrInfo (BaseAttrInfo baseAttrInfo);                       //保存平台属性--属性值

    public BaseAttrInfo getAttrInfo(String attrId);                                  //获取具体属性对象

    public List<SpuInfo> getSpuInfoList(SpuInfo spuInfo);                    //获取商品信息列表


    public List<BaseSaleAttr> getBaseSaleAttrList();                                // 查询基本销售属性表

    void saveSpuInfo(SpuInfo spuInfo);                                                   //保存具体的商品信息对象

    public List<SpuImage> getSpuImageList(String spuId);                      //获取商品图片列表

    public List<SpuSaleAttr> getSpuSaleAttrList(String spuId);                  //获取指定商品销售属性列表

    public void saveSku(SkuInfo skuInfo);                                                 //保存具体的skuInfo对象

    public List<SkuInfo> getSkuInfoListBySpu(String spuId);                     //根据指定的spuId，查询获得所有skuInfo


    public SkuInfo getSkuInfo(String skuId);                                               //根据指定对的skuId查询相对应的SkuInfo对象

    /**
     *根据spuId，skuId查询销售属性，销售属性值，并且使对应的skuId的销售属性值默认选中
     * @param skuInfo
     * @return
     */
   public List<SpuSaleAttr> selectSpuSaleAttrListCheckBySku(SkuInfo skuInfo);


    /**
     * 根据spuId查询skuId对应的销售属性值集合
     * @param spuId
     * @return
     */
    public List<SkuSaleAttrValue> getSkuSaleAttrValueListBySpu(String spuId);



    /**
     * 通过平台属性值id集合，去查询相对应的平台属性集合
     * @param attrValueIdList
     */
    List<BaseAttrInfo> getAttrList(List<String> attrValueIdList);
}
