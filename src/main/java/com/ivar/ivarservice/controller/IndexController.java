package com.ivar.ivarservice.controller;

import com.ivar.ivarservice.service.IndexService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.io.*;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Copyright (c) 2018. scicom.com.my - All Rights Reserved
 * Created by kalana.w on 5/14/2020.
 */
@Controller
public class IndexController {

    @Autowired
    private IndexService indexService;

    @GetMapping()
    public String index(Model model) {
        return "index";
    }

    @GetMapping("/initialize")
    public String initialize(Model model) {
        Map<String, String> map = new HashMap<>();
        boolean init = this.init();
        map.put("status", "1");
        map.put("message", "success");
        if (!init) {
            map.put("status", "-1");
            map.put("message", "failed");
        }
        model.addAllAttributes(map);
        return "index";
    }

    private boolean init() {
        try {
            int i = this.indexService.loadCompanyList();
            int i1 = this.indexService.loadPriceCsv();
            int i2 = this.indexService.loadPages();
            if (i == 1 && i1 == 1 && i2 == 1) {
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return false;
    }

    @GetMapping("/viewIvar")
    public String viewIvar(Model model) {
        Map<String, String> map = new HashMap<>();
        map.put("status", "1");
        map.put("message", "success");
        model.addAllAttributes(map);
        return "viewIvar";
    }
}
