package sunyu.demo.integration.bigdata.admin.toolkit;

import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.XmlUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.log.StaticLog;
import cn.hutool.setting.dialect.Props;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public enum AlarmTool {
    INSTANCE;

    private Map<String, Map<String, String>> alarmMap = new HashMap<>();

    AlarmTool() {
        try {
            Props props = new Props("application.properties");
            String url = props.getProperty("alarm.config.url");
            List<Map<String, String>> alarmConfig = XmlUtil.readObjectFromXml(HttpUtil.get(url));
            alarmConfig.forEach(m -> alarmMap.put(m.get("故障编码"), m));
        } catch (Exception e) {
            StaticLog.error(e);
        }
    }

    /**
     * 通过故障编码获得中文故障名称
     *
     * @param alarmCode
     * @return
     */
    public String getAlarmCn(String alarmCode) {
        if (MapUtil.isNotEmpty(alarmMap)) {
            Map<String, String> m = alarmMap.get(alarmCode);
            if (MapUtil.isNotEmpty(m)) {
                return m.get("故障名称");
            }
        }
        return alarmCode;
    }
}
