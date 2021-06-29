package com.tanhua.manage.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tanhua.manage.domain.Log;
import com.tanhua.manage.mapper.LogMapper;
import org.springframework.stereotype.Service;

@Service
public class LogService extends ServiceImpl<LogMapper, Log> {
}
