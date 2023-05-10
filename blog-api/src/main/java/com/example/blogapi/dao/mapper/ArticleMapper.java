package com.example.blogapi.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.blogapi.dao.dos.Archives;
import com.example.blogapi.dao.pojo.Article;

import java.util.List;

public interface ArticleMapper extends BaseMapper<Article> {

    List<Archives> listArchives();
}
