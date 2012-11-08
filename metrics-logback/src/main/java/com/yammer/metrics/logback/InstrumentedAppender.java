package com.yammer.metrics.logback;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.AppenderBase;
import com.yammer.metrics.Metrics;
import com.yammer.metrics.core.Meter;
import com.yammer.metrics.core.MetricName;
import com.yammer.metrics.core.MetricsRegistry;

/**
 * A Logback {@link AppenderBase} which has six meters, one for each logging level and one for the
 * total number of statements being logged.
 */
public class InstrumentedAppender extends AppenderBase<ILoggingEvent> {
    private final Meter all;
    private final Meter trace;
    private final Meter debug;
    private final Meter info;
    private final Meter warn;
    private final Meter error;

    public InstrumentedAppender() {
        this(Metrics.defaultRegistry());
    }

    public InstrumentedAppender(MetricsRegistry registry) {
        this.all = registry.add(MetricName.name(Appender.class, "all"), new Meter("statements"));
        this.trace = registry.add(MetricName.name(Appender.class, "trace"), new Meter("statements"));
        this.debug = registry.add(MetricName.name(Appender.class, "debug"), new Meter("statements"));
        this.info = registry.add(MetricName.name(Appender.class, "info"), new Meter("statements"));
        this.warn = registry.add(MetricName.name(Appender.class, "warn"), new Meter("statements"));
        this.error = registry.add(MetricName.name(Appender.class, "error"), new Meter("statements"));
    }

    @Override
    protected void append(ILoggingEvent event) {
        all.mark();
        switch (event.getLevel().toInt()) {
            case Level.TRACE_INT:
                trace.mark();
                break;
            case Level.DEBUG_INT:
                debug.mark();
                break;
            case Level.INFO_INT:
                info.mark();
                break;
            case Level.WARN_INT:
                warn.mark();
                break;
            case Level.ERROR_INT:
                error.mark();
                break;
        }
    }
}
