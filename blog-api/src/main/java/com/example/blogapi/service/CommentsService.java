package com.example.blogapi.service;

import com.example.blogapi.vo.Result;
import com.example.blogapi.vo.params.CommentParam;

public interface CommentsService {
    /**
     * 根据文章id查询评论列表
     * @param id
     * @return
     */
    Result commentsByArticleId(Long id);

    Result comment(CommentParam commentParam);
}
