package com.tcc.billing_service.infrastructure.persistence.repository;

import com.tcc.billing_service.infrastructure.persistence.entity.BillingRecordJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SpringDataBillingJpaRepository extends JpaRepository<BillingRecordJpaEntity, Long> {
}
