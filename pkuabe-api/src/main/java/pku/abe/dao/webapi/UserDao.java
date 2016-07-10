package pku.abe.dao.webapi;

import pku.abe.data.model.UserInfo;
import pku.abe.data.model.params.DemoRequestParams;
import pku.abe.data.model.params.UserParams;

/**
 * Created by Vontroy on 16/3/19.
 */
public interface UserDao {

    int updateUserInfo(UserParams userParams);

    int resetUserPwd(UserParams userParams);

    int changeUserPwd(UserParams userParams);

    int setLoginInfos(UserParams userParams); //登录时更新lastLoginTime和token

    UserInfo getUserInfo(String email);

    boolean queryEmail(String email); // 验证邮箱是否存在

    boolean setRandomCode(String randomCode, String email);   //忘记密码后,重置密码前生成随机码

    int updateToken(UserParams userParams);

    String getToken(String email);

    int requestDemo(DemoRequestParams demoRequestParams);
}
