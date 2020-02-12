package sunyu.demo.integration.bigdata.admin.controller;

import cn.hutool.core.util.StrUtil;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import sunyu.demo.integration.bigdata.admin.toolkit.ProtocolTool;

import java.util.*;

@Controller
@RequestMapping("/config")
@CrossOrigin
public class ConfigController extends IndexController {
    ProtocolTool protocolTool = ProtocolTool.INSTANCE;

    @RequestMapping("/queryProtocol")
    @ResponseBody
    public List<Map<String, Object>> queryProtocol(String q) {
        List<String> distinct = new ArrayList<>();
        List<Map<String, Object>> l = new ArrayList<>();
        if (StrUtil.isBlank(q)) {
            l.add(new HashMap<String, Object>() {{
                put("TIME", new HashMap<String, Object>() {{
                    put("cn", "网关接收时间");
                    put("en", "TIME");
                }});
            }});
            l.add(new HashMap<String, Object>() {{
                put("did", new HashMap<String, Object>() {{
                    put("cn", "设备编号");
                    put("en", "did");
                }});
            }});
            List<String> defaultConfig = Arrays.asList("3014", "2205", "2601", "2602", "2603");
            protocolTool.getConfig().forEach((id, configMap) -> {
                if (defaultConfig.contains(id)) {
                    l.add(new HashMap<String, Object>() {{
                        put(id, configMap);
                    }});
                }
            });
        } else {
            if ("TIME".toLowerCase().contains(q.toLowerCase())) {
                if (!distinct.contains(q)) {
                    distinct.add("TIME");
                    l.add(new HashMap<String, Object>() {{
                        put("TIME", new HashMap<String, Object>() {{
                            put("cn", "网关接收时间");
                            put("en", "TIME");
                        }});
                    }});
                }
            }
            if ("did".toLowerCase().contains(q.toLowerCase())) {
                if (!distinct.contains(q)) {
                    distinct.add("did");
                    l.add(new HashMap<String, Object>() {{
                        put("did", new HashMap<String, Object>() {{
                            put("cn", "设备编号");
                            put("en", "did");
                        }});
                    }});
                }
            }
            if ("hex".toLowerCase().contains(q.toLowerCase())) {
                if (!distinct.contains(q)) {
                    distinct.add("hex");
                    l.add(new HashMap<String, Object>() {{
                        put("hex", new HashMap<String, Object>() {{
                            put("cn", "原始报文");
                            put("en", "hex");
                        }});
                    }});
                }
            }
            protocolTool.getConfig().forEach((id, configMap) -> {
                if (id.contains(q)) {
                    if (!distinct.contains(id)) {
                        distinct.add(id);
                        l.add(new HashMap<String, Object>() {{
                            put(id, configMap);
                        }});
                    }
                }
                if (configMap.get("cn") != null && configMap.get("cn").contains(q)) {
                    if (!distinct.contains(id)) {
                        distinct.add(id);
                        l.add(new HashMap<String, Object>() {{
                            put(id, configMap);
                        }});
                    }
                }
                if (configMap.get("en") != null && configMap.get("en").toLowerCase().contains(q.toLowerCase())) {
                    if (!distinct.contains(id)) {
                        distinct.add(id);
                        l.add(new HashMap<String, Object>() {{
                            put(id, configMap);
                        }});
                    }
                }
            });
        }
        return l;
    }

}
