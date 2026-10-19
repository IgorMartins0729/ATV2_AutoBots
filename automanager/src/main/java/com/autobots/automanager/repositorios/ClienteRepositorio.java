package com.autobots.automanager.repositorios;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.autobots.automanager.entidades.Cliente;

public interface ClienteRepositorio extends JpaRepository<Cliente, Long> {
    Optional<Cliente> findByTelefonesId(Long id);

    Optional<Cliente> findByDocumentosId(Long id);

    Optional<Cliente> findByEnderecoId(Long id);
}