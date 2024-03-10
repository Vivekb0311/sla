package com.bootnext.platform.sla.model.template;

import java.io.Serializable;

import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.FilterDefs;
import org.hibernate.annotations.Filters;
import org.hibernate.annotations.NamedQueries;
import org.hibernate.annotations.NamedQuery;
import org.hibernate.annotations.ParamDef;
import org.hibernate.envers.Audited;

import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.NamedNativeQuery;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;


@Entity
@Audited
@Table(name = "SLA_HISTORY")
@Data
// @XmlRootElement(name = "SlaHistory")
@DynamicUpdate(value = true)

@NamedNativeQuery(
    name = "SLA_HISTORY_AUD.findAll",
//     query = "select s.CREATED_TIME as createdTime, s.state as state from SLA_HISTORY_AUD s"
        query = "SELECT COUNT(1) AS record_count, STATE, DATE_FORMAT(FROM_UNIXTIME(CREATED_TIME / 1000), '%Y-%m-%d') AS converted_time FROM SLA_HISTORY_AUD WHERE CREATED_TIME BETWEEN UNIX_TIMESTAMP(CURDATE() - INTERVAL 30 DAY) * 1000 AND UNIX_TIMESTAMP(CURDATE()) * 1000 GROUP BY STATE, converted_time"

)
@NamedNativeQuery(name = "getThirtyDaysBreaches", query = "SELECT COUNT(*), DATE_FORMAT(FROM_UNIXTIME(BREACH_TIME  / 1000), '%Y-%m-%d') AS converted_time FROM SLA_HISTORY WHERE BREACH_TIME BETWEEN UNIX_TIMESTAMP(CURDATE() - INTERVAL 30 DAY) * 1000 AND UNIX_TIMESTAMP(CURDATE()) * 1000 GROUP BY converted_time")
@NamedNativeQuery(name = "getBreachProximity", query = "SELECT 'Today' AS period, SUM(CASE WHEN DATE(FROM_UNIXTIME(BREACH_TIME / 1000)) = CURDATE() THEN 1 ELSE 0 END) AS count FROM SLA_HISTORY UNION SELECT 'Tomorrow' AS period, SUM(CASE WHEN DATE(FROM_UNIXTIME(BREACH_TIME / 1000)) = CURDATE() + INTERVAL 1 DAY THEN 1 ELSE 0 END) AS count FROM SLA_HISTORY UNION SELECT 'ThisWeek' AS period, SUM(CASE WHEN DATE(FROM_UNIXTIME(BREACH_TIME / 1000)) BETWEEN CURDATE() AND CURDATE() + INTERVAL 7 DAY THEN 1 ELSE 0 END) AS count FROM SLA_HISTORY UNION SELECT 'ThisMonth' AS period, SUM(CASE WHEN DATE(FROM_UNIXTIME(BREACH_TIME / 1000)) BETWEEN CURDATE() AND CURDATE() + INTERVAL 30 DAY THEN 1 ELSE 0 END) AS count FROM SLA_HISTORY")
// @NamedNativeQuery(name = "historyAudById", query = "SELECT ID, CREATED_TIME, STATE, BREACH_STATUS, BREACHED_At, LEVEL, REVTYPE, BREACH_TIME, APPLICATION, ENTITY_IDENTIFIER, EXECUTED_ON, IN_TIME, OUT_TIME, SLA_IDENTIFIER, CANCEL_CONDITION, LEVEL_TEMPLATE,ON_HOLD_CONDITION, OPERATIONAL_HOURS, RESET_CONDITION, RESUME_CONDITION, START_CONDITION, STOP_CONDITION, TIME_ZONE FROM SLA_HISTORY_AUD WHERE ID=:id ORDER BY REVTYPE DESC")

@NamedNativeQuery(name = "historyAudById", query = "SELECT MODIFIED_TIME, STATE, BREACH_STATUS, LEVEL, SPEL_EXPRESSION FROM SLA_HISTORY_AUD WHERE ID=:id ORDER BY REVTYPE ASC")
@NamedQueries(value = {
        @NamedQuery(name = "findSlaHistoryBySlaIdentifier", query = "select s from SlaHistory s where s.slaIdentifier=:slaIdentifier"),
        @NamedQuery(name = "getAllSlaHistory", query = "select s from SlaHistory s where breachStatus=false"),
        @NamedQuery(name = "getAllBreachedSlaHistory", query = "select s from SlaHistory s where breachStatus=:breachStatus"),
        @NamedQuery(name = "getInProgressOrOnHoldHistoryByEntityIdNameAppAndSlaId", query = "select s from SlaHistory s where s.application=:applicationName and s.executedOn=:entityName and s.entityIdentifier=:entityId and s.slaIdentifier=:slaIdentifier and (s.state='IN_PROGRESS' or s.state='ON_HOLD') "),
        @NamedQuery(name = "getHistoryByEntityId", query = "select s from SlaHistory s where s.entityIdentifier=:entityId"),
        @NamedQuery(name = "getPresentDayBreachActivities", query = "select COUNT(s) from SlaHistory s where s.breachStatus = true and s.breachTime=:currentDate"),
        @NamedQuery(name = "getTopSlaBreached", query = "SELECT st.name, max(e.createdTime), COUNT(e) FROM SlaHistory e join SlaTemplate st on e.slaIdentifier=st.slaId where e.breachStatus = true GROUP BY e.slaIdentifier, st.name  order by count(e) desc "),
        @NamedQuery(name = "getTopSlaTriggered", query = "SELECT st.name, max(e.createdTime), COUNT(e) FROM SlaHistory e join SlaTemplate st on e.slaIdentifier=st.slaId GROUP BY e.slaIdentifier, st.name  order by count(e) desc "),
        @NamedQuery(name = "getSlaBreachProximity", query = "SELECT COUNT(s) from SlaHistory s where s.breachTime=:date"),
        @NamedQuery(name = "getTotalHistoryCount", query = "SELECT COUNT(s) from SlaHistory s"),
        @NamedQuery(name = "getSlaBreachProximityInRange", query = "SELECT COUNT(s) from SlaHistory s where s.breachTime between : startDate and :endDate"),
        @NamedQuery(name = "getTopSlaEntityWise", query = "SELECT count(e), count(CASE WHEN e.state = 'IN_PROGRESS' THEN 1 ELSE NULL END) as In_Progress, count(CASE WHEN e.breachStatus = true THEN 1 ELSE NULL END) as breached, count(CASE WHEN e.state = 'COMPLETED' THEN 1 ELSE NULL END) as completed, e.executedOn FROM SlaHistory as e group by e.executedOn order by count(e) desc "),
        @NamedQuery(name = "levelWiseSlaBreached", query = "SELECT e.breachedAt, COUNT(e) FROM SlaHistory e where e.breachStatus=true GROUP BY e.breachedAt"),
        @NamedQuery(name = "getResolvedTime", query = "SELECT MAX(e.modifiedTime - e.createdTime) as maxTimeDifference, MIN(e.modifiedTime - e.createdTime) as minTimeDifference, AVG(e.modifiedTime - e.createdTime) as avgTimeDifference FROM SlaHistory e WHERE e.state = 'COMPLETED' AND e.executedOn = :executedOn"),
})
@FilterDefs(value = { @FilterDef(name = "getSlaBreachList", parameters = {}),
        @FilterDef(name = "getSlaBreachListWithGroupBy", parameters = {}),
        @FilterDef(name = "getSlaBreachListBySlaName", parameters = {
                @ParamDef(name = "slaName", type = String.class) }),
        @FilterDef(name = "getSlaBreachListBySlaNameWithGroupBy", parameters = {
                @ParamDef(name = "slaName", type = String.class) }) })

@Filters(value = {
        @Filter(name = "getSlaBreachList", condition = "ID in (select s.ID from SLA_HISTORY s join "
                + "SLA_CONFIGURATION sc  on s.SLA_IDENTIFIER=sc.GENERATED_VALUE and   s.MAIL_SENT_TIME is not null)"),

        @Filter(name = "getSlaBreachListWithGroupBy", condition = "ID in (select s.ID from "
                + "SLA_HISTORY s join SLA_CONFIGURATION sc  on s.SLA_IDENTIFIER=sc.GENERATED_VALUE and"
                + "  s.MAIL_SENT_TIME is not null) group by ENTITY_ID, LEVEL"),

        @Filter(name = "getSlaBreachListBySlaName", condition = "ID in (select s.ID from SLA_HISTORY s "
                + "join SLA_CONFIGURATION sc  on s.SLA_IDENTIFIER=sc.GENERATED_VALUE and s.MAIL_SENT_TIME is not null"
                + " and (sc.NAME is null or sc.NAME like :slaName))"),

        @Filter(name = "getSlaBreachListBySlaNameWithGroupBy", condition = "ID in (select s.ID from SLA_HISTORY s"
                + " join SLA_CONFIGURATION sc  on s.SLA_IDENTIFIER=sc.GENERATED_VALUE  and s.MAIL_SENT_TIME is not null"
                + " and (sc.NAME is null or sc.NAME like :slaName)) group by ENTITY_ID,LEVEL"),
})

public class SlaHistory implements Serializable{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID", columnDefinition = "INT")
    private Integer id;

    @Basic
    @NotNull
    @Column(name = "SLA_IDENTIFIER")
    @Size(min = 0, max = 20)
    private String slaIdentifier;

    @Column(name ="STATUS")
    private boolean status;

    @Column(name = "IN_TIME", columnDefinition = "json")
    private String inTime;
    @Column(name = "OUT_TIME", columnDefinition = "json")
    private String outTime;

    @NotNull
    @Basic
    @Column(name = "LEVEL")
    private Integer level;
    
    @NotNull
    @Column(name = "BREACH_STATUS")
    private boolean breachStatus;

    @Column(name = "BREACHED_At")
    private int breachedAt;

    @Basic
    @Column(name = "LAST_UPDATED_TIME")
    private Long lastUpdatedTime;

    @Basic
    @Column(name = "MAIL_SENT_TIME")
    private Long mailSentDate;

    @Basic
    @Column(name = "BREACH_TIME")
    private Long breachTime;

    @Column(name = "OPERATIONAL_HOURS")
    private String operationalHours;

    @Basic
    @Column(name = "EXCLUDE_NON_WORKING_DAY", nullable = false)
    @ColumnDefault(value = "false")
    private Boolean isExcludeNonWorkingDays;

    @Basic
    @NotNull
    @Column(name = "ENTITY_IDENTIFIER")
    @Size(min = 0, max = 20)
    private String entityIdentifier;

    @Basic
    @Column(name = "MODIFIED_TIME", insertable = true, updatable = true)
    private Long modifiedTime;

    @Basic
    @Column(name = "CREATED_TIME", insertable = true, updatable = false)
    private Long createdTime;

    @Basic
    @Column(name = "CREATOR", updatable = false)
    @Size(min = 0, max = 50)
    private String creator;

    @Basic
    @Column(name = "CREATOR_FK", updatable = false)
    private Integer creatorId;

    @Basic
    @Column(name = "MODIFIER")
    @Size(min = 0, max = 50)
    private String lastModifier;

    @Basic
    @Column(name = "MODIFIER_FK")
    private Integer lastModifierId;

    @Basic
    @Column(name = "MODULE_ID")
    private Integer moduleId;

    @Basic
    @Column(name = "MODULE_NAME")
    private String moduleName;
    
    @NotNull
    @Column(name = "APPLICATION")
    private String application;

    @NotNull
    @Column(name = "EXECUTED_ON")
    private String executedOn;

    @Column(name = "ESCALATE_TIME")
    private Long escalateTime;

    @NotNull
    @Column(name = "START_CONDITION", columnDefinition = "json")
    private String startCondition;

    @Column(name = "CANCEL_CONDITION", columnDefinition = "json")
    private String cancelCondition;

    @Column(name = "ON_HOLD_CONDITION", columnDefinition = "json")
    private String onHoldCondition;

    @Column(name = "RESUME_CONDITION", columnDefinition = "json")
    private String resumeCondition;

    @NotNull
    @Column(name = "STOP_CONDITION", columnDefinition = "json")
    private String stopCondition;

    @Column(name = "RESET_CONDITION", columnDefinition = "json")
    private String resetCondition;
    
    @NotNull
    @Column(name = "LEVEL_TEMPLATE", columnDefinition = "json")
    private String levelTemplate;

    @Column(name = "TIME_ZONE")
    private String timeZone;
    
    @NotNull
    @Column(name = "STATE")
    @Enumerated(EnumType.STRING)
    private STAGE state;

    public  enum STAGE {
        IN_PROGRESS, ON_HOLD, COMPLETED, CANCELLED, NEW
    }
    
    @Column(name = "REMAINING_TIME")
    private long remainingTime;
    
    @Column(name = "OWNER")
    private String owner;

    @Column(name = "SPEL_EXPRESSION")
    private String spelExpression;

    @Column(name = "SLA_ID")
    private String slaId;
    
}
