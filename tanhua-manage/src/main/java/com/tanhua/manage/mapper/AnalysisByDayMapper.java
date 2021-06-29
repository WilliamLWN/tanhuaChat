package com.tanhua.manage.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tanhua.manage.vo.AnalysisByDay;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

public interface AnalysisByDayMapper extends BaseMapper<AnalysisByDay> {

    @Select("SELECT sum(num_active) FROM tb_analysis_by_day " +
            "where record_date between #{start} and #{end}")
    Long findNumActiveByDate(@Param("start") String start,@Param("end") String end);
}
