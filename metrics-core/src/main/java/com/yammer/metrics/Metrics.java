package com.yammer.metrics;

import com.yammer.metrics.core.Metric;
import com.yammer.metrics.core.MetricRegistry;
import com.yammer.metrics.reporting.JmxReporter;

/**
 * A default metrics registry.
 */
public class Metrics {
    private static final MetricRegistry DEFAULT_REGISTRY = new MetricRegistry();
    private static final JmxReporter JMX_REPORTER = new JmxReporter(DEFAULT_REGISTRY);

    static {
        JMX_REPORTER.start();
    }

    private Metrics() { /* unused */ }

    /**
     * Returns the (static) default registry.
     *
     * @return the metrics registry
     */
    public static MetricRegistry defaultRegistry() {
        return DEFAULT_REGISTRY;
    }

    public static <T extends Metric> T add(String name, T metric) {
        return DEFAULT_REGISTRY.add(name, metric);
    }

    public static boolean remove(String name) {
        return DEFAULT_REGISTRY.remove(name);
    }

    public static String name(String name, String... names) {
        final StringBuilder builder = new StringBuilder();
        append(builder, name);
        for (String s : names) {
            append(builder, s);
        }
        return builder.toString();
    }

    public static String name(Class<?> klass, String... names) {
        return name(klass.getCanonicalName(), names);
    }

    private static void append(StringBuilder builder, String part) {
        if (part != null && !part.isEmpty()) {
            if (builder.length() > 0) {
                builder.append('.');
            }
            builder.append(part);
        }
    }
}
