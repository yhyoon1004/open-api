package yh_project.openapi.ai;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AiUsageRepository extends JpaRepository<AiUsage, String> {

    @Query("SELECT SUM(u.totalCost) FROM AiUsage u")
    Double getTotalUsageCost();
}

