package com.yammer.metrics.core;

import com.yammer.metrics.util.FilteredIterator;
import com.yammer.metrics.util.UnmodifiableIterator;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * A registry of metric instances.
 */
public class MetricRegistry implements Iterable<Map.Entry<String, Metric>> {
    private final ConcurrentMap<String, Metric> metrics;
    private final List<MetricRegistryListener> listeners;
    private final String name;

    /**
     * Creates a new {@link MetricRegistry}.
     */
    public MetricRegistry() {
        this(null);
    }

    /**
     * Creates a new {@link MetricRegistry} with the given name and {@link Clock} instance.
     *
     * @param name     the name of the registry
     */
    public MetricRegistry(String name) {
        this.name = name;
        this.metrics = new ConcurrentSkipListMap<String, Metric>();
        this.listeners = new CopyOnWriteArrayList<MetricRegistryListener>();
    }

    @SuppressWarnings("unchecked")
    public <T> Gauge<T> add(String name, Gauge<T> gauge) {
        return (Gauge<T>) add(name, (Metric) gauge);
    }

    public Counter add(String name, Counter counter) {
        return (Counter) add(name, (Metric) counter);
    }

    public Histogram add(String name, Histogram histogram) {
        return (Histogram) add(name, (Metric) histogram);
    }

    public Meter add(String name, Meter meter) {
        return (Meter) add(name, (Metric) meter);
    }

    public Timer add(String name, Timer timer) {
        return (Timer) add(name, (Metric) timer);
    }

    public Metric add(String name, Metric metric) {
        final Metric existingMetric = metrics.get(name);
        if (existingMetric == null) {
            final Metric justAddedMetric = metrics.putIfAbsent(name, metric);
            if (justAddedMetric == null) {
                notifyMetricRegistered(name, metric);
                return metric;
            }
            return justAddedMetric;
        }
        return existingMetric;
    }

    /**
     * Removes the metric with the given name.
     *
     * @param name the name of the metric
     * @return {@code true} if the metric was removed; {@code false} otherwise
     */
    public boolean remove(String name) {
        final Metric metric = metrics.remove(name);
        if (metric != null) {
            notifyMetricUnregistered(name);
            return true;
        }
        return false;
    }

    /**
     * Adds a {@link MetricRegistryListener} to a collection of listeners that will be notified on
     * metric creation.  Listeners will be notified in the order in which they are added.
     * <p/>
     * <b>N.B.:</b> The listener will be notified of all existing metrics when it first registers.
     *
     * @param listener the listener that will be notified
     */
    public void addListener(MetricRegistryListener listener) {
        listeners.add(listener);
        for (Map.Entry<String, Metric> entry : metrics.entrySet()) {
            listener.onMetricAdded(entry.getKey(), entry.getValue());
        }
    }

    /**
     * Removes a {@link MetricRegistryListener} from this registry's collection of listeners.
     *
     * @param listener the listener that will be removed
     */
    public void removeListener(MetricRegistryListener listener) {
        listeners.remove(listener);
    }

    private void notifyMetricUnregistered(String name) {
        for (MetricRegistryListener listener : listeners) {
            listener.onMetricRemoved(name);
        }
    }

    private void notifyMetricRegistered(String name, Metric metric) {
        for (MetricRegistryListener listener : listeners) {
            listener.onMetricAdded(name, metric);
        }
    }

    public String getName() {
        return name;
    }

    @Override
    public Iterator<Map.Entry<String, Metric>> iterator() {
        return new UnmodifiableIterator<Map.Entry<String, Metric>>(metrics.entrySet().iterator());
    }

    public Iterable<Map.Entry<String, Metric>> filter(final MetricPredicate predicate) {
        return new Iterable<Map.Entry<String, Metric>>() {
            @Override
            public Iterator<Map.Entry<String, Metric>> iterator() {
                return new FilteredIterator<Map.Entry<String, Metric>>(MetricRegistry.this.iterator()) {
                    @Override
                    protected boolean matches(Map.Entry<String, Metric> possibleElement) {
                        return predicate.matches(possibleElement.getKey(),
                                                 possibleElement.getValue());
                    }
                };
            }
        };
    }
}
