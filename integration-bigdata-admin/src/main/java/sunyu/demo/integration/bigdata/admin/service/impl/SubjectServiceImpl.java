package sunyu.demo.integration.bigdata.admin.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import sunyu.demo.integration.bigdata.admin.mapper.SubjectMapper;
import sunyu.demo.integration.bigdata.admin.pojo.ShiroSubject;
import sunyu.demo.integration.bigdata.admin.service.SubjectService;

@Service
public class SubjectServiceImpl extends ServiceImpl<SubjectMapper, ShiroSubject> implements SubjectService {
}
