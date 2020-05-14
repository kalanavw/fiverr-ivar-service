package com.ivar.ivarservice.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Copyright (c) 2018. scicom.com.my - All Rights Reserved
 * Created by kalana.w on 5/14/2020.
 */
@Controller
@RequestMapping("")
public class IndexController
{
	@GetMapping()
	public String index( Model model )
	{
		return "index";
	}
}
