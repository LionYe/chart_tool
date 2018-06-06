package com.harbor.chart.web.controller;

import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 页面跳转控制控制器
 * @author szy
 *
 */
@Controller
public class MainController {

	@RequestMapping("test")
	public String chart(HttpServletRequest request, Model model) {
		return "test";
	}
}
