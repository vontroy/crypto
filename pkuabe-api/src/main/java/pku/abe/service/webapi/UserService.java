package pku.abe.service.webapi;

import pku.abe.data.model.UserInfo;
import pku.abe.data.model.params.DemoRequestParams;
import pku.abe.data.model.params.UserParams;

/**
 * Created by Vontroy on 16/3/19.
 */
public interface UserService {


    UserInfo userLogin(UserParams userParams);

    boolean userRegister(UserParams userParams);

    boolean validateEmail(UserParams userParams);

    boolean userLogout(UserParams userParams);

    boolean resetUserPwd(UserParams userParams);

    boolean forgotPwd(UserParams userParams);

    boolean resetForgottenPwd(UserParams userParams);

    boolean requestDemo(DemoRequestParams demoRequestParams);
}
