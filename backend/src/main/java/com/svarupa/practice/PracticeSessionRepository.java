package com.svarupa.practice;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PracticeSessionRepository extends JpaRepository<PracticeSession, UUID> {

  Page<PracticeSession> findByStudentIdOrderByStartedAtDesc(UUID studentId, Pageable pageable);

  List<PracticeSession> findByStudentIdAndStartedAtAfterOrderByStartedAtAsc(
      UUID studentId, Instant after);

  @Query(
      "SELECT COUNT(s) FROM PracticeSession s WHERE s.student.id = :studentId AND s.overallMatchPct IS NOT NULL")
  long countScoredByStudent(@Param("studentId") UUID studentId);

  @Query(
      "SELECT AVG(s.overallMatchPct) FROM PracticeSession s WHERE s.student.id = :studentId AND s.overallMatchPct IS NOT NULL")
  Double avgMatchByStudent(@Param("studentId") UUID studentId);

  @Query(
      "SELECT COALESCE(SUM(s.durationSeconds), 0) FROM PracticeSession s WHERE s.student.id = :studentId")
  long totalSecondsByStudent(@Param("studentId") UUID studentId);
}
