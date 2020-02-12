package sunyu.demo.integration.bigdata.admin.controller;

import cn.hutool.core.convert.Convert;
import cn.hutool.log.StaticLog;
import cn.hutool.setting.dialect.Props;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import sunyu.demo.integration.bigdata.admin.pojo.TablePlusResult;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/azkaban")
@CrossOrigin
public class AzkabanController extends IndexController {
    Props props = new Props("application.properties");
    String url = props.getStr("azkaban.url") + "/executor";
    Connection connect = Jsoup.connect(url);

    @PostMapping("/executor")
    @ResponseBody
    synchronized public TablePlusResult getSparkAppStatus() {
        TablePlusResult r = new TablePlusResult();
        try {
            Connection.Response response = connect.execute();
            Document doc = response.parse();
            if (doc.select("#login-form").size() == 1) {//未登录
                Map<String, String> params = new HashMap<>();
                params.put("action", "login");
                params.put("username", "azkaban");
                params.put("password", "azkaban");
                connect.data(params);
                response = connect.ignoreContentType(true).method(Connection.Method.POST).execute();
                connect = Jsoup.connect(url).cookies(response.cookies());
            }
            doc = connect.get();
            Elements executingJobs = doc.select("#executingJobs");
            Elements trs = executingJobs.select("tbody tr");
            if (trs.size() > 0) {
                //拼装数据
                for (Element tr : trs) {
                    Map row = new HashMap();
                    Elements tds = tr.select("td");
                    for (int i = 0; i < tds.size(); i++) {
                        if (i != 0 && i != 8 && i != 10 && i != 11) {
                            row.put("c" + i, tds.get(i).text());
                        }
                    }
                    //http://192.168.11.132:8081/executor?execid=36175#jobslist
                    row.put("url", url + "?execid=" + row.get("c1") + "#jobslist");
                    r.getRows().add(row);
                }
                r.setTotal(Convert.toLong(r.getRows().size()));
            }
        } catch (IOException e) {
            StaticLog.error(e);
        }
        return r;
    }
}
