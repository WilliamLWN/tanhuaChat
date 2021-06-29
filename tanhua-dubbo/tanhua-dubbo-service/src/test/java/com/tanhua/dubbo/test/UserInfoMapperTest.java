package com.tanhua.dubbo.test;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tanhua.domain.db.UserInfo;
import com.tanhua.dubbo.mapper.UserInfoMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * 需要配置分页拦截器，创建分页插件对象
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class UserInfoMapperTest {
    @Autowired
    private UserInfoMapper userInfoMapper;

    @Test
    public void pageTest() {
        //1. 分页参数：当前页、页大小
        IPage<UserInfo> page = new Page<>(1, 2);
        //2. 分页参数：查询条件
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.le("age", 100);

        //3. 分页查询，测试
        IPage iPage = userInfoMapper.selectPage(page, queryWrapper);

        //4. 获取结果
        System.out.println("当前页：" + iPage.getCurrent()); //1
        System.out.println("总页数：" + iPage.getPages());  //1
        System.out.println("当前页数据：" + iPage.getRecords());
        System.out.println("页大小：" + iPage.getSize()); //2
        System.out.println("总记录：" + iPage.getTotal()); //2
    }
}
