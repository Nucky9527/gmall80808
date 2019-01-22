package com.atguigu.gmall.item.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.bean.SkuInfo;
import com.atguigu.gmall.bean.SkuSaleAttrValue;
import com.atguigu.gmall.bean.SpuSaleAttr;
import com.atguigu.gmall.config.LoginRequire;
import com.atguigu.gmall.service.ListService;
import com.atguigu.gmall.service.ManagerService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;


@Controller
public class ItemController {

        @Reference
         ListService listService;

        @Reference
        ManagerService managerService;

        @RequestMapping("{skuId}.html")
        //@LoginRequire
        public String skuInfoPage(@PathVariable String skuId, HttpServletRequest request){

            System.out.println("skuId:"+skuId);
            SkuInfo skuInfo = managerService.getSkuInfo(skuId);
            //加载销售属性值
            List<SpuSaleAttr> saleAttrList=managerService.selectSpuSaleAttrListCheckBySku(skuInfo);
            //获取所有的销售属性值集合
            List<SkuSaleAttrValue> skuSaleAttrValueListBySpu = managerService.getSkuSaleAttrValueListBySpu(skuInfo.getSpuId());

            String key="";
            HashMap<String,Object>map = new HashMap<>();
            for (int i = 0; i < skuSaleAttrValueListBySpu.size(); i++) {
                 SkuSaleAttrValue skuSaleAttrValue = skuSaleAttrValueListBySpu.get(i);
                 if(key.length()>0){
                     key+="|";
                 }
                key+=skuSaleAttrValue.getSaleAttrValueId();
                 if((i+1)==skuSaleAttrValueListBySpu.size() || !skuSaleAttrValue.getSkuId().equals(skuSaleAttrValueListBySpu.get(i+1).getSkuId())){
                     map.put(key,skuSaleAttrValue.getSkuId());
                     key="";
                 }
            }

            String valueSkuJson = JSON.toJSONString(map);
            System.out.println(valueSkuJson);
            request.setAttribute("valueSkuJson",valueSkuJson);

            request.setAttribute("saleAttrList",saleAttrList);
            //从数据库中获取到skuInfo对象之后要存入作用域中
            request.setAttribute("skuInfo",skuInfo);

            //记录商品被访问的次数
            listService.incrHotScore(skuId);

            return "item";
        }


        }


