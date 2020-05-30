package com.ivar.ivarservice.controller;

import com.ivar.ivarservice.service.IvarService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.HashMap;
import java.util.Map;

/**
 * Copyright (c) 2018. scicom.com.my - All Rights Reserved
 * Created by kalana.w on 5/14/2020.
 */
@Controller
public class IVarController {

    @Autowired
    private IvarService ivarService;

    @GetMapping("/calculate")
    public String initialize(Model model, @RequestParam int period, @RequestParam String startDate, @RequestParam String stockName) {
        Map<String, Object> objectMap = this.ivarService.calculateIvar(startDate, period, stockName);
        model.addAllAttributes(objectMap);
        return "viewIvar";
    }

}
