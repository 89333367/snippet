package sunyu.demo.integration.bigdata.admin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.springframework.stereotype.Component;
import sunyu.demo.integration.bigdata.admin.pojo.ShiroSubject;

@Component
public interface SubjectMapper extends BaseMapper<ShiroSubject> {
}
