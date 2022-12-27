package com.tjise.mall.product.web;

import com.tjise.mall.product.entity.CategoryEntity;
import com.tjise.mall.product.service.CategoryService;
import com.tjise.mall.product.vo.Catalog2VO;
import org.redisson.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * @auther 刘子敬
 * @create 2022-10-11-13:57
 */
@Controller
public class IndexController {

    @Autowired
    CategoryService categoryService;
    @Autowired
    RedissonClient redisson;
    @Autowired
    StringRedisTemplate redisTemplate;

    @GetMapping({"/", "index,html"})
    public String indexPage(Model model){

        //TODO 1、查出所有的1级分类
        List<CategoryEntity> categoryEntities = categoryService.getLevel1CateGorys();

        model.addAttribute("categorys", categoryEntities);
        return "index";
    }

    //index/catalog.json
    @ResponseBody
    @GetMapping("index/catalog.json")
    public Map<String, List<Catalog2VO>> getCatalogJson(){
        Map<String, List<Catalog2VO>> map = categoryService.getCatalogJson();
        return map;
    }

    @ResponseBody
    @GetMapping("/hello")
    public String hello(){
        //1.获取一把锁，只要锁的名字一样就是同一把锁
        RLock lock = redisson.getLock("my-lock");

        //2.加锁
//        lock.lock();
        //问题，lock.lock();在锁时间过期后
        //1）锁的自动续期，如果业务超长没运行期间自动给锁续上新的30s，不用担心业务时间长，锁自动国企呗删掉
        //2）加锁的业务只要运行完成，就不会给当前锁续期，即使不手动解锁，锁默认在30s以后自动删除
        lock.lock(10, TimeUnit.SECONDS);//阻塞式等待，默认加的都是30s时间;若自己设定解锁时间，设置时间一定大于业务执行时间
        //1.如果我们传递了锁的超时时间，就发送给redis执行脚本，进行占锁，默认超时时间就是我们指定的时间
        //2.如果我们未指定锁的超时时间，就是用30*1000;【lockWatchdogTimeout看门口狗的默认事件】
        //  只要占锁成功，就会启动一个定时任务【重新给锁设置过期时间，新的过期时间就是看门狗的默认时间】
        //  internallockleaseTime【看门狗时间】 / 3，10s

        //最佳实战
        //1）、lock.lock(30, TimeUnit,SECONDS);省掉了整个续期操作，手动解锁
        try {
            System.out.println("加锁成功，执行业务...." + Thread.currentThread().getId());
            Thread.sleep(30000);
        }catch (Exception e) {

        }finally {
            //3.解锁  假设解锁代码没有运行，redisson会不会死锁
            System.out.println("释放锁...." + Thread.currentThread().getId());
            lock.unlock();
        }

        return "hello";
    }

    //保证一定能读到最新数据，修改期间，写锁式一个排他锁（互斥锁），读锁是一个共享锁
    //写锁没释放读锁必须等待
    @GetMapping("/write")
    @ResponseBody
    public String wirteValue(){
        RReadWriteLock lock = redisson.getReadWriteLock("rw-lock");
        String s = "";
        RLock rLock = lock.writeLock();
        try {
            //1.该数据加写锁，读数据加读锁
            rLock.lock();
            s = UUID.randomUUID().toString();
            Thread.sleep(30 * 1000);
            redisTemplate.opsForValue().set("writeValue", s);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            rLock.unlock();
        }
        return s;
    }

    @GetMapping("/read")
    @ResponseBody
    public String readValue(){
        RReadWriteLock lock = redisson.getReadWriteLock("rw-lock");
        RLock rLock = lock.readLock();
        String s = "";
        //加读锁
        rLock.lock();
        try {
            s = redisTemplate.opsForValue().get("writeValue");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            rLock.unlock();
        }
        return s;
    }

    /**
     * 车库停车
     * 3车位
     * 信号量也可以用作分布式限流
     */
    @GetMapping("park")
    @ResponseBody
    public String park() throws InterruptedException {
        RSemaphore park = redisson.getSemaphore("park");
//        park.acquire();//获取一个信号，获取一个值
        boolean b = park.tryAcquire();
        if (b){
            //执行业务
        }else {
            return "error";
        }
        return "ok" + b;
    }

    @GetMapping("go")
    @ResponseBody
    public String go() throws InterruptedException {
        RSemaphore park = redisson.getSemaphore("park");
        park.release();//释放一个车位

        return "ok";
    }
    /**
     *放假，锁门
     * 1班没人了，2
     * 5个班全部走完，我们可以锁大门
     */
    @GetMapping("lockDoor")
    @ResponseBody
    public String lockDoor() throws InterruptedException {
        RCountDownLatch door = redisson.getCountDownLatch("door");
        door.trySetCount(5);
        door.await();//等待闭锁都完成

        return "放假了。。。。。";
    }

    @GetMapping("/gogogo/{id}")
    @ResponseBody
    public String gogogo(@PathVariable("id") Long id){
        RCountDownLatch door = redisson.getCountDownLatch("door");
        door.countDown();//计数减1；
        return id + "班的人都走了";
    }

}
