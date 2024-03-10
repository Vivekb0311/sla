package com.bootnext.platform.sla.dao.impl;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Repository;

import com.bootnext.core.generic.dao.impl.HibernateGenericDao;
import com.bootnext.core.generic.exceptions.application.BusinessException;
import com.bootnext.platform.sla.dao.ISlaHistoryDao;
import com.bootnext.platform.sla.model.template.SlaHistory;
import com.bootnext.platform.sla.utils.SlaUtils;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import lombok.extern.slf4j.Slf4j;



@Repository
@Slf4j
public class SlaHistoryDaoImpl extends HibernateGenericDao<Integer, SlaHistory> implements ISlaHistoryDao {


    public SlaHistoryDaoImpl(EntityManager entityManager) {
        super(SlaHistory.class, entityManager);
    }

    /**
     * Retrieve a list of SlaHistory objects based on the specified SLA identifier.
     *
     * @param slaIdentifier The SLA identifier to search for.
     * @return A list of SlaHistory objects that match the specified SLA identifier (could be empty).
     * @throws BusinessException If any error occurs during the retrieval process.
     */
    @Override
    public List<SlaHistory> findSlaHistoryBySlaIdentifier(String slaIdentifier) {
        Query q = getEntityManager().createNamedQuery("findSlaHistoryBySlaIdentifier").setParameter("slaIdentifier",slaIdentifier);
        return q.getResultList();
    }

    /**
     * Retrieve a single SlaHistory object based on the specified entityId.
     *
     * @param entityId The entityId of the SlaHistory object to retrieve.
     * @return The matching SlaHistory object, or null if no matching record is found.
     * @throws BusinessException If any error occurs during the retrieval process.
     */
    @Override
    public SlaHistory getHistoryByEntityId(String entityId) {
        try{
            Query q = getEntityManager().createNamedQuery("getHistoryByEntityId", SlaHistory.class).setParameter("entityId",entityId);
            return (SlaHistory)q.getSingleResult();
        }catch(Exception e) {
            log.error("Error in getHistoryByEntityId", e);
        }
        return null;
    }

    /**
     * Retrieve a list of all SlaHistory objects.
     *
     * @return A list of all SlaHistory objects (could be empty).
     * @throws BusinessException If any error occurs during the retrieval process.
     */
    @Override
    public List<SlaHistory> getAllSlaHistory() {
        log.info(SlaUtils.INSIDE_METHOD, "getAllHistory");
        Query q = getEntityManager().createNamedQuery("getAllSlaHistory");
        return q.getResultList();
    }

    /**
     * Retrieve a list of all breached SlaHistory objects.
     *
     * @return A list of all breached SlaHistory objects (could be empty).
     * @throws BusinessException If any error occurs during the retrieval process.
     */
    @Override
    public List<SlaHistory> getAllBreachedSlaHistory(boolean breachStatus) {
        try{
        Query q = getEntityManager().createNamedQuery("getAllBreachedSlaHistory").setParameter("breachStatus",breachStatus);
        return q.getResultList();
        }catch (Exception e) {
        log.error("Error Inside @class: SlaHistoryDaoImpl @Method :getAllBreachedSlaHistory() {}", e.getMessage(), e);
            throw new BusinessException(e.getMessage());
    }
    }
    /**
     * Retrieve the total count of SlaHistory objects in the database.
     *
     * @return The total count of SlaHistory objects.
     * @throws BusinessException If any error occurs during the retrieval process.
     */
    @Override
    public Long getTotalHistoryCount (){
        Query q = getEntityManager().createNamedQuery("getTotalHistoryCount");
        return (Long) q.getSingleResult();
    }

    /**
     * Retrieve the SlaHistory object that matches the specified entityId, appName, entityName, and slaIdentifier.
     *
     * @param entityId The entityId of the SlaHistory object to retrieve.
     * @param appName The application name of the SlaHistory object to retrieve.
     * @param entityName The entity name of the SlaHistory object to retrieve.
     * @param slaIdentifier The slaIdentifier of the SlaHistory object to retrieve.
     * @return The matching SlaHistory object, or null if no matching record is found.
     * @throws BusinessException If any error occurs during the retrieval process.
     */
    @Override
    public SlaHistory findSlaHistoryByEntityIdAppNameAndEntityName(String entityId, String appName, String entityName, String slaIdentifier) {
        try {
            Query q = getEntityManager().createNamedQuery("getInProgressOrOnHoldHistoryByEntityIdNameAppAndSlaId", SlaHistory.class).setParameter("entityId",entityId).setParameter("applicationName",appName).setParameter("entityName", entityName).setParameter("slaIdentifier",slaIdentifier);
            List<SlaHistory> results = q.getResultList();
            if (!results.isEmpty()) {
                return results.get(0);
            }
        }catch (Exception e) {
        log.error("Error Inside @class: SlaHistoryDaoImpl @Method :findSlaHistoryByEntityIdAppNameAndEntityName() {}", e.getMessage(), e);
            throw new BusinessException(e.getMessage());
    }
        return null;
    }


    /**
     * Retrieve the count of SlaHistory objects that have breached on the present day, based on the provided currentDate.
     *
     * @param currentDate The date for which to retrieve the count of breached SlaHistory objects.
     * @return The count of SlaHistory objects breached on the present day.
     * @throws BusinessException If any error occurs during the retrieval process.
     */
    @Override
    public long getPresentDayBreachActivities(Date currentDate) {
        long count =0;
        try{
            Query q = getEntityManager().createNamedQuery("getPresentDayBreachActivities")
                    .setParameter("currentDate", currentDate);
            count = (long) q.getSingleResult();
        } catch (Exception e) {
        log.error("Error Inside @class: SlaHistoryDaoImpl @Method :getPresentDayBreachActivities() {}", e.getMessage(), e);
            throw new BusinessException(e.getMessage());
    }
        return count;
    }




    @Override
    public List<Object[]> getSlaProximity(){
        try{
            Query query =getEntityManager().createNamedQuery("getBreachProximity");
            return query.getResultList();
        }catch (Exception e) {
        log.error("Error Inside @class: SlaHistoryDaoImpl @Method :getSlaProximity() {}", e.getMessage(), e);
            throw new BusinessException(e.getMessage());
    }
    }


    /**
     * Retrieve the top breached SlaHistory objects and return them as a JSONArray.
     *
     * @return A JSONArray containing the top breached SlaHistory objects with their names, counts, and dates.
     *         Each element in the JSONArray is a JSONObject representing a breached SlaHistory object.
     *         The JSONObject contains the following keys:
     *         - "name": The name of the breached SlaHistory object.
     *         - "count": The count of breaches for the SlaHistory object.
     *         - "date": The date of the breach for the SlaHistory object.
     * @throws BusinessException If any error occurs during the retrieval process.
     */
    @Override
    public JSONArray getTopSlaBreached() {
        try{
            Query q = getEntityManager().createNamedQuery("getTopSlaBreached");
            if(q != null){
                List<Object[]> resultSet = q.getResultList();
                JSONArray jsonArray = new JSONArray();

                for (Object[] obj : resultSet) {
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put(SlaUtils.NAME, obj[0]);
                    jsonObject.put(SlaUtils.COUNT, obj[2]);
                    jsonObject.put(SlaUtils.DATE, obj[1]);

                    log.info("jsonObject {}", jsonObject);
                    jsonArray.put(jsonObject);
                    log.info("jsonArray {}", jsonArray);
                }
                return jsonArray;
            }
        }catch (Exception e) {
        log.error("Error Inside @class: SlaHistoryDaoImpl @Method :getTopSlaBreached() {}", e.getMessage(), e);
            throw new BusinessException(e.getMessage());
    }
        return null;
    }


    /**
     * Retrieve the top triggered SlaHistory objects and return them as a JSONArray.
     *
     * @return A JSONArray containing the top triggered SlaHistory objects with their names, counts, and dates.
     *         Each element in the JSONArray is a JSONObject representing a triggered SlaHistory object.
     *         The JSONObject contains the following keys:
     *         - "name": The name of the triggered SlaHistory object.
     *         - "count": The count of triggers for the SlaHistory object.
     *         - "date": The date of the trigger for the SlaHistory object.
     * @throws BusinessException If any error occurs during the retrieval process.
     */
    @Override
    public JSONArray getTopSlaTriggered() {
        try{
            Query q = getEntityManager().createNamedQuery("getTopSlaTriggered");
            if(q != null){

                List<Object[]> resultSet = q.getResultList();
                JSONArray jsonArray = new JSONArray();

                for (Object[] obj : resultSet) {
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put(SlaUtils.NAME, obj[0]);
                    jsonObject.put(SlaUtils.COUNT, obj[2]);
                    jsonObject.put(SlaUtils.DATE, obj[1]);

                    jsonArray.put(jsonObject);
                }
                return jsonArray;
            }
        }catch (Exception e) {
        log.error("Error Inside @class: SlaHistoryDaoImpl @Method :getTopSlaTriggered() {}", e.getMessage(), e);
            throw new BusinessException(e.getMessage());
    }
        return null;
    }

    /**
     * Retrieve the top 5 SlaHistory objects entity-wise and return them as a JSONArray.
     *
     * @return A JSONArray containing the top 5 SlaHistory objects entity-wise with their statistics.
     *         Each element in the JSONArray is a JSONObject representing a SlaHistory object.
     *         The JSONObject contains the following keys:
     *         - "pie_chart": A JSONObject representing the pie chart statistics for the SlaHistory object.
     *                        The JSONObject contains the following keys:
     *                        - "count": The total count of SlaHistory objects for the entity.
     *                        - "in_progress": The count of SlaHistory objects currently in progress for the entity.
     *                        - "breached": The count of breached SlaHistory objects for the entity.
     *         - "completed": The count of completed SlaHistory objects for the entity.
     *         - "executed_on": The date when the SlaHistory object was executed on.
     *         - "min": The minimum resolved time for the entity (computed from a list of resolved times).
     *         - "max": The maximum resolved time for the entity (computed from a list of resolved times).
     *         - "avg": The average resolved time for the entity (computed as (max - min) / 2).
     * @throws BusinessException If any error occurs during the retrieval process.
     */
    @Override
    public JSONArray getTopSlaEntityWise() {
     try{
         Query q = getEntityManager().createNamedQuery("getTopSlaEntityWise");
         q.setMaxResults(5);
         if(q != null){
             List<Object[]> resultSet = q.getResultList();
             JSONArray jsonArray = new JSONArray();
             int desiredSize = 5; 
             int count = 0;
             for (Object[] obj : resultSet) {
                 if (count >= desiredSize) {
                     break; 
                 }
                 JSONObject jsonObject = new JSONObject();
                 JSONObject jsonForPie = new JSONObject();
                 Object[] list = getResolvedTime((String) obj[4]);
                 Long min = (list[1] != null) ? ((Long) list[1]) / (1000 * 60 * 60) : 0L;
                 Long max = (list[0] != null) ? ((Long) list[0])/(1000 * 60 * 60) : 0L;
                 Double avg = (list[2] != null) ? ((Double) list[2]) / (1000 * 60 * 60) : 0.0;
                 int decimalPlaces = 2;
                 double scalingFactor = Math.pow(10, decimalPlaces);
                 double roundedAvg = Math.round(avg * scalingFactor) / scalingFactor;


                 jsonForPie.put(SlaUtils.COMPLETED, obj[3]);
                 jsonForPie.put(SlaUtils.IN_PROGRESS, obj[1]);
                 jsonForPie.put(SlaUtils.BREACHED, obj[2]);
                 jsonObject.put(SlaUtils.PIE_CHART, jsonForPie);
                 jsonObject.put(SlaUtils.COUNT, obj[0]);
                 jsonObject.put(SlaUtils.EXECUTED_ON, obj[4]);
                 jsonObject.put(SlaUtils.MIN, min);
                 jsonObject.put(SlaUtils.MAX,max);
                 jsonObject.put(SlaUtils.AVG, roundedAvg);
                 jsonArray.put(jsonObject);
                 count++;
             }
             while (count < desiredSize) {
                 JSONObject emptyObject = new JSONObject();
                 JSONObject jsonPie = new JSONObject();
                 jsonPie.put(SlaUtils.COMPLETED, 0);
                 jsonPie.put(SlaUtils.IN_PROGRESS, 0);
                 jsonPie.put(SlaUtils.BREACHED, 0);
                 emptyObject.put(SlaUtils.PIE_CHART,jsonPie );
                 emptyObject.put(SlaUtils.COUNT, 0);
                 emptyObject.put(SlaUtils.EXECUTED_ON, "");
                 emptyObject.put(SlaUtils.MIN, 0);
                 emptyObject.put(SlaUtils.MAX,0);
                 emptyObject.put(SlaUtils.AVG, 0);
                 jsonArray.put(emptyObject);
                 count++;
             }
             return jsonArray;
         }

     }catch (Exception e) {
        log.error("Error Inside @class: SlaHistoryDaoImpl @Method :getTopSlaEntityWise() {}", e.getMessage(), e);
            throw new BusinessException(e.getMessage());
    }
     return null;
    }

    /**
     * Retrieve the counts of SlaHistory objects breached at different levels and return them as a JSONObject.
     *
     * @return A JSONObject containing the counts of SlaHistory objects breached at different levels.
     *         The keys of the JSONObject represent the different levels, and the values represent the respective counts.
     * @throws BusinessException If any error occurs during the retrieval process.
     */
    @Override
    public JSONObject levelWiseSlaBreached() {
        try{
            Query q = getEntityManager().createNamedQuery("levelWiseSlaBreached");
            if(q != null){
                List<Object[]> resultSet = q.getResultList();
                JSONObject jsonObject = new JSONObject();
                for (Object[] obj : resultSet) {
                    jsonObject.put(String.valueOf(obj[0]), obj[1]);
                }
                return jsonObject;
            }
        }catch (Exception e) {
        log.error("Error Inside @class: SlaHistoryDaoImpl @Method :levelWiseSlaBreached() {}", e.getMessage(), e);
            throw new BusinessException(e.getMessage());
    }
        return null;
    }

    /**
     * Retrieve the resolved time differences as an ArrayList of Long values based on the executedOn date.
     *
     * @param executedOn The date in string format representing when the tasks were executed.
     * @return An ArrayList of Long values representing the resolved time differences for the tasks executed on the given date.
     * @throws BusinessException If any error occurs during the retrieval process.
     */
     @Override
     public Object[] getResolvedTime(String executedOn) {
        Object[] result = null;
        try {
            Query q = getEntityManager().createNamedQuery("getResolvedTime")
                .setParameter("executedOn", executedOn);
            
            result = (Object[]) q.getSingleResult();
            log.info("Result: {}", Arrays.toString(result));
        } catch (Exception e) {
            log.error("Error in SlaHistoryDaoImpl getResolvedTime(): {}", e.getMessage(), e);
        }
    return result;
    }

    /**
     * Retrieve all SLA audit data as a list of Object arrays.
     *
     * @return A List of Object arrays representing the SLA audit data.
     *         Each Object array contains the audit information for a specific SLA history record.
     * @throws BusinessException If any error occurs during the retrieval process.
     */
    @Override
    public List<Object[]> getAllSlaAudit() {
    try {
        log.info("inside dao................");
        Query query = getEntityManager().createNamedQuery("SLA_HISTORY_AUD.findAll");
        log.info("query.getResultList {}", query.getResultList());
        return query.getResultList();

    } catch (Exception e) {
        log.error("Error Inside @class: SlaHistoryDaoImpl @Method :getAllSlaAudit() {}", e.getMessage(), e);
            throw new BusinessException(e.getMessage());
    }
}

    @Override
        public List<Object[]> getThirtyDaysBreaches() {
        try {
            log.info("inside dao................");
            Query query = getEntityManager().createNamedQuery("getThirtyDaysBreaches");
            log.info("query.getResultList {}", query.getResultList());
            return query.getResultList();

        } catch (Exception e) {
            log.error("Error Inside @class: SlaHistoryDaoImpl @Method :getThirtyDaysBreaches() {}", e.getMessage(), e);
                throw new BusinessException(e.getMessage());
        }
    }

@Override
public JSONArray getHistoryAuditById(long id) {
    try {
        Query query = getEntityManager().createNamedQuery("historyAudById");
        query.setParameter("id", id);
        List<Object[]> results = query.getResultList();
        log.info("results=========  {}", results);
        JSONArray jsonArray = new JSONArray();
        
        if (!results.isEmpty()) {
            for (Object[] obj : results) {
                JSONObject jsonObject = new JSONObject();
                // jsonObject.put("id", obj[0]);
                jsonObject.put("modifiedTime", obj[0]);
                jsonObject.put("state", obj[1]);
                jsonObject.put("breachStatus", obj[2]);
                // jsonObject.put("breachedAt", obj[4]);
                jsonObject.put("level", obj[3]);
                jsonObject.put("spelExpression", obj[4]);
                // jsonObject.put("revType", obj[6]);
                // jsonObject.put("breachTime", obj[7]);
                // jsonObject.put("application", obj[8]);
                // jsonObject.put("entityIdentifier", obj[9]);
                // jsonObject.put("executedOn", obj[10]);
                // jsonObject.put("inTime", obj[11]);
                // jsonObject.put("outTime", obj[12]);
                // jsonObject.put("slaIdentifier", obj[13]);
                // jsonObject.put("cancelCondition", obj[14]);
                // jsonObject.put("levelTemplate", obj[15]);
                // jsonObject.put("onHoldCondition", obj[16]);
                // jsonObject.put("operationalHours", obj[17]);
                // jsonObject.put("resetCondition", obj[18]);
                // jsonObject.put("resumeCondition", obj[19]);
                // jsonObject.put("startCondition", obj[20]);
                // jsonObject.put("stopCondition", obj[21]);
                // jsonObject.put("timeZone", obj[22]);
                jsonArray.put(jsonObject);
            }
            return jsonArray;
        } else {
            return null;
        }
    } catch (Exception e) {
        e.printStackTrace();
        return null;
    }
}

}
