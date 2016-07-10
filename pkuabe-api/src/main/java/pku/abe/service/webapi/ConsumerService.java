package pku.abe.service.webapi;

import pku.abe.dao.webapi.ConsumerAppDao;
import pku.abe.data.model.ConsumerAppInfo;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * Created by LinkedME01 on 16/4/8.
 */

@Service
public class ConsumerService {
    @Resource
    ConsumerAppDao consumerAppDao;
    public long createConsumerApp() {
        return 0;
    }

    public ConsumerAppInfo getConsumerAppInfo(long appId) {
        ConsumerAppInfo consumerAppInfo = consumerAppDao.getConsumerAppInfo(appId);
        consumerAppInfo.setAppId((int)appId);
        return consumerAppInfo;
    }

    public List<ConsumerAppInfo> getAllConsumerApp() {
        List<ConsumerAppInfo> consumerAppInfoList = consumerAppDao.getAllConsumerApps();
        return consumerAppInfoList;
    }
}
