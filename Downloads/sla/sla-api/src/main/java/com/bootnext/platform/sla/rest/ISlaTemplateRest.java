package com.bootnext.platform.sla.rest;

import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.bootnext.core.generic.exceptions.application.BusinessException;
import com.bootnext.platform.sla.model.template.SlaTemplate;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;


/**
 * Feign Client Interface for accessing Sla Template API.
 * This interface provides methods to interact with the Sla Template API endpoints.
 */

@FeignClient(name = "ISlaTemplate", url = "${sla-service.url}", path = "/SlaTemplate", primary = false)
@Tag(name = "ISlaTemplateRest") 
public interface ISlaTemplateRest {

    /**
     * Create Sla Template.
     *
     * @param slaTemplate The SlaTemplate object to be created.
     * @return The created SlaTemplate object.
     * @throws BusinessException If any error occurs during the creation process.
     */
    @Operation(summary = "Create Sla Template", tags = "createSLA", description = "Api to create sla template")
    @PostMapping(path = "create", consumes = MediaType.APPLICATION_JSON_VALUE)
    SlaTemplate createSLA(@RequestBody(required = false) SlaTemplate slaTemplate)
            throws BusinessException;

    /**
     * Update Sla Template.
     *
     * @param slaTemplate The SlaTemplate object to be updated.
     * @return The updated SlaTemplate object.
     * @throws BusinessException If any error occurs during the update process.
     */
    @Operation(summary = "Update Sla Template", tags = "updateSLA", description = "Api to update sla template")
    @PostMapping(path = "update", consumes = MediaType.APPLICATION_JSON_VALUE)
    SlaTemplate updateSLA(@RequestBody(required = false) SlaTemplate slaTemplate)
            throws BusinessException;

     /**
     * Get SlaTemplate By GeneratedValue.
     *
     * @param generatedValue The generated value to search for.
     * @return The matching SlaTemplate object.
     * @throws BusinessException If any error occurs during the retrieval process.
     */
    @Operation(summary = "get SlaTemplate By GeneratedValue", tags = "getSlaTemplateByGeneratedValue", description = "Api to get generated value wise sla Template List")
    @GetMapping(path = "getSlaTemplateByGeneratedValue")
    SlaTemplate getSlaTemplateByGeneratedValue(@RequestParam(name = "generatedValue") String generatedValue)
            throws BusinessException;
    
    /**
     * Get Active SlaTemplates.
     *
     * @return The list of active SlaTemplate objects.
     */
    @Operation(summary = "get Active SlaTemplates", tags = "getActiveSlaTemplates", description = "Api to get status wise sla Template List")
    @GetMapping(path = "getActiveSlaTemplates")
    List<SlaTemplate> getActiveSlaTemplates();

    /**
     * Search Sla template.
     *
     * @param query      Search context that gives result on the basis of search parameter.
     * @param lowerLimit Minimum number of records required.
     * @param upperLimit Maximum number of records required.
     * @param orderBy    Order by any column name.
     * @param orderType  Sort the list according to order type.
     * @return The list of matching SlaTemplate objects.
     */
    @Operation(summary = "Search Sla template", tags = "search", description = "Api to search sla template")
    @GetMapping(path = "search")
    List<SlaTemplate> search(@Parameter(name = "Search context that gives result on the basis of search parameter") @RequestParam(required = false, name = "_s") String query,
    @Parameter(name = "Minimum number of records required") @RequestParam(required = false, name = "llimit", defaultValue = "0") Integer lowerLimit,
    @Parameter(name = "Maximum number of records required") @RequestParam(required = false, name = "ulimit", defaultValue = "450") Integer upperLimit,
    @Parameter(name = "Order by any column name") @RequestParam(required = false, name = "orderBy") String orderBy,
    @Parameter(name = "Sort the list according to order type") @RequestParam(required = false, name = "orderType") String orderType);
    
    /**
     * Get SlaTemplate Count.
     *
     * @return The count of SlaTemplate objects.
     */
    @Operation(summary = "get SlaTemplate Count", tags = "getCountsForTemplate", description = "Api to get counts for sla template")   
    @GetMapping(path = "getCountsForTemplate")
    String getCountsForTemplate();

    /**
     * Get SlaTemplate Count Entity Wise.
     *
     * @return The count of SlaTemplate objects entity-wise(Executed On).
     */
    @Operation(summary = "get SlaTemplate Count Entity Wise", tags = "getTemplateEntityWise", description = "Api to get counts for sla template Entity Wise")
    @GetMapping(path = "getTemplateEntityWise")
    String getTemplateEntityWise();

        /**
         * Get Count Of SlaTemplate.
         *
         * @param query The search query.
         * @return The count of SlaTemplate objects based on the search query.
         * @throws BusinessException If any error occurs during the retrieval process.
         */
        @Operation(summary = "getCountOfSlaTemplate", tags = "getCountOfSlaTemplate", description = "Api to search Sla template")
        @GetMapping(path = "getCountOfSlaTemplate")
        long getCountOfSlaTemplate(@RequestParam(name = "_s", required = false) String query)
                throws BusinessException;


        @Operation(summary = "export SlaTemplate By GeneratedValue", tags = "exportSlaTemplateByGeneratedValue", description = "Api export generated value wise sla Template")
        @GetMapping(path = "exportSla")
        String exportSla(@RequestParam(name = "generatedValue") String generatedValue)
                throws BusinessException;
                

        @Operation(summary = "export SlaTemplate By application", tags = "exportSlaTemplateByApplication", description = "Api export application wise sla Template")
        @GetMapping(path = "exportSlaApplicationWise")
        String exportSlaByApplication(@RequestParam(name = "application") String application)
                throws BusinessException;

        @Operation(summary = "import SlaTemplate", tags = "importSlaTemplate", description = "Api to import sla Template")
        @PostMapping(path = "importSlaTemplate", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
        List<SlaTemplate> importSlaTemplate( @RequestParam(name = "filedata") MultipartFile in) throws BusinessException;
        

        // @Operation(summary = "import SlaTemplate by  application", tags = "importSlaTemplateByApplication", description = "Api to import sla Template by Application name")
        // @PostMapping(path = "importSlaTemplateByApplication", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
        // List<SlaTemplate> importSlaTemplateByApplication( @RequestParam(name = "filedata") MultipartFile in) throws BusinessException;
        
     
        
}
