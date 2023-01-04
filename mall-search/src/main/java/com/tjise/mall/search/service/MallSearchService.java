package com.tjise.mall.search.service;

import com.tjise.mall.search.vo.SearchParam;
import com.tjise.mall.search.vo.SearchResult;

/**
 * @auther 刘子敬
 * @create 2023-01-04-11:45
 */
public interface MallSearchService {

    /**
     *
     * @param param 检索的所有参数
     * @return 返回的检索结果，里面包含页面的所有信息
     */
    SearchResult search(SearchParam param);

}
