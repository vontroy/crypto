package pku.abe.service.sdkapi;

import pku.abe.data.model.ClientInfo;

/**
 * Created by LinkedME01 on 16/3/20.
 */
public interface ClientService {
    int addClient(ClientInfo clientInfo, long deepLinkId);
}
