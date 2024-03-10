package com.bootnext.platform.sla.rest.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.bootnext.core.generic.exceptions.application.BusinessException;
import com.bootnext.platform.sla.model.template.Escalation;
import com.bootnext.platform.sla.rest.IEscalationRest;
import com.bootnext.platform.sla.service.IEscalationService;
import com.bootnext.platform.sla.utils.SlaUtils;

import lombok.extern.slf4j.Slf4j;
/**
 * EscalationRestImpl
 * 
 * This class is a Spring REST Controller that handles HTTP requests related to Escalation.
 * It implements the IEscalationRest interface and defines several methods for handling different
 * API endpoints related to Escalation. The class is also annotated with @RequestMapping to specify
 * the base URL path for the API and the media type of the response.
 * 
 */
@RestController
@RequestMapping(produces = MediaType.APPLICATION_JSON_VALUE, path = "/Escalation")
@Primary
@Slf4j
public class EscalationRestImpl implements IEscalationRest {

    @Autowired
    private IEscalationService escalationService;

    /**
     * Schedule the escalation process for users.
     *
     * This method triggers the escalation process for users based on predefined rules and conditions. 
     * It is responsible for escalating users who have not met their SLA requirements within the
     * specified time frame. The escalation logic is implemented in the escalationService, 
     * which handles the actual escalation tasks.
     *
     * @return A success message indicating that users have been escalated.
     * @throws BusinessException If any error occurs during the scheduling process.
     */
    @Override
    public String scheduleEscalateUser() {
        try{
            log.info("inside restImpl ScheduledEscalateHistory");
            String str = "success users have been escalated";
           escalationService.scheduleEscalateUser();
            return str;
        }catch (Exception e) {
            log.error("Error Inside @class: EscalationRestImpl @Method :scheduleEscalateUser() {}", e.getMessage(), e);
            throw new BusinessException(e.getMessage());
        }
    }

    /**
     * Get a list of escalations based on the generated value.
     *
     * This method retrieves a list of escalations associated with the provided generatedValue.
     * The generatedValue is used as a parameter to query the escalationService, 
     * which performs the actual retrieval of escalations based on the provided value.
     *
     * @param generatedValue The generated value to search for escalations.
     * @return A list of escalations matching the provided generatedValue.
     * @throws BusinessException If any error occurs during the retrieval process.
     */
    @Override
    public List<Escalation> getEscalationByGeneratedValue(String generatedValue) {
        try {
            log.info(SlaUtils.INSIDE_METHOD, "getEscalationByGeneratedValue");
            return escalationService.getEscalationByGeneratedValue(generatedValue);
        } catch (Exception e) {
            log.error("Error Inside @class: EscalationRestImpl @Method :getEscalationByGeneratedValue() {}", e.getMessage(), e);
            throw new BusinessException(e.getMessage());
        }
    }

    /**
     * Get the top SLAs that have been escalated.
     *
     * This method retrieves a String representation of the top SLAs that have been escalated. 
     * It calls the escalationService to fetch this information. The returned String contains 
     * details about the escalated SLAs, such as names, counts, and dates of escalation.
     *
     * @return A String representation of the top escalated SLAs.
     * @throws BusinessException If any error occurs during the retrieval process.
     */
    @Override
    public String getTopSlaEscalated() {
        try {
            return escalationService.getTopSlaEscalated();
        } catch (Exception e) {
            log.error("Error Inside @class: EscalationRestImpl @Method :getTopSlaEscalated() {}", e.getMessage(), e);
            throw new BusinessException(e.getMessage());
        }
    }
}
