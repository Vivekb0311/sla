package com.bootnext.platform.sla.service.impl;

import java.io.IOException;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.beans.factory.annotation.Autowired;
import com.bootnext.core.generic.exceptions.application.BusinessException;
import com.bootnext.core.generic.service.impl.AbstractService;
import com.bootnext.platform.notification.model.NotificationTemplate;
import com.bootnext.platform.notification.model.NotificationTemplateDetail;
import com.bootnext.platform.notification.rest.INotificationTemplateRest;
import com.bootnext.platform.sla.dao.ISlaLevelTemplateDao;
import com.bootnext.platform.sla.dao.ISlaTemplateDao;
import com.bootnext.platform.sla.model.template.SlaLevelTemplate;
import com.bootnext.platform.sla.model.template.SlaTemplate;
import com.bootnext.platform.sla.service.ISlaTemplateService;
import com.bootnext.platform.sla.utils.SlaUtils;
import com.bootnext.platform.utils.Utils;
import com.bootnext.platform.umapi.product.um.user.rest.UserRest;
import com.bootnext.platform.umapi.product.um.user.wrapper.UserContextWrappper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;

/**
 * Service implementation for managing Service Level Agreement (SLA) templates.
 * Extends the AbstractService class with Integer as the entity ID type and SlaTemplate as the entity type.
 *
 * This service provides methods for retrieving and manipulating SLA templates in the application.
 * It interacts with the data access layer and handles business logic related to SLA templates.
 *
 * The service is responsible for managing SLA templates, such as creating, updating, and deleting templates.
 * It communicates with the data access layer through the ISlaTemplateDao interface to perform database operations.
 *
 * SLA templates define the agreement between a service provider and a service consumer in terms of response times,
 * resolution times, and other performance indicators.
 */
@Service
@Slf4j
public class SlaTemplateServiceImpl extends AbstractService<Integer, SlaTemplate>
        implements ISlaTemplateService {

    @Autowired
    private ISlaTemplateDao slaTemplateDao;

    @Autowired
    private ISlaLevelTemplateDao slaLevelTemplateDao;

     @Autowired
    private INotificationTemplateRest notificationTemplateRest;
    
    @Autowired
    private UserRest userRest;


    @Autowired
    public void setDao(ISlaTemplateDao dao) {
        super.setDao(dao);
        slaTemplateDao = dao;
    }

    /**
     * Creates a new SlaTemplate and persists it in the database.
     * If the SlaTemplate with the same name already exists, it throws a BusinessException.
     *
     * @param slaTemplate The SlaTemplate object to be created.
     * @return The created SlaTemplate object.
     * @throws BusinessException If the SlaTemplate with the same name already exists, or an error occurs during the creation process.
     */
    @Override
    @Transactional
    public SlaTemplate createSLATemplate(SlaTemplate slaTemplate) {
        if (!(checkForSlaTemplateExists(slaTemplate.getName()) && (slaTemplate.getId() == null))) {
            try {
                int id = slaTemplate.getId() != null ? slaTemplate.getId() : 0;
                UserContextWrappper userInContext = userRest.userinfo();
                initializeSlaTemplate(slaTemplate, userInContext);
                slaTemplate.setApproval(SlaTemplate.APPROVAL.PENDING);
                slaTemplate.setIsExcludeNonWorkingDays(true);
                slaTemplateDao.create(slaTemplate);

                JSONArray levelJson = new JSONArray(slaTemplate.getLevelTemplate());
                processSlaLevelTemplates(id, levelJson, slaTemplate);
                return slaTemplate;
            } catch (Exception e) {
                log.error("Error Inside @class: SlaTemplateServiceImpl @Method :create() {}", e.getMessage(), e);
                throw new BusinessException(e.getMessage());
            }
        } else {
            throw new BusinessException(SlaUtils.SLA_ALREADY_EXIST);
        }
    }

    /**
     * Initializes the SlaTemplate object with default values and user context information.
     *
     * @param slaTemplate    The SlaTemplate object to be initialized.
     * @param userInContext  The UserContextWrappper object containing user context information.
     */
    private void initializeSlaTemplate(SlaTemplate slaTemplate, UserContextWrappper userInContext) {
        slaTemplate.setSlaId("" + new Date().getTime());
        slaTemplate.setStatus(true);
        slaTemplate.setCreator(userInContext.getFirstName() + " " + userInContext.getLastName());
        slaTemplate.setCreatorId(userInContext.getUserid());
        slaTemplate.setCreatedTime(new Date().getTime());
        slaTemplate.setLastModifier(userInContext.getFirstName() + " " + userInContext.getLastName());
        slaTemplate.setLastModifierId(userInContext.getUserid());
        slaTemplate.setModifiedTime(new Date().getTime());
        slaTemplate.setApproval(SlaTemplate.APPROVAL.PENDING);
    }

    /**
     * Processes the SlaLevelTemplates and sets their values based on the provided level JSON data.
     *
     * @param id          The ID of the SlaTemplate.
     * @param levelJson   The JSONArray containing level JSON data.
     * @param slaTemplate The SlaTemplate object to which the SlaLevelTemplates belong.
     */
    private void processSlaLevelTemplates(int id, JSONArray levelJson, SlaTemplate slaTemplate) {
        for (int i = 0; i < levelJson.length(); i++) {
            JSONObject levelObj = levelJson.getJSONObject(i);
            SlaLevelTemplate slaLevelTemplate = id == 0
                    ? new SlaLevelTemplate()
                    : slaLevelTemplateDao.getSlaLevelTemplateByIdAndLevel(slaTemplate.getId(), i + 1);
            setSlaLevelTemplate(i, slaTemplate, slaLevelTemplate, levelObj);
        }
    }

    /**
     * Sets properties for the SlaLevelTemplate entity based on the information provided in the JSONObject.
     *
     * @param index            The level index.
     * @param slaTemplate   The parent SLA template.
     * @param slaLevelTemplate The SlaLevelTemplate entity to be updated.
     * @param levelObj      The JSONObject containing properties for the SlaLevelTemplate.
     */
    private void setSlaLevelTemplate(int index, SlaTemplate slaTemplate, SlaLevelTemplate slaLevelTemplate, JSONObject levelObj){

        JSONArray jsonArray = levelObj.optJSONArray("selectedToUserRole");
        JSONObject jsonObject = levelObj.optJSONObject("notificationTemplate");
        String templateName = jsonObject.optString("name");
        JSONObject selectedToObject = levelObj.getJSONObject("selectedTo");
        slaLevelTemplate.setEmailTemplate(templateName);
        slaLevelTemplate.setDurationInMinute(levelObj.optString("breachDate"));
        slaLevelTemplate.setDuration(levelObj.optString("duration"));
        slaLevelTemplate.setEscalateWhom(selectedToObject.getString("value"));
        slaLevelTemplate.setSlaTemplate(slaTemplate);
        slaLevelTemplate.setLevel(index + 1);
        if(levelObj.optString("selectedEntityLevelOwner") != null){
            slaLevelTemplate.setEntityForOwner(levelObj.optString("selectedEntityLevelOwner"));
        }
        slaLevelTemplate.setEscalationTarget(jsonArray);
        if(levelObj.optBoolean("enableGeo", false)){
            slaLevelTemplate.setEntityForGeo(levelObj.optString("selectedEntityLevel"));
        }
        slaLevelTemplate.setWhenToEscalate(levelObj.optString("escalateWhen"));
        JSONObject templateConfig = getMailConfiguration(templateName);
        slaLevelTemplate.setTemplateConfiguration(String.valueOf(templateConfig));
        slaLevelTemplateDao.create(slaLevelTemplate); 
    }

    /**
     * Checks if an SLA template with the given name already exists in the database.
     *
     * @param name The name of the SLA template to be checked for existence.
     * @return true if an SLA template with the given name exists, false otherwise.
     */
    private boolean checkForSlaTemplateExists(String name){
        Boolean isExist = false;
        try{
            List<SlaTemplate> slaTemplate = slaTemplateDao.isTemplateExist(name);
            if(slaTemplate != null && ! slaTemplate.isEmpty()){
                isExist = true;
            }
        }catch(Exception e){
            log.error("Error while checking existing SLA Template: {} ", Utils.getStackTrace(e));
        }
        return isExist;
    }

    /**
     * Retrieves an SLA template based on its generated value.
     *
     * @param generatedValue The generated value of the SLA template to retrieve.
     * @return The retrieved SLA template if found, null otherwise.
     * @throws BusinessException If an error occurs while retrieving the SLA template.
     */
    @Override
    public SlaTemplate getSlaTemplateByGeneratedValue(String generatedValue) {
        try {
            log.info(SlaUtils.INSIDE_METHOD, "getSlaConfigurationByGeneratedValue");
            return slaTemplateDao.getSlaTemplateByGeneratedValue(generatedValue);
        } catch (Exception e) {
            log.error(SlaUtils.ERROR_OCCURRED, "getSlaTemplateByGeneratedValue");
            log.error(SlaUtils.EXCEPTION_STACK_TRACE, Utils.getStackTrace(e));
            log.error(SlaUtils.EXCEPTION_MSG, e.getMessage());
            throw new BusinessException(SlaUtils.SOMETHING_WENT_WRONG);
        }
    }

    /**
     * Retrieves a list of active SLA templates.
     *
     * @return A list of active SLA templates if found, an empty list otherwise.
     * @throws BusinessException If an error occurs while retrieving the active SLA templates.
     */
    @Override
    public List<SlaTemplate> getActiveSlaTemplates() {
        log.info(SlaUtils.INSIDE_METHOD, "getActiveSlaConfigurations");
        try {
            return slaTemplateDao.getActiveSlaTemplates();
        } catch (Exception e) {
            log.error(SlaUtils.ERROR_OCCURRED, "getActiveSlaTemplates");
            log.error(SlaUtils.EXCEPTION_MSG, e.getMessage());
            throw new BusinessException(SlaUtils.SOMETHING_WENT_WRONG);
        }
    }

    /**
     * Retrieves the counts for various types of SLA templates and returns them as a JSON object.
     *
     * @return A JSON object containing the counts for active, inactive, pending approval, and total SLA templates.
     * The JSON object has the following structure:
     * {
     *   "active": count_of_active_templates,
     *   "inActive": count_of_inactive_templates,
     *   "approval": count_of_templates_pending_approval,
     *   "total": toal_count_of_templates
     * }
     * @throws BusinessException If an error occurs while retrieving the counts for the SLA templates.
     */
    @Override
    public String getCountsForTemplate() {

        try{
            long active = slaTemplateDao.getTemplateCountByStatus("true");
            long inActive = slaTemplateDao.getTemplateCountByStatus("false");
            long approval = slaTemplateDao.getTemplateCountByApproval(SlaTemplate.APPROVAL.PENDING);
            long total = active + inActive;
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("active", active);
            jsonObject.put("inActive", inActive);
            jsonObject.put("approval", approval);
            jsonObject.put("total", total);

            return jsonObject.toString();
        }catch (Exception e) {
            log.error(SlaUtils.ERROR_OCCURRED, "getCountsForTemplate");
            log.error(SlaUtils.EXCEPTION_MSG, e.getMessage());
            throw new BusinessException(SlaUtils.SOMETHING_WENT_WRONG);
        }
    }

    /**
     * Retrieves SLA templates grouped by entity and returns the result as a JSON object.
     *
     * @return A JSON object containing SLA templates grouped by entity.
     * @throws BusinessException If an error occurs while retrieving the SLA templates.
     */
    @Override
    public String getTemplateEntityWise() {
        try{
            JSONObject jsonObject = slaTemplateDao.getTemplateEntityWise();
            return jsonObject.toString();
        }
        catch (Exception e) {
            log.error("Error Inside @class: SlaConfigurationServiceImpl @Method :getTemplateEntityWise() {}", e.getMessage(), e);
            throw new BusinessException(e.getMessage());
        }
    }

    /**
     * Updates an existing SLA template record in the database.
     *
     * @param slaTemplate The SLA template object containing the updated information.
     * @return The updated SLA template object after successful update.
     * @throws BusinessException If an error occurs while updating the SLA template.
     */
    @Override
    @Transactional
    public SlaTemplate update(SlaTemplate slaTemplate) {
        log.info("Update record by slaTemplate");
        try {
            slaTemplate.setModifiedTime(new Date().getTime());
            return slaTemplateDao.update(slaTemplate);
        } catch (Exception e) {
            log.error("Error Inside @class: SlaConfigurationServiceImpl @Method :update() {}", e.getMessage(), e);
            throw new BusinessException(e.getMessage());
        }
    }

    public JSONObject getMailConfiguration(String templateName ) {
        log.info("inside getMailConfiguration {}", templateName);
        JSONObject customContent = new JSONObject();
        try {
            NotificationTemplate notificationTemplate = notificationTemplateRest.templateByName(templateName);
            if(notificationTemplate != null){

            for (NotificationTemplateDetail notificationTemplateType : notificationTemplate.getTemplateType()) {
                JSONObject object = processNotificationTemplateType(notificationTemplateType);
                if (object != null) {
                    customContent.put(notificationTemplateType.getTemplatetype().toString(), object);
                }
            }
            }
            return customContent;
        } catch (Exception e) {
            log.error("Error Inside @class: SlaTemplateServiceImpl @Method :getMailConfiguration() {}", e.getMessage(), e);
                throw new BusinessException(e.getMessage());
        }
    }
 

    private JSONObject processNotificationTemplateType(NotificationTemplateDetail notificationTemplateType) {
        String content = notificationTemplateType.getContent();
        String subject = notificationTemplateType.getSubject();
        String config = notificationTemplateType.getConfigurations();
        JSONObject object = new JSONObject();
        object.put("content", content);
        object.put("subject", subject);
        object.put("config", config);
        return object;
    }

    @Override
    public String exportSla(String generatedValue) {
        try {
            SlaTemplate slaTemplate = slaTemplateDao.getSlaTemplateByGeneratedValue(generatedValue);
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.writeValueAsString(slaTemplate);
        } catch (IOException e) {
            log.error("Error Inside @class: SlaTemplateServiceImpl @Method :exportSla() {}", e.getMessage(), e);
                throw new BusinessException(e.getMessage());
        }

    }
    
    @Override
    public String exportSlaByApplication(String application){
        try{
            List<SlaTemplate> slaTemplate = slaTemplateDao.getSlaTemplateByApplication(application);
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.writeValueAsString(slaTemplate);
        }catch (IOException e) {
            log.error("Error Inside @class: SlaTemplateServiceImpl @Method :exportSlaByApplication() {}", e.getMessage(), e);
                throw new BusinessException(e.getMessage());
        }
    }
 

@Override

public List<SlaTemplate> importSla(MultipartFile file) throws BusinessException {
    try {
        if (file.isEmpty()) {
            throw new BusinessException("No file provided for import.");
        }
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode rootNode = objectMapper.readTree(file.getInputStream());

        if (rootNode.isArray()) {
            // If it's an array, parse it as a list of SlaTemplate objects
            List<SlaTemplate> slaTemplates = objectMapper.readValue(rootNode.traverse(), new TypeReference<List<SlaTemplate>>() {});
            for (SlaTemplate slaTemplate : slaTemplates) {
                slaTemplate.setId(null);
                createSLATemplate(slaTemplate);
            }
            return slaTemplates;
        } else if (rootNode.isObject()) {
            // If it's a single object, parse it as a single SlaTemplate object
            SlaTemplate slaTemplate = objectMapper.readValue(rootNode.traverse(), SlaTemplate.class);
            slaTemplate.setId(null);
            createSLATemplate(slaTemplate);
            return Collections.singletonList(slaTemplate);
        } else {
            throw new BusinessException("Invalid JSON format. Expected either a single object or an array.");
        }
    } catch (IOException e) {
        log.error("Error Inside @class: SlaTemplateServiceImpl @Method :importSla() {}", e.getMessage(), e);
        throw new BusinessException(e.getMessage());
    }
}

    
    
    
}
