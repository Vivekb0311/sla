package com.bootnext.platform.sla.model.template;

import java.io.Serializable;

import org.hibernate.envers.Audited;
import org.json.JSONArray;

import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedNativeQuery;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Size;
import lombok.Data;


        @NamedQuery(name = "getAllEscalations", query = "select s from Escalation s where status=:status")
        @NamedQuery(name = "getEscalationByGeneratedValue", query = "select s from Escalation s where s.slaId=:generatedValue")
        // @NamedQuery(name = "getEscalationByLevelAndEntityId", query = "select s from Escalation s where s.level=:level and s.entityId=:entityId")
        @NamedQuery(name = "getEscalationByLevelAndSlaHistory", query = "select s from Escalation s where s.level=:level and s.slaHistory.id=:slaHistory")
        @NamedQuery(name = "getEscalationByEntityId", query = "select s from Escalation s where s.entityId=:entityId")
         @NamedQuery(name = "getEscalationBySlaHistory", query = "select s from Escalation s where s.slaHistory.id=:slaHistory")
        @NamedQuery(name = "getPresentDayEscalateActivities", query = "select COUNT(s) from Escalation s where status= true and s.escalationTime=:currentDate")
        @NamedQuery(name = "getTopSlaEscalated", query = "SELECT st.name, max(e.escalationTime), COUNT(e) FROM Escalation e join SlaTemplate st on e.slaId=st.slaId where e.status = true GROUP BY e.slaId, st.name  order by count(e) desc ")
        @NamedNativeQuery(name = "getThirtyDaysEscalations", query = "SELECT COUNT(*), DATE_FORMAT(FROM_UNIXTIME(ESCALATE_TIME  / 1000), '%Y-%m-%d') AS converted_time FROM ESCALATION WHERE ESCALATE_TIME BETWEEN UNIX_TIMESTAMP(CURDATE() - INTERVAL 30 DAY) * 1000 AND UNIX_TIMESTAMP(CURDATE()) * 1000 GROUP BY  converted_time")
        @NamedNativeQuery(name = "getAuditByFk", query = "SELECT ESCALATE_TIME, WHOM_TO_ESCALATE, LEVEL FROM ESCALATION WHERE SLA_HISTORY=:id AND STATUS = TRUE ORDER BY CREATED_TIME DESC")




//@JsonIgnoreProperties(value = { "hibernateLazyInitializer", "handler" })
@Audited
@Entity
@Table(name = "ESCALATION")
@Data
public class Escalation implements Serializable {
    private static final long serialVersionUID = 2550819599709342392L;

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @Column(name = "ID", columnDefinition = "INT")
    private Integer id;
    
    @Column(name = "LEVEL")
    private int level;
    
    @Basic
    @Column(name = "GENERATED_VALUE", nullable = false)
    @Size(min = 0, max = 20)
    private String slaId;
    
    @Column(name = "ESCALATE_TIME")
    private Long escalationTime;
    
    @Convert(converter = JpaConverterForJSONArray.class)
    @Column(name = "WHOM_TO_ESCALATE")
    private JSONArray whomToEscalate;
    
    @Column(name = "STATUS")
    private boolean status;
    
    @Column(name = "BREACH_TIME")
    private Long breachTime;
    
    @Column(name = "ENTITY_ID")
    private String entityId;
    
    @Column(name = "CREATED_TIME", insertable = true, updatable = false)
    private Long createdTime;
    
    @Column(name = "MAIL_TEMPLATE_NAME")
    private String mailTemplateName;

    @Column(name = "TEMPLATE_CONFIGURATION", columnDefinition = "json")
    private String templateConfiguration;
    
    @Column(name = "TIME_ZONE")
    private String timeZone;

    @Column(name = "SEND_EMAIL")
    private Boolean sendEmail;

    @Column(name= "SEND_NOTIFICATION")
    private Boolean sendNotification;

    @Column(name= "ENABLE_GEOGRAPHY")
    private Boolean enableGeography;

    @Column(name = "ENTITY_FIELD_FOR_OWNER")
    private String entityFieldForOwner;

    @Column(name = "ENTITY_FIELD_FOR_GEOGRAPHY")
    private String entityFieldForGeography;

    @ManyToOne
    @JoinColumn(name = "SLA_HISTORY", nullable = false)
    private SlaHistory slaHistory;



    
}
