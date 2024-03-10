package com.bootnext.platform.sla.model.template;


import java.io.Serializable;

import org.hibernate.envers.Audited;
import org.json.JSONArray;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;
import lombok.Data;

@NamedQueries(value = {
        @NamedQuery(name = "getSlaLevelTemplateByGeneratedValue", query = "select s from SlaTemplate s   where s.slaId=:generatedValue"),
        @NamedQuery(name = "getSlaLevelTemplateById", query = "select s from SlaLevelTemplate s where s.slaTemplate.id=:id"),
        @NamedQuery(name = "getSlaLevelTemplateByIdAndLevel", query ="select s from SlaLevelTemplate s where s.slaTemplate.id=:id and level=:level"),
}
)

@Audited
@Entity
@Table(name = "SLA_LEVEL_TEMPLATE")
@Data
public class SlaLevelTemplate implements Serializable {

    private static final long serialVersionUID = 2550819599709342392L;

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @Column(name = "ID", columnDefinition = "INT")
    private Integer id;
    
    @ManyToOne
    @JoinColumn(name = "SLA_TEMPLATE", nullable = false)
    private SlaTemplate slaTemplate;

    @Column(name = "DURATION", columnDefinition = "json")
    private String duration;
    
    @Column(name = "WHEN_TO_ESCALATE")
    private String whenToEscalate;

    @Column(name = "DURATION_IN_MINUTE")
    private String durationInMinute;

    @Column(name = "ESCALATE_WHOM")
    private String escalateWhom;

    @Convert(converter = JpaConverterForJSONArray.class)
    @Column(name = "ESCALATION_TARGET")
    private JSONArray escalationTarget;

    @Column(name = "EMAIL_TEMPLATE")
    private String emailTemplate;

    @Column(name = "LEVEL")
    private int level;

    @Column(name = "TEMPLATE_CONFIGURATION", columnDefinition = "json")
    private String templateConfiguration;

    @Column(name = "SEND_EMAIL")
    private Boolean sendEmail;

    @Column(name= "SEND_NOTIFICATION")
    private Boolean sendNotification;

    @Column(name = "ENTITY_FOR_GEO")
    private String entityForGeo;

    @Column(name = "ENTITY_FOR_OWNER")
    private String entityForOwner;
}
