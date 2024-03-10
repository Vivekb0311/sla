package com.bootnext.platform.sla.rest.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.bootnext.core.generic.exceptions.application.BusinessException;
import com.bootnext.platform.customannotation.annotation.GenericAnnotation;
import com.bootnext.platform.sla.model.template.SlaTemplate;
import com.bootnext.platform.sla.rest.ISlaTemplateRest;
import com.bootnext.platform.sla.service.ISlaTemplateService;
import com.bootnext.platform.sla.utils.SlaUtils;
import com.bootnext.product.audit.utils.ActionType;
import com.bootnext.product.audit.utils.Auditable;

import lombok.extern.slf4j.Slf4j;

/**
 * SlaTemplateRestImpl
 * 
 * This class is a Spring REST Controller that handles HTTP requests related to SLA Template.
 * It implements the ISlaLevelTemplateRest interface and defines several methods for handling different
 * API endpoints related to SLA Template. The class is also annotated with @RequestMapping to specify
 * the base URL path for the API and the media type of the response.
 * 
 */
@RestController
@RequestMapping(produces = MediaType.APPLICATION_JSON_VALUE, path = "/SlaTemplate")
@Primary
@Slf4j
public class SlaTemplateRestImpl implements ISlaTemplateRest {

    @Autowired
    private ISlaTemplateService slaTemplateService;

    /**
     * Creates a new SLA template based on the provided SlaTemplate object and returns the newly created SLA template.
     * This method is audited for CREATE action with the action name "SLACONFIGURATION_CREATE".
     *
     * @param slaTemplate The SlaTemplate object containing the details of the SLA template to be created.
     * @return The newly created SLA template with its generated ID.
     * @throws BusinessException If an error occurs during the creation process.
     */
    @Override
    // @GenericAnnotation(actionType="CREATE",annotationName = {"GlobleSearch"}, appName = "${application}", entityName = "Site",globalSearchData="displayName, code",searchTitle="name", uniqEntityId = "id", pageType=GenericAnnotation.TYPE.Custom)
    @Auditable(actionType = ActionType.CREATE, actionName = SlaUtils.SLACONFIGURATION_CREATE,componentName = "SLA")
    public SlaTemplate createSLA(SlaTemplate slaTemplate) {
        log.info(SlaUtils.INSIDE_METHOD, "createSLA");
        try {
            SlaTemplate newSlaTemplate = slaTemplateService.createSLATemplate(slaTemplate);
            log.info("after sla creation sla id: {}", newSlaTemplate.getId());
            return newSlaTemplate;
        } catch (Exception e) {
            log.error("Error Inside @class: SlaTemplateRestImpl @Method :createSLA() {}", e.getMessage(), e);
            throw new BusinessException(e.getMessage());
        }
    }

    /**
     * Updates an existing SLA template based on the provided SlaTemplate object and returns the updated SLA template.
     * This method is audited for UPDATE action with the action name "SLACONFIGURATION_UPDATE".
     *
     * @param slaTemplate The SlaTemplate object containing the updated details of the SLA template.
     * @return The updated SLA template.
     * @throws BusinessException If an error occurs during the update process.
     */
    @Override
    @Auditable(actionType = ActionType.UPDATE, actionName = SlaUtils.SLACONFIGURATION_UPDATE, componentName = "SLA")
    public SlaTemplate updateSLA(SlaTemplate slaTemplate) throws BusinessException {
        log.info(SlaUtils.INSIDE_METHOD, "update");
        try {
            return slaTemplateService.update(slaTemplate);
        } catch (Exception e) {
            log.error("Error Inside @class: SlaTemplateRestImpl @Method :updateSLA() {}", e.getMessage(), e);
            throw new BusinessException(e.getMessage());
        }
    }

    /**
     * Retrieves an existing SLA template based on the provided generated value and returns the corresponding SlaTemplate object.
     * This method is audited for READ action with the action name "SLACONFIGURATION_READ".
     *
     * @param generatedValue The generated value of the SLA template to be retrieved.
     * @return The SLA template corresponding to the provided generated value.
     * @throws BusinessException If an error occurs during the retrieval process.
     */
    @Override
    public SlaTemplate getSlaTemplateByGeneratedValue(String generatedValue) {
        try {
            log.info(SlaUtils.INSIDE_METHOD, "getSlaConfigurationByGeneratedValue");
            return slaTemplateService.getSlaTemplateByGeneratedValue(generatedValue);
        } catch (Exception e) {
            log.error("Error Inside @class: SlaTemplateRestImpl @Method :getSlaTemplateByGeneratedValue() {}", e.getMessage(), e);
            throw new BusinessException(e.getMessage());
        }
    }

    /**
     * Retrieves a list of active SLA templates and returns them as a List of SlaTemplate objects.
     * This method is audited for READ action with the action name "SLACONFIGURATION_READ".
     *
     * @return A List of active SLA templates.
     * @throws BusinessException If an error occurs during the retrieval process.
     */
    @Override
    public List<SlaTemplate> getActiveSlaTemplates() {
        log.info(SlaUtils.INSIDE_METHOD, "getActiveSlaConfigurations");
        try {
            return slaTemplateService.getActiveSlaTemplates();
        } catch (Exception e) {
            log.error("Error Inside @class: SlaTemplateRestImpl @Method :getActiveSlaTemplates() {}", e.getMessage(), e);
            throw new BusinessException(e.getMessage());
        }
    }

    /**
     * Searches for SLA templates based on the provided query and pagination parameters.
     * This method returns a list of SLA templates that match the search criteria within the specified limits.
     *
     * @param query       The search query used to filter SLA templates.
     * @param lowerLimit  The lower limit for pagination.
     * @param upperLimit  The upper limit for pagination.
     * @param orderBy     The field used for sorting the results.
     * @param orderType   The type of sorting (ASC or DESC).
     * @return A List of SLA templates that match the search criteria within the specified limits.
     * @throws BusinessException If an error occurs during the search process.
     */
    @Override
    public List<SlaTemplate> search(String query, Integer lowerLimit, Integer upperLimit, String orderBy,
                                         String orderType) {
        try {
            return slaTemplateService.searchWithLimitAndOrderBy(query, upperLimit, lowerLimit, orderBy, orderType);
        } catch (Exception e) {
            log.error("Error Inside @class: SlaTemplateRestImpl @Method :search() {}", e.getMessage(), e);
            throw new BusinessException(e.getMessage());
        }
    }

    /**
     * Gets the count of SLA templates that match the provided search query.
     *
     * @param query The search query used to filter SLA templates.
     * @return The count of SLA templates that match the search query.
     * @throws BusinessException If an error occurs during the count process.
     */
    @Override
    public long getCountOfSlaTemplate(String query) throws BusinessException {
        try {
            long count = slaTemplateService.searchRecordsCount(query);
            log.info(" count {}", count);
            return  count;

        } catch (Exception e) {
            log.error("Error Inside @class: SlaTemplateRestImpl @Method :getCountOfSlaTemplate() {}", e.getMessage(), e);
            throw new BusinessException(e.getMessage());
        }
    }

    /**
     * Gets the counts for SLA templates.
     *
     * @return A JSON string containing the counts for different SLA template statuses.
     * @throws BusinessException If an error occurs while fetching the counts.
     */
    @Override
    public String getCountsForTemplate() {
        try {
            return slaTemplateService.getCountsForTemplate();
        } catch (Exception e) {
            log.error("Error Inside @class: SlaTemplateRestImpl @Method :getCountsForTemplate() {}", e.getMessage(), e);
            throw new BusinessException(e.getMessage());
        }

    }

    /**
     * Retrieves the SLA template entities and their counts.
     *
     * @return A JSON string containing the SLA template entities and their counts.
     * @throws BusinessException If an error occurs while fetching the data.
     */
    @Override
    public String getTemplateEntityWise() {
        try {
            return slaTemplateService.getTemplateEntityWise();
        } catch (Exception e) {
            log.error("Error Inside @class: SlaTemplateRestImpl @Method :getTemplateEntityWise() {}", e.getMessage(), e);
            throw new BusinessException(e.getMessage());
        }
    }

    @Override
    public String exportSla(String generatedValue) {
        try {
            log.info(SlaUtils.INSIDE_METHOD, "exportSla");
            return slaTemplateService.exportSla(generatedValue);
        } catch (Exception e) {
            log.error("Error Inside @class: SlaTemplateRestImpl @Method :exportSla() {}", e.getMessage(), e);
            throw new BusinessException(e.getMessage());
        }
    }
    
    @Override
    public String exportSlaByApplication(String application) {
        try {
            log.info(SlaUtils.INSIDE_METHOD, "exportSla");
            return slaTemplateService.exportSlaByApplication(application);
        } catch (Exception e) {
            log.error("Error Inside @class: SlaTemplateRestImpl @Method :exportSlaByApplication() {}", e.getMessage(), e);
            throw new BusinessException(e.getMessage());
        }
    }

    @Override
    public List<SlaTemplate> importSlaTemplate(MultipartFile filename) {
        try {
            log.info(SlaUtils.INSIDE_METHOD, "importSla");
            return slaTemplateService.importSla(filename);
        } catch (Exception e) {
            log.error("Error Inside @class: SlaTemplateRestImpl @Method :importSla() {}", e.getMessage(), e);
            throw new BusinessException(e.getMessage());
        }
    }
    
}
