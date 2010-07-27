package org.rsbot.util;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;

public final class ThrowableUtils {

    public static String throwableToString(final Throwable t) {
        if (t == null)
            return "";
        final Writer exception = new StringWriter();
        final PrintWriter printWriter = new PrintWriter(exception);
        t.printStackTrace(printWriter);
        return exception.toString();
    }
}
