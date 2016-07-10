package pku.abe.mcq;

import javax.annotation.Resource;

import pku.abe.commons.json.JsonBuilder;
import pku.abe.commons.mcq.forward.MsgForwarder;
import pku.abe.commons.mcq.writer.McqBaseWriter;
import pku.abe.commons.redis.clients.jedis.Client;
import pku.abe.data.model.ClientInfo;

/**
 * Created by LinkedME01 on 16/3/8.
 */
public class ClientMsgPusher {

    @Resource
    private McqBaseWriter apiMcqWriter;

    public void addClient(ClientInfo clientInfo, long deepLinkId) {
        JsonBuilder clientMsg = new JsonBuilder();
        clientMsg.append("type", 21);
        JsonBuilder infoJson = MsgUtils.toClientMsgJson(clientInfo);
        infoJson.append("deeplink_id", deepLinkId);
        clientMsg.append("info", infoJson.flip());
        apiMcqWriter.writeMsg(clientMsg.flip().toString());
    }

}
