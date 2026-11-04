package com.adega.repository;

import com.adega.model.Bebida;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BebidaRepository extends JpaRepository<Bebida, Long> {

    Optional<Bebida> findByNomeIgnoreCase(String nome);

    boolean existsByNomeIgnoreCase(String nome);

    boolean existsByNomeIgnoreCaseAndIdNot(String nome, Long id);
}
