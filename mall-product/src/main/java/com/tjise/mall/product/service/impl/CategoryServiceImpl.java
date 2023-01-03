package com.tjise.mall.product.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.tjise.mall.product.service.CategoryBrandRelationService;
import com.tjise.mall.product.vo.Catalog2VO;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
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
    @Autowired
    private RedissonClient redissonClient;

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
     *
     * @CacheEvict  缓存失效模式
     * 1、同时进行多种缓存操作     @Caching
     * 2、删除某个分区下的所有数据   @CacheEvict(value = "category", allEntries = true)
     * 3、存储同一类型的数据，都可以指定成同一个分区，分区名默认就是缓存的前缀
     * @param category
     */
//    @CacheEvict(value = {"category"}, key = "'getLevel1CateGorys'")
//    @Caching(evict = {
//            @CacheEvict(value = {"category"}, key = "'getLevel1CateGorys'"),
//            @CacheEvict(value = {"category"}, key = "'getCatalogJson'")
//    })
    @CacheEvict(value = "category", allEntries = true)  //失效模式
//    @CachePut   //双写模式使用这个注解
    @Transactional
    @Override
    public void updateCascade(CategoryEntity category) {
        this.updateById(category);
        categoryBrandRelationService.updateCategory(category.getCatId(),category.getName());

        //同时修改缓存中得数据
        //redis.del("catalogJSON");等待下次主动查询更新
    }

    /**
     * 1、每一个需要缓存得数据我们都来指定要放到哪个名字得缓存。【缓存的分区（按照业务类型）】
     * 2、@Cacheable({"category"})
     *      代表当前方法得结果需要缓存，如果缓存中有，方法不用调用
     *      如果缓存中没有，会调用方法，最后将方法得结果放入缓存
     * 3)、默认行为
     *      1）、如果缓存中有数据，方法不被调用
     *      2）、key默认自动生成，缓存的名字::SimpleKey【】(自主生成的key指)
     *      3）、缓存的value值，默认使用jdk序列化机制，将序列化后的数据存到redis
     *      4）、默认ttl时间，-1
     *
     *      自定义：
     *          1）、指定生成的缓存使用的key    key属性指定，接收一个SpEL
     *          2）、指定缓存的数据的存活时间     配置文件中修改ttl
     *          3）、将数据保存为JSON格式:
     *
     *4)、spring-cache的不足
     *      1）、读模式：
     *          缓存穿透：查询一个null数据，解决：缓存空数据：cache-null-values=true
     *          缓存击穿：大量并发进来同时查询一个正好过期的数据。解决：加锁 ？ 默认时无加锁的;sync = true(加锁，解决击穿)
     *          缓存雪崩：大量的key同时过期。解决：加随机时间。spring.cache.redis.time-to-live=3600000
     *      2）、写模式：（缓存与数据库一直）
     *          1）、读写枷锁。
     *          2）、引入Canal，感知到MySQL的更新去更新缓存
     *          3）、读多写少，直接去缓存查询就行
     *
     *     总结：
     *          常规数据（读多写少，即时性，一致性要求不高的数据）：完全可以使用Spring-Cache负责缓存的读写;写模式（只要缓存的数据有过期时间就足够了）
     *
     *          特殊数据：特殊设计
     *
     *     原理：
     *          CacheManager（RedisCacheManager）->Cache（RedisCache）->Cache负责缓存读写
     * @return
     */
    @Cacheable(value = {"category"}, key = "#root.method.name", sync = true)
    @Override
    public List<CategoryEntity> getLevel1CateGorys() {
        System.out.println("getLevelCategorys....");
        long l = System.currentTimeMillis();
        List<CategoryEntity> categoryEntities = baseMapper.selectList(new QueryWrapper<CategoryEntity>().eq("parent_cid", 0));
        return categoryEntities;
    }

    @Cacheable(value = {"category"}, key = "#root.method.name")
    @Override
    public Map<String, List<Catalog2VO>> getCatalogJson() {
        System.out.println("查询了数据库");
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
                    if (null != level3Catelog) {
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

        return parent_cid;
    }

//    @Override
    public Map<String, List<Catalog2VO>> getCatalogJson2() {
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
            Map<String, List<Catalog2VO>> catalogJsonFromDb = getCatalogJsonFromDbWithRedissonLock();
            return catalogJsonFromDb;
        }

        System.out.println("缓存命中。。。。直接返回");
        //转位我们指定的对象
        Map<String, List<Catalog2VO>> result = JSON.parseObject(catalogJSON, new TypeReference<Map<String, List<Catalog2VO>>>(){});

        return result;
    }

    /**
     * 缓存里面得数据如何和数据库保持一致
     * 缓存数据一致性
     * 1)、双写模式
     * 2)、失效模式
     * @return
     */
    public Map<String, List<Catalog2VO>> getCatalogJsonFromDbWithRedissonLock(){

        //1.锁的名字。锁的粒度，越细越快。
        //锁的粒度，具体缓存的是某个数据，11-好商品； product-11-lock product-12-lock
        RLock lock = redissonClient.getLock("catalogJson-lock");
        lock.lock();

        Map<String, List<Catalog2VO>> dataFromDb;
        try {
            dataFromDb = getDataFromDb();
        }finally {
            lock.unlock();
        }
        return dataFromDb;
    }

    public Map<String, List<Catalog2VO>> getCatalogJsonFromDbWithRedisLock(){
        //1.占分布式锁，去redis占坑
        String uuid = UUID.randomUUID().toString();
        Boolean lock = redisTemplate.opsForValue().setIfAbsent("lock", uuid, 300, TimeUnit.SECONDS);
        if (lock){
            System.out.println("获取分布式锁成功。。。。。");
            //加锁成功...执行业务
            //2.设置过期时间，必须和加锁是同步的，原子的
//            redisTemplate.expire("lock", 30, TimeUnit.SECONDS);
            Map<String, List<Catalog2VO>> dataFromDb;
            try {
                dataFromDb = getDataFromDb();
            }finally {
                String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
                //删除锁
                Long lock1 = redisTemplate.execute(new DefaultRedisScript<Long>(script, Long.class),
                        Arrays.asList("lock"),
                        uuid);
            }

//            redisTemplate.delete("lock");//删除锁

            //获取值对比，对比成功删除。原子操作，lua脚本解锁
//            String lockValue = redisTemplate.opsForValue().get("lock");
//            if (uuid.equals(lockValue)){
//                //删除自己的锁
//                redisTemplate.delete("lock");
//            }
            return dataFromDb;
        }else {
            //加锁失败...重试。synchronized()
            //休眠100ms重试
            System.out.println("获取分布式锁失败。。。。。等待重试");
            try {
                Thread.sleep(200);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return getCatalogJsonFromDbWithRedisLock();//自旋的方式
        }
    }

    private Map<String, List<Catalog2VO>> getDataFromDb() {
        //得到锁以后，我们应该再去缓存中确定一次，如果没有才需要继续查询
        //TODO 本地锁，synchronized,JUC(lock)只能锁当前进程资源，在分布式情况下需要使用分布式锁
        String catalogJSON = redisTemplate.opsForValue().get("catalogJSON");
        if (!StringUtils.isEmpty(catalogJSON)) {
            Map<String, List<Catalog2VO>> result = JSON.parseObject(catalogJSON, new TypeReference<Map<String, List<Catalog2VO>>>() {
            });
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
                    if (null != level3Catelog) {
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

    /**
     * 从数据库查询并封装数据
     * @return
     */
    public Map<String, List<Catalog2VO>> getCatalogJsonFromDbWithLocalLock() {

        //只要是同一把锁，就能锁住，需要这个锁的所有线程
        //1。synchronized（this），SpringBoot所有的组件在容器中都是单例的。
        synchronized (this){
            //得到锁以后，我们应该再去缓存中确定一次，如果没有才需要继续查询
            //TODO 本地锁，synchronized,JUC(lock)只能锁当前进程资源，在分布式情况下需要使用分布式锁
            return getDataFromDb();
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