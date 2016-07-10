package pku.abe.service.sdkapi.impl;

import pku.abe.commons.log.ApiLogger;
import pku.abe.commons.redis.JedisPort;
import pku.abe.commons.shard.ShardingSupportHash;
import pku.abe.dao.sdkapi.FingerPrintDao;
import pku.abe.data.dao.strategy.TableChannel;
import pku.abe.data.model.FingerPrintInfo;
import pku.abe.service.sdkapi.FingerPrintService;

import javax.annotation.Resource;

/**
 * Created by vontroy on 16-7-4.
 */
public class FingerPrintServiceImpl implements FingerPrintService {
    @Resource
    private FingerPrintDao fingerPrintDao;

    public FingerPrintInfo getFingerPrint(FingerPrintInfo fingerPrintInfo) {
        if (fingerPrintInfo == null) {
            ApiLogger.error("FingerPrintDaoImpl.addFingerPrint fingerPrintInfo is null, get failed");
        }

        FingerPrintInfo resultInfo = fingerPrintDao.getFingerPrint(fingerPrintInfo);

        return resultInfo;
    }

    public int setValidStatus(FingerPrintInfo fingerPrintInfo, int val) {
        if (fingerPrintInfo.getId() <= 1) {
            ApiLogger.error("Invalid FingerPrint id, update valid status failed");
        }
        return fingerPrintDao.setValidStatusById(fingerPrintInfo, val);
    }

    public int addFingerPrint(FingerPrintInfo fingerPrintInfo) {
        FingerPrintInfo newFingerPrintInfo = fingerPrintInfo;
        if (fingerPrintInfo == null) {
            ApiLogger.error("FingerPrintDaoImpl.addFingerPrint fingerPrintInfo is null, add failed");
        }
        int result = 0;

        if (fingerPrintInfo.getStage() == FingerPrintInfo.UPDATE_FINGERPRINT_INFO) {
            FingerPrintInfo existedFingerPrintInfo = getFingerPrint(fingerPrintInfo);
            if (existedFingerPrintInfo.getId() == -1) {
                result += fingerPrintDao.addFingerPrint(newFingerPrintInfo, 0);
            } else {
                if (existedFingerPrintInfo.getValid_status() != 0) {
                    result += setValidStatus(existedFingerPrintInfo, 0);
                }
            }
            newFingerPrintInfo.setIdentityId(fingerPrintInfo.getNewIdentityId());
        }
        result += fingerPrintDao.addFingerPrint(newFingerPrintInfo, 1);

        return result;
    }
}
