package com.tjise.mall.search.feign;

import com.tjise.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * @auther 刘子敬
 * @create 2023-02-23-15:43
 */
@FeignClient("mall-product")
public interface ProductFeighService {

    @GetMapping("/product/attr/info/{attrId}")
    public R attrInfo(@PathVariable("attrId") Long attrId);
}
