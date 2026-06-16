package chaos.tree.benchmark.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation was designed for limit test.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Chaos {
    String description() default "Systemic Boundary Collapse Test";
    long initialNodeCap() default 500_000_000L;
}