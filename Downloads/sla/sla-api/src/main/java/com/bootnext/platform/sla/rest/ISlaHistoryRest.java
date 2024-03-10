package com.bootnext.platform.sla.rest;

import java.util.List;
import java.util.Map;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import com.bootnext.core.generic.exceptions.application.BusinessException;
import com.bootnext.platform.sla.model.template.SlaHistory;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Feign Client Interface for accessing Sla History API.
 * This interface provides methods to interact with the Sla History API endpoints.
 */


@FeignClient(name = "ISlaHistory", url = "${sla-service.url}", path = "/SlaHistory", primary = false)
@Tag(name = "ISlaHistoryRest") 
public interface ISlaHistoryRest {
    
    /**
     * Trigger Sla.
     *
     * @param entityString    The entityString parameter.
     * @param applicationName The applicationName parameter.
     * @param entityName      The entityName parameter.
     * @param entityId        The entityId parameter.
     * @return True if the Sla is triggered successfully, false otherwise.
     * @throws BusinessException If any error occurs during the triggering process.
     */
    @Operation(summary = "trigger Sla", tags = "triggerSla", description = "Api to trigger Sla")
    @PostMapping(path = "triggerSla", consumes = MediaType.APPLICATION_JSON_VALUE)
    Boolean triggerSla(@RequestBody String entityString,
                                    @RequestParam(required = true, name = "applicationName") String applicationName,
                                    @RequestParam(required = true,name = "entityName") String entityName,
                                    @RequestParam(required = true, name = "entityId") String entityId)
            throws BusinessException;
    
    /**
     * Check and Execute History.
     *
     * @return A string indicating the execution status.
     */
    @Operation(summary = "check And Execute History", tags = "scheduledExecuteHistory", description = "Api to execute sla history ")
    @GetMapping(path = "scheduledExecuteHistory")
    String scheduledExecuteHistory();

    /**
     * Search SlaHistory.
     *
     * @param query      The search query.
     * @param lowerLimit The lower limit for the number of records required.
     * @param upperLimit The upper limit for the number of records required.
     * @param orderBy    Order by any column name.
     * @param orderType  Sort the list according to the order type.
     * @return The list of matching SlaHistory objects.
     * @throws BusinessException If any error occurs during the retrieval process.
     */
    @Operation(summary = "Search SlaHistory", tags = "searchSlaHistory", description = "Api to search SlaHistory")
    @GetMapping(path = "searchSlaHistory")
    List<SlaHistory> searchSlaHistory(@RequestParam(name = "_s", required = false) String query,
                                              @RequestParam(required = false, defaultValue = "0", name = "llimit") Integer lowerLimit,
                                              @RequestParam(required = false,defaultValue = "450", name = "ulimit") Integer upperLimit,
                                              @RequestParam(required = false, name = "orderBy") String orderBy,
                                              @RequestParam(required = false, name = "orderType") String orderType)
            throws BusinessException;

    /**
     * Get Present Day Activities
     * 
     * @return A String representing total SLA Breached, Total SLA Escalated and there grand total for current day. 
     */
    @Operation(summary = "get Present day activities count", tags = "getPresentDayActivities", description = "Api to get Present day activities count")
    @GetMapping(path = "getPresentDayActivities")
    String getPresentDayActivities();

    /**
     * Get Activities for Thirty Days
     * 
     * @return A String representing total SLA Breached, Total SLA Escalated and there grand total for previous 30 days.
     */
    @Operation(summary = "get last 30 days activities count", tags = "getActivitiesForThirtyDays", description = "Api to get last 30 days activities count")
    @GetMapping(path = "getActivitiesForThirtyDays")
    Map<String,Map<String,Integer>> getActivitiesForThirtyDays();

    /**
     * Get Top Sla Breached
     * 
     * @return A string representing data for top 5 Sla Template Breached, includes SLA template name, count and Last breached time.
     */
    @Operation(summary = "get TopSla Template Breached", tags = "getTopSlaBreached", description = "Api to get TopSla Template Breached")
    @GetMapping(path = "getTopSlaBreached")
    String getTopSlaBreached();

    /**
     * Get Top Sla Triggered
     * 
     * @return A string representing data for top 5 Sla Template Triggered, includes SLA template name, count and Last breached time.
     */
    @Operation(summary = "get TopSla Template Triggered", tags = "getTopSlaTriggered", description = "Api to get TopSla Template Triggered")
    @GetMapping(path = "getTopSlaTriggered")
    String getTopSlaTriggered();

    /**
     * Get Top SLA Entity Wise
     * 
     * @return A String representing data for top 5 SLA Template triggered grouped by 'Exceuted On', this includes
     *         name of entity(ExecutedOn), counts for sla (In progress, Completed and Breached).
     */
    @Operation(summary = "get Top Sla Template Entity Wise", tags = "getTopSlaEntityWise", description = "Api to get Top Sla Template Entity Wise")
    @GetMapping(path = "getTopSlaEntityWise")
    String getTopSlaEntityWise();

    /**
     * Get SLA Breach Proximity
     * 
     * @return A String reprenting data for the SLA which are about to breach (Today, Tomorrow, this Week and This Month).
     */
    @Operation(summary = "get sla breach proximity", tags = "getSlaBreachProximity", description = "Api to get sla breach proximity")
    @GetMapping(path = "getSlaBreachProximity")
    String getSlaBreachProximity();

    /**
     * Get the count of SlaHistory records based on the search query.
     *
     * @param query The search query to filter the SlaHistory records (optional).
     * @return The count of SlaHistory records that match the search query.
     * @throws BusinessException If any error occurs during the retrieval process.
     */
    @Operation(summary = "Search SlaHistory", tags = "getCountOfHistory", description = "Api to search SlaHistory")
    @GetMapping(path = "getCountOfHistory")
    long getCountOfHistory(@RequestParam(name = "_s", required = false) String query)
            throws BusinessException;

    /**
     * Level Wise Sla Breached 
     * 
     * @return A String represnting data for SLA breached grouped by levels.
     */        
    @Operation(summary = "get level wise sla breach count", tags = "levelWiseSlaBreached", description = "Api to get level wise sla breach count")
    @GetMapping(path = "levelWiseSlaBreached")
    String levelWiseSlaBreached();

    @Operation(summary = "get total count of history", tags = "getTotalHistoryCount", description = "Api to get total count of history")
    @GetMapping(path = "getTotalHistoryCount")
    Long getTotalHistoryCount();

    /**
     * Sla Triggered By Status
     * 
     * @return A String representing data for count of SLA Triggered each day for previous 30 days grouped by status(In Progress, Complete, On Hold).
     */
    @Operation(summary = "get last 30 days count of sla triggered by status", tags = "slaTriggeredByStatus", description = "Api to get last 30 days count of sla triggered by status")
    @GetMapping(path = "slaTriggeredByStatus")
    Map<String,Map<String,Integer>> slaTriggeredByStatus();

    @Operation(summary = "get sla history audit by Id", tags = "getSlaHistoryAudById", description = "Api to get sla history audit by slaId")
    @GetMapping(path = "getSlaHistoryAudBySlaId")
    String getSlaHistoryAudById(@RequestParam(name="id") long id);


    @Operation(summary = "get sla history audit count by Id", tags = "getSlaHistoryAudCountById", description = "Api to get sla history audit count by slaId")
    @GetMapping(path = "getSlaHistoryAudCountBySlaId")
    int auditCount(@RequestParam(name="id") long id);


}


