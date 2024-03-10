package com.bootnext.platform.sla.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.bootnext.core.generic.exceptions.application.BusinessException;
import com.bootnext.core.generic.service.impl.AbstractService;
import com.bootnext.platform.sla.dao.ISlaLevelTemplateDao;
import com.bootnext.platform.sla.model.template.SlaLevelTemplate;
import com.bootnext.platform.sla.service.ISlaLevelTemplateService;
import com.bootnext.platform.sla.utils.SlaUtils;

import lombok.extern.slf4j.Slf4j;


/**
 * Service implementation for managing Service Level Agreement (SLA) level templates.
 * Extends the AbstractService class with Integer as the entity ID type and SlaLevelTemplate as the entity type.
 *
 * This service provides methods for retrieving and manipulating SLA level templates in the application.
 * It interacts with the data access layer and handles business logic related to SLA level templates.
 *
 * It communicates with the data access layer through the ISlaLevelTemplateDao interface to perform database operations.
 *
 * The SLA level templates define different escalation levels and their associated rules and configurations for SLA breaches.
 * The service allows the application to define and customize escalation levels to handle SLA violations effectively.
 */
@Service
@Slf4j
public class SlaLevelTemplateServiceImpl extends AbstractService<Integer, SlaLevelTemplate>
        implements ISlaLevelTemplateService  {

        @Autowired
    private ISlaLevelTemplateDao slaLevelTemplateDao;   

    /**
     * Retrieves an SLA level template by its generated value from the database.
     *
     * @param generatedValue The generated value of the SLA level template.
     * @return The SLA level template with the specified generated value.
     * @throws BusinessException If an error occurs while retrieving the SLA level template.
     */
    @Override
    
    public SlaLevelTemplate getSlaLevelTemplateByGeneratedValue(String generatedValue) {
        try {
            log.info(SlaUtils.INSIDE_METHOD, "getSlaLevelTemplateByGeneratedValue");
            return slaLevelTemplateDao.getSlaLevelTemplateByGeneratedValue(generatedValue);
        } catch (Exception e) {
            log.error("Error Inside @class: SlaLevelTemplateServiceImpl @Method :getSlaLevelTemplateByGeneratedValue() {}", e.getMessage(), e);
            throw new BusinessException(e.getMessage());
        }
    }


    
}
