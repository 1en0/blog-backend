package com.example.blogapi.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.blogapi.dao.dos.Archives;
import com.example.blogapi.dao.mapper.ArticleBodyMapper;
import com.example.blogapi.dao.mapper.ArticleMapper;
import com.example.blogapi.dao.mapper.ArticleTagMapper;
import com.example.blogapi.dao.pojo.Article;
import com.example.blogapi.dao.pojo.ArticleBody;
import com.example.blogapi.dao.pojo.ArticleTag;
import com.example.blogapi.dao.pojo.SysUser;
import com.example.blogapi.service.*;
import com.example.blogapi.utils.UserThreadLocal;
import com.example.blogapi.vo.ArticleBodyVo;
import com.example.blogapi.vo.ArticleVo;
import com.example.blogapi.vo.Result;
import com.example.blogapi.vo.TagVo;
import com.example.blogapi.vo.params.ArticleParam;
import com.example.blogapi.vo.params.PageParams;
import org.apache.ibatis.annotations.Mapper;
import org.joda.time.DateTime;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ArticleServiceImpl implements ArticleService {
    @Autowired
    private ArticleMapper articleMapper;
    @Autowired
    private TagService tagService;
    @Autowired
    private SysUserService sysUserService;
    @Autowired
    private ArticleTagMapper articleTagMapper;
    @Override
    public Result listArticle(PageParams pageParams) {
        /**
         * 分页查询article数据库表
         */
        Page<Article> page = new Page<>(pageParams.getPage(), pageParams.getPageSize());
        LambdaQueryWrapper<Article> queryWrapper = new LambdaQueryWrapper<>();
        if (pageParams.getCategoryId() != null) {
            queryWrapper.eq(Article::getCategoryId,pageParams.getCategoryId());
        }
        List<Long> articleIdList = new ArrayList<>();
        if (pageParams.getTagId() != null){
            //article表中没有tag字段，需要在article_tag表中查询
            LambdaQueryWrapper<ArticleTag> articleTagLambdaQueryWrapper = new LambdaQueryWrapper<>();
            articleTagLambdaQueryWrapper.eq(ArticleTag::getTagId,pageParams.getTagId());
            List<ArticleTag> articleTags = articleTagMapper.selectList(articleTagLambdaQueryWrapper);
            for (ArticleTag articleTag : articleTags) {
                articleIdList.add(articleTag.getArticleId());
            }
            if (articleIdList.size() > 0){
                queryWrapper.in(Article::getId,articleIdList);
            }
        }
        //按是否置顶和创建时间进行排序
        queryWrapper.orderByDesc(Article::getWeight, Article::getCreateDate);
        Page<Article> articlePage = articleMapper.selectPage(page, queryWrapper);
        List<Article> records = articlePage.getRecords();
        //不能直接返回
        List<ArticleVo> articleVoList = copyList(records, true, true);
        return Result.success(articleVoList);
    }

    @Override
    public Result hotArticle(int limit) {
        LambdaQueryWrapper<Article> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.orderByDesc(Article::getViewCounts);
        queryWrapper.select(Article::getId,Article::getTitle);
        queryWrapper.last("limit " + limit);
        List<Article> articles = articleMapper.selectList(queryWrapper);
        return Result.success(copyList(articles,false,false));
    }

    @Override
    public Result newArticles(int limit) {
        LambdaQueryWrapper<Article> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.orderByDesc(Article::getCreateDate);
        queryWrapper.select(Article::getId,Article::getTitle);
        queryWrapper.last("limit "+limit);
        //select id,title from article order by create_date desc limit 5
        List<Article> articles = articleMapper.selectList(queryWrapper);

        return Result.success(copyList(articles,false,false));
    }

    @Override
    public Result listArchives() {
        List<Archives> archivesList = articleMapper.listArchives();
        return Result.success(archivesList);
    }

    @Autowired
    private ThreadService threadService;
    @Override
    public Result findArticleById(Long articleId) {
        /*
         * 1.根据id查询文章信息
         * 2.根据body id和category id做关联查询
         */
        Article article = this.articleMapper.selectById(articleId);
        ArticleVo articleVo = copy(article, true, true, true, true);
        //查看完文章增加阅读数？
        //查看完文章后，本应返回数据时，做更新操作的话更新时加写锁会阻塞其他的读操作，降低性能
        //更新增加了接口的耗时，如果更新出问题，不能影响查看文章的操作
        //线程池 可以把更新操作扔到线程池中去执行，和主线程就不相关了
        threadService.updateArticleViewCount(articleMapper, article);
        return Result.success(articleVo);
    }

    @Override
    public Result publish(ArticleParam articleParam) {
        /*
         * 1.发布文章目的是构建Article对象
         * 2.作者id，当前的登录用户
         * 3.拿到标签 将标签和文章的id加入到关联列表中
         * 4.body内容存储
         */
        SysUser sysUser = UserThreadLocal.get();

        Article article = new Article();
        article.setAuthorId(sysUser.getId());
        article.setCategoryId(Long.parseLong(articleParam.getCategory().getId()));
        article.setCreateDate(System.currentTimeMillis());
        article.setCommentCounts(0);
        article.setSummary(articleParam.getSummary());
        article.setTitle(articleParam.getTitle());
        article.setViewCounts(0);
        article.setWeight(Article.Article_Common);
        article.setBodyId(-1L);
        this.articleMapper.insert(article);
        //tags
        List<TagVo> tags = articleParam.getTags();
        if (tags != null) {
            for (TagVo tag : tags) {
                ArticleTag articleTag = new ArticleTag();
                articleTag.setArticleId(article.getId());
                articleTag.setTagId(Long.parseLong(tag.getId()));
                this.articleTagMapper.insert(articleTag);
            }
        }
        ArticleBody articleBody = new ArticleBody();
        articleBody.setContent(articleParam.getBody().getContent());
        articleBody.setContentHtml(articleParam.getBody().getContentHtml());
        articleBody.setArticleId(article.getId());
        articleBodyMapper.insert(articleBody);

        article.setBodyId(articleBody.getId());
        articleMapper.updateById(article);
        Map<String, String> map = new HashMap<>();
        map.put("id", article.getId().toString());
        return Result.success(map);
    }

    private List<ArticleVo> copyList(List<Article> records, boolean hasTag, boolean hasAuthor) {
        List<ArticleVo> articleVoList = new ArrayList<>();
        for(Article record : records){
            articleVoList.add(copy(record, hasTag, hasAuthor, false, false));
        }
        return articleVoList;
    }

    private List<ArticleVo> copyList(List<Article> records, boolean hasTag, boolean hasAuthor, boolean hasBody, boolean hasCategory) {
        List<ArticleVo> articleVoList = new ArrayList<>();
        for(Article record : records){
            articleVoList.add(copy(record, hasTag, hasAuthor, hasBody, hasCategory));
        }
        return articleVoList;
    }

    @Autowired
    private CategoryService categoryService;

    private ArticleVo copy(Article article, boolean hasTag, boolean hasAuthor, boolean hasBody, boolean hasCategory){
        ArticleVo articleVo = new ArticleVo();
        BeanUtils.copyProperties(article, articleVo);
        articleVo.setId(String.valueOf(article.getId()));
        //articleVo.setAuthor("aaa");
        articleVo.setCreateDate(new DateTime(article.getCreateDate()).toString("yyyy-MM-dd HH:mm"));
        if(hasTag){
            Long articleId = article.getId();
            articleVo.setTags(tagService.findTagsByArticleId(articleId));
        }
        if(hasAuthor){
            Long authorId = article.getAuthorId();
            articleVo.setAuthor(sysUserService.findUserById(authorId).getNickname());
        }
        if(hasBody){
            Long bodyId = article.getBodyId();
            articleVo.setBody(findArticleBodyById(bodyId));
        }
        if(hasCategory){
            Long categoryId = article.getCategoryId();
            articleVo.setCategory(categoryService.findCategoryById(categoryId));
        }
        return articleVo;
    }

    @Autowired
    private ArticleBodyMapper articleBodyMapper;
    private ArticleBodyVo findArticleBodyById(Long bodyId) {
        ArticleBody articleBody = articleBodyMapper.selectById(bodyId);
        ArticleBodyVo articleBodyVo = new ArticleBodyVo();
        articleBodyVo.setContent(articleBody.getContent());
        return articleBodyVo;
    }


}
