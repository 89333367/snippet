package sunyu.demo.integration.bigdata.admin.controller;

import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.SecureUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.shiro.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import sunyu.demo.integration.bigdata.admin.pojo.ShiroSubject;
import sunyu.demo.integration.bigdata.admin.pojo.TablePlusResult;
import sunyu.demo.integration.bigdata.admin.pojo.ViewModel;
import sunyu.demo.integration.bigdata.admin.service.SubjectService;
import sunyu.demo.integration.bigdata.admin.toolkit.MailTool;

import java.util.Arrays;

@Controller
@RequestMapping("/shiro")
@CrossOrigin
public class ShiroController extends IndexController {
    @Autowired
    MailTool mailTool;

    @Autowired
    SubjectService subjectService;

    @PostMapping("/modifyPwd")
    @ResponseBody
    public ViewModel modifyPwd(@RequestParam(required = true) String oldPwd
            , @RequestParam(required = true) String newPwd) {
        ViewModel viewModel = new ViewModel();
        ShiroSubject subject = (ShiroSubject) SecurityUtils.getSubject().getPrincipal();
        ShiroSubject user = subjectService.getOne(Wrappers.<ShiroSubject>lambdaQuery().eq(ShiroSubject::getEmail, subject.getEmail()));
        if (user != null) {
            if (SecureUtil.md5(oldPwd).equals(user.getPassword())) {
                user.setPassword(SecureUtil.md5(newPwd));
                subjectService.updateById(user);
                viewModel.setMessage("密码修改成功！");
            } else {
                viewModel.setStatus(10002);
                viewModel.setError("原密码错误！");
            }
        } else {
            viewModel.setStatus(10001);
            viewModel.setError("用户不存在！");
        }
        return viewModel;
    }

    @PostMapping("/changeStatus")
    @ResponseBody
    public ViewModel changeStatus(@RequestParam(required = true) Integer userId) {
        ViewModel viewModel = new ViewModel();
        ShiroSubject user = subjectService.getOne(Wrappers.<ShiroSubject>lambdaQuery().eq(ShiroSubject::getId, userId));
        if (user != null) {
            if (user.getDisabled() == 0) {
                user.setDisabled(1);
                viewModel.setMessage("用户 " + user.getName() + " 已禁用");
            } else {
                user.setDisabled(0);
                viewModel.setMessage("用户 " + user.getName() + " 已启用");
            }
            subjectService.updateById(user);
        } else {
            viewModel.setStatus(10001);
            viewModel.setError("用户不存在！");
        }
        return viewModel;
    }

    @PostMapping("/resetPwd")
    @ResponseBody
    public ViewModel resetPwd(@RequestParam(required = true) Integer userId) {
        ViewModel viewModel = new ViewModel();
        ShiroSubject user = subjectService.getOne(Wrappers.<ShiroSubject>lambdaQuery().eq(ShiroSubject::getId, userId));
        if (user != null) {
            String pwd = "bcld";
            user.setPassword(SecureUtil.md5(pwd));
            subjectService.updateById(user);
            mailTool.sendMail(Arrays.asList(user.getEmail()), "大数据支撑平台密码重置", "您的新密码是：<br/>" + pwd, true);
            viewModel.setMessage("密码已重置，新密码已发送到 " + user.getEmail() + " 请注意查收！");
        } else {
            viewModel.setStatus(10001);
            viewModel.setError("用户不存在！");
        }
        return viewModel;
    }

    @GetMapping("/user/list")
    public ModelAndView user_list_page(Model model
            , @RequestParam(defaultValue = "") String name
            , @RequestParam(defaultValue = "") String email
            , @RequestParam(defaultValue = "") String disabled) {
        model.addAttribute("name", name);
        model.addAttribute("email", email);
        model.addAttribute("disabled", disabled);
        return new ModelAndView("shiro/user_list", model.asMap());
    }

    @PostMapping("/user/list")
    @ResponseBody
    public TablePlusResult user_list_data(Model model
            , @RequestParam(defaultValue = "") String name
            , @RequestParam(defaultValue = "") String email
            , @RequestParam(defaultValue = "") String disabled
            , @RequestParam(defaultValue = "1") Integer page
            , @RequestParam(defaultValue = "10") Integer pageSize) {
        TablePlusResult result = new TablePlusResult();
        IPage<ShiroSubject> iPage = new Page<>(page, pageSize);
        QueryWrapper<ShiroSubject> wrapper = new QueryWrapper<>();
        if (StrUtil.isNotBlank(name)) {
            wrapper.like("name", name);
        }
        if (StrUtil.isNotBlank(email)) {
            wrapper.like("email", email);
        }
        if (StrUtil.isNotBlank(disabled)) {
            wrapper.eq("disabled", disabled);
        }
        if (!wrapper.isEmptyOfWhere()) {
            subjectService.page(iPage, wrapper);
        } else {
            subjectService.page(iPage);
        }
        if (iPage.getSize() > 0) {
            result.setRows(iPage.getRecords());
        }
        return result;
    }
}
