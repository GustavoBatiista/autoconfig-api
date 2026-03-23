package com.gustavobatista.autoconfig.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.gustavobatista.autoconfig.entity.Client;

@Repository
public interface ClientRepository extends JpaRepository<Client, Long> {

    boolean existsByPhoneNumber(String phoneNumber);

    boolean existsByPhoneNumberAndIdNot(String phoneNumber, Long id);

}
