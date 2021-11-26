package org.openmedstack.messaging.rabbitmq.guice;

import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import org.openmedstack.DeploymentConfiguration;
import org.openmedstack.commands.IRouteCommands;
import org.openmedstack.messaging.rabbitmq.RabbitMqRouter;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.TimeoutException;

@SuppressWarnings("ALL")
public class RabbitMqModule extends AbstractModule {
    private final DeploymentConfiguration _configuration;

    public RabbitMqModule(DeploymentConfiguration configuration) {
        _configuration = configuration;
    }

    @Override
    protected void configure() {
        bind(IRouteCommands.class).toConstructor((Constructor<RabbitMqRouter>)RabbitMqRouter.class.getConstructors()[0]);
        bind(ConnectionFactory.class).toProvider(() -> {
            try {
                return getConnectionFactory();
            } catch (NoSuchAlgorithmException | KeyManagementException | URISyntaxException e) {
                throw new RuntimeException(e);
            }
        }).asEagerSingleton();
        bind(Connection.class).toProvider(ConnectionProvider.class);
        bind(Channel.class).toProvider(ChannelProvider.class);
    }

    private ConnectionFactory getConnectionFactory() throws NoSuchAlgorithmException, KeyManagementException, URISyntaxException {
        var conn = new ConnectionFactory();
        conn.setUri(_configuration.getServiceBus());

        return conn;
    }
}

class ConnectionProvider implements Provider<Connection>{
    private final ConnectionFactory _connectionFactory;

    @Inject
    public ConnectionProvider(ConnectionFactory connectionFactory){
        _connectionFactory = connectionFactory;
    }

    @Override
    public Connection get() {
        try {
            return _connectionFactory.newConnection();
        } catch (IOException|TimeoutException e) {
            throw new RuntimeException(e);
        }
    }
}

class ChannelProvider implements Provider<Channel>{
    private final ConnectionFactory _connectionFactory;

    @Inject
    public ChannelProvider(ConnectionFactory connectionFactory){
        _connectionFactory = connectionFactory;
    }
    @Override
    public Channel get() {
        try {
            return _connectionFactory.newConnection().createChannel();
        } catch (IOException |TimeoutException e) {
           throw new RuntimeException(e);
        }
    }
}