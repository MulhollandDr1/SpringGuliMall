package com.example.gulimall.search.service;

import com.example.gulimall.search.vo.SearchParameterVo;
import com.example.gulimall.search.vo.SearchResultVo;

public interface MallSearchService {
    SearchResultVo search(SearchParameterVo searchParameterVo);
}
