package com.bootnext.platform.sla.dao;

import java.util.List;

import org.json.JSONObject;

import com.bootnext.core.generic.dao.IGenericDao;
import com.bootnext.platform.sla.model.template.SlaTemplate;

/**
 * The Interface ISlaTemplateDao.
 */
public interface ISlaTemplateDao extends IGenericDao<Integer, SlaTemplate> {

    List<SlaTemplate> getActiveSlaTemplates();

    JSONObject getSlaCountByModuleWise();

    SlaTemplate getSlaTemplateByGeneratedValue(String generatedValue);

    List<SlaTemplate> getSlaTemplateByApplicationAndExecutedOn(String application, String executedOn);
    
    long getTemplateCountByStatus(String status);

    long getTemplateCountByApproval(SlaTemplate.APPROVAL approval);

    JSONObject getTemplateEntityWise ();

    List<SlaTemplate> isTemplateExist (String name);

    List<SlaTemplate> getSlaTemplateByApplication(String application);
}
