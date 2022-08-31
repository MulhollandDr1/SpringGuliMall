package com.example.gulimall.search.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class SearchController {
    @GetMapping("/")
    public String search(){
        return "index";
    }
    @GetMapping("/list.html")
    public String list(@RequestParam("catalog3Id") Long catalog3Id){
        System.out.println(catalog3Id);
        return "list";
    }
}
