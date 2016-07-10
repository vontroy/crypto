package pku.abe.service.sdkapi;

import pku.abe.data.model.AppListInfo;

import java.util.ArrayList;

/**
 * Created by Vontroy on 16/4/24.
 */
public interface AppListService {
    int addAppList(ArrayList<AppListInfo> appListInfos);
}
