package sunyu.demo.integration.bigdata.admin.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping("/dashboard")
@CrossOrigin
public class DashboardController extends IndexController {

    @GetMapping("/dashboard")
    public ModelAndView dashboard() {
        return new ModelAndView("dashboard/dashboard");
    }

}
