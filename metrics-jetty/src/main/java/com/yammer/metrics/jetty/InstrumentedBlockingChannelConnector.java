package com.yammer.metrics.jetty;

import com.yammer.metrics.Metrics;
import com.yammer.metrics.core.*;
import org.eclipse.jetty.io.Connection;
import org.eclipse.jetty.server.nio.BlockingChannelConnector;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class InstrumentedBlockingChannelConnector extends BlockingChannelConnector {
    private final Timer duration;
    private final Meter accepts, connects, disconnects;
    private final Counter connections;

    public InstrumentedBlockingChannelConnector(int port) {
        this(Metrics.defaultRegistry(), port);
    }

    public InstrumentedBlockingChannelConnector(MetricRegistry registry,
                                                int port) {
        super();
        setPort(port);
        this.duration = registry.add(Metrics.name(BlockingChannelConnector.class,
                                                  "connection-duration",
                                                  Integer.toString(port)),
                                     Metrics.timer());
        this.accepts = registry.add(Metrics.name(BlockingChannelConnector.class,
                                                 "accepts",
                                                 Integer.toString(port)),
                                    Metrics.meter("connections"));
        this.connects = registry.add(Metrics.name(BlockingChannelConnector.class,
                                                  "connects",
                                                  Integer.toString(port)),
                                     Metrics.meter("connections"));
        this.disconnects = registry.add(Metrics.name(BlockingChannelConnector.class,
                                                     "disconnects",
                                                     Integer.toString(port)),
                                        Metrics.meter("connections"));
        this.connections = registry.add(Metrics.name(BlockingChannelConnector.class,
                                                     "active-connections",
                                                     Integer.toString(port)),
                                        Metrics.counter());
    }

    @Override
    public void accept(int acceptorID) throws IOException, InterruptedException {
        super.accept(acceptorID);
        accepts.mark();
    }

    @Override
    protected void connectionOpened(Connection connection) {
        connections.inc();
        super.connectionOpened(connection);
        connects.mark();
    }

    @Override
    protected void connectionClosed(Connection connection) {
        super.connectionClosed(connection);
        disconnects.mark();
        final long duration = System.currentTimeMillis() - connection.getTimeStamp();
        this.duration.update(duration, TimeUnit.MILLISECONDS);
        connections.dec();
    }
}
