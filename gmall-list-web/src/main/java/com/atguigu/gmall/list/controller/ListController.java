package com.atguigu.gmall.list.controller;


import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.bean.BaseAttrInfo;
import com.atguigu.gmall.bean.BaseAttrValue;
import com.atguigu.gmall.bean.SkuLsInfo;
import com.atguigu.gmall.bean.SkuLsParams;
import com.atguigu.gmall.bean.SkuLsResult;
import com.atguigu.gmall.service.ListService;
import com.atguigu.gmall.service.ManagerService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Controller
public class ListController {

    @Reference
    private ListService listService;

    @Reference
    private ManagerService managerService;



    @RequestMapping("list.html")
    //@ResponseBody
    public String getList(SkuLsParams skuLsParam, HttpServletRequest request){

        //设置每页显示的条数--自己设置固定的
        skuLsParam.setPageSize(3);
        SkuLsResult skuLsResult = listService.search(skuLsParam);
        //将对象转化为字符串
        String skuLsJson = JSON.toJSONString(skuLsResult);
        //获取sku属性值列表
        List<SkuLsInfo> skuLsInfoList = skuLsResult.getSkuLsInfoList();
        //从结果中取出平台属性值列表 通过平台属性值的id，去查询相对应的平台属性
        List<String> attrValueIdList = skuLsResult.getAttrValueIdList();
        //制作href 要连接的url参数
        String urlParam = makeUrlParam(skuLsParam);
        //调用服务层方法 查询平台属性集合
        List<BaseAttrInfo> baseAttrInfoList=managerService.getAttrList(attrValueIdList);
        //声明一个集合来存储面包屑
        ArrayList<BaseAttrValue> baseAttrValueArrayList = new ArrayList<>();
        //使用迭代器来循环遍历集合
        for (Iterator<BaseAttrInfo> iterator = baseAttrInfoList.iterator(); iterator.hasNext(); ) {
             BaseAttrInfo baseAttrInfo  =  iterator.next();
            //取得平台属性值集合
            List<BaseAttrValue> attrValueList = baseAttrInfo.getAttrValueList();
            if(attrValueIdList != null && attrValueIdList.size()>0){
                //循环判断
                for (BaseAttrValue baseAttrValue : attrValueList) {
                    //还需要跟skuLsParams.getValueId()数组中的valueId 进行比较，如果valueId相同，则将数据移除！
                    if(skuLsParam.getValueId()!=null && skuLsParam.getValueId().length>0){
                        for(String valueId : skuLsParam.getValueId()){
                            if(valueId.equals(baseAttrValue.getId())){
                                //将相同的数据在集合中移除！
                                iterator.remove();
                                //构造平台属性名称：平台属性值名称
                                BaseAttrValue baseAttrValueed = new BaseAttrValue();
                                baseAttrValueed.setValueName(baseAttrInfo.getAttrName()+":"+baseAttrValue.getValueName());
                                //url中的整个参数，点击面包屑，要取得平台属性值id
                                String makeUrlParam = makeUrlParam(skuLsParam,valueId);
                                //将最新的urlParam放入到BaseAttrValue.urlParam
                                baseAttrValueed.setUrlParam(makeUrlParam);
                                //将baseAttrValueed对象放入集合中
                                baseAttrValueArrayList.add(baseAttrValueed);

                            }
                        }
                    }

                }
            }
        }
        //保存urlParam
        request.setAttribute("urlParam",urlParam);
        //分页功能
        request.setAttribute("totalPages",skuLsResult.getTotalPages());
        request.setAttribute("pageNo",skuLsParam.getPageNo());
        //给页面渲染
        request.setAttribute("baseAttrInfoList",baseAttrInfoList);
        //保存一个关键字
        request.setAttribute("keyword",skuLsParam.getKeyword());
        //保存面包屑集合，给前台使用
        request.setAttribute("baseAttrValueArrayList",baseAttrValueArrayList);

        //将skuLsInfo集合保存，到前台页面进行渲染
        request.setAttribute("skuLsInfoList",skuLsInfoList);

        return "list";
    }

    /**
     *
     * @param skuLsParams
     * @param excludeValueIds
     * @return
     */
    private String makeUrlParam(SkuLsParams skuLsParams,String... excludeValueIds){
        String urlParam = "";
        if(skuLsParams.getKeyword()!=null){
            urlParam+="keyword="+skuLsParams.getKeyword();
        }
        if(skuLsParams.getCatalog3Id()!=null){
            if(urlParam.length()>0){
                urlParam+="&";
            }
            urlParam+="catalog3Id="+skuLsParams.getCatalog3Id();
        }
        if(skuLsParams.getValueId()!=null && skuLsParams.getValueId().length>0){
            //循环遍历
            for (int i = 0; i < skuLsParams.getValueId().length; i++) {
                String valueId = skuLsParams.getValueId()[i];
                if(excludeValueIds!=null&&excludeValueIds.length>0){
                    //每次用户只能点击一个值，取得下标为0 的数据
                    String excludeValueId = excludeValueIds[0];
                    if(excludeValueId.equals(valueId)){
                        continue;
                    }
                }
                if(urlParam.length()>0){
                    urlParam+="&";
                }
                urlParam+="valueId="+valueId;
                //将valueId拼接到urlParam！

            }
        }
        System.out.println("urlParam:"+urlParam);
        return  urlParam;
    }

}
