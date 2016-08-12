package com.github.sgwhp.openapm.sample.Instrumentation;

/**
 * Created by rarshion on 16/8/11.
 */

import java.lang.annotation.*;

@Target({ ElementType.METHOD })
public @interface SkipTrace {
}
