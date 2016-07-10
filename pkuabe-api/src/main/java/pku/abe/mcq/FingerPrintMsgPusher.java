package pku.abe.mcq;

import pku.abe.commons.mcq.writer.McqBaseWriter;
import pku.abe.data.model.FingerPrintInfo;

import javax.annotation.Resource;

/**
 * Created by vontroy on 16-7-4.
 */
public class FingerPrintMsgPusher {

    @Resource
    private McqBaseWriter apiMcqWriter;

    public void updateFingerPrint(FingerPrintInfo fingerPrintInfo) {
        String fingerPrintInfoMsg = MsgUtils.updateFingerPrintMsgJson(fingerPrintInfo);
        apiMcqWriter.writeMsg(fingerPrintInfoMsg);
    }
}
