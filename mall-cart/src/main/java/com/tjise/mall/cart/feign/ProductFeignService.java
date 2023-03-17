package com.tjise.mall.cart.feign;

import com.tjise.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

/**
 * @auther 刘子敬
 * @create 2023-03-17-14:35
 */
@FeignClient("mall-product")
public interface ProductFeignService {

    @RequestMapping("/product/skuinfo/info/{skuId}")
    //@RequiresPermissions("product:skuinfo:info")
    R getSkuInfo(@PathVariable("skuId") Long skuId);

    @GetMapping("/product/skusaleattrvalue/stringlist/{skuId}")
    List<String> getSkuSaleAttrValue(@PathVariable("skuId") Long skuId);

}
