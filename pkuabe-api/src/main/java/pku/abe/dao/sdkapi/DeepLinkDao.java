package pku.abe.dao.sdkapi;

import pku.abe.data.model.DeepLink;
import pku.abe.data.model.params.DashboardUrlParams;

import java.util.List;

/**
 * Created by LinkedME01 on 16/3/8.
 */
public interface DeepLinkDao {
    int addDeepLink(DeepLink deepLink);

    DeepLink getDeepLinkInfo(long deepLinkId, long appId);

    List<DeepLink> getDeepLinks(long appid, String start_date, String end_date, String feature, String campaign, String stage,
            String channel, String tag, String source, boolean unique);

    boolean deleteDeepLink(long deepLinkId, long appId);

    DeepLink getUrlInfo( long deepLinkId, long appid);

    boolean updateUrlInfo(DashboardUrlParams dashboardUrlParams);

}
