package com.tjise.mall.product.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.tjise.mall.product.service.CategoryBrandRelationService;
import com.tjise.mall.product.vo.Catalog2VO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tjise.common.utils.PageUtils;
import com.tjise.common.utils.Query;

import com.tjise.mall.product.dao.CategoryDao;
import com.tjise.mall.product.entity.CategoryEntity;
import com.tjise.mall.product.service.CategoryService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;


@Service("categoryService")
public class CategoryServiceImpl extends ServiceImpl<CategoryDao, CategoryEntity> implements CategoryService {
//    @Autowired
//    CategoryDao categoryDao;

    @Autowired
    CategoryBrandRelationService categoryBrandRelationService;
    @Autowired
    private StringRedisTemplate redisTemplate;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<CategoryEntity> page = this.page(
                new Query<CategoryEntity>().getPage(params),
                new QueryWrapper<CategoryEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public List<CategoryEntity> listWithTree() {
        //1、查出所有分类
        List<CategoryEntity> entities = baseMapper.selectList(null);

        //2、组装成父子的树形结构

        //2.1）、找到所有的一级分类
        List<CategoryEntity> level1Menus = entities.stream().filter(categoryEntity ->
             categoryEntity.getParentCid() == 0
        ).map((menu)->{
            menu.setChildren(getChildrens(menu,entities));
            return menu;
        }).sorted((menu1,menu2)->{
            return (menu1.getSort()==null?0:menu1.getSort()) - (menu2.getSort()==null?0:menu2.getSort());
        }).collect(Collectors.toList());




        return level1Menus;
    }

    @Override
    public void removeMenuByIds(List<Long> asList) {
        //TODO  1、检查当前删除的菜单，是否被别的地方引用

        //逻辑删除
        baseMapper.deleteBatchIds(asList);
    }

    //[2,25,225]
    @Override
    public Long[] findCatelogPath(Long catelogId) {
        List<Long> paths = new ArrayList<>();
        List<Long> parentPath = findParentPath(catelogId, paths);

        Collections.reverse(parentPath);


        return parentPath.toArray(new Long[parentPath.size()]);
    }

    /**
     * 级联更新所有关联的数据
     * @param category
     */
    @Transactional
    @Override
    public void updateCascade(CategoryEntity category) {
        this.updateById(category);
        categoryBrandRelationService.updateCategory(category.getCatId(),category.getName());
    }

    @Override
    public List<CategoryEntity> getLevel1CateGorys() {
        List<CategoryEntity> categoryEntities = baseMapper.selectList(new QueryWrapper<CategoryEntity>().eq("parent_cid", 0));
        return categoryEntities;
    }

    @Override
    public Map<String, List<Catalog2VO>> getCatalogJson() {
        //给缓存中放JSON字符串，拿出的JSON字符串，还逆转为能用的对象类型，【序列化与反序列化】

        /**
         * 1,空结果缓存，解决缓存穿透、
         * 2.设置过期时间（加随机值），解决缓存雪崩
         * 3.枷锁，解决缓存击穿
         */
        //1.加入缓存逻辑，缓存中存的数据是JSON字符串
        //JSON跨语言，跨平台兼容
        String catalogJSON = redisTemplate.opsForValue().get("catalogJSON");
        if (StringUtils.isEmpty(catalogJSON)){
            //2.缓存中没有，查询数据库
            System.out.println("缓存不命中。。。。将要查询数据库");
            Map<String, List<Catalog2VO>> catalogJsonFromDb = getCatalogJsonFromDb();
            return catalogJsonFromDb;
        }

        System.out.println("缓存命中。。。。直接返回");
        //转位我们指定的对象
        Map<String, List<Catalog2VO>> result = JSON.parseObject(catalogJSON, new TypeReference<Map<String, List<Catalog2VO>>>(){});

        return result;
    }

    /**
     * 从数据库查询并封装数据
     * @return
     */
    public Map<String, List<Catalog2VO>> getCatalogJsonFromDb() {

        //只要是同一把锁，就能锁住，需要这个锁的所有线程
        //1。synchronized（this），SpringBoot所有的组件在容器中都是单例的。
        synchronized (this){
            //得到锁以后，我们应该再去缓存中确定一次，如果没有才需要继续查询
            //TODO 本地锁，synchronized,JUC(lock)只能锁当前进程资源，在分布式情况下需要使用分布式锁
            String catalogJSON = redisTemplate.opsForValue().get("catalogJSON");
            if (!StringUtils.isEmpty(catalogJSON)){
                Map<String, List<Catalog2VO>> result = JSON.parseObject(catalogJSON, new TypeReference<Map<String, List<Catalog2VO>>>(){});
                return result;
            }
            System.out.println("查询了数据库。。。。。");

            /**
             * 优化：
             * 1.将数据库里的多次查询变为一次
             */
            List<CategoryEntity> selectList = baseMapper.selectList(null);

            //1.查出所有1级分类
            List<CategoryEntity> level1CateGorys = getParent_cid(selectList, 0L);

            //2.封装数据
            Map<String, List<Catalog2VO>> parent_cid = level1CateGorys.stream().collect(Collectors.toMap(k -> k.getCatId().toString(), v -> {
                //1.每一个一级分类，查到这个一级分类的二级分类
                List<CategoryEntity> categoryEntities = getParent_cid(selectList, v.getCatId());

                List<Catalog2VO> catalog2VOS = null;
                if (null != categoryEntities) {
                    //2.封装上面的结果
                    catalog2VOS = categoryEntities.stream().map(l2 -> {
                        Catalog2VO catalog2VO = new Catalog2VO(v.getCatId().toString(), null, l2.getCatId().toString(), l2.getName());
                        //1.招当前二级分类的三级分类封装成vo
                        List<CategoryEntity> level3Catelog = getParent_cid(selectList, l2.getCatId());
                        if (null != level3Catelog){
                            List<Catalog2VO.Catalog3Vo> collect = level3Catelog.stream().map(l3 -> {
                                //2.封装成指定格式数据
                                Catalog2VO.Catalog3Vo catalog3Vo = new Catalog2VO.Catalog3Vo(l2.getCatId().toString(), l3.getCatId().toString(), l3.getName());
                                return catalog3Vo;
                            }).collect(Collectors.toList());
                            catalog2VO.setCatalog3List(collect);
                        }
                        return catalog2VO;
                    }).collect(Collectors.toList());

                }
                return catalog2VOS;
            }));

            //3.查到的数据再放入缓存，将对象转为JSON放在缓存中
            String s = JSON.toJSONString(parent_cid);
            redisTemplate.opsForValue().set("catalogJSON", s, 1, TimeUnit.DAYS);
            return parent_cid;
        }
    }

    private List<CategoryEntity> getParent_cid(List<CategoryEntity> selectList, Long parent_cid) {
        List<CategoryEntity> collect = selectList.stream().filter(item -> item.getParentCid() == parent_cid).collect(Collectors.toList());
        return collect;
    }

    //225,25,2
    private List<Long> findParentPath(Long catelogId,List<Long> paths){
        //1、收集当前节点id
        paths.add(catelogId);
        CategoryEntity byId = this.getById(catelogId);
        if(byId.getParentCid()!=0){
            findParentPath(byId.getParentCid(),paths);
        }
        return paths;

    }


    //递归查找所有菜单的子菜单
    private List<CategoryEntity> getChildrens(CategoryEntity root,List<CategoryEntity> all){

        List<CategoryEntity> children = all.stream().filter(categoryEntity -> {
            return categoryEntity.getParentCid() == root.getCatId();
        }).map(categoryEntity -> {
            //1、找到子菜单
            categoryEntity.setChildren(getChildrens(categoryEntity,all));
            return categoryEntity;
        }).sorted((menu1,menu2)->{
            //2、菜单的排序
            return (menu1.getSort()==null?0:menu1.getSort()) - (menu2.getSort()==null?0:menu2.getSort());
        }).collect(Collectors.toList());

        return children;
    }



}