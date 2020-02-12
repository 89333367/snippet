package sunyu.demo.integration.bigdata.admin.controller;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.date.DateField;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUnit;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.lang.Snowflake;
import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.log.StaticLog;
import cn.hutool.poi.excel.ExcelUtil;
import cn.hutool.poi.excel.ExcelWriter;
import cn.hutool.poi.excel.StyleSet;
import cn.hutool.setting.dialect.Props;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.shiro.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import sunyu.demo.integration.bigdata.admin.pojo.ShiroSubject;
import sunyu.demo.integration.bigdata.admin.pojo.TablePlusResult;
import sunyu.demo.integration.bigdata.admin.toolkit.MailTool;
import sunyu.demo.integration.bigdata.admin.toolkit.ProtocolTool;
import sunyu.toolkit.redis.RedisClusterTool;
import sunyu.toolkit.redis.pojo.ScoredEntryBean;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

@Controller
@RequestMapping("/redis")
@CrossOrigin
public class RedisController extends IndexController {
    @Autowired
    MailTool mailTool;

    Props props = new Props("application.properties");
    RedisClusterTool redisClusterTool = new RedisClusterTool(props.getStr("spring.redis.cluster.nodes"));

    ProtocolTool protocolTool = ProtocolTool.INSTANCE;

    Snowflake snowflake = IdUtil.createSnowflake(1, 1);

    @PostMapping("/cluster/ping")
    @ResponseBody
    public String cluster_ping() {
        boolean b = redisClusterTool.ping();
        if (b) {
            return "OK";
        }
        return "Error";
    }

    @PostMapping("/interface/log/size")
    @ResponseBody
    public Integer interface_log_size() {
        return redisClusterTool.llen("interface:log");
    }

    @PostMapping("/interface/token/size")
    @ResponseBody
    public Integer interface_token_size() {
        //interface:token:b51f01d80b6083d9c82d49ed7110205c
        return redisClusterTool.keys("interface:token:*").size();
    }


    @PostMapping("/ne/alarm/today/size")
    @ResponseBody
    public Integer today_alarm_size() {
        //ne:alarm:BYN1010160900121:WB00200004801000303:20190117082645
        return redisClusterTool.keys("ne:alarm:*:*:" + new DateTime().toString("yyyyMMdd") + "*").size();
    }

    @PostMapping("/ne/session/online")
    @ResponseBody
    public Integer ne_session_online(@RequestParam(defaultValue = "30") Integer second) {
        DateTime b = new DateTime();
        DateTime a = DateUtil.offsetSecond(b, -second);
        return redisClusterTool.zcount("ne:session", a.getTime(), true, b.getTime(), true);
    }

    @PostMapping("/ne/session/last")
    @ResponseBody
    public TablePlusResult ne_session_last(@RequestParam(defaultValue = "10") Integer pageSize) {
        TablePlusResult dr = new TablePlusResult();
        List<Map<String, String>> r = new ArrayList<>();
        Collection<ScoredEntryBean> l = redisClusterTool.zrange_entry("ne:session", true, 0, pageSize - 1);
        if (CollUtil.isNotEmpty(l)) {
            l.forEach(scoredEntry -> {
                Map<String, String> m = new HashMap<>();
                m.put("did", scoredEntry.getValue());
                m.put("TIME", new DateTime(scoredEntry.getScore().longValue()).toString("yyyy-MM-dd HH:mm:ss"));
                r.add(m);
            });

            dr.setTotal(Convert.toLong(r.size()));
            dr.setRows(r);
        }
        return dr;
    }

    @PostMapping("/ne/command/last")
    @ResponseBody
    public TablePlusResult ne_command_last(@RequestParam(defaultValue = "10") Integer pageSize) {
        TablePlusResult dr = new TablePlusResult();
        List<Map<String, String>> r = new ArrayList<>();
        //ne:command:BYN1010160900121:35947
        List<String> l = redisClusterTool.keys("ne:command:*:*");
        if (CollUtil.isNotEmpty(l)) {
            for (String key : l) {
                String v = redisClusterTool.get(key);
                if (StrUtil.isNotBlank(v)) {
                    Map<String, String> m = new HashMap<>();
                    m.put("key", key);
                    m.put("value", v);
                    r.add(m);

                    if (r.size() == pageSize) {
                        break;
                    }
                }
            }
            dr.setTotal(Convert.toLong(r.size()));
            dr.setRows(r);
        }
        return dr;
    }

    @PostMapping("/ne/alarm/last")
    @ResponseBody
    public TablePlusResult ne_alarm_last(@RequestParam(defaultValue = "10") Integer pageSize) {
        TablePlusResult dr = new TablePlusResult();
        List<Map<String, String>> r = new ArrayList<>();
        Collection<ScoredEntryBean> l = redisClusterTool.zrange_entry("ne:alarm:time", true, 0, pageSize - 1);
        if (CollUtil.isNotEmpty(l)) {
            l.forEach(scoredEntry -> {
                Map<String, String> m = new HashMap<>();
                m.put("did", scoredEntry.getValue());
                m.put("TIME", new DateTime(scoredEntry.getScore().longValue()).toString("yyyy-MM-dd HH:mm:ss"));
                r.add(m);
            });

            dr.setTotal(Convert.toLong(r.size()));
            dr.setRows(r);
        }
        return dr;
    }

    @PostMapping("/ne/now/run")
    @ResponseBody
    public TablePlusResult ne_now_run(@RequestParam(defaultValue = "10") Integer pageSize) {
        TablePlusResult dr = new TablePlusResult();
        List<Map<String, String>> l = new ArrayList<>();
        Set<Map.Entry<String, String>> s = redisClusterTool.hscan_entry("ne:run:" + new DateTime().getField(DateField.HOUR_OF_DAY));
        if (CollUtil.isNotEmpty(s)) {
            AtomicInteger count = new AtomicInteger(0);
            Iterator<Map.Entry<String, String>> it = s.iterator();
            while (it.hasNext()) {
                Map.Entry<String, String> objectObjectEntry = it.next();
                Map<String, String> m = new HashMap<>();
                m.put("did", objectObjectEntry.getKey());
                m.put("TIME", DateUtil.parse(objectObjectEntry.getValue(), "yyyyMMddHHmmss").toString("yyyy-MM-dd HH:mm:ss"));
                l.add(m);
                if (count.incrementAndGet() == pageSize) {
                    break;
                }
            }

            dr.setTotal(Convert.toLong(l.size()));
            dr.setRows(l);
        }
        return dr;
    }

    @PostMapping("/ne/now/charging")
    @ResponseBody
    public TablePlusResult ne_now_charging(@RequestParam(defaultValue = "10") Integer pageSize) {
        TablePlusResult dr = new TablePlusResult();
        List<Map<String, String>> l = new ArrayList<>();
        Set<Map.Entry<String, String>> s = redisClusterTool.hscan_entry("ne:charge:" + new DateTime().getField(DateField.HOUR_OF_DAY));
        if (CollUtil.isNotEmpty(s)) {
            AtomicInteger count = new AtomicInteger(0);
            Iterator<Map.Entry<String, String>> it = s.iterator();
            while (it.hasNext()) {
                Map.Entry<String, String> objectObjectEntry = it.next();
                Map<String, String> m = new HashMap<>();
                m.put("did", objectObjectEntry.getKey());
                m.put("TIME", DateUtil.parse(objectObjectEntry.getValue(), "yyyyMMddHHmmss").toString("yyyy-MM-dd HH:mm:ss"));
                l.add(m);
                if (count.incrementAndGet() == pageSize) {
                    break;
                }
            }

            dr.setTotal(Convert.toLong(l.size()));
            dr.setRows(l);
        }
        return dr;
    }

    @PostMapping("/farm/session/last")
    @ResponseBody
    public TablePlusResult farm_session_last(@RequestParam(defaultValue = "10") Integer pageSize) {
        TablePlusResult dr = new TablePlusResult();
        List<Map<String, String>> r = new ArrayList<>();
        Collection<ScoredEntryBean> l = redisClusterTool.zrange_entry("farm:session", true, 0, pageSize - 1);
        if (CollUtil.isNotEmpty(l)) {
            l.forEach(scoredEntry -> {
                Map<String, String> m = new HashMap<>();
                m.put("did", scoredEntry.getValue());
                m.put("TIME", new DateTime(scoredEntry.getScore().longValue()).toString("yyyy-MM-dd HH:mm:ss"));
                r.add(m);
            });

            dr.setTotal(Convert.toLong(r.size()));
            dr.setRows(r);
        }
        return dr;
    }

    @PostMapping("/farm/session/online")
    @ResponseBody
    public Integer farm_session_online(@RequestParam(defaultValue = "30") Integer second) {
        DateTime b = new DateTime();
        DateTime a = DateUtil.offsetSecond(b, -second);
        return redisClusterTool.zcount("farm:session", a.getTime(), true, b.getTime(), true);
    }

    @PostMapping("/ne/session/total")
    @ResponseBody
    public Integer ne_session_total() {
        return redisClusterTool.zcount("ne:session", 0, true, new DateTime().getTime(), true);
    }

    @PostMapping("/farm/session/total")
    @ResponseBody
    public Integer farm_session_total() {
        return redisClusterTool.zcount("farm:session", 0, true, new DateTime().getTime(), true);
    }


    @GetMapping("/ne/alarm")
    public ModelAndView ne_alarm_page(Model model, @RequestParam(defaultValue = "") String did) {
        model.addAttribute("did", did.trim());
        return new ModelAndView("redis/ne_alarm", model.asMap());
    }


    @PostMapping("/ne/alarm")
    @ResponseBody
    public TablePlusResult ne_alarm_data(@RequestParam(required = true) String did) {
        TablePlusResult results = new TablePlusResult();
        if (StrUtil.isNotBlank(did)) {
            //ne:alarm:BYN1010160900121:WB00200004801000303:20190117082645
            List<String> keys = redisClusterTool.keys("ne:alarm:" + did.trim() + ":*:" + new DateTime().toString("yyyyMMdd") + "*");
            packageRedisResult(results, keys);
        }
        return results;
    }

    @GetMapping("/ne/route")
    public ModelAndView ne_route_page(Model model, @RequestParam(defaultValue = "") String did) {
        model.addAttribute("did", did.trim());
        return new ModelAndView("redis/ne_route", model.asMap());
    }


    @PostMapping("/ne/route")
    @ResponseBody
    public TablePlusResult ne_route_data(@RequestParam(required = true) String did) {
        TablePlusResult results = new TablePlusResult();
        if (StrUtil.isNotBlank(did)) {
            //ne:route:BYN1010160900174:20190118164000
            List<String> keys = redisClusterTool.keys("ne:route:" + did.trim() + ":" + new DateTime().toString("yyyyMMdd") + "*");
            packageRedisResult(results, keys);
        }
        return results;
    }

    private void packageRedisResult(TablePlusResult results, List<String> keys) {
        if (CollUtil.isNotEmpty(keys)) {
            results.getTitles().put("key", "RedisKey");
            results.getTitles().put("value", "RedisValue");
            redisClusterTool.mget(keys).forEach((key, value) -> {
                Map<String, String> m = new HashMap<>();
                m.put("key", key);
                m.put("value", value);
                results.getRows().add(m);
            });
            results.setTotal(Convert.toLong(results.getRows().size()));
        }
    }

    @GetMapping("/ne/realtime")
    public ModelAndView ne_realtime_page(Model model, @RequestParam(defaultValue = "") String did) {
        model.addAttribute("did", did.trim());
        return new ModelAndView("redis/ne_realtime", model.asMap());
    }

    @GetMapping("/ne/batch/realtime")
    public ModelAndView ne_batch_realtime_page(Model model, @RequestParam(defaultValue = "") String columns) {
        model.addAttribute("columns", columns.trim());
        return new ModelAndView("redis/ne_batch_realtime", model.asMap());
    }

    @PostMapping("/ne/batch/realtime")
    public ModelAndView ne_batch_realtime_data(Model model, @RequestParam(defaultValue = "") String columns, @RequestParam(name = "file", required = false) MultipartFile file) {
        String now = DateUtil.now();
        ShiroSubject shiroSubject = (ShiroSubject) SecurityUtils.getSubject().getPrincipal();
        List<String> cs = Arrays.asList(columns.split(","));
        String redisKeyPre = "ne:realtime:";
        String fileName = "最后数据";
        model.addAttribute("columns", columns.trim());
        if (file.isEmpty()) {
            model.addAttribute("status", "10001");
            model.addAttribute("msg", "请上传设备信息！");
        } else {
            CompletableFuture.supplyAsync(() -> {
                String errMessage = null;
                List<String> keys = new ArrayList<>();
                try {
                    for (List<Object> rows : ExcelUtil.getReader(file.getInputStream()).read(1)) {
                        if (StrUtil.isNotBlank(rows.get(0).toString())) {
                            //拼装设备编号
                            keys.add(redisKeyPre + rows.get(0).toString().trim());
                        }
                    }
                    if (CollUtil.isNotEmpty(keys)) {
                        ThreadUtil.execAsync(() -> {
                            List<Map<String, Object>> datas = new ArrayList<>();
                            //查询redis
                            redisClusterTool.mget(keys).forEach((k, v) -> {
                                if (StrUtil.isNotBlank(v)) {
                                    JSONObject o = JSON.parseObject(v);
                                    if (StrUtil.isNotBlank(columns)) {
                                        Map<String, Object> m = new HashMap<>();
                                        for (String c : cs) {
                                            if (o.containsKey(c)) {
                                                m.put(c, o.getString(c));
                                            }
                                        }
                                        datas.add(m);
                                    } else {
                                        datas.add(o.getInnerMap());
                                    }
                                } else {
                                    //没有找到设备信息的时候，给一个空的行
                                    datas.add(new HashMap<String, Object>() {{
                                        put("did", k.substring(redisKeyPre.length()));
                                    }});
                                }
                            });

                            //生成excel
                            ExcelWriter bigWriter = ExcelUtil.getBigWriter();
                            StyleSet style = bigWriter.getStyleSet();
                            style.getHeadCellStyle().setAlignment(HorizontalAlignment.LEFT);
                            style.getCellStyle().setAlignment(HorizontalAlignment.LEFT);
                            List<Map<String, String>> newDatas = new ArrayList<>();
                            Map<String, String> titles = new HashMap<>();

                            //新拼装title
                            for (Map<String, Object> data : datas) {
                                data.forEach((k, v) -> {
                                    titles.put(k, k);
                                });
                            }

                            //整理缺失key的数据信息
                            for (Map<String, Object> data : datas) {
                                titles.forEach((k, v) -> {
                                    if (!data.containsKey(k)) {
                                        data.put(k, "");
                                    }
                                });
                            }

                            //转换标题
                            protocolTool.internalProtocolTitleMapConvert(titles);

                            //拼装数据
                            for (Map<String, Object> data : datas) {
                                Map<String, String> m = new HashMap<>();
                                data.forEach((k, v) -> {
                                    m.put(k, (String) v);
                                });
                                Map<String, String> cm = protocolTool.internalProtocolMapConvertValue(m);//转换值
                                Map<String, String> tm = new HashMap<>();//转换标题后带值的
                                cm.forEach((k, v) -> {
                                    tm.put(titles.get(k), v);
                                });
                                newDatas.add(tm);
                            }

                            //写入到excel文件
                            bigWriter.write(newDatas, true);
                            File emailFile = new File(fileName + snowflake.nextId() + ".xlsx");
                            bigWriter.flush(emailFile);
                            bigWriter.close();
                            mailTool.sendMail(Arrays.asList(shiroSubject.getEmail()), fileName, fileName + " 请下载附件查看<br/>数据导出请求时间：" + now, true, emailFile);
                            FileUtil.del(emailFile);
                        });
                    }
                } catch (IOException e) {
                    StaticLog.error(e);
                    errMessage = e.getMessage();
                }
                return errMessage;
            }).handle((errMessage, throwable) -> {
                if (throwable != null) {
                    mailTool.sendMail(Arrays.asList(shiroSubject.getEmail()), fileName + "导出失败", fileName + " 导出失败<br/>数据导出请求时间：" + now + " <br/>失败原因：" + throwable.getMessage(), true);
                }
                if (StrUtil.isNotBlank(errMessage)) {
                    mailTool.sendMail(Arrays.asList(shiroSubject.getEmail()), fileName + "导出失败", fileName + " 导出失败<br/>数据导出请求时间：" + now + " <br/>失败原因：" + errMessage, true);
                }
                return null;
            });
            model.addAttribute("status", "10000");
            model.addAttribute("msg", "您要导出的数据后台正在排队处理，预计半小时内会以附件的形式发送到您的 " + shiroSubject.getEmail() + " 邮箱，请注意查收。");
        }
        return new ModelAndView("redis/ne_batch_realtime", model.asMap());
    }

    @PostMapping("/ne/realtime")
    @ResponseBody
    public TablePlusResult ne_realtime_data(@RequestParam(required = true) String did) {
        TablePlusResult results = new TablePlusResult();
        if (StrUtil.isNotBlank(did)) {
            //ne:realtime:BYN0000000000000
            String key = "ne:realtime:" + did.trim();
            String realtime = redisClusterTool.get(key);
            if (StrUtil.isNotBlank(realtime)) {
                results.getTitles().put("key", "Key");
                results.getTitles().put("value", "Value");

                Map<String, String> m = new HashMap<>();
                m.put("key", "RedisKey");
                m.put("value", key);
                results.getRows().add(m);

                Map<String, String> obj = JSON.parseObject(realtime, Map.class);
                Map<String, String> o = protocolTool.internalProtocolMapConvertValue(obj);
                Map<String, String> titleMap = new HashMap<>();
                o.forEach((k, v) -> {
                    titleMap.put(k, k);
                });
                protocolTool.internalProtocolTitleMapConvert(titleMap);
                o.forEach((k, v) -> {
                    Map<String, String> m1 = new HashMap<>();
                    m1.put("key", titleMap.get(k));
                    m1.put("value", String.valueOf(v));
                    results.getRows().add(m1);
                });

                results.setTotal(Convert.toLong(results.getRows().size()));
            }
        }
        return results;
    }

    @GetMapping("/ne/fence")
    public ModelAndView ne_fence_page(Model model, @RequestParam(defaultValue = "") String did) {
        model.addAttribute("did", did.trim());
        return new ModelAndView("redis/ne_fence", model.asMap());
    }


    @PostMapping("/ne/fence")
    @ResponseBody
    public TablePlusResult ne_fence_data(@RequestParam(required = true) String did) {
        TablePlusResult results = new TablePlusResult();
        if (StrUtil.isNotBlank(did)) {
            String fence = redisClusterTool.hget("ne:fence", did.trim());
            if (StrUtil.isNotBlank(fence)) {
                results.getTitles().put("fence", "围栏信息");
                Map<String, String> m = new HashMap<>();
                m.put("fence", fence);
                results.getRows().add(m);

                results.setTotal(Convert.toLong(results.getRows().size()));
            }
        }
        return results;
    }

    @GetMapping("/ne/command")
    public ModelAndView ne_command_page(Model model, @RequestParam(defaultValue = "") String did) {
        model.addAttribute("did", did.trim());
        return new ModelAndView("redis/ne_command", model.asMap());
    }


    @PostMapping("/ne/command")
    @ResponseBody
    public TablePlusResult ne_command_data(@RequestParam(required = true) String did) {
        TablePlusResult results = new TablePlusResult();
        if (StrUtil.isNotBlank(did)) {
            //ne:command:BYN1010160900298:35047
            List<String> keys = redisClusterTool.keys("ne:command:" + did.trim() + ":*");
            packageRedisResult(results, keys);
        }
        return results;
    }

    @GetMapping("/farm/realtime")
    public ModelAndView farm_realtime_page(Model model, @RequestParam(defaultValue = "") String did) {
        model.addAttribute("did", did.trim());
        return new ModelAndView("redis/farm_realtime", model.asMap());
    }


    @PostMapping("/farm/realtime")
    @ResponseBody
    public TablePlusResult farm_realtime_data(@RequestParam(required = true) String did) {
        TablePlusResult results = new TablePlusResult();
        if (StrUtil.isNotBlank(did)) {
            //farm:realtime:CAT2018110100001
            String key = "farm:realtime:" + did.trim();
            String realtime = redisClusterTool.get(key);
            if (StrUtil.isNotBlank(realtime)) {
                results.getTitles().put("key", "Key");
                results.getTitles().put("value", "Value");

                Map<String, String> m = new HashMap<>();
                m.put("key", "RedisKey");
                m.put("value", key);
                results.getRows().add(m);

                Map<String, String> obj = JSON.parseObject(realtime, Map.class);
                Map<String, String> o = protocolTool.internalProtocolMapConvertValue(obj);
                Map<String, String> titleMap = new HashMap<>();
                o.forEach((k, v) -> {
                    titleMap.put(k, k);
                });
                protocolTool.internalProtocolTitleMapConvert(titleMap);
                o.forEach((k, v) -> {
                    Map<String, String> m1 = new HashMap<>();
                    m1.put("key", titleMap.get(k));
                    m1.put("value", String.valueOf(v));
                    results.getRows().add(m1);
                });

                results.setTotal(Convert.toLong(results.getRows().size()));
            }
        }
        return results;
    }


    @GetMapping("/farm/fence")
    public ModelAndView farm_fence_page(Model model, @RequestParam(defaultValue = "") String did) {
        model.addAttribute("did", did.trim());
        return new ModelAndView("redis/farm_fence", model.asMap());
    }


    @PostMapping("/farm/fence")
    @ResponseBody
    public TablePlusResult farm_fence_data(@RequestParam(required = true) String did) {
        TablePlusResult results = new TablePlusResult();
        if (StrUtil.isNotBlank(did)) {
            String fence = redisClusterTool.hget("farm:fence", did.trim());
            if (StrUtil.isNotBlank(fence)) {
                results.getTitles().put("fence", "围栏信息");
                Map<String, String> m = new HashMap<>();
                m.put("fence", fence);
                results.getRows().add(m);

                results.setTotal(Convert.toLong(results.getRows().size()));
            }
        }
        return results;
    }

    //新能源在线状态
    @GetMapping("/ne/online")
    public ModelAndView ne_online_page(Model model, @RequestParam(defaultValue = "") String did,
                                       @RequestParam(defaultValue = "10") Integer second) {
        model.addAttribute("did", did.trim());
        model.addAttribute("second", second);
        return new ModelAndView("redis/ne_online", model.asMap());
    }

    //新能源在线状态和最后一次信息
    @PostMapping("/ne/online")
    @ResponseBody
    public TablePlusResult ne_online_data(@RequestParam(required = true) String did,
                                          @RequestParam(defaultValue = "10") Integer second) {
        TablePlusResult results = new TablePlusResult();
        if (StrUtil.isNotBlank(did)) {
            //查询新能源在线状态
            //ne:heartbeat:BYN0000000000000
            String onlineKey = "ne:heartbeat:" + did.trim();
            String online = "不在线";
            DateTime lastDate = null;
            String lastOnline = redisClusterTool.get(onlineKey);
            if (StrUtil.isNotBlank(lastOnline)) {
                lastDate = new DateTime(lastOnline, "yyyyMMddHHmmss");
                long between = DateUtil.between(new Date(), lastDate, DateUnit.SECOND);
                online = between > second ? "不在线" : "在线";
            }


            //ne:realtime:BYN0000000000000
            String key = "ne:realtime:" + did.trim();
            String realtime = redisClusterTool.get(key);
            if (StrUtil.isNotBlank(realtime)) {
                results.getTitles().put("key", "Key");
                results.getTitles().put("value", "Value");

                //最后上传时间
                Map<String, String> mt = new HashMap<>();
                mt.put("key", "最后上传时间");
                mt.put("value", lastDate.toString("yyyy-MM-dd HH:mm:ss"));
                results.getRows().add(mt);

                //设备状态
                Map<String, String> mo = new HashMap<>();
                mo.put("key", "设备状态");
                mo.put("value", online);
                results.getRows().add(mo);

                Map<String, String> m = new HashMap<>();
                m.put("key", "RedisKey");
                m.put("value", key);
                results.getRows().add(m);

                Map<String, String> obj = JSON.parseObject(realtime, Map.class);
                Map<String, String> o = protocolTool.internalProtocolMapConvertValue(obj);
                Map<String, String> titleMap = new HashMap<>();
                o.forEach((k, v) -> {
                    titleMap.put(k, k);
                });
                protocolTool.internalProtocolTitleMapConvert(titleMap);
                o.forEach((k, v) -> {
                    Map<String, String> m1 = new HashMap<>();
                    m1.put("key", titleMap.get(k));
                    m1.put("value", String.valueOf(v));
                    results.getRows().add(m1);
                });

                results.setTotal(Convert.toLong(results.getRows().size()));
            }
        }
        return results;
    }


    //农机在线状态
    @GetMapping("/farm/online")
    public ModelAndView farm_online_page(Model model, @RequestParam(defaultValue = "") String did,
                                         @RequestParam(defaultValue = "10") Integer second) {
        model.addAttribute("did", did.trim());
        model.addAttribute("second", second);
        return new ModelAndView("redis/farm_online", model.asMap());
    }

    //农机在线状态和最后一次信息
    @PostMapping("/farm/online")
    @ResponseBody
    public TablePlusResult farm_online_data(@RequestParam(required = true) String did,
                                            @RequestParam(defaultValue = "10") Integer second) {
        TablePlusResult results = new TablePlusResult();
        if (StrUtil.isNotBlank(did)) {
            //查询农机在线状态
            //ne:heartbeat:BYN0000000000000
            String online = "不在线";
            DateTime lastDate = null;
            Double zscore = redisClusterTool.zscore("farm:session", did.trim());
            if (null != zscore) {
                lastDate = new DateTime(zscore.longValue());
                long between = DateUtil.between(new Date(), lastDate, DateUnit.SECOND);
                online = between > second ? "不在线" : "在线";
            }

            //ne:realtime:BYN0000000000000
            String key = "farm:realtime:" + did.trim();
            String realtime = redisClusterTool.get(key);
            if (StrUtil.isNotBlank(realtime)) {
                results.getTitles().put("key", "Key");
                results.getTitles().put("value", "Value");

                //最后上传时间
                Map<String, String> mt = new HashMap<>();
                mt.put("key", "最后上传时间");
                mt.put("value", lastDate.toString("yyyy-MM-dd HH:mm:ss"));
                results.getRows().add(mt);

                //设备状态
                Map<String, String> mo = new HashMap<>();
                mo.put("key", "设备状态");
                mo.put("value", online);
                results.getRows().add(mo);

                Map<String, String> m = new HashMap<>();
                m.put("key", "RedisKey");
                m.put("value", key);
                results.getRows().add(m);

                Map<String, String> obj = JSON.parseObject(realtime, Map.class);
                Map<String, String> o = protocolTool.internalProtocolMapConvertValue(obj);
                Map<String, String> titleMap = new HashMap<>();
                o.forEach((k, v) -> {
                    titleMap.put(k, k);
                });
                protocolTool.internalProtocolTitleMapConvert(titleMap);
                o.forEach((k, v) -> {
                    Map<String, String> m1 = new HashMap<>();
                    m1.put("key", titleMap.get(k));
                    m1.put("value", String.valueOf(v));
                    results.getRows().add(m1);
                });

                results.setTotal(Convert.toLong(results.getRows().size()));
            }
        }
        return results;
    }


}
