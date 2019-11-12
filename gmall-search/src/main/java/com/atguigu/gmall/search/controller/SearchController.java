package com.atguigu.gmall.search.controller;

import com.atguigu.gmall.search.service.SearchService;
import com.atguigu.gmall.search.vo.SearchParamVO;
import com.atguigu.gmall.search.vo.SearchResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


/**
 * Created by FJC on 2019-11-11.
 */
@RestController
@RequestMapping("search")
public class SearchController {


    @Autowired
    private SearchService searchService;

    @GetMapping
    public SearchResponse search(SearchParamVO searchParamVO){
        SearchResponse searchResponse = this.searchService.search(searchParamVO);
        return searchResponse;
    }

}
