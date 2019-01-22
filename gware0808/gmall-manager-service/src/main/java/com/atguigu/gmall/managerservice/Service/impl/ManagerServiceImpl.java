package com.atguigu.gmall.managerservice.Service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.bean.*;
import com.atguigu.gmall.config.RedisUtil;
import com.atguigu.gmall.managerservice.constant.ManagerConstant;
import com.atguigu.gmall.managerservice.mapper.*;
import com.atguigu.gmall.service.ManagerService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;

import java.util.List;

/*
* 相关方法实现
* */


@Service
public class    ManagerServiceImpl implements ManagerService {


    /*
    * 自动注入Mapper实例对象
    * */

    @Autowired
    private BaseAttrInfoMapper baseAttrInfoMapper;

    @Autowired
    private BaseAttrValueMapper baseAttrValueMapper;

    @Autowired
    private BaseCatalog1Mapper baseCatalog1Mapper;

    @Autowired
    private BaseCatalog2Mapper baseCatalog2Mapper;

    @Autowired
    private BaseCatalog3Mapper baseCatalog3Mapper;

    @Autowired
    private SpuInfoMapper spuInfoMapper;

    @Autowired
    private BaseSaleAttrMapper baseSaleAttrMapper;


    @Autowired
    private SpuSaleAttrMapper spuSaleAttrMapper;

    @Autowired
    private SpuSaleAttrValueMapper spuSaleAttrValueMapper;

    @Autowired
    private SpuImageMapper spuImageMapper;

    @Autowired
    private SkuInfoMapper skuInfoMapper;

    @Autowired
    private SkuImageMapper skuImageMapper;

    @Autowired
    private SkuAttrValueMapper skuAttrValueMapper;

    @Autowired
    private SkuSaleAttrValueMapper skuSaleAttrValueMapper;

    @Autowired
    private RedisUtil redisUtil;

    /**
     * 页面加载时，查询一级分类
     *
     * @return
     */
    @Override
    public List<BaseCatalog1> getCatalog1List() {
        List<BaseCatalog1> baseCatalog1List = baseCatalog1Mapper.selectAll();
        return baseCatalog1List;
    }


    /**
     * 根据一级分类的id，查询二级分类所有数据
     *
     * @param catalog1Id
     * @return
     */
    @Override
    public List<BaseCatalog2> getCatalog2List(String catalog1Id) {
        BaseCatalog2 baseCatalog2 = new BaseCatalog2();
        baseCatalog2.setCatalog1Id(catalog1Id);
        List<BaseCatalog2> baseCatalog2List = baseCatalog2Mapper.select(baseCatalog2);
        return baseCatalog2List;
    }


    /**
     * 根据二级分类的Id，查询三级分类所有数据
     *
     * @param catalog2Id
     * @return
     */
    @Override
    public List<BaseCatalog3> getCatalog3List(String catalog2Id) {
        BaseCatalog3 baseCatalog3 = new BaseCatalog3();
        baseCatalog3.setCatalog2Id(catalog2Id);
        List<BaseCatalog3> baseCatalog3List = baseCatalog3Mapper.select(baseCatalog3);
        return baseCatalog3List;
    }


    /**
     * 根据三级分类的Id,查询属性列表所有数据
     */
    @Override
    public List<BaseAttrInfo> getBaseAttrInfoList(String catalog3Id) {
        //BaseAttrInfo baseAttrInfo = new BaseAttrInfo();
        //baseAttrInfo.setCatalog3Id(catalog3Id);
        //
        //List<BaseAttrInfo> baseAttrInfoList = baseAttrInfoMapper.select(baseAttrInfo);

        List<BaseAttrInfo> baseAttrInfoList = baseAttrInfoMapper.getBaseAttrInfoListByCatalog3Id(Long.parseLong(catalog3Id));
        return baseAttrInfoList;
    }


    /**
     * 实现保存平台属性功能
     * <p>
     * 熟悉base_catalog3（三级分类表），base_attr_info（属性表），base_attr_value（属性值表）之间的关系
     * <p>
     * 保存得存两张表的数据，属性名称和属性值名称。
     * 保存实际上是删除原先的值，重新插入新的值
     */
    @Override
    public void saveAttrInfo(BaseAttrInfo baseAttrInfo) {
        //如果有主键就进行更新，如果没有就插入
        if (baseAttrInfo.getId() != null && baseAttrInfo.getId().length() > 0) {
            baseAttrInfoMapper.updateByPrimaryKey(baseAttrInfo);
        } else {
            //防止主键被赋上一个空字符串  先添加判断条件，主键是否为空 让主键实现自动增长
            if (baseAttrInfo.getId().length() == 0) {
                baseAttrInfo.setId(null);
            }
            baseAttrInfoMapper.insertSelective(baseAttrInfo);
        }
        //把原属性值全部清空
        BaseAttrValue baseAttrValue = new BaseAttrValue();
        baseAttrValue.setAttrId(baseAttrInfo.getId());      //通过attr_Id找到对应的属性值，将其清空
        baseAttrValueMapper.delete(baseAttrValue);

        //重新插入属性值
        if (baseAttrInfo.getAttrValueList() != null && baseAttrInfo.getAttrValueList().size() > 0) {
            for (BaseAttrValue attrValue : baseAttrInfo.getAttrValueList()) {
                //防止主键被赋上一个空字符串  先添加判断条件，主键是否为空 让主键实现自动增长
                if (attrValue.getId().length() == 0) {
                    attrValue.setId(null);
                }
                attrValue.setAttrId(baseAttrInfo.getId());      //通过attr_Id找到对应的属性，给其赋值
                baseAttrValueMapper.insertSelective(attrValue);
            }
        }
    }


    /**
     * 通过属性Id，获得平台属性信息
     *
     * @param attrId
     * @return
     */
    @Override
    public BaseAttrInfo getAttrInfo(String attrId) {
        BaseAttrInfo baseAttrInfo = baseAttrInfoMapper.selectByPrimaryKey(attrId);
        BaseAttrValue baseAttrValue = new BaseAttrValue();
        baseAttrValue.setAttrId(baseAttrInfo.getId());
        List<BaseAttrValue> attrValueList = baseAttrValueMapper.select(baseAttrValue);
        baseAttrInfo.setAttrValueList(attrValueList);

        return baseAttrInfo;
    }


    /**
     * 根据三级分类的Id，查询spuInfo的集合
     *
     * @param spuInfo
     * @return
     */
    @Override
    public List<SpuInfo> getSpuInfoList(SpuInfo spuInfo) {

        return spuInfoMapper.select(spuInfo);


    }

    /**
     * 获得所有销售属性的列表
     *
     * @return
     */
    @Override
    public List<BaseSaleAttr> getBaseSaleAttrList() {

        return baseSaleAttrMapper.selectAll();
    }


    /**
     * 保存具体的商品信息（注意表与表之间的关联）
     *
     * @param spuInfo
     */
    @Override
    public void saveSpuInfo(SpuInfo spuInfo) {

        //    private String id;            自增
        //    private String spuName;
        //    private String description;
        //    private  String catalog3Id;
        //    private List<SpuSaleAttr> spuSaleAttrList;
        //    private List<SpuImage> spuImageList;


        /*
       保存的原理就是 想办法把更新数据和插入数据合到一个方法里。
       首先得通过spuInfo的Id，判断当前是保存新数据还是更新旧数据
         */
        if (spuInfo.getId() == null || spuInfo.getId().length() == 0) {
            spuInfo.setId(null);    //先将Id设置为null；
            spuInfoMapper.insertSelective(spuInfo);     // 插入新数据
        } else {
            spuInfoMapper.updateByPrimaryKeySelective(spuInfo);     //更新旧数据
        }

        //商品图片表：先删除，再自增
        SpuImage spuImage = new SpuImage();
        spuImage.setSpuId(spuInfo.getId());
        spuImageMapper.delete(spuImage);
        //先获取页面提交过来的数据
        List<SpuImage> spuImageList = spuInfo.getSpuImageList();
        if (spuImageList != null && spuImageList.size() > 0) {
            //循环插入图片
            for (SpuImage image : spuImageList) {
                image.setId(null);
                //指定对应的SpuId
                image.setSpuId(spuInfo.getId());
                spuImageMapper.insertSelective(image);


            }
        }

        //销售属性表SpuSaleAttr:也是先删除，再重新插入新的数据
        SpuSaleAttr spuSaleAttr = new SpuSaleAttr();
        spuSaleAttr.setId(spuInfo.getId());
        spuSaleAttrMapper.delete(spuSaleAttr);
        //先获取数据
        List<SpuSaleAttr> spuSaleAttrList = spuInfo.getSpuSaleAttrList();
        if (spuSaleAttrList != null && spuSaleAttrList.size() > 0) {
            for (SpuSaleAttr saleAttr : spuSaleAttrList) {
                saleAttr.setId(null);
                saleAttr.setSpuId(spuInfo.getId());
                spuSaleAttrMapper.insertSelective(saleAttr);
                //销售属性值表SpuSaleAttrValue,此表与销售属性有关联
                List<SpuSaleAttrValue> spuSaleAttrValueList = saleAttr.getSpuSaleAttrValueList();
                if (spuSaleAttrValueList != null && spuSaleAttrValueList.size() > 0) {
                    for (SpuSaleAttrValue spuSaleAttrValue : spuSaleAttrValueList) {
                        spuSaleAttrValue.setId(null);
                        spuSaleAttrValue.setSpuId(spuInfo.getId());
                        spuSaleAttrValueMapper.insertSelective(spuSaleAttrValue);
                    }

                }
            }
        }
    }

    /**
     * 通过当前选中的spuId，查询所有图片
     *
     * @param spuId
     * @return
     */
    @Override
    public List<SpuImage> getSpuImageList(String spuId) {
        // 创建商品图片实例对象
        SpuImage spuImage = new SpuImage();
        //根据指定的SpuId
        spuImage.setSpuId(spuId);
        //返回相应的商品图片列表
        return spuImageMapper.select(spuImage);


    }

    /**
     * 通过spuId查找指定商品的销售属性--属性值（多表关联查询）
     *
     * @param spuId
     * @return
     */
    @Override
    public List<SpuSaleAttr> getSpuSaleAttrList(String spuId) {
        return spuSaleAttrMapper.selectSpuSaleAttrList(spuId);
    }


    /**
     * 保存skuInfo对象
     *
     * @param skuInfo
     */
    @Override
    public void saveSku(SkuInfo skuInfo) {

        //String id;
        //String spuId;
        //String price;
        //String skuName;
        //String weight;
        //String skuDesc;
        //String catalog3Id;
        //String skuDefaultImg;
        //List<SkuImage> skuImageList;
        //List<SkuAttrValue> skuAttrValueList;
        //List<SkuSaleAttrValue> skuSaleAttrValueList;
        //前台页面将skuInfo的属性逐一保存

        //先判断skuInfo对象的id是否为空，如果空--是新数据要插入，如果不空--是旧数据要更新
        if (skuInfo.getId() != null && skuInfo.getId().length() > 0) {
           skuInfoMapper.updateByPrimaryKeySelective(skuInfo);
        } else {
            skuInfo.setId(null);
            skuInfoMapper.insertSelective(skuInfo);
        }
        //插入skuImage表中数据，先按指定的skuId删除之前的数据，再重新插入新的数据
        SkuImage skuImage = new SkuImage();
        skuImage.setSkuId(skuInfo.getId());
        skuImageMapper.delete(skuImage);

        List<SkuImage> skuImageList = skuInfo.getSkuImageList();
        if (skuImageList != null && skuImageList.size() > 0) {
            for (SkuImage image : skuImageList) {
                image.setId(null);
                image.setSkuId(skuInfo.getId());
                skuImageMapper.insertSelective(image);
            }
        }

        //插入sku平台属性表的数据，先按指定skuId删除旧数据，再插入新数据
        SkuAttrValue skuAttrValue = new SkuAttrValue();
        skuAttrValue.setId(skuInfo.getId());
        skuAttrValueMapper.delete(skuAttrValue);

        List<SkuAttrValue> skuAttrValueList = skuInfo.getSkuAttrValueList();
        if (skuAttrValueList != null && skuAttrValueList.size() > 0) {
            for (SkuAttrValue attrValue : skuAttrValueList) {
                attrValue.setId(null);
                attrValue.setSkuId(skuInfo.getId());
                skuAttrValueMapper.insertSelective(attrValue);
            }
        }


        //插入sku销售属性表的数据，先按指定skuId删除旧数据，再插入新数据
        SkuSaleAttrValue skuSaleAttrValue = new SkuSaleAttrValue();
        skuSaleAttrValue.setId(skuInfo.getId());
        skuSaleAttrValueMapper.delete(skuSaleAttrValue);

        List<SkuSaleAttrValue> skuSaleAttrValueList = skuInfo.getSkuSaleAttrValueList();
        if (skuSaleAttrValueList != null && skuSaleAttrValueList.size() > 0) {
            for (SkuSaleAttrValue saleAttrValue : skuSaleAttrValueList) {
                saleAttrValue.setId(null);
                saleAttrValue.setSkuId(skuInfo.getId());
                skuSaleAttrValueMapper.insertSelective(saleAttrValue);

            }
        }


    }


    /**
     * 根据指定的spuId，查询获得所有skuInfo
     *
     * @param spuId
     * @return
     */
    @Override
    public List<SkuInfo> getSkuInfoListBySpu(String spuId) {
        List<SkuInfo> skuInfoList = skuInfoMapper.selectSkuInfoListBySpu(Long.parseLong(spuId));
        return skuInfoList;

    }

    /**
     * 根据指定的SkuId查询相对应的SkuInfo对象
     *
     * 重写的目的是将指定的商品详情放入redis中
     *
     * 重点：数据查询以及数据保存到redis的具体流程图（考虑判断条件）
     * @param skuId
     * @return
     */
    @Override
    public SkuInfo getSkuInfo(String skuId) {


       /* //先测试一下redis的工具类
        Jedis jedis = redisUtil.getJedis();
        jedis.set("test","测试！！！");
        jedis.close();
        ---测试成功
        */
        SkuInfo skuInfo = null;
     try {
         Jedis jedis = redisUtil.getJedis();
         String skuInfoKey = ManagerConstant.SKUKEY_PREFIX+skuId+ManagerConstant.SKUKEY_SUFFIX;
         String skuJson = jedis.get(skuInfoKey);
         if(skuJson==null || skuJson.length()==0){
             System.out.println("没有命中缓存");
             String skuLockKey = ManagerConstant.SKUKEY_PREFIX+skuId+ManagerConstant.SKULOCK_SUFFIX;
             String lockKey = jedis.set(skuLockKey,"OK","NX","PX",ManagerConstant.SKULOCK_EXPIRE_PX);
             if("OK".equals(lockKey)){
                 System.out.println("获得锁！");
                 //先从数据库查询出数据
                 skuInfo = getSkuInfoDB(skuId);
                 //在将数据对象转换成JSON串
                 String skuRedisStr = JSON.toJSONString(skuInfo);
                 jedis.setex(skuInfoKey,ManagerConstant.SKUKEY_TIMEOUT,skuRedisStr);
                 jedis.close();
                 return skuInfo;
             }else {
                 System.out.println("等待!");
                 Thread.sleep(1000);
                 return getSkuInfo(skuId);
             }
         }else{
             skuInfo = JSON.parseObject(skuJson,SkuInfo.class);
             jedis.close();
             return skuInfo;
         }

       }catch (Exception e){
         e.printStackTrace();
     }
     return getSkuInfoDB(skuId);

        /*Jedis jedis = redisUtil.getJedis();
        //定义Key
        String skuInfoKey = ManagerConstant.SKUKEY_PREFIX+skuId+ManagerConstant.SKUKEY_SUFFIX;
        if(jedis.exists(skuInfoKey)){
            String skuJson = jedis.get(skuInfoKey);
            if(skuJson !=null && !"".equals(skuJson)){
                //redis中查询到的json串转换为对象
                 skuInfo = JSON.parseObject(skuJson, SkuInfo.class);
                return skuInfo;
            }
        }else{
             skuInfo = getSkuInfoDB(skuId);
            //将数据库中的数据转换为json串存入redis中
            jedis.setex(skuInfoKey,ManagerConstant.SKUKEY_TIMEOUT,JSON.toJSONString(skuInfo));
        }
        return skuInfo;
*/

    }

    //这一部分是从数据库中查询数据，最好抽取成一个独立的方法供对象调用。
    private SkuInfo getSkuInfoDB(String skuId) {
        //单纯的信息
        SkuInfo skuInfo = skuInfoMapper.selectByPrimaryKey(skuId);

        //查询图片并将所有图片赋予具体对象
        SkuImage skuImage = new SkuImage();
        skuImage.setSkuId(skuId);
        List<SkuImage> skuImageList = skuImageMapper.select(skuImage);
        skuInfo.setSkuImageList(skuImageList);

        //查询属性值并赋值给具体对象
        SkuSaleAttrValue skuSaleAttrValue = new SkuSaleAttrValue();
        skuSaleAttrValue.setSkuId(skuId);
        List<SkuSaleAttrValue> skuSaleAttrValueList = skuSaleAttrValueMapper.select(skuSaleAttrValue);
        skuInfo.setSkuSaleAttrValueList(skuSaleAttrValueList);

        //查询skuAttrValue并赋值给具体对象
        SkuAttrValue skuAttrValue = new SkuAttrValue();
        skuAttrValue.setSkuId(skuId);
        List<SkuAttrValue> skuAttrValues = skuAttrValueMapper.select(skuAttrValue);
        skuInfo.setSkuAttrValueList(skuAttrValues);


        return skuInfo;
    }


    /**
     * 通过具体的skuInfo，查找商品的销售属性列表
     * @param skuInfo
     * @return
     */
    @Override
    public List<SpuSaleAttr> selectSpuSaleAttrListCheckBySku(SkuInfo skuInfo) {
        //传入spuId,skuId
        return spuSaleAttrMapper.selectSpuSaleAttrListCheckBySku(skuInfo.getSpuId(),skuInfo.getId());
    }

    /**
     * 通过具体的spuId，获取sku的销售属性值
     * @param spuId
     * @return
     */
    @Override
    public List<SkuSaleAttrValue> getSkuSaleAttrValueListBySpu(String spuId) {

        return skuSaleAttrValueMapper.selectSkuSaleAttrValueListBySpu(spuId);

    }

    /**
     * 通过平台属性值id集合，去查询相对应的平台属性集合
     * @param attrValueIdList
     * @return
     */
    @Override
    public List<BaseAttrInfo> getAttrList(List<String> attrValueIdList) {

        String attrValueIds = StringUtils.join(attrValueIdList.toArray(), ",");
       List<BaseAttrInfo> baseAttrInfoList= baseAttrInfoMapper.selectAttrInfoListByIds(attrValueIds);

        return baseAttrInfoList;
    }


}

