package dev.technici4n.moderndynamics.test;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface MdGameTest {
    int timeoutTicks() default 100;

    String batch() default "defaultBatch";

    int rotationSteps() default 0;

    long setupTicks() default 0L;

    int attempts() default 1;

    int requiredSuccesses() default 1;
}
