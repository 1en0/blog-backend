package com.example.blogapi.service;

import com.example.blogapi.vo.CategoryVo;
import com.example.blogapi.vo.Result;

public interface CategoryService {
    CategoryVo findCategoryById(Long categoryId);

    Result findAll();

    Result findAllDetail();

    Result categoryDetailById(Long id);
}
