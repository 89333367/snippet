package sunyu.demo.integration.bigdata.admin.controller;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.util.StrUtil;
import cn.hutool.log.StaticLog;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.session.Session;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.web.util.SavedRequest;
import org.apache.shiro.web.util.WebUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import sunyu.demo.integration.bigdata.admin.config.convert.DateTimeEditor;

import javax.servlet.http.HttpServletRequest;

@Controller
@RequestMapping("/")
@CrossOrigin
public class IndexController {
    @InitBinder
    protected void dateBinder(WebDataBinder binder) {
        binder.registerCustomEditor(DateTime.class, new DateTimeEditor());
    }

    @GetMapping("/")
    public ModelAndView index() {
        return new ModelAndView("index");
    }

    @RequestMapping(value = {"/robots.txt", "/robot.txt"})
    public ModelAndView getRobotsTxt() {
        return new ModelAndView("index");
    }

    @GetMapping("/widgets")
    public ModelAndView widgets() {
        return new ModelAndView("widgets");
    }

    @RequestMapping("/403")
    public ModelAndView code403(Model model, HttpServletRequest request) {
        model.addAttribute("status", "403");
        model.addAttribute("error", "无权限");
        model.addAttribute("message", "您无权限访问 " + WebUtils.getSavedRequest(request).getRequestUrl());
        return new ModelAndView("403", model.asMap());
    }

    @GetMapping("/login")
    public ModelAndView login() {
        return new ModelAndView("login");
    }

    @PostMapping("/login")
    public ModelAndView login(Model model, String email, String password) {
        // 从SecurityUtils里边创建一个 subject
        Subject subject = SecurityUtils.getSubject();
        // 在认证提交前准备 token（令牌）
        UsernamePasswordToken token = new UsernamePasswordToken(email, password);
        // 执行认证登陆
        try {
            subject.login(token);
            Session session = subject.getSession(false);
            //能走到这里说明登入成功
            //session.setAttribute("","object");//这里可以存入自己想要的信息
            if (session != null) {
                SavedRequest savedRequest = (SavedRequest) session.getAttribute(WebUtils.SAVED_REQUEST_KEY);
                if (savedRequest != null) {
                    String requestUrl = savedRequest.getRequestUrl();
                    if (StrUtil.isNotBlank(requestUrl) && !requestUrl.contains("/login")) {
                        StaticLog.debug("重定向到之前访问的页面 {}", requestUrl);
                        return new ModelAndView("redirect:" + requestUrl);
                    }
                }
                return new ModelAndView("redirect:/");
            }
            return index();
        } catch (AuthenticationException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("email", email);
            model.addAttribute("password", password);
            return new ModelAndView("login", model.asMap());
        }
    }

    @GetMapping("/logout")
    public ModelAndView logout() {
        SecurityUtils.getSubject().logout();
        return login();
    }

}
