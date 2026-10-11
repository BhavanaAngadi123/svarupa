package com.svarupa.library;

import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReferenceStepRepository extends JpaRepository<ReferenceStep, UUID> {

  Page<ReferenceStep> findByActiveTrue(Pageable pageable);

  @Query(
      """
      SELECT r FROM ReferenceStep r WHERE r.active = true
        AND (:form IS NULL OR LOWER(r.danceForm) = LOWER(:form))
        AND (:difficulty IS NULL OR r.difficulty = :difficulty)
        AND (:q IS NULL OR LOWER(r.title) LIKE :qPattern)
      """)
  Page<ReferenceStep> search(
      @Param("form") String form,
      @Param("difficulty") ReferenceStep.Difficulty difficulty,
      @Param("q") String q,
      @Param("qPattern") String qPattern,
      Pageable pageable);

  List<ReferenceStep> findByTeacherIdAndActiveTrue(UUID teacherId);

  long countByTeacherId(UUID teacherId);
}
