package com.yammer.metrics.jetty;

import com.yammer.metrics.Metrics;
import com.yammer.metrics.core.*;
import org.eclipse.jetty.io.Connection;
import org.eclipse.jetty.server.bio.SocketConnector;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class InstrumentedSocketConnector extends SocketConnector {
    private final Timer duration;
    private final Meter accepts, connects, disconnects;
    private final Counter connections;

    public InstrumentedSocketConnector(int port) {
        this(Metrics.defaultRegistry(), port);
    }

    public InstrumentedSocketConnector(MetricsRegistry registry, int port) {
        super();
        setPort(port);
        this.duration = registry.add(MetricName.name(SocketConnector.class,
                                                     "connection-duration",
                                                     Integer.toString(port)),
                                     new Timer());
        this.accepts = registry.add(MetricName.name(SocketConnector.class,
                                                    "accepts",
                                                    Integer.toString(port)),
                                    new Meter("connections"));
        this.connects = registry.add(MetricName.name(SocketConnector.class,
                                                     "connects",
                                                     Integer.toString(port)),
                                     new Meter("connections"));
        this.disconnects = registry.add(MetricName.name(SocketConnector.class,
                                                        "disconnects",
                                                        Integer.toString(port)),
                                        new Meter("connections"));
        this.connections = registry.add(MetricName.name(SocketConnector.class,
                                                        "active-connections",
                                                        Integer.toString(port)),
                                        new Counter());
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
