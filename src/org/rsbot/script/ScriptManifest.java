package org.rsbot.script;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface ScriptManifest {
    String[] authors();

    String category() default "Other";

    String description() default "";

    String name();

    String summary() default "";

    double version() default 1.0;
}
