package com.tcc.billing_service.infrastructure.persistence.mapper;

import com.tcc.billing_service.domain.model.BillingRecord;
import com.tcc.billing_service.infrastructure.persistence.entity.BillingRecordJpaEntity;
import org.springframework.stereotype.Component;

@Component
public class BillingPersistenceMapper {

    public BillingRecordJpaEntity toEntity(BillingRecord domain) {
        BillingRecordJpaEntity entity = new BillingRecordJpaEntity();
        entity.setId(domain.getId());
        entity.setDataSubjectId(domain.getDataSubjectId());
        entity.setDescription(domain.getDescription());
        entity.setAmount(domain.getAmount());
        entity.setStatus(domain.getStatus());
        entity.setCreatedAt(domain.getCreatedAt());
        return entity;
    }

    public BillingRecord toDomain(BillingRecordJpaEntity entity) {
        BillingRecord domain = new BillingRecord();
        domain.setId(entity.getId());
        domain.setDataSubjectId(entity.getDataSubjectId());
        domain.setDescription(entity.getDescription());
        domain.setAmount(entity.getAmount());
        domain.setStatus(entity.getStatus());
        domain.setCreatedAt(entity.getCreatedAt());
        return domain;
    }
}
