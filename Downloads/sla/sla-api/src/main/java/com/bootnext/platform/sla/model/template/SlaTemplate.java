package com.bootnext.platform.sla.model.template;

import java.io.Serializable;

import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.FilterDefs;
import org.hibernate.annotations.Filters;
import org.hibernate.annotations.NamedQueries;
import org.hibernate.annotations.NamedQuery;
import org.hibernate.annotations.ParamDef;
import org.hibernate.envers.Audited;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@NamedQueries({
        @NamedQuery(name = "getActiveSlaTemplate", query = "select s from SlaTemplate s where s.isActive='True'"),
        @NamedQuery(name = "getSlaTemplateByGeneratedValue", query = "select s from SlaTemplate s   where s.slaId=:generatedValue"),
        @NamedQuery(name = "getTemplateCountByStatus", query = "select COUNT(s) from SlaTemplate s   where s.isActive=:isActive"),
        @NamedQuery(name = "getTemplateCountByApproval", query = "select COUNT(s) from SlaTemplate s where s.status= true and s.approval=:approval"),
        @NamedQuery(name = "getTemplateEntityWise", query = "SELECT e.executedOn, COUNT(e) FROM SlaTemplate e GROUP BY e.executedOn"),
        @NamedQuery(name = "isTemplateExist", query = "SELECT e FROM SlaTemplate e where name=:name "),
        @NamedQuery(name = "getSlaTemplateByApplication", query = "SELECT s FROM SlaTemplate s where application=:application")
})
@NamedQuery(name = "getSlaTemplateByApplicationAndExecutedOn", query = "SELECT e FROM SlaTemplate e WHERE e.application=:application AND e.executedOn=:executedOn AND e.isActive='True'")
@FilterDefs(value = { @FilterDef(name = "getAllSlaForUser", parameters = {
        @ParamDef(name = "userId", type = Integer.class) }) })

@Filters(value = { @Filter(name = "getAllSlaForUser", condition = "CREATOR_FK=:userId") })

@JsonIgnoreProperties(value = { "hibernateLazyInitializer", "handler" })
@Audited
@Entity
@Table(name = "SLA_TEMPLATE")
@Data
// @DynamicUpdate(value = true)
public class SlaTemplate implements Serializable {
    /**
     *
     */
    private static final long serialVersionUID = 2550819599709342392L;

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @Column(name = "ID", columnDefinition = "INT")
    private Integer id;

    @Basic
    @NotNull
    @Column(name = "NAME", nullable = false)
    @Size(min = 0, max = 150)
    private String name;

    @Basic
    @NotNull
    @Column(name = "GENERATED_VALUE", unique = true, nullable = false)
    @Size(min = 0, max = 20)
    private String slaId;

    @Basic
    @Column(name = "DESCRIPTION")
    private String description;

    @Basic
    @Column(name = "ACTIVE", nullable = false)
    private String isActive;

    @Column(name = "STATUS")
    private boolean status;

    @Basic
    @NotNull
    @Column(name = "EXECUTED_ON")
    private String executedOn;

    @Basic
    @Column(name = "EXCLUDE_NON_WORKING_DAY", nullable = false)
    @ColumnDefault(value = "false")
    private Boolean isExcludeNonWorkingDays;

    @NotNull
    @Column(name = "TIME_ZONE_DATA")
    private String timeZoneData;

    @NotNull
    @Column(name = "APPLICATION")
    private String application;

    @NotNull
    @Column(name = "DURATION", columnDefinition = "json")
    private String duration;

    @NotNull
    @Column(name = "BREACH_DATE")
    private String breachDate;

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

    @Column(name = "CONFIG", columnDefinition = "json")
    private String config;

    @NotNull
    @Lob()
    @Column(name = "LEVEL_TEMPLATE", columnDefinition = "json", length = 10000)
    private String levelTemplate;

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

    @Column(name = "OPERATIONAL_HOURS")
    private String operationalHours;

    @Column(name = "IN_TIME", columnDefinition = "json")
    private String inTime;

    @Column(name = "OUT_TIME", columnDefinition = "json")
    private String outTime;

    @Column(name = "APPROVAL")
    @Enumerated(EnumType.STRING)
    private APPROVAL approval;

    public enum APPROVAL {
        PENDING, APPROVED
    }

    public enum STATUS {
        TRUE, FALSE, DRAFT
    }

}
