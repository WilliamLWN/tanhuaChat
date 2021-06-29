package com.tanhua.dubbo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tanhua.domain.db.UserInfo;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

public interface UserInfoMapper extends BaseMapper<UserInfo> {

    //多表查询，通过黑名单表的 userid 查到此id的黑名单人的 userid ,通过黑名单人的 userid 获取黑名单人的用户信息
    //第一个参数page是mybatis-plus帮你分页，第二个参数是被查找的userid
    @Select("SELECT info.* FROM tb_black_list b,tb_user_info info WHERE b.black_user_id=info.id AND b.user_id=#{userId}")
    IPage<UserInfo> findBlackList(Page<UserInfo> page, @Param("userId") Long userId);
}
