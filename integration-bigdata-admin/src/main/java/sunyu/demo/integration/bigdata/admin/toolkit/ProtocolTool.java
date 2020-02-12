package sunyu.demo.integration.bigdata.admin.toolkit;

import cn.hutool.core.codec.Base64;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.*;
import cn.hutool.http.HttpRequest;
import cn.hutool.log.StaticLog;
import cn.hutool.setting.dialect.Props;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 协议解析单例类
 * <p>
 * 通过网络上的config.xml配置文件，解析内部协议字符串为map对象，key：内部协议编号，value：值
 *
 * @author 孙宇
 */
public enum ProtocolTool {

    INSTANCE;

    AlarmTool alarmTool = AlarmTool.INSTANCE;

    Map<String, Map<String, String>> config = new HashMap<>();// 代表整个config配置，key：内部协议数字key，value是配置信息
    Map<String, Map<String, String>> enConfig = new HashMap<>();// 代表整个config配置，key：内部协议英文key，value是配置信息

    public Map<String, Map<String, String>> getEnConfig() {
        return enConfig;
    }

    public Map<String, Map<String, String>> getConfig() {
        return config;
    }

    ProtocolTool() {
        Props props = new Props("application.properties");
        String url = props.getStr("protocol.config.url");
        Document xml = XmlUtil.readXML(HttpRequest.get(url).execute().bodyStream());
        List<Element> elements = XmlUtil.transElements(XmlUtil.getNodeListByXPath("//Element/prop", xml));
        elements.forEach(element -> {
            String id = element.getAttribute("id");
            Map<String, String> p = new HashMap<>();
            XmlUtil.transElements(element.getChildNodes()).forEach(element1 -> {
                p.put(element1.getTagName(), element1.getTextContent().trim());
            });
            config.put(id, p);

            if (StrUtil.isNotBlank(p.get("en"))) {
                enConfig.put(p.get("en"), p);
            }
        });
    }

    /**
     * 内部协议map二次转换值，主要用于前台显示
     *
     * @param protocolMap
     * @return
     */
    public Map<String, String> internalProtocolMapConvertValue(Map<String, String> protocolMap) {
        Map<String, String> m = new HashMap<>();
        protocolMap.forEach((k, v) -> {
            if (StrUtil.isNotBlank(v)) {
                Map<String, String> configMap = config.get(k);// 2
                String newId = null;
                if (MapUtil.isEmpty(configMap)) {
                    newId = protocolMap.get("params3") + "_" + k;// GETARG_2
                    configMap = config.get(newId);
                }
                if (MapUtil.isNotEmpty(configMap)) {// 按配置文件转换值
                    String newValue = null;
                    String ref = configMap.get("ref");// <ref>3003</ref>
                    String convertResult = configMap.get("convertResult");// <convertResult>true</convertResult>
                    if (StrUtil.isNotBlank(ref)) {// 有引用转换配置
                        String refValue = protocolMap.get(ref);
                        if (StrUtil.isNotBlank(refValue)) {
                            newValue = configMap.get("value_" + refValue + "_" + v);// <value_4_3>F507PHEV混动</value_4_3>
                        }
                    } else if (StrUtil.isNotBlank(convertResult) && convertResult.equals("true")) {// 有转换配置
                        newValue = configMap.get("value_" + v);// <value_3>关闭状态</value_3>
                    }
                    if (StrUtil.isBlank(newValue)) {
                        newValue = v;// 如果转换后的值还是空，那么继续使用之前的值
                    }
                    if (StrUtil.isNotBlank(newId)) {
                        m.put(newId, newValue);
                    } else {
                        m.put(k, newValue);
                    }
                } else {
                    m.put(k, v);
                }

                // 按业务逻辑转换值
                if (k.equals("TIME") && protocolMap.containsKey("totalRepeat")) {
                    // 如果是重复数据的TIME，那么需要解析多个时间
                    try {
                        StringBuilder t = new StringBuilder();
                        boolean b = false;
                        for (String time : v.split(" // ")) {
                            if (b) {
                                t.append(" // ");
                            }
                            t.append(new DateTime(time, "yyyyMMddHHmmss").toString("yyyy-MM-dd HH:mm:ss"));
                            b = true;
                        }
                        m.put(k, t.toString());
                    } catch (Exception e) {
                        StaticLog.error(e);
                    }
                } else if (k.equals("TIME") || k.equals("3014") || k.equals("startTime") || k.equals("endTime")
                        || k.equals("GPSDateTime") || k.equals("jobStartTime") || k.equals("jobEndTime")
                        || k.equals("responseTime") || k.equals("sendTime")) {
                    m.put(k, new DateTime(v, "yyyyMMddHHmmss").toString("yyyy-MM-dd HH:mm:ss"));
                } else if (k.equals("2601")) {
                    if (v.equals("0")) {
                        m.put(k, "已定位");
                    } else {
                        m.put(k, "未定位");
                    }
                } else if (k.equals("2802") || k.equals("2804") || k.equals("2806") || k.equals("2808")
                        || k.equals("2810")) {
                    StringBuilder alarm = new StringBuilder();
                    for (String code : v.split("\\|")) {
                        alarm.append("|" + alarmTool.getAlarmCn(code));
                    }
                    m.put(k, alarm.toString().substring(1));
                } else if (k.equals("errType")) {
                    // 1 :gps时间早于服务器时间, 2: 设备号解析出错, 3: 其它
                    switch (Integer.parseInt(v)) {
                    case 1:
                        m.put("errType", "GPS时间错误");
                        break;
                    case 2:
                        m.put("errType", "设备号解析出错");
                        break;
                    case 3:
                        m.put("errType", "其他");
                        break;
                    }
                } else if (k.equals("type") && protocolMap.containsKey("totalRepeat")) {
                    // 说明是重复数据的type类型
                    switch (Integer.parseInt(v)) {
                    case 1:
                        m.put(k, "GPS时间重复，上报内容相同");
                        break;
                    case 2:
                        m.put(k, "GPS时间重复，上报内容不同");
                        break;
                    }
                }
            } else {
                m.put(k, "");
            }
        });
        return m;
    }

    /**
     * 内部协议字符串转map，主要用于大数据存储
     *
     * @param protocolStr
     * @return
     */
    public Map<String, String> internalProtocolConvertMap(String protocolStr) {
        if (StrUtil.isNotBlank(protocolStr)) {
            String[] data = protocolStr.split("\\$");
            // 1. 消息元数据间使用一个“$”为分隔符。
            // 2. 消息结构：消息前缀;序列号;终端 ID;命令标识;参数明细。固定 5 列。
            // 3. 前缀主要有 SUBMIT 和 REPORT 两种，SUBMIT 表示主动发送，REPORT 表示应答。
            // 4. 序列号主要用于下行指令的状态通知匹配，主动上传的消息序列号默认为 1。
            // 5. 国标终端的 VIN 用于终端 ID，其它终端 ID 为博创自定义 ID。
            // 6. 参数明细包含多个参数，单个参数以 KEY:VALUE 形式，多个参数以半角逗号分隔。
            // 7. 车辆登入示例： SUBMIT$1$LVBV4J0B2AJ063987$LOGIN$TIME:20150623120000,1001:1
            if (data.length == 5 && StrUtil.isNotBlank(data[2])) {// 如果是内部协议的5段，并且，设备编号不为空
                Map<String, String> params = new HashMap<>();
                params.put("params0", data[0]);// 前缀,SUBMIT 和REPORT 两种，SUBMIT 表示主动发送，REPORT 表示应答
                params.put("params1", data[1]);// 消息id
                params.put("params2", data[2]);// 终端编号
                params.put("params3", data[3]);// 命令标识,PACKET,LINKSTATUS,TERMIN,TERMOUT,REALTIME,HISTORY,GETARG,SETARG,CONTROL,UPDATE,NOTIFY
                params.put("did", params.get("params2"));// 冗余key，便于检索
                if (params.get("did") == null || params.get("did").equalsIgnoreCase("null")
                        || !ReUtil.isMatch("^[a-zA-Z0-9]*\\s?[a-zA-Z0-9]*$", params.get("did"))) {
                    return null;// 如果设备编号不符合编号规则
                }
                String[] datas = data[4].split(",");// 数据部分
                for (String kv : datas) {// 拼装内部协议key数据
                    String[] keyValue = kv.split(":");
                    if (keyValue.length == 2) {// key和value都有，例如：key:value
                        String id = keyValue[0];// 内部协议数字key
                        String value = keyValue[1];// 网关转换后的值
                        if (StrUtil.isNotBlank(id) && StrUtil.isNotBlank(value)) {
                            // 进行value的转换，这里目前只需要转换base64hex与base64
                            value = convertValue(data, id, value);

                            params.put(id, value);// 使用内部协议的数字key
                        }
                    }
                }
                return params;
            }
        }
        return null;
    }

    private String convertValue(String[] data, String id, String value) {
        Map<String, String> configMap = config.get(id);// 2
        if (MapUtil.isEmpty(configMap)) {
            configMap = config.get(data[3] + "_" + id);// GETARG_2
        }
        if (MapUtil.isNotEmpty(configMap)) {
            String base64Hex = configMap.get("base64Hex");
            String base64 = configMap.get("base64");
            if (StrUtil.isNotBlank(base64Hex) && base64Hex.equals("true")) {
                StringBuilder newValue = new StringBuilder();
                String[] vv = value.split("\\|");
                for (int i = 0; i < vv.length; i++) {
                    if (i > 0) {
                        newValue.append("|");
                    }
                    newValue.append(HexUtil.encodeHexStr(Base64.decode(vv[i])));
                }
                value = newValue.toString();
            } else if (StrUtil.isNotBlank(base64) && base64.equals("true")) {
                StringBuilder newValue = new StringBuilder();
                String[] vv = value.split("\\|");
                for (int i = 0; i < vv.length; i++) {
                    if (i > 0) {
                        newValue.append("|");
                    }
                    newValue.append(Base64.decodeStr(vv[i], CharsetUtil.UTF_8));
                }
                value = newValue.toString();
            }
        }
        return value;
    }

    /**
     * 将内部协议的标题转换成能看懂的标题
     *
     * @param protocolTitleMap 内部协议标题map
     * @return 能看懂的标题map
     */
    public Map<String, String> internalProtocolTitleMapConvert(Map<String, String> protocolTitleMap) {
        protocolTitleMap.forEach((k, v) -> {
            Map<String, String> enProtocolConf = getEnConfig().get(k);// 英文key与中文对应关系
            if (enProtocolConf != null) {
                String enName = enProtocolConf.get("en");
                String cnName = enProtocolConf.get("cn");
                if (StrUtil.isNotBlank(enName)) {
                    protocolTitleMap.put(k, String.format("%s(%s)", cnName, k));
                }
            } else {
                Map<String, String> protocolConf = getConfig().get(k);// 数字key与中文对应关系
                if (protocolConf != null) {
                    String cnName = protocolConf.get("cn");
                    if (StrUtil.isNotBlank(cnName)) {
                        protocolTitleMap.put(k, String.format("%s(%s)", cnName, k));
                    }
                } else {
                    if (k.equals("hex")) {
                        protocolTitleMap.put(k, "原始报文(hex)");
                    } else if (k.equals("errType")) {
                        protocolTitleMap.put(k, "错误类型(errType)");
                    } else if (k.equals("did")) {
                        protocolTitleMap.put(k, "设备编号(did)");
                    } else if (k.equals("params0")) {
                        protocolTitleMap.put(k, "消息前缀(params0)");
                    } else if (k.equals("params1")) {
                        protocolTitleMap.put(k, "消息ID(params1)");
                    } else if (k.equals("params2")) {
                        protocolTitleMap.put(k, "终端编号(params2)");
                    } else if (k.equals("transform")) {
                        protocolTitleMap.put(k, "内部协议(transform)");
                    } else if (k.equals("command")) {
                        protocolTitleMap.put(k, "指令(command)");
                    } else if (k.equals("replyCommand")) {
                        protocolTitleMap.put(k, "应答指令(replyCommand)");
                    } else if (k.equals("responseTime")) {
                        protocolTitleMap.put(k, "应答时间(responseTime)");
                    } else if (k.equals("sendTime")) {
                        protocolTitleMap.put(k, "发送时间(sendTime)");
                    } else if (k.equals("status")) {
                        protocolTitleMap.put(k, "状态(status)");
                    } else if (k.equals("serNo")) {
                        protocolTitleMap.put(k, "序号(serNo)");
                    } else if (k.equals("content")) {
                        protocolTitleMap.put(k, "内容(content)");
                    } else if (k.equals("environment")) {
                        protocolTitleMap.put(k, "环境(environment)");
                    } else if (k.equals("user")) {
                        protocolTitleMap.put(k, "用户(user)");
                    } else if (k.equals("code")) {
                        protocolTitleMap.put(k, "编码(code)");
                    } else if (k.equals("startAddress")) {
                        protocolTitleMap.put(k, "开始地点(startAddress)");
                    } else if (k.equals("startLon")) {
                        protocolTitleMap.put(k, "开始经度(startLon)");
                    } else if (k.equals("startLat")) {
                        protocolTitleMap.put(k, "开始纬度(startLat)");
                    } else if (k.equals("startTime")) {
                        protocolTitleMap.put(k, "开始时间(startTime)");
                    } else if (k.equals("endAddress")) {
                        protocolTitleMap.put(k, "结束地点(endAddress)");
                    } else if (k.equals("endLon")) {
                        protocolTitleMap.put(k, "结束经度(endLon)");
                    } else if (k.equals("endLat")) {
                        protocolTitleMap.put(k, "结束纬度(endLat)");
                    } else if (k.equals("endTime")) {
                        protocolTitleMap.put(k, "结束时间(endTime)");
                    } else if (k.equals("level")) {
                        protocolTitleMap.put(k, "级别(level)");
                    } else if (k.equals("serialNumber")) {
                        protocolTitleMap.put(k, "序号(serialNumber)");
                    } else if (k.equals("totalRepeat")) {
                        protocolTitleMap.put(k, "重复次数(totalRepeat)");
                    } else if (k.equals("type")) {
                        protocolTitleMap.put(k, "类型(type)");
                    } else if (k.equals("groupId")) {
                        protocolTitleMap.put(k, "组织ID(groupId)");
                    } else if (k.equals("groupName")) {
                        protocolTitleMap.put(k, "组织名称(groupName)");
                    } else if (k.equals("can_type")) {
                        protocolTitleMap.put(k, "车辆类型(can_type)");
                    } else if (k.equals("vehicleModel")) {
                        protocolTitleMap.put(k, "车辆模型(vehicleModel)");
                    } else if (k.equals("vin")) {
                        protocolTitleMap.put(k, "车架号(vin)");
                    } else if (k.equals("lpn")) {
                        protocolTitleMap.put(k, "车牌号(lpn)");
                    }
                }
            }
        });
        return protocolTitleMap;
    }
}