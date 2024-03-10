package com.bootnext.platform.sla.rest;

import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.bootnext.core.generic.exceptions.application.BusinessException;
import com.bootnext.platform.sla.model.template.Escalation;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Feign Client Interface for accessing Escalation API.
 * This interface provides methods to interact with the Escalation API endpoints.
 */


@FeignClient(name = "IEscalation", url = "${sla-service.url}", path = "/Escalation", primary = false)
// @Api(tags = { "IEscalationRest" })

        @Tag(name = "IEscalationRest") 
public interface IEscalationRest {

    /**
     * Check and Escalate user.
     *
     * @return A string indicating the success or failure of the escalation process.
     */    
    @Operation(summary = "check And Escalate user", tags = "scheduledEscalateUser", description = "Api to escalate user ")
    @GetMapping(path = "scheduleEscalateUser")
    String scheduleEscalateUser();
    
    /**
     * Get Escalation By Generated Value.
     *
     * @param generatedValue The generated value to search for.
     * @return The list of matching Escalation objects.
     * @throws BusinessException If any error occurs during the retrieval process.
     */
    @Operation(summary = "get Escalation By GeneratedValue", tags = "getEscalationByGeneratedValue", description = "Api to get generated value wise sla Escalation List")
    @GetMapping(path = "getEscalationByGeneratedValue")
    List<Escalation> getEscalationByGeneratedValue(@RequestParam(name = "generatedValue") String generatedValue)
            throws BusinessException;

    /**
     * Get Top Sla Template Escalated.
     *
     * @return A string representing the Top Sla Template Escalated.
     */
    @Operation(summary = "get Top Sla Template Escalated", tags = "getTopSlaEscalated", description = "Api to get TopSla Template Escalated")
    @GetMapping(path = "getTopSlaEscalated")
    String getTopSlaEscalated();

}
