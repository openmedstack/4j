package org.openmedstack.messaging.rabbitmq;

import com.rabbitmq.client.*;
import org.openmedstack.commands.CommandResponse;
import org.openmedstack.commands.DomainCommand;
import org.openmedstack.commands.IRouteCommands;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.concurrent.CompletableFuture;

public class RabbitMqRouter implements IRouteCommands {
    private final Channel _connection;

    public RabbitMqRouter(Channel connection) {
        _connection = connection;
    }

    @Override
    public <T extends DomainCommand> CompletableFuture<CommandResponse> send(T command, HashMap<String, Object> headers) {
        try {
            _connection.basicPublish("", "", true, new AMQP.BasicProperties(), "".getBytes(StandardCharsets.UTF_8));
            return CompletableFuture.completedFuture(CommandResponse.success(command));
        } catch (IOException io) {
            throw new RuntimeException(io);
        }
    }
}
