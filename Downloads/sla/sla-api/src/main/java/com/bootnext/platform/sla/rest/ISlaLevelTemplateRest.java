package com.bootnext.platform.sla.rest;


import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.bootnext.core.generic.exceptions.application.BusinessException;
import com.bootnext.platform.sla.model.template.SlaLevelTemplate;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Feign Client Interface for accessing SlaLevelTemplate API.
 * This interface provides methods to interact with the SlaLevelTemplate API endpoints.
 */

@FeignClient(name = "ISlaLevelTemplate", url = "${sla-service.url}", path = "/SlaLevelTemplate", primary = false)
// @Api(tags = { "ISlaLevelTemplateRest" })

        @Tag(name = "ISlaLevelTemplateRest") 
public interface ISlaLevelTemplateRest {

    /**
     * Get SlaLevelTemplate By GeneratedValue.
     *
     * @param generatedValue The generated value to search for.
     * @return The matching SlaLevelTemplate object.
     * @throws BusinessException If any error occurs during the retrieval process.
     */
    @Operation(summary = "get SlaLevelTemplate By GeneratedValue",tags = "getSlaLevelTemplateByGeneratedValue")
    @GetMapping(path = "getSlaLevelTemplateByGeneratedValue")
    SlaLevelTemplate getSlaLevelTemplateByGeneratedValue(@RequestParam(name = "generatedValue") String generatedValue)
            throws BusinessException;
}
