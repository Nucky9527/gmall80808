package com.atguigu.gmall.managerweb.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall.bean.*;
import com.atguigu.gmall.service.ManagerService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;

@Controller
public class ManagerController {


    @Reference
    private ManagerService managerService;

    /*测试页面方法
    * */
    @RequestMapping("index")
    public String index(){
        return "index";
    }



    @RequestMapping("attrListPage")
    public String getAttrListPage(){
            return "attrListPage";
        }

    /*
    * 获得一级分类列表
    * */
    @RequestMapping("getCatalog1")
    @ResponseBody
    public List<BaseCatalog1> getCatalog1List(){

         return managerService.getCatalog1List();



    }

    /*
    * 获取二级分类列表
    * */
    @RequestMapping("getCatalog2")
    @ResponseBody
    public List<BaseCatalog2> getCatalog2List(String catalog1Id){


        return managerService.getCatalog2List(catalog1Id);

    }


    /*
    * 获得三级分类列表
    * */
    @RequestMapping("getCatalog3")
    @ResponseBody
    public List<BaseCatalog3> getCatalog3List(String catalog2Id){

        return managerService.getCatalog3List(catalog2Id);



    }

    /**
     *
     * @param catalog3Id
     * @return
     */

    @RequestMapping("getAttrInfoList")
    @ResponseBody
    public List<BaseAttrInfo> getAttrInfoList(String catalog3Id){

        return managerService.getBaseAttrInfoList(catalog3Id);

    }


    /**
     *
     * @param baseAttrInfo
     * @return
     */
    @RequestMapping("saveAttrInfo")
    @ResponseBody
    public String saveAttrInfo(BaseAttrInfo baseAttrInfo){
        //调用服务层的保存方法
        managerService.saveAttrInfo(baseAttrInfo);
        return "SUCCESS!!!";
    }

    /**
     *
     * @param attrId
     * @return
     */
   @RequestMapping("getAttrValueList")
    @ResponseBody
    public List<BaseAttrValue> getAttrValueList(String attrId){
       BaseAttrInfo attrInfo = managerService.getAttrInfo(attrId);

       return attrInfo.getAttrValueList();

   }

    /**
     *  查询基本销售属性列表
     * @return
     */
   @RequestMapping("baseSaleAttrList")
   @ResponseBody
   public List<BaseSaleAttr> getBaseSaleAttrList(){

       return managerService.getBaseSaleAttrList();
   }



}
