package com.lsvp.InventoryManagement.repository;

import com.lsvp.InventoryManagement.entity.Container;
import com.lsvp.InventoryManagement.enums.ContainerType;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;

public interface IContainerRepository extends JpaRepository<Container, Long> {
    void deleteById(Long id);

    Optional<Container> findByCode(String code);

    Optional<Container> findByType(ContainerType type);
    
    // Paginação e filtros
    Page<Container> findByCodeContainingIgnoreCase(String code, org.springframework.data.domain.Pageable pageable);

    @Query(
    value = "SELECT DISTINCT c FROM Container c JOIN c.units u JOIN u.product p JOIN p.category cat WHERE LOWER(cat.description) LIKE LOWER(CONCAT('%', :category, '%'))",
    countQuery = "SELECT COUNT(DISTINCT c) FROM Container c JOIN c.units u JOIN u.product p JOIN p.category cat WHERE LOWER(cat.description) LIKE LOWER(CONCAT('%', :category, '%'))"
)
Page<Container> findByProductCategoryDescriptionContainingIgnoreCase(
        @Param("category") String category, 
        Pageable pageable
);
}
