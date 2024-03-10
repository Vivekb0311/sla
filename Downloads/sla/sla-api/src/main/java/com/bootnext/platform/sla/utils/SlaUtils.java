package com.bootnext.platform.sla.utils;


import java.text.SimpleDateFormat;
import java.util.Date;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.configuration.reloading.FileChangedReloadingStrategy;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public class SlaUtils {

    /** The Constant successJson. */
    public static final String SUCCESSJSON = "{\"status\":\"success\"}";
    /** The Constant failureJson. */
    public static final String FAILUREJSON = "{\"status\":\"failure\"}";

    public static final String INSIDE_CLASS = "Inside @class: {}";
    public static final String ERROR_OCCURRED = "Error occured inside @class:";
    public static final String DATE_FORMAT = "yyyy-MM-dd hh:mm a";
    /** The Constant TICKET_STATUS_CLOSED. */
    /* TICKET_STATUS_CLOSED */
    public static final String TICKET_STATUS_CLOSED = "Closed";
    public static final String INSIDE_METHOD = "Inside method: {}";

    public static final String PLATFORM_APP_SERVER_BASE_URL = "PLATFORM_APP_SERVER_BASE_URL";

    public static final String PLATFORM_API_L1_MANAGER_USERS_URL = "PLATFORM_API_L1_MANAGER_USERS_URL";

    public static final String TRIBE_URL = "TRIBE_URL";
    /** The Constant SLA_EXPORT_XLSX_SAMPLE. */
    public static final String SLA_EXPORT_XLSX_SAMPLE = "SLA_HISTORY_REPORT.xlsx";

    public static final String CONFIG_PROPS = "config.properties";
    public static final String EXCEPTION_STACK_TRACE = " Exception Stack Trace ";
    public static final String EXCEPTION_MSG = "Exception Message";
    public static final String ID = "id";
    public static final String SEARCH = "search";
    public static final String ORDERBY = "orderBy";
    public static final String UPPERLIMIT = "lowerLimit";
    public static final String LOWERLIMIT = "upperLimit";
    public static final String ORDERTYPE = "orderType";
    public static final String PAGE = "page";
    public static final String SYSTEM_TYPE_SF = "PLATFORM2";
    public static final String START_DATE = "startDate";
    public static final String SLA_NAME = "slaName";
    public static final String END_DATE = "endDate";
    public static final String ENTITY_ID = "entityId";
    public static final String LEVEL = "level";
    public static final String MODULE_NAME = "moduleName";
    public static final String ENTITY_NAME = "entityName";
    public static final String SLA_ID = "slaId";
    public static final String SITE_REFERENCE_ID = "siteReferenceId";
    public static final String PROJECT_NAME = "projectName";
    public static final String LIMIT = "limit";
    public static final String BASE_WAR_DIRECTORY = "BASE_WAR_DIRECTORY";
    public static final String SOMETHING_WENT_WRONG = "Something went wrong";
    public static final String SLA_ALREADY_EXIST = "SLA template already exists";
    public static final String SLA_OLA_REPORT = "SLA_OLA_REPORT";
    public static final String SLA_OLA_FOLDER = "SlaOla/";
    public static final String ENTITY_IDENTIFIER = "entityIdentifier";
    public static final String YYYY_MM_DD = "yyyy-MM-dd";
    public static final String BREACH_DATE = "breachDate";
    public static final String ESCALATE_WHEN = "escalateWhen";
    public static final String ESCALATE = "escalate";
    public static final String VALUE = "value";
    public static final String MATCHED = "matched";
    public static final String CURLY_BRACKETS = "{}";
    public static final String GET_ALL_ESCALATIONS = "getAllEscalations";
    public static final String SLACONFIGURATION_CREATE = "SLACONFIGURATION_CREATE";
    public static final String SLACONFIGURATION_UPDATE = "SLACONFIGURATION_UPDATE";
    public static final String GET_ESCALATION_BY_GERATED_VALUE = "getEscalationByGeneratedValue";
    public static final String CSV_FILE_SEPARATOR = "CSV_FILE_SEPARATOR";
    public static final String PATH_DELIMITER = "/";
    public static final String FONTS = "/app/assets/fonts/pdfFonts/Arial-Unicode-Regular.ttf";
    public static final String EMAIL = "EMAIL";
    public static final String DATE = "date";
    public static final String NAME = "name";
    public static final String EXECUTED_ON = "executedOn";
    public static final String COUNT = "count";
    public static final String USER_GROUP = "userGroup";
    public static final String IN_PROGRESS = "In progress";
    public static final String BREACHED = "Breached";
    public static final String PIE_CHART = "pieChart";
    public static final String COMPLETED = "Completed";
    public static final String MIN = "min";
    public static final String MAX = "max";
    public static final String AVG = "avg";
    public static final String USER = "user";
    public static final String VENDOR = "vendor";
    public static final String USER_SPACE = "userSpace";
    public static final String USER_NAME = "userName";
    public static final String BUSINESS_UNIT = "businessUnit";
    public static final String RULES = "rules";
    public static final String BACK_SLASH = "/";
    public static final String FIRST_DATE = "date1";
    public static final String SECOND_DATE = "date2";
    public static final String GEO = "primaryGeoL1";
    public static final String GENERATED_VALUE = "generatedValue";
    public static final String FALSE = "false";
    public static final String TRUE = "true";
    public static final String PENDING = "PENDING";
    public static final String SUBJECT = "subject";
    public static final String CONTENT = "content";
    public static final String STATE = "state";
    public static final String ACTION = "action";
    public static final String DESCRIPTION = "description";
    public static final String FIELDS = "fields";
    public static final String VAULE = "value";
    public static final String CONFIG = "config";
    public static final String LEVEL1 = "LEVEL 1";
    public static final String ACTION_TIME = "actionTime";
    public static final String LEVEL_TAG = "level-";
    public static final String MODIFIED_TIME = "modifiedTime";
    public static final String SPEL_EXPRESSION = "spelExpression";
    public static final String HOURS = "hours";
    public static final String MINUTES =   "minutes";
    private static PropertiesConfiguration config;
    private static Logger logger = LogManager.getLogger(SlaUtils.class.getName());

    public static boolean isStringExistInStringList(String mainString, String findString) {
        String[] stringValue = mainString.split("\\s*,\\s*");
        for (String s : stringValue) {
            if (s.equalsIgnoreCase(findString)) {
                return true;
            }
        }
        return false;
    }

    static {
        try {
            config = new PropertiesConfiguration(CONFIG_PROPS);
            config.setReloadingStrategy(new FileChangedReloadingStrategy());
        } catch (ConfigurationException e) {
            logger.error(e.getMessage());
        } finally {
            logger.info("Inside method findClassesFromPackage with Finally Block 1");
        }
    }


    public static String getConfigPropWithoutDecryption(String key) {
        return config.getProperty(key) != null ? config.getProperty(key).toString() : (key);
    }



    public static String formatDate(Date date, String pattern) {
        if (date != null) {
            SimpleDateFormat formateDate = new SimpleDateFormat(pattern);
            return formateDate.format(date);
        } else {
            SimpleDateFormat formateDate = new SimpleDateFormat(DATE_FORMAT);
            return formateDate.format(new Date());

        }
    }

    public static String getDateFormatforExport() {
        SimpleDateFormat sdf = new SimpleDateFormat("MM-dd-yy");
        return sdf.format(new Date());
    }

}
