package com.pavan.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.pavan.model.PredictionHistory;

public interface PredictionHistoryRepository extends JpaRepository<PredictionHistory, Long> {

}
