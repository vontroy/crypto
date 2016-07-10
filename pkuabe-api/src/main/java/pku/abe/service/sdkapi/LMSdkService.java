package pku.abe.service.sdkapi;


import pku.abe.data.model.params.CloseParams;
import pku.abe.data.model.params.InstallParams;
import pku.abe.data.model.params.OpenParams;
import pku.abe.data.model.params.PreInstallParams;
import pku.abe.data.model.params.UrlParams;
import pku.abe.data.model.params.WebCloseParams;
import pku.abe.data.model.params.WebInitParams;

/**
 * Created by LinkedME00 on 16/1/15.
 */
public interface LMSdkService {

    String webinit(WebInitParams webInitParams);

    void webClose(WebCloseParams webCloseParams);

    String install(InstallParams installParams);

    String url(UrlParams urlParams);

    String open(OpenParams openParams);

    void close(CloseParams CloseParams);

    String preInstall(PreInstallParams preInstallParams);

}
