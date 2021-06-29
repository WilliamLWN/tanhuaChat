package com.tanhua.manage.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tanhua.manage.domain.Log;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

public interface LogMapper extends BaseMapper<Log> {

    /**
     * 统计log表中，今日注册人数
     */
    @Select("SELECT COUNT(DISTINCT user_id) FROM tb_log " +
            "WHERE TYPE=#{type} AND log_time=#{now}")
    Long queryNumsByType(@Param("now") String now, @Param("type") String type);

    /**
     * 统计log表中，活跃用户数
     */
    @Select("SELECT COUNT(DISTINCT user_id) FROM tb_log WHERE log_time=#{now}")
    Long queryNumsByDate(@Param("now") String now);

    /**
     * 统计log表中，次日留存用户数
     */
    @Select("SELECT COUNT(DISTINCT user_id) FROM tb_log WHERE log_time=#{now} AND user_id IN (\n" +
            "  SELECT user_id FROM tb_log WHERE TYPE='0102' AND log_time=#{yes}\n" +
            ")\n")
    Long queryRetention1d(@Param("now") String now, @Param("yes") String yesterday);
}
