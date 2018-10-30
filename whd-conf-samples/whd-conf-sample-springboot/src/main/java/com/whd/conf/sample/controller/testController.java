package com.whd.conf.sample.controller;

import com.whd.conf.sample.demo.DemoConf;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @author hayden 2018-10-27 23:27:12
 */
@Controller
public class testController {

    @Autowired
    private DemoConf demoConf;

    @RequestMapping("/test")
    @ResponseBody
    public String getConf(){
        return demoConf.neteaseurl;
    }
}
