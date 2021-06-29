package com.tanhua.dubbo.handler;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * mybatis-plus 支持自定义处理器s实现添加或更新的自动填充
 */
@Component
public class MyMetaObjectHandler implements MetaObjectHandler {

    //添加数据时自动填充时间
    @Override
    public void insertFill(MetaObject metaObject) {
        Object created = getFieldValByName("created", metaObject);
        if(created == null) {
            setFieldValByName("created", new Date(), metaObject);
        }


        Object updated = getFieldValByName("updated", metaObject);
        if (updated == null) {
            //字段为空，可以进行填充
            setFieldValByName("updated", new Date(), metaObject);
        }
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        //更新不用判空，因为更新根本不会空
            setFieldValByName("updated", new Date(), metaObject);
    }
}
