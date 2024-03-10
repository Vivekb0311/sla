package com.bootnext.platform.sla.dao.impl;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.bootnext.core.generic.dao.impl.HibernateGenericDao;
import com.bootnext.core.generic.exceptions.application.BusinessException;
import com.bootnext.platform.sla.dao.ISlaLevelTemplateDao;
import com.bootnext.platform.sla.model.template.SlaLevelTemplate;
import com.bootnext.platform.sla.utils.SlaUtils;
import com.bootnext.platform.utils.Utils;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;
import lombok.extern.slf4j.Slf4j;

@Repository
@Slf4j
public class SlaLevelTemplateDaoImpl  extends HibernateGenericDao<Integer, SlaLevelTemplate> implements ISlaLevelTemplateDao {

    public SlaLevelTemplateDaoImpl( EntityManager entityManager) {
        super(SlaLevelTemplate.class, entityManager);
    }

    /**
     * Retrieve a list of SlaLevelTemplate objects that match the specified identifier.
     *
     * @param id The identifier of the SlaLevelTemplate to search for.
     * @return A List of SlaLevelTemplate objects that match the provided identifier.
     * @throws BusinessException If any error occurs during the retrieval process.
     */
    @Override
    public List<SlaLevelTemplate> getSlaLevelTemplateById(Integer id) {
        try{
            Query q = getEntityManager().createNamedQuery("getSlaLevelTemplateById").
            setParameter("id", id);
            return q.getResultList();
        }catch (Exception e) {
			log.error(SlaUtils.ERROR_OCCURRED, "getSlaLevelTemplateById");
            log.error(SlaUtils.EXCEPTION_STACK_TRACE, Utils.getStackTrace(e));
            log.error(SlaUtils.EXCEPTION_MSG, e.getMessage());
			throw new BusinessException(SlaUtils.SOMETHING_WENT_WRONG);
		}
    }

    /**
     * Retrieve a single SlaLevelTemplate object that matches the specified identifier and level.
     *
     * @param id    The identifier of the SlaLevelTemplate to search for.
     * @param level The level of the SlaLevelTemplate to search for.
     * @return The SlaLevelTemplate object that matches the provided identifier and level.
     * @throws BusinessException If any error occurs during the retrieval process.
     */
    @Override
    public SlaLevelTemplate getSlaLevelTemplateByIdAndLevel(Integer id, int level) {
        try{
            log.info("id {}, level {}");
            Query q = getEntityManager().createNamedQuery("getSlaLevelTemplateByIdAndLevel").
            setParameter("id", id).setParameter("level", level);
            return (SlaLevelTemplate) q.getSingleResult();
        }catch (Exception e) {
			log.error(SlaUtils.ERROR_OCCURRED, "getSlaLevelTemplateByIdAndLevel");
            log.error(SlaUtils.EXCEPTION_STACK_TRACE, Utils.getStackTrace(e));
            log.error(SlaUtils.EXCEPTION_MSG, e.getMessage());
			throw new BusinessException(SlaUtils.SOMETHING_WENT_WRONG);
		}
    }

    /**
     * Retrieve a single SlaLevelTemplate object that matches the provided generated value.
     *
     * @param generatedValue The generated value to search for.
     * @return The SlaLevelTemplate object that matches the provided generated value.
     * @throws BusinessException If any error occurs during the retrieval process.
     */
     @Override
    public SlaLevelTemplate getSlaLevelTemplateByGeneratedValue(String generatedValue) {
        SlaLevelTemplate slaLevelTemplate = null;
        try {
            log.info(SlaUtils.INSIDE_METHOD, "getslaLevelTemplateByGeneratedValue");
                    
            Query q = getEntityManager().createNamedQuery("getSlaLevelTemplateByGeneratedValue")
                    .setParameter(SlaUtils.GENERATED_VALUE, generatedValue);
            slaLevelTemplate = (SlaLevelTemplate) q.getSingleResult();
        } catch (Exception e) {
            log.error(SlaUtils.ERROR_OCCURRED, "getSlaLevelTemplateByGeneratedValue");
            log.error(SlaUtils.EXCEPTION_STACK_TRACE, Utils.getStackTrace(e));
            log.error(SlaUtils.EXCEPTION_MSG, e.getMessage());
            throw new BusinessException(SlaUtils.SOMETHING_WENT_WRONG);
        }
        return slaLevelTemplate;
    }

    
}

