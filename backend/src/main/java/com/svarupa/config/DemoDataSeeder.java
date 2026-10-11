package com.svarupa.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.svarupa.common.Role;
import com.svarupa.library.JointAngles;
import com.svarupa.library.ReferenceStep;
import com.svarupa.library.ReferenceStepRepository;
import com.svarupa.user.User;
import com.svarupa.user.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

/**
 * Seeds a demo teacher and three Kuchipudi reference steps on a fresh
 * database, so the library is never empty on first boot.
 */
@Configuration
public class DemoDataSeeder {

  private static final Logger log = LoggerFactory.getLogger(DemoDataSeeder.class);

  @Bean
  CommandLineRunner seedDemoData(
      UserRepository users,
      ReferenceStepRepository references,
      PasswordEncoder passwordEncoder,
      ObjectMapper objectMapper) {
    return args -> seed(users, references, passwordEncoder, objectMapper);
  }

  @Transactional
  void seed(
      UserRepository users,
      ReferenceStepRepository references,
      PasswordEncoder passwordEncoder,
      ObjectMapper objectMapper) {
    if (references.count() > 0) {
      return;
    }
    var teacher =
        users
            .findByEmailIgnoreCase("guru@svarupa.app")
            .orElseGet(
                () -> {
                  var u =
                      new User(
                          "guru@svarupa.app",
                          passwordEncoder.encode("Svarupa!123"),
                          "Demo Guru",
                          Role.TEACHER);
                  return users.save(u);
                });

    seedStep(
        references,
        objectMapper,
        teacher,
        "Araimandi — Half-sitting stance",
        "The foundational Kuchipudi posture: knees bent outward, torso upright, arms in Natyarambha.",
        new JointAngles(95, 95, 160, 160, 45, 45, 100, 100),
        ReferenceStep.Difficulty.BEGINNER);
    seedStep(
        references,
        objectMapper,
        teacher,
        "Samabhanga — Standing straight",
        "Neutral standing: weight even, shoulders open, gaze forward. The reset between steps.",
        new JointAngles(175, 175, 170, 170, 15, 15, 175, 175),
        ReferenceStep.Difficulty.BEGINNER);
    seedStep(
        references,
        objectMapper,
        teacher,
        "Alapadma Hasta — Lotus hand",
        "Fingers spread like lotus petals, elbows softly rounded, wrists lifted.",
        new JointAngles(170, 170, 120, 120, 35, 35, 170, 170),
        ReferenceStep.Difficulty.INTERMEDIATE);
    log.info("Seeded demo teacher + 3 reference steps");
  }

  private void seedStep(
      ReferenceStepRepository references,
      ObjectMapper objectMapper,
      User teacher,
      String title,
      String description,
      JointAngles angles,
      ReferenceStep.Difficulty difficulty) {
    try {
      var step = new ReferenceStep();
      step.setTeacher(teacher);
      step.setTitle(title);
      step.setDanceForm("Kuchipudi");
      step.setDescription(description);
      step.setJointAngles(objectMapper.writeValueAsString(angles));
      step.setDifficulty(difficulty);
      references.save(step);
    } catch (Exception e) {
      throw new IllegalStateException("demo seed failed", e);
    }
  }
}
