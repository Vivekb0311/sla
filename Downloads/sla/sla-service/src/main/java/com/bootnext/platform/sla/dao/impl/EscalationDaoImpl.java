package com.bootnext.platform.sla.dao.impl;

import java.util.Date;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Repository;

import com.bootnext.core.generic.dao.impl.HibernateGenericDao;
import com.bootnext.core.generic.exceptions.application.BusinessException;
import com.bootnext.platform.sla.dao.IEscalationDao;
import com.bootnext.platform.sla.model.template.Escalation;
import com.bootnext.platform.sla.utils.SlaUtils;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;
import lombok.extern.slf4j.Slf4j;
import java.util.Collections;


/**
 * Escalation Dao Implementation class
 */

@Repository
@Slf4j
public class EscalationDaoImpl extends HibernateGenericDao<Integer, Escalation> implements IEscalationDao {

    public EscalationDaoImpl( EntityManager entityManager) {
        super(Escalation.class, entityManager);
    }

    /**
     * Retrieve a list of Escalation objects based on the specified status.
     *
     * @param status The status of the Escalation objects to retrieve (true for active, false for inactive).
     * @return A list of Escalation objects that match the specified status (could be empty).
     * @throws BusinessException If any error occurs during the retrieval process.
     */
    
    @Override
    public List<Escalation> getAllEscalations(boolean status) {
        try {
            Query q = getEntityManager().createNamedQuery("getAllEscalations").setParameter("status", status);
            return q.getResultList();
        } catch (Exception e) {
            log.error("Error Inside @class: SlaHistoryDaoImpl @Method :getAllEscalations() {}", e.getMessage(), e);
            throw new BusinessException(e.getMessage());
        }
    }

    /**
     * Retrieve a list of Escalation objects based on the provided generated value.
     *
     * @param generatedValue The generated value to search for Escalation objects.
     * @return A list of Escalation objects that match the provided generated value (could be empty).
     * @throws BusinessException If any error occurs during the retrieval process.
     */
    @Override
    public List<Escalation> getEscalationByGeneratedValue(String generatedValue) {
        List<Escalation> escalation = null;
        log.info(SlaUtils.INSIDE_METHOD, SlaUtils.GET_ESCALATION_BY_GERATED_VALUE);
        try {
            Query q = getEntityManager().createNamedQuery("getEscalationByGeneratedValue")
                    .setParameter(SlaUtils.GENERATED_VALUE, generatedValue);
            escalation = q.getResultList();
        }catch (Exception e) {
        log.error("Error Inside @class: SlaHistoryDaoImpl @Method :getEscalationByGeneratedValue() {}", e.getMessage(), e);
            throw new BusinessException(e.getMessage());
    }
        return escalation;
    }

    /**
     * Retrieve an Escalation object based on the specified level and entityId.
     *
     * @param level The level of the Escalation to retrieve.
     * @param slaHistory The sla history Id of the Escalation to retrieve.
     * @return The Escalation object that matches the specified level and entityId, or null if not found.
     * @throws BusinessException If any error occurs during the retrieval process.
     */    
     @Override
    public Escalation getEscalationByLevelAndEntityId(int level, int slaHistory){
        Escalation escalation = null;
        try{
            log.info("level {}, slaHistory {}", level, slaHistory);
            Query q = getEntityManager().createNamedQuery("getEscalationByLevelAndSlaHistory")
            .setParameter("level", level).setParameter("slaHistory", slaHistory);
            escalation = (Escalation)q.getSingleResult();
        }catch (Exception e) {
        log.error("Error Inside @class: SlaHistoryDaoImpl @Method :getEscalationByLevelAndEntityId() {}", e.getMessage(), e);
            throw new BusinessException(e.getMessage());
    }
        return escalation;
    }

    /**
     * Retrieve a list of Escalation objects based on the specified entityId.
     *
     * @param entityId The entityId of the Escalation objects to retrieve.
     * @return A list of Escalation objects that match the specified entityId (could be empty).
     * @throws BusinessException If any error occurs during the retrieval process.
     */
    @Override
    public List<Escalation> getEscalationByEntityId(String entityId){
        try{
            Query q = getEntityManager().createNamedQuery("getEscalationByEntityId")
                .setParameter("entityId", entityId);
            return q.getResultList();
        }catch (Exception e) {
        log.error("Error Inside @class: SlaHistoryDaoImpl @Method :getEscalationByEntityId() {}", e.getMessage(), e);
            throw new BusinessException(e.getMessage());
    }
    }

    public List<Escalation> getEscalationBySlaHistory(int slaHistory){
        try{
            Query q = getEntityManager().createNamedQuery("getEscalationBySlaHistory")
                .setParameter("slaHistory", slaHistory);
            return q.getResultList();
        }catch (Exception e) {
        log.error("Error Inside @class: SlaHistoryDaoImpl @Method :getEscalationBySlaHistory() {}", e.getMessage(), e);
            throw new BusinessException(e.getMessage());
    }
    }

    /**
     * Retrieve a JSONArray containing the top SLAs (Service Level Agreements) that have been escalated.
     *
     * @return A JSONArray containing the top escalated SLAs with their names, counts, and dates (could be empty).
     * @throws BusinessException If any error occurs during the retrieval process.
     */
    @Override
    public JSONArray getTopSlaEscalated() {
        try{
            Query q = getEntityManager().createNamedQuery("getTopSlaEscalated");
            if(q != null){
                log.info("LIST== {}", q.getResultList());
                List<Object[]> resultSet = q.getResultList();
                log.info("resultSet {}", resultSet);
                JSONArray jsonArray = new JSONArray();
                for (Object[] obj : resultSet) {
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("name", obj[0]);
                    jsonObject.put("count", obj[2]);
                    jsonObject.put("date", obj[1]);

                    log.info("jsonObject {}", jsonObject);
                    jsonArray.put(jsonObject);
                    log.info("jsonArray {}", jsonArray);
                }
                return jsonArray;
            }
        }catch (Exception e) {
        log.error("Error Inside @class: SlaHistoryDaoImpl @Method :getTopSlaEscalated() {}", e.getMessage(), e);
            throw new BusinessException(e.getMessage());
    }
        return null;
    }

    /**
     * Retrieve the count of escalated activities for the present day based on the provided currentDate.
     *
     * @param currentDate The date for which to retrieve the count of escalated activities.
     * @return The count of escalated activities for the present day.
     * @throws BusinessException If any error occurs during the retrieval process.
     */
    @Override
    public long getPresentDayEscalateActivities(Date currentDate) {
        long count =0;
        try{
            Query q = getEntityManager().createNamedQuery("getPresentDayEscalateActivities")
                    .setParameter("currentDate", currentDate);
            count = (long) q.getSingleResult();
        }catch (Exception e) {
        log.error("Error Inside @class: SlaHistoryDaoImpl @Method :getPresentDayEscalateActivities() {}", e.getMessage(), e);
            throw new BusinessException(e.getMessage());
    }
        return count;
    }


    @Override
        public List<Object[]> getThirtyDaysEscalations() {
        try {
            log.info("inside dao................");
            Query query = getEntityManager().createNamedQuery("getThirtyDaysEscalations");
            log.info("query.getResultList {}", query.getResultList());
            return query.getResultList();

        } catch (Exception e) {
            log.error("Error Inside @class: SlaHistoryDaoImpl @Method :getThirtyDaysEscalations() {}", e.getMessage(), e);
                throw new BusinessException(e.getMessage());
        }
    }


 @Override
public JSONArray getAuditByFk(long id) {
    try {
        Query query = getEntityManager().createNamedQuery("getAuditByFk");
        query.setParameter("id", id);
        List<Object[]> results = query.getResultList();
        log.info("results=========  {}", results);
        JSONArray jsonArray = new JSONArray();

        if (!results.isEmpty()) {
            for (Object[] obj : results) {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("createdTime", obj[0]);
                Object objValue = obj[1];
                JSONArray jsonArrayValue = new JSONArray((String) objValue);
                String email = editMailList(jsonArrayValue);
                log.info("email==== {}", email);
                jsonObject.put("whomToEscalate", email);
                jsonObject.put("level", obj[2]);
                jsonArray.put(jsonObject);
            }
            return jsonArray;
        } else {
            return null;
        }
    } catch (Exception e) {
        log.error("Error Inside @class: SlaHistoryDaoImpl @Method :getAuditByFk() {}", e.getMessage(), e);
        return null;
    }
}

private String editMailList(JSONArray jsonArray) {
    StringBuilder emailList = new StringBuilder();
    emailList.append("Mail sent to: ");
    try {
        for (int i = 0; i < jsonArray.length(); i++) {
            String email = jsonArray.getString(i);
            if (i > 0) {
                emailList.append(", ");
            }
            emailList.append(email);
        }
        return emailList.toString();
    } catch (Exception e) {
        log.error("Error Inside @class: SlaHistoryDaoImpl @Method :editMailList() {}", e.getMessage(), e);
        return null;
    }
}








}
