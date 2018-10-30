package com.whd.conf.admin.controller;

import com.whd.conf.admin.controller.annotation.PermessionLimit;
import com.whd.conf.admin.core.util.ReturnT;
import com.whd.conf.admin.dao.ConfUserDao;
import com.whd.conf.admin.core.model.ConfProject;
import com.whd.conf.admin.core.model.ConfUser;
import com.whd.conf.admin.dao.ConfProjectDao;
import com.whd.conf.admin.service.impl.LoginService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author hayden 2018-03-01
 */
@Controller
@RequestMapping("/user")
public class UserController {

    @Resource
    private ConfUserDao confUserDao;
    @Resource
    private ConfProjectDao confProjectDao;

    @RequestMapping("")
    @PermessionLimit(adminuser = true)
    public String index(Model model){

        List<ConfProject> projectList = confProjectDao.findAll();
        model.addAttribute("projectList", projectList);

        return "user/user.index";
    }

    @RequestMapping("/pageList")
    @PermessionLimit(adminuser = true)
    @ResponseBody
    public Map<String, Object> pageList(@RequestParam(required = false, defaultValue = "0") int start,
                                        @RequestParam(required = false, defaultValue = "10") int length,
                                        String username,
                                        int permission) {

        // ConfNode in mysql
        List<ConfUser> data = confUserDao.pageList(start, length, username, permission);
        int list_count = confUserDao.pageListCount(start, length, username, permission);

        // package result
        Map<String, Object> maps = new HashMap<String, Object>();
        maps.put("data", data);
        maps.put("recordsTotal", list_count);		// 总记录数
        maps.put("recordsFiltered", list_count);	// 过滤后的总记录数
        return maps;
    }

    /**
     * add
     *
     * @return
     */
    @RequestMapping("/add")
    @PermessionLimit(adminuser = true)
    @ResponseBody
    public ReturnT<String> add(ConfUser confUser){

        // valid
        if (StringUtils.isBlank(confUser.getUsername())){
            return new ReturnT<String>(ReturnT.FAIL.getCode(), "用户名不可为空");
        }
        if (StringUtils.isBlank(confUser.getPassword())){
            return new ReturnT<String>(ReturnT.FAIL.getCode(), "密码不可为空");
        }
        if (!(confUser.getPassword().length()>=4 && confUser.getPassword().length()<=100)) {
            return new ReturnT<String>(ReturnT.FAIL.getCode(), "密码长度限制为4~50");
        }

        // passowrd md5
        String md5Password = DigestUtils.md5DigestAsHex(confUser.getPassword().getBytes());
        confUser.setPassword(md5Password);

        int ret = confUserDao.add(confUser);
        return ret>0? ReturnT.SUCCESS: ReturnT.FAIL;
    }

    /**
     * delete
     *
     * @return
     */
    @RequestMapping("/delete")
    @PermessionLimit(adminuser = true)
    @ResponseBody
    public ReturnT<String> delete(HttpServletRequest request, String username){

        ConfUser loginUser = (ConfUser) request.getAttribute(LoginService.LOGIN_IDENTITY);
        if (loginUser.getUsername().equals(username)) {
            return new ReturnT<String>(ReturnT.FAIL.getCode(), "禁止操作当前登录账号");
        }

        /*List<ConfUser> adminList = confUserDao.pageList(0, 1 , null, 1);
        if (adminList.size()<2) {

        }*/

        confUserDao.delete(username);
        return ReturnT.SUCCESS;
    }

    /**
     * update
     *
     * @return
     */
    @RequestMapping("/update")
    @PermessionLimit(adminuser = true)
    @ResponseBody
    public ReturnT<String> update(HttpServletRequest request, ConfUser confUser){

        ConfUser loginUser = (ConfUser) request.getAttribute(LoginService.LOGIN_IDENTITY);
        if (loginUser.getUsername().equals(confUser.getUsername())) {
            return new ReturnT<String>(ReturnT.FAIL.getCode(), "禁止操作当前登录账号");
        }

        // valid
        if (StringUtils.isBlank(confUser.getUsername())){
            return new ReturnT<String>(ReturnT.FAIL.getCode(), "用户名不可为空");
        }

        ConfUser existUser = confUserDao.load(confUser.getUsername());
        if (existUser == null) {
            return new ReturnT<String>(ReturnT.FAIL.getCode(), "用户名非法");
        }

        if (StringUtils.isNotBlank(confUser.getPassword())) {
            if (!(confUser.getPassword().length()>=4 && confUser.getPassword().length()<=50)) {
                return new ReturnT<String>(ReturnT.FAIL.getCode(), "密码长度限制为4~50");
            }
            // passowrd md5
            String md5Password = DigestUtils.md5DigestAsHex(confUser.getPassword().getBytes());
            existUser.setPassword(md5Password);
        }
        existUser.setPermission(confUser.getPermission());

        int ret = confUserDao.update(existUser);
        return ret>0? ReturnT.SUCCESS: ReturnT.FAIL;
    }

    @RequestMapping("/updatePermissionProjects")
    @PermessionLimit(adminuser = true)
    @ResponseBody
    public ReturnT<String> updatePermissionProjects(HttpServletRequest request,
                                                    String username,
                                                    @RequestParam(required = false) String[] permissionProjects){

        String permissionProjectsStr = StringUtils.join(permissionProjects, ",");
        ConfUser existUser = confUserDao.load(username);
        if (existUser == null) {
            return new ReturnT<String>(ReturnT.FAIL.getCode(), "参数非法");
        }
        existUser.setPermissionProjects(permissionProjectsStr);
        confUserDao.update(existUser);

        return ReturnT.SUCCESS;
    }

    @RequestMapping("/updatePwd")
    @ResponseBody
    public ReturnT<String> updatePwd(HttpServletRequest request, String password){

        // new password(md5)
        if (StringUtils.isBlank(password)){
            return new ReturnT<String>(ReturnT.FAIL.getCode(), "密码不可为空");
        }
        if (!(password.length()>=4 && password.length()<=100)) {
            return new ReturnT<String>(ReturnT.FAIL.getCode(), "密码长度限制为4~50");
        }
        String md5Password = DigestUtils.md5DigestAsHex(password.getBytes());

        // update pwd
        ConfUser loginUser = (ConfUser) request.getAttribute(LoginService.LOGIN_IDENTITY);

        ConfUser existUser = confUserDao.load(loginUser.getUsername());
        existUser.setPassword(md5Password);
        confUserDao.update(existUser);

        return ReturnT.SUCCESS;
    }

}
