package com.atguigu.gmall.search.service;

import com.atguigu.gmall.search.vo.SearchParamVO;
import com.atguigu.gmall.search.vo.SearchResponse;

/**
 * Created by FJC on 2019-11-11.
 */
public interface SearchService {
    SearchResponse search(SearchParamVO searchParamVO);
}
