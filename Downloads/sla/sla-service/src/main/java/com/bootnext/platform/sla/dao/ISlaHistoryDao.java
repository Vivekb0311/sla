package com.bootnext.platform.sla.dao;

import java.util.Date;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import com.bootnext.core.generic.dao.IGenericDao;
import com.bootnext.platform.sla.model.template.SlaHistory;

/**
 * The Interface ISlaHistoryDao.
 */
public interface ISlaHistoryDao extends IGenericDao<Integer, SlaHistory> {

    List<SlaHistory> findSlaHistoryBySlaIdentifier(String slaIdentifier);

    SlaHistory getHistoryByEntityId(String entityId);

    List<SlaHistory>  getAllSlaHistory();

    List<SlaHistory>  getAllBreachedSlaHistory(boolean breachStatus);

    SlaHistory findSlaHistoryByEntityIdAppNameAndEntityName(String entityId, String appName, String entityName, String slaIdentifier);

    long getPresentDayBreachActivities(Date currentDate);

    JSONArray getTopSlaBreached();

    JSONArray getTopSlaTriggered();

    JSONArray getTopSlaEntityWise();

    JSONObject levelWiseSlaBreached ();

   Object[] getResolvedTime(String executedOn);

    Long getTotalHistoryCount ();


    List<Object[]>  getAllSlaAudit();

    List<Object[]> getThirtyDaysBreaches();

    List<Object[]> getSlaProximity();
    
    JSONArray getHistoryAuditById(long id);
}
