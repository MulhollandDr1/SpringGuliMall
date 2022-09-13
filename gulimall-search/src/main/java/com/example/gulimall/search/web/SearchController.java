package com.example.gulimall.search.web;

import com.example.gulimall.search.service.MallSearchService;
import com.example.gulimall.search.vo.SearchParameterVo;
import com.example.gulimall.search.vo.SearchResultVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class SearchController {
    @Autowired
    MallSearchService mallSearchService;
    @GetMapping("/")
    public String index(){
        return "list";
    }

    /**
     *
     * @param searchParameterVo
     * @param model
     * @return
     */
    @GetMapping("/list.html")
    public String list(/*@RequestParam 需要springMVC帮我们封装数据就不带@RequestParam*/SearchParameterVo searchParameterVo, Model model){
        SearchResultVo searchResultVo = mallSearchService.search(searchParameterVo);
        model.addAttribute("searchResult",searchResultVo);
        return "list";
    }
}
