package pku.abe.dao.sdkapi;

import pku.abe.data.model.DeepLink;

/**
 * Created by LinkedME00 on 16/3/19.
 */
public interface DeepLinkParamDao {
    int addDeepLinkParam(DeepLink deepLink);
    String getAddDeeplinkParam(long deepLinkId);
}
