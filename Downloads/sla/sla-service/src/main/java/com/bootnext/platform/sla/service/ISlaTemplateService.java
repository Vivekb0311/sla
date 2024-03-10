package com.bootnext.platform.sla.service;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.bootnext.core.generic.service.IBaseService;
import com.bootnext.platform.sla.model.template.SlaTemplate;

public interface ISlaTemplateService extends IBaseService<Integer, SlaTemplate> {

    SlaTemplate createSLATemplate(SlaTemplate slaTemplate);

    SlaTemplate getSlaTemplateByGeneratedValue(String generatedValue);

    List<SlaTemplate> getActiveSlaTemplates();

    String getCountsForTemplate();

    String getTemplateEntityWise();

    String exportSla(String generatedValue);

    List<SlaTemplate> importSla(MultipartFile file);

    String exportSlaByApplication(String application);
    
}
