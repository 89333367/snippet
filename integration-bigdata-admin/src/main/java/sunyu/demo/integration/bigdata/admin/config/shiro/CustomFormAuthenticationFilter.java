package sunyu.demo.integration.bigdata.admin.config.shiro;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import cn.hutool.log.StaticLog;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.web.filter.authc.FormAuthenticationFilter;
import org.apache.shiro.web.util.WebUtils;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import sunyu.demo.integration.bigdata.admin.pojo.ShiroSubject;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

/**
 * 权限过滤器
 */
public class CustomFormAuthenticationFilter extends FormAuthenticationFilter {
    @Bean
    public FilterRegistrationBean registration(CustomFormAuthenticationFilter filter) {
        FilterRegistrationBean registration = new FilterRegistrationBean(filter);
        registration.setEnabled(false);
        return registration;
    }

    /**
     * 每次资源访问都会进入这里
     *
     * @param request
     * @param response
     * @param mappedValue
     * @return
     */
    @Override
    protected boolean isAccessAllowed(ServletRequest request, ServletResponse response, Object mappedValue) {
        Subject subject = getSubject(request, response);
        String uri = getPathWithinApplication(request);
        ShiroSubject principal = (ShiroSubject) subject.getPrincipal();
        if (principal != null) {
            if (subject.isAuthenticated()) {//如果已经登录过
                StaticLog.debug("{} 访问 {}", principal.getEmail(), uri);
            }
        } else {
            StaticLog.debug("未登入 访问 {}", uri);
        }
        WebUtils.saveRequest(request);
        return super.isAccessAllowed(request, response, mappedValue);
    }

    /**
     * isAccessAllowed返回false会进入这里
     *
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    @Override
    protected boolean onAccessDenied(ServletRequest request, ServletResponse response) throws Exception {
        String uri = getPathWithinApplication(request);
        HttpServletRequest httpServletRequest = (HttpServletRequest) request;
        HttpServletResponse httpServletResponse = (HttpServletResponse) response;
        Map<String, Object> result = new HashMap<>();
        result.put("status", 403);
        Subject subject = getSubject(request, response);
        if (isAjaxRequest(httpServletRequest)) {
            httpServletResponse.setCharacterEncoding("UTF-8");
            httpServletResponse.setContentType("application/json");
            httpServletResponse.setStatus(403);
            if (subject.isAuthenticated()) {
                result.put("error", "您无权限访问 " + uri);
            } else {
                result.put("error", "session已过期，请重新登入");
            }
            httpServletResponse.getWriter().write(JSONUtil.toJsonStr(result));
        } else {
            if (subject.isAuthenticated()) {
                httpServletRequest.setAttribute("status", 403);
                httpServletRequest.setAttribute("error", "您无权限访问 " + uri);
                httpServletRequest.getRequestDispatcher("/403").forward(httpServletRequest, httpServletResponse);
            } else {
                return super.onAccessDenied(request, response);
            }
        }
        return false;
    }

    /**
     * 判断是否是ajax请求
     *
     * @param httpServletRequest
     * @return
     */
    private boolean isAjaxRequest(HttpServletRequest httpServletRequest) {
        return StrUtil.equals(httpServletRequest.getHeader("x-requested-with"), "XMLHttpRequest");
    }
}
