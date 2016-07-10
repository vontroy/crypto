package pku.abe.service.webapi.impl;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.annotation.Resource;

import pku.abe.commons.util.Constants;
import pku.abe.data.model.params.DashboardUrlParams;
import com.google.common.base.Strings;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.ArrayUtils;

import com.esotericsoftware.kryo.KryoException;
import com.google.gson.Gson;

import pku.abe.commons.exception.LMException;
import pku.abe.commons.exception.LMExceptionFactor;
import pku.abe.commons.log.ApiLogger;
import pku.abe.commons.memcache.MemCacheTemplate;
import pku.abe.commons.redis.JedisPort;
import pku.abe.commons.serialization.KryoSerializationUtil;
import pku.abe.commons.shard.ShardingSupportHash;
import pku.abe.commons.util.Base62;
import pku.abe.commons.util.MD5Utils;
import pku.abe.commons.uuid.UuidCreator;
import pku.abe.dao.webapi.AppDao;
import pku.abe.data.model.AppInfo;
import pku.abe.data.model.UrlTagsInfo;
import pku.abe.data.model.params.AppParams;
import pku.abe.service.webapi.AppService;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 * Created by LinkedME01 on 16/3/18.
 */
public class AppServiceImpl implements AppService {
    @Resource
    UuidCreator uuidCreator;

    @Resource
    private AppDao appDao;

    @Resource
    private AppDao urlTagDao;

    @Resource
    private MemCacheTemplate<byte[]> appInfoMemCache;

    @Resource
    private ShardingSupportHash<JedisPort> linkedmeKeyShardingSupport;

    private void updateAppleAssociationFile(String appIdentifier, String appID) {
        BufferedReader br = null;
        String fileName = "/data1/tomcat8080/webapps/ROOT/apple-app-site-association";
        JSONObject json = new JSONObject();
        try {
            br = new BufferedReader(new FileReader(fileName));
            String temp;
            while ((temp = br.readLine()) != null) {
                json = JSONObject.fromObject(temp);
                JSONObject appLinkJson = json.getJSONObject("applinks");
                JSONArray details = appLinkJson.getJSONArray("details");
                String pathsItem = "/" + appIdentifier + "/*";

                boolean hasRecord = false;
                for (int i = 0; i < details.size(); i++) {
                    JSONObject appJson = details.getJSONObject(i);
                    String pathsStr = appJson.getJSONArray("paths").get(0).toString();
                    if (pathsStr.equals(pathsItem)) {
                        hasRecord = true;
                        json.getJSONObject("applinks").getJSONArray("details").getJSONObject(i).put("appID", appID);
                        break;
                    }
                }
                if (!hasRecord) {
                    JSONObject addJson = new JSONObject();
                    addJson.put("appID", appID);
                    JSONArray pathsJson = new JSONArray();
                    pathsJson.add(pathsItem);
                    addJson.put("paths", pathsJson);
                    details.add(addJson);
                }
            }
        } catch (FileNotFoundException e) {
            ApiLogger.error(String.format("AppServiceImpl.updateAppleAssociationFile error, file: %s is not found", fileName), e);
        } catch (IOException e) {
            ApiLogger.error("readline failed");
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    ApiLogger.error("close file failed");
                }
            }
        }

        writeFile(fileName, json.toString());
    }

    public long createApp(AppParams appParams) {
        AppInfo appInfo = new AppInfo();
        Random random = new Random(appParams.user_id);
        String linkedmeKey = MD5Utils.md5(appParams.app_name + "live" + appParams.user_id + random.nextInt());
        String secret = MD5Utils.md5(appParams.user_id + "live" + appParams.app_name + random.nextInt());

        appInfo.setApp_key(linkedmeKey);
        appInfo.setApp_secret(secret);
        appInfo.setType("live");
        appInfo.setUser_id(appParams.user_id);
        appInfo.setApp_name(appParams.app_name);

        // appName不能重复
        AppInfo app = appDao.getAppByName(appParams.user_id, appParams.app_name);
        if (app != null && app.getApp_name() != null) {
            throw new LMException(LMExceptionFactor.LM_ILLEGAL_PARAM_VALUE, "app_name already exists:" + appParams.app_name);
        }
        long appId = appDao.insertApp(appInfo);
        if (appId > 0) {
            JedisPort linkedmeKeyClient = linkedmeKeyShardingSupport.getClient(linkedmeKey);
            linkedmeKeyClient.hset(linkedmeKey, "appid", appId);
            linkedmeKeyClient.hset(linkedmeKey, "secret", secret);
            return appId;
        }
        throw new LMException(LMExceptionFactor.LM_SYS_ERROR, "Create appInfo failed");
    }

    @Override
    public boolean setAppInfoToCache(AppInfo appInfo) {
        Gson gson = new Gson();
        String gsonStr = gson.toJson(appInfo);

        byte[] b = KryoSerializationUtil.serializeObj(gsonStr);
        boolean res = appInfoMemCache.set(String.valueOf(appInfo.getApp_id()), b);
        return res;
    }

    public List<AppInfo> getAppsByUserId(AppParams appParams) {
        List<AppInfo> appList = appDao.getAppsByUserId(appParams);
        if (CollectionUtils.isEmpty(appList)) {
            return new ArrayList<AppInfo>(0);
        }
        return appList;
    }

    public int deleteApp(AppParams appParams) {
        int result = appDao.delApp(appParams);

        String appIdentifier = Base62.encode(appParams.app_id);
        // 删除apple-app-site-association中对应信息(ios universe link)
        if (!Strings.isNullOrEmpty(String.valueOf(appParams.app_id))) {
            JedisPort client = linkedmeKeyShardingSupport.getClient(0);
            client.hdel("applinks.ios", appIdentifier);
        }

        // 删除assetlinks.json文件中对应信息(Android app links)
        if (!Strings.isNullOrEmpty(String.valueOf(appParams.app_id))) {
            // updateAppLinksFile(Long.toString(appParams.app_id),
            // appParams.android_package_name, appParams.android_sha256_fingerprints);
            JedisPort client = linkedmeKeyShardingSupport.getClient(0);
            client.hdel("applinks.adr", appIdentifier);
        }

        if (result > 0) {
            // 删除mc里的app信息
            appInfoMemCache.delete(String.valueOf(appParams.app_id));
        }
        return result;
    }

    public AppInfo getAppById(long appId) {
        AppInfo appInfo;
        Gson gson = new Gson();
        // 先从mc取,没有命中再从DB取
        byte[] appInfoByteArr = appInfoMemCache.get(String.valueOf(appId));
        if (appInfoByteArr != null && appInfoByteArr.length > 0) {

            try {
                String appInfoJson = KryoSerializationUtil.deserializeObj(appInfoByteArr, String.class);
                appInfo = gson.fromJson(appInfoJson, AppInfo.class);
            } catch (KryoException e) {
                appInfo = null;
                appInfoMemCache.delete(String.valueOf(appId));
            }
            if (appInfo != null) {
                return appInfo;
            }
        }

        appInfo = appDao.getAppByAppId(appId);
        if (appInfo != null && appInfo.getApp_id() > 0) {
            setAppInfoToCache(appInfo);
            return appInfo;
        }
        return null;
    }

    public int updateApp(AppParams appParams) {
        // TODO 判断更新的app_name不能重复
        // 更新apple-app-site-association(ios universe link)
        String appIdentifier = Base62.encode(appParams.app_id);
        if (!Strings.isNullOrEmpty(appParams.ios_app_prefix) && !Strings.isNullOrEmpty(appParams.ios_bundle_id)) {
            String appID = appParams.ios_app_prefix + "." + appParams.ios_bundle_id;
            // updateAppleAssociationFile(appIdentifier, appID);

            JedisPort client = linkedmeKeyShardingSupport.getClient(0);

            Map<String, String> appDetails = client.hgetAll("applinks.ios");
            if (appDetails == null || appDetails.size() == 0) {
                client.hset("applinks.ios", appIdentifier, appID);
            } else {
                String judgeVal = appDetails.get(appIdentifier);
                if (appDetails.containsValue(appID) && (judgeVal == null || !judgeVal.equals(appID))) {
                    throw new LMException(LMExceptionFactor.LM_ILLEGAL_PARAM_VALUE, "Duplicate prefix and bundle_id!");
                } else {
                    client.hset("applinks.ios", appIdentifier, appID);
                }
            }
        }

        // 更新assetlinks.json文件(Android app links)
        if (!Strings.isNullOrEmpty(appParams.android_package_name) && !Strings.isNullOrEmpty(appParams.android_sha256_fingerprints)) {
            // updateAppLinksFile(Long.toString(appParams.app_id),
            // appParams.android_package_name, appParams.android_sha256_fingerprints);

            String target = appParams.android_package_name + "|" + appParams.android_sha256_fingerprints;

            JedisPort client = linkedmeKeyShardingSupport.getClient(0);

            Map<String, String> appDetails = client.hgetAll("applinks.adr");

            if (appDetails == null || appDetails.size() == 0) {
                client.hset("applinks.adr", appIdentifier, target);
            } else {
                String judgeVal = appDetails.get(appIdentifier);
                if (appDetails.containsValue(target) && (judgeVal == null || !judgeVal.equals(target))) {
                    throw new LMException(LMExceptionFactor.LM_ILLEGAL_PARAM_VALUE, "Duplicate package_name and sha256_fingerprints!");
                } else
                    client.hset("applinks.adr", appIdentifier, target);
            }
        }

        int result = appDao.updateApp(appParams);
        if (result > 0) {
            AppInfo appInfo = new AppInfo();
            appInfo.setApp_id(appParams.app_id);
            appInfo.setUser_id(appParams.user_id);
            appInfo.setApp_name(appParams.app_name);
            appInfo.setApp_key(appParams.lkme_key);
            appInfo.setApp_secret(appParams.lkme_secret);

            appInfo.setIos_android_flag(appParams.ios_android_flag);
            appInfo.setIos_not_url(appParams.ios_not_url);
            appInfo.setIos_uri_scheme(appParams.ios_uri_scheme);
            appInfo.setIos_search_option(appParams.ios_search_option);
            appInfo.setIos_store_url(appParams.ios_store_url);
            appInfo.setIos_custom_url(appParams.ios_custom_url);
            appInfo.setIos_bundle_id(appParams.ios_bundle_id);
            appInfo.setIos_app_prefix(appParams.ios_app_prefix);

            appInfo.setAndroid_not_url(appParams.android_not_url);
            appInfo.setAndroid_uri_scheme(appParams.android_uri_scheme);
            appInfo.setAndroid_search_option(appParams.android_search_option);
            appInfo.setGoogle_play_url(appParams.google_play_url);
            appInfo.setAndroid_custom_url(appParams.android_custom_url);
            appInfo.setAndroid_package_name(appParams.android_package_name);
            appInfo.setAndroid_sha256_fingerprints(appParams.android_sha256_fingerprints);

            appInfo.setUse_default_landing_page(appParams.use_default_landing_page);
            appInfo.setCustom_landing_page(appParams.custom_landing_page);
            appInfo.setApp_logo(appParams.app_logo);

            // 向mc中写入最新app信息
            setAppInfoToCache(appInfo);
        }
        return result;
    }

    public List<UrlTagsInfo> getUrlTags(AppParams appParams) {
        return urlTagDao.getUrlTagsByAppId(appParams);
    }

    public boolean configUrlTags(AppParams appParams) {
        return urlTagDao.configUrlTags(appParams);
    }

    public void addUrlTags(DashboardUrlParams urlParams) {
        AppParams appParams = new AppParams();
        appParams.app_id = urlParams.app_id;
        if (ArrayUtils.isNotEmpty(urlParams.feature)) {
            appParams.value = urlParams.feature;
            appParams.type = "feature";
            configUrlTags(appParams);
        }

        if (ArrayUtils.isNotEmpty(urlParams.campaign)) {
            appParams.value = urlParams.campaign;
            appParams.type = "campaign";
            configUrlTags(appParams);
        }

        if (ArrayUtils.isNotEmpty(urlParams.stage)) {
            appParams.value = urlParams.stage;
            appParams.type = "stage";
            configUrlTags(appParams);
        }

        if (ArrayUtils.isNotEmpty(urlParams.channel)) {
            appParams.value = urlParams.channel;
            appParams.type = "channel";
            configUrlTags(appParams);
        }

        if (ArrayUtils.isNotEmpty(urlParams.tags)) {
            appParams.value = urlParams.tags;
            appParams.type = "tag";
            configUrlTags(appParams);
        }
    }

    @Override
    public String uploadImg(AppParams appParams) {
        String imageName = appParams.getApp_id() + Constants.APP_LOGO_IMG_TYPE;
        byte[] bytes = KryoSerializationUtil.serializeObj(appParams.img_data.substring(22));
        appInfoMemCache.set(imageName, bytes);
        int result = appDao.uploadImg(appParams, bytes);
        if (result > 0) {
            return imageName;
        }
        return "";
    }

    @Override
    public byte[] getAppImg(int appId, String type) {
        byte[] picBytes = appInfoMemCache.get(appId + "." + type);
        if (picBytes == null || picBytes.length == 0) {
            picBytes = appDao.getAppImg(appId); // 从数据库里取
        }
        if (picBytes == null || picBytes.length == 0) {
            // 该App没有上传logo,则取默认图片
            picBytes = appInfoMemCache.get("default." + type);
            if (picBytes == null || picBytes.length == 0) {
                picBytes = appDao.getAppImg(1); // 默认图片的id为1
                if (picBytes != null && picBytes.length > 0) {
                    appInfoMemCache.set("default." + type, picBytes);
                }
            }
        } else {
            appInfoMemCache.set(appId + "." + type, picBytes);
        }
        String picStr = KryoSerializationUtil.deserializeObj(picBytes, String.class);
        Base64 base64 = new Base64();
        return base64.decode(picStr);
    }

    private void updateAppLinksFile(String appID, String packageName, String sha256CertFingerprints) {
        BufferedReader br = null;
        String fileName = "/data1/tomcat8080/webapps/ROOT/webapp/.well-known/assetlinks.json";
        JSONArray json = new JSONArray();
        try {
            br = new BufferedReader(new FileReader(fileName));
            String temp;
            while ((temp = br.readLine()) != null) {

                json = JSONArray.fromObject(temp);
                boolean hasRecord = false;
                for (int i = 0; i < json.size(); i++) {
                    JSONObject appItem = json.getJSONObject(i);
                    String appIdJson = appItem.getString("appID");
                    if (appIdJson.equals(appID)) {
                        hasRecord = true;
                        JSONArray sha256Json = new JSONArray();
                        sha256Json.add(sha256CertFingerprints);
                        json.getJSONObject(i).getJSONObject("target").put("package_name", packageName);
                        json.getJSONObject(i).getJSONObject("target").put("sha256_cert_fingerprints", sha256Json);
                        break;
                    }
                }

                if (!hasRecord) {
                    JSONArray relation = new JSONArray();
                    relation.add("delegate_permission/common.handle_all_urls");
                    JSONObject target = new JSONObject();
                    JSONArray sha256_cer_fingerprints = new JSONArray();
                    sha256_cer_fingerprints.add(sha256CertFingerprints);
                    target.put("namespace", "android_app");
                    target.put("package_name", packageName);
                    target.put("sha256_cert_fingerprints", sha256_cer_fingerprints);

                    JSONObject appJson = new JSONObject();
                    appJson.put("appID", appID);
                    appJson.put("relation", relation);
                    appJson.put("target", target);

                    json.add(appJson);

                }
            }
        } catch (FileNotFoundException e) {
            ApiLogger.error(String.format("AppServiceImpl.updateAppleAssociationFile error, file: %s is not found", fileName), e);
        } catch (IOException e) {
            ApiLogger.error("readline failed");
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    ApiLogger.error("close file failed");
                }
            }
        }
        writeFile(fileName, json.toString());
    }

    private void writeFile(String fileName, String fileContent) {
        BufferedWriter bw = null;
        try {
            bw = new BufferedWriter(new FileWriter(fileName));
            bw.write(fileContent);
            bw.flush();
        } catch (FileNotFoundException e) {
            ApiLogger.error(String.format("AppServiceImpl.updateAppleAssociationFile error, file: %s is not found", fileName), e);
        } catch (IOException e) {
            ApiLogger.error("write file failed");
        } finally {
            if (bw != null) {
                try {
                    bw.close();
                } catch (IOException e) {
                    ApiLogger.error("close file failed");
                }
            }
        }
    }

}
