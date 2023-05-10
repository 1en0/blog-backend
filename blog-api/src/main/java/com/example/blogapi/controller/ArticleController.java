package com.example.blogapi.controller;

import com.example.blogapi.common.aop.LogAnnotation;
import com.example.blogapi.common.cache.Cache;
import com.example.blogapi.service.ArticleService;
import com.example.blogapi.vo.ArticleVo;
import com.example.blogapi.vo.Result;
import com.example.blogapi.vo.params.ArticleParam;
import com.example.blogapi.vo.params.PageParams;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

//json数据进行交互
@RestController
@RequestMapping("articles")
public class ArticleController {
    //Result是统一结果返回
    @Autowired
    private ArticleService articleService;
    @PostMapping
    //加上此注解代表要对此接口记录日志
    //@LogAnnotation(module="文章", operator="获取文章列表")
    @Cache(expire = 5 * 60 * 1000,name = "list_article")
    public Result articles(@RequestBody PageParams pageParams) {
        //ArticleVo 页面接收的数据
        return articleService.listArticle(pageParams);
    }
    //首页最热文章
    @PostMapping("hot")
    @Cache(expire = 5 * 60 * 1000,name = "hot_article")
    public Result hotArticle(){
        int limit = 5;
        return articleService.hotArticle(limit);
    }
    /**
     * 首页 最新文章
     * @return
     */
    @PostMapping("new")
    @Cache(expire = 5 * 60 * 1000,name = "new_article")
    public Result newArticles(){
        int limit = 5;
        return articleService.newArticles(limit);
    }

    /**
     * 首页 最新文章
     * @return
     */
    @PostMapping("listArchives")
    public Result listArchives(){
        return articleService.listArchives();
    }

    @PostMapping("view/{id}")
    public Result findArticleById(@PathVariable("id") Long articleId){
        return articleService.findArticleById(articleId);
    }

    @PostMapping("publish")
    public Result publish(@RequestBody ArticleParam articleParam){
        return articleService.publish(articleParam);
    }

}
