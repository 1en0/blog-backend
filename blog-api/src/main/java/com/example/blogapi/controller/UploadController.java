package com.example.blogapi.controller;
import org.apache.commons.lang3.StringUtils;
import com.example.blogapi.vo.Result;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RestController
@RequestMapping("upload")
public class UploadController {
    public Result upload(@RequestParam("image") MultipartFile file){
        //原始文件名称
        String originalFilename = file.getOriginalFilename();
        //生成唯一的文件名称
        String fileName = UUID.randomUUID().toString() + "." + StringUtils.substringAfterLast(originalFilename, ".");
        //文件上传到哪里？
        return null;
    }
}
