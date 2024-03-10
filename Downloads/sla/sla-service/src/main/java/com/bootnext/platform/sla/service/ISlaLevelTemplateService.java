package com.bootnext.platform.sla.service;



import com.bootnext.core.generic.service.IBaseService;
import com.bootnext.platform.sla.model.template.SlaLevelTemplate;


public interface ISlaLevelTemplateService extends IBaseService<Integer, SlaLevelTemplate> {

SlaLevelTemplate getSlaLevelTemplateByGeneratedValue(String generatedValue);



}
