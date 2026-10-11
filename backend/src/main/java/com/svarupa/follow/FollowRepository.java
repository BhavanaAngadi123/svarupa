package com.svarupa.follow;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface FollowRepository extends JpaRepository<Follow, UUID> {

  Optional<Follow> findByStudentIdAndTeacherId(UUID studentId, UUID teacherId);

  boolean existsByStudentIdAndTeacherId(UUID studentId, UUID teacherId);

  @Query("SELECT f.teacher.id FROM Follow f WHERE f.student.id = :studentId")
  List<UUID> findTeacherIdsByStudent(@Param("studentId") UUID studentId);

  @Query("SELECT f FROM Follow f JOIN FETCH f.student WHERE f.teacher.id = :teacherId ORDER BY f.createdAt DESC")
  List<Follow> findByTeacherWithStudent(@Param("teacherId") UUID teacherId);

  @Query("SELECT COUNT(f) FROM Follow f WHERE f.teacher.id = :teacherId")
  long countByTeacher(@Param("teacherId") UUID teacherId);
}
