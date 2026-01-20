package com.example.tricol.tricolspringbootrestapi.repository;

import com.example.tricol.tricolspringbootrestapi.model.Product;
import com.example.tricol.tricolspringbootrestapi.model.StockSlot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StockSlotRepository extends JpaRepository<StockSlot,Long> {
    
    List<StockSlot> findByProductAndAvailableQuantityGreaterThanOrderByEntryDateAsc(
            Product product, Double quantity);
    
    List<StockSlot> findByProductAndAvailableQuantityGreaterThan(
            Product product, Double quantity);
    
    List<StockSlot> findByProduct(Product product);

    Optional<StockSlot> findTopByOrderByIdDesc();

    @Query("SELECT s FROM StockSlot s WHERE s.lotNumber LIKE :yearPattern ORDER BY s.lotNumber DESC LIMIT 1")
    Optional<StockSlot> findLatestLotNumberByYear(String yearPattern);
}
