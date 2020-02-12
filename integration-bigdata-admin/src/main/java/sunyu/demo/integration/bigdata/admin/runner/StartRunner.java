package sunyu.demo.integration.bigdata.admin.runner;

import cn.hutool.core.util.ArrayUtil;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import sunyu.demo.integration.bigdata.admin.toolkit.HostUtil;

/**
 * @author SunYu
 */
@Component
public class StartRunner implements CommandLineRunner {
    private HostUtil hostUtil;

    public StartRunner(HostUtil hostUtil) {
        this.hostUtil = hostUtil;
    }

    @Override
    public void run(String... args) throws Exception {
        System.out.println(hostUtil.toString() + " 已启动");
        if (ArrayUtil.isNotEmpty(args)) {
            System.out.println("动态参数：");
            for (String arg : args) {
                System.out.println(arg);
            }
        }
    }
}
