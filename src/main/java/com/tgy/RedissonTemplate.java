package com.tgy;

import org.redisson.Redisson;
import org.redisson.api.RKeys;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.StringCodec;
import org.redisson.config.Config;

import java.util.concurrent.TimeUnit;

/**
 * @title RedissonTemplate$
 * @copyright: copyright (c) 2019
 * @company: X科技有限公司
 * @author: tgyman$
 * @date: 2020/4/29$ 15:54$
 * @firstReview:
 * @lastReview:
 * @desc: 常见redisson锁结构
 */
public class RedissonTemplate {
    public static void main(String[] args) throws Exception{
        Config config=new Config();
        //指定编码:默认为JsonJacksonCodec
        config.setCodec(new StringCodec());
        //指定使用单节点部署方式并设置地址和密码
        config.useSingleServer().setAddress("redis://192.168.17.133:6385")
                                .setPassword("123");

        RedissonClient redisson = Redisson.create(config);

        RLock lock = redisson.getLock("TestLock");
        //最常见的使用方法
        lock.lock();


        //过期解锁
        //10毫秒以后自动解锁，不需要再调用unlock方法手动解锁
        lock.lock(10, TimeUnit.MILLISECONDS );

        //尝试加锁，最大等待时间为100毫秒，上锁后30毫秒自动解锁
        boolean res=lock.tryLock(100, 30, TimeUnit.MILLISECONDS);


        lock.unlock();

        System.out.println("=======程序结束======");
        redisson.shutdown();

    }
}
