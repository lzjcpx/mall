package com.tjise.mall.cart.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.tjise.common.utils.R;
import com.tjise.mall.cart.feign.ProductFeignService;
import com.tjise.mall.cart.interceptor.CartInterceptor;
import com.tjise.mall.cart.service.CartService;
import com.tjise.mall.cart.to.UserInfoTo;
import com.tjise.mall.cart.vo.Cart;
import com.tjise.mall.cart.vo.CartItem;
import com.tjise.mall.cart.vo.SkuInfoVo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

/**
 * @auther 刘子敬
 * @create 2023-03-16-16:31
 */
@Slf4j
@Service
public class CartServiceImpl implements CartService {

    @Autowired
    StringRedisTemplate redisTemplate;

    @Autowired
    ProductFeignService productFeignService;

    @Autowired
    ThreadPoolExecutor executor;

    private final String CART_PREFIX = "mymall:cart:";

    @Override
    public CartItem addToCart(Long skuId, Integer num) throws ExecutionException, InterruptedException {
        BoundHashOperations<String, Object, Object> cartOps = getCartOps();
        String result = (String) cartOps.get(skuId.toString());
        if (StringUtils.isEmpty(result)) {
            CartItem cartItem = new CartItem();
            //购物车无次商品
            //2、添加新商品到购物车
            CompletableFuture<Void> getSkuInfoTask = CompletableFuture.runAsync(() -> {
                //1、远程查询当前要查询的商品的信息
                R skuInfo = productFeignService.getSkuInfo(skuId);
                SkuInfoVo data = skuInfo.getData("skuInfo", new TypeReference<SkuInfoVo>() {
                });

                cartItem.setCheck(true);
                cartItem.setCount(num);
                cartItem.setImage(data.getSkuDefaultImg());
                cartItem.setTitle(data.getSkuTitle());
                cartItem.setSkuId(skuId);
                cartItem.setPrice(data.getPrice());
            }, executor);

            //3、远程查询sku的组合信息
            CompletableFuture<Void> getSkuSaleAttrValues = CompletableFuture.runAsync(() -> {
                List<String> values = productFeignService.getSkuSaleAttrValue(skuId);
                cartItem.setSkuAttr(values);
            }, executor);

            CompletableFuture.allOf(getSkuInfoTask, getSkuSaleAttrValues).get();
            String s = JSON.toJSONString(cartItem);
            cartOps.put(skuId.toString(), s);
            return cartItem;
        } else {
            //购物车有此商品，修改数量即可
            CartItem cartItem = JSON.parseObject(result, CartItem.class);
            cartItem.setCount(cartItem.getCount() + num);

            cartOps.put(skuId.toString(), JSON.toJSONString(cartItem));
            return cartItem;
        }
    }

    /**
     * 获取购物车中某个购物项
     * @param skuId
     * @return
     */
    @Override
    public CartItem getCartItem(Long skuId) {
        BoundHashOperations<String, Object, Object> cartOps = getCartOps();
        String str = (String) cartOps.get(skuId.toString());
        CartItem item = JSON.parseObject(str, CartItem.class);
        return item;
    }

    /**
     * 获取整个购物车
     * @return
     */
    @Override
    public Cart getcart() throws ExecutionException, InterruptedException {
        Cart cart = new Cart();
        UserInfoTo userInfoTo = CartInterceptor.threadLocal.get();
        if (userInfoTo.getUserId() != null) {
            //1、登录
            String cartKey = CART_PREFIX + userInfoTo.getUserId();
            String tempCartKey = CART_PREFIX + userInfoTo.getUserKey();
            //2、如果临时购物车的数据还没合并【合并购物车】
            List<CartItem> tempCartItems = getCartItems(tempCartKey);
            if (tempCartItems != null) {
                //临时购物车有数据，需要合并
                for (CartItem item : tempCartItems) {
                    addToCart(item.getSkuId(), item.getCount());
                }
                //清楚临时购物车的数据
                clearCart(tempCartKey);
            }

            //3、获取登陆后的购物车的数据【包含合并过来的临时购物车的数据，和登陆后的购物车的数据】
            List<CartItem> cartItems = getCartItems(cartKey);
            cart.setItems(cartItems);
        } else {
            //2、没登录
            String cartKey = CART_PREFIX + userInfoTo.getUserKey();
            //获取临时购物车的所有购物项
            List<CartItem> cartItems = getCartItems(cartKey);
            cart.setItems(cartItems);
        }
        return cart;
    }

    /**
     * 获取到我们要操作的购物车
     * @return
     */
    private BoundHashOperations<String, Object, Object> getCartOps() {
        UserInfoTo userInfoTo = CartInterceptor.threadLocal.get();

        String cartKey = "";
        if (userInfoTo.getUserId() != null) {
            //mymall:cart:1
            cartKey = CART_PREFIX + userInfoTo.getUserId();
        } else {
            cartKey = CART_PREFIX + userInfoTo.getUserKey();
        }

        BoundHashOperations<String, Object, Object> operations = redisTemplate.boundHashOps(cartKey);
        return operations;
    }

    private List<CartItem> getCartItems(String cartKey) {
        BoundHashOperations<String, Object, Object> hashOps = redisTemplate.boundHashOps(cartKey);
        List<Object> values = hashOps.values();
        if (values != null && values.size() > 0) {
            List<CartItem> collect = values.stream().map((obj) -> {
                String str = (String)obj;
                CartItem item = JSON.parseObject(str, CartItem.class);
                return item;
            }).collect(Collectors.toList());
            return collect;
        }
        return null;
    }

    @Override
    public void clearCart(String cartKey) {
        redisTemplate.delete(cartKey);
    }

    /**
     * 勾选购物项
     * @param skuId
     * @param check
     */
    @Override
    public void checkItem(Long skuId, Integer check) {
        BoundHashOperations<String, Object, Object> cartOps = getCartOps();
        CartItem cartItem = getCartItem(skuId);
        cartItem.setCheck(check == 1 ? true : false);
        String s = JSON.toJSONString(cartItem);
        cartOps.put(skuId.toString(), s);
    }
}
