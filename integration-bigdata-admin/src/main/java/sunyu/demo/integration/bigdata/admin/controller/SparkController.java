package sunyu.demo.integration.bigdata.admin.controller;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.date.DateTime;
import cn.hutool.setting.dialect.Props;
import com.alibaba.fastjson.JSONArray;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import sunyu.demo.integration.bigdata.admin.pojo.SparkAppStatus;
import sunyu.demo.integration.bigdata.admin.pojo.TablePlusResult;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/spark")
@CrossOrigin
public class SparkController extends IndexController {
    Props props = new Props("application.properties");

    @PostMapping("/app/status")
    @ResponseBody
    synchronized public TablePlusResult getSparkAppStatus() {
        TablePlusResult r = new TablePlusResult();
        List<SparkAppStatus> l = new ArrayList<>();

        try {
            // 先获取所有spark项目列表
            // http://cdh2:8088/cluster/apps/RUNNING
            String runningUrl = props.getStr("spark.cluster.url") + "/cluster/apps/RUNNING";
            Document runningDoc = Jsoup.connect(runningUrl).get();

            JSONArray appDatas = JSONArray.parseArray(runningDoc.select("#apps script").html().replace("var appsTableData=", ""));

            if (CollUtil.isNotEmpty(appDatas)) {
                for (Object appData : appDatas) {
                    SparkAppStatus status = new SparkAppStatus();

                    JSONArray data = (JSONArray) appData;
                    String appUrl = Jsoup.parse(data.getString(data.size() - 1)).select("a").attr("href");

                    // http://cdh2:8088/proxy/application_1546499077880_0560/streaming/
                    String streamingUrl = appUrl + "streaming/";
                    // http://cdh2:8088/proxy/application_1546499077880_0560/executors/
                    String executorsUrl = appUrl + "executors/";
                    // http://cdh2:8088/cluster/app/application_1546499077880_0306
                    String appClusterUrl = appUrl.replace("/proxy/", "/cluster/app/");

                    //running页面信息
                    status.setId(Jsoup.parse(data.getString(0)).select("a").text());
                    //http://cdh2:8088/cluster/app/application_1553139809646_0354
                    status.setUrl(props.getStr("spark.cluster.url") + "/cluster/app/" + status.getId());
                    status.setName(data.getString(2));
                    status.setStartTime(new DateTime(data.getLongValue(5)).toString("yyyy-MM-dd HH:mm:ss"));
                    status.setContainers(data.getString(9));
                    status.setCpu(data.getString(10));
                    status.setMemory(data.getString(11));

                    //app cluster页面信息
                    try {
                        Document appClusterDoc = Jsoup.connect(appClusterUrl).get();
                        status.setAttemptNumber(String.valueOf(appClusterDoc.select("#app tbody tr").size() - 2));
                    } catch (Exception e) {
                        //StaticLog.error(e);
                    }

                    //streaming页面信息
                    try {
                        Document streamingDoc = Jsoup.connect(streamingUrl).get();
                        Elements statTable = streamingDoc.select("#stat-table");
                        status.setWindowPeriod(statTable.prev().prev().prev().select("strong:eq(0)").text());
                        status.setInput(statTable
                                .select("tbody tr:eq(1) td:eq(0) div:eq(0) div:eq(1)")
                                .text()
                                .trim());
                        status.setSchedulingDelay(statTable
                                .select("tbody tr:eq(2) td:eq(0) div:eq(0) div:eq(1)")
                                .text()
                                .replace("Avg:", "")
                                .trim());
                        status.setProcessingTime(statTable
                                .select("tbody tr:eq(3) td:eq(0) div:eq(0) div:eq(1)")
                                .text()
                                .trim());
                        status.setTotalDelay(statTable
                                .select("tbody tr:eq(4) td:eq(0) div:eq(0) div:eq(1)")
                                .text()
                                .replace("Avg:", "")
                                .trim());
                    } catch (Exception e) {
                        //StaticLog.error(e);
                    }

                    //executors页面信息
                    try {
                        Document executorsDoc = Jsoup.connect(executorsUrl).get();
                        Element summaryTable = executorsDoc.select("table").get(0);
                        status.setFailed(summaryTable.select("tbody tr:eq(2) td:eq(6)").text());
                        status.setDead(summaryTable.select("tbody tr:eq(1) td:eq(0)").text().replace("Dead(", "").replace(")", ""));
                    } catch (Exception e) {
                        //StaticLog.error(e);
                    }

                    l.add(status);
                }
            }
        } catch (Exception e) {
            //StaticLog.error(e);
        }

        if (CollUtil.isNotEmpty(l)) {
            l.sort((o1, o2) -> o2.getStartTime().compareTo(o1.getStartTime()));
            r.setRows(l);
            r.setTotal(Convert.toLong(l.size()));
        }

        return r;
    }

}
