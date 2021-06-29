package com.tanhua.dubbo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tanhua.domain.db.BlackList;

public interface BlackListMapper extends BaseMapper<BlackList> {

//    /**
//     * 调用api服务，移除黑名单用户(从tb_black_list表中删除一条记录)
//     * @param userId
//     * @param blackUserId
//     */
//    @Delete("DELETE FROM tb_black_list WHERE user_id=#{userId} " +
//            "AND black_user_id=#{blackUserId}")
//    void deleteBlackUser(@Param("userId") Long userId, @Param("blackUserId") Long blackUserId);
}
