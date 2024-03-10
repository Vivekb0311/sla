package com.bootnext.platform.sla.dao;

import java.util.List;

import com.bootnext.core.generic.dao.IGenericDao;
import com.bootnext.platform.sla.model.template.SlaLevelTemplate;

/**
 * The Interface ISlaLevelTemplateDao.
 */
public interface ISlaLevelTemplateDao extends IGenericDao<Integer, SlaLevelTemplate> {

    List<SlaLevelTemplate> getSlaLevelTemplateById(Integer id);

    SlaLevelTemplate getSlaLevelTemplateByIdAndLevel(Integer id, int level);
    SlaLevelTemplate getSlaLevelTemplateByGeneratedValue(String generatedValue);
    
}
