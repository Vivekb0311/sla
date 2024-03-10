package com.bootnext.platform.sla.rest.impl;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.bootnext.core.generic.exceptions.application.BusinessException;
import com.bootnext.platform.sla.model.template.SlaHistory;
import com.bootnext.platform.sla.rest.ISlaHistoryRest;
import com.bootnext.platform.sla.service.ISlaHistoryService;

import lombok.extern.slf4j.Slf4j;

/**
 * 
 * SlaHistoryRestImpl
 * 
 * This class is a Spring REST Controller that handles HTTP requests related to SLA history.
 * It implements the ISlaHistoryRest interface and defines several methods for handling different
 * API endpoints related to SLA history. The class is also annotated with @RequestMapping to specify
 * the base URL path for the API and the media type of the response.
 */

@RestController
@RequestMapping(produces = MediaType.APPLICATION_JSON_VALUE, path = "/SlaHistory")
@Primary
@Slf4j
public class SlaHistoryRestImpl implements ISlaHistoryRest {

    @Autowired
    private ISlaHistoryService slaHistoryService;

    /**
     * Trigger an SLA for a specific entity.
     *
     * This method is responsible for triggering an SLA for a specific entity based on the provided 
     * parameters. It calls the slaHistoryService to perform the actual triggering process. 
     * The SLA is triggered for the given entity, identified by its entityString, applicationName, 
     * entityName, and entityId. If the SLA trigger is successful, the method returns true; otherwise, 
     * returns false.
     *
     * @param entityString The entityString representing the specific entity.
     * @param applicationName The name of the application associated with the entity.
     * @param entityName The name of the entity.
     * @param entityId The unique identifier of the entity.
     * @return true if the SLA trigger is successful, false otherwise.
     * @throws BusinessException If any error occurs during the SLA triggering process.
     */
    @Override
    public Boolean triggerSla(String entityString, String applicationName, String entityName, String entityId) throws BusinessException {
        try{
            return slaHistoryService.triggerSLA( entityString ,applicationName, entityName, entityId);
        }catch (Exception e) {
            log.error("Error Inside @class: SlaHistoryRestImpl @Method :triggerSla() {}", e.getMessage(), e);
            throw new BusinessException(e.getMessage());
        }
    }

    /**
     * Execute scheduled SLA history tasks.
     *
     * This method is responsible for executing scheduled SLA history tasks. 
     * It calls the 'slaHistoryService.scheduledExecuteHistory' method to perform the scheduled 
     * execution of SLA history tasks. If the execution is successful, the method returns "Success"; 
     * otherwise, it throws a 'BusinessException' with an error message.
     *
     * @return "Success" if the scheduled execution of SLA history tasks is successful.
     * @throws BusinessException If any error occurs during the scheduled execution of SLA history tasks.
     */
    @Override
    public String scheduledExecuteHistory() {
        try{
            log.info("scheduledExecuteHistory");
            String str =  "Success";
            slaHistoryService.scheduledExecuteHistory();
            return  str;
        }catch (Exception e) {
            log.error("Error Inside @class: SlaHistoryRestImpl @Method :scheduledExecuteHistory() {}", e.getMessage(), e);
            throw new BusinessException(e.getMessage());
        }
    }

    /**
     * Get present-day activities related to SLA history.
     *
     * This method retrieves the present-day activities related to SLA history by calling the 
     * 'slaHistoryService.getPresentDayActivities' method. If the retrieval is successful, 
     * it returns the present-day activities as a String. Otherwise, it throws a 'BusinessException' 
     * with an error message.
     *
     * @return The present-day activities related to SLA history as a String.
     * @throws BusinessException If any error occurs during the retrieval of present-day activities.
     */
    @Override
    public String getPresentDayActivities() {
        try {
            return slaHistoryService.getPresentDayActivities();
        } catch (Exception e) {
            log.error("Error Inside @class: SlaHistoryRestImpl @Method :getPresentDayActivities() {}", e.getMessage(), e);
            throw new BusinessException(e.getMessage());
        }
    }

    /**
     * Get activities related to SLA history for the last thirty days.
     *
     * This method retrieves activities related to SLA history for the last thirty days by calling the 
     * 'slaHistoryService.getActivitiesForThirtyDays' method. If the retrieval is successful, 
     * it returns the activities as a String. Otherwise, it throws a BusinessException with an error message.
     *
     * @return The activities related to SLA history for the last thirty days as a String.
     * @throws BusinessException If any error occurs during the retrieval of activities for the last thirty days.
     */
    @Override
    public Map<String,Map<String,Integer>> getActivitiesForThirtyDays() {
        try {
            return slaHistoryService.getActivitiesForThirtyDays();
        } catch (Exception e) {
            log.error("Error Inside @class: SlaHistoryRestImpl @Method :getActivitiesForThirtyDays() {}", e.getMessage(), e);
            throw new BusinessException(e.getMessage());
        }
    }

    /** 
    * Get information about the top SLAs that have been breached.
    *
    * This method retrieves information about the top SLAs that have been breached by calling the `slaHistoryService.getTopSlaBreached` method. If the retrieval is successful, it returns the information as a String. Otherwise, it throws a `BusinessException` with an error message.
    *
    * @return Information about the top SLAs that have been breached as a String.
    * @throws BusinessException If any error occurs during the retrieval of information about the top breached SLAs.
    */
    @Override
    public String getTopSlaBreached() {
        try {
            return slaHistoryService.getTopSlaBreached();
        } catch (Exception e) {
            log.error("Error Inside @class: SlaHistoryRestImpl @Method :getTopSlaBreached() {}", e.getMessage(), e);
            throw new BusinessException(e.getMessage());
        }
    }

    /**
     * Get information about the top SLAs that have been triggered.
     * This method retrieves information about the top SLAs that have been triggered by calling the `slaHistoryService.getTopSlaTriggered` method. If the retrieval is successful, it returns the information as a String. Otherwise, it throws a `BusinessException` with an error message.
     * @return Information about the top SLAs that have been triggered as a String.
     * @throws BusinessException If any error occurs during the retrieval of information about the top triggered SLAs.
     */
    @Override
    public String getTopSlaTriggered() {
        try {
            return slaHistoryService.getTopSlaTriggered();
        } catch (Exception e) {
            log.error("Error Inside @class: SlaHistoryRestImpl @Method :getTopSlaTriggered() {}", e.getMessage(), e);
            throw new BusinessException(e.getMessage());
        }
    }

    /**
     * Get information about the top SLAs entity-wise.
     *
     * This method retrieves information about the top SLAs entity-wise by calling the `slaHistoryService.getTopSlaEntityWise` method. If the retrieval is successful, it returns the information as a String. Otherwise, it throws a `BusinessException` with an error message.
     *
     * @return Information about the top SLAs entity-wise as a String.
     * @throws BusinessException If any error occurs during the retrieval of information about the top SLAs entity-wise.
     */
    @Override
    public String getTopSlaEntityWise() {
        try {
            return slaHistoryService.getTopSlaEntityWise();
        } catch (Exception e) {
            log.error("Error Inside @class: SlaHistoryRestImpl @Method :getTopSlaEntityWise() {}", e.getMessage(), e);
            throw new BusinessException(e.getMessage());
        }
    }

    /**
     * Get information about SLA breach proximity.
     *
     * This method retrieves information about SLA breach proximity by calling the `slaHistoryService.getSlaBreachProximity` method. If the retrieval is successful, it returns the information as a String. Otherwise, it throws a `BusinessException` with an error message.
     *
     * @return Information about SLA breach proximity as a String.
     * @throws BusinessException If any error occurs during the retrieval of information about SLA breach proximity.
     */
    @Override
    public String getSlaBreachProximity() {
        try{
            return slaHistoryService.getSlaBreachProximity();
        }catch (Exception e) {
            log.error("Error Inside @class: SlaHistoryRestImpl @Method :getSlaBreachProximity() {}", e.getMessage(), e);
            throw new BusinessException(e.getMessage());
        }
    }

    /**
     * Get the count of historical records based on a search query.
     *
     * This method retrieves the count of historical records that match the given search query by calling the `slaHistoryService.searchRecordsCount` method. If the retrieval is successful, it returns the count as a long value. Otherwise, it throws a `BusinessException` with an error message.
     *
     * @param query The search query used to find the historical records.
     * @return The count of historical records that match the search query as a long value.
     * @throws BusinessException If any error occurs during the retrieval of the count of historical records.
     */

    @Override
    public long getCountOfHistory(String query) throws BusinessException {
        try {
            return slaHistoryService.searchRecordsCount(query);

        } catch (Exception e) {
            log.error("Error Inside @class: SlaHistoryRestImpl @Method :getCountOfHistory() {}", e.getMessage(), e);
            throw new BusinessException(e.getMessage());
        }
    }

    /**
     * Search for SlaHistory records based on a given search query and return a limited number of results within a specified range.
     *
     * This method calls the `slaHistoryService.searchWithLimitAndOrderBy` method to perform the search. It passes the search query, lower and upper limit, column name to sort by (orderBy), and the order type (orderType) as parameters to the service method. The method returns a list of `SlaHistory` records that match the search query and fall within the specified range. If any error occurs during the search process, a `BusinessException` is thrown with an error message.
     *
     * @param query The search query used to find the SlaHistory records.
     * @param lowerLimit The lower limit of the range for the result set.
     * @param upperLimit The upper limit of the range for the result set.
     * @param orderBy The column name to sort the results by.
     * @param orderType The order type, which can be "ASC" (ascending) or "DESC" (descending).
     * @return A list of SlaHistory records that match the search query and fall within the specified range.
     * @throws BusinessException If any error occurs during the search process.
     */
    @Override
    public List<SlaHistory> searchSlaHistory(String query, Integer lowerLimit, Integer upperLimit, String orderBy,
                                                     String orderType) {
        try {
            return slaHistoryService.searchWithLimitAndOrderBy(query, upperLimit, lowerLimit, orderBy, orderType);

        } catch (Exception e) {
            log.error("Error Inside @class: SlaHistoryRestImpl @Method :searchSlaHistory() {}", e.getMessage(), e);
            throw new BusinessException(e.getMessage());
        }
    }

    /**
     * Get a JSON representation of SLA breaches grouped by SLA level.
     *
     * This method calls the `slaHistoryService.levelWiseSlaBreached` method to retrieve SLA breaches grouped by SLA level in JSON format. The method returns the JSON representation of SLA breaches grouped by SLA level. If any error occurs during the process, a `BusinessException` is thrown with an error message.
     *
     * @return A JSON representation of SLA breaches grouped by SLA level.
     * @throws BusinessException If any error occurs during the process.
     */
    @Override
    public String levelWiseSlaBreached() {
        try {
            return slaHistoryService.levelWiseSlaBreached();
        } catch (Exception e) {
            log.error("Error Inside @class: SlaHistoryRestImpl @Method :levelWiseSlaBreached() {}", e.getMessage(), e);
            throw new BusinessException(e.getMessage());
        }
    }

    /**
     * Get the total count of SlaHistory records in the database.
     *
     * This method calls the `slaHistoryService.getTotalHistoryCount` method to retrieve the total count of SlaHistory records in the database. The method returns the total count as a `Long` value. If any error occurs during the process, a `BusinessException` is thrown with an error message.
     *
     * @return The total count of SlaHistory records in the database.
     * @throws BusinessException If any error occurs during the process.
     */
    @Override
    public Long getTotalHistoryCount() {
        try{
            return slaHistoryService.getTotalHistoryCount();
        }catch (Exception e) {
            log.error("Error Inside @class: SlaHistoryRestImpl @Method :getTotalHistoryCount() {}", e.getMessage(), e);
            throw new BusinessException(e.getMessage());
        }
    }

    /**
     * Get a JSON representation of SLA triggers grouped by their status.
     *
     * This method calls the `slaHistoryService.slaTriggeredByStatus` method to retrieve SLA triggers grouped by their status in JSON format. The method returns the JSON representation of SLA triggers grouped by their status. If any error occurs during the process, a `BusinessException` is thrown with an error message.
     *
     * @return A JSON representation of SLA triggers grouped by their status.
     * @throws BusinessException If any error occurs during the process.
     */
     @Override
    public Map<String,Map<String,Integer>> slaTriggeredByStatus() {
        try{
            return slaHistoryService.slaTriggeredByStatus();
        }catch (Exception e) {
            log.error("Error Inside @class: SlaHistoryRestImpl @Method :slaTriggeredByStatus() {}", e.getMessage(), e);
            throw new BusinessException(e.getMessage());
        }
    }


    @Override
    public String getSlaHistoryAudById(long id){
        try {
            return slaHistoryService.getSlaHistoryAudById(id);
            } catch (Exception e) {
            log.error("Error Inside @class: SlaHistoryRestImpl @Method :getSlaHistoryAudById() {}", e.getMessage(), e);
            throw new BusinessException(e.getMessage());
            }
        }


    @Override
    public int auditCount(long id){
        try {
            return slaHistoryService.auditCount(id);
            } catch (Exception e) {
            log.error("Error Inside @class: SlaHistoryRestImpl @Method :auditCount() {}", e.getMessage(), e);
            throw new BusinessException(e.getMessage());
            }
        }
}
