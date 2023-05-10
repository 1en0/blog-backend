package com.example.blogapi.service;

import com.example.blogapi.vo.Result;
import com.example.blogapi.vo.params.ArticleParam;
import com.example.blogapi.vo.params.PageParams;

public interface ArticleService {
    /**
     * 分页查询 文章列表
     * @param pageParams
     * @return
     */
    Result listArticle(PageParams pageParams);

    Result hotArticle(int limit);

    Result newArticles(int limit);

    Result listArchives();

    Result findArticleById(Long articleId);

    /**
     * 文章发布服务
     * @param articleParam
     * @return
     */
    Result publish(ArticleParam articleParam);
}
