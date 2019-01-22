package com.atguigu.gmall.managerweb.controller;

import org.apache.commons.lang3.StringUtils;
import org.csource.common.MyException;
import org.csource.fastdfs.ClientGlobal;
import org.csource.fastdfs.StorageClient;
import org.csource.fastdfs.TrackerClient;
import org.csource.fastdfs.TrackerServer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;


import java.io.IOException;

@RestController
public class FileUploadController {

   @Value("${fileServer.url}")     //表示从配置文件中获得数据，当前类，必须在spring容器中
    private  String fileUrl;            // 相当于 fileUrl= http://192.168.203.128

    @RequestMapping("fileUpload")
    public String fileUpload(@RequestParam("file") MultipartFile file) throws IOException,MyException{
        //上传之后要返回的图片路径
        //将图片上传到图片服务器，上传成功之后，并返回图片地址
        //编程实现软编码，将重要的可变信息放入配置文件中application properties
        String imgUrl = fileUrl;
        if(file!=null) {


            //读取配置文件
            String configfile = this.getClass().getResource("/tracker.conf").getFile();
            ClientGlobal.init(configfile);
            TrackerClient trackerClient = new TrackerClient();
            TrackerServer trackerServer = trackerClient.getConnection();
            StorageClient storageClient = new StorageClient(trackerServer, null);
            // String orginalFilename="e://img//tom.jpg";
            String orginalFilename = file.getOriginalFilename();        //上传文件名称
            String extName = StringUtils.substringAfterLast(orginalFilename, ".");
            String[] upload_file = storageClient.upload_file(file.getBytes(), extName, null);
            for (int i = 0; i < upload_file.length; i++) {
                String path = upload_file[i];
                imgUrl += "/" + path;
                //System.out.println("s = " + s);
            }
        }

        return imgUrl;
    }
}
