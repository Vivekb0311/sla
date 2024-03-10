package com.bootnext.platform.sla.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.bootnext.core.generic.exceptions.application.BusinessException;

public class SlaConfigurationUtils {

    private static Logger logger = LogManager.getLogger(SlaConfigurationUtils.class);

    public static final String JSON_FIELD_NAME = "field";
    public static final String JSON_NAME = "name";
    public static final String JSON_COLUMN_NAME = "columnName";
    public static final String JSON_CONDITION = "condition";
    public static final String JSON_VALUE = "value";
    public static final String JSON_TYPE = "type";
    public static final String JSON_WHEN = "when";
    public static final String JSON_TARGET = "target";
    public static final String JSON_OPERATOR = "operator";
    public static final String JSON_RESPOND_TIME = "respondTime";
    public static final String JSON_RESPOND_TIME_UNIT = "time";
    public static final String JSON_ESCALATION_WHEN = "escalateWhen";
    public static final String JSON_ESCALATION_TARGET = "escalationTarget";
    public static final String JSON_ESCALATION_WHOM = "escalateWhom";
    public static final String JSON_EMAIL_TEMPLATE = "emailTemplate";
    public static final String JSON_ESCALATION_TARGET_MOB = "escalationTargetMob";
    public static final String JSON_ESCALATION_WHOM_MOB = "escalateWhomMob";
    public static final String JSON_EMAIL_TEMPLATE_MOB = "emailTemplateMob";
    public static final String JSON_ESCALATION_TIME = "escalationTime";
    public static final String JSON_ESCALATION_TIME_UNIT = "escalationTimeUnit";
    public static final String FIELD_TYPE_STRING = "String";
    public static final String FIELD_TYPE_DATE = "Date";
    public static final String FIELD_TYPE_ENUM = "ENUM";
    public static final String FIELD_TYPE_BOOLEAN = "BOOLEAN";
    public static final String FIELD_TYPE_INTEGER = "Integer";
    public static final String CONDITION_EQUAL = "EQUAL";
    public static final String CONDITION_CONTAINS = "CONTAINS";
    public static final String CONDITION_NOT_EQUAL = "NOT_EQUAL";
    public static final String CONDITION_GREATER_THAN = "GREATER_THAN";
    public static final String CONDITION_LESS_THAN = "LESS_THAN";
    public static final String CONDITION_GREATER_THAN_EQUAL = "GREATER_THAN_EQUAL";
    public static final String CONDITION_LESS_THAN_EQUAL = "LESS_THAN_EQUAL";
    public static final String CONDITION_IS_NULL = "IS_NULL";
    public static final String CONDITION_IS_NOT_NULL = "IS_NOT_NULL";
    public static final String CONDITION_BEFORE = "before";
    public static final String CONDITION_AFTER = "after";
    public static final String JSON_TARGET_ACTION = "selectedAction";
    public static final String JSON_TARGET_ACTION_WORKFLOW = "workflow";
    public static final String JSON_TARGET_ACTION_MAIL = "sendMail";
    public static final String JSON_TARGET_ACTION_MOBILE_NOTIFICATION = "mobileNotification";
    public static final String JSON_WORKFLOW_TEMP = "workflowTemplate";
    public static final String JSON_RESPONSIBLE_ROLE = "responsibleRole";
    public static final String JSON_TARGET_ACTION_TICKET = "createTicket";
    public static final String JSON_TARGET_TICKET = "ticket";
    public static final String JSON_ROLEID = "roleid";

    public static final String OPERATOR_AND = "AND";
    public static final String OPERATOR_OR = "OR";

    public static final String ESCALATE_TO_USER = "user";
    public static final String ESCALATE_TO_ROLE = "role";
    public static final String ESCALATE_TO_WORKGROUP = "workgroup";
    public static final String ESCALATE_TO_OWNER = "owner";
    public static final String ESCALATE_TO_ALLOCATED_PROJECT_MANAGER = "allocatedProjectManager";
    public static final String ESCALATE_TO_ALLOCATED_PROJECT_MEMBER = "allocatedProjectMember";
    public static final String ESCALATE_TO_INTERNAL = "internal";
    public static final String SLA_MODULE = "TaskActivityMapping";
    public static final String ASSIGNEE_USERS = "users";
    public static final String ASSIGNEE_ROLES = "roles";
    public static final String ASSIGNEE_WORKGROUP = "work_group";
    public static final String ESCALATE_TARGET_SELF = "self";

    public static final String MODEL_TICKET = "Ticket";
    public static final String MODEL_PROJECT_ENTITY = "ProjectEntity";
    public static final String MODEL_PROJECT_TASK = "ProjectTask";
    public static final String MODEL_ALARM = "Alarm";
    public static final String MODEL_PROJECT = "Project";
    public static final String MODEL_CHANGE_REQUEST = "ChangeRequest";
    public static final String MODEL_RISK = "Risk";
    public static final String MODEL_TASK_ACTION = "TaskActivityMapping";

    private static final String[] SLA_HISTORY_MODEL_ARRAY = { MODEL_TICKET, MODEL_PROJECT_ENTITY, MODEL_PROJECT_TASK,
            MODEL_ALARM, MODEL_PROJECT, MODEL_RISK };

    public static String[] getSlaHistoryModelArray() {
        return SLA_HISTORY_MODEL_ARRAY;
    }

    public static final String ESCALATION_TIME_UNIT_HOUR = "hour";
    public static final String ESCALATION_TIME_UNIT_MINUTE = "minute";
    public static final String ESCALATION_TIME_UNIT_DAY = "day";
    public static final String DEFAULT_ESCALATION_TIME_UNIT = "minute";
    public static final String BREACHED_COUNT = "breachedCount";
    public static final String TOTAL_COUNT = "totalCount";

    public static final String ESCALATE_WHEN_ONTIME = "ontime";
    public static final String ESCALATE_WHEN_BEFORE = "before";
    public static final String ESCALATE_WHEN_AFTER = "after";

    public static final String DEFAULT_TARGET_TABLE = "SLA_HISTORY";
    public static final String DURATION = "duration";
    public static final String PLANNEDENDTIME = "plannedEndTime";
    public static final String PROJECTMEMBER = "Project Member";
    public static final String PROJECTMANAGER = "Project Manager";
    public static final Integer MINIMUMBEFOREDAYS = 0;
    public static final Integer MAXIMUMAFTEREDAYS = 9999999;
    public static final String FREQUENCY_COUNTER = "frequency";
    public static final String TIME = "time";
    public static final String VALUE_KEY = "value";
    public static final String REST_METHOD_KEY = "restMethod";
    public static final String REST_NAME_KEY = "restName";
    public static final String REST_PATH_KEY = "restPath";
    public static final String STATUS_KEY = "status";
    public static final String TICKET_KEY = "ticket";
    public static final String GET_ID_KEY = "getId";
    public static final String GET_WORKSPACE_ID_KEY = "getWorkspaceId";
    public static final String GET_PROJECT_ID_KEY = "getProjectId";
    public static final String ENTITY_ID_KEY = "entityid";
    public static final String NOTIFICATION_TEXT_KEY = "notificationText";
    public static final String PROJECT_NAME_KEY = "projectName";
    public static final String HAS_PROJECT_FOR_TICKET_KEY = "hasProjectForTicket";
    public static final String SLA_NAME_KEY = "slaName";
    public static final String ASSIGNEE_KEY = "assignee";
    public static final String PROJECT_TASK_TYPE_KEY = "ProjectTaskType";
    public static final String SITE_NAME_KEY = "siteName";
    public static final String CHECK_USER_CONFIG_STRING = "*** check user config******";
    public static final String MOBILE_TEMPLATE_STARTING_STRING = "mailtemplates/sla_mobile_";

    public static final String TICKET_MAIL_TEMPLATE_PATH = "";
    public static final String LIKE = " like ";

    public static String getConditionFromJson(String operator) {
        logger.info("@Method: getConditionOperator @opertaor: {} ", operator);
        String oprt = "";
        if (operator != null) {
            if (operator.equalsIgnoreCase(CONDITION_EQUAL)) {
                oprt = "=";
            }
            if (operator.equalsIgnoreCase(CONDITION_NOT_EQUAL)) {
                oprt = "!=";
            }
            if (operator.equalsIgnoreCase(CONDITION_GREATER_THAN)) {
                oprt = ">";
            }
            if (operator.equalsIgnoreCase(CONDITION_GREATER_THAN_EQUAL)) {
                oprt = ">=";
            }
            if (operator.equalsIgnoreCase(CONDITION_LESS_THAN)) {
                oprt = "<";
            }
            if (operator.equalsIgnoreCase(CONDITION_LESS_THAN_EQUAL)) {
                oprt = "<=";
            }
            if (operator.equalsIgnoreCase(CONDITION_CONTAINS)) {
                oprt = LIKE;
            }
            if (operator.equalsIgnoreCase(CONDITION_IS_NULL)) {
                oprt = " is null ";
            }
            if (operator.equalsIgnoreCase(CONDITION_IS_NOT_NULL)) {
                oprt = " is not null ";
            }
            if (operator.equalsIgnoreCase(CONDITION_BEFORE)) {
                oprt = LIKE;
            }
            if (operator.equalsIgnoreCase(CONDITION_AFTER)) {
                oprt = LIKE;
            }

        }
        return oprt;
    }

    public static String getFieldValueAccordingToType(String value, String type, String condition) {
        logger.info("Inside @getFieldValueAccordingToType : @value:{}  @type: {}  @conditions:{}", value, type,
                condition);
        if ((type.equalsIgnoreCase(FIELD_TYPE_STRING) || type.equalsIgnoreCase(FIELD_TYPE_BOOLEAN)
                || type.equalsIgnoreCase(FIELD_TYPE_DATE)) && !(value.contains(","))) {
            logger.info("Inside @getFieldValueAccordingToType @string :{} @conditions:{} ", value, condition);
            if (condition.equalsIgnoreCase(SlaConfigurationUtils.CONDITION_CONTAINS)) {
                value = "'%" + value + "%'";
            } else {
                value = "'" + value + "'";
            }

        } else if (type.equalsIgnoreCase(FIELD_TYPE_ENUM)) {
            logger.info("Inside @getFieldValueAccordingToType @enum :{} @conditions:{} ", value, condition);
            String[] valArray = value.split(",");
            if (valArray.length == 1) {
                if (condition.equalsIgnoreCase(SlaConfigurationUtils.CONDITION_CONTAINS)) {
                    value = "'%" + valArray[0] + "%'";
                } else {
                    value = "'" + valArray[0] + "'";
                }
            }
        } else if (type.equalsIgnoreCase(FIELD_TYPE_DATE)) {
            logger.info("Inside @getFieldValueAccordingToType @date : {} @conditions:{}", value, condition);
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                Date convertedCurrentDate = sdf.parse(value);
                String date = sdf.format(convertedCurrentDate);
                value = "'" + date + "'";
            } catch (ParseException e) {
                throw new BusinessException(SlaUtils.SOMETHING_WENT_WRONG);
            }
        }
        return value;
    }

    public static String getOperatorFromJson(String operator) {
        String op = "";
        if (operator.equalsIgnoreCase(OPERATOR_AND)) {
            op = "and";
        } else if (operator.equalsIgnoreCase(OPERATOR_OR)) {
            op = "or";
        }

        return op;
    }

    public static String getDateFormatforExport() {
        SimpleDateFormat sdf = new SimpleDateFormat("MM-dd-yy");
        return sdf.format(new Date());
    }

}
