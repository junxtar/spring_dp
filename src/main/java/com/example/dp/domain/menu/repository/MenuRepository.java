package com.example.dp.domain.menu.repository;

import com.example.dp.domain.menu.entity.Menu;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

public interface MenuRepository extends JpaRepository<Menu, Long> {

    Optional<Menu> findByName(String name);

    List<Menu> findByOrderByCreatedAt();

    boolean existsByName(String name);

    List<Menu> findByNameContains(String menuName);

    @Lock(LockModeType.PESSIMISTIC_READ)
    @Query("select m from Menu m where m.id = :id")
    Optional<Menu> findByIdWithPessimisticLock(Long id);
}
