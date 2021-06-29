package com.tanhua.server.interceptor;

import com.tanhua.domain.db.User;

/**
 * 通过ThreadLocal对象，存储用户的数据
 *  作用：提供一个线程内公共变量（比如本次请求的用户信息），减少同一个线程内多个函数或者组件之间一些公共变量的传递的复杂度
 *      1、声明ThreadLocal对象
 *      2、向当前线程存储数据：threadLocal.set();
 *      3、从当前线程获取数据：threadLocal.get();
 */
public class UserHolder {

    private static ThreadLocal<User> threadLocal = new ThreadLocal<>();

    //向当前线程存储数据
    public static void setUser(User user){
        threadLocal.set(user);
    }
    //从当前线程获取数据
    public static User getUser() {
        return threadLocal.get();
    }
    //获取当前用户的id
    public static Long getUserId(){
        return getUser().getId();
    }
}
