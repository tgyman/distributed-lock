package com.tgy;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.apache.curator.retry.ExponentialBackoffRetry;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * @title CuratorLockTest$
 * @copyright: copyright (c) 2019
 * @company: X科技有限公司
 * @author: tgyman$
 * @date: 2020/4/29$ 17:16$
 * @firstReview:
 * @lastReview:
 * @desc: zookeeper方式模拟分布式锁,同样采用多线程
 */
public class CuratorLockTest {
    static int fixNum=5;

    public static void main(String[] args) throws Exception{
        CountDownLatch latch=new CountDownLatch(fixNum);
        String zkAddr="192.168.17.133:2181";
        CuratorFramework client = CuratorFrameworkFactory.newClient(zkAddr,
                new ExponentialBackoffRetry(1000, 3));
        client.start();
        System.out.println("客户端启动。。。");
        ExecutorService exec = Executors.newFixedThreadPool(fixNum);
        for(int i=0;i<fixNum;i++){
            exec.submit(new TestLock("client"+i,client ,latch));
        }
        exec.shutdown();
        latch.await();
        System.out.println("所有任务执行完毕");
        System.out.println("客户端关闭");
        client.close();
    }

    static class TestLock implements Runnable{
        private String name;
        private CuratorFramework client;
        private CountDownLatch latch;

        public TestLock(String name, CuratorFramework client, CountDownLatch latch) {
            this.name = name;
            this.client = client;
            this.latch = latch;
        }

        public void setName(String name) {
            this.name = name;
        }

        @Override
        public void run() {
            //定义锁
            InterProcessMutex lock = new InterProcessMutex(client, "/test_lock");

            try{
                System.out.println("--------"+this.name+"----等待获取锁。------");
                //获取锁
                if (lock.acquire(200, TimeUnit.MILLISECONDS)) {
                    //最大等待200ms获取锁，不需要设置获取锁之后的超时时间，因为每次调用acquire会在/test_lock节点下使用
                    //CreateMode.EPHEMERAL_SEQUENTIAL 创建新的临时节点
                    System.out.println("--------"+this.name+"----获得锁--开始处理资源----");
                    Thread.sleep(1000);//模拟业务处理100ms
                    System.out.println("--------"+this.name+"----锁使用完毕----");
                    latch.countDown();
                    System.out.println("--------"+this.name+"----释放锁----");
                    latch.countDown();

                }
            }catch (Exception e){

            }finally {
                
            }
        }
    }
}
