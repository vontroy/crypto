package pku.abe.dao.webapi;

import pku.abe.data.model.ButtonInfo;

import java.util.List;

/**
 * Created by LinkedME01 on 16/4/7.
 */
public interface ButtonDao {
    int insertButton(ButtonInfo buttonInfo);
    ButtonInfo getButtonInfo(String btnId);
    List<ButtonInfo> getButtonListByBtnId(String btnId);
    List<ButtonInfo> getButtonListByAppId(long appId, boolean isAll);
    boolean updateButton(ButtonInfo buttonInfo);
    boolean deleteButton(String btnId);
}
