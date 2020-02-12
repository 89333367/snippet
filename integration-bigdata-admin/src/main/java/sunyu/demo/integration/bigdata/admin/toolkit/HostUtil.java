package sunyu.demo.integration.bigdata.admin.toolkit;

import cn.hutool.core.net.NetUtil;
import cn.hutool.core.util.StrUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.InetAddress;

/**
 * @author SunYu
 */
@Component
public class HostUtil {
    @Value("${server.port}")
    private Integer serverPort;
    @Value("${spring.application.name}")
    private String springApplicationName;

    private InetAddress host = NetUtil.getLocalhost();

    public Integer getServerPort() {
        return serverPort;
    }

    public void setServerPort(Integer serverPort) {
        this.serverPort = serverPort;
    }

    public String getSpringApplicationName() {
        return springApplicationName;
    }

    public void setSpringApplicationName(String springApplicationName) {
        this.springApplicationName = springApplicationName;
    }

    public InetAddress getHost() {
        return host;
    }

    @Override
    public String toString() {
        return StrUtil.format("{} ( {}:{} )", springApplicationName, host.getHostAddress(), serverPort);
    }
}
