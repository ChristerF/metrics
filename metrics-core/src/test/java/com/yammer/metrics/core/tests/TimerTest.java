package com.yammer.metrics.core.tests;

import com.yammer.metrics.core.Clock;
import com.yammer.metrics.core.Timer;
import com.yammer.metrics.stats.Snapshot;
import org.junit.Test;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class TimerTest {
    private final Clock clock = new Clock() {
        // a mock clock that increments its ticker by 50msec per call
        private long val = 0;

        @Override
        public long getTick() {
            return val += 50000000;
        }
    };
    private final Timer timer = new Timer(clock);

    @Test
    public void aBlankTimer() throws Exception {
        assertThat("the timer has a count of zero",
                   timer.getCount(),
                   is(0L));

        assertThat("the timer has a max duration of zero",
                   timer.getMax(),
                   is(0L));

        assertThat("the timer has a min duration of zero",
                   timer.getMin(),
                   is(0L));

        assertThat("the timer has a mean duration of zero",
                   timer.getMean(),
                   is(0L));

        assertThat("the timer has a duration standard deviation of zero",
                   timer.getStdDev(),
                   is(closeTo(0.0, 0.001)));

        final Snapshot snapshot = timer.getSnapshot();

        assertThat("the timer has a median duration of zero",
                   snapshot.getMedian(),
                   is(0L));

        assertThat("the timer has a 75th percentile duration of zero",
                   snapshot.get75thPercentile(),
                   is(0L));

        assertThat("the timer has a 99th percentile duration of zero",
                   snapshot.get99thPercentile(),
                   is(0L));

        assertThat("the timer has a mean rate of zero",
                   timer.getMeanRate(),
                   is(closeTo(0.0, 0.001)));

        assertThat("the timer has a one-minute rate of zero",
                   timer.getOneMinuteRate(),
                   is(closeTo(0.0, 0.001)));

        assertThat("the timer has a five-minute rate of zero",
                   timer.getFiveMinuteRate(),
                   is(closeTo(0.0, 0.001)));

        assertThat("the timer has a fifteen-minute rate of zero",
                   timer.getFifteenMinuteRate(),
                   is(closeTo(0.0, 0.001)));

        assertThat("the timer has no values",
                   timer.getSnapshot().size(),
                   is(0));
    }

    @Test
    public void timingASeriesOfEvents() throws Exception {
        timer.update(10, TimeUnit.NANOSECONDS);
        timer.update(20, TimeUnit.NANOSECONDS);
        timer.update(20, TimeUnit.NANOSECONDS);
        timer.update(30, TimeUnit.NANOSECONDS);
        timer.update(40, TimeUnit.NANOSECONDS);

        assertThat("the timer has a count of 5",
                   timer.getCount(),
                   is(5L));

        assertThat("the timer has a max duration of 40",
                   timer.getMax(),
                   is(40L));

        assertThat("the timer has a min duration of 10",
                   timer.getMin(),
                   is(10L));

        assertThat("the timer has a mean duration of 24",
                   timer.getMean(),
                   is(24L));

        assertThat("the timer has a duration standard deviation of zero",
                   timer.getStdDev(),
                   is(closeTo(11.401, 0.001)));

        final Snapshot snapshot = timer.getSnapshot();

        assertThat("the timer has a median duration of 20",
                   snapshot.getMedian(),
                   is(20L));

        assertThat("the timer has a 75th percentile duration of 35",
                   snapshot.get75thPercentile(),
                   is(35L));

        assertThat("the timer has a 99th percentile duration of 40",
                   snapshot.get99thPercentile(),
                   is(40L));

        assertThat("the timer has no values",
                   timer.getSnapshot().getValues(),
                   is(new long[]{10, 20, 20, 30, 40}));
    }

    @Test
    public void timingVariantValues() throws Exception {
        timer.update(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        timer.update(0, TimeUnit.NANOSECONDS);

        assertThat("the timer has an accurate standard deviation",
                   timer.getStdDev(),
                   is(closeTo(6.521908912666392E18, 0.001)));
    }

    @Test
    public void timingCallableInstances() throws Exception {
        final String value = timer.time(new Callable<String>() {
            @Override
            public String call() throws Exception {
                return "one";
            }
        });

        assertThat("the timer has a count of 1",
                   timer.getCount(),
                   is(1L));

        assertThat("returns the result of the callable",
                   value,
                   is("one"));

        assertThat("records the duration of the Callable#call()",
                   timer.getMax(),
                   is(50000000L));
    }

    @Test
    public void timingContexts() throws Exception {
        timer.time().stop();

        assertThat("the timer has a count of 1",
                   timer.getCount(),
                   is(1L));

        assertThat("records the duration of the context",
                   timer.getMax(),
                   is(50000000L));
    }
}
