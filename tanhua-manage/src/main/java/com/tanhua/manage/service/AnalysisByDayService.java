package com.tanhua.manage.service;

import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tanhua.manage.mapper.AnalysisByDayMapper;
import com.tanhua.manage.mapper.LogMapper;
import com.tanhua.manage.util.ComputeUtil;
import com.tanhua.manage.vo.AnalysisByDay;
import com.tanhua.manage.vo.AnalysisSummaryVo;
import com.tanhua.manage.vo.AnalysisUsersVo;
import org.checkerframework.checker.units.qual.A;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class AnalysisByDayService extends ServiceImpl<AnalysisByDayMapper, AnalysisByDay> {
    @Autowired
    private AnalysisByDayMapper analysisByDayMapper;
    @Autowired
    private LogMapper logMapper;

    private AnalysisSummaryVo vo = null;

    /**
     * 接口名称：概要统计信息
     */
    public ResponseEntity<Object> summary() {
        //1. 累计用户数: 统计新注册用户数
        //SELECT SUM(num_registered) FROM tb_analysis_by_day
        AnalysisByDay analysisByDay = query().select("SUM(num_registered) as numRegistered").one();
        Integer numRegistered = analysisByDay.getNumRegistered();

        //2. 统计今日数据：查询今日新增用户数量、今日登录次数、今日活跃用户数量
        //String now = "2020-09-25";
        Date date = new Date();
        String now = new SimpleDateFormat("yyyy-MM-dd").format(date);
        AnalysisByDay recordToday = query().eq("record_date", now).one();
        if (recordToday != null) {
            Integer newUsersToday = recordToday.getNumRegistered();
            Integer numLogin = recordToday.getNumLogin();
            Integer numActive = recordToday.getNumActive();

            //3. 统计昨日数据：查询昨日新增用户数量、登陆次数、活跃用户数量
            String yesterday = ComputeUtil.offsetDay(date, -1);
            AnalysisByDay recordYes = query().eq("record_date", yesterday).one();
            Integer yes_newUsersToday = recordYes.getNumRegistered();
            Integer yes_numLogin = recordYes.getNumLogin();
            Integer yes_numActive = recordYes.getNumActive();

            //4. 过去30天活跃用户数和过去7天活跃用户
            Long day7 = analysisByDayMapper.findNumActiveByDate(ComputeUtil.offsetDay(date, -7), now);
            Long day30 = analysisByDayMapper.findNumActiveByDate(ComputeUtil.offsetDay(date, -30), now);

            // 封装vo
            vo = new AnalysisSummaryVo();
            vo.setCumulativeUsers(numRegistered.longValue());
            vo.setNewUsersToday(newUsersToday.longValue());
            vo.setLoginTimesToday(numLogin.longValue());
            vo.setActiveUsersToday(numActive.longValue());
            //设置涨跌率：newUsersTodayRate、loginTimesTodayRate、activeUsersTodayRate
            vo.setNewUsersTodayRate(ComputeUtil.computeRate(newUsersToday, yes_newUsersToday));
            vo.setLoginTimesTodayRate(ComputeUtil.computeRate(numLogin, yes_numLogin));
            vo.setActiveUsersTodayRate(ComputeUtil.computeRate(numActive, yes_numActive));
            // 设置过去7天、30天活跃用户
            vo.setActivePassWeek(day7);
            vo.setActivePassMonth(day30);
        }

        return ResponseEntity.ok(vo);
    }

    /**
     * 接口名称：新增、活跃用户、次日留存率
     * @param sd
     * @param ed
     * @param type
     * @return
     */
    public ResponseEntity<Object> users(long sd, long ed, Integer type) throws ParseException {
        //今年的今天
        Date start = new Date(sd);
        Date end = new Date(ed);
//        Date lastStart = new Date(sd - 1000*60*60*24*365);
//        Date lastEnd = new Date(ed - 1000*60*60*24*365);
        String startTime = new SimpleDateFormat("yyyy-MM-dd").format(start);
        String endTime = new SimpleDateFormat("yyyy-MM-dd").format(end);

        //拼接字符串转成去年的今天
        String[] splitStart = startTime.split("-");
        Integer yearStart = Integer.valueOf(splitStart[0]);
        String yeStart = String.valueOf(yearStart - 1);
        String lastStartStr = yeStart + "-" + splitStart[1] + "-" + splitStart[2];
        Date lastStart = new SimpleDateFormat("yyyy-MM-dd").parse(lastStartStr);
        String[] splitEnd = endTime.split("-");
        Integer yearEnd= Integer.valueOf(splitEnd[0]);
        String yeEnd = String.valueOf(yearEnd - 1);
        String lastEndStr = yeEnd + "-" + splitEnd[1] + "-" + splitEnd[2];
        Date lastEnd = new SimpleDateFormat("yyyy-MM-dd").parse(lastEndStr);

        //转成数据库需要的格式
        String lastStartTime = new SimpleDateFormat("yyyy-MM-dd").format(lastStart);
        String lastEndTime = new SimpleDateFormat("yyyy-MM-dd").format(lastEnd);

        //转成Date类型为了两个日期相减
        SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd");
        Date thisYearStart = sdf.parse(startTime);
        Date thisYearEnd = sdf.parse(endTime);
        Date lastYearStart = sdf.parse(lastStartTime);
        Date lastYearEnd = sdf.parse(lastEndTime);

        //封装数据
        List<Map<String, Object>> thisYear = null;
        List<Map<String, Object>> lastYear = null;

        switch (type) {
            case 101: //新增用户数
                 thisYear = getNumRegistered(thisYearStart, thisYearEnd);
                 lastYear = getNumRegistered(lastYearStart, lastYearEnd);
                break;
            case 102: //活跃用户数
                thisYear = getNumActive(thisYearStart, thisYearEnd);
                lastYear = getNumActive(lastYearStart, lastYearEnd);
                break;
            case 103: //次日留存用户数
                thisYear = getNumRetention1d(thisYearStart, thisYearEnd);
                lastYear = getNumRetention1d(lastYearStart, lastYearEnd);
                break;
            default:
                break;
        }

        AnalysisUsersVo analysisUsersVo = new AnalysisUsersVo();
        analysisUsersVo.setThisYear(thisYear);
        analysisUsersVo.setLastYear(lastYear);

        return ResponseEntity.ok(analysisUsersVo);
    }


    private List<Map<String, Object>> getNumRegistered(Date start, Date end) {
        List<Map<String, Object>> list = new ArrayList<>();
        //相隔多少天,+1才是对的
        long daysBetween=(end.getTime()- start.getTime()+1000000)/(60*60*24*1000) + 1;
                for (long i=0; i<=daysBetween; i++) {
                    Map<String, Object> m = new HashMap<>();
                    String title = ComputeUtil.offsetDay(start, (int)i);
                    AnalysisByDay recordDay = query().eq("record_date", title).one();
                    if(recordDay != null) {
                        Integer amount = recordDay.getNumRegistered();
                        m.put("title", title);
                        m.put("amount", amount);
                        list.add(m);
                    } else {
                        m.put("title", title);
                        m.put("amount", 0);
                    }
                }
        System.out.println("list = " + list);
        return list;
    }

    private List<Map<String, Object>> getNumActive(Date start, Date end) {
        List<Map<String, Object>> list = new ArrayList<>();
        //相隔多少天,+1才是对的
        long daysBetween=(end.getTime()- start.getTime()+1000000)/(60*60*24*1000) + 1;
        for (long i=0; i<=daysBetween; i++) {
            Map<String, Object> m = new HashMap<>();
            String title = ComputeUtil.offsetDay(start, (int)i);
            AnalysisByDay recordDay = query().eq("record_date", title).one();
            if(recordDay != null) {
                Integer amount = recordDay.getNumActive();
                m.put("title", title);
                m.put("amount", amount);
                list.add(m);
            } else {
                m.put("title", title);
                m.put("amount", 0);
            }
        }
        System.out.println("list = " + list);
        return list;
    }

    private List<Map<String, Object>> getNumRetention1d(Date start, Date end) {
        List<Map<String, Object>> list = new ArrayList<>();
        //相隔多少天,+1才是对的
        long daysBetween=(end.getTime()- start.getTime()+1000000)/(60*60*24*1000) + 1;
        for (long i=0; i<=daysBetween; i++) {
            Map<String, Object> m = new HashMap<>();
            String title = ComputeUtil.offsetDay(start, (int)i);
            AnalysisByDay recordDay = query().eq("record_date", title).one();
            if(recordDay != null) {
                Integer amount = recordDay.getNumRetention1d();
                m.put("title", title);
                m.put("amount", amount);
                list.add(m);
            } else {
                m.put("title", title);
                m.put("amount", 0);
            }
        }
        System.out.println("list = " + list);
        return list;
    }

    /**
     * 定时任务，每5分钟执行一次数据统计，把log的数据统计到AnalysisByDay表中
     */
    public void anasysis() {

        //1、获取当前时间
        // 当前时间字符串
        String now = DateUtil.formatDate(new Date());
        // 当前时间对象
        Date nowDate = DateUtil.parse(now);

        //2、根据当前时间，查询统计分析表数据: tb_analysis_by_day
        QueryWrapper<AnalysisByDay> query = new QueryWrapper<>();
        query.eq("record_date",now);
        AnalysisByDay analysisByDay = analysisByDayMapper.selectOne(query);

        //3. 如果第一次统计，创建一条今日的统计数据
        if (analysisByDay == null) {
            analysisByDay = new AnalysisByDay();
            analysisByDay.setRecordDate(nowDate);
            analysisByDay.setCreated(nowDate);
            analysisByDay.setUpdated(nowDate);
            analysisByDayMapper.insert(analysisByDay);
        }

        // 昨天的日期
        String yesterday = ComputeUtil.offsetDay(new Date(),-1);

        //4. 统计
        //4.1 统计log表中，今日注册人数
        Long registeredNum = logMapper.queryNumsByType(now,"0102");
        //4.2 统计log表中，登陆次数
        Long loginNum = logMapper.queryNumsByType(now,"0101");
        //4.3 统计log表中，活跃用户数
        Long activeNum = logMapper.queryNumsByDate(now);
        //4.4 统计log表中，次日留存用户数
        Long retention1d = logMapper.queryRetention1d (now,yesterday);

        //4. 封装数据，更新统计分析表
        analysisByDay.setNumRegistered(registeredNum.intValue());
        analysisByDay.setNumLogin(loginNum.intValue());
        analysisByDay.setNumActive(activeNum.intValue());
        analysisByDay.setNumRetention1d(retention1d.intValue());
        analysisByDay.setUpdated(new Date());
        analysisByDayMapper.updateById(analysisByDay);
    }
}
