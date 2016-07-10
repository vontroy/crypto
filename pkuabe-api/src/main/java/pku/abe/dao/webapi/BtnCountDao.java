package pku.abe.dao.webapi;

import pku.abe.data.model.ButtonCount;

import java.util.List;

/**
 * Created by LinkedME01 on 16/4/12.
 */
public interface BtnCountDao {
    List<ButtonCount> getConsumerIncome(long appId, long consumerAppId, String startDate, String endDate);
    List<ButtonCount> getButtonCounts(long appId, String btnId, String startDate, String endDate);
}
