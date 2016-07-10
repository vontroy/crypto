package pku.abe.service.sdkapi.impl;

import pku.abe.commons.log.ApiLogger;
import pku.abe.commons.redis.JedisPort;
import pku.abe.commons.shard.ShardingSupportHash;
import pku.abe.commons.util.Util;
import pku.abe.dao.sdkapi.ClientDao;
import pku.abe.data.model.ClientInfo;
import pku.abe.service.DeepLinkService;
import pku.abe.service.sdkapi.ClientService;
import com.google.common.base.Strings;

import javax.annotation.Resource;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by LinkedME01 on 16/3/20.
 */
public class ClientServiceImpl implements ClientService {
    @Resource
    private ClientDao clientDao;

    @Resource
    private DeepLinkService deepLinkService;

    @Resource
    private ShardingSupportHash<JedisPort> deepLinkCountShardingSupport;

    @Resource
    private ShardingSupportHash<JedisPort> linkedmeKeyShardingSupport;

    public static ThreadPoolExecutor deepLinkCountThreadPool = new ThreadPoolExecutor(20, 20, 60L, TimeUnit.SECONDS,
            new LinkedBlockingQueue<Runnable>(300), new ThreadPoolExecutor.DiscardOldestPolicy());

    public int addClient(ClientInfo clientInfo, long deepLinkId) {
        int result = clientDao.addClient(clientInfo);
        return result;
    }
}
