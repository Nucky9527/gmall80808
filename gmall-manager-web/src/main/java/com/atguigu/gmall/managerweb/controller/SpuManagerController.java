package com.atguigu.gmall.managerweb.controller;


import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall.bean.SpuInfo;
import com.atguigu.gmall.service.ManagerService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
public class SpuManagerController {

    @Reference
    private ManagerService managerService;


    /**
     * 跳转到商品列表页面
     * @return
     */
    @RequestMapping("spuListPage")
    public String spuListPage(){

        return "spuListPage";
    }


    /**
     * 获取到商品信息的列表 通过三级分类Id
     * @param spuInfo
     * @return
     */
    @RequestMapping("getSpuInfoList")
    @ResponseBody
    public List<SpuInfo> getSpuInfoList(SpuInfo spuInfo){

       return managerService.getSpuInfoList(spuInfo);


    }

    /**
     * 保存具体商品信息对象
     * @param spuInfo
     * @return
     */
    @RequestMapping("saveSpuInfo")
    @ResponseBody
    public String saveSpuInfo(SpuInfo spuInfo){

        managerService.saveSpuInfo(spuInfo);
        return "OK!!!";
    }

}
