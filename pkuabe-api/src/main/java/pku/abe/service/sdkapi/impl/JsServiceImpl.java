package pku.abe.service.sdkapi.impl;

import pku.abe.commons.redis.JedisPort;
import pku.abe.commons.shard.ShardingSupportHash;
import pku.abe.data.model.params.JsRecordIdParams;
import pku.abe.service.sdkapi.JsService;

import javax.annotation.Resource;

/**
 * Created by LinkedME01 on 16/5/19.
 */
public class JsServiceImpl implements JsService {
    @Resource
    private ShardingSupportHash<JedisPort> clientShardingSupport;

    @Override
    public void recordId(JsRecordIdParams jsRecordIdParams) {
        String identityId = String.valueOf(jsRecordIdParams.identity_id);
        if (jsRecordIdParams.is_valid_identityid) {
            JedisPort identityRedisClient = clientShardingSupport.getClient(identityId);
            boolean res = identityRedisClient.set(identityId + ".dpi", jsRecordIdParams.deeplink_id);
            if (res) {
                identityRedisClient.expire(identityId + ".dpi", 2 * 60 * 60);
            }
        }
        String browserFingerprintId = jsRecordIdParams.browser_fingerprint_id;
        JedisPort browseFingerprintIdRedisClient = clientShardingSupport.getClient(browserFingerprintId);
        browseFingerprintIdRedisClient.hset(browserFingerprintId, "iid", identityId);
        browseFingerprintIdRedisClient.hset(browserFingerprintId, "did", jsRecordIdParams.deeplink_id);
        browseFingerprintIdRedisClient.expire(browserFingerprintId, 2 * 60 * 60); // 设置过期时间

    }
}
