package pku.abe.dao.webapi;

import pku.abe.data.model.AppInfo;
import pku.abe.data.model.UrlTagsInfo;
import pku.abe.data.model.params.AppParams;

import java.util.List;

/**
 * Created by LinkedME01 on 16/3/18.
 */
public interface AppDao {
    int insertApp(AppInfo appInfo);

    List<AppInfo> getAppsByUserId(AppParams appParams);

    int delApp(AppParams appParams);

    AppInfo getAppByName(long userId, String appName);

    AppInfo getAppByAppId(long appId);

    int updateApp(final AppParams appParams);

    List<UrlTagsInfo> getUrlTagsByAppId(AppParams appParams);

    boolean configUrlTags(AppParams appParams);

    int uploadImg(AppParams appParams, byte[] picDatas);

    byte[] getAppImg(int appId);
}
