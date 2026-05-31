package com.playstory.excel.repository;

import com.playstory.excel.entity.Order;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByIdGreaterThanOrderByIdAsc(
            Long lastId,
            Pageable pageable
    );
}
