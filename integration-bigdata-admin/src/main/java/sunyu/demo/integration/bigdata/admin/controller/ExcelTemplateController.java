package sunyu.demo.integration.bigdata.admin.controller;

import cn.hutool.core.io.IoUtil;
import cn.hutool.core.io.resource.ResourceUtil;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStream;

@Controller
@RequestMapping("/excelTemplate")
@CrossOrigin
public class ExcelTemplateController extends IndexController {

    @GetMapping("/didTemplate")
    public ResponseEntity<Resource> didTemplate(HttpServletRequest request) throws IOException {
        String agent = request.getHeader("User-Agent");
        String excelName = "设备编号模板.xlsx";
        String fileName = null;
        InputStream stream = ResourceUtil.getStream("static/excelTemplate/" + excelName);
        byte[] bytes = IoUtil.readBytes(stream);
        ByteArrayResource resource = new ByteArrayResource(bytes);
        if (agent.toLowerCase().indexOf("firefox") > -1) {
            fileName = new String(excelName.getBytes("UTF-8"), "ISO-8859-1");
        } else {
            fileName = java.net.URLEncoder.encode(excelName, "UTF8");
        }
        HttpHeaders headers = new HttpHeaders();
        headers.add("Cache-Control", "no-cache, no-store, must-revalidate");
        headers.add("Content-Disposition", String.format("attachment; filename=%s", fileName));
        headers.add("Pragma", "no-cache");
        headers.add("Expires", "0");
        return ResponseEntity
                .ok()
                .headers(headers)
                .contentLength(bytes.length)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
    }
}
