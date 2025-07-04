// In src/main/java/com/swp2/demo/service/AnalysisResultService.java
package com.swp2.demo.service;

import com.swp2.demo.repository.AnalysisResultRepository; // Assuming you have this
import com.swp2.demo.entity.AnalysisResultEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AnalysisResultService {

    private final AnalysisResultRepository analysisResultRepository;

    @Autowired
    public AnalysisResultService(AnalysisResultRepository analysisResultRepository) {
        this.analysisResultRepository = analysisResultRepository;
    }

    /**
     * Finds the latest AnalysisResultEntity for a given user ID.
     * Assumes your repository has a method to find by user and order by creation time.
     * You might need to add this method to your AnalysisResultRepository.
     *
     * @param userId The ID of the user.
     * @return An Optional containing the latest AnalysisResultEntity, or empty if none found.
     */
    public Optional<AnalysisResultEntity> findLatestByUserId(Long userId) {
        // You will need to define this method in your AnalysisResultRepository
        // Example: findTopByUser_IdOrderByCreatedAtDesc(Long userId)
        return analysisResultRepository.findTopByUser_IdOrderByCreatedAtDesc(userId);
    }

    // You might add other methods here for saving, deleting, etc.
}