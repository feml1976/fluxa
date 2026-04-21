package com.fml.fluxa.auth.infrastructure.persistence;

import com.fml.fluxa.auth.domain.model.Group;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GroupJpaRepository extends JpaRepository<Group, Long> {
}
