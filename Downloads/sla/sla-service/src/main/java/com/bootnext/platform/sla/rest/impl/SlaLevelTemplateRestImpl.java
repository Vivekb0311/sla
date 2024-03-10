package com.bootnext.platform.sla.rest.impl;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.bootnext.core.generic.exceptions.application.BusinessException;
import com.bootnext.platform.sla.model.template.SlaLevelTemplate;
import com.bootnext.platform.sla.rest.ISlaLevelTemplateRest;
import com.bootnext.platform.sla.service.ISlaLevelTemplateService;
import com.bootnext.platform.sla.utils.SlaUtils;

import lombok.extern.slf4j.Slf4j;

/**
 * SlaLevelTemplateRestImpl
 * 
 * This class is a Spring REST Controller that handles HTTP requests related to SLA Level Template.
 * It implements the ISlaLevelTemplateRest interface and defines several methods for handling different
 * API endpoints related to SLA Level Template. The class is also annotated with @RequestMapping to specify
 * the base URL path for the API and the media type of the response.
 * 
 */
@RestController
@RequestMapping(produces = MediaType.APPLICATION_JSON_VALUE, path = "/SlaLevelTemplate")
@Primary
@Slf4j
public class SlaLevelTemplateRestImpl implements ISlaLevelTemplateRest {


    @Autowired
    private ISlaLevelTemplateService slaLevelTemplateService;

    /**
     * Retrieves a specific SLA level template by its generated value and returns the result.
     *
     * @param generatedValue The generated value of the SLA level template to retrieve.
     * @return The SLA level template matching the provided generated value, or null if not found.
     * @throws BusinessException If an error occurs during the operation.
     */
    @Override
    public SlaLevelTemplate getSlaLevelTemplateByGeneratedValue(String generatedValue) {
        try {
            log.info(SlaUtils.INSIDE_METHOD, "getSlaLevelTemplateByGeneratedValue");
            return slaLevelTemplateService.getSlaLevelTemplateByGeneratedValue(generatedValue);
        } catch (Exception e) {
            log.error("Error Inside @class: SlaLevelTemplateRestImpl @Method :getSlaLevelTemplateByGeneratedValue() {}", e.getMessage(), e);
            throw new BusinessException(e.getMessage());
        }
    }
}
