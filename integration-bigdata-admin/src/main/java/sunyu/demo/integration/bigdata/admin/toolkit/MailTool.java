package sunyu.demo.integration.bigdata.admin.toolkit;

import cn.hutool.core.util.CharsetUtil;
import cn.hutool.log.StaticLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeUtility;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.List;

@Component
public class MailTool {

    static {
        //不允许自动截取邮件中附件的长度
        System.setProperty("mail.mime.splitlongparameters", "false");
    }

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    String mailUser;

    /**
     * 发送邮件
     *
     * @param tos     给谁发
     * @param title   标题
     * @param content 内容
     * @param files   附件
     */
    public void sendMail(List<String> tos, String title, String content, boolean isHtml, File... files) {
        try {
            for (String to : tos) {
                MimeMessage mimeMessage = mailSender.createMimeMessage();
                MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, true, CharsetUtil.UTF_8);
                mimeMessageHelper.setFrom(mailUser);
                mimeMessageHelper.setTo(to);
                mimeMessageHelper.setSubject(title);
                mimeMessageHelper.setText(content, isHtml);

                if (files != null) {
                    for (File file : files) {
                        mimeMessageHelper.addAttachment(MimeUtility.encodeWord(file.getName(), CharsetUtil.UTF_8, "B"), new FileSystemResource(file));
                    }
                }

                mailSender.send(mimeMessage);
            }
        } catch (MessagingException e) {
            StaticLog.error(e);
        } catch (UnsupportedEncodingException e) {
            StaticLog.error(e);
        }
    }
}
