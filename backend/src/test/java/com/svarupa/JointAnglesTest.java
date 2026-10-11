package com.svarupa;

import static org.assertj.core.api.Assertions.*;

import com.svarupa.library.JointAngles;
import jakarta.validation.Validation;
import org.junit.jupiter.api.Test;

/** Bean validation on the joint-angle contract. */
class JointAnglesTest {

  private final jakarta.validation.Validator validator =
      Validation.buildDefaultValidatorFactory().getValidator();

  @Test
  void validAnglesPass() {
    var angles = new JointAngles(95, 95, 160, 160, 45, 45, 100, 100);
    assertThat(validator.validate(angles)).isEmpty();
  }

  @Test
  void outOfRangeAnglesFail() {
    var angles = new JointAngles(95, 95, 160, 160, 45, 45, 100, 200);
    assertThat(validator.validate(angles)).isNotEmpty();
  }

  @Test
  void negativeAnglesFail() {
    var angles = new JointAngles(-5, 95, 160, 160, 45, 45, 100, 100);
    assertThat(validator.validate(angles)).isNotEmpty();
  }

  @Test
  void deviationsAreAbsolute() {
    var a = new JointAngles(100, 95, 160, 160, 45, 45, 100, 100);
    var b = new JointAngles(90, 95, 160, 160, 45, 45, 100, 100);
    assertThat(a.deviationsFrom(b).get("lKnee")).isEqualTo(10.0);
    assertThat(b.deviationsFrom(a).get("lKnee")).isEqualTo(10.0);
  }
}
