package com.example.gulimall.product.exception;

import com.example.common.exception.BizCodeEnum;
import com.example.common.utils.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@ControllerAdvice(basePackages = "com.example.gulimall.product.controller")
@ResponseBody
public class ExceptionControllerAdvice {
    @ExceptionHandler(value = MethodArgumentNotValidException.class) /*@ExceptionHandler告诉springMVC这个方法能处理什么类型的异常*/
    public R handleValidException(MethodArgumentNotValidException e){
        log.error("数据校验出现问题{}，异常类型：{}",e.getMessage(),e.getClass());
        BindingResult bindingResult = e.getBindingResult();
        Map<String,String> errorMap = new HashMap<>();
        bindingResult.getFieldErrors().forEach((error) ->{
            errorMap.put(error.getField(),error.getDefaultMessage());
        });
        return R.error(BizCodeEnum.VALIDATE_EXCEPTION.getCode(), BizCodeEnum.VALIDATE_EXCEPTION.getMessage()).put("data",errorMap);
    }
/*    @ExceptionHandler(value = Throwable.class) *//*处理其它所有异常。先注释掉，否则其他异常被这个方法捕获返回给前端，idea控制台看不到异常报错，影响代码排错*//*
    public R handleException(Throwable throwable){
        log.error(throwable.getMessage());
        return R.error();
    }*/
}
