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
        BindingResult bindingResult = e.getBindingResult();
        Map<String,String> errorMap = new HashMap<>();
        bindingResult.getFieldErrors().forEach((error) ->{
            errorMap.put(error.getField(),error.getDefaultMessage());
        });
        log.error(e.toString());
        return R.error(BizCodeEnum.VALIDATE_EXCEPTION.getCode(), BizCodeEnum.VALIDATE_EXCEPTION.getMessage()).put("data",errorMap);
    }
    @ExceptionHandler(value = Throwable.class) /*处理其它所有异常*/
    public R handleException(Throwable throwable){
        return R.error();
    }
}
