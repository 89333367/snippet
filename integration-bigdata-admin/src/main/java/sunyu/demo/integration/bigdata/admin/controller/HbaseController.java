package sunyu.demo.integration.bigdata.admin.controller;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.date.DateField;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.lang.Snowflake;
import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.ReUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.SecureUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import cn.hutool.log.StaticLog;
import cn.hutool.poi.excel.ExcelUtil;
import cn.hutool.poi.excel.ExcelWriter;
import cn.hutool.poi.excel.StyleSet;
import cn.hutool.setting.dialect.Props;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.shiro.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import sunyu.demo.integration.bigdata.admin.pojo.ShiroSubject;
import sunyu.demo.integration.bigdata.admin.pojo.TablePlusResult;
import sunyu.demo.integration.bigdata.admin.pojo.ViewModel;
import sunyu.demo.integration.bigdata.admin.toolkit.MailTool;
import sunyu.demo.integration.bigdata.admin.toolkit.ProtocolTool;
import sunyu.toolkit.hbase.util.HbaseTool;
import sunyu.toolkit.jts.CoordTransformTool;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.*;
import java.util.Map.Entry;

@Controller
@RequestMapping("/hbase")
@CrossOrigin
public class HbaseController extends IndexController {
    Log log = LogFactory.get();

    @Autowired
    MailTool mailTool;

    @Value("${get.device.log}")
    String getDeviceLogUrl;

    Props props = new Props("application.properties");
    HbaseTool hbaseTool = new HbaseTool(props.getStr("hbase.zookeeper.quorum"), props.getStr("zookeeper.znode.parent"), Runtime.getRuntime().availableProcessors());

    ProtocolTool protocolTool = ProtocolTool.INSTANCE;
    CoordTransformTool coordTransformTool = CoordTransformTool.INSTANCT;
    Snowflake snowflake = IdUtil.createSnowflake(1, 1);

    @GetMapping("/device/log")
    public ModelAndView device_log_page(Model model, @RequestParam(defaultValue = "") String did,
                                        DateTime startTime,
                                        DateTime endTime,
                                        @RequestParam(defaultValue = "true") Boolean reverseScan) {
        saveDidStartTimeEndTimeModel(model, did, startTime, endTime);
        model.addAttribute("reverseScan", reverseScan);
        return new ModelAndView("hbase/device_log", model.asMap());
    }

    @PostMapping("/device/log/command")
    @ResponseBody
    public ViewModel device_log_command(@RequestParam(required = true) String did) {
        ViewModel viewModel = new ViewModel();
        try {
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("dids", did);
            HttpUtil.post(getDeviceLogUrl.replace("{env}", "farm"), paramMap);
            viewModel.setMessage("设备日志请求成功，等待设备上传日志信息，请稍后查看。");
        } catch (Exception e) {
            viewModel.setStatus(10001);
            viewModel.setError("设备日志请求失败！");
        }
        return viewModel;
    }

    @PostMapping("/device/log")
    @ResponseBody
    public TablePlusResult device_log_data(String offsetRowKey, @RequestParam(required = true) String did,
                                           @RequestParam(required = true) DateTime startTime,
                                           @RequestParam(required = true) DateTime endTime,
                                           @RequestParam(defaultValue = "10") Integer pageSize, @RequestParam(defaultValue = "*") String columns,
                                           @RequestParam(defaultValue = "true") Boolean reverseScan) {
        TablePlusResult results = new TablePlusResult();
        if (StrUtil.isNotBlank(did)) {
            String startRow = did.trim() + "_" + startTime.toString("yyyyMMddHHmmss") + "_";
            String stopRow = did.trim() + "_" + endTime.toString("yyyyMMddHHmmss") + "_";
            if (reverseScan == true) {
                // 逆序
                if (startRow.compareTo(stopRow) < 0) {
                    String t = startRow;
                    startRow = stopRow;
                    stopRow = t;
                }
                if (StrUtil.isNotBlank(offsetRowKey)) {
                    offsetRowKey = HbaseTool.lastCharAsciiSubOne(offsetRowKey);
                    if (offsetRowKey.compareTo(stopRow) > 0) {
                        startRow = offsetRowKey;
                    } else {
                        return results;
                    }
                }
            } else {
                // 正序
                if (startRow.compareTo(stopRow) > 0) {
                    String t = startRow;
                    startRow = stopRow;
                    stopRow = t;
                }
                if (StrUtil.isNotBlank(offsetRowKey)) {
                    offsetRowKey = HbaseTool.lastCharAsciiAddOne(offsetRowKey);
                    if (offsetRowKey.compareTo(stopRow) < 0) {
                        startRow = offsetRowKey;
                    } else {
                        return results;
                    }
                }
            }
            String order = reverseScan == true ? "desc" : "asc";
            StringBuilder sql = new StringBuilder();
            sql.append(" select " + columns + " from device_log#log where ");
            sql.append("     startRowKey = '" + startRow + "' ");
            sql.append(" and stopRowKey = '" + stopRow + "' ");
            sql.append(" order by rowKey " + order);
            sql.append(" limit " + pageSize);
            List<Map<String, String>> rows = hbaseTool.select(sql.toString());
            convertTitlesAndValues(results, rows);
        }
        return results;
    }

    @GetMapping("/ne/gateway")
    public ModelAndView ne_gateway_page(Model model, @RequestParam(defaultValue = "") String did,
                                        DateTime startTime,
                                        DateTime endTime,
                                        @RequestParam(defaultValue = "") String columns, @RequestParam(defaultValue = "true") Boolean reverseScan) {
        saveDidStartTimeEndTimeColumnsModel(model, did, startTime, endTime, columns);
        model.addAttribute("reverseScan", reverseScan);
        return new ModelAndView("hbase/ne_gateway", model.asMap());
    }

    @GetMapping("/ne/repeat")
    public ModelAndView ne_repeat_page(Model model, @RequestParam(defaultValue = "") String did,
                                       DateTime startTime,
                                       DateTime endTime,
                                       @RequestParam(defaultValue = "") String type, @RequestParam(defaultValue = "true") Boolean reverseScan) {
        saveDidStartTimeEndTimeModel(model, did, startTime, endTime);
        model.addAttribute("type", type);
        model.addAttribute("reverseScan", reverseScan);
        return new ModelAndView("hbase/ne_repeat", model.asMap());
    }

    @GetMapping("/farm/repeat")
    public ModelAndView farm_repeat_page(Model model, @RequestParam(defaultValue = "") String did,
                                         DateTime startTime,
                                         DateTime endTime,
                                         @RequestParam(defaultValue = "") String type, @RequestParam(defaultValue = "true") Boolean reverseScan) {
        saveDidStartTimeEndTimeModel(model, did, startTime, endTime);
        model.addAttribute("type", type);
        model.addAttribute("reverseScan", reverseScan);
        return new ModelAndView("hbase/farm_repeat", model.asMap());
    }

    private void saveDidStartTimeEndTimeColumnsModel(Model model, String did, DateTime startTime, DateTime endTime,
                                                     String columns) {
        saveDidStartTimeEndTimeModel(model, did, startTime, endTime);
        model.addAttribute("columns", columns.trim());
    }

    @GetMapping("/ne/hex")
    public ModelAndView ne_hex_page(Model model, @RequestParam(defaultValue = "") String did,
                                    DateTime startTime,
                                    DateTime endTime,
                                    @RequestParam(defaultValue = "") String columns, @RequestParam(defaultValue = "true") Boolean reverseScan) {
        saveDidStartTimeEndTimeColumnsModel(model, did, startTime, endTime, columns);
        model.addAttribute("reverseScan", reverseScan);
        return new ModelAndView("hbase/ne_hex", model.asMap());
    }

    @GetMapping("/ne/errorHex")
    public ModelAndView ne_errorHex_page(Model model, @RequestParam(defaultValue = "") String did,
                                         @RequestParam(defaultValue = "") String ip, @RequestParam(defaultValue = "") String type,
                                         DateTime startTime,
                                         DateTime endTime,
                                         @RequestParam(defaultValue = "true") Boolean reverseScan) {
        model.addAttribute("ip", ip);
        model.addAttribute("type", type);
        saveDidStartTimeEndTimeModel(model, did, startTime, endTime);
        model.addAttribute("reverseScan", reverseScan);
        return new ModelAndView("hbase/ne_errorHex", model.asMap());
    }

    private void saveDidStartTimeEndTimeModel(Model model, String did, DateTime startTime, DateTime endTime) {
        DateTime dateTime = new DateTime();
        model.addAttribute("did", did.trim());
        saveStartTimeEndTimeModel(model, startTime, endTime, dateTime);
    }

    @GetMapping("/farm/errorHex")
    public ModelAndView farm_errorHex_page(Model model, @RequestParam(defaultValue = "") String did,
                                           @RequestParam(defaultValue = "") String ip, @RequestParam(defaultValue = "") String type,
                                           DateTime startTime,
                                           DateTime endTime,
                                           @RequestParam(defaultValue = "true") Boolean reverseScan) {
        model.addAttribute("ip", ip);
        model.addAttribute("type", type);
        saveDidStartTimeEndTimeModel(model, did, startTime, endTime);
        model.addAttribute("reverseScan", reverseScan);
        return new ModelAndView("hbase/farm_errorHex", model.asMap());
    }

    @PostMapping(value = {"/ne/errorHex"})
    @ResponseBody
    public TablePlusResult ne_errorHex_data(String offsetRowKey, @RequestParam(defaultValue = "") String did,
                                            @RequestParam(defaultValue = "") String ip, @RequestParam(defaultValue = "") String type,
                                            @RequestParam(required = true) DateTime startTime,
                                            @RequestParam(required = true) DateTime endTime,
                                            @RequestParam(defaultValue = "10") Integer pageSize,
                                            @RequestParam(defaultValue = "true") Boolean reverseScan) {
        String familyName = "ne";
        return getErrorHexTablePlusResult(offsetRowKey, did, ip, type, startTime, endTime, reverseScan, pageSize,
                familyName);
    }

    @PostMapping(value = {"/ne/repeat"})
    @ResponseBody
    public TablePlusResult ne_repeat_data(String offsetRowKey, @RequestParam(defaultValue = "") String did,
                                          @RequestParam(required = true) DateTime startTime,
                                          @RequestParam(required = true) DateTime endTime,
                                          @RequestParam(defaultValue = "10") Integer pageSize, @RequestParam(defaultValue = "") String type,
                                          @RequestParam(defaultValue = "true") Boolean reverseScan) {
        TablePlusResult results = new TablePlusResult();
        String startRow = startTime.toString("yyyyMMddHHmmss") + "_";
        String stopRow = endTime.toString("yyyyMMddHHmmss") + "_";
        if (reverseScan == true) {
            // 逆序
            if (startRow.compareTo(stopRow) < 0) {
                String t = startRow;
                startRow = stopRow;
                stopRow = t;
            }
            if (StrUtil.isNotBlank(offsetRowKey)) {
                offsetRowKey = HbaseTool.lastCharAsciiSubOne(offsetRowKey);
                if (offsetRowKey.compareTo(stopRow) > 0) {
                    startRow = offsetRowKey;
                } else {
                    return results;
                }
            }
        } else {
            // 正序
            if (startRow.compareTo(stopRow) > 0) {
                String t = startRow;
                startRow = stopRow;
                stopRow = t;
            }
            if (StrUtil.isNotBlank(offsetRowKey)) {
                offsetRowKey = HbaseTool.lastCharAsciiAddOne(offsetRowKey);
                if (offsetRowKey.compareTo(stopRow) < 0) {
                    startRow = offsetRowKey;
                } else {
                    return results;
                }
            }
        }
        String order = reverseScan == true ? "desc" : "asc";
        StringBuilder sql = new StringBuilder();
        sql.append(" select * from repeat#ne where ");
        sql.append("     startRowKey = '" + startRow + "' ");
        sql.append(" and stopRowKey = '" + stopRow + "' ");
        if (StrUtil.isNotBlank(did)) {
            sql.append(" and did = '" + did + "' ");
        }
        if (StrUtil.isNotBlank(type)) {
            sql.append(" and type = '" + type + "' ");
        }
        sql.append(" order by rowKey " + order);
        sql.append(" limit " + pageSize);
        List<Map<String, String>> rows = hbaseTool.select(sql.toString());
        convertTitlesAndValues(results, rows);
        return results;
    }

    @PostMapping(value = {"/farm/repeat"})
    @ResponseBody
    public TablePlusResult farm_repeat_data(String offsetRowKey, @RequestParam(defaultValue = "") String did,
                                            @RequestParam(required = true) DateTime startTime,
                                            @RequestParam(required = true) DateTime endTime,
                                            @RequestParam(defaultValue = "10") Integer pageSize, @RequestParam(defaultValue = "") String type,
                                            @RequestParam(defaultValue = "true") Boolean reverseScan) {
        TablePlusResult results = new TablePlusResult();
        String startRow = startTime.toString("yyyyMMddHHmmss") + "_";
        String stopRow = endTime.toString("yyyyMMddHHmmss") + "_";
        if (reverseScan == true) {
            // 逆序
            if (startRow.compareTo(stopRow) < 0) {
                String t = startRow;
                startRow = stopRow;
                stopRow = t;
            }
            if (StrUtil.isNotBlank(offsetRowKey)) {
                offsetRowKey = HbaseTool.lastCharAsciiSubOne(offsetRowKey);
                if (offsetRowKey.compareTo(stopRow) > 0) {
                    startRow = offsetRowKey;
                } else {
                    return results;
                }
            }
        } else {
            // 正序
            if (startRow.compareTo(stopRow) > 0) {
                String t = startRow;
                startRow = stopRow;
                stopRow = t;
            }
            if (StrUtil.isNotBlank(offsetRowKey)) {
                offsetRowKey = HbaseTool.lastCharAsciiAddOne(offsetRowKey);
                if (offsetRowKey.compareTo(stopRow) < 0) {
                    startRow = offsetRowKey;
                } else {
                    return results;
                }
            }
        }
        String order = reverseScan == true ? "desc" : "asc";
        StringBuilder sql = new StringBuilder();
        sql.append(" select * from repeat#nrmv where ");
        sql.append("     startRowKey = '" + startRow + "' ");
        sql.append(" and stopRowKey = '" + stopRow + "' ");
        if (StrUtil.isNotBlank(did)) {
            sql.append(" and did = '" + did + "' ");
        }
        if (StrUtil.isNotBlank(type)) {
            sql.append(" and type = '" + type + "' ");
        }
        sql.append(" order by rowKey " + order);
        sql.append(" limit " + pageSize);
        List<Map<String, String>> rows = hbaseTool.select(sql.toString());
        convertTitlesAndValues(results, rows);
        return results;
    }

    @RequestMapping(value = {"/ne/repeat/export"})
    public void ne_repeat_export(HttpServletResponse response, @RequestParam(required = false) String did,
                                 @RequestParam(required = false) String type,
                                 @RequestParam(required = true) DateTime startTime,
                                 @RequestParam(required = true) DateTime endTime,
                                 @RequestParam(required = true) String exportTitle,
                                 @RequestParam(defaultValue = "true") Boolean reverseScan) {
        String startRow = startTime.toString("yyyyMMddHHmmss") + "_";
        String stopRow = endTime.toString("yyyyMMddHHmmss") + "_";
        if (reverseScan == true) {
            // 逆序
            if (startRow.compareTo(stopRow) < 0) {
                String t = startRow;
                startRow = stopRow;
                stopRow = t;
            }
        } else {
            // 正序
            if (startRow.compareTo(stopRow) > 0) {
                String t = startRow;
                startRow = stopRow;
                stopRow = t;
            }
        }
        String order = reverseScan == true ? "desc" : "asc";
        StringBuilder sql = new StringBuilder();
        sql.append(" select * from repeat#ne where ");
        sql.append("     startRowKey = '" + startRow + "' ");
        sql.append(" and stopRowKey = '" + stopRow + "' ");
        if (StrUtil.isNotBlank(did)) {
            sql.append(" and did = '" + did + "' ");
        }
        if (StrUtil.isNotBlank(type)) {
            sql.append(" and type = '" + type + "' ");
        }
        sql.append(" order by rowKey " + order);
        exportExcel(response, exportTitle, sql);
    }

    @RequestMapping(value = {"/farm/repeat/export"})
    public void farm_repeat_export(HttpServletResponse response, @RequestParam(required = false) String did,
                                   @RequestParam(required = false) String type,
                                   @RequestParam(required = true) DateTime startTime,
                                   @RequestParam(required = true) DateTime endTime,
                                   @RequestParam(required = true) String exportTitle,
                                   @RequestParam(defaultValue = "true") Boolean reverseScan) {
        String startRow = startTime.toString("yyyyMMddHHmmss") + "_";
        String stopRow = endTime.toString("yyyyMMddHHmmss") + "_";
        if (reverseScan == true) {
            // 逆序
            if (startRow.compareTo(stopRow) < 0) {
                String t = startRow;
                startRow = stopRow;
                stopRow = t;
            }
        } else {
            // 正序
            if (startRow.compareTo(stopRow) > 0) {
                String t = startRow;
                startRow = stopRow;
                stopRow = t;
            }
        }
        String order = reverseScan == true ? "desc" : "asc";
        StringBuilder sql = new StringBuilder();
        sql.append(" select * from repeat#nrmv where ");
        sql.append("     startRowKey = '" + startRow + "' ");
        sql.append(" and stopRowKey = '" + stopRow + "' ");
        if (StrUtil.isNotBlank(did)) {
            sql.append(" and did = '" + did + "' ");
        }
        if (StrUtil.isNotBlank(type)) {
            sql.append(" and type = '" + type + "' ");
        }
        sql.append(" order by rowKey " + order);
        exportExcel(response, exportTitle, sql);
    }

    private TablePlusResult getErrorHexTablePlusResult(String offsetRowKey, String did, String ip, String type,
                                                       DateTime startTime, DateTime endTime, Boolean reverseScan, Integer pageSize, String familyName) {
        TablePlusResult results = new TablePlusResult();
        String startRow = startTime.toString("yyyyMMddHHmmss") + "_";
        String stopRow = endTime.toString("yyyyMMddHHmmss") + "_";
        if (reverseScan == true) {
            // 逆序
            if (startRow.compareTo(stopRow) < 0) {
                String t = startRow;
                startRow = stopRow;
                stopRow = t;
            }
            if (StrUtil.isNotBlank(offsetRowKey)) {
                offsetRowKey = HbaseTool.lastCharAsciiSubOne(offsetRowKey);
                if (offsetRowKey.compareTo(stopRow) > 0) {
                    startRow = offsetRowKey;
                } else {
                    return results;
                }
            }
        } else {
            // 正序
            if (startRow.compareTo(stopRow) > 0) {
                String t = startRow;
                startRow = stopRow;
                stopRow = t;
            }
            if (StrUtil.isNotBlank(offsetRowKey)) {
                offsetRowKey = HbaseTool.lastCharAsciiAddOne(offsetRowKey);
                if (offsetRowKey.compareTo(stopRow) < 0) {
                    startRow = offsetRowKey;
                } else {
                    return results;
                }
            }
        }
        String order = reverseScan == true ? "desc" : "asc";
        StringBuilder sql = new StringBuilder();
        sql.append(" select * from errhex#" + familyName + " where ");
        sql.append("     startRowKey = '" + startRow + "' ");
        sql.append(" and stopRowKey = '" + stopRow + "' ");
        if (StrUtil.isNotBlank(did)) {
            sql.append(" and did = '" + did + "' ");
        }
        if (StrUtil.isNotBlank(ip)) {
            sql.append(" and ip = '" + ip + "' ");
        }
        if (StrUtil.isNotBlank(type)) {
            sql.append(" and errType = '" + type + "' ");
        }
        sql.append(" order by rowKey " + order);
        sql.append(" limit " + pageSize);
        List<Map<String, String>> rows = hbaseTool.select(sql.toString());
        convertTitlesAndValues(results, rows);
        return results;
    }

    @PostMapping(value = {"/farm/errorHex"})
    @ResponseBody
    public TablePlusResult farm_errorHex_data(String offsetRowKey, @RequestParam(defaultValue = "") String did,
                                              @RequestParam(defaultValue = "") String ip, @RequestParam(defaultValue = "") String type,
                                              @RequestParam(required = true) DateTime startTime,
                                              @RequestParam(required = true) DateTime endTime,
                                              @RequestParam(defaultValue = "10") Integer pageSize,
                                              @RequestParam(defaultValue = "true") Boolean reverseScan) {
        String familyName = "nrmv";
        return getErrorHexTablePlusResult(offsetRowKey, did, ip, type, startTime, endTime, reverseScan, pageSize,
                familyName);
    }

    @RequestMapping(value = {"/farm/errorHex/export"})
    public void farm_errorHex_export(HttpServletResponse response, @RequestParam(required = false) String did,
                                     @RequestParam(required = false) String ip, @RequestParam(required = false) String type,
                                     @RequestParam(required = true) DateTime startTime,
                                     @RequestParam(required = true) DateTime endTime,
                                     @RequestParam(required = true) String exportTitle,
                                     @RequestParam(defaultValue = "true") Boolean reverseScan) {
        String startRow = startTime.toString("yyyyMMddHHmmss") + "_";
        String stopRow = endTime.toString("yyyyMMddHHmmss") + "_";
        if (reverseScan == true) {
            // 逆序
            if (startRow.compareTo(stopRow) < 0) {
                String t = startRow;
                startRow = stopRow;
                stopRow = t;
            }
        } else {
            // 正序
            if (startRow.compareTo(stopRow) > 0) {
                String t = startRow;
                startRow = stopRow;
                stopRow = t;
            }
        }
        String order = reverseScan == true ? "desc" : "asc";
        StringBuilder sql = new StringBuilder();
        sql.append(" select * from errhex#nrmv where ");
        sql.append("     startRowKey = '" + startRow + "' ");
        sql.append(" and stopRowKey = '" + stopRow + "' ");
        if (StrUtil.isNotBlank(did)) {
            sql.append(" and did = '" + did + "' ");
        }
        if (StrUtil.isNotBlank(type)) {
            sql.append(" and type = '" + type + "' ");
        }
        if (StrUtil.isNotBlank(ip)) {
            sql.append(" and ip = '" + ip + "' ");
        }
        sql.append(" order by rowKey " + order);
        exportExcel(response, exportTitle, sql);
    }

    @RequestMapping(value = {"/ne/errorHex/export"})
    public void ne_errorHex_export(HttpServletResponse response, @RequestParam(required = false) String did,
                                   @RequestParam(required = false) String ip, @RequestParam(required = false) String type,
                                   @RequestParam(required = true) DateTime startTime,
                                   @RequestParam(required = true) DateTime endTime,
                                   @RequestParam(required = true) String exportTitle,
                                   @RequestParam(defaultValue = "true") Boolean reverseScan) {
        String startRow = startTime.toString("yyyyMMddHHmmss") + "_";
        String stopRow = endTime.toString("yyyyMMddHHmmss") + "_";
        if (reverseScan == true) {
            // 逆序
            if (startRow.compareTo(stopRow) < 0) {
                String t = startRow;
                startRow = stopRow;
                stopRow = t;
            }
        } else {
            // 正序
            if (startRow.compareTo(stopRow) > 0) {
                String t = startRow;
                startRow = stopRow;
                stopRow = t;
            }
        }
        String order = reverseScan == true ? "desc" : "asc";
        StringBuilder sql = new StringBuilder();
        sql.append(" select * from errhex#ne where ");
        sql.append("     startRowKey = '" + startRow + "' ");
        sql.append(" and stopRowKey = '" + stopRow + "' ");
        if (StrUtil.isNotBlank(did)) {
            sql.append(" and did = '" + did + "' ");
        }
        if (StrUtil.isNotBlank(type)) {
            sql.append(" and type = '" + type + "' ");
        }
        if (StrUtil.isNotBlank(ip)) {
            sql.append(" and ip = '" + ip + "' ");
        }
        sql.append(" order by rowKey " + order);
        exportExcel(response, exportTitle, sql);
    }

    @PostMapping(value = {"/ne/gateway", "/ne/hex"})
    @ResponseBody
    public TablePlusResult ne_gateway_data(String offsetRowKey, @RequestParam(required = true) String did,
                                           @RequestParam(required = true) DateTime startTime,
                                           @RequestParam(required = true) DateTime endTime,
                                           @RequestParam(defaultValue = "10") Integer pageSize, @RequestParam(defaultValue = "*") String columns,
                                           @RequestParam(defaultValue = "true") Boolean reverseScan) {
        TablePlusResult results = new TablePlusResult();
        if (StrUtil.isNotBlank(did)) {
            String md5 = SecureUtil.md5(did.trim());
            String startRow = md5 + "_" + startTime.toString("yyyyMMddHHmmss");
            String stopRow = md5 + "_" + endTime.toString("yyyyMMddHHmmss");
            if (reverseScan == true) {
                // 逆序
                if (startRow.compareTo(stopRow) < 0) {
                    String t = startRow;
                    startRow = stopRow;
                    stopRow = t;
                }
                if (StrUtil.isNotBlank(offsetRowKey)) {
                    offsetRowKey = HbaseTool.lastCharAsciiSubOne(offsetRowKey);
                    if (offsetRowKey.compareTo(stopRow) > 0) {
                        startRow = offsetRowKey;
                    } else {
                        return results;
                    }
                }
            } else {
                // 正序
                if (startRow.compareTo(stopRow) > 0) {
                    String t = startRow;
                    startRow = stopRow;
                    stopRow = t;
                }
                if (StrUtil.isNotBlank(offsetRowKey)) {
                    offsetRowKey = HbaseTool.lastCharAsciiAddOne(offsetRowKey);
                    if (offsetRowKey.compareTo(stopRow) < 0) {
                        startRow = offsetRowKey;
                    } else {
                        return results;
                    }
                }
            }
            String order = reverseScan == true ? "desc" : "asc";
            StringBuilder sql = new StringBuilder();
            sql.append(" select " + columns + " from gateway#log where ");
            sql.append("     startRowKey = '" + startRow + "' ");
            sql.append(" and stopRowKey = '" + stopRow + "' ");
            sql.append(" order by rowKey " + order);
            sql.append(" limit " + pageSize);
            List<Map<String, String>> rows = hbaseTool.select(sql.toString());
            convertTitlesAndValues(results, rows);
        }
        return results;
    }

    @RequestMapping(value = {"/ne/gateway/export", "/ne/hex/export"})
    public void ne_gateway_export(HttpServletResponse response, @RequestParam(required = true) String did,
                                  @RequestParam(required = true) DateTime startTime,
                                  @RequestParam(required = true) DateTime endTime,
                                  @RequestParam(defaultValue = "*") String columns, @RequestParam(required = true) String exportTitle,
                                  @RequestParam(defaultValue = "true") Boolean reverseScan) {
        String md5 = SecureUtil.md5(did.trim());
        String startRow = md5 + "_" + startTime.toString("yyyyMMddHHmmss");
        String stopRow = md5 + "_" + endTime.toString("yyyyMMddHHmmss");
        if (reverseScan == true) {
            // 逆序
            if (startRow.compareTo(stopRow) < 0) {
                String t = startRow;
                startRow = stopRow;
                stopRow = t;
            }
        } else {
            // 正序
            if (startRow.compareTo(stopRow) > 0) {
                String t = startRow;
                startRow = stopRow;
                stopRow = t;
            }
        }
        String order = reverseScan == true ? "desc" : "asc";
        StringBuilder sql = new StringBuilder();
        sql.append(" select " + columns + " from gateway#log where ");
        sql.append("     startRowKey = '" + startRow + "' ");
        sql.append(" and stopRowKey = '" + stopRow + "' ");
        sql.append(" order by rowKey " + order);
        exportExcel(response, exportTitle, sql);
    }

    private void exportExcel(HttpServletResponse response, String fileName, StringBuilder sql) {
        ShiroSubject shiroSubject = (ShiroSubject) SecurityUtils.getSubject().getPrincipal();
        String mailAddress = shiroSubject.getEmail();
        try {
            ServletOutputStream out = response.getOutputStream();
            response.setCharacterEncoding("utf-8");
            response.setContentType("text/plain");
            response.setHeader("content-Disposition",
                    "attachment;filename=" + URLEncoder.encode(fileName + ".txt", "utf-8"));
            out.write(StrUtil.bytes("您要导出的数据后台正在排队处理，稍后会以附件的形式发送到您的 " + mailAddress + " 邮箱，请注意查收。"));
        } catch (IOException e) {
            StaticLog.error(e);
        }

        ThreadUtil.execAsync(() -> {
            DateTime dateTime = new DateTime();
            List<Map<String, String>> rows = hbaseTool.select(sql.toString());
            if (CollUtil.isNotEmpty(rows)) {
                ExcelWriter bigWriter = ExcelUtil.getBigWriter();
                StyleSet style = bigWriter.getStyleSet();
                style.getHeadCellStyle().setAlignment(HorizontalAlignment.LEFT);
                style.getCellStyle().setAlignment(HorizontalAlignment.LEFT);
                TablePlusResult results = new TablePlusResult();
                convertTitlesAndValues(results, rows);
                bigWriter.setHeaderAlias(results.getTitles());
                for (Object row : results.getRows()) {
                    Map<String, String> r = (Map) row;
                    results.getTitles().forEach((k, v) -> {
                        if (!r.containsKey(k)) {
                            r.put(k, "");
                        }
                    });
                }
                bigWriter.write(results.getRows(), true);
                File file = new File(fileName + snowflake.nextId() + ".xlsx");
                bigWriter.flush(file);
                mailTool.sendMail(Arrays.asList(mailAddress), fileName,
                        fileName + " 请下载附件查看<br/>数据导出请求时间：" + dateTime.toString("yyyy-MM-dd HH:mm:ss"), true, file);
                FileUtil.del(file);
                bigWriter.close();
            }
        });
    }

    private void convertTitlesAndValues(TablePlusResult results, List<Map<String, String>> rows) {
        if (CollUtil.isNotEmpty(rows)) {
            // 填充数据
            for (Map<String, String> row : rows) {
                results.getRows().add(row);
                for (Entry<String, String> kv : row.entrySet()) {
                    results.getTitles().put(kv.getKey(), kv.getKey());
                }
            }
            // 转换标题名称
            protocolTool.internalProtocolTitleMapConvert(results.getTitles());
            // 转换列值
            for (Object r : results.getRows()) {
                Map row = (Map) r;
                if (row.containsKey("2") && row.containsKey("hex")) {
                    row.remove("2");
                    results.getTitles().remove("2");
                }
                row.putAll(protocolTool.internalProtocolMapConvertValue(row));
            }
        }
    }

    @GetMapping("/ne/can")
    public ModelAndView ne_can_page(Model model, @RequestParam(defaultValue = "") String did,
                                    DateTime startTime,
                                    DateTime endTime,
                                    @RequestParam(defaultValue = "") String columns, @RequestParam(defaultValue = "true") Boolean reverseScan) {
        saveDidStartTimeEndTimeColumnsModel(model, did, startTime, endTime, columns);
        model.addAttribute("reverseScan", reverseScan);
        return new ModelAndView("hbase/ne_can", model.asMap());
    }

    @PostMapping("/ne/can")
    @ResponseBody
    public TablePlusResult ne_can_data(String offsetRowKey, @RequestParam(required = true) String did,
                                       @RequestParam(required = true) DateTime startTime,
                                       @RequestParam(required = true) DateTime endTime,
                                       @RequestParam(defaultValue = "10") Integer pageSize, @RequestParam(defaultValue = "*") String columns,
                                       @RequestParam(defaultValue = "true") Boolean reverseScan) {
        TablePlusResult results = new TablePlusResult();
        if (StrUtil.isNotBlank(did)) {
            String md5 = SecureUtil.md5(did.trim());
            String startRow = md5 + "_" + startTime.toString("yyyyMMddHHmmss");
            String stopRow = md5 + "_" + endTime.toString("yyyyMMddHHmmss");
            if (reverseScan == true) {
                // 逆序
                if (startRow.compareTo(stopRow) < 0) {
                    String t = startRow;
                    startRow = stopRow;
                    stopRow = t;
                }
                if (StrUtil.isNotBlank(offsetRowKey)) {
                    offsetRowKey = HbaseTool.lastCharAsciiSubOne(offsetRowKey);
                    if (offsetRowKey.compareTo(stopRow) > 0) {
                        startRow = offsetRowKey;
                    } else {
                        return results;
                    }
                }
            } else {
                // 正序
                if (startRow.compareTo(stopRow) > 0) {
                    String t = startRow;
                    startRow = stopRow;
                    stopRow = t;
                }
                if (StrUtil.isNotBlank(offsetRowKey)) {
                    offsetRowKey = HbaseTool.lastCharAsciiAddOne(offsetRowKey);
                    if (offsetRowKey.compareTo(stopRow) < 0) {
                        startRow = offsetRowKey;
                    } else {
                        return results;
                    }
                }
            }
            String order = reverseScan == true ? "desc" : "asc";
            StringBuilder sql = new StringBuilder();
            sql.append(" select " + columns + " from can_ne#can where ");
            sql.append("     startRowKey = '" + startRow + "' ");
            sql.append(" and stopRowKey = '" + stopRow + "' ");
            sql.append(" order by rowKey " + order);
            sql.append(" limit " + pageSize);
            List<Map<String, String>> rows = hbaseTool.select(sql.toString());
            convertTitlesAndValues(results, rows);
        }
        return results;
    }

    @RequestMapping("/ne/can/export")
    public void ne_can_export(HttpServletResponse response, @RequestParam(required = true) String did,
                              @RequestParam(required = true) DateTime startTime,
                              @RequestParam(required = true) DateTime endTime,
                              @RequestParam(defaultValue = "*") String columns, @RequestParam(required = true) String exportTitle,
                              @RequestParam(defaultValue = "true") Boolean reverseScan) {
        String md5 = SecureUtil.md5(did.trim());
        String startRow = md5 + "_" + startTime.toString("yyyyMMddHHmmss");
        String stopRow = md5 + "_" + endTime.toString("yyyyMMddHHmmss");
        if (reverseScan == true) {
            // 逆序
            if (startRow.compareTo(stopRow) < 0) {
                String t = startRow;
                startRow = stopRow;
                stopRow = t;
            }
        } else {
            // 正序
            if (startRow.compareTo(stopRow) > 0) {
                String t = startRow;
                startRow = stopRow;
                stopRow = t;
            }
        }
        String order = reverseScan == true ? "desc" : "asc";
        StringBuilder sql = new StringBuilder();
        sql.append(" select " + columns + " from can_ne#can where ");
        sql.append("     startRowKey = '" + startRow + "' ");
        sql.append(" and stopRowKey = '" + stopRow + "' ");
        sql.append(" order by rowKey " + order);
        exportExcel(response, exportTitle, sql);
    }

    @GetMapping("/ne/command")
    public ModelAndView ne_command_page(Model model, @RequestParam(defaultValue = "") String did,
                                        DateTime startTime,
                                        DateTime endTime,
                                        @RequestParam(defaultValue = "") String columns, @RequestParam(defaultValue = "true") Boolean reverseScan) {
        saveDidStartTimeEndTimeColumnsModel(model, did, startTime, endTime, columns);
        model.addAttribute("reverseScan", reverseScan);
        return new ModelAndView("hbase/ne_command", model.asMap());
    }

    @PostMapping("/ne/command")
    @ResponseBody
    public TablePlusResult ne_command_data(String offsetRowKey, @RequestParam(required = true) String did,
                                           @RequestParam(required = true) DateTime startTime,
                                           @RequestParam(required = true) DateTime endTime,
                                           @RequestParam(defaultValue = "10") Integer pageSize, @RequestParam(defaultValue = "*") String columns,
                                           @RequestParam(defaultValue = "true") Boolean reverseScan) {
        TablePlusResult results = new TablePlusResult();
        if (StrUtil.isNotBlank(did)) {
            String startRow = did.trim() + "_" + startTime.toString("yyyyMMddHHmmss") + "_";
            String stopRow = did.trim() + "_" + endTime.toString("yyyyMMddHHmmss") + "_";
            if (reverseScan == true) {
                // 逆序
                if (startRow.compareTo(stopRow) < 0) {
                    String t = startRow;
                    startRow = stopRow;
                    stopRow = t;
                }
                if (StrUtil.isNotBlank(offsetRowKey)) {
                    offsetRowKey = HbaseTool.lastCharAsciiSubOne(offsetRowKey);
                    if (offsetRowKey.compareTo(stopRow) > 0) {
                        startRow = offsetRowKey;
                    } else {
                        return results;
                    }
                }
            } else {
                // 正序
                if (startRow.compareTo(stopRow) > 0) {
                    String t = startRow;
                    startRow = stopRow;
                    stopRow = t;
                }
                if (StrUtil.isNotBlank(offsetRowKey)) {
                    offsetRowKey = HbaseTool.lastCharAsciiAddOne(offsetRowKey);
                    if (offsetRowKey.compareTo(stopRow) < 0) {
                        startRow = offsetRowKey;
                    } else {
                        return results;
                    }
                }
            }
            String order = reverseScan == true ? "desc" : "asc";
            StringBuilder sql = new StringBuilder();
            sql.append(" select " + columns + " from command#command where ");
            sql.append("     startRowKey = '" + startRow + "' ");
            sql.append(" and stopRowKey = '" + stopRow + "' ");
            sql.append(" order by rowKey " + order);
            sql.append(" limit " + pageSize);
            List<Map<String, String>> rows = hbaseTool.select(sql.toString());
            convertTitlesAndValues(results, rows);
        }
        return results;
    }

    @RequestMapping("/ne/command/export")
    public void ne_command_export(HttpServletResponse response, @RequestParam(required = true) String did,
                                  @RequestParam(required = true) DateTime startTime,
                                  @RequestParam(required = true) DateTime endTime,
                                  @RequestParam(defaultValue = "*") String columns, @RequestParam(required = true) String exportTitle,
                                  @RequestParam(defaultValue = "true") Boolean reverseScan) {
        String startRow = did.trim() + "_" + startTime.toString("yyyyMMddHHmmss") + "_";
        String stopRow = did.trim() + "_" + endTime.toString("yyyyMMddHHmmss") + "_";
        if (reverseScan == true) {
            // 逆序
            if (startRow.compareTo(stopRow) < 0) {
                String t = startRow;
                startRow = stopRow;
                stopRow = t;
            }
        } else {
            // 正序
            if (startRow.compareTo(stopRow) > 0) {
                String t = startRow;
                startRow = stopRow;
                stopRow = t;
            }
        }
        String order = reverseScan == true ? "desc" : "asc";
        StringBuilder sql = new StringBuilder();
        sql.append(" select " + columns + " from command#command where ");
        sql.append("     startRowKey = '" + startRow + "' ");
        sql.append(" and stopRowKey = '" + stopRow + "' ");
        sql.append(" order by rowKey " + order);
        exportExcel(response, exportTitle, sql);
    }

    @GetMapping("/ne/alarm")
    public ModelAndView ne_alarm_page(Model model, @RequestParam(defaultValue = "") String did,
                                      DateTime startTime,
                                      DateTime endTime,
                                      @RequestParam(defaultValue = "") String columns, @RequestParam(defaultValue = "true") Boolean reverseScan) {
        saveDidStartTimeEndTimeColumnsModel(model, did, startTime, endTime, columns);
        model.addAttribute("reverseScan", reverseScan);
        return new ModelAndView("hbase/ne_alarm", model.asMap());
    }

    @PostMapping("/ne/alarm")
    @ResponseBody
    public TablePlusResult ne_alarm_data(String offsetRowKey, @RequestParam(required = true) String did,
                                         @RequestParam(required = true) DateTime startTime,
                                         @RequestParam(required = true) DateTime endTime,
                                         @RequestParam(defaultValue = "10") Integer pageSize, @RequestParam(defaultValue = "*") String columns,
                                         @RequestParam(defaultValue = "true") Boolean reverseScan) {
        TablePlusResult results = new TablePlusResult();
        if (StrUtil.isNotBlank(did)) {
            String md5 = SecureUtil.md5(did.trim());
            String startRow = md5 + "_" + startTime.toString("yyyyMMddHHmmss") + "_";
            String stopRow = md5 + "_" + endTime.toString("yyyyMMddHHmmss") + "_";
            if (reverseScan == true) {
                // 逆序
                if (startRow.compareTo(stopRow) < 0) {
                    String t = startRow;
                    startRow = stopRow;
                    stopRow = t;
                }
                if (StrUtil.isNotBlank(offsetRowKey)) {
                    offsetRowKey = HbaseTool.lastCharAsciiSubOne(offsetRowKey);
                    if (offsetRowKey.compareTo(stopRow) > 0) {
                        startRow = offsetRowKey;
                    } else {
                        return results;
                    }
                }
            } else {
                // 正序
                if (startRow.compareTo(stopRow) > 0) {
                    String t = startRow;
                    startRow = stopRow;
                    stopRow = t;
                }
                if (StrUtil.isNotBlank(offsetRowKey)) {
                    offsetRowKey = HbaseTool.lastCharAsciiAddOne(offsetRowKey);
                    if (offsetRowKey.compareTo(stopRow) < 0) {
                        startRow = offsetRowKey;
                    } else {
                        return results;
                    }
                }
            }
            String order = reverseScan == true ? "desc" : "asc";
            StringBuilder sql = new StringBuilder();
            sql.append(" select " + columns + " from alarm#alarm where ");
            sql.append("     startRowKey = '" + startRow + "' ");
            sql.append(" and stopRowKey = '" + stopRow + "' ");
            sql.append(" order by rowKey " + order);
            sql.append(" limit " + pageSize);
            List<Map<String, String>> rows = hbaseTool.select(sql.toString());
            convertTitlesAndValues(results, rows);
        }
        return results;
    }

    @GetMapping("/ne/journey")
    public ModelAndView ne_journey_page(Model model, @RequestParam(defaultValue = "") String did,
                                        DateTime time,
                                        @RequestParam(defaultValue = "false") String refresh) {
        model.addAttribute("did", did.trim());
        if (time == null) {
            model.addAttribute("time", DateUtil.yesterday().toString("yyyy-MM-dd"));
        } else {
            model.addAttribute("time", time.toString("yyyy-MM-dd"));
        }
        model.addAttribute("refresh", refresh);
        return new ModelAndView("hbase/ne_journey", model.asMap());
    }

    @PostMapping("/ne/journey")
    @ResponseBody
    public TablePlusResult ne_journey_data(Model model, @RequestParam(required = true) String did,
                                           @RequestParam(required = true) DateTime time,
                                           @RequestParam(required = true) String refresh) {
        TablePlusResult results = new TablePlusResult();
        if (StrUtil.isNotBlank(did)) {
            String journeyRowKey = did + "_" + time.toString("yyyyMMdd");
            List<Map<String, String>> journey = hbaseTool.select("select * from journey#ne where startRowKey = '" + journeyRowKey + "' and stopRowKey = '" + journeyRowKey + "'");
            if (refresh.equals("true")) {// 需要重新生成
                String md5 = SecureUtil.md5(did.trim());
                String startRow = md5 + "_" + DateUtil.beginOfDay(time).toString("yyyyMMddHHmmss");
                String stopRow = md5 + "_" + DateUtil.endOfDay(time).toString("yyyyMMddHHmmss");
                StringBuilder sql = new StringBuilder();
                sql.append(" select 3040,2204,3014,2602,2603,2201,3020,2305,3012,2205 from can_ne#can where ");
                sql.append("     startRowKey = '" + startRow + "' ");
                sql.append(" and stopRowKey = '" + stopRow + "' ");
                sql.append("and 2601 = '0'");
                List<Map<String, String>> rows = hbaseTool.select(sql.toString());
                convertTitlesAndValues(results, rows);
                // 只要已定位的信息；定位状态 2601 0 GPS 已定位；1 GPS 未定位；
                rows = hbaseTool.select(sql.toString());
                if (CollUtil.isNotEmpty(rows)) {
                    // 行程列表
                    List<Map<String, String>> journeyList = new ArrayList<>();// [{startTime:'',endTime:'',pointCount:'',no:''},{},...]
                    // 行程信息列表
                    List<List<Map<String, String>>> journeyInfoList = new ArrayList<>();// [
                    // [{2602:'',2603:'',3014:'',speed:'',2305:'',3012:'',2205:''},...],...
                    // ]
                    String startTimeFlag = null;// 行程段开始时间标志
                    List<Map<String, String>> journeyInfo = null;
                    for (Map<String, String> row : rows) {
                        String acc = StrUtil.blankToDefault(row.get("2201"), row.get("3020"));// 2201车辆状态
                        // 1：车辆启动状态；2：熄火；3：其他状态；//3020//终端ACC
                        // 状态 0 关闭，1 开启
                        String speed = StrUtil.blankToDefault(row.get("3040"), row.get("2204"));// can 速度 3040 Can
                        // 信息采集的速度，单位 km/h //GPS
                        // 速度 2204 单位：1km/h
                        if (startTimeFlag == null && acc.equals("1")) {// 行程开始
                            startTimeFlag = row.get("3014");

                            journeyInfo = new ArrayList<>();
                            // [{2602:'',2603:'',3014:'',speed:'',2305:'',3012:'',2205:''},...]
                            journeyInfo.add(new HashMap<String, String>() {
                                {
                                    put("2602", row.get("2602"));
                                    put("2603", row.get("2603"));
                                    put("3014", row.get("3014"));
                                    put("speed", speed);
                                    put("2305", row.get("2305"));
                                    put("3012", row.get("3012"));
                                    put("2205", row.get("2205"));
                                }
                            });
                        } else if (startTimeFlag != null && !acc.equals("1")) {// 行程结束
                            // [{2602:'',2603:'',3014:'',speed:'',2305:'',3012:'',2205:''},...]
                            journeyInfo.add(new HashMap<String, String>() {
                                {
                                    put("2602", row.get("2602"));
                                    put("2603", row.get("2603"));
                                    put("3014", row.get("3014"));
                                    put("speed", speed);
                                    put("2305", row.get("2305"));
                                    put("3012", row.get("3012"));
                                    put("2205", row.get("2205"));
                                }
                            });
                            journeyInfoList.add(journeyInfo);

                            Map<String, String> journeyMap = new HashMap<>();
                            journeyMap.put("startTime", startTimeFlag);
                            journeyMap.put("endTime", row.get("3014"));
                            journeyMap.put("pointCount", String.valueOf(journeyInfo.size()));
                            journeyMap.put("no", String.valueOf(journeyList.size() + 1));
                            journeyList.add(journeyMap);

                            startTimeFlag = null;
                            journeyInfo = null;
                        } else if (journeyInfo != null) {// 行程中间的点
                            // [{2602:'',2603:'',3014:'',speed:'',2305:'',3012:'',2205:''},...]
                            journeyInfo.add(new HashMap<String, String>() {
                                {
                                    put("2602", row.get("2602"));
                                    put("2603", row.get("2603"));
                                    put("3014", row.get("3014"));
                                    put("speed", speed);
                                    put("2305", row.get("2305"));
                                    put("3012", row.get("3012"));
                                    put("2205", row.get("2205"));
                                }
                            });
                        }
                    }
                    if (startTimeFlag != null) {// 收尾，说明行程段跨天了
                        journeyInfoList.add(journeyInfo);

                        Map<String, String> journeyMap = new HashMap<>();
                        journeyMap.put("startTime", startTimeFlag);
                        journeyMap.put("endTime", DateUtil.endOfDay(DateUtil.parse(startTimeFlag, "yyyyMMddHHmmss"))
                                .toString("yyyyMMddHHmmss"));
                        journeyMap.put("pointCount", String.valueOf(journeyInfo.size()));
                        journeyMap.put("no", String.valueOf(journeyList.size() + 1));
                        journeyList.add(journeyMap);
                    }
                    // 将信息写入hbase
                    if (CollUtil.isNotEmpty(journeyList)) {
                        Map<String, String> data = new HashMap<>();
                        data.put("journey", JSON.toJSONString(journeyList));
                        data.put("journeyInfo", JSON.toJSONString(journeyInfoList));
                        hbaseTool.put("journey", "ne", journeyRowKey, data);
                    }
                }
            }
            if (CollUtil.isNotEmpty(journey)) {// 在行程表里面查到了记录
                // results.getTitles().put("startTime", "行程开始时间");
                // results.getTitles().put("endTime", "行程结束时间");
                for (Entry<String, String> kv : journey.get(0).entrySet()) {
                    if (kv.getKey().equals("journey")) {
                        // [{startTime:'',endTime:'',pointCount:'',no:''},{},...]
                        List<Map> arr = JSONArray.parseArray(kv.getValue(), Map.class);
                        // 循环所有行程段
                        arr.forEach(journeyInfo -> {
                            results.getRows().add(journeyInfo);
                        });
                    }
                }
                results.setTotal(Convert.toLong(results.getRows().size()));
            }
        }
        results.getRows().forEach(o -> {
            Map<String, String> m = (Map<String, String>) o;
            m.forEach((key, value) -> {
                if (key.equals("startTime") || key.equals("endTime")) {
                    m.put(key, DateUtil.parse(value, "yyyyMMddHHmmss").toString("yyyy-MM-dd HH:mm:ss"));
                }
            });
        });
        return results;
    }

    @GetMapping("/ne/journey_info")
    public ModelAndView ne_journey_info_page(Model model, @RequestParam(required = true) String did,
                                             @RequestParam(required = true) DateTime time,
                                             @RequestParam(required = true) String no) {
        model.addAttribute("did", did.trim());
        model.addAttribute("time", time.toString("yyyy-MM-dd"));
        model.addAttribute("no", no.trim());
        return new ModelAndView("hbase/ne_journey_info", model.asMap());
    }

    @PostMapping("/ne/journey_info")
    @ResponseBody
    public List ne_journey_info_data(@RequestParam(required = true) String did,
                                     @RequestParam(required = true) DateTime time,
                                     @RequestParam(required = true) String no) {
        List results = new ArrayList<>();
        String journeyRowKey = did + "_" + time.toString("yyyyMMdd");
        List<Map<String, String>> journey = hbaseTool.select("select * from journey#ne where startRowKey = '" + journeyRowKey + "' and stopRowKey = '" + journeyRowKey + "'");
        if (CollUtil.isNotEmpty(journey)) {// 在行程表里面查到了记录
            for (Entry<String, String> kv : journey.get(0).entrySet()) {
                if (kv.getKey().equals("journeyInfo")) {
                    // [ [{2602:'',2603:'',3014:'',speed:'',2305:'',3012:'',2205:''},...],... ]
                    List<List> arr = JSONArray.parseArray(kv.getValue(), List.class);
                    results = arr.get(Integer.parseInt(no) - 1);
                    break;
                }
            }
        }
        // 转换成百度坐标系，用于前台地图显示
        results.forEach(o -> {
            Map<String, String> m = (Map<String, String>) o;
            double[] latLon = coordTransformTool.wgs2BD09(Double.parseDouble(m.get("2603")),
                    Double.parseDouble(m.get("2602")));
            m.put("lon", String.valueOf(latLon[1]));
            m.put("lat", String.valueOf(latLon[0]));
        });
        return results;
    }

    @GetMapping("/interface")
    public ModelAndView interface_page(Model model, @RequestParam(defaultValue = "") String tokenId,
                                       DateTime startTime,
                                       DateTime endTime,
                                       @RequestParam(defaultValue = "10") Integer pageSize, @RequestParam(defaultValue = "") String columns,
                                       @RequestParam(defaultValue = "true") Boolean reverseScan) {
        DateTime dateTime = new DateTime();
        model.addAttribute("tokenId", tokenId.trim());
        saveStartTimeEndTimeModel(model, startTime, endTime, dateTime);
        model.addAttribute("columns", columns.trim());
        model.addAttribute("reverseScan", reverseScan);
        return new ModelAndView("hbase/interface", model.asMap());
    }

    private void saveStartTimeEndTimeModel(Model model,
                                           DateTime startTime,
                                           DateTime endTime, DateTime dateTime) {
        if (startTime == null) {
            model.addAttribute("startTime", DateUtil.beginOfDay(dateTime).toString("yyyy-MM-dd HH:mm:ss"));
        } else {
            model.addAttribute("startTime", startTime.toString("yyyy-MM-dd HH:mm:ss"));
        }
        if (endTime == null) {
            model.addAttribute("endTime", DateUtil.endOfDay(dateTime).toString("yyyy-MM-dd HH:mm:ss"));
        } else {
            model.addAttribute("endTime", endTime.toString("yyyy-MM-dd HH:mm:ss"));
        }
    }

    @PostMapping("/interface")
    @ResponseBody
    public TablePlusResult interface_data(String offsetRowKey, @RequestParam(required = true) String tokenId,
                                          @RequestParam(required = true) DateTime startTime,
                                          @RequestParam(required = true) DateTime endTime,
                                          @RequestParam(defaultValue = "10") Integer pageSize, @RequestParam(defaultValue = "*") String columns,
                                          @RequestParam(defaultValue = "true") Boolean reverseScan) {
        TablePlusResult results = new TablePlusResult();
        if (StrUtil.isNotBlank(tokenId)) {
            String startRow = tokenId.trim() + "_" + startTime.getTime();
            String stopRow = tokenId.trim() + "_" + endTime.getTime();
            if (reverseScan == true) {
                // 逆序
                if (startRow.compareTo(stopRow) < 0) {
                    String t = startRow;
                    startRow = stopRow;
                    stopRow = t;
                }
                if (StrUtil.isNotBlank(offsetRowKey)) {
                    offsetRowKey = HbaseTool.lastCharAsciiSubOne(offsetRowKey);
                    if (offsetRowKey.compareTo(stopRow) > 0) {
                        startRow = offsetRowKey;
                    } else {
                        return results;
                    }
                }
            } else {
                // 正序
                if (startRow.compareTo(stopRow) > 0) {
                    String t = startRow;
                    startRow = stopRow;
                    stopRow = t;
                }
                if (StrUtil.isNotBlank(offsetRowKey)) {
                    offsetRowKey = HbaseTool.lastCharAsciiAddOne(offsetRowKey);
                    if (offsetRowKey.compareTo(stopRow) < 0) {
                        startRow = offsetRowKey;
                    } else {
                        return results;
                    }
                }
            }
            String order = reverseScan == true ? "desc" : "asc";
            StringBuilder sql = new StringBuilder();
            sql.append(" select " + columns + " from interface#log where ");
            sql.append("     startRowKey = '" + startRow + "' ");
            sql.append(" and stopRowKey = '" + stopRow + "' ");
            sql.append(" order by rowKey " + order);
            sql.append(" limit " + pageSize);
            List<Map<String, String>> rows = hbaseTool.select(sql.toString());
            convertTitlesAndValues(results, rows);
        }
        return results;
    }

    @GetMapping("/farm/gateway")
    public ModelAndView farm_gateway_page(Model model, @RequestParam(defaultValue = "") String did,
                                          DateTime startTime,
                                          DateTime endTime,
                                          @RequestParam(defaultValue = "") String columns, @RequestParam(defaultValue = "true") Boolean reverseScan) {
        saveDidStartTimeEndTimeColumnsModel(model, did, startTime, endTime, columns);
        model.addAttribute("reverseScan", reverseScan);
        return new ModelAndView("hbase/farm_gateway", model.asMap());
    }

    @GetMapping("/farm/hex")
    public ModelAndView farm_hex_page(Model model, @RequestParam(defaultValue = "") String did,
                                      DateTime startTime,
                                      DateTime endTime,
                                      @RequestParam(defaultValue = "") String columns, @RequestParam(defaultValue = "true") Boolean reverseScan) {
        saveDidStartTimeEndTimeColumnsModel(model, did, startTime, endTime, columns);
        model.addAttribute("reverseScan", reverseScan);
        return new ModelAndView("hbase/farm_hex", model.asMap());
    }

    @PostMapping(value = {"/farm/gateway", "/farm/hex"})
    @ResponseBody
    public TablePlusResult farm_gateway_data(String offsetRowKey, @RequestParam(required = true) String did,
                                             @RequestParam(required = true) DateTime startTime,
                                             @RequestParam(required = true) DateTime endTime,
                                             @RequestParam(defaultValue = "10") Integer pageSize, @RequestParam(defaultValue = "*") String columns,
                                             @RequestParam(defaultValue = "true") Boolean reverseScan) {
        TablePlusResult results = new TablePlusResult();
        if (StrUtil.isNotBlank(did)) {
            String md5 = SecureUtil.md5(did.trim());
            String startRow = md5 + "_" + startTime.toString("yyyyMMddHHmmss");
            String stopRow = md5 + "_" + endTime.toString("yyyyMMddHHmmss");
            if (reverseScan == true) {
                // 逆序
                if (startRow.compareTo(stopRow) < 0) {
                    String t = startRow;
                    startRow = stopRow;
                    stopRow = t;
                }
                if (StrUtil.isNotBlank(offsetRowKey)) {
                    offsetRowKey = HbaseTool.lastCharAsciiSubOne(offsetRowKey);
                    if (offsetRowKey.compareTo(stopRow) > 0) {
                        startRow = offsetRowKey;
                    } else {
                        return results;
                    }
                }
            } else {
                // 正序
                if (startRow.compareTo(stopRow) > 0) {
                    String t = startRow;
                    startRow = stopRow;
                    stopRow = t;
                }
                if (StrUtil.isNotBlank(offsetRowKey)) {
                    offsetRowKey = HbaseTool.lastCharAsciiAddOne(offsetRowKey);
                    if (offsetRowKey.compareTo(stopRow) < 0) {
                        startRow = offsetRowKey;
                    } else {
                        return results;
                    }
                }
            }
            String order = reverseScan == true ? "desc" : "asc";
            StringBuilder sql = new StringBuilder();
            sql.append(" select " + columns + " from farm_gateway#log where ");
            sql.append("     startRowKey = '" + startRow + "' ");
            sql.append(" and stopRowKey = '" + stopRow + "' ");
            sql.append(" order by rowKey " + order);
            sql.append(" limit " + pageSize);
            List<Map<String, String>> rows = hbaseTool.select(sql.toString());
            convertTitlesAndValues(results, rows);
        }
        return results;
    }

    @RequestMapping(value = {"/farm/gateway/export", "/farm/hex/export"})
    public void farm_gateway_export(HttpServletResponse response, @RequestParam(required = true) String did,
                                    @RequestParam(required = true) DateTime startTime,
                                    @RequestParam(required = true) DateTime endTime,
                                    @RequestParam(defaultValue = "*") String columns, @RequestParam(required = true) String exportTitle,
                                    @RequestParam(defaultValue = "true") Boolean reverseScan) {
        String md5 = SecureUtil.md5(did.trim());
        String startRow = md5 + "_" + startTime.toString("yyyyMMddHHmmss");
        String stopRow = md5 + "_" + endTime.toString("yyyyMMddHHmmss");
        if (reverseScan == true) {
            // 逆序
            if (startRow.compareTo(stopRow) < 0) {
                String t = startRow;
                startRow = stopRow;
                stopRow = t;
            }
        } else {
            // 正序
            if (startRow.compareTo(stopRow) > 0) {
                String t = startRow;
                startRow = stopRow;
                stopRow = t;
            }
        }
        String order = reverseScan == true ? "desc" : "asc";
        StringBuilder sql = new StringBuilder();
        sql.append(" select " + columns + " from farm_gateway#log where ");
        sql.append("     startRowKey = '" + startRow + "' ");
        sql.append(" and stopRowKey = '" + stopRow + "' ");
        sql.append(" order by rowKey " + order);
        exportExcel(response, exportTitle, sql);
    }

    @GetMapping("/farm/can")
    public ModelAndView farm_can_page(Model model, @RequestParam(defaultValue = "") String did,
                                      DateTime startTime,
                                      DateTime endTime,
                                      @RequestParam(defaultValue = "") String columns, @RequestParam(defaultValue = "true") Boolean reverseScan) {
        saveDidStartTimeEndTimeColumnsModel(model, did, startTime, endTime, columns);
        model.addAttribute("reverseScan", reverseScan);
        return new ModelAndView("hbase/farm_can", model.asMap());
    }

    @PostMapping("/farm/can")
    @ResponseBody
    public TablePlusResult farm_can_data(String offsetRowKey, @RequestParam(required = true) String did,
                                         @RequestParam(required = true) DateTime startTime,
                                         @RequestParam(required = true) DateTime endTime,
                                         @RequestParam(defaultValue = "10") Integer pageSize, @RequestParam(defaultValue = "*") String columns,
                                         @RequestParam(defaultValue = "true") Boolean reverseScan) {
        TablePlusResult results = new TablePlusResult();
        if (StrUtil.isNotBlank(did)) {
            String startRow = did.trim() + "_" + startTime.toString("yyyyMMddHHmmss") + "_";
            String stopRow = did.trim() + "_" + endTime.toString("yyyyMMddHHmmss") + "_";
            if (reverseScan == true) {
                // 逆序
                if (startRow.compareTo(stopRow) < 0) {
                    String t = startRow;
                    startRow = stopRow;
                    stopRow = t;
                }
                if (StrUtil.isNotBlank(offsetRowKey)) {
                    offsetRowKey = HbaseTool.lastCharAsciiSubOne(offsetRowKey);
                    if (offsetRowKey.compareTo(stopRow) > 0) {
                        startRow = offsetRowKey;
                    } else {
                        return results;
                    }
                }
            } else {
                // 正序
                if (startRow.compareTo(stopRow) > 0) {
                    String t = startRow;
                    startRow = stopRow;
                    stopRow = t;
                }
                if (StrUtil.isNotBlank(offsetRowKey)) {
                    offsetRowKey = HbaseTool.lastCharAsciiAddOne(offsetRowKey);
                    if (offsetRowKey.compareTo(stopRow) < 0) {
                        startRow = offsetRowKey;
                    } else {
                        return results;
                    }
                }
            }
            String order = reverseScan == true ? "desc" : "asc";
            StringBuilder sql = new StringBuilder();
            sql.append(" select " + columns + " from farm_can#can where ");
            sql.append("     startRowKey = '" + startRow + "' ");
            sql.append(" and stopRowKey = '" + stopRow + "' ");
            sql.append(" order by rowKey " + order);
            sql.append(" limit " + pageSize);
            List<Map<String, String>> rows = hbaseTool.select(sql.toString());
            convertTitlesAndValues(results, rows);
        }
        return results;
    }

    @RequestMapping("/farm/can/export")
    public void farm_can_export(HttpServletResponse response, @RequestParam(required = true) String did,
                                @RequestParam(required = true) DateTime startTime,
                                @RequestParam(required = true) DateTime endTime,
                                @RequestParam(defaultValue = "*") String columns, @RequestParam(required = true) String exportTitle,
                                @RequestParam(defaultValue = "true") Boolean reverseScan) {
        String startRow = did.trim() + "_" + startTime.toString("yyyyMMddHHmmss") + "_";
        String stopRow = did.trim() + "_" + endTime.toString("yyyyMMddHHmmss") + "_";
        if (reverseScan == true) {
            // 逆序
            if (startRow.compareTo(stopRow) < 0) {
                String t = startRow;
                startRow = stopRow;
                stopRow = t;
            }
        } else {
            // 正序
            if (startRow.compareTo(stopRow) > 0) {
                String t = startRow;
                startRow = stopRow;
                stopRow = t;
            }
        }
        String order = reverseScan == true ? "desc" : "asc";
        StringBuilder sql = new StringBuilder();
        sql.append(" select " + columns + " from farm_can#can where ");
        sql.append("     startRowKey = '" + startRow + "' ");
        sql.append(" and stopRowKey = '" + stopRow + "' ");
        sql.append(" order by rowKey " + order);
        exportExcel(response, exportTitle, sql);
    }

    @GetMapping("/farm/command")
    public ModelAndView farm_command_page(Model model, @RequestParam(defaultValue = "down") String type,
                                          @RequestParam(defaultValue = "") String did,
                                          DateTime startTime,
                                          DateTime endTime,
                                          @RequestParam(defaultValue = "") String columns, @RequestParam(defaultValue = "true") Boolean reverseScan) {
        saveDidStartTimeEndTimeColumnsModel(model, did, startTime, endTime, columns);
        model.addAttribute("type", type);
        model.addAttribute("reverseScan", reverseScan);
        return new ModelAndView("hbase/farm_command", model.asMap());
    }

    @PostMapping("/farm/command")
    @ResponseBody
    public TablePlusResult farm_command_data(String offsetRowKey, @RequestParam(defaultValue = "down") String type,
                                             @RequestParam(required = true) String did,
                                             @RequestParam(required = true) DateTime startTime,
                                             @RequestParam(required = true) DateTime endTime,
                                             @RequestParam(defaultValue = "10") Integer pageSize, @RequestParam(defaultValue = "*") String columns,
                                             @RequestParam(defaultValue = "true") Boolean reverseScan) {
        TablePlusResult results = new TablePlusResult();
        if (StrUtil.isNotBlank(did)) {
            String startRow = type + "_" + did.trim() + "_" + startTime.toString("yyyyMMddHHmmss") + "_";
            String stopRow = type + "_" + did.trim() + "_" + endTime.toString("yyyyMMddHHmmss") + "_";
            if (reverseScan == true) {
                // 逆序
                if (startRow.compareTo(stopRow) < 0) {
                    String t = startRow;
                    startRow = stopRow;
                    stopRow = t;
                }
                if (StrUtil.isNotBlank(offsetRowKey)) {
                    offsetRowKey = HbaseTool.lastCharAsciiSubOne(offsetRowKey);
                    if (offsetRowKey.compareTo(stopRow) > 0) {
                        startRow = offsetRowKey;
                    } else {
                        return results;
                    }
                }
            } else {
                // 正序
                if (startRow.compareTo(stopRow) > 0) {
                    String t = startRow;
                    startRow = stopRow;
                    stopRow = t;
                }
                if (StrUtil.isNotBlank(offsetRowKey)) {
                    offsetRowKey = HbaseTool.lastCharAsciiAddOne(offsetRowKey);
                    if (offsetRowKey.compareTo(stopRow) < 0) {
                        startRow = offsetRowKey;
                    } else {
                        return results;
                    }
                }
            }
            String order = reverseScan == true ? "desc" : "asc";
            StringBuilder sql = new StringBuilder();
            sql.append(" select " + columns + " from farm_command#command where ");
            sql.append("     startRowKey = '" + startRow + "' ");
            sql.append(" and stopRowKey = '" + stopRow + "' ");
            sql.append(" order by rowKey " + order);
            sql.append(" limit " + pageSize);
            List<Map<String, String>> rows = hbaseTool.select(sql.toString());
            convertTitlesAndValues(results, rows);
        }
        return results;
    }

    @RequestMapping("/farm/command/export")
    public void farm_command_export(HttpServletResponse response, @RequestParam(defaultValue = "down") String type,
                                    @RequestParam(required = true) String did,
                                    @RequestParam(required = true) DateTime startTime,
                                    @RequestParam(required = true) DateTime endTime,
                                    @RequestParam(defaultValue = "*") String columns, @RequestParam(required = true) String exportTitle,
                                    @RequestParam(defaultValue = "true") Boolean reverseScan) {
        String startRow = type + "_" + did.trim() + "_" + startTime.toString("yyyyMMddHHmmss") + "_";
        String stopRow = type + "_" + did.trim() + "_" + endTime.toString("yyyyMMddHHmmss") + "_";
        if (reverseScan == true) {
            // 逆序
            if (startRow.compareTo(stopRow) < 0) {
                String t = startRow;
                startRow = stopRow;
                stopRow = t;
            }
        } else {
            // 正序
            if (startRow.compareTo(stopRow) > 0) {
                String t = startRow;
                startRow = stopRow;
                stopRow = t;
            }
        }
        String order = reverseScan == true ? "desc" : "asc";
        StringBuilder sql = new StringBuilder();
        sql.append(" select " + columns + " from farm_command#command where ");
        sql.append("     startRowKey = '" + startRow + "' ");
        sql.append(" and stopRowKey = '" + stopRow + "' ");
        sql.append(" order by rowKey " + order);
        exportExcel(response, exportTitle, sql);
    }

    @GetMapping("/farm/work")
    public ModelAndView farm_work_page(Model model, @RequestParam(defaultValue = "") String did,
                                       DateTime startTime,
                                       DateTime endTime,
                                       @RequestParam(defaultValue = "") String columns, @RequestParam(defaultValue = "true") Boolean reverseScan) {
        saveDidStartTimeEndTimeColumnsModel(model, did, startTime, endTime, columns);
        model.addAttribute("reverseScan", reverseScan);
        return new ModelAndView("hbase/farm_work", model.asMap());
    }

    @PostMapping("/farm/work")
    @ResponseBody
    public TablePlusResult farm_work_data(String offsetRowKey, @RequestParam(required = true) String did,
                                          @RequestParam(required = true) DateTime startTime,
                                          @RequestParam(required = true) DateTime endTime,
                                          @RequestParam(defaultValue = "10") Integer pageSize, @RequestParam(defaultValue = "*") String columns,
                                          @RequestParam(defaultValue = "true") Boolean reverseScan) {
        TablePlusResult results = new TablePlusResult();
        if (StrUtil.isNotBlank(did)) {
            String startRow = did.trim() + "_" + startTime.toString("yyyyMMddHHmmss");
            String stopRow = did.trim() + "_" + endTime.toString("yyyyMMddHHmmss");
            if (reverseScan == true) {
                // 逆序
                if (startRow.compareTo(stopRow) < 0) {
                    String t = startRow;
                    startRow = stopRow;
                    stopRow = t;
                }
                if (StrUtil.isNotBlank(offsetRowKey)) {
                    offsetRowKey = HbaseTool.lastCharAsciiSubOne(offsetRowKey);
                    if (offsetRowKey.compareTo(stopRow) > 0) {
                        startRow = offsetRowKey;
                    } else {
                        return results;
                    }
                }
            } else {
                // 正序
                if (startRow.compareTo(stopRow) > 0) {
                    String t = startRow;
                    startRow = stopRow;
                    stopRow = t;
                }
                if (StrUtil.isNotBlank(offsetRowKey)) {
                    offsetRowKey = HbaseTool.lastCharAsciiAddOne(offsetRowKey);
                    if (offsetRowKey.compareTo(stopRow) < 0) {
                        startRow = offsetRowKey;
                    } else {
                        return results;
                    }
                }
            }
            String order = reverseScan == true ? "desc" : "asc";
            StringBuilder sql = new StringBuilder();
            sql.append(" select " + columns + " from farm_work#work where ");
            sql.append("     startRowKey = '" + startRow + "' ");
            sql.append(" and stopRowKey = '" + stopRow + "' ");
            sql.append(" order by rowKey " + order);
            sql.append(" limit " + pageSize);
            List<Map<String, String>> rows = hbaseTool.select(sql.toString());
            convertTitlesAndValues(results, rows);
        }
        return results;
    }

    @GetMapping("/farm/split")
    public ModelAndView farm_split_page(Model model, @RequestParam(defaultValue = "") String did,
                                        DateTime startTime,
                                        DateTime endTime,
                                        @RequestParam(defaultValue = "") String columns, @RequestParam(defaultValue = "true") Boolean reverseScan) {
        saveDidStartTimeEndTimeColumnsModel(model, did, startTime, endTime, columns);
        model.addAttribute("reverseScan", reverseScan);
        return new ModelAndView("hbase/farm_work_split", model.asMap());
    }

    @PostMapping("/farm/split")
    @ResponseBody
    public TablePlusResult farm_split_data(String offsetRowKey, @RequestParam(required = true) String did,
                                           @RequestParam(required = true) DateTime startTime,
                                           @RequestParam(required = true) DateTime endTime,
                                           @RequestParam(defaultValue = "10") Integer pageSize, @RequestParam(defaultValue = "*") String columns,
                                           @RequestParam(defaultValue = "true") Boolean reverseScan) {
        TablePlusResult results = new TablePlusResult();
        if (StrUtil.isNotBlank(did)) {
            String startRow = did.trim() + "_" + startTime.toString("yyyyMMddHHmmss");
            String stopRow = did.trim() + "_" + endTime.toString("yyyyMMddHHmmss");
            if (reverseScan == true) {
                // 逆序
                if (startRow.compareTo(stopRow) < 0) {
                    String t = startRow;
                    startRow = stopRow;
                    stopRow = t;
                }
                if (StrUtil.isNotBlank(offsetRowKey)) {
                    offsetRowKey = HbaseTool.lastCharAsciiSubOne(offsetRowKey);
                    if (offsetRowKey.compareTo(stopRow) > 0) {
                        startRow = offsetRowKey;
                    } else {
                        return results;
                    }
                }
            } else {
                // 正序
                if (startRow.compareTo(stopRow) > 0) {
                    String t = startRow;
                    startRow = stopRow;
                    stopRow = t;
                }
                if (StrUtil.isNotBlank(offsetRowKey)) {
                    offsetRowKey = HbaseTool.lastCharAsciiAddOne(offsetRowKey);
                    if (offsetRowKey.compareTo(stopRow) < 0) {
                        startRow = offsetRowKey;
                    } else {
                        return results;
                    }
                }
            }
            String order = reverseScan == true ? "desc" : "asc";
            StringBuilder sql = new StringBuilder();
            sql.append(" select " + columns + " from farm_work_split#work where ");
            sql.append("     startRowKey = '" + startRow + "' ");
            sql.append(" and stopRowKey = '" + stopRow + "' ");
            sql.append(" order by rowKey " + order);
            sql.append(" limit " + pageSize);
            List<Map<String, String>> rows = hbaseTool.select(sql.toString());
            convertTitlesAndValues(results, rows);
        }
        return results;
    }

    @GetMapping("/ne/packageCount")
    public ModelAndView ne_packageCount_page(Model model, @RequestParam(defaultValue = "") String did,
                                             DateTime startTime,
                                             DateTime endTime,
                                             @RequestParam(defaultValue = "") String actualPackageNum,
                                             @RequestParam(defaultValue = "true") Boolean reverseScan) {
        saveDidStartTimeEndTimeActualPackageNumModel(model, did, startTime, endTime, actualPackageNum);
        model.addAttribute("reverseScan", reverseScan);
        return new ModelAndView("hbase/ne_packageCount", model.asMap());
    }

    @GetMapping("/farm/canCount")
    public ModelAndView farm_canCount_page(Model model, @RequestParam(defaultValue = "") String did,
                                           DateTime day) {
        model.addAttribute("did", did);
        if (day != null) {
            model.addAttribute("day", day.toString("yyyy-MM-dd"));
        } else {
            model.addAttribute("day", new DateTime().toString("yyyy-MM-dd"));
        }
        return new ModelAndView("hbase/farm_canCount", model.asMap());
    }

    @PostMapping("/farm/canCount")
    @ResponseBody
    public Map<String, List> farm_canCount_data(@RequestParam(required = true) String did,
                                                @RequestParam(required = true) DateTime day) {
        Map<String, List> m = new HashMap<>();
        List<String> titles = new ArrayList<>();
        List<Long> datas = new ArrayList<>();
        if (StrUtil.isNotBlank(did)) {
            for (int i = 0; i < 24; i++) {
                String hour = StrUtil.fill("" + i, '0', 2, true);
                titles.add(StrUtil.format("{}:00:00-{}:59:59", hour, hour));
                String sql = StrUtil
                        .format("select count(*) from farm_can#can where startRowKey = '{}' and stopRowKey = '{}'"
                                , did + "_" + day.toString("yyyyMMdd") + hour + "0000"
                                , did + "_" + day.toString("yyyyMMdd") + hour + "5959"
                        );
                datas.add(hbaseTool.count(sql));
            }
        } else {
            for (int i = 0; i < 24; i++) {
                String hour = StrUtil.fill("" + i, '0', 2, true);
                titles.add(StrUtil.format("{}:00:00-{}:59:59", hour, hour));
                datas.add(0L);
            }
        }
        m.put("titles", titles);
        m.put("datas", datas);
        return m;
    }

    private void saveDidStartTimeEndTimeActualPackageNumModel(Model model, String did, DateTime startTime,
                                                              DateTime endTime, String actualPackageNum) {
        saveDidStartTimeEndTimeModel(model, did, startTime, endTime);
        model.addAttribute("actualPackageNum", actualPackageNum.trim());
    }

    @PostMapping("/ne/packageCount")
    @ResponseBody
    public TablePlusResult ne_packageCount_data(String offsetRowKey, @RequestParam(required = true) String did,
                                                @RequestParam(required = true) DateTime startTime,
                                                @RequestParam(required = true) DateTime endTime,
                                                @RequestParam(defaultValue = "") String actualPackageNum,
                                                @RequestParam(defaultValue = "true") Boolean reverseScan) {
        TablePlusResult results = new TablePlusResult();
        if (StrUtil.isNotBlank(did)) {
            results = getPackageCountResult(results, "1", "1", did, startTime, endTime);
        }
        return results;
    }

    @GetMapping("/farm/packageCount")
    public ModelAndView farm_packageCount_page(Model model, @RequestParam(defaultValue = "") String did,
                                               DateTime startTime,
                                               DateTime endTime,
                                               @RequestParam(defaultValue = "") String actualPackageNum,
                                               @RequestParam(defaultValue = "true") Boolean reverseScan) {
        saveDidStartTimeEndTimeActualPackageNumModel(model, did, startTime, endTime, actualPackageNum);
        model.addAttribute("reverseScan", reverseScan);
        return new ModelAndView("hbase/farm_packageCount", model.asMap());
    }

    @PostMapping("/farm/packageCount")
    @ResponseBody
    public TablePlusResult farm_packageCount_data(String offsetRowKey, @RequestParam(required = true) String did,
                                                  @RequestParam(required = true) DateTime startTime,
                                                  @RequestParam(required = true) DateTime endTime,
                                                  @RequestParam(defaultValue = "") String actualPackageNum,
                                                  @RequestParam(defaultValue = "true") Boolean reverseScan) {
        TablePlusResult results = new TablePlusResult();
        if (StrUtil.isNotBlank(did)) {
            results = getPackageCountResult(results, "2", "1", did, startTime, endTime);
        }
        return results;
    }

    private TablePlusResult getPackageCountResult(TablePlusResult results, String statType, String order, String did,
                                                  DateTime startTime, DateTime endTime) {
        String md5 = SecureUtil.md5(did);
        String tableName = "gateway";
        String familyName = "log";
        if (statType.equals("2")) {
            tableName = "farm_gateway";
        }
        DateTime a = null, b = null;
        results.getTitles().put("packageTime", "接收时间");
        results.getTitles().put("packageNum", "接收报文条数");
        List rowsList = results.getRows();
        if (endTime.getTime() > startTime.getTime()) {
            a = startTime;
            b = endTime;
        } else {
            b = startTime;
            a = endTime;
        }
        // TODO 循环每小时
        for (; ; ) {
            if (a.getTime() > b.getTime()) {
                break;
            }
            String startRow, stopRow;
            Map rowMap = new HashMap();
            if ((a.toString("yyyyMMddHH") + ":59:59").compareTo(b.toString("yyyyMMddHHmmss")) > 0) {
                rowMap.put("packageTime", a.toString("yyyy-MM-dd HH:mm:ss") + " 至 " + b.toString("yyyy-MM-dd HH:mm:ss"));
                startRow = md5 + "_" + a.toString("yyyyMMddHHmmss");
                stopRow = md5 + "_" + b.toString("yyyyMMddHHmmss");
            } else {
                rowMap.put("packageTime", a.toString("yyyy-MM-dd HH:mm:ss") + " 至 " + a.toString("yyyy-MM-dd HH") + ":59:59");
                startRow = md5 + "_" + a.toString("yyyyMMddHHmmss");
                stopRow = md5 + "_" + a.toString("yyyyMMddHH") + ":59:59";
            }
            rowMap.put("packageNum", hbaseTool.count("select count(*) from " + tableName + "#" + familyName + " where startRowKey = '" + startRow + "' and stopRowKey = '" + stopRow + "'"));
            rowsList.add(rowMap);
            a = a.setField(DateField.MINUTE, 0);
            a = a.setField(DateField.SECOND, 0);
            a = a.offset(DateField.HOUR_OF_DAY, 1);
        }
        return results;
    }

    // 统计包间隔页面-新能源
    @GetMapping("/ne/intervalCount")
    public ModelAndView ne_intervalCount_page(Model model, @RequestParam(defaultValue = "") String did,
                                              DateTime startTime,
                                              DateTime endTime) {
        saveDidStartTimeEndTimeModel(model, did, startTime, endTime);
        return new ModelAndView("hbase/ne_intervalCount", model.asMap());
    }

    // 统计包间隔数据-新能源
    @PostMapping("/ne/intervalCount")
    @ResponseBody
    public TablePlusResult intervalCount_data(@RequestParam(required = true) String did,
                                              @RequestParam(required = true) DateTime startTime,
                                              @RequestParam(required = true) DateTime endTime) {
        TablePlusResult results = new TablePlusResult();
        Map<Long, Long> resultMap = new TreeMap<>(Long::compareTo);
        if (StrUtil.isNotBlank(did)) {
            String md5 = SecureUtil.md5(did.trim());
            String startRow = md5 + "_" + startTime.toString("yyyyMMddHHmmss");
            String stopRow = md5 + "_" + endTime.toString("yyyyMMddHHmmss");
            if (startRow.compareTo(stopRow) > 0) {
                String t = startRow;
                startRow = stopRow;
                stopRow = t;
            }
            List<Map<String, String>> rows = hbaseTool.select("select 3014 from can_ne#can where startRowKey = '" + startRow + "' and stopRowKey = '" + stopRow + "'");
            if (CollUtil.isNotEmpty(rows)) {
                Long lastLong = null;
                for (Map<String, String> row : rows) {
                    String gpsTime = row.get("3014");
                    if (null != gpsTime) {
                        long gpsLong = Long.parseLong(gpsTime);
                        if (null != lastLong) {
                            long mapKey = gpsLong - lastLong;
                            if (null == resultMap.get(mapKey)) {
                                resultMap.put(mapKey, 1l);
                            } else {
                                resultMap.put(mapKey, resultMap.get(mapKey) + 1);
                            }
                        }
                        lastLong = gpsLong;
                    }
                }
            }

            results.getTitles().put("second", "second");
            results.getTitles().put("count", "count");

            for (Entry<Long, Long> entry : resultMap.entrySet()) {
                Map<String, Long> m = new HashMap<>();
                m.put("second", Math.abs(entry.getKey()));
                m.put("count", entry.getValue());
                results.getRows().add(m);
            }

            results.setTotal(Convert.toLong(results.getRows().size()));

        }
        return results;
    }

    // 统计包间隔页面-农机
    @GetMapping("/farm/intervalCount")
    public ModelAndView farm_intervalCount_page(Model model, @RequestParam(defaultValue = "") String did,
                                                DateTime startTime,
                                                DateTime endTime) {
        saveDidStartTimeEndTimeModel(model, did, startTime, endTime);
        return new ModelAndView("hbase/farm_intervalCount", model.asMap());
    }

    // 统计包间隔数据-农机
    @PostMapping("/farm/intervalCount")
    @ResponseBody
    public TablePlusResult farm_intervalCount_data(@RequestParam(required = true) String did,
                                                   @RequestParam(required = true) DateTime startTime,
                                                   @RequestParam(required = true) DateTime endTime) {
        TablePlusResult results = new TablePlusResult();
        Map<Long, Long> resultMap = new TreeMap<>(Long::compareTo);

        if (StrUtil.isNotBlank(did)) {
            String startRow = did.trim() + "_" + startTime.toString("yyyyMMddHHmmss");
            String stopRow = did.trim() + "_" + endTime.toString("yyyyMMddHHmmss");
            if (startRow.compareTo(stopRow) > 0) {
                String t = startRow;
                startRow = stopRow;
                stopRow = t;
            }
            List<Map<String, String>> rows = hbaseTool.select("select 3014 from farm_can#can where startRowKey = '" + startRow + "' and stopRowKey = '" + stopRow + "'");
            if (CollUtil.isNotEmpty(rows)) {
                Long lastLong = null;
                for (Map<String, String> row : rows) {
                    String gpsTime = row.get("3014");
                    if (null != gpsTime) {
                        long gpsLong = Long.parseLong(gpsTime);
                        if (null != lastLong) {
                            long mapKey = gpsLong - lastLong;
                            if (null == resultMap.get(mapKey)) {
                                resultMap.put(mapKey, 1l);
                            } else {
                                resultMap.put(mapKey, resultMap.get(mapKey) + 1);
                            }
                        }
                        lastLong = gpsLong;
                    }
                }
            }

            results.getTitles().put("second", "second");
            results.getTitles().put("count", "count");

            for (Entry<Long, Long> entry : resultMap.entrySet()) {
                Map<String, Long> m = new HashMap<>();
                m.put("second", Math.abs(entry.getKey()));
                m.put("count", entry.getValue());
                results.getRows().add(m);
            }

            results.setTotal(Convert.toLong(results.getRows().size()));

        }
        return results;
    }


    @GetMapping("/ql")
    public ModelAndView ql_page(Model model, @RequestParam(defaultValue = "select * from can_ne#can where 3014 != '' order by rowKey desc limit 10") String ql) {
        model.addAttribute("ql", ql.trim());
        return new ModelAndView("hbase/ql", model.asMap());
    }

    @PostMapping("/ql")
    @ResponseBody
    public TablePlusResult ql_data(String offsetRowKey, @RequestParam(required = true) String ql) {
        ql = ql.trim();
        String select = null, where = null, order = null, limit = null, startRowKey = HbaseTool.FIRST_VISIBLE_ASCII, stopRowKey = HbaseTool.LAST_VISIBLE_ASCII;
        if (!ql.contains(" count")) {
            if (ql.contains(" limit ")) {
                int limitIndex = ql.indexOf(" limit ");
                limit = ql.substring(limitIndex);
                ql = ql.substring(0, limitIndex);
            } else {
                limit = " limit 10";
            }
            if (ql.contains(" order ") && ql.contains(" by ")) {
                int orderIndex = ql.indexOf(" order ");
                order = ql.substring(orderIndex);
                ql = ql.substring(0, orderIndex);
            } else {
                order = StrUtil.format(" order by {} asc", HbaseTool.ROW_KEY_NAME);
            }
            if (order.contains(" desc")) {
                startRowKey = HbaseTool.LAST_VISIBLE_ASCII;
                stopRowKey = HbaseTool.FIRST_VISIBLE_ASCII;
            }
        }
        if (ql.contains(" where ")) {
            int whereIndex = ql.indexOf(" where ");
            where = ql.substring(whereIndex);
            ql = ql.substring(0, whereIndex);
            if (!where.contains(StrUtil.format(" {}", HbaseTool.START_ROW_KEY_NAME))) {
                where += StrUtil
                        .format(" and {} = '{}'"
                                , HbaseTool.START_ROW_KEY_NAME, startRowKey
                        );
            }
            if (!where.contains(StrUtil.format(" {}", HbaseTool.STOP_ROW_KEY_NAME))) {
                where += StrUtil
                        .format(" and {} = '{}'"
                                , HbaseTool.STOP_ROW_KEY_NAME, stopRowKey
                        );
            }
        } else {
            where = StrUtil
                    .format(" where {} = '{}' and {} = '{}'"
                            , HbaseTool.START_ROW_KEY_NAME, startRowKey
                            , HbaseTool.STOP_ROW_KEY_NAME, stopRowKey
                    );
        }
        if (StrUtil.isNotBlank(offsetRowKey)) {
            if (order.contains(" desc")) {
                offsetRowKey = HbaseTool.lastCharAsciiSubOne(offsetRowKey);
            } else {
                offsetRowKey = HbaseTool.lastCharAsciiAddOne(offsetRowKey);
            }
            StringBuilder newWhere = new StringBuilder();
            String reg = StrUtil.format(" {}[ ]*=[ ]*'[a-zA-z0-9!~]+'", HbaseTool.START_ROW_KEY_NAME);
            String startRowKeyContent = ReUtil.get(reg, where, 0);
            newWhere.append(where.substring(0, where.indexOf(StrUtil.format(" {}", HbaseTool.START_ROW_KEY_NAME))));
            int pre = newWhere.toString().length();
            newWhere.append(StrUtil.format(" {} = '{}'", HbaseTool.START_ROW_KEY_NAME, offsetRowKey));
            newWhere.append(where.substring(startRowKeyContent.length() + pre));
            where = newWhere.toString();
        }
        select = ql;
        StringBuilder sql = new StringBuilder();
        sql.append(select);
        sql.append(where);
        if (StrUtil.isNotBlank(order)) {
            sql.append(order);
        }
        if (StrUtil.isNotBlank(limit)) {
            sql.append(limit);
        }
        log.debug(sql.toString());
        TablePlusResult results = new TablePlusResult();
        if (sql.toString().contains(" count")) {
            List<Map<String, String>> datas = new ArrayList();
            Map<String, String> row = new HashMap();
            row.put("c", "" + hbaseTool.count(sql.toString()));
            datas.add(row);
            results.getTitles().put("c", "总数");
            results.setRows(datas);
            results.setTotal(1L);
        } else {
            List<Map<String, String>> datas = hbaseTool.select(sql.toString());
            if (CollUtil.isNotEmpty(datas)) {
                for (Map<String, String> data : datas) {
                    for (Entry<String, String> kv : data.entrySet()) {
                        results.getTitles().put(kv.getKey(), kv.getKey());
                    }
                }
                results.setRows(datas);
            }
        }
        return results;
    }

}
