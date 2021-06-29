package com.tanhua.server.test;

import com.tanhua.domain.db.UserInfo;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class UserInfoTestService {

    /**
     * 缓存测试方法,测试流程：
     * 1、方法上不加注解执行2次，发现没有都从数据库查询（模拟）
     * 2、加上注解 @Cacheable(value = "users")
     * 第一次执行：从数据库获取数据
     * 第二次执行：从缓存获取数据
     */
    @Cacheable(value = "users")
    public List<UserInfo> findAll(){
        System.out.println("从数据库获取数据：");
        List<UserInfo> list = new ArrayList<>();
        for (int i = 1; i <= 7; i++) {
            UserInfo userInfo = new UserInfo();
            userInfo.setNickname("葫芦娃兄弟"+i);
            userInfo.setCity("gz");
            list.add(userInfo);
        }
        return list;
    }

    /**
     * 保存数据时候，需要删除缓存中数据
     * @CacheEvict
     *   allEntries: true表示清除缓存中的所有元素
     *   value 指定要清除的缓存的key
     *
     */
    @CacheEvict(value = "users",allEntries = true)
    public void save(UserInfo userInfo){
        System.out.println("保存数据！");
    }

    /**
     * 自定义key
     * @Cacheable
     *    key
     *      1. 用来指定key名称，支持spel，可以从方法形参中获取数据
     *      2. 举例：key = "#userId"  指定形参名称是userId的值
     *      2. 举例：key = "#p0"      获取方法第一个参数的值
     */
    @Cacheable(value = "user",key = "#userId")
    //@Cacheable(value = "user",key = "#p0") // 同上
    public UserInfo findById(Long userId) {
        System.out.println("根据id查询数据库：" + userId);
        UserInfo userInfo = new UserInfo();
        userInfo.setNickname("葫芦娃兄弟");
        userInfo.setCity("gz");
        return userInfo;
    }

    /**
     * key = "#userInfo.id"
     *   清空的缓存的key是方法参数中userInfo对象的id属性
     */
    @CacheEvict(value = "user",key = "#userInfo.id")
    public void update(UserInfo userInfo) {
        System.out.println("更新...");
    }
}
