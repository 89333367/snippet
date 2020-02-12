package sunyu.demo.integration.bigdata.admin.config.shiro;


import com.jagregory.shiro.freemarker.ShiroTags;
import freemarker.template.Configuration;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ShiroFreemarkerTagConfig implements InitializingBean {
    @Autowired
    private Configuration configuration;

    @Override
    public void afterPropertiesSet() throws Exception {
        // 加上这句后，可以在页面上使用shiro标签
        // <@shiro.hasPermission name="xxx:xxx">
        //      有权限才能访问的内容
        // </@shiro.hasPermission>

        //<@shiro.principal property="email"/>
        configuration.setSharedVariable("shiro", new ShiroTags());
    }
}
