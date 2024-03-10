package com.bootnext.platform.sla.service.impl;

import java.text.SimpleDateFormat;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Service;

import com.bootnext.core.generic.exceptions.application.BusinessException;
import com.bootnext.core.generic.service.impl.AbstractService;
import com.bootnext.platform.customannotation.annotation.GenericAnnotation;
import com.bootnext.platform.notification.model.NotificationTemplate;
import com.bootnext.platform.notification.model.NotificationTemplateDetail;
import com.bootnext.platform.notification.rest.INotificationTemplateRest;
import com.bootnext.platform.sla.dao.IEscalationDao;
import com.bootnext.platform.sla.dao.ISlaHistoryDao;
import com.bootnext.platform.sla.dao.ISlaLevelTemplateDao;
import com.bootnext.platform.sla.dao.ISlaTemplateDao;
import com.bootnext.platform.sla.model.template.Escalation;
import com.bootnext.platform.sla.model.template.SlaHistory;
import com.bootnext.platform.sla.model.template.SlaLevelTemplate;
import com.bootnext.platform.sla.model.template.SlaTemplate;
import com.bootnext.platform.sla.service.ISlaHistoryService;
import com.bootnext.platform.sla.utils.SlaUtils;
import com.bootnext.platform.umapi.product.um.user.model.User;
import com.bootnext.platform.umapi.product.um.user.rest.UserRest;
import com.bootnext.platform.umapi.product.um.usergroup.model.UserGroup;
import com.bootnext.platform.umapi.product.um.usergroup.rest.UserBusinessUnitRest;
import com.bootnext.product.audit.utils.ActionType;
import com.bootnext.product.audit.utils.Auditable;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;

import lombok.extern.slf4j.Slf4j;

/**
 * Service implementation for managing SLA history records.
 * Extends the AbstractService class with Integer as the entity ID type and
 * SlaHistory as the entity type.
 *
 * This service provides methods for retrieving and manipulating SLA history
 * records in the application.
 * It interacts with the data access layer and handles business logic related to
 * SLA history records.
 */

@Service
@Slf4j
public class SlaHistoryServiceImpl extends AbstractService<Integer, SlaHistory>
        implements ISlaHistoryService {

    @Autowired
    private ISlaTemplateDao slaTemplateDao;
    @Autowired
    private ISlaLevelTemplateDao slaLevelTemplateDao;
    @Autowired
    private ISlaHistoryDao slaTemplateHistoryDao;
    @Autowired
    private IEscalationDao escalationDao;

    @Autowired
    private INotificationTemplateRest notificationTemplateRest;

    @Autowired
    private UserRest userRest;

    @Autowired
    private UserBusinessUnitRest userBusinessUnitRest;

    @Autowired
    public void setDao(ISlaHistoryDao dao) {
        super.setDao(dao);
        slaTemplateHistoryDao = dao;
    }

    /**
     * Checks if an SLA should be triggered for a given entity based on certain
     * conditions.
     * If condition matches then performs tasks specified in the template.
     *
     * @param entityString    The JSON representation of the entity.
     * @param applicationName The name of the application.
     * @param entityName      The name of the entity.
     * @param entityId        The ID of the entity.
     * @return True if the SLA is triggered, false otherwise.
     * @throws BusinessException If an error occurs while checking the SLA trigger
     *                           conditions.
     */

    @Override
    // @GenericAnnotation(actionType="UPDATE",annotationName = {"GlobleSearch"},
    // appName = "${APP_NAME}", entityName =
    // "SlaHistory",globalSearchData="application",searchTitle="name", uniqEntityId
    // = "id")
    // @Auditable(actionType = ActionType.CREATE, actionName = "CREATE")
    public Boolean triggerSLA(String entityString, String applicationName, String entityName, String entityId) {
        log.info("entityString {}, applicationName {}, entityName {}, entityId {}", entityString, applicationName,
                entityName, entityId);
        boolean matched = false;
        try {
            JSONObject entity = new JSONObject(entityString);
            List<SlaTemplate> slaTemplates = slaTemplateDao.getSlaTemplateByApplicationAndExecutedOn(applicationName,
                    entityName);

            for (SlaTemplate slaTemplate : slaTemplates) {
                SlaHistory history = slaTemplateHistoryDao.findSlaHistoryByEntityIdAppNameAndEntityName(entityId,
                        applicationName, entityName, slaTemplate.getSlaId());

                if (history != null) {
                    if (history.getState().equals(SlaHistory.STAGE.IN_PROGRESS)
                            && (isOnHoldConditionApply(slaTemplate, entity, history)
                                    || isStopConditionApply(slaTemplate, entity, history)
                                    || isCancelConditionApply(slaTemplate, entity, history)
                                    || isResetConditionApply(slaTemplate, entity, history))) {
                        matched = true;
                    } else if (history.getState().equals(SlaHistory.STAGE.ON_HOLD)
                            && (isStopConditionApply(slaTemplate, entity, history)
                                    || isResumeConditionApply(slaTemplate, entity, history, entityId)
                                    || isCancelConditionApply(slaTemplate, entity, history)
                                    || isResetConditionApply(slaTemplate, entity, history))) {
                        matched = true;
                    }
                } else if (isStartConditionApply(slaTemplate, entity, entityId)) {
                    matched = true;
                }
            }
        } catch (Exception e) {
            log.error("Error Inside @class: SlaHistoryServiceImpl @Method :triggerSLA() {}", e.getMessage(), e);
            throw new BusinessException(e.getMessage());
        }
        return matched;
    }

    /**
     * Checks if the start conditions for a given SLA template are met for a
     * specific entity.
     * If the condition is satisfied it creates new SLA History.
     *
     * @param slaTemplate The SLA template to check.
     * @param entity      The JSON representation of the entity.
     * @param entityId    The ID of the entity.
     * @return True if the start conditions are met, false otherwise.
     * @throws Exception If an error occurs while checking the start conditions.
     */
    public boolean isStartConditionApply(SlaTemplate slaTemplate, JSONObject entity, String entityId) throws Exception {
        boolean matched = false;
        JSONObject slaObject = new JSONObject(slaTemplate.getStartCondition());
        StringBuilder expressionBuilder = new StringBuilder();
        matched = checkRule(slaTemplate, entity, slaObject, expressionBuilder);

        log.info("matched in isStartConditionApply {}, startCondition===== {}", matched, slaObject);
        if (matched) {
            matched = createSlaHistory(slaTemplate, entityId, entity, expressionBuilder);
        }
        return matched;
    }

    /**
     * Checks if the OnHold conditions for a given SLA template are met for a
     * specific entity.
     * If the condition is satisfied it Changes the state of SLA to ON_Hold.
     *
     * @param slaTemplate The SLA template to check.
     * @param entity      The JSON representation of the entity.
     * @param history     The SLA History to set required fields.
     * @return True if the onHold conditions are met, false otherwise.
     * @throws Exception If an error occurs while checking the onHold conditions.
     */
    public boolean isOnHoldConditionApply(SlaTemplate slaTemplate, JSONObject entity, SlaHistory history)
            throws Exception {
        log.info("inside isOnHoldConditionApply {}", history.getId());
        boolean matched = false;
        JSONObject slaObject = new JSONObject(slaTemplate.getOnHoldCondition());
        StringBuilder expressionBuilder = new StringBuilder();
        if (slaObject.length() > 0)
            matched = checkRule(slaTemplate, entity, slaObject, expressionBuilder);
        if (matched) {
            JSONObject modifier = entity.optJSONObject("lastModifier");
            history.setLastModifier(modifier.optString("userName"));
            history.setState(SlaHistory.STAGE.ON_HOLD);
            history.setModifiedTime(new Date().getTime());
            history.setSpelExpression(expressionBuilder.toString());
            slaTemplateHistoryDao.update(history);
        }
        return matched;
    }

    /**
     * Checks if the stop conditions for a given SLA template are met for a specific
     * entity.
     * If the condition is satisfied it Changes the state of SLA to COMPLETED.
     *
     * @param slaTemplate The SLA template to check.
     * @param entity      The JSON representation of the entity.
     * @param history     The SLA History to set required fields.
     * @return True if the stop conditions are met, false otherwise.
     * @throws Exception If an error occurs while checking the stop conditions.
     */
    public boolean isStopConditionApply(SlaTemplate slaTemplate, JSONObject entity, SlaHistory history)
            throws Exception {
        log.info("inside isStopConditionApply {}", history.getId());
        boolean matched = false;
        JSONObject slaObject = new JSONObject(slaTemplate.getStopCondition());
        StringBuilder expressionBuilder = new StringBuilder();
        if (slaObject.length() > 0)
            matched = checkRule(slaTemplate, entity, slaObject, expressionBuilder);
        if (matched) {
            JSONObject modifier = entity.optJSONObject("lastModifier");
            history.setLastModifier(modifier.optString("userName"));
            history.setState(SlaHistory.STAGE.COMPLETED);
            history.setModifiedTime(new Date().getTime());
            history.setSpelExpression(expressionBuilder.toString());
            slaTemplateHistoryDao.update(history);
        }
        return matched;
    }

    /**
     * Checks if the cancel conditions for a given SLA template are met for a
     * specific entity.
     * If the condition is satisfied it Changes the state of SLA to CANCELLED.
     *
     * @param slaTemplate The SLA template to check.
     * @param entity      The JSON representation of the entity.
     * @param history     The SLA History to set required fields.
     * @return True if the cancel conditions are met, false otherwise.
     * @throws Exception If an error occurs while checking the cancel conditions.
     */
    public boolean isCancelConditionApply(SlaTemplate slaTemplate, JSONObject entity, SlaHistory history)
            throws Exception {
        log.info("inside isCancelConditionApply {}", history.getId());
        boolean matched = false;
        JSONObject slaObject = new JSONObject(slaTemplate.getCancelCondition());
        StringBuilder expressionBuilder = new StringBuilder();
        if (slaObject.length() > 0)
            matched = checkRule(slaTemplate, entity, slaObject, expressionBuilder);
        if (matched) {
            JSONObject modifier = entity.optJSONObject("lastModifier");
            history.setLastModifier(modifier.optString("userName"));
            history.setState(SlaHistory.STAGE.CANCELLED);
            history.setSpelExpression(expressionBuilder.toString());
            history.setModifiedTime(new Date().getTime());
            slaTemplateHistoryDao.update(history);
        }
        return matched;
    }

    /**
     * Checks if the resume conditions for a given SLA template are met for a
     * specific entity.
     * If the condition is satisfied it Changes the state of SLA from ON_HOLD TO
     * IN_PROGRESS.
     * calculates the time difference till which SLA was on ON_HOLD condition and
     * futher adds
     * it to breach time and escalation time respectively.
     * 
     * @param slaTemplate The SLA template to check.
     * @param entity      The JSON representation of the entity.
     * @param entityId    The ID of the entity.
     * @param history     The SLA History to set required fields.
     * @return True if the resume conditions are met, false otherwise.
     * @throws Exception If an error occurs while checking the resume conditions.
     */
    public boolean isResumeConditionApply(SlaTemplate slaTemplate, JSONObject entity, SlaHistory history,
            String entityId) throws Exception {
        log.info("inside isResumeConditionApply {}", history.getId());
        boolean matched = false;
        JSONObject slaObject = new JSONObject(slaTemplate.getStartCondition());

        JSONObject inTime = new JSONObject(slaTemplate.getInTime());
        JSONObject outTime = new JSONObject(slaTemplate.getOutTime());
        StringBuilder expressionBuilder = new StringBuilder();
        if (slaObject.length() > 0)
            matched = checkRule(slaTemplate, entity, slaObject, expressionBuilder);
        if (matched) {

            long timeToAdd = new Date().getTime() - history.getModifiedTime();
            long diffInMinutes = TimeUnit.MILLISECONDS.toMinutes(timeToAdd);
            long oldBreachDateInMilli = history.getBreachTime();
            long oldEscalateDateInMilli = history.getEscalateTime();
            String timeZone = history.getTimeZone();
            ZoneId zoneId = ZoneId.of(timeZone);
            ZonedDateTime oldBreachDate = ZonedDateTime.ofInstant(Instant.ofEpochMilli(oldBreachDateInMilli), zoneId);
            ZonedDateTime oldEscalateDate = ZonedDateTime.ofInstant(Instant.ofEpochMilli(oldEscalateDateInMilli),
                    zoneId);
            boolean isExcludingNonWorkingDay = history.getIsExcludeNonWorkingDays();
            String operationalHours = history.getOperationalHours();

            ZonedDateTime newBreachDate = addTimeExcludingWeekendsAndWorkingHours(oldBreachDate, diffInMinutes, "add",
                    isExcludingNonWorkingDay, operationalHours, timeZone, inTime, outTime);
            boolean isExcludingNonWorkingDayLevel = true;
            ZonedDateTime newEscalateDate = addTimeExcludingWeekendsAndWorkingHours(oldEscalateDate, diffInMinutes,
                    "add", isExcludingNonWorkingDayLevel, operationalHours, timeZone, inTime, outTime);

            long millisnewBreachDate = newBreachDate.toInstant().toEpochMilli();
            long millisNewEscalateDate = newEscalateDate.toInstant().toEpochMilli();

            JSONObject modifier = entity.optJSONObject("lastModifier");
            history.setLastModifier(modifier.optString("userName"));
            history.setEscalateTime(millisNewEscalateDate);
            history.setBreachTime(millisnewBreachDate);
            history.setState(SlaHistory.STAGE.IN_PROGRESS);
            history.setSpelExpression(expressionBuilder.toString());
            history.setModifiedTime(new Date().getTime());
            slaTemplateHistoryDao.update(history);

            List<Escalation> escalation = escalationDao.getEscalationByEntityId(entityId);
            for (Escalation escalation1 : escalation) {

                ZonedDateTime escalateDate = ZonedDateTime
                        .ofInstant(Instant.ofEpochMilli(escalation1.getEscalationTime()), zoneId);
                ZonedDateTime newDate = addTimeExcludingWeekendsAndWorkingHours(escalateDate, diffInMinutes, "add",
                        isExcludingNonWorkingDayLevel, operationalHours, timeZone, inTime, outTime);
                escalation1.setEscalationTime(newDate.toInstant().toEpochMilli());
                escalationDao.update(escalation1);
            }
        }
        return matched;
    }

    /**
     * Checks if the reset conditions for a given SLA template are met for a
     * specific entity.
     * If the condition is satisfied it resets all the calculations and parameter to
     * the new one.
     *
     * @param slaTemplate The SLA template to check.
     * @param entity      The JSON representation of the entity.
     * @param history     The SLA History to set required fields.
     * @return True if the reset conditions are met, false otherwise.
     * @throws Exception If an error occurs while checking the reset conditions.
     */
    public boolean isResetConditionApply(SlaTemplate slaTemplate, JSONObject entity, SlaHistory history)
            throws Exception {
        log.info("inside isResetConditionApply {}", history.getId());
        JSONObject inTime = new JSONObject(slaTemplate.getInTime());
        JSONObject outTime = new JSONObject(slaTemplate.getOutTime());
        boolean matched = false;
        JSONObject slaObject = new JSONObject(slaTemplate.getResetCondition());
        StringBuilder expressionBuilder = new StringBuilder();
        if (slaObject.length() > 0)
            matched = checkRule(slaTemplate, entity, slaObject, expressionBuilder);
        if (matched) {
            long timeInMinute = Integer.parseInt(slaTemplate.getBreachDate());
            boolean isExcludingNonWorkingDay = slaTemplate.getIsExcludeNonWorkingDays();
            String operationalHours = slaTemplate.getOperationalHours();
            String timeZone = history.getTimeZone();
            int id = slaTemplate.getId();
            List<SlaLevelTemplate> slaLevelTemplates = slaLevelTemplateDao.getSlaLevelTemplateById(id);
            log.info("slaLevelTemplates {}", slaLevelTemplates);
            SlaLevelTemplate slaLevelTemplate = slaLevelTemplateDao.getSlaLevelTemplateByIdAndLevel(slaTemplate.getId(),
                    1);
            int amountToAdd = Integer.parseInt(slaLevelTemplate.getDurationInMinute());
            String whenToEscalate = slaLevelTemplate.getWhenToEscalate();
            boolean isExcludingNonWorkingDayLevel = true;
            String entityId = history.getEntityIdentifier();
            ZoneId zoneId = ZoneId.of(timeZone);
            ZonedDateTime currentDateTime = ZonedDateTime.now(zoneId);
            ZonedDateTime newDateTime = addTimeExcludingWeekendsAndWorkingHours(currentDateTime, timeInMinute, "add",
                    isExcludingNonWorkingDay, operationalHours, timeZone, inTime, outTime);

            ZonedDateTime escalateTime = calculateEscalationTime(newDateTime, amountToAdd, whenToEscalate,
                    isExcludingNonWorkingDayLevel, history.getId(), timeZone, slaTemplate);
            JSONObject modifier = entity.optJSONObject("lastModifier");
            history.setLastModifier(modifier.optString("userName"));
            history.setBreachTime(newDateTime.toInstant().toEpochMilli());
            history.setLevel(1);
            history.setSpelExpression(expressionBuilder.toString());
            history.setEscalateTime(escalateTime.toInstant().toEpochMilli());
            history.setState(SlaHistory.STAGE.IN_PROGRESS);
            history.setBreachStatus(false);
            slaTemplateHistoryDao.update(history);

            updateEscalationForReset(entityId, history, newDateTime, isExcludingNonWorkingDayLevel, slaTemplate);
        }
        return matched;
    }

    /**
     * Updates the escalation details for a given entity and SLA history when the
     * SLA is reset.
     *
     * @param entityId                      The ID of the entity.
     * @param history                       The SLA history to update.
     * @param newDateTime                   The new date and time for the reset SLA.
     * @param isExcludingNonWorkingDayLevel True if non-working days are excluded at
     *                                      the level, false otherwise.
     * @param slaTemplate                   The SLA template associated with the
     *                                      entity and history.
     */
    public void updateEscalationForReset(String entityId, SlaHistory history, ZonedDateTime newDateTime,
            boolean isExcludingNonWorkingDayLevel, SlaTemplate slaTemplate) {
        List<Escalation> escalation = escalationDao.getEscalationByEntityId(entityId);
        for (Escalation escalation1 : escalation) {
            int i = 0;
            JSONArray levelJson = new JSONArray(history.getLevelTemplate());
            String timeZone = history.getTimeZone();
            JSONObject levelObj = levelJson.getJSONObject(i);
            long amountToAddLevel = levelObj.getLong(SlaUtils.BREACH_DATE);
            String whenToEscalateLevel = levelObj.optString(SlaUtils.ESCALATE_WHEN);
            ZonedDateTime escalationTime = calculateEscalationTime(newDateTime, amountToAddLevel, whenToEscalateLevel,
                    isExcludingNonWorkingDayLevel, history.getId(), timeZone, slaTemplate);
            escalation1.setBreachTime(newDateTime.toInstant().toEpochMilli());
            escalation1.setEscalationTime(escalationTime.toInstant().toEpochMilli());
            escalationDao.update(escalation1);
        }
    }

    /**
     * Evaluates the rules defined in the SLA template against the provided entity
     * data using SpEL (Spring Expression Language).
     *
     * @param slaTemplate The SLA template against which the rules are defined.
     * @param entity      The JSON object representing the entity data.
     * @param slaObject   The array of SLA conditions to evaluate.
     * @return True if the rules are matched, false otherwise.
     */
    public boolean checkRule(SlaTemplate slaTemplate, JSONObject entity, JSONObject slaObject,
            StringBuilder finalExpression) {
        try {
            ExpressionParser parser = new SpelExpressionParser();
            StandardEvaluationContext context = new StandardEvaluationContext();
            StringBuilder expressionBuilder = new StringBuilder();
            JSONArray rulesArray = slaObject.getJSONArray(SlaUtils.RULES);
            iterateRules(rulesArray, slaObject.optString("condition", "N/A"), expressionBuilder, entity,
                    finalExpression);
            String expression = expressionBuilder.toString();
            log.info("expression {}", expression);
            boolean matched = false;
            if (expression != null || expression.equals("")) {
                matched = parser.parseExpression(expression.trim()).getValue(context, Boolean.class);
            }
            log.info("final expression {}, matched {}", expression, matched);
            return matched;
        } catch (Exception e) {
            log.error("Error Inside @class: SlaHistoryServiceImpl @Method :checkRule() {}", e.getMessage(), e);
            throw new BusinessException(e.getMessage());
        }
    }

    /**
     * Recursive method to iterate through the rules defined in the SLA template and
     * construct the SpEL expression.
     *
     * @param rules             The JSON array containing the rules to iterate.
     * @param parentCondition   The parent condition to apply between nested rules.
     * @param expressionBuilder The StringBuilder to build the SpEL expression.
     * @param entity            The JSON object representing the entity data used to
     *                          evaluate the rules.
     */

    public void iterateRules(JSONArray rules, String parentCondition, StringBuilder expressionBuilder,
            JSONObject entity, StringBuilder finalExpression) {
        try {
            for (int i = 0; i < rules.length(); i++) {
                JSONObject rule = rules.getJSONObject(i);
                if (rule.has("rules")) {
                    JSONArray nestedRules = rule.getJSONArray("rules");
                    String nestedCondition = rule.optString("condition", "N/A");
                    expressionBuilder.append("(");
                    finalExpression.append("(");
                    iterateRules(nestedRules, nestedCondition, expressionBuilder, entity, finalExpression);
                    expressionBuilder.append(")");
                    finalExpression.append(")");
                } else {
                    finalExpression.append("(");
                    expressionBuilder.append("(").append(generateExpression(rule, entity, finalExpression)).append(")");
                    finalExpression.append(")");
                }
                if (i < rules.length() - 1) {
                    expressionBuilder.append(parentCondition).append(" ");
                    finalExpression.append(parentCondition).append(" ");
                }
            }
        } catch (Exception e) {
            log.error("Error Inside @class: SlaHistoryServiceImpl @Method :iterateRules() {}", e.getMessage(), e);
            throw new BusinessException(e.getMessage());
        }
    }

    /**
     * Generates the SpEL expression for a single rule defined in the SLA template.
     *
     * @param rule   The JSON object representing a single rule.
     * @param entity The JSON object representing the entity data used to evaluate
     *               the rule.
     * @return The SpEL expression corresponding to the given rule and entity data.
     */
    public String generateExpression(JSONObject rule, JSONObject entity, StringBuilder finalExpression) {
        log.info("rule======={}", rule);

        if (!rule.has(SlaUtils.FIELDS)) {
            log.info("came to check fields ==============");
            return SlaUtils.FALSE;
        }
        if ((rule.get("operators") instanceof String) || !(rule.get("value") instanceof String)
                || (rule.get(SlaUtils.FIELDS) instanceof String)) {
            return SlaUtils.FALSE;
        }

        ExpressionParser parser = new SpelExpressionParser();
        StandardEvaluationContext context = new StandardEvaluationContext();
        JSONObject operators = rule.getJSONObject("operators");
        String operator = operators.optString(SlaUtils.VALUE);
        String slaValue = rule.optString(SlaUtils.VALUE);
        StringBuilder expressionBuilder = new StringBuilder();
        String json = String.valueOf(entity);
        JSONObject fields = rule.getJSONObject(SlaUtils.FIELDS);
        String jsonPathExpression = "/" + fields.getString("name").replace(".", "/");
        log.info("jsonPathExpression", jsonPathExpression);
        System.out.println(jsonPathExpression);
        String valueAtPath = getValueByPath(entity, jsonPathExpression);
        Expression expressionForObject = parser.parseExpression("#jsonPath.read(#root, '" + jsonPathExpression + "')");
        context.setVariable("jsonPath", new JsonPathAccessor());
        String objectEntityValueRaw = getValueByPath(entity, jsonPathExpression);
        String objectEntityValue = htmlToText(objectEntityValueRaw);
        log.info("jsonPathExpression {}, expressionForObject {},  objectEntityValue {}", jsonPathExpression,
                expressionForObject, objectEntityValue);
        log.info("operator {}, value {}, field {}, objectEntityValue {}", operator, slaValue, fields.getString("name"),
                objectEntityValue);
        finalExpression.append(" key: " + fields.getString("name") + ", condition: " + operator + ", value: "
                + objectEntityValue + " ");
        switch (operator) {
            case "==", ">", "<", "!=", "<=", ">=":
                if (objectEntityValueRaw == null || "".equals(objectEntityValueRaw)) {
                    return SlaUtils.FALSE;
                }
                expressionBuilder.append("").append(Integer.parseInt(objectEntityValue)).append(" ").append(operator)
                        .append(" ").append(Integer.parseInt(slaValue)).append(" ");
                break;
            case "Is_Null":
                expressionBuilder.append(objectEntityValue).append("== null")
                        .append(" ");
                break;
            case "Is_Not_Null":
                expressionBuilder.append("'").append(objectEntityValue).append("' ").append("!= null")
                        .append(" ");
                break;
            case "Is_Empty":
                expressionBuilder.append("'").append(objectEntityValue).append("' ").append("==")
                        .append("''").append(" ");
                break;
            case "Is_Not_Empty":
                expressionBuilder.append("'").append(objectEntityValue).append("' ").append("!=")
                        .append("''").append(" ");
                break;
            case "Is_Equals":
                expressionBuilder.append("'").append(objectEntityValue).append("'.").append("equalsIgnoreCase")
                        .append("('")
                        .append(slaValue).append("') ").append(" ");
                break;
            case "Is_Not_Equals":
                expressionBuilder.append("!('").append(objectEntityValue).append("'.").append("equalsIgnoreCase")
                        .append("('")
                        .append(slaValue).append("')) ").append(" ");
                break;
            case "Starts_With":
                expressionBuilder.append("'").append(objectEntityValue).append("'.").append("startsWith").append("('")
                        .append(slaValue)
                        .append("') ").append(" ");
                break;
            case "Ends_With":
                expressionBuilder.append("'").append(objectEntityValue).append("'.").append("endsWith").append("('")
                        .append(slaValue)
                        .append("') ").append(" ");
                break;
            case "Contains":
                expressionBuilder.append("'").append(objectEntityValue).append("'.").append("contains").append("('")
                        .append(slaValue)
                        .append("') ").append(" ");
                break;
            case "Does_Not_Contains":
                expressionBuilder.append("!('").append(objectEntityValue).append("'.").append("contains").append("('")
                        .append(slaValue)
                        .append("')) ").append(" ");
                break;
            case "Is_Before":
                JSONObject obj = new JSONObject(slaValue);

                if (compareDates(objectEntityValue, rule, "before")) {
                    expressionBuilder.append(SlaUtils.TRUE).append(" ");
                } else {
                    expressionBuilder.append(SlaUtils.FALSE).append(" ");
                }
                break;
            case "Is_After":
                if (compareDates(objectEntityValue, rule, "after")) {
                    expressionBuilder.append(SlaUtils.TRUE).append(" ");
                } else {
                    expressionBuilder.append(SlaUtils.FALSE).append(" ");
                }
                break;
            case "Is_Between":
                expressionBuilder.append(isDateInBtw(objectEntityValue, rule));
                break;

            default:
                expressionBuilder.append("'").append(objectEntityValue).append("'.").append("('").append(slaValue)
                        .append("') ").append(operator).append(" ");

                break;
        }
        return expressionBuilder.toString();
    }

    private String getValueByPath(JSONObject jsonObject, String path) {
        // Split the path by '/'
        String[] keys = path.split("/");

        // Traverse the JSON object based on the path
        JSONObject currentObj = jsonObject;
        for (String key : keys) {
            // Check if key is not empty (as the split method creates an empty string at the
            // beginning due to leading '/')
            if (!key.isEmpty()) {
                // Get the value corresponding to the current key
                Object value = currentObj.get(key);

                // Check if the value is a JSONObject for further traversal
                if (value instanceof JSONObject) {
                    currentObj = (JSONObject) value; // Move to the next JSON object
                } else {
                    return value.toString(); // Return the value when found
                }
            }
        }

        return null; // Return null if path not found
    }

    private String htmlToText(String html) {
        log.info("inside htmlToText =============={}", html);
        StringBuilder plainText = new StringBuilder();
        boolean insideTag = false;

        for (char c : html.toCharArray()) {
            if (c == '<') {
                insideTag = true;
            } else if (c == '>') {
                insideTag = false;
            } else if (!insideTag) {
                plainText.append(c);
            }
        }

        return plainText.toString();
    }

    private boolean compareDates(String objectEntityValue, JSONObject rule, String condition) {

        long hours = rule.getLong(SlaUtils.HOURS);
        long minutes = rule.getLong(SlaUtils.MINUTES);

        Instant instant = Instant.ofEpochMilli(Long.parseLong(objectEntityValue));
        LocalDateTime dateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault());

        LocalDateTime now = LocalDateTime.now();
        if (condition.equals("after")) {
            now = now.plusHours(hours);
            now = now.plusMinutes(minutes);
            if (now.compareTo(dateTime) > 0) {
                return true;
            }
        } else {
            now = now.minusHours(hours);
            now = now.minusMinutes(minutes);
            if (now.compareTo(dateTime) < 0) {
                return true;
            }
        }

        return false;

    }

    class JsonPathAccessor {
        public String read(String json, String path) {
            ObjectMapper objectMapper = new ObjectMapper();
            try {
                JsonNode rootNode = objectMapper.readTree(json);
                JsonNode valueNode = rootNode.at(path);
                return valueNode.asText();
            } catch (Exception e) {
                log.error("Error Inside @class: SlaHistoryServiceImpl {}", e.getMessage(), e);
                throw new BusinessException(e.getMessage());
            }
        }
    }

    /**
     * Converts a date string from the given input format to the format
     * "yyyy-MM-dd".
     *
     * @param inputDate   The date string to be converted.
     * @param inputFormat The format of the input date string.
     * @return The converted date string in the format "yyyy-MM-dd".
     */
    public static String convertToYYYYMMDD(String inputDate, String inputFormat) {
        DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern(inputFormat);
        LocalDateTime localDateTime = LocalDateTime.parse(inputDate, inputFormatter);

        OffsetDateTime offsetDateTime = OffsetDateTime.of(localDateTime, ZoneOffset.UTC);

        DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        return offsetDateTime.format(outputFormatter);
    }

    public boolean isDateInBtw(String objValue, JSONObject rule) {

        long currentTimeMillis = System.currentTimeMillis();
        Date currentDate = new Date(currentTimeMillis);

        Date givenDate = new Date(Long.parseLong(objValue) / 1000);

        long timeDiffMillis = currentDate.getTime() - givenDate.getTime();

        long hours = TimeUnit.MILLISECONDS.toHours(timeDiffMillis);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(timeDiffMillis) % 60;

        long finalTime = hours * 60 + minutes;
        long firstTimeInMin = rule.getLong("hours") * 60 + rule.getLong("minute");
        long secondTimeInMin = rule.getLong("hoursData") * 60 + rule.getLong("minutesData");

        long diff = firstTimeInMin - secondTimeInMin;
        return (diff > finalTime);
    }

    /**
     * Creates a new SlaHistory record and associated Escalation records based on
     * the provided SlaTemplate,
     * entityId, and entity data.
     *
     * @param slaTemplate The SlaTemplate object to create SlaHistory and Escalation
     *                    records from.
     * @param entityId    The ID of the entity for which the SLA history is created.
     * @param entity      The JSON representation of the entity.
     * @return True if the SlaHistory and Escalation records are successfully
     *         created, otherwise false.
     */
    public boolean createSlaHistory(SlaTemplate slaTemplate, String entityId, JSONObject entity,
            StringBuilder expressionBuilder) {
        log.info("inside createSlaHistory");

        try {
            log.info("inside createSlaHistory");

            JSONObject inTime = new JSONObject(slaTemplate.getInTime());
            JSONObject outTime = new JSONObject(slaTemplate.getOutTime());
            log.info("inTime {}, outTime {}", inTime, outTime);

            int timeInMinute = Integer.parseInt(slaTemplate.getBreachDate());
            String timeZone = slaTemplate.getTimeZoneData();
            boolean isExcludingNonWorkingDay = slaTemplate.getIsExcludeNonWorkingDays();
            String operationalHours = slaTemplate.getOperationalHours();
            ZonedDateTime currentDateTime = ZonedDateTime.now(ZoneId.of(timeZone));
            log.info("currentDateTime {}", currentDateTime);

            JSONArray levelJson = new JSONArray(slaTemplate.getLevelTemplate());
            JSONObject levelJsonObj = levelJson.getJSONObject(0);
            log.info("levelJsonObj {}", levelJsonObj);
            long amountToAdd = levelJsonObj.getLong("breachDate");
            String whenToEscalate = levelJsonObj.optString("escalateWhen");
            boolean isExcludingNonWorkingDayLevel = true;

            ZonedDateTime breachTime = addTimeExcludingWeekendsAndWorkingHours(currentDateTime, timeInMinute, "add",
                    isExcludingNonWorkingDay, operationalHours, timeZone, inTime, outTime);
            log.info("newDateTime {}", breachTime);
            ZonedDateTime escalateTime = calculateEscalationTime(breachTime, amountToAdd, whenToEscalate,
                    isExcludingNonWorkingDayLevel, 0, timeZone, slaTemplate);
            log.info("escalateTime {}", escalateTime);

            JSONObject creator = entity.optJSONObject("creator");
            JSONObject modifier = entity.optJSONObject("lastModifier");

            SlaHistory slaHistory = new SlaHistory();
            slaHistory.setState(SlaHistory.STAGE.IN_PROGRESS);
            slaHistory.setEntityIdentifier(entityId);
            slaHistory.setApplication(slaTemplate.getApplication());
            slaHistory.setCreatedTime(new Date().getTime());
            slaHistory.setModifiedTime(new Date().getTime());
            slaHistory.setSlaIdentifier(slaTemplate.getSlaId());
            slaHistory.setBreachStatus(false);
            slaHistory.setStatus(true);
            slaHistory.setBreachTime(breachTime.toInstant().toEpochMilli());
            slaHistory.setExecutedOn(slaTemplate.getExecutedOn());
            slaHistory.setIsExcludeNonWorkingDays(slaTemplate.getIsExcludeNonWorkingDays());
            slaHistory.setStartCondition(slaTemplate.getStartCondition());
            slaHistory.setCancelCondition(slaTemplate.getCancelCondition());
            slaHistory.setOnHoldCondition(slaTemplate.getOnHoldCondition());
            slaHistory.setResumeCondition(slaTemplate.getResumeCondition());
            slaHistory.setResetCondition(slaTemplate.getResetCondition());
            slaHistory.setStopCondition(slaTemplate.getStopCondition());
            slaHistory.setLevelTemplate(slaTemplate.getLevelTemplate());
            slaHistory.setEscalateTime(escalateTime.toInstant().toEpochMilli());
            slaHistory.setLevel(1);
            slaHistory.setSlaId("SLA-" + slaTemplate.getId());
            slaHistory.setSpelExpression(expressionBuilder.toString());
            slaHistory.setInTime(slaTemplate.getInTime());
            slaHistory.setOutTime(slaTemplate.getOutTime());
            slaHistory.setOperationalHours(slaTemplate.getOperationalHours());
            slaHistory.setOwner(entity.optString("user"));
            slaHistory.setTimeZone(slaTemplate.getTimeZoneData());
            slaHistory.setCreator(creator.optString("userName"));
            slaHistory.setLastModifier(modifier.optString("userName"));
            slaTemplateHistoryDao.create(slaHistory);

            int id = slaTemplate.getId();
            log.info("sla_template_id {}", id);
            List<SlaLevelTemplate> slaLevelTemplates = slaLevelTemplateDao.getSlaLevelTemplateById(id);

            for (SlaLevelTemplate slaLevelTemplate : slaLevelTemplates) {
                String templateName = slaLevelTemplate.getEmailTemplate();
                long amountToAddLevel = Long.parseLong(slaLevelTemplate.getDurationInMinute());
                String whenToEscalateLevel = slaLevelTemplate.getWhenToEscalate();
                ZonedDateTime escalationTime = calculateEscalationTime(breachTime, amountToAddLevel,
                        whenToEscalateLevel, isExcludingNonWorkingDayLevel, slaHistory.getId(), timeZone, slaTemplate);
                long escalationTimeInTimeStamp = escalationTime.toInstant().toEpochMilli();
                log.info("template config for escalation {}", slaLevelTemplate.getTemplateConfiguration());
                JSONObject mailContent = new JSONObject(slaLevelTemplate.getTemplateConfiguration());
                Escalation escalation = new Escalation();
                escalation.setSlaId(slaTemplate.getSlaId());
                escalation.setLevel(slaLevelTemplate.getLevel());
                escalation.setEntityId(entityId);
                escalation.setCreatedTime(new Date().getTime());
                escalation.setBreachTime(breachTime.toInstant().toEpochMilli());
                escalation.setEscalationTime(escalationTimeInTimeStamp);
                escalation.setTimeZone(timeZone);
                escalation.setMailTemplateName(templateName);
                escalation.setTemplateConfiguration(updateMailContent(mailContent, entity).toString());
                escalation.setSlaHistory(slaHistory);
                escalation.setSendEmail(slaLevelTemplate.getSendEmail());
                escalation.setSendNotification(slaLevelTemplate.getSendNotification());
                JSONArray escalationJsonArray = new JSONArray(slaLevelTemplate.getEscalationTarget());
                List<String> escalate = jsonArrayToList(escalationJsonArray);
                String userName = entity.optString(slaLevelTemplate.getEntityForOwner());
                String geo = slaLevelTemplate.getEntityForGeo();
                log.info("geo {}", geo);
                List<String> mailToEscalate = getMailForEscalation(escalate, slaLevelTemplate.getEscalateWhom(),
                        userName, geo);
                escalation.setWhomToEscalate(listToArray(mailToEscalate));
                escalationDao.create(escalation);
            }
            return true;
        } catch (Exception e) {
            log.error("Error Inside @class: SlaHistoryServiceImpl @Method :createSlaHistory() {}", e.getMessage(), e);
            throw new BusinessException(e.getMessage());
        }
    }

    private List<String> jsonArrayToList(JSONArray jsonArray) {
        List<String> stringList = new ArrayList<>();
        for (int i = 0; i < jsonArray.length(); i++) {
            String stringValue = jsonArray.getString(i);
            stringList.add(stringValue);
        }
        return stringList;

    }

    private JSONArray listToArray(List<String> list) {
        JSONArray jsonArray = new JSONArray();
        for (String stringValue : list) {
            jsonArray.put(stringValue);
        }
        return jsonArray;
    }

    public String convertToCommaSeparatedString(List<String> emails) {
        return String.join(",", emails);
    }

    /**
     * Retrieves a list of email addresses for escalation based on the provided
     * parameters.
     *
     * @param escalate    The escalate string which can be either an email address
     *                    or an expression starting with "$".
     * @param isUser      A boolean flag indicating whether the escalation is for a
     *                    user.
     * @param isVendor    A boolean flag indicating whether the escalation is for a
     *                    vendor.
     * @param isUserGroup A boolean flag indicating whether the escalation is for a
     *                    user group.
     * @param userName    The username for which escalation is triggered.
     * @param geo         The JSON array representing the geographic locations.
     * @return A list of email addresses for escalation.
     */
    public List<String> getMailForEscalation(List<String> escalations, String whomToEscalate, String userName,
            String geo) {
        List<String> mailList = new ArrayList<>();
        try {
            log.info("getMailForEscalation________________");
            User currentUserWrapper = new User();
            String currentUser = null;
            Map<String, Object> filterMap = new HashMap<>();

            for (String escalate : escalations) {
                if (escalate.startsWith("$")) {
                    if (userName != null) {
                        currentUserWrapper = userRest.byUserName(userName);
                        currentUser = userName;
                    }
                    processEscalatePattern(escalate, currentUserWrapper, currentUser, mailList, filterMap, geo);
                } else {
                    String[] parts = escalate.split(" : ");
                    String type = null;
                    String reciever = null;
                    if (parts.length == 2) {
                        type = parts[0].trim();
                        reciever = parts[1].trim();
                    }
                    if (type == null) {
                        mailList.add(escalate);
                    } else if (type.equalsIgnoreCase("user")) {
                        mailList.add(reciever);
                    } else if (type.equalsIgnoreCase("vendor") || type.equalsIgnoreCase("userGroup")) {
                        filterMap.put(SlaUtils.GEO, geo);

                        if (type.equalsIgnoreCase("vendor")) {
                            String valueName = currentUserWrapper.getBusinessUnit().getName();
                            filterMap.put("businessUnitName", valueName);
                        } else if (type.equalsIgnoreCase("userGroup")) {
                            List<UserGroup> userGroupList = userRest.getUserGroupListByUserName(currentUser);
                            filterMap.put("groupName", userGroupList);
                        }
                        Integer llimit = 0;
                        Integer ulimit = 100;
                        String gsonString = new Gson().toJson(filterMap);
                        Map<String, Object> umMap = Collections.singletonMap("filterMap", gsonString);
                        Map<String, Object> userMap = userRest.usersByFilter(llimit, ulimit, umMap);
                        extractEmailsFromUserMap(userMap, mailList);
                    }
                }
            }
        } catch (Exception e) {
            log.error("Error Inside @class: SlaHistoryServiceImpl @Method :getMailForEscalation() {}", e.getMessage(),
                    e);
            throw new BusinessException(e.getMessage());
        }
        return mailList;
    }

    /**
     * Extracts email addresses from a user map and adds them to the provided mail
     * list.
     *
     * This method iterates through the user map, which contains user data, and
     * extracts
     * the email addresses from it. It assumes that the user map contains String
     * keys
     * and Object values, where the email address is stored under the key "email" as
     * a String.
     * The extracted email addresses are then added to the provided mail list.
     *
     * @param userMap  The map containing user data with email addresses.
     * @param mailList The list to which the extracted email addresses will be
     *                 added.
     */
    private void extractEmailsFromUserMap(Map<String, Object> userMap, List<String> mailList) {
        for (Map.Entry<String, Object> entry : userMap.entrySet()) {
            Object valueObject = entry.getValue();
            if (valueObject instanceof Map) {
                Map<String, Object> userData = (Map<String, Object>) valueObject;
                String eMail = (String) userData.get("email");
                mailList.add(eMail);
            }
        }
    }

    /**
     * Processes the escalate pattern when the escalate string starts with "$".
     *
     * @param escalate           The escalate string starting with "$".
     * @param currentUserWrapper The User object representing the current user.
     * @param currentUser        The username of the current user.
     * @param mailList           The list to store email addresses for escalation.
     * @param filterMap          The filter map to be used for vendor or user group
     *                           filtering.
     * @param geo                The JSON array representing the geographic
     *                           locations.
     */
    private void processEscalatePattern(String escalate, User currentUserWrapper, String currentUser,
            List<String> mailList, Map<String, Object> filterMap, String geo) {
        String[] splitPattern = escalate.split("\\.");
        for (int i = 0; i < splitPattern.length; i++) {
            String value = splitPattern[i];
            if (value.equalsIgnoreCase("$Owner")) {
                User ownerUser = userRest.byUserName(currentUser);
                currentUserWrapper = ownerUser;
                String eMail = ownerUser.getEmail();
                if (i == splitPattern.length - 1) {
                    mailList.add(eMail);
                }
            } else if (value.equalsIgnoreCase("manager")) {
                currentUserWrapper = userRest.byUserName(currentUserWrapper.getReportingManager());
                String eMail = currentUserWrapper.getEmail();
                if (i == splitPattern.length - 1) {
                    mailList.add(eMail);
                }
            } else if (value.equalsIgnoreCase(SlaUtils.VENDOR) || value.equalsIgnoreCase(SlaUtils.USER_SPACE)
                    || value.equalsIgnoreCase(SlaUtils.BUSINESS_UNIT) || value.equalsIgnoreCase(SlaUtils.USER_GROUP)) {
                processVendorOrUserGroup(currentUserWrapper, value, filterMap, geo);
            }
        }
    }

    /**
     * Processes and filters vendors or user groups for escalation based on the
     * provided value.
     *
     * @param currentUserWrapper The User object representing the current user.
     * @param value              The value indicating the type of escalation to be
     *                           processed.
     * @param filterMap          The filter map used for vendor or user group
     *                           filtering.
     * @param geo                The JSON array representing the geographic
     *                           locations.
     */
    private void processVendorOrUserGroup(User currentUserWrapper, String value, Map<String, Object> filterMap,
            String geo) {

        filterMap.put(SlaUtils.GEO, geo);

        if (value.equalsIgnoreCase(SlaUtils.VENDOR) || value.equalsIgnoreCase(SlaUtils.BUSINESS_UNIT)) {
            String valueName = currentUserWrapper.getBusinessUnit().getName();
            filterMap.put("businessUnitName", valueName);
        } else if (value.equalsIgnoreCase(SlaUtils.USER_SPACE)) {
            String valueName = currentUserWrapper.getUserSpace().getName();
            filterMap.put(SlaUtils.USER_SPACE, valueName);
        } else if (value.equalsIgnoreCase(SlaUtils.USER_GROUP)) {
            List<UserGroup> userGroupList = userRest.getUserGroupListByUserName(currentUserWrapper.getUserName());
            filterMap.put("groupName", userGroupList);
        }
    }

    private JSONObject updateMailContent(JSONObject template, JSONObject entity) {
        for (String key : template.keySet()) {
            JSONObject object = template.getJSONObject(key);
            object.put(SlaUtils.SUBJECT, getPattern(object.optString(SlaUtils.SUBJECT), entity));
            object.put(SlaUtils.CONTENT, getPattern(object.optString(SlaUtils.CONTENT), entity));
        }
        return template;
    }

    private String getPattern(String input, JSONObject entity) {
        String regex = "\\$[a-zA-Z0-9.]+";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(input);
        while (matcher.find()) {
            String match = matcher.group();
            String[] parts = match.split("\\.");
            String result = entity.optString(parts[1]);
            // Replace the matched placeholder with the result
            input = input.replace(match, result);
        }
        return input;
    }

    /**
     * Retrieves the mail configuration for a specific notification template
     * associated with an escalation level.
     *
     * @param levelObj The JSONObject representing the configuration of the
     *                 escalation level.
     * @param entity   The JSONObject representing the data associated with the
     *                 entity.
     * @return A JSONObject containing the custom content for the mail.
     */
    public JSONObject getMailConfiguration(JSONObject levelObj, JSONObject entity) {
        JSONObject customContent = new JSONObject();
        try {
            NotificationTemplate notificationTemplate = notificationTemplateRest
                    .templateByName(levelObj.optString("notificationTemplate"));
            log.info("notification Template {}", notificationTemplate);
            if (notificationTemplate != null) {

                for (NotificationTemplateDetail notificationTemplateType : notificationTemplate.getTemplateType()) {
                    JSONObject object = processNotificationTemplateType(notificationTemplateType, entity);
                    if (object != null) {
                        customContent.put(notificationTemplateType.getTemplatetype().toString(), object);
                    }
                }
            }
            log.info("------------------------customContent------------------------ {}", customContent);
            return customContent;
        } catch (Exception e) {
            log.error("Error Inside @class: SlaHistoryServiceImpl @Method :getMailConfiguration() {}", e.getMessage(),
                    e);
            throw new BusinessException(e.getMessage());
        }
    }

    /**
     * Processes a specific NotificationTemplateDetail to generate the custom
     * content for the mail.
     *
     * @param notificationTemplateType The NotificationTemplateDetail representing
     *                                 the configuration of the notification
     *                                 template.
     * @param entity                   The JSONObject representing the data
     *                                 associated with the entity.
     * @return A JSONObject containing the processed custom content for the mail.
     */
    private JSONObject processNotificationTemplateType(NotificationTemplateDetail notificationTemplateType,
            JSONObject entity) {
        String content = notificationTemplateType.getContent();
        String subject = notificationTemplateType.getSubject();
        String config = notificationTemplateType.getConfigurations();

        if (config != null) {
            Map<String, String> emailDataMap = createEmailDataMap(config, entity);
            if (!emailDataMap.isEmpty() && content != null) {
                content = replaceEmailData(content, emailDataMap);

                JSONObject object = new JSONObject();
                object.put(SlaUtils.CONTENT, content);
                object.put(SlaUtils.SUBJECT, subject);
                return object;
            }
        }

        return null;
    }

    // private JSONObject processTemplate(String templateConfig, JSONObject entity){
    // log.info("templateConfig {}, entity {}",templateConfig, entity );
    // JSONObject mailContent = new JSONObject(templateConfig);
    //
    //
    // if (mailContent.has("email")) {
    // String content = mailContent.optString(SlaUtils.CONTENT);
    // String subject = mailContent.optString(SlaUtils.SUBJECT);
    // String config = mailContent.optString(SlaUtils.CONFIG);
    //
    // if (config != null) {
    // Map<String, String> emailDataMap = createEmailDataMap(config, entity);
    // if (!emailDataMap.isEmpty() && content != null) {
    // content = replaceEmailData(content, emailDataMap);
    //
    // JSONObject object = new JSONObject();
    // object.put(SlaUtils.CONTENT, content);
    // object.put(SlaUtils.SUBJECT, subject);
    // return object;
    // }
    // }
    //
    // }
    //
    // String content = mailContent.optString("content");
    // String subject = mailContent.optString("subject");
    // String config = mailContent.optString("config");
    //
    // if (config != null) {
    // Map<String, String> emailDataMap = createEmailDataMap(config, entity);
    // if (!emailDataMap.isEmpty() && content != null) {
    // content = replaceEmailData(content, emailDataMap);
    //
    // JSONObject object = new JSONObject();
    // object.put("content", content);
    // object.put("subject", subject);
    // return object;
    // }
    // }
    // return mailContent;
    // }

    /**
     * Replaces placeholders in the content string with their corresponding values
     * from the emailDataMap.
     *
     * @param content      The content string containing placeholders represented as
     *                     `${key}`.
     * @param emailDataMap A map containing placeholder keys and their corresponding
     *                     values.
     * @return The content string with all placeholders replaced by their values.
     */
    private String replaceEmailData(String content, Map<String, String> emailDataMap) {
        for (Map.Entry<String, String> entry : emailDataMap.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            content = content.replaceAll("\\$\\{" + key + "\\}", value);
        }
        return content;
    }

    /**
     * Creates a map of email data by processing the config string and the entity
     * data.
     *
     * @param config The comma-separated string containing placeholders (keys) with
     *               the prefix "$emailData."
     * @param entity The JSONObject representing the data associated with the
     *               entity.
     * @return A map of email data with keys representing placeholders and values
     *         representing the corresponding data from the entity.
     */
    private Map<String, String> createEmailDataMap(String config, JSONObject entity) {
        Map<String, String> emailDataMap = new HashMap<>();
        String prefix = "$emailData.";
        List<String> stringList = Arrays.asList(config.split(","));

        for (String element : stringList) {
            if (element.startsWith(prefix)) {
                element = element.replace(prefix, "");
            }

            if (entity.has(element)) {
                emailDataMap.put(element, entity.optString(element));
            }
        }

        return emailDataMap;
    }

    /**
     * Calculates the escalation time based on the specified parameters.
     *
     * @param dateTime                 The base date and time from which to
     *                                 calculate the escalation time.
     * @param amountToAdd              The amount of time to add or subtract for
     *                                 escalation, in minutes.
     * @param whenToEscalate           The condition for escalation, such as "BEFORE
     *                                 SLA IS BREACHED" or "AFTER SLA IS BREACHED".
     * @param isExcludingNonWorkingDay A flag indicating whether non-working days
     *                                 should be excluded from the calculation.
     * @param entityId                 The entity identifier for which the
     *                                 escalation is being calculated.
     * @param timeZone                 The time zone in which the escalation time
     *                                 should be calculated.
     * @param slaTemplate              The SLA template containing operational hours
     *                                 and other information.
     * @return The escalated date and time based on the specified parameters.
     */
    private ZonedDateTime calculateEscalationTime(ZonedDateTime dateTime, long amountToAdd, String whenToEscalate,
            boolean isExcludingNonWorkingDay, int entityId, String timeZone, SlaTemplate slaTemplate) {
        log.info(
                "============================ dateTime {}, amountToAdd {}, whenToEscalate {}, isExcludingNonWorkingDay {}, entityId {}",
                dateTime, amountToAdd, whenToEscalate, isExcludingNonWorkingDay, entityId);

        JSONObject inTime = new JSONObject(slaTemplate.getInTime());
        JSONObject outTime = new JSONObject(slaTemplate.getOutTime());
        String escalateCondition = whenToEscalate.toUpperCase();
        Escalation escalation;
        int level = 0;
        switch (escalateCondition) {
            case "BEFORE SLA IS BREACHED":
                dateTime = addTimeExcludingWeekendsAndWorkingHours(dateTime, amountToAdd, "sub",
                        isExcludingNonWorkingDay, slaTemplate.getOperationalHours(), timeZone, inTime, outTime);
                break;

            case "AFTER SLA IS BREACHED":
                dateTime = addTimeExcludingWeekendsAndWorkingHours(dateTime, amountToAdd, "add",
                        isExcludingNonWorkingDay, slaTemplate.getOperationalHours(), timeZone, inTime, outTime);
                break;

            case "AS SOON AS LEVEL 1 IS ESCALATED", "AS SOON AS LEVEL 2 IS ESCALATED":
                level = escalateCondition.contains(SlaUtils.LEVEL1) ? 1 : 2;
                escalation = escalationDao.getEscalationByLevelAndEntityId(level, entityId);
                dateTime = ZonedDateTime.ofInstant(Instant.ofEpochMilli(escalation.getEscalationTime()),
                        ZoneId.of(timeZone));
                break;

            case "AFTER LEVEL 1 IS ESCALATED", "AFTER LEVEL 2 IS ESCALATED":
                level = escalateCondition.contains(SlaUtils.LEVEL1) ? 1 : 2;
                escalation = escalationDao.getEscalationByLevelAndEntityId(level, entityId);
                ZonedDateTime dateL = ZonedDateTime.ofInstant(Instant.ofEpochMilli(escalation.getEscalationTime()),
                        ZoneId.of(timeZone));
                dateTime = addTimeExcludingWeekendsAndWorkingHours(dateL, amountToAdd, "add", isExcludingNonWorkingDay,
                        slaTemplate.getOperationalHours(), timeZone, inTime, outTime);
                break;

            case "BEFORE LEVEL 1 IS ESCALATED", "BEFORE LEVEL 2 IS ESCALATED":
                level = escalateCondition.contains(SlaUtils.LEVEL1) ? 1 : 2;
                escalation = escalationDao.getEscalationByLevelAndEntityId(level, entityId);
                ZonedDateTime dateF = ZonedDateTime.ofInstant(Instant.ofEpochMilli(escalation.getEscalationTime()),
                        ZoneId.of(timeZone));
                dateTime = addTimeExcludingWeekendsAndWorkingHours(dateF, amountToAdd, "sub", isExcludingNonWorkingDay,
                        slaTemplate.getOperationalHours(), timeZone, inTime, outTime);
                break;

            default:
                break;
        }

        return dateTime;
    }

    /**
     * Calculates the new date and time by adding or subtracting the specified
     * amount of time while considering operational hours and excluding weekends if
     * necessary.
     *
     * @param dateTime         The base date and time from which to calculate the
     *                         new date and time.
     * @param amountToAdd      The amount of time to add or subtract, in minutes.
     * @param addOrSub         Specifies whether to add or subtract the amount of
     *                         time.
     * @param excludeWeekends  A flag indicating whether to exclude weekends from
     *                         the calculation.
     * @param operationalHours The type of operational hours ("Calendar Hours" or
     *                         custom working hours).
     * @param timeZone         The time zone in which the calculation should be
     *                         performed.
     * @param inTime           The starting time of the working hours as a
     *                         JSONObject with "hours" and "minutes" fields.
     * @param outTime          The ending time of the working hours as a JSONObject
     *                         with "hours" and "minutes" fields.
     * @return The new date and time after adding or subtracting the specified
     *         amount of time.
     */
    private ZonedDateTime addTimeExcludingWeekendsAndWorkingHours(ZonedDateTime dateTime, long amountToAdd,
            String addOrSub, boolean excludeWeekends, String operationalHours, String timeZone, JSONObject inTime,
            JSONObject outTime) {
        log.info(
                "dateTime {}, amountToAdd {}, addOrSub {}, excludeWeekends {}, operationalHours {}, timeZone {}, inTime {}, outTime {}",
                dateTime, amountToAdd, addOrSub, excludeWeekends, operationalHours, timeZone, inTime, outTime);
        try {
            ZonedDateTime resultDateTime = dateTime;

            if (operationalHours.equalsIgnoreCase("Calendar Hours")) {
                return resultDateTime.plus(amountToAdd, ChronoUnit.MINUTES);
            }

            int startHour = Integer.parseInt(inTime.optString("hours"));
            int startMinute = Integer.parseInt(inTime.optString("minutes"));
            int endHour = Integer.parseInt(outTime.optString("hours"));
            int endMinute = Integer.parseInt(outTime.optString("minutes"));

            if (excludeWeekends) {
                resultDateTime = adjustForWeekends(resultDateTime);
            }

            ZonedDateTime startDateTime = resultDateTime.withHour(startHour).withMinute(startMinute);
            ZonedDateTime endDateTime = getEndDateTime(resultDateTime, startHour, startMinute, endHour, endMinute,
                    excludeWeekends);

            resultDateTime = adjustForComparison(resultDateTime, startDateTime, endDateTime, addOrSub);

            if (amountToAdd > 0) {
                long remainingWorkingMinutes = getRemainingWorkingMinutes(resultDateTime, endDateTime);

                if (amountToAdd < remainingWorkingMinutes) {
                    resultDateTime = resultDateTime.plus(amountToAdd, ChronoUnit.MINUTES);
                } else {
                    resultDateTime = handleRemainingMinutes(resultDateTime, amountToAdd, startDateTime, endDateTime,
                            addOrSub, excludeWeekends);
                }
            }
            return resultDateTime;
        } catch (Exception e) {
            log.error(
                    "Error Inside @class: SlaHistoryServiceImpl @Method :addTimeExcludingWeekendsAndWorkingHours() {}",
                    e.getMessage(), e);
            throw new BusinessException(e.getMessage());
        }
    }

    /**
     * Adjusts the given ZonedDateTime to the next working day if it falls on a
     * Saturday or Sunday.
     *
     * @param dateTime The ZonedDateTime to be adjusted.
     * @return The adjusted ZonedDateTime.
     */
    private ZonedDateTime adjustForWeekends(ZonedDateTime dateTime) {
        if (dateTime.getDayOfWeek() == DayOfWeek.SATURDAY) {
            return dateTime.plusDays(2);
        } else if (dateTime.getDayOfWeek() == DayOfWeek.SUNDAY) {
            return dateTime.plusDays(1);
        }
        return dateTime;
    }

    /**
     * Calculates the ZonedDateTime for the end of the working hours based on the
     * input parameters.
     *
     * @param currentDateTime The current ZonedDateTime.
     * @param startHour       The hour of the starting time.
     * @param startMinute     The minute of the starting time.
     * @param endHour         The hour of the ending time.
     * @param endMinute       The minute of the ending time.
     * @param excludeWeekends A boolean flag indicating whether weekends should be
     *                        excluded.
     * @return The ZonedDateTime for the end of the working hours.
     */
    private ZonedDateTime getEndDateTime(ZonedDateTime currentDateTime, int startHour, int startMinute, int endHour,
            int endMinute, boolean excludeWeekends) {
        ZonedDateTime endDateTime;
        if ((startHour > endHour) || ((startHour == endHour) && (startMinute > endMinute))) {
            if (excludeWeekends) {
                if (currentDateTime.getDayOfWeek() == DayOfWeek.FRIDAY) {
                    endDateTime = currentDateTime.withHour(23).withMinute(59);
                } else {
                    endDateTime = currentDateTime.plusDays(1).withHour(endHour).withMinute(endMinute);
                }
            } else {
                endDateTime = currentDateTime.plusDays(1).withHour(endHour).withMinute(endMinute);
            }
        } else {
            endDateTime = currentDateTime.withHour(endHour).withMinute(endMinute);
        }
        return endDateTime;
    }

    /**
     * Adjusts the ZonedDateTime based on the comparison with the startDateTime and
     * endDateTime.
     *
     * @param resultDateTime The ZonedDateTime to be adjusted.
     * @param startDateTime  The ZonedDateTime representing the start of the working
     *                       hours.
     * @param endDateTime    The ZonedDateTime representing the end of the working
     *                       hours.
     * @param addOrSub       The operation to be performed (either "add" or "sub").
     * @return The adjusted ZonedDateTime.
     */
    private ZonedDateTime adjustForComparison(ZonedDateTime resultDateTime, ZonedDateTime startDateTime,
            ZonedDateTime endDateTime, String addOrSub) {
        int comparisonResultForStart = resultDateTime.compareTo(startDateTime);
        int comparisonResultForEnd = resultDateTime.compareTo(endDateTime);
        if (addOrSub.equalsIgnoreCase("add")) {
            if (comparisonResultForStart < 0) {
                return resultDateTime.withHour(startDateTime.getHour()).withMinute(startDateTime.getMinute());
            } else if (comparisonResultForEnd > 0) {
                return resultDateTime.plusDays(1).withHour(startDateTime.getHour())
                        .withMinute(startDateTime.getMinute());
            }
        } else {
            if (comparisonResultForStart < 0) {
                return resultDateTime.withHour(endDateTime.getHour()).withMinute(endDateTime.getMinute());
            } else if (comparisonResultForEnd > 0) {
                return resultDateTime.minusDays(1).withHour(endDateTime.getHour()).withMinute(endDateTime.getMinute());
            }
        }
        return resultDateTime;
    }

    /**
     * Calculates the remaining working minutes between two ZonedDateTime instances.
     *
     * @param resultDateTime The starting ZonedDateTime.
     * @param endDateTime    The ending ZonedDateTime.
     * @return The remaining working minutes between the two ZonedDateTime
     *         instances.
     */
    private long getRemainingWorkingMinutes(ZonedDateTime resultDateTime, ZonedDateTime endDateTime) {
        Duration duration = Duration.between(resultDateTime, endDateTime);
        return duration.toMinutes();
    }

    /**
     * Handles the remaining minutes after adding regular working hours and working
     * days.
     *
     * @param resultDateTime  The current ZonedDateTime.
     * @param amountToAdd     The remaining minutes to be added.
     * @param startDateTime   The starting ZonedDateTime with working hours.
     * @param endDateTime     The ending ZonedDateTime with working hours.
     * @param addOrSub        The operation to add or subtract time.
     * @param excludeWeekends Flag indicating whether weekends are excluded.
     * @return The adjusted ZonedDateTime with the remaining minutes added.
     */
    private ZonedDateTime handleRemainingMinutes(ZonedDateTime resultDateTime, long amountToAdd,
            ZonedDateTime startDateTime, ZonedDateTime endDateTime, String addOrSub, boolean excludeWeekends) {
        long workingMinutes = getWorkingMinutesBetweenDates(startDateTime, endDateTime);
        amountToAdd -= workingMinutes;

        while (amountToAdd > 0) {
            resultDateTime = resultDateTime.plusDays(1);
            if (excludeWeekends) {
                resultDateTime = skipWeekends(resultDateTime, addOrSub);
            }
            amountToAdd -= workingMinutes;
        }

        if (amountToAdd < 0) {
            resultDateTime = adjustForExcessTime(resultDateTime, amountToAdd, workingMinutes, addOrSub);
        }

        return resultDateTime;
    }

    /**
     * Skips weekends (Saturday and Sunday) by adding or subtracting days from the
     * given ZonedDateTime.
     *
     * @param dateTime The current ZonedDateTime.
     * @param addOrSub The operation to add or subtract days.
     * @return The adjusted ZonedDateTime that falls on a working day.
     */
    private ZonedDateTime skipWeekends(ZonedDateTime dateTime, String addOrSub) {
        while (dateTime.getDayOfWeek() == DayOfWeek.SATURDAY || dateTime.getDayOfWeek() == DayOfWeek.SUNDAY) {
            if (addOrSub.equalsIgnoreCase("add")) {
                dateTime = dateTime.plusDays(1);
            } else {
                dateTime = dateTime.minusDays(1);
            }
        }
        return dateTime;
    }

    /**
     * Adjusts the dateTime for excess time when the amountToAdd is greater than
     * workingMinutes in a single working day.
     *
     * @param dateTime       The current ZonedDateTime.
     * @param amountToAdd    The excess amount of time to be added or subtracted.
     * @param workingMinutes The number of working minutes in a single working day.
     * @param addOrSub       The operation to add or subtract excess time.
     * @return The adjusted ZonedDateTime considering the excess time.
     */
    private ZonedDateTime adjustForExcessTime(ZonedDateTime dateTime, long amountToAdd, long workingMinutes,
            String addOrSub) {
        amountToAdd += workingMinutes;
        if (addOrSub.equalsIgnoreCase("add")) {
            return dateTime.plus(amountToAdd, ChronoUnit.MINUTES);
        } else {
            return dateTime.minus(amountToAdd, ChronoUnit.MINUTES);
        }
    }

    /**
     * Calculates the working minutes between two ZonedDateTime objects.
     *
     * @param startDateTime The start ZonedDateTime.
     * @param endDateTime   The end ZonedDateTime.
     * @return The total number of working minutes between the two dates.
     */
    private long getWorkingMinutesBetweenDates(ZonedDateTime startDateTime, ZonedDateTime endDateTime) {
        Duration duration = Duration.between(startDateTime, endDateTime);
        return duration.toMinutes();
    }

    /**
     * Scheduled task to check and execute SLA history.
     *
     * @return Returns null.
     */
    @Override
    public String scheduledExecuteHistory() {
        log.info(SlaUtils.INSIDE_METHOD, "checkAndExecuteHistory");
        try {
            List<SlaHistory> slaTemplateHistories = slaTemplateHistoryDao.getAllSlaHistory();
            for (SlaHistory slaHistory1 : slaTemplateHistories) {

                long breachDate = slaHistory1.getBreachTime();
                long currentDate = new Date().getTime();
                if (currentDate > breachDate && slaHistory1.getState().equals(SlaHistory.STAGE.IN_PROGRESS)) {
                    slaHistory1.setLastUpdatedTime(new Date().getTime());
                    slaHistory1.setBreachStatus(true);
                    slaHistory1.setModifiedTime(new Date().getTime());
                    slaHistory1.setBreachedAt(slaHistory1.getLevel());
                    log.info("Breached histories are executed !!!!! {}", slaHistory1.getEntityIdentifier());
                    slaTemplateHistoryDao.create(slaHistory1);
                }
            }
        } catch (Exception e) {
            log.error("Error Inside @class: SlaHistoryServiceImpl @Method :scheduledExecuteHistory() {}",
                    e.getMessage(), e);
            throw new BusinessException(e.getMessage());
        }
        return null;
    }

    /**
     * Get the count of SLA breaches and SLA escalations for the present day.
     *
     * @return Returns a JSON object containing the counts of breaches, escalations,
     *         and total activities for the present day.
     */
    @Override
    public String getPresentDayActivities() {
        try {
            Date currentDate = new Date();
            SimpleDateFormat dateFormat = new SimpleDateFormat(SlaUtils.YYYY_MM_DD);
            String formattedCurrentDate = dateFormat.format(currentDate);
            long slaBreached = getBreachCount(formattedCurrentDate, dateFormat, true);
            long slaEscalated = getEscalationCount(formattedCurrentDate, dateFormat);
            long totalActivities = slaBreached + slaEscalated;
            JSONObject jsonObject = new JSONObject();

            jsonObject.put("slaBreached", slaBreached);
            jsonObject.put("slaEscalated", slaEscalated);
            jsonObject.put("totalActivities", totalActivities);

            return jsonObject.toString();
        } catch (Exception e) {
            log.error("Error Inside @class: SlaHistoryServiceImpl @Method :getPresentDayActivities() {}",
                    e.getMessage(), e);
            throw new BusinessException(e.getMessage());
        }
    }

    /**
     * Get the count of SLA escalations that occurred on a given date.
     *
     * @param date       The date for which the escalations are counted.
     * @param dateFormat The date format used to parse the escalation dates.
     * @return Returns the count of SLA escalations that occurred on the given date.
     */
    public long getEscalationCount(String date, SimpleDateFormat dateFormat) {

        List<Escalation> escalations = escalationDao.getAllEscalations(true);
        long slaEscalated = 0;
        for (Escalation escalation : escalations) {
            Long escalationDate = escalation.getEscalationTime();
            String formattedEscalationDate = dateFormat.format(escalationDate);
            boolean isSameDate = formattedEscalationDate.compareTo(date) == 0;
            if (isSameDate) {
                slaEscalated++;
            }
        }
        return slaEscalated;
    }

    /**
     * Get the count of SLA breaches that occurred on a given date.
     *
     * @param date       The date for which the breaches are counted.
     * @param dateFormat The date format used to parse the breach dates.
     * @return Returns the count of SLA breaches that occurred on the given date.
     */
    public long getBreachCount(String date, SimpleDateFormat dateFormat, boolean breachStatus) {
        log.info("date {}, dateFormat {}, breachStatus {}", date, dateFormat, breachStatus);
        List<SlaHistory> histories = slaTemplateHistoryDao.getAllBreachedSlaHistory(breachStatus);
        long slaBreached = 0;
        for (SlaHistory history : histories) {
            Long breachDateInLong = history.getBreachTime();
            Date breachDate = new Date(breachDateInLong);
            String formattedBreachDate = dateFormat.format(breachDate);
            boolean isSameDate = formattedBreachDate.compareTo(date) == 0;
            if (isSameDate) {
                slaBreached++;
            }
        }
        return slaBreached;
    }

    /**
     * Get the SLA breach proximity for different time ranges (today, tomorrow, this
     * week, and this month).
     *
     * @return Returns a JSON object containing the counts of breaches for each time
     *         range.
     */
    @Override
    public String getSlaBreachProximity() {

        List<Object[]> list = slaTemplateHistoryDao.getSlaProximity();
        JSONObject jsonObject = new JSONObject();
        for (Object[] obj : list) {
            String key = obj[0].toString();
            String value = obj[1].toString();
            jsonObject.put(key, value);
        }
        return jsonObject.toString();
    }

    /**
     * Get the activities (breach count and escalation count) for the last 30 days.
     *
     * @return Returns a JSON array containing the breach count and escalation count
     *         for each day.
     */
    @Override
    public Map<String, Map<String, Integer>> getActivitiesForThirtyDays() {

        List<Object[]> breaches = slaTemplateHistoryDao.getThirtyDaysBreaches();
        List<Object[]> escalations = escalationDao.getThirtyDaysEscalations();
        Date currentDate = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat(SlaUtils.YYYY_MM_DD);
        Map<String, Map<String, Integer>> outterMap = new HashMap<>();
        Map<String, Integer> innerMap = new HashMap<>();
        for (int i = 1; i <= 30; i++) {
            String formattedCurrentDate = dateFormat.format(currentDate);
            innerMap.put("breached", 0);
            innerMap.put("escalated", 0);
            outterMap.put(formattedCurrentDate, innerMap);
            final long ONE_MINUTE_IN_MILLIS = 60000;
            currentDate = new Date(currentDate.getTime() - (24 * 60 * ONE_MINUTE_IN_MILLIS));
        }

        for (Object[] replacement : breaches) {
            int quantity = ((Long) replacement[0]).intValue();
            String state = "breached";
            String dateStr = (String) replacement[1];
            if (outterMap.containsKey(dateStr)) {
                Map<String, Integer> qMap = new HashMap<>(outterMap.get(dateStr));
                qMap.put(state, quantity);
                outterMap.put(dateStr, qMap);
            }
        }

        for (Object[] replacement : escalations) {
            int quantity = ((Long) replacement[0]).intValue();
            String state = "escalated";
            String dateStr = (String) replacement[1];
            if (outterMap.containsKey(dateStr)) {
                Map<String, Integer> qMap = new HashMap<>(outterMap.get(dateStr));
                qMap.put(state, quantity);
                outterMap.put(dateStr, qMap);
            }
        }

        return outterMap;

    }

    /**
     * Get the top breached SLAs.
     *
     * @return Returns a JSON array containing the top breached SLAs.
     */
    @Override
    public String getTopSlaBreached() {
        try {
            JSONArray jsonObject = slaTemplateHistoryDao.getTopSlaBreached();
            return jsonObject.toString();
        } catch (Exception e) {
            log.error("Error Inside @class: SlaHistoryServiceImpl @Method :getTopSlaBreached() {}", e.getMessage(), e);
            throw new BusinessException(e.getMessage());
        }
    }

    /**
     * Get the top triggered SLAs.
     *
     * @return Returns a JSON array containing the top triggered SLAs.
     */
    @Override
    public String getTopSlaTriggered() {
        try {
            JSONArray jsonObject = slaTemplateHistoryDao.getTopSlaTriggered();
            return jsonObject.toString();
        } catch (Exception e) {
            log.error("Error Inside @class: SlaHistoryServiceImpl @Method :getTopSlaTriggered() {}", e.getMessage(), e);
            throw new BusinessException(e.getMessage());
        }
    }

    /**
     * Get the top triggered SLAs organized by entities.
     *
     * @return Returns a JSON array containing the top triggered SLAs organized by
     *         entities.
     */
    @Override
    public String getTopSlaEntityWise() {
        try {
            JSONArray jsonObject = slaTemplateHistoryDao.getTopSlaEntityWise();

            return jsonObject.toString();
        } catch (Exception e) {
            log.error("Error Inside @class: SlaHistoryServiceImpl @Method :getTopSlaEntityWise() {}", e.getMessage(),
                    e);
            throw new BusinessException(e.getMessage());
        }
    }

    /**
     * Get the SLAs breached based on different levels.
     *
     * @return Returns a JSON object containing the SLAs breached based on different
     *         levels.
     */
    @Override
    public String levelWiseSlaBreached() {

        try {
            JSONObject jsonObject = slaTemplateHistoryDao.levelWiseSlaBreached();
            return jsonObject.toString();
        } catch (Exception e) {
            log.error("Error Inside @class: SlaHistoryServiceImpl @Method :levelWiseSlaBreached() {}", e.getMessage(),
                    e);
            throw new BusinessException(e.getMessage());
        }
    }

    /**
     * Get the total count of SLA history records.
     *
     * @return Returns the total count of SLA history records.
     */
    @Override
    public Long getTotalHistoryCount() {
        try {
            return slaTemplateHistoryDao.getTotalHistoryCount();
        } catch (Exception e) {
            log.error("Error Inside @class: SlaHistoryServiceImpl @Method :getTotalHistoryCount() {}", e.getMessage(),
                    e);
            throw new BusinessException(e.getMessage());
        }
    }

    /**
     * Get SLA history counts based on different statuses.
     *
     * @return Returns a JSON array containing SLA history counts based on different
     *         statuses.
     */
    @Override
    public Map<String, Map<String, Integer>> slaTriggeredByStatus() {
        try {
            List<Object[]> slaHistories = slaTemplateHistoryDao.getAllSlaAudit();
            Date currentDate = new Date();
            SimpleDateFormat dateFormat = new SimpleDateFormat(SlaUtils.YYYY_MM_DD);
            Map<String, Map<String, Integer>> outterMap = new HashMap<>();
            Map<String, Integer> innerMap = new HashMap<>();
            for (int i = 1; i <= 30; i++) {
                String formattedCurrentDate = dateFormat.format(currentDate);
                innerMap.put("NEW", 0);
                innerMap.put("IN_PROGRESS", 0);
                innerMap.put("ON_HOLD", 0);
                innerMap.put("COMPLETED", 0);
                innerMap.put("CANCELLED", 0);
                outterMap.put(formattedCurrentDate, innerMap);
                final long ONE_MINUTE_IN_MILLIS = 60000;
                currentDate = new Date(currentDate.getTime() - (24 * 60 * ONE_MINUTE_IN_MILLIS));
            }
            for (Object[] replacement : slaHistories) {
                int quantity = ((Long) replacement[0]).intValue();
                String state = (String) replacement[1];
                String dateStr = (String) replacement[2];

                if (outterMap.containsKey(dateStr)) {
                    Map<String, Integer> qMap = new HashMap<>(outterMap.get(dateStr)); // Create a new instance for each
                                                                                       // date
                    qMap.put(state, quantity);
                    outterMap.put(dateStr, qMap);
                }
            }
            return outterMap;
        } catch (Exception e) {
            log.error("Error Inside @class: SlaHistoryServiceImpl @Method :slaTriggeredByStatus() {}", e.getMessage(),
                    e);
            throw new BusinessException(e.getMessage());
        }
    }

    @Override
    public int auditCount(long slaId) {
        try {
            String result = getSlaHistoryAudById(slaId);
            JSONArray jsonArray = new JSONArray(result);
            return jsonArray.length();
        } catch (Exception e) {
            log.error("Error Inside @class: SlaHistoryServiceImpl @Method :getSlaHistoryAudById() {}", e.getMessage(),
                    e);
            throw new BusinessException("An error occurred while retrieving SLA history data.", e);
        }
    }

    @Override
    public String getSlaHistoryAudById(long slaId) {
        String result = getSlaHistoryAud(slaId);
        log.info("getSlaHistoryAudById {}", result);
        JSONArray jsonArray = new JSONArray(result);
        List<JSONObject> list = new ArrayList<>();
        for (int i = 0; i < jsonArray.length(); i++) {
            if (!jsonArray.getJSONObject(i).isEmpty()) {
                list.add(jsonArray.getJSONObject(i));
            }
        }
        list.sort(Comparator.comparing(obj -> Long.parseLong(obj.optString(SlaUtils.ACTION_TIME))));
        log.info("list====== {}", list);
        JSONArray sortedArray = new JSONArray();
        for (JSONObject obj : list) {
            sortedArray.put(obj);
        }
        log.info("sortedArray====== {}", sortedArray);
        return sortedArray.toString();
    }

    public String getSlaHistoryAud(long slaId) {
        try {
            JSONArray jsonArray = slaTemplateHistoryDao.getHistoryAuditById(slaId);
            log.info("jsonArray {}, jsonArray.length()========", jsonArray, jsonArray.length());
            JSONArray newJsonArray = new JSONArray();
            JSONObject previousObject = null;

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject currentObject = jsonArray.getJSONObject(i);
                JSONObject newJsonObject = new JSONObject();

                if (previousObject != null) {

                    String currentStateRaw = currentObject.optString(SlaUtils.STATE);
                    String previousStateRaw = previousObject.optString(SlaUtils.STATE);
                    String currentState = editAction(currentStateRaw);
                    String previousState = editAction(previousStateRaw);

                    boolean currentBreachStatus = currentObject.optBoolean("breachStatus");
                    boolean previousBreachStatus = previousObject.optBoolean("breachStatus");

                    if (!currentState.equals(previousState)) {
                        newJsonObject.put(SlaUtils.ACTION, currentState);
                        newJsonObject.put(SlaUtils.DESCRIPTION, currentObject.optString(SlaUtils.SPEL_EXPRESSION));
                        newJsonObject.put(SlaUtils.ACTION_TIME, currentObject.optString(SlaUtils.MODIFIED_TIME));
                        newJsonObject.put(SlaUtils.LEVEL, SlaUtils.LEVEL_TAG + currentObject.optString(SlaUtils.LEVEL));
                    }

                    if (currentBreachStatus != previousBreachStatus) {
                        newJsonObject.put(SlaUtils.ACTION, "Breached");
                        newJsonObject.put("description", "SLA has been breached");
                        newJsonObject.put(SlaUtils.ACTION_TIME, currentObject.optString(SlaUtils.MODIFIED_TIME));
                        newJsonObject.put(SlaUtils.LEVEL, SlaUtils.LEVEL_TAG + currentObject.optString(SlaUtils.LEVEL));
                    }

                    newJsonArray.put(newJsonObject);
                } else {
                    previousObject = currentObject;
                    newJsonObject.put(SlaUtils.ACTION_TIME, currentObject.optString(SlaUtils.MODIFIED_TIME));
                    newJsonObject.put(SlaUtils.ACTION, editAction(currentObject.optString(SlaUtils.STATE)));
                    newJsonObject.put(SlaUtils.LEVEL, SlaUtils.LEVEL_TAG + currentObject.optString(SlaUtils.LEVEL));
                    newJsonObject.put(SlaUtils.DESCRIPTION, currentObject.optString(SlaUtils.SPEL_EXPRESSION));
                    newJsonArray.put(newJsonObject);
                }
            }

            newJsonArray = auditEscalation(newJsonArray, slaId);

            return newJsonArray.toString();

        } catch (Exception e) {
            log.error("Error Inside @class: SlaHistoryServiceImpl @Method :getSlaHistoryAudById() {}", e.getMessage(),
                    e);
            throw new BusinessException("An error occurred while retrieving SLA history data.", e);
        }

    }

    private String editAction(String input) {
        String[] words = input.toLowerCase().split("_");
        StringBuilder result = new StringBuilder();
        for (String word : words) {
            if (result.length() > 0) {
                result.append(" ");
            }
            result.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1));
        }

        return result.toString();
    }

    private JSONArray auditEscalation(JSONArray inputJsonArray, long slaId) {
        try {
            JSONArray jsonArray = escalationDao.getAuditByFk(slaId);
            log.info("jsonArray======== {}", jsonArray);
            if (jsonArray == null) {
                return inputJsonArray;
            }
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject currentObject = jsonArray.getJSONObject(i);
                JSONObject jsonObject = new JSONObject();

                jsonObject.put(SlaUtils.ACTION, "escalated at level " + currentObject.optString(SlaUtils.LEVEL));
                jsonObject.put(SlaUtils.ACTION_TIME, currentObject.optString("createdTime"));
                jsonObject.put(SlaUtils.LEVEL, SlaUtils.LEVEL_TAG + currentObject.optString(SlaUtils.LEVEL));
                jsonObject.put(SlaUtils.DESCRIPTION, currentObject.optString("whomToEscalate"));
                log.info("jsonObject============= {}", jsonObject);
                inputJsonArray.put(jsonObject);
            }
            return inputJsonArray;
        } catch (Exception e) {
            log.error("Error Inside @class: YourClassName @Method: auditEscalation() {}", e.getMessage(), e);
            throw new BusinessException("An error occurred during the escalation audit.", e);
        }
    }
}
