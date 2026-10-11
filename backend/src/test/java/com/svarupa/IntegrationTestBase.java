package com.svarupa;

import com.svarupa.common.Role;
import com.svarupa.follow.FollowRepository;
import com.svarupa.library.JointAngles;
import com.svarupa.library.ReferenceStep;
import com.svarupa.library.ReferenceStepRepository;
import com.svarupa.payment.PaymentRepository;
import com.svarupa.practice.PracticeSessionRepository;
import com.svarupa.user.User;
import com.svarupa.user.UserRepository;
import java.util.List;
import java.util.function.Supplier;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

/** Base for integration tests: full app context, H2, real Flyway migrations. */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
public abstract class IntegrationTestBase {

  @Autowired protected UserRepository users;
  @Autowired protected ReferenceStepRepository references;
  @Autowired protected PracticeSessionRepository sessions;
  @Autowired protected FollowRepository follows;
  @Autowired protected PaymentRepository payments;
  @Autowired protected PasswordEncoder passwordEncoder;

  protected User student;
  protected User teacher;
  protected User admin;
  protected ReferenceStep reference;

  static final JointAngles POSE_A =
      new JointAngles(95, 95, 160, 160, 45, 45, 100, 100);
  static final JointAngles POSE_PERFECT_COPY =
      new JointAngles(95, 95, 160, 160, 45, 45, 100, 100);

  /** Run code with a SecurityContext carrying the user's role (for @PreAuthorize). */
  protected <T> T runAs(User user, Supplier<T> work) {
    var auth =
        new UsernamePasswordAuthenticationToken(
            user.getEmail(),
            null,
            List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name())));
    var ctx = SecurityContextHolder.createEmptyContext();
    ctx.setAuthentication(auth);
    SecurityContextHolder.setContext(ctx);
    try {
      return work.get();
    } finally {
      SecurityContextHolder.clearContext();
    }
  }

  /** Run a void action with a SecurityContext carrying the user's role. */
  protected void runAsVoid(User user, Runnable work) {
    runAs(user, () -> {
      work.run();
      return null;
    });
  }

  @BeforeEach
  void setUpBase() {
    String suffix = java.util.UUID.randomUUID().toString().substring(0, 8);
    student =
        users.save(
            new User(
                "stu-" + suffix + "@test.local",
                passwordEncoder.encode("Student!123"),
                "Test Student",
                Role.STUDENT));
    teacher =
        users.save(
            new User(
                "tea-" + suffix + "@test.local",
                passwordEncoder.encode("Teacher!123"),
                "Test Teacher",
                Role.TEACHER));
    admin =
        users.save(
            new User(
                "adm-" + suffix + "@test.local",
                passwordEncoder.encode("Admin!123"),
                "Test Admin",
                Role.ADMIN));

    reference = new ReferenceStep();
    reference.setTeacher(teacher);
    reference.setTitle("Araimandi Test");
    reference.setDanceForm("Kuchipudi");
    reference.setDescription("Test stance");
    try {
      reference.setJointAngles(
          new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(POSE_A));
    } catch (Exception e) {
      throw new IllegalStateException(e);
    }
    reference.setDifficulty(ReferenceStep.Difficulty.BEGINNER);
    reference = references.save(reference);
  }
}
