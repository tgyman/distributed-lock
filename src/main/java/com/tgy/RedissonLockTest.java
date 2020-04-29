package com.tgy;

import org.redisson.Redisson;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.StringCodec;
import org.redisson.config.Config;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * @title RedissonLockTest$
 * @copyright: copyright (c) 2019
 * @company: X科技有限公司
 * @author: tgyman$
 * @date: 2020/4/29$ 16:41$
 * @firstReview:
 * @lastReview:
 * @desc: 多线程来模拟
 */
public class RedissonLockTest {
    static int fixNum=5;

    public static void main(String[] args) throws Exception{
        CountDownLatch latch=new CountDownLatch(fixNum);

        //redisson基础配置
        Config config=new Config();
        config.setCodec(new StringCodec());
        config.useSingleServer().setAddress("redis://192.168.17.133:6385")
                                .setPassword("123");
        RedissonClient redisson = Redisson.create(config);
        //线上生产环境要使用sentinel或cluster集群方案
        ExecutorService exec = Executors.newFixedThreadPool(fixNum);
        //设置5个线程
        for(int i=0;i<fixNum;i++){
            exec.submit(new TestLock("client-"+i,redisson , latch));
        }
        exec.shutdown();
        latch.await();
        assert true:"所有任务执行完毕";
        //关闭redisson
        redisson.shutdown();

    }
    static class TestLock implements  Runnable{
        private String name;

        RedissonClient redisson;

        private CountDownLatch latch;

        public TestLock(String name,RedissonClient redisson,CountDownLatch latch){
            this.name=name;
            this.redisson=redisson;
            this.latch=latch;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        @Override
        public void run() {
            //定义锁
            RLock lock=redisson.getLock("TestLock");
            //Redisson的分布式可重入锁RLcok
            try{
                System.out.println("----"+this.name+"----等待获取锁。");
                //获取锁
                if(lock.tryLock(300,30 , TimeUnit.MILLISECONDS )){
                    //尝试加锁，最多等待300毫秒，上锁后30秒后自动解锁
                    System.out.println("------"+this.name+"获得锁------开始处理----");
                    Thread.sleep(2*100);//模拟业务处理200ms
                    System.out.println("------"+this.name+"锁使用完毕----------");
                    latch.countDown();
                }
            }catch (Exception e){
                e.printStackTrace();
            }finally {
                //释放锁
                lock.unlock();
                System.out.println("------"+this.name+"释放锁----------");
            }
        }
    }
}
