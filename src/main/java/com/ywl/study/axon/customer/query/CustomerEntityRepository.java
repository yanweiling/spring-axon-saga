package com.ywl.study.axon.customer.query;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerEntityRepository extends JpaRepository<CustomerEntity,String> {
}
