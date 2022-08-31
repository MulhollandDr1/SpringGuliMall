package com.example.gulimall.product.web;

import com.example.gulimall.product.entity.CategoryEntity;
import com.example.gulimall.product.service.CategoryService;
import com.example.gulimall.product.vo.Category2Vo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;

@Controller
public class IndexController {
    @Autowired
    CategoryService categoryService;
    @GetMapping({"/", "/index.html"})
    public String IndexPage(Model model){
        List<CategoryEntity> categoryEntityList = categoryService.getFirstLevel();
        model.addAttribute("categorys",categoryEntityList);
        return "index";
    }
    @ResponseBody
    @GetMapping("/index/catalog.json")
    public Map<String, List<Category2Vo>> getCatalogJson(){
        return categoryService.getCatalogJson();
    }
}
