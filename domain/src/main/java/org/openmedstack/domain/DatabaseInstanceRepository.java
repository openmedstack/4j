package org.openmedstack.domain;

import java.sql.*;
import java.util.HashMap;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public abstract class DatabaseInstanceRepository implements Repository {
    private Connection _connection;

    public DatabaseInstanceRepository(Connection conn) {
        _connection = conn;
    }

    public <TAggregate extends Aggregate> CompletableFuture<TAggregate> getById(String id){
        try {
            PreparedStatement statement = _connection.prepareStatement(String.format("SELECT * FROM table WHERE id = %s ORDER BY version DESC LIMIT 1", id));
            statement.closeOnCompletion();
            ResultSet rs = statement.getResultSet();
            rs.next();
            rs.close();
            return null;
        } catch (SQLException e) {
            return null;
        }
    }

    public <TAggregate extends Aggregate> CompletableFuture<TAggregate> getById(String id, Integer version){
        try {
            PreparedStatement statement = _connection.prepareStatement(String.format("SELECT * FROM table WHERE id = %s AND version = %s", id, version));
            statement.closeOnCompletion();
            ResultSet rs = statement.getResultSet();
            rs.next();
            rs.close();
            return null;
        } catch (SQLException e) {
            return null;
        }
    }

    public CompletableFuture<Boolean> save(Aggregate aggregate, Consumer<HashMap<String, Object>> updateHeaders){
        try {
            PreparedStatement statement = _connection.prepareStatement(String.format("UPDATE table SET (id, version) VALUES (%s, %s)", aggregate.getId(), aggregate.getVersion()));
            statement.closeOnCompletion();
            ResultSet rs = statement.getResultSet();
            rs.next();
            rs.close();
            return null;
        } catch (SQLException e) {
            return null;
        }
    }
}
