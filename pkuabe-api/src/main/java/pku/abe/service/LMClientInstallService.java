package pku.abe.service;

import pku.abe.model.LMClientInstallEntity;

/**
 * Created by qipo on 15/9/1.
 */
public interface LMClientInstallService {

    void addClientInstall(LMClientInstallEntity lmRegisterInstallEntity);

    LMClientInstallEntity getOneClientInstall(String hardwareId);
}

