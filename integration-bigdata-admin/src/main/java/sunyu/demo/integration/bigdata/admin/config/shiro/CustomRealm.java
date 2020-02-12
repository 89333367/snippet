package sunyu.demo.integration.bigdata.admin.config.shiro;

import cn.hutool.crypto.SecureUtil;
import cn.hutool.log.StaticLog;
import cn.hutool.setting.dialect.Props;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.apache.shiro.authc.*;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.springframework.beans.factory.annotation.Autowired;
import sunyu.demo.integration.bigdata.admin.mapper.SubjectMapper;
import sunyu.demo.integration.bigdata.admin.pojo.ShiroSubject;

/**
 * 自定义授权领域
 */
public class CustomRealm extends AuthorizingRealm {
    Props props = new Props("application.properties");

    @Autowired
    SubjectMapper subjectMapper;

    /**
     * 获取身份验证信息，调用subject.login(token)的时候触发
     * Shiro中，最终是通过 Realm 来获取应用程序中的用户、角色及权限信息的。
     *
     * @param authenticationToken 用户身份信息 token
     * @return 返回封装了用户信息的 AuthenticationInfo 实例
     */
    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken authenticationToken) throws AuthenticationException {
        UsernamePasswordToken token = (UsernamePasswordToken) authenticationToken;
        String username = token.getUsername();
        String password = new String(token.getPassword());
        StaticLog.debug("{} 进行身份认证", username);
        // 从数据库获取对应用户名和密码的用户
        ShiroSubject shiroSubject = subjectMapper.selectOne(Wrappers.<ShiroSubject>lambdaQuery().eq(ShiroSubject::getEmail, username).eq(ShiroSubject::getPassword, SecureUtil.md5(password)));
        if (null == shiroSubject) {
            throw new AccountException("用户名或密码不正确");
        }

        if (props.getStr("shiro.sa").equals(username)) {//超级管理员
            shiroSubject.getRoles().add("shiro");
            StaticLog.info("赋予 {} shiro角色", username);
        } else {
            if (shiroSubject.getDisabled() == 1) {
                throw new DisabledAccountException(shiroSubject.getEmail() + " 已被禁用");
            }
        }

        //普通用户直接赋予bigdata角色，可以管理大数据资源
        shiroSubject.getRoles().add("bigdata");
        StaticLog.info("赋予 {} bigdata角色", username);

        return new SimpleAuthenticationInfo(shiroSubject, password, getName());
    }

    /**
     * 获取授权信息
     *
     * @param principalCollection
     * @return
     */
    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principalCollection) {
        ShiroSubject shiroSubject = (ShiroSubject) getAvailablePrincipal(principalCollection);
        SimpleAuthorizationInfo info = new SimpleAuthorizationInfo();
        info.setRoles(shiroSubject.getRoles());
        info.setStringPermissions(shiroSubject.getPermissions());
        return info;
    }
}
