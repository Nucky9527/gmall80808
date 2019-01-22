package com.atguigu.gmall.managerweb.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall.bean.SkuInfo;
import com.atguigu.gmall.bean.SkuLsInfo;
import com.atguigu.gmall.bean.SpuImage;
import com.atguigu.gmall.bean.SpuSaleAttr;
import com.atguigu.gmall.service.ListService;
import com.atguigu.gmall.service.ManagerService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Controller
public class SkuManagerController {

    @Reference
    private ManagerService managerService;

    @Reference
    private ListService listService;


    /**
     * 获取指定商品所有图片列表
     * @param spuId
     * @return
     */
    @RequestMapping("spuImageList")
    @ResponseBody
    public List<SpuImage> getSpuImageList(String spuId){

        return managerService.getSpuImageList(spuId);

    }

    /**
     * 根据SpuId获得指定商品销售属性列表
     * @param spuId
     * @return
     */
    @RequestMapping("spuSaleAttrList")
    @ResponseBody
    public List<SpuSaleAttr> getSpuSaleAttrList(String spuId){

        return managerService.getSpuSaleAttrList(spuId);
    }

    /**
     * 保存具体的SkuInfo对象
     * @param skuInfo
     * @return
     */
    @RequestMapping("saveSku")
    @ResponseBody
    public String saveSku(SkuInfo skuInfo){

        managerService.saveSku(skuInfo);
        return "OK!!!";
    }

    /**
     * sku列表--根据指定的spuId查询所有的skuInfo
     * @param httpServletRequest
     * @return
     */
    @RequestMapping(value="skuInfoListBySpu")
    @ResponseBody
    public List<SkuInfo>  getSkuInfoListBySpu(HttpServletRequest httpServletRequest){
        String spuId = httpServletRequest.getParameter("spuId");
        List<SkuInfo> skuInfoList = managerService.getSkuInfoListBySpu(spuId);
        return skuInfoList;
    }


    @RequestMapping("onSale")
    @ResponseBody
    public String onSale(String skuId){
        //通过skuId 查询skuInfo对象
        SkuInfo skuInfo = managerService.getSkuInfo(skuId);

        //声明对象
        SkuLsInfo skuLsInfo = new SkuLsInfo();

       /* try{
            BeanUtils.copyProperties(skuLsInfo,skuInfo);
        }catch (IllegalAccessException e){
            e.printStackTrace();
        }catch (InvocationTargetException e){
            e.printStackTrace();
        }*/
        //下面的方法不用报错
        BeanUtils.copyProperties(skuInfo,skuLsInfo);
        listService.saveSkuInfo(skuLsInfo);
        return "OK";
    }



}
