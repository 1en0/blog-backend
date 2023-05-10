package com.example.blogapi.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.blogapi.dao.mapper.ArticleMapper;
import com.example.blogapi.dao.pojo.Article;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class ThreadService {
    //期望此操作在线程池中执行，不影响原有的主线程
    @Async("taskExecutor")
    public void updateArticleViewCount(ArticleMapper articleMapper, Article article) {
        int viewCounts = article.getViewCounts();
        Article articleUpdate = new Article();
        articleUpdate.setViewCounts(article.getViewCounts() + 1);
        LambdaQueryWrapper<Article> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Article::getId,article.getId());
        //为了线程安全
        queryWrapper.eq(Article::getViewCounts,article.getViewCounts());
        articleMapper.update(articleUpdate,queryWrapper);
    }
}
