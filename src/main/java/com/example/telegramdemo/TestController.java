package com.example.telegramdemo;

import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.TreeMap;

/**
 * Description: <br>
 *
 * @author xwc1125 <br>
 * @Copyright: Copyright (c) 2018 <br>
 * @date 2018-12-26  19:13 <br>
 */
@Controller
@RequestMapping("/api/telegram")
public class TestController {


    @GetMapping("/test1")
    @ResponseBody
    public void recharge(HttpServletRequest request, HttpServletResponse response, ModelMap model) {
        MainLauncher.test();
    }
}
