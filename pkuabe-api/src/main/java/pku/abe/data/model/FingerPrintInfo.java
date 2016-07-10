package pku.abe.data.model;

/**
 * Created by vontroy on 16-7-4.
 */
public class FingerPrintInfo {
    public final static int ADD_FINGERPRINT_INFO = 0;
    public final static int UPDATE_FINGERPRINT_INFO = 1;
    public final static int NO_OPTIONS = -1;

    private int id = -1;
    private String deviceId;
    private int deviceType;
    private long identityId;
    private long newIdentityId;
    private int valid_status;
    private int stage;
    private String currentTime;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public int getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(int deviceType) {
        this.deviceType = deviceType;
    }

    public long getIdentityId() {
        return identityId;
    }

    public void setIdentityId(long identityId) {
        this.identityId = identityId;
    }

    public long getNewIdentityId() {
        return newIdentityId;
    }

    public void setNewIdentityId(long newIdentityId) {
        this.newIdentityId = newIdentityId;
    }

    public int getValid_status() {
        return valid_status;
    }

    public void setValid_status(int valid_status) {
        this.valid_status = valid_status;
    }

    public int getStage() {
        return stage;
    }

    public void setStage(int stage) {
        this.stage = stage;
    }

    public String getCurrentTime() {
        return currentTime;
    }

    public void setCurrentTime(String currentTime) {
        this.currentTime = currentTime;
    }
}
