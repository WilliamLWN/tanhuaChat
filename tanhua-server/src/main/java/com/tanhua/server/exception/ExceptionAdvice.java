package com.tanhua.server.exception;

import com.tanhua.domain.vo.ErrorResult;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

//自定义异常处理器
@ControllerAdvice
public class ExceptionAdvice {
    /**
     *  @ExceptionHandler :
     *      value : 指定当前异常处理器处理的异常类型
     */
    @ExceptionHandler(value=Exception.class)
    @ResponseBody
    public ResponseEntity handlerException(Exception e) {
        e.printStackTrace();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ErrorResult.error());
    }
}
