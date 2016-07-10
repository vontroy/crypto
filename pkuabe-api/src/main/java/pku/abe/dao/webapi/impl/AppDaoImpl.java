package pku.abe.dao.webapi.impl;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import pku.abe.commons.util.Util;
import org.apache.commons.collections.map.HashedMap;
import org.apache.commons.io.IOUtils;
import org.apache.mina.util.byteaccess.ByteArray;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.RowMapper;

import com.google.api.client.repackaged.com.google.common.base.Strings;

import pku.abe.commons.exception.LMException;
import pku.abe.commons.exception.LMExceptionFactor;
import pku.abe.commons.log.ApiLogger;
import pku.abe.commons.util.Constants;
import pku.abe.dao.BaseDao;
import pku.abe.dao.webapi.AppDao;
import pku.abe.data.dao.strategy.TableChannel;
import pku.abe.data.dao.util.DaoUtil;
import pku.abe.data.dao.util.JdbcTemplate;
import pku.abe.data.model.AppInfo;
import pku.abe.data.model.UrlTagsInfo;
import pku.abe.data.model.params.AppParams;

/**
 * Created by LinkedME01 on 16/3/18.
 */
public class AppDaoImpl extends BaseDao implements AppDao {
    private static final String ADD_APP = "ADD_APP";
    private static final String GET_APP_ID = "GET_APP_ID";
    private static final String GET_APPS_BY_USERID = "GET_APPS_BY_USERID";
    private static final String DEL_APP_BY_USERID_AND_APPID = "DEL_APP_BY_USERID_AND_APPID";
    private static final String GET_APP_BY_APPID = "GET_APP_BY_APPID";
    private static final String GET_APP_BY_NAME = "GET_APP_BY_NAME";
    private static final String VALIDATE_APP_NAME = "VALIDATE_APP_NAME";
    private static final String UPDATE_APP_BY_APPID = "UPDATE_APP_BY_APPID";
    private static final String GET_URL_TAGS_BY_APPID = "GET_URL_TAGS_BY_APPID";
    private static final String GET_URL_TAGS_BY_APPID_AND_TYPE = "GET_URL_TAGS_BY_APPID_AND_TYPE";
    private static final String SET_URL_TAGS_BY_APPID_AND_TYPE = "SET_URL_TAGS_BY_APPID_AND_TYPE";
    private static final String UPLOAD_IMG = "UPLOAD_IMG";
    private static final String GET_IMG = "GET_IMG";

    public int insertApp(AppInfo appInfo) {
        int result = 0;
        if (appInfo == null) {
            ApiLogger.error("AppDaoImpl.insertApp appInfo is null, add failed");
            return result;
        }

        try {
            TableChannel tableChannel = tableContainer.getTableChannel("appInfo", ADD_APP, 0L, 0L);
            Object[] values =
                    {appInfo.getApp_name(), appInfo.getUser_id(), appInfo.getApp_key(), appInfo.getApp_secret(), appInfo.getType()};
            result += tableChannel.getJdbcTemplate().update(tableChannel.getSql(), values);
        } catch (DataAccessException e) {
            if (DaoUtil.isDuplicateInsert(e)) {
                ApiLogger.warn(new StringBuilder(128).append("Duplicate insert app table, app_name=").append(appInfo.getApp_name()), e);
                throw new LMException(LMExceptionFactor.LM_FAILURE_DB_OP, "duplicate insert app table, app_name=" + appInfo.getApp_name());
            }
            throw new LMException(LMExceptionFactor.LM_FAILURE_DB_OP, "Failed to insert app" + appInfo.getApp_id());
        }

        AppInfo app = new AppInfo();
        if (result > 0) {
            try {
                TableChannel tableChannel = tableContainer.getTableChannel("appInfo", GET_APP_ID, 0L, 0L);
                Object[] values = {appInfo.getUser_id(), appInfo.getApp_name()};
                tableChannel.getJdbcTemplate().query(tableChannel.getSql(), values, new RowMapper() {
                    public Object mapRow(ResultSet resultSet, int i) throws SQLException {
                        app.setApp_id(resultSet.getInt("id"));
                        return null;
                    }
                });
            } catch (DataAccessException e) {
                throw new LMException(LMExceptionFactor.LM_FAILURE_DB_OP, "Failed to query app id");
            }
        }
        return (int) app.getApp_id();
    }

    public List<AppInfo> getAppsByUserId(AppParams appParams) {
        TableChannel tableChannel = tableContainer.getTableChannel("appInfo", GET_APPS_BY_USERID, appParams.user_id, appParams.user_id);
        JdbcTemplate jdbcTemplate = tableChannel.getJdbcTemplate();
        final List<AppInfo> appInfos = new ArrayList<AppInfo>();
        jdbcTemplate.query(tableChannel.getSql(), new Object[]{appParams.user_id}, new RowMapper() {
            public Object mapRow(ResultSet resultSet, int i) throws SQLException {
                AppInfo app = new AppInfo();
                app.setApp_id(resultSet.getLong("id"));
                app.setType(resultSet.getString("type"));
                app.setUser_id(appParams.user_id);
                app.setApp_name(resultSet.getString("app_name"));
                app.setApp_logo(resultSet.getString("app_logo"));
                app.setApp_key(resultSet.getString("app_key"));
                app.setApp_secret(resultSet.getString("app_secret"));
                app.setIos_uri_scheme(resultSet.getString("ios_uri_scheme"));
                app.setIos_search_option(resultSet.getString("ios_search_option"));
                app.setIos_store_url(resultSet.getString("ios_store_url"));
                app.setIos_custom_url(resultSet.getString("ios_custom_url"));
                app.setIos_bundle_id(resultSet.getString("ios_bundle_id"));
                app.setIos_app_prefix(resultSet.getString("ios_app_prefix"));
                app.setAndroid_uri_scheme(resultSet.getString("android_uri_scheme"));
                app.setAndroid_search_option(resultSet.getString("android_search_option"));
                app.setGoogle_play_url(resultSet.getString("google_play_url"));
                app.setAndroid_custom_url(resultSet.getString("android_custom_url"));
                app.setAndroid_package_name(resultSet.getString("android_package_name"));
                app.setAndroid_sha256_fingerprints(resultSet.getString("android_sha256_fingerprints"));
                app.setIos_android_flag(resultSet.getInt("ios_android_flag"));
                app.setUse_default_landing_page(resultSet.getBoolean("use_default_landing_page"));
                app.setCustom_landing_page(resultSet.getString("custom_landing_page"));

                DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                Timestamp register_time = resultSet.getTimestamp("register_time");
                app.setCreation_time(sdf.format(register_time));
                appInfos.add(app);
                return null;
            }
        });
        return appInfos;
    }

    public int delApp(AppParams appParams) {
        int result = 0;
        TableChannel tableChannel =
                tableContainer.getTableChannel("appInfo", DEL_APP_BY_USERID_AND_APPID, appParams.user_id, appParams.user_id);
        JdbcTemplate jdbcTemplate = tableChannel.getJdbcTemplate();

        try {
            result += jdbcTemplate.update(tableChannel.getSql(), new Object[]{appParams.user_id, appParams.app_id});
        } catch (DataAccessException e) {
            throw new LMException(LMExceptionFactor.LM_FAILURE_DB_OP, "Failed to delete app" + appParams.app_id);
        }
        return result;
    }

    public AppInfo getAppByName(long userId, String appName) {
        TableChannel tableChannel = tableContainer.getTableChannel("appInfo", GET_APP_BY_NAME, 0L, 0L);
        JdbcTemplate jdbcTemplate = tableChannel.getJdbcTemplate();

        AppInfo appInfo = new AppInfo();
        jdbcTemplate.query(tableChannel.getSql(), new Object[]{userId, appName}, new RowMapper() {
            public Object mapRow(ResultSet resultSet, int i) throws SQLException {
                appInfo.setApp_name(resultSet.getString("app_name"));
                return null;
            }
        });

        if (appInfo.getApp_name() == null) {
            return null;
        }
        return appInfo;
    }

    public boolean isAppNameValidate(AppParams appParams) {
        TableChannel tableChannel = tableContainer.getTableChannel("appInfo", VALIDATE_APP_NAME, 0L, 0L);
        JdbcTemplate jdbcTemplate = tableChannel.getJdbcTemplate();

        List<AppInfo> appInfos = new ArrayList<>();

        jdbcTemplate.query(tableChannel.getSql(), new Object[]{appParams.user_id, appParams.app_name}, new RowMapper() {
            public Object mapRow(ResultSet resultSet, int i) throws SQLException {
                AppInfo appInfo = new AppInfo();
                appInfo.setApp_id(resultSet.getLong("id"));
                appInfo.setApp_name(appParams.app_name);

                appInfos.add(appInfo);
                return null;
            }
        });

        if (appInfos.size() == 0) {
            return true;
        }
        if (appInfos.size() == 1 && appInfos.get(0).getApp_id() == appParams.app_id) {
            return true;
        }
        return false;
    }

    public AppInfo getAppByAppId(final long app_id) {
        TableChannel tableChannel = tableContainer.getTableChannel("appInfo", GET_APP_BY_APPID, 0L, 0L);
        JdbcTemplate jdbcTemplate = tableChannel.getJdbcTemplate();

        final List<AppInfo> appInfos = new ArrayList<AppInfo>();
        jdbcTemplate.query(tableChannel.getSql(), new Object[]{app_id}, new RowMapper() {
            public Object mapRow(ResultSet resultSet, int i) throws SQLException {
                AppInfo app = new AppInfo();
                app.setApp_id(app_id);
                app.setUser_id(resultSet.getLong("user_id"));
                app.setApp_name(resultSet.getString("app_name"));
                app.setApp_logo(resultSet.getString("app_logo"));
                app.setApp_key(resultSet.getString("app_key"));
                app.setApp_secret(resultSet.getString("app_secret"));

                app.setIos_uri_scheme(resultSet.getString("ios_uri_scheme"));
                app.setIos_not_url(resultSet.getString("ios_not_url"));
                app.setIos_store_url(resultSet.getString("ios_store_url"));
                app.setIos_custom_url(resultSet.getString("ios_custom_url"));
                app.setIos_app_prefix(resultSet.getString("ios_app_prefix"));
                app.setIos_search_option(resultSet.getString("ios_search_option"));
                app.setIos_bundle_id(resultSet.getString("ios_bundle_id"));

                app.setAndroid_uri_scheme(resultSet.getString("android_uri_scheme"));
                app.setAndroid_not_url(resultSet.getString("android_not_url"));
                app.setGoogle_play_url(resultSet.getString("google_play_url"));
                app.setAndroid_custom_url(resultSet.getString("android_custom_url"));
                app.setAndroid_search_option(resultSet.getString("android_search_option"));
                app.setAndroid_package_name(resultSet.getString("android_package_name"));
                app.setAndroid_sha256_fingerprints(resultSet.getString("android_sha256_fingerprints"));
                app.setIos_android_flag(resultSet.getInt("ios_android_flag"));
                app.setUse_default_landing_page(resultSet.getBoolean("use_default_landing_page"));
                app.setCustom_landing_page(resultSet.getString("custom_landing_page"));

                appInfos.add(app);
                return null;
            }
        });

        if (!appInfos.isEmpty())
            return appInfos.get(0);
        else
            return null;
    }

    public int updateApp(final AppParams appParams) {
        int res = 0;
        if (!isAppNameValidate(appParams))
            throw new LMException(LMExceptionFactor.LM_ILLEGAL_PARAM_VALUE, "Duplicate app name");
        else {
            TableChannel tableChannel =
                    tableContainer.getTableChannel("appInfo", UPDATE_APP_BY_APPID, appParams.user_id, appParams.user_id);
            JdbcTemplate jdbcTemplate = tableChannel.getJdbcTemplate();

            Object[] values = new Object[]{appParams.app_name, appParams.type, appParams.ios_uri_scheme, appParams.ios_not_url,
                    appParams.ios_search_option, appParams.ios_store_url, appParams.ios_custom_url, appParams.ios_bundle_id,
                    appParams.ios_app_prefix, appParams.android_uri_scheme, appParams.android_not_url, appParams.android_search_option,
                    appParams.google_play_url, appParams.android_custom_url, appParams.android_package_name,
                    appParams.android_sha256_fingerprints, appParams.ios_android_flag, appParams.use_default_landing_page,
                    appParams.custom_landing_page, appParams.app_id};

            try {
                res += jdbcTemplate.update(tableChannel.getSql(), values);
            } catch (DataAccessException e) {
                if (DaoUtil.isDuplicateInsert(e)) {
                    ApiLogger.warn(new StringBuilder(128).append("Duplicate insert app table, app_name=").append(appParams.getApp_name()),
                            e);
                    throw new LMException(LMExceptionFactor.LM_FAILURE_DB_OP,
                            "duplicate insert app table, app_name=" + appParams.getApp_name());
                }
                throw new LMException(LMExceptionFactor.LM_FAILURE_DB_OP);
            }
        }
        return res;

    }

    public List<UrlTagsInfo> getUrlTagsByAppId(AppParams appParams) {
        TableChannel tableChannel = tableContainer.getTableChannel("urlTags", GET_URL_TAGS_BY_APPID, 0L, 0L);
        JdbcTemplate jdbcTemplate = tableChannel.getJdbcTemplate();
        final List<UrlTagsInfo> urlTagsInfos = new ArrayList<UrlTagsInfo>();
        jdbcTemplate.query(tableChannel.getSql(), new Object[]{appParams.app_id}, new RowMapper() {

            public Object mapRow(ResultSet resultSet, int i) throws SQLException {
                do {
                    UrlTagsInfo urlTagsInfo = new UrlTagsInfo();
                    urlTagsInfo.setAppId(appParams.app_id);
                    urlTagsInfo.setTag_content(resultSet.getString("tag_content"));
                    urlTagsInfo.setTag_type(resultSet.getString("tag_type"));

                    urlTagsInfos.add(urlTagsInfo);
                } while (resultSet.next());
                return null;
            }
        });
        if (!urlTagsInfos.isEmpty())
            return urlTagsInfos;
        else
            return null;
    }

    public boolean configUrlTags(AppParams appParams) {

        String[] values = appParams.value;
        String type = appParams.type;

        TableChannel tableChannel = tableContainer.getTableChannel("urlTags", GET_URL_TAGS_BY_APPID_AND_TYPE, 0L, 0L);
        JdbcTemplate jdbcTemplate = tableChannel.getJdbcTemplate();

        final List<UrlTagsInfo> urlTagsInfos = new ArrayList<UrlTagsInfo>();

        jdbcTemplate.query(tableChannel.getSql(), new Object[]{appParams.app_id, appParams.type}, new RowMapper() {

            public Object mapRow(ResultSet resultSet, int i) throws SQLException {
                do {
                    UrlTagsInfo urlTagsInfo = new UrlTagsInfo();
                    urlTagsInfo.setAppId(appParams.app_id);
                    urlTagsInfo.setTag_content(resultSet.getString("tag_content"));
                    urlTagsInfo.setTag_type(appParams.type);

                    urlTagsInfos.add(urlTagsInfo);
                } while (resultSet.next());
                return null;
            }
        });

        Map<String, Long> tagMap = new HashedMap();

        for (int i = 0; i < urlTagsInfos.size(); i++) {
            tagMap.put(urlTagsInfos.get(i).getTag_content(), 1L);
        }

        int result = 0;
        int flag = 0;
        for (int i = 0; i < values.length; i++) {
            if (!tagMap.isEmpty() && tagMap.get(values[i]) != null && tagMap.get(values[i]) == 1L) {
                flag++;
                continue;
            } else {
                TableChannel tableChannel_set = tableContainer.getTableChannel("urlTags", SET_URL_TAGS_BY_APPID_AND_TYPE, 0L, 0L);
                JdbcTemplate jdbcTemplate_set = tableChannel_set.getJdbcTemplate();

                try {
                    result += jdbcTemplate_set.update(tableChannel_set.getSql(), new Object[]{appParams.app_id, values[i], type});
                } catch (DataAccessException e) {
                    if (DaoUtil.isDuplicateInsert(e)) {
                        ApiLogger.warn(new StringBuilder(128).append("Duplicate insert url_tags_info table, tag_content= ")
                                .append(appParams.getValue()), e);
                        throw new LMException(LMExceptionFactor.LM_FAILURE_DB_OP,
                                "Duplicate insert url_tags_info table, tag_content = " + appParams.getValue());
                    }
                    throw new LMException(LMExceptionFactor.LM_FAILURE_DB_OP);
                }
            }
        }
        if (result > 0) return true;
        if (result == 0 && flag == values.length) return true;
        return false;
    }

    @Override
    public int uploadImg(AppParams appParams, byte[] picDatas) {
        int res = 0;
        TableChannel tableChannel = tableContainer.getTableChannel("appInfo", UPLOAD_IMG, 0L, 0L);
        String sql =
                "insert into dashboard_0.app_logo_0 (app_id, pic_data) values(?, ?) on duplicate key update pic_data = values(pic_data) ";
        JdbcTemplate jdbcTemplate = tableChannel.getJdbcTemplate();
        try {
            res += jdbcTemplate.update(sql, new Object[]{appParams.app_id, picDatas});
        } catch (DataAccessException e) {
            throw new LMException(LMExceptionFactor.LM_FAILURE_DB_OP, "Failed to update app_logo");
        }
        return res;
    }

    @Override
    public byte[] getAppImg(int appId) {
        TableChannel tableChannel = tableContainer.getTableChannel("appInfo", UPLOAD_IMG, 0L, 0L);
        String sql = "select pic_data from dashboard_0.app_logo_0 where app_id = ?";
        JdbcTemplate jdbcTemplate = tableChannel.getJdbcTemplate();
        List<Object> picDatas = jdbcTemplate.query(sql, new Object[]{appId}, new RowMapper() {
            public Object mapRow(ResultSet resultSet, int i) throws SQLException {
                InputStream in = resultSet.getBinaryStream("pic_data");
                try {
                    return IOUtils.toByteArray(in);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }
        });
        if (picDatas == null || picDatas.size() == 0) {
            return null;
        }
        return (byte[]) picDatas.get(0);
    }

    public int deleteOldImg(AppParams appParams) {
        TableChannel tableChannel = tableContainer.getTableChannel("appInfo", GET_IMG, 0L, 0L);
        JdbcTemplate jdbcTemplate = tableChannel.getJdbcTemplate();
        int res = 0;
        final List<AppInfo> appInfos = new ArrayList<AppInfo>();
        jdbcTemplate.query(tableChannel.getSql(), new Object[]{appParams.app_id, appParams.user_id}, new RowMapper() {
            @Override
            public Object mapRow(ResultSet resultSet, int i) throws SQLException {
                AppInfo app = new AppInfo();
                app.setApp_logo(resultSet.getString("app_logo"));
                appInfos.add(app);
                return null;
            }
        });
        AppInfo appInfo = new AppInfo();
        if (!appInfos.isEmpty()) {
            appInfo = appInfos.get(0);
        }
        String path = appInfo.getApp_logo();
        if (Strings.isNullOrEmpty(path)) {
            return 0;
        }
        Pattern pattern = Pattern.compile(".*images/(.*)");
        Matcher matcher = pattern.matcher(path);
        String imagePath = null;
        while (matcher.find()) {
            imagePath = matcher.group(1);
        }
        // delete img
        File file = new File(Constants.ImgPath + imagePath);
        if (!file.exists()) return -1;
        boolean flag = file.delete();
        return flag ? 1 : -1;
    }
}
