package sunyu.demo.integration.bigdata.admin.config.convert;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;

import java.beans.PropertyEditorSupport;

/**
 * 将前台form表单传递过来的字符串，转换成DateTime类型
 *
 * @author SunYu
 */
public class DateTimeEditor extends PropertyEditorSupport {

    @Override
    public void setAsText(String text) throws IllegalArgumentException {
        if (StrUtil.isBlank(text)) {
            setValue(null);
        } else {
            DateTime dateTime = null;
            try {
                dateTime = DateUtil.parse(text, "yyyy-MM-dd HH:mm:ss");
            } catch (Exception e) {
                try {
                    dateTime = DateUtil.parse(text, "yyyy-MM-dd");
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
            setValue(dateTime);
        }
    }

    @Override
    public String getAsText() {
        DateTime value = (DateTime) getValue();
        return (value != null ? value.toString("yyyy-MM-dd HH:mm:ss") : "");
    }
}
