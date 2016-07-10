package pku.abe.mcq.processor;

import pku.abe.commons.log.ApiLogger;
import pku.abe.commons.mcq.reader.McqProcessor;
import pku.abe.commons.switcher.Switcher;
import pku.abe.commons.switcher.SwitcherManagerFactoryLoader;
import pku.abe.commons.util.ApiUtil;
import pku.abe.commons.util.UseTimeStasticsMonitor;
import pku.abe.data.model.ClientInfo;
import pku.abe.data.model.DeepLink;
import pku.abe.data.model.DeepLinkDateCount;
import pku.abe.data.model.FingerPrintInfo;
import pku.abe.mcq.MsgUtils;
import pku.abe.service.DeepLinkService;
import pku.abe.service.sdkapi.ClientService;
import pku.abe.service.sdkapi.FingerPrintService;
import net.sf.json.JSONObject;

import javax.annotation.Resource;
import java.util.LinkedList;

/**
 * process packet from mcq
 */
public class MsgMcqProcessor extends McqProcessor {
    public static final UseTimeStasticsMonitor McqProcMonitor = new UseTimeStasticsMonitor("McqProcMonitor"); // mcq处理统计
    private static Switcher deepLinkCountSwitcher = SwitcherManagerFactoryLoader.getSwitcherManagerFactory().getSwitcherManager()
            .registerSwitcher("linkedme.deeplink.count.enable", true);

    /**
     * 是否更新db
     */
    private boolean updateDb = true;

    /**
     * 是否更新mc
     */
    private boolean updateMc = true;

    private static final boolean debug = false;

    @Resource
    private DeepLinkService deepLinkService;

    @Resource
    private ClientService clientService;

    @Resource
    private FingerPrintService fingerPrintService;


    // 处理收到的消息
    @Override
    protected void handleMsq(final String msg) {
        String logMsg = new StringBuilder(256).append("key:").append(getReadKey()).append(" updateDb:").append(updateDb)
                .append(" updateMc:").append(updateMc).append(" MsgProcessor recv:").append(msg).toString();
        ApiLogger.info(logMsg);
        LinkedList<Long> stamps = McqProcMonitor.start(null, debug);
        int result = ApiUtil.MQ_PROCESS_SUCCESS;
        if (updateMc || updateDb) {
            try {
                result = process(msg);
            } catch (Exception e) {
                result = ApiUtil.MQ_PROCESS_RETRY;
                ApiLogger.warn(new StringBuilder(256).append("MblogMsgProcessor process error! msg:").append(msg), e);
            }
        }
        // 将处理不完整的消息写入失败队列
        if (result == ApiUtil.MQ_PROCESS_RETRY) {
            saveErrorMsg(msg);
        } else if (result == ApiUtil.MQ_PROCESS_DEGRADATION) {
            saveDegradationMsg(msg);
        } else if (result == ApiUtil.MQ_PROCESS_ABORT) {
            ApiLogger.warn(new StringBuilder(256).append("[MblogMsgProcessor] Abort msg:").append(msg));
        }
        // 结束统计
        McqProcMonitor.end(stamps, false, debug);
    }

    public int process(String mcqMsg) throws Exception {
        int result = ApiUtil.MQ_PROCESS_RETRY;
        // 根据不同的消息作不同的处理
        JSONObject msgJson = JSONObject.fromObject(mcqMsg);
        int type = msgJson.getInt("type");
        JSONObject info = msgJson.getJSONObject("info");
        if (MsgUtils.isDeeplinkMsgType(type)) {
            // deepLink消息
            result = processDeepLinkMsg(type, info);
        } else if (MsgUtils.isClientMsgType(type)) {
            // client消息(安装app)
            result = processClientMsg(type, info);
        } else if (MsgUtils.isCountType(type)) {
            // 短链计数
            if (deepLinkCountSwitcher.isOpen()) {
                result = processCountMsg(type, info);
            } else {
                result = ApiUtil.MQ_PROCESS_DEGRADATION;
            }
        } else if (MsgUtils.isFingerPrintType(type)) {
            result = processFingerPrintMsg(type, info);
        }
        return result;
    }

    private int processDeepLinkMsg(int type, JSONObject info) {
        int result = ApiUtil.MQ_PROCESS_ABORT; // 默认为丢弃
        DeepLink deepLink = MsgUtils.toDeepLinkObj(info);
        if (type == 11) {
            // add deepLink
            result = addDeepLink(deepLink);
        } else if (type == 12) {
            // delete deepLink
        } else if (type == 13) {
            // update deepLink
        }
        return result;
    }

    private int processClientMsg(int type, JSONObject info) {
        int result = ApiUtil.MQ_PROCESS_ABORT;
        ClientInfo clientInfo = MsgUtils.toClientInfoObj(info);
        long deepLinkId = info.getLong("deeplink_id");
        if (type == 21) {
            result = addClient(clientInfo, deepLinkId);
        } else if (type == 22) {

        } else if (type == 23) {

        }
        return result;
    }

    private int processCountMsg(int type, JSONObject info) {
        // TODO 可以改成批量插入計數
        int result = ApiUtil.MQ_PROCESS_ABORT;

        DeepLinkDateCount deepLinkDateCount = new DeepLinkDateCount();
        deepLinkDateCount.setDeeplinkId(info.getLong("deeplink_id"));
        deepLinkDateCount.setAppId(info.getInt("app_id"));
        deepLinkDateCount.setDate(info.getString("date"));

        String countType = info.getString("count_type");
        if (type == 31) {
            result = addDeepLinkCount(deepLinkDateCount, countType);
        }
        return result;
    }

    private int processFingerPrintMsg(int type, JSONObject info) {
        int result = ApiUtil.MQ_PROCESS_ABORT;
        FingerPrintInfo fingerPrintInfo = new FingerPrintInfo();
        int stage = info.getInt("stage");
        if (type == 41) {
            fingerPrintInfo.setDeviceId(info.getString("device_id"));
            fingerPrintInfo.setDeviceType(info.getInt("device_type"));
            fingerPrintInfo.setIdentityId(info.getLong("identity_id"));
            fingerPrintInfo.setCurrentTime(info.getString("current_time"));
            fingerPrintInfo.setStage(stage);
            if (stage == 1) {
                fingerPrintInfo.setNewIdentityId(info.getLong("new_identity_id"));
            }
            result = updateFingerPrint(fingerPrintInfo);
        }

        return result;
    }

    private int addDeepLink(DeepLink deepLink) {
        int result = 0;
        if (updateDb) {
            result = deepLinkService.addDeepLink(deepLink);
        }

        if (updateMc) {
            deepLinkService.addDeepLinkToCache(deepLink);
        }
        return result;
    }

    private int addDeepLinkCount(DeepLinkDateCount deepLinkDateCount, String countType) {
        return deepLinkService.addDeepLinkCount(deepLinkDateCount, countType);
    }

    private int addClient(ClientInfo clientInfo, long deepLinkId) {
        int result = 0;
        if (updateDb) {
            result = clientService.addClient(clientInfo, deepLinkId);
        }
        if (updateMc) {

        }
        return result;
    }

    private int updateFingerPrint(FingerPrintInfo fingerPrintInfo) {
        int result = 0;
        if (updateDb) {
            if (fingerPrintInfo.getStage() != FingerPrintInfo.NO_OPTIONS) {
                result += fingerPrintService.addFingerPrint(fingerPrintInfo);
            }
        }

        if (updateMc) {

        }
        return result;
    }

    public void saveErrorMsg(String msg) {
        ApiLogger.info("Retry failed msg:" + msg);
    }

    public void saveDegradationMsg(String msg) {
        ApiLogger.info("DegradationMsg:" + msg);
    }

    @Override
    protected String getStatMQReadFlag() {
        return "all_mq_read_MSG_PROCESS";
    }

    @Override
    protected String getStatMQReadStatFlag() {
        return "all_mq_read_stat_MSG_PROCESS";
    }

    @Override
    protected void startExtWork() {
        // 调整为需要时加载的方式
    }

    public boolean isUpdateDb() {
        return updateDb;
    }

    public void setUpdateDb(boolean updateDb) {
        this.updateDb = updateDb;
    }

    public boolean isUpdateMc() {
        return updateMc;
    }

    public void setUpdateMc(boolean updateMc) {
        this.updateMc = updateMc;
    }

}
