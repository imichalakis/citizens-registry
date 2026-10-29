package gr.ekpa.citizens.service;

import gr.ekpa.citizens.domain.Citizen;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface CitizenRepository extends JpaRepository<Citizen, String>, JpaSpecificationExecutor<Citizen> {
}
