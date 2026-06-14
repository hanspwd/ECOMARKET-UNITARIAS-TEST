package com.ecomarket.soporteservice.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ecomarket.soporteservice.model.entity.MensajeChat;

public interface MensajeChatRepository extends JpaRepository<MensajeChat, Long>{

    List<MensajeChat> findByTicketIdOrderByFechaEnvioAsc(Long ticketId);

    List<MensajeChat> findByRemitenteId(Long remitenteId);
    
}
