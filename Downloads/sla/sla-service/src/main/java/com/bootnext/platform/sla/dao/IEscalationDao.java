package com.bootnext.platform.sla.dao;

import java.util.Date;
import java.util.List;

import org.json.JSONArray;

import com.bootnext.core.generic.dao.IGenericDao;
import com.bootnext.platform.sla.model.template.Escalation;

/**
 * The Interface IEscalationDao.
 */
public interface IEscalationDao extends IGenericDao<Integer, Escalation> {

    List<Escalation> getAllEscalations(boolean status);

    List<Escalation> getEscalationByGeneratedValue(String generatedValue);
    
    
    Escalation getEscalationByLevelAndEntityId(int level, int slaHistory);

    List<Escalation> getEscalationByEntityId(String entityId);

    List<Escalation> getEscalationBySlaHistory(int slaHistory);

    List<Object[]> getThirtyDaysEscalations();

    JSONArray getTopSlaEscalated();

    long getPresentDayEscalateActivities(Date currentDate);
    
    JSONArray getAuditByFk(long id);
}
