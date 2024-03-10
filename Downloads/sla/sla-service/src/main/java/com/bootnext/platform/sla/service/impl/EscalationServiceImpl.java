package com.bootnext.platform.sla.service.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.bootnext.core.generic.exceptions.application.BusinessException;
import com.bootnext.core.generic.service.impl.AbstractService;
import com.bootnext.platform.notification.mail.rest.INotificationMailRest;
import com.bootnext.platform.notification.mail.wrapper.NotificationMailWrapper;
import com.bootnext.platform.sla.dao.IEscalationDao;
import com.bootnext.platform.sla.dao.ISlaHistoryDao;
import com.bootnext.platform.sla.model.template.Escalation;
import com.bootnext.platform.sla.model.template.SlaHistory;
import com.bootnext.platform.sla.service.IEscalationService;
import com.bootnext.platform.sla.utils.SlaUtils;

import lombok.extern.slf4j.Slf4j;


/**
 * Service implementation for managing escalation records.
 * Extends the AbstractService class with Integer as the entity ID type and Escalation as the entity type.
 *
 * This service provides methods for retrieving and manipulating escalation records in the application.
 * It interacts with the data access layer and handles business logic related to escalation records.
 *
 * The service is responsible for managing the escalation process, such as creating, updating, and deleting escalation rules.
 * It communicates with the data access layer through the IEscalationDao interface to perform database operations.
 */
@Service
@Slf4j
public class EscalationServiceImpl extends AbstractService<Integer, Escalation> implements IEscalationService {


    @Autowired
    private IEscalationDao escalationDao;

    @Autowired
    private ISlaHistoryDao slaTemplateHistoryDao;

    @Autowired
    private INotificationMailRest emailNotification;




    /**
     * Scheduled execution of escalations for SLA templates.
     * compares current date and time to specified Escalation time, 
     * if current date and time is greater than escalation time specified user is escalated.
     *
     * @return A string indicating the result of the escalation process.
     * @throws BusinessException If an error occurs while scheduling and executing escalations.
     */
    @Override
    public String scheduleEscalateUser() {
        log.info("inside ................... scheduleEscalateUser");
        String result = null;
        try{
            List<Escalation> escalations = escalationDao.getAllEscalations(false);
            for(Escalation escalation: escalations){

                Date escalateTime = new Date(escalation.getEscalationTime());
                Date currentTime = new Date();
                int comparison = escalateTime.compareTo(currentTime);
                SlaHistory slaHistory = escalation.getSlaHistory();
                if(comparison < 0 && (slaHistory.getState() == SlaHistory.STAGE.IN_PROGRESS)){
                escalation.setStatus(true);
                escalationDao.update(escalation);
                log.info("inside if condition after check, escalation.getSlaHistory {}", escalation.getSlaHistory());
                List<String> mailList = new ArrayList<>();
                JSONArray jsonArray = escalation.getWhomToEscalate();
                for (int i = 0; i < jsonArray.length(); i++) {
                String value = jsonArray.getString(i);
                mailList.add(value);
                }
                String mailTemplateName = escalation.getMailTemplateName();
                String mailConfig = escalation.getTemplateConfiguration();
                if(mailConfig != null){
                JSONObject object = new JSONObject(mailConfig);
                if(object.has(SlaUtils.EMAIL)){
                result = sendEmail(mailList, mailTemplateName, mailConfig);
                }
                }
                List<Escalation> currentEscalation = escalationDao.getEscalationBySlaHistory(slaHistory.getId());
                   log.info("currentEscalation.size() {}", currentEscalation.size());
                   if((currentEscalation.size() - 1) > slaHistory.getLevel()){
                       log.info("history.getLevel() {},  slaHistory{}", slaHistory.getLevel(), slaHistory );
                    Escalation escalation1 = escalationDao.getEscalationByLevelAndEntityId(slaHistory.getLevel()+ 1, slaHistory.getId());
                   slaHistory.setEscalateTime(escalation1.getEscalationTime());
                   slaHistory.setLevel(slaHistory.getLevel()+ 1);
                   slaTemplateHistoryDao.update(slaHistory);
               }
                }
            }
        }catch (Exception e) {
            log.error("Error Inside @class: EscalationServiceImpl @Method :scheduleEscalateUser() {}", e.getMessage(), e);
            throw new BusinessException(e.getMessage());
        }
        return result;
    }

    public static List<String> convertToList(String commaSeparatedString) {
        String[] emailsArray = commaSeparatedString.split(",");
        return Arrays.asList(emailsArray); 
    }

    /**
     * Send Email, this method composes the emailNotificationWrapper and calls sendInstantEmail method to send email to the specified user.
     * 
     * @param whomToEscalate gives the list of users to be escalated.
     * @param mailTemplateName the template to be used to generate email content.
     * @param mailConfig modified template content, contains subject and content.
     * 
     * @return response generated by sendInstantMail method 
     * @throws BusinessException If an error occurs while sending Email.
     */
    public String sendEmail(List<String> whomToEscalate, String mailTemplateName,  String mailConfig){

        log.info("inside send notification method, whom To Escalate {}", whomToEscalate);
        try{ 

            Set<String> ccUser = new HashSet<>(); 
            
        JSONObject configObject = new JSONObject(mailConfig);
        log.info("configObject {}", configObject);
        JSONObject emailContent = new JSONObject(configObject.optString("EMAIL"));
        String subject = emailContent.optString("subject");
        String content = emailContent.optString("content");
        log.info("contents {}, subject {}", emailContent, subject);
        NotificationMailWrapper emailNotificationWrapper = new NotificationMailWrapper();
        emailNotificationWrapper.setCcEmailIds(ccUser);
        emailNotificationWrapper.setBccEmailIds(ccUser);
        emailNotificationWrapper.setEmailContent(content);
        emailNotificationWrapper.setSubject(subject);
        emailNotificationWrapper.setFromEmail("info@bootnext.biz");
        emailNotificationWrapper.setTemplateName(mailTemplateName);
//        emailNotificationWrapper.setEmailNotificationType("EMAIL");
        emailNotificationWrapper.setToEmailIds(new HashSet<>(whomToEscalate));

        log.info("emailWrapper {}", emailNotificationWrapper);
        return sendInstantEmail(emailNotificationWrapper);

        }catch (Exception e) {
            log.error("Error Inside @class: EscalationServiceImpl @Method :sendEmail() {}", e.getMessage(), e);
            throw new BusinessException(e.getMessage());
        }
    }

    /**
     * sendInstantEmail 
     * 
     * @param emailNotificationWrapper, wrapper to send mail
     * @return result as response given by 'emailNotification.sendInstantEmail' method.
     * @throws BusinessException If an error occurs while sending Email.
     * 
     */
    public String sendInstantEmail(NotificationMailWrapper emailNotificationWrapper){
        String result = null;
        try
        {
        result = emailNotification.sendEmail(emailNotificationWrapper,true);
        log.info("result.......................................... {}", result);
        }catch(Exception e)
        {
            log.error("Error Inside @class: EscalationServiceImpl @Method :sendInstantEmail() {}", e.getMessage(), e);
            throw new BusinessException(e.getMessage());
        }
        return result;
    }

    /**
     * getEscalationByGeneratedValue
     * 
     * @param generatedValue
     * @return Escalation related to the specified generatedValue.
     * @throws BusinessException If an error occurs while retriving data.
     */
    @Override
    public List<Escalation> getEscalationByGeneratedValue(String generatedValue) {
        try {
             return escalationDao.getEscalationByGeneratedValue(generatedValue);
        } catch (Exception e) {
            log.error("Error Inside @class: EscalationServiceImpl @Method :getEscalationByGeneratedValue() {}", e.getMessage(), e);
            throw new BusinessException(e.getMessage());
        }
    }

    /**
     * Retrieves the top escalated SLA templates from the database.
     *
     * @return A JSON string representing the top escalated SLA templates.
     * @throws BusinessException If an error occurs while retrieving the escalated SLA templates.
     */
    @Override
    public String getTopSlaEscalated() {
        try{
            JSONArray jsonObject = escalationDao.getTopSlaEscalated();
            return jsonObject.toString();
        }catch (Exception e) {
            log.error("Error Inside @class: EscalationServiceImpl @Method :getTopSlaEscalated() {}", e.getMessage(), e);
            throw new BusinessException(e.getMessage());
        }
    }

}
