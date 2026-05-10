package com.tcc.billing_service.infrastructure.persistence.repository;

import com.tcc.billing_service.domain.model.BillingRecord;
import com.tcc.billing_service.domain.repository.BillingRecordRepository;
import com.tcc.billing_service.infrastructure.persistence.entity.BillingRecordJpaEntity;
import com.tcc.billing_service.infrastructure.persistence.mapper.BillingPersistenceMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
@Transactional(readOnly = true)
public class JpaBillingRepositoryAdapter implements BillingRecordRepository {

    private final SpringDataBillingJpaRepository springDataRepository;
    private final BillingPersistenceMapper mapper;

    public JpaBillingRepositoryAdapter(SpringDataBillingJpaRepository springDataRepository,
                                       BillingPersistenceMapper mapper) {
        this.springDataRepository = springDataRepository;
        this.mapper = mapper;
    }

    @Override
    @Transactional
    public BillingRecord save(BillingRecord record) {
        BillingRecordJpaEntity entity = mapper.toEntity(record);
        if (entity.getCreatedAt() == null) {
            entity.setCreatedAt(java.time.Instant.now());
        }
        BillingRecordJpaEntity saved = springDataRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<BillingRecord> findById(Long id) {
        return springDataRepository.findById(id)
                .map(mapper::toDomain);
    }
}
