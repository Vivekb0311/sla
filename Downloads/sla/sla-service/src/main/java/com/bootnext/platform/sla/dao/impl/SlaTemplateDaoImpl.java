package com.bootnext.platform.sla.dao.impl;



import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;
import org.springframework.stereotype.Repository;

import com.bootnext.core.generic.dao.impl.HibernateGenericDao;
import com.bootnext.core.generic.exceptions.application.BusinessException;
import com.bootnext.platform.sla.dao.ISlaTemplateDao;
import com.bootnext.platform.sla.model.template.SlaTemplate;
import com.bootnext.platform.sla.utils.SlaUtils;
import com.bootnext.platform.utils.Utils;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;
import lombok.extern.slf4j.Slf4j;

@Repository
@Slf4j
public class SlaTemplateDaoImpl extends HibernateGenericDao<Integer, SlaTemplate>
        implements ISlaTemplateDao {

    public SlaTemplateDaoImpl(EntityManager entityManager) {
        super(SlaTemplate.class, entityManager);
    }

    /**
     * Retrieve a list of active SlaTemplate objects.
     *
     * @return A list of active SlaTemplate objects.
     */
    @Override
    public List<SlaTemplate> getActiveSlaTemplates() {
        log.info(SlaUtils.INSIDE_METHOD, "getActiveSlaConfigurations");
        Query q = getEntityManager().createNamedQuery("getActiveSlaConfiguration");
        return q.getResultList();
    }

    /**
     * Retrieve a JSONObject containing the count of SLA configurations grouped by module.
     *
     * @return A JSONObject containing the count of SLA configurations grouped by module.
     */
    @Override
    public JSONObject getSlaCountByModuleWise() {
        log.info(SlaUtils.INSIDE_METHOD, "getSlaCountByModuleWise");
        try {
            Query query = null;
            query = getEntityManager()
                    .createNativeQuery("select EXECUTE_ON,count(*) from SLA_CONFIGURATION  group by EXECUTED_ON");
            if (query != null) {
                log.info("LIST== {}", query.getResultList());
                List<Object[]> resultSet = query.getResultList();
                JSONObject jsonObject = new JSONObject();
                for (Object[] obj : resultSet) {
                    jsonObject.put(String.valueOf(obj[0]), obj[1]);
                }
                return jsonObject;
            }
        } catch (Exception e) {
            log.error(SlaUtils.ERROR_OCCURRED, "getSlaCountByModuleWise");
            log.error(SlaUtils.EXCEPTION_STACK_TRACE, Utils.getStackTrace(e));
            log.error(SlaUtils.EXCEPTION_MSG, e.getMessage());
        }
        return null;
    }

    /**
     * Retrieve a JSONObject containing the mapping of template entities.
     *
     * @return A JSONObject containing the mapping of template entities.
     */
    @Override
    public JSONObject getTemplateEntityWise() {
        try{
            Query q = getEntityManager().createNamedQuery("getTemplateEntityWise");
            if(q != null){
                log.info("LIST== {}", q.getResultList());
                List<Object[]> resultSet = q.getResultList();
                JSONObject jsonObject = new JSONObject();
                for (Object[] obj : resultSet) {
                    jsonObject.put(String.valueOf(obj[0]), obj[1]);
                }
                return jsonObject;
            }
        }
        catch (Exception e) {
            log.error(SlaUtils.ERROR_OCCURRED, "getTemplateEntityWise");
            log.error(SlaUtils.EXCEPTION_STACK_TRACE, Utils.getStackTrace(e));
            log.error(SlaUtils.EXCEPTION_MSG, e.getMessage());
        }
        return null;
    }

    /**
     * Retrieve the SlaTemplate based on the provided generatedValue.
     *
     * @param generatedValue The generated value to search for the SlaTemplate.
     * @return The SlaTemplate object matching the provided generatedValue.
     * @throws BusinessException If any error occurs during the retrieval process.
     */
    @Override
    public SlaTemplate getSlaTemplateByGeneratedValue(String generatedValue) {
        SlaTemplate slaTemplate = null;
        try {
            log.info(SlaUtils.INSIDE_METHOD, "getslaTemplateByGeneratedValue");
              
            Query q = getEntityManager().createNamedQuery("getSlaTemplateByGeneratedValue")
                    .setParameter(SlaUtils.GENERATED_VALUE, generatedValue);
            slaTemplate = (SlaTemplate) q.getSingleResult();
        } catch (Exception e) {
            log.error(SlaUtils.ERROR_OCCURRED, "getSlaTemplateByGeneratedValue");
            log.error(SlaUtils.EXCEPTION_STACK_TRACE, Utils.getStackTrace(e));
            log.error(SlaUtils.EXCEPTION_MSG, e.getMessage());
            throw new BusinessException(SlaUtils.SOMETHING_WENT_WRONG);
        }
        return slaTemplate;
    }

    @Override
    public List<SlaTemplate> getSlaTemplateByApplication(String application){
        List<SlaTemplate> slaTemplate = null;
        try{
            Query q = getEntityManager().createNamedQuery("getSlaTemplateByApplication").setParameter("application", application);
            slaTemplate = q.getResultList();

        }catch(Exception e){
            log.error("error occured inside @class: SlaTemplateDaoImpl @method: getSlaTemplateByApplication ", e.getMessage(), e);
        }
        return slaTemplate;
    }

    /**
     * Check if a SlaTemplate with the specified name exists.
     *
     * @param name The name of the SlaTemplate to check for existence.
     * @return A list of SlaTemplate objects matching the specified name (could be empty if no matches are found).
     * @throws BusinessException If any error occurs during the check process.
     */
    @Override
    public List<SlaTemplate> isTemplateExist(String name) {
        try {
            List<SlaTemplate> slaTemplate = new ArrayList<>();
            log.info(SlaUtils.INSIDE_METHOD, "isTemplateExist");
            
            Query q = getEntityManager().createNamedQuery("isTemplateExist")
                    .setParameter("name", name);
            slaTemplate =  q.getResultList();
            return slaTemplate;
        } catch (Exception e) {
            log.error(SlaUtils.ERROR_OCCURRED, "is_Template_Exist");
            log.error(SlaUtils.EXCEPTION_STACK_TRACE, Utils.getStackTrace(e));
            log.error(SlaUtils.EXCEPTION_MSG, e.getMessage());
            throw new BusinessException(SlaUtils.SOMETHING_WENT_WRONG);
        }
    }

    /**
     * Retrieve a list of SlaTemplate objects based on the specified application and executedOn date.
     *
     * @param application The application name to filter the SlaTemplate objects.
     * @param executedOn  The executedOn date to filter the SlaTemplate objects.
     * @return A list of SlaTemplate objects matching the specified application and executedOn date (could be empty if no matches are found).
     * @throws BusinessException If any error occurs during the retrieval process.
     */
    @Override
    public List<SlaTemplate> getSlaTemplateByApplicationAndExecutedOn(String application, String executedOn) {
        List<SlaTemplate> slaTemplates = null;
        try {
            log.info(SlaUtils.INSIDE_METHOD, "getSlaTemplateByApplicationAndExecutedOn");

            TypedQuery<SlaTemplate> query = getEntityManager().createQuery(
                    "SELECT e FROM SlaTemplate e WHERE e.application=:application AND e.executedOn=:executedOn", SlaTemplate.class);

            slaTemplates = query.setParameter("application", application).setParameter("executedOn", executedOn)
                    .getResultList();
        } catch (Exception e) {
            log.error(SlaUtils.ERROR_OCCURRED, "getSlaTemplateByApplicationAndExecutedOn");
            log.error(SlaUtils.EXCEPTION_STACK_TRACE, Utils.getStackTrace(e));
            log.error(SlaUtils.EXCEPTION_MSG, e.getMessage());
            throw new BusinessException(SlaUtils.SOMETHING_WENT_WRONG);
        }
        return slaTemplates;
    }

    /**
     * Get the count of SlaTemplate objects based on the specified isActive status.
     *
     * @param isActive The isActive status to filter the SlaTemplate objects. Valid values are "true" for active templates and "false" for inactive templates.
     * @return The count of SlaTemplate objects matching the specified isActive status.
     * @throws BusinessException If any error occurs during the retrieval process.
     */
    @Override
    public long getTemplateCountByStatus(String isActive) {
        long count = 0;
        try {
            Query q = getEntityManager().createNamedQuery("getTemplateCountByStatus")
                    .setParameter("isActive", isActive);
            count = (long) q.getSingleResult();
        }catch (Exception e) {
             e.printStackTrace();
            log.error(SlaUtils.ERROR_OCCURRED, "getTemplateCountByStatus");
            log.error(SlaUtils.EXCEPTION_STACK_TRACE, Utils.getStackTrace(e));
            log.error(SlaUtils.EXCEPTION_MSG, e.getMessage());
            throw new BusinessException(SlaUtils.SOMETHING_WENT_WRONG);
        }

        return count;
    }

    /**
     * Get the count of SlaTemplate objects based on the specified approval status.
     *
     * @param approval The approval status to filter the SlaTemplate objects. Use the SlaTemplate.APPROVAL enum to provide valid values.
     * @return The count of SlaTemplate objects matching the specified approval status.
     * @throws BusinessException If any error occurs during the retrieval process.
     */
    @Override
    public long getTemplateCountByApproval(SlaTemplate.APPROVAL approval) {
        long count = 0;
        try {
            Query q = getEntityManager().createNamedQuery("getTemplateCountByApproval")
                    .setParameter("approval", approval);
            count = (long) q.getSingleResult();
        }catch (Exception e) {
            log.error(SlaUtils.ERROR_OCCURRED, "getTemplateCountByApproval");
            log.error(SlaUtils.EXCEPTION_STACK_TRACE, Utils.getStackTrace(e));
            log.error(SlaUtils.EXCEPTION_MSG, e.getMessage());
            throw new BusinessException(SlaUtils.SOMETHING_WENT_WRONG);
        }
        return count;
    }



}
