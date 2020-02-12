package sunyu.demo.integration.bigdata.admin.runner;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.resource.ResourceUtil;
import cn.hutool.core.util.CharsetUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.SecureUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import sunyu.demo.integration.bigdata.admin.pojo.ShiroSubject;
import sunyu.demo.integration.bigdata.admin.service.SubjectService;

@Component
public class InitDataBase implements CommandLineRunner {
    @Autowired
    SubjectService subjectService;

    @Override
    public void run(String... args) throws Exception {
        for (String line : FileUtil.readLines(ResourceUtil.getResource("email-data.txt"), CharsetUtil.UTF_8)) {
            if (StrUtil.isNotBlank(line)) {
                String[] sl = line.split("\\t");
                String name = sl[0];
                String email = sl[1];
                if (subjectService.count(Wrappers.<ShiroSubject>lambdaQuery().eq(ShiroSubject::getEmail, email)) == 0) {
                    ShiroSubject shiroSubject = new ShiroSubject();
                    shiroSubject.setId(subjectService.count() + 1);
                    shiroSubject.setDisabled(0);
                    shiroSubject.setEmail(email);
                    shiroSubject.setName(name);
                    shiroSubject.setPassword(SecureUtil.md5("bcld"));
                    subjectService.save(shiroSubject);
                }
            }
        }
    }

}
