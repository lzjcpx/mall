package com.tjise.mall.product.feign;

import com.tjise.common.utils.R;
import com.tjise.mall.product.vo.SkuHasStockVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * @auther 刘子敬
 * @create 2022-10-10-15:58
 */
@FeignClient("mall-ware")
public interface WareFeignService {

    @PostMapping("/ware/waresku/hasStock")
    R getSkuHasStock(@RequestBody List<Long> skuIds);

}
