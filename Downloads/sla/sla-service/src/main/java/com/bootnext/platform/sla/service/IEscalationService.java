package com.bootnext.platform.sla.service;

import java.util.List;

import com.bootnext.platform.sla.model.template.Escalation;

public interface IEscalationService {


    String scheduleEscalateUser();

    List<Escalation> getEscalationByGeneratedValue(String generatedValue);
    String getTopSlaEscalated();

}
