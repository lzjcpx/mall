package com.tjise.mall.search.thread;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import java.util.concurrent.*;

/**
 * @auther 刘子敬
 * @create 2023-02-24-11:11
 */
public class ThreadTest {
    public static ExecutorService service = Executors.newFixedThreadPool(10);

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        System.out.println("main....start.....");
//        CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
//            System.out.println("当前线程：" + Thread.currentThread().getId());
//            int i = 10 / 2;
//            System.out.println("运行结果：" + i);
//        }, service);

        /**
         * 方法完成后的感知
         */
//        CompletableFuture<Integer> future = CompletableFuture.supplyAsync(() -> {
//            System.out.println("当前线程：" + Thread.currentThread().getId());
//            int i = 10 / 0;
//            System.out.println("运行结果：" + i);
//            return i;
//        }, service).whenComplete((res, exception) -> {
//            //虽然能得到异常信息，但是没办法修改返回数据
//            System.out.println("异步任务成功完成了...结果是：" + res + ";+异常是：" + exception);
//        }).exceptionally(exception -> {
//            //感知异常，同事返回默认值
//            return 10;
//        });

        /**
         * 方法执行完成后的处理
         */
//        CompletableFuture<Integer> future = CompletableFuture.supplyAsync(() -> {
//            System.out.println("当前线程：" + Thread.currentThread().getId());
//            int i = 10 / 4;
//            System.out.println("运行结果：" + i);
//            return i;
//        }, service).handle((res, thr) -> {
//            if (res != null) {
//                return res * 2;
//            }
//            if (thr != null) {
//                return 0;
//            }
//            return 0;
//        });

        /**
         * 线程串行化
         * 1）、thenRun：不能获取到上一步的执行结果，无返回值
         * .thenRunAsync(() -> {
         *             System.out.println("任务2启动了。。。。");
         *         }, service);
         * 2）、thenAcceptAsync能接收上一步结果，无返回值
         * .thenAcceptAsync(res -> {
         *             System.out.println("任务2启动了" + res);
         *         }, service);
         * 3）、thenApplyAsync能接收上一步结果与返回值
         */
//        CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> {
//            System.out.println("当前线程：" + Thread.currentThread().getId());
//            int i = 10 / 4;
//            System.out.println("运行结果：" + i);
//            return i;
//        }, service).thenApplyAsync(res -> {
//            System.out.println("任务2启动了..." + res);
//            return "Hello" + res;
//        }, service);
//        Integer integer = future.get();

        /**
         * 两个都完成
         */
//        CompletableFuture<Object> future01 = CompletableFuture.supplyAsync(() -> {
//            System.out.println("任务1线程线程：" + Thread.currentThread().getId());
//            int i = 10 / 4;
//            System.out.println("任务1线程运行结束：");
//            return i;
//        }, service);
//
//        CompletableFuture<Object> future02 = CompletableFuture.supplyAsync(() -> {
//            System.out.println("任务2线程线程：" + Thread.currentThread().getId());
//            try {
//                Thread.sleep(3000);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//            System.out.println("任务2线程运行结束：");
//            return "Hello";
//        }, service);

//        future01.runAfterBothAsync(future02,() -> {
//            System.out.println("任务3开始....");
//        }, service);

//        future01.thenAcceptBothAsync(future02, (f1, f2) -> {
//            System.out.println("任务3开始...之前的结果" + f1 + "-->" + f2);
//        }, service);

//        CompletableFuture<String> future = future01.thenCombineAsync(future02, (f1, f2) -> {
//            return f1 + ":" + f2 + "-->HaHa";
//        }, service);

        /**
         * 两个任务，只要有一个完成，就执行任务3
         * runAfterEitherAsync:不感知结果，自己也无返回值
         * acceptEitherAsync:感知结果，没有返回值
         * applyToEitherAsync:感知结果，有返回值
         */
//        future01.runAfterEitherAsync(future02 ,() -> {
//            System.out.println("任务3开始....之前的结果");
//        }, service);

//        future01.acceptEitherAsync(future02 ,(res) -> {
//            System.out.println("任务3开始....之前的结果:" + res);
//        }, service);

//        CompletableFuture<String> future = future01.applyToEitherAsync(future02, res -> res.toString() + "-->哈哈", service);

        CompletableFuture<String> futureImg = CompletableFuture.supplyAsync(() -> {
            System.out.println("查询商品的图片信息");
            return "hello.jpg";
        },service);

        CompletableFuture<String> futureAttr = CompletableFuture.supplyAsync(() -> {
            System.out.println("查询商品的属性");
            return "黑色+256G";
        }, service);

        CompletableFuture<String> futureDesc = CompletableFuture.supplyAsync(() -> {
            System.out.println("查询商品介绍");
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return "华为";
        }, service);

//        CompletableFuture<Void> allOf = CompletableFuture.allOf(futureImg, futureAttr, futureDesc);
        CompletableFuture<Object> anyOf = CompletableFuture.anyOf(futureImg, futureAttr, futureDesc);
        anyOf.get();//等待所有结果完成

//        future.get()是阻塞方法
        System.out.println("main....end....." + anyOf.get());
    }

    public void thread(String[] args) throws ExecutionException, InterruptedException {
        System.out.println("main....start.....");
        /**
         * 1)、继承Thread
         *         Thread01 thread = new Thread01();
         *         thread.start();
         * 2）、实现Runnable接口
         *         Runable01 runable01 = new Runable01();
         *         new Thread(runable01).start();
         * 3）、实现Callable接口 + FutureTask（可以拿到返回结果，可以处理异常）
         *         FutureTask<Integer> futureTask = new FutureTask<>(new Callable01());
         *         new Thread(futureTask).start();
         *         //阻塞等待整个线程执行完成，获取返回结果
         *         Integer integer = futureTask.get();
         * 4）、线程池[ExecutorService]
         *          给线程池直接提交任务。
         *          service.execute(new Runable01());
         *          1、创建：
         *              1）、Executors
         *              2）、submit
         *
         * 区别：
         *      1、2不能得到返回值。3、可以获取返回值
         *      1、2、3都不能控制资源
         *      4、可以控制资源，整个系统的性能稳定
         */

        //我们以后业务代码里面，以上三种启动线程的方式都不用，【将所有的多线程异步任务都交给线程池执行】
//        System.out.println("main....end....." + integer);

        //当前系统中池只有一两个，每个异步任务，提交给线程池让他自己执行
        /**
         * 七大参数
         * corePoolSize：核心线程数【一直存在，除非（allowCoreThreadTimeOut）】；线程池创建好以后就准备就绪的线程数量，就等待来接手异步任务去执行
         * maximumPoolSize：最大线程数；控制资源
         * keepAliveTime：存活时间；如果当前正在运行的线程数量大于核心数量。
         *      释放空闲的线程（maximumPoolSize - corePoolSize），只要线程空闲大于指定的存活时间
         * unit：时间单位
         * BlockingQueue<Runnable> workQueue：阻塞队列。如果任务有很多，就会将目前多的任务放在队列里面。
         *              只要有线程空闲，就会去队列里面取出新的任务继续执行
         * threadFactory：线程的创建工厂
         * RejectedExecutionHandler handler：如果队列满了，按照我们指定的拒绝策略执行任务
         *
         * 工作顺序：
         * 1）、线程池创建，准备好core数量的核心线程，准备接收任务
         * 2)、新的任务进来，用core准备好的空闲线程执行
         *      （1）、core满了，就将再进来的任务放入阻塞队列种，空闲的core就会去阻塞队列获取任务执行
         *      （2）、阻塞队列满了，就直接开新线程执行，最大只能开到max指定的数量
         *      （3）、max都执行满了就用RejectedExecutionHandler拒绝任务
         *      （4）、max都执行完了，有很多空闲，在指定的时间以后，释放max-core这些线程
         *
         *          new LinkedBlockingDeque<>():默认是integer的最大值，内存不够
         *
         */
        ThreadPoolExecutor executor = new ThreadPoolExecutor(5,
                200,
                10,
                TimeUnit.SECONDS,
                new LinkedBlockingDeque<>(100000),
                Executors.defaultThreadFactory(),
                new ThreadPoolExecutor.AbortPolicy());

//        Executors.newCachedThreadPool();核心线程数是0，所有都可回收
//        Executors.newFixedThreadPool();固定大小，coremax都不可回收
//        Executors.newScheduledThreadPool();定时任务的线程池
//        Executors.newSingleThreadExecutor();单线程的线程池，后台从队列中获取任务，一个接一个执行
        System.out.println("main....end.....");
    }

    public static class Thread01 extends Thread {
        @Override
        public void run() {
            System.out.println("当前线程：" + Thread.currentThread().getId());
            int i = 10 / 2;
            System.out.println("运行结果：" + i);
        }
    }

    public static class Runable01 implements Runnable {

        @Override
        public void run() {
            System.out.println("当前线程：" + Thread.currentThread().getId());
            int i = 10 / 2;
            System.out.println("运行结果：" + i);
        }
    }

    public static class Callable01 implements Callable<Integer> {

        @Override
        public Integer call() throws Exception {
            System.out.println("当前线程：" + Thread.currentThread().getId());
            int i = 10 / 2;
            System.out.println("运行结果：" + i);
            return i;
        }
    }

}
