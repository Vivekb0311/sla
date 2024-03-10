package com.bootnext.platform.sla.service;

import java.util.Map;

import com.bootnext.core.generic.service.IBaseService;
import com.bootnext.platform.sla.model.template.SlaHistory;

public interface ISlaHistoryService extends IBaseService<Integer, SlaHistory> {

    Boolean triggerSLA(String entityString, String applicationName, String entityName, String entityId);

    String scheduledExecuteHistory();

    String getPresentDayActivities();

     Map<String,Map<String,Integer>>  getActivitiesForThirtyDays();

    String getTopSlaBreached();

    String getTopSlaTriggered();

    String getSlaBreachProximity();

    String getTopSlaEntityWise();

    String levelWiseSlaBreached();

    Long getTotalHistoryCount();
    
    Map<String,Map<String,Integer>> slaTriggeredByStatus();

    String getSlaHistoryAudById(long slaId);

    int auditCount(long slaId);
}
