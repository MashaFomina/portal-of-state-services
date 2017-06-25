package services.stateservices.storage;

import services.stateservices.errors.InvalidTicketsDatesException;

import java.sql.SQLException;
import java.util.List;


public interface Mapper<T> {
    T findByID(int id) throws SQLException;
    List<T> findAll() throws SQLException;
    void update(T item) throws SQLException;
    void update() throws SQLException;
    void closeConnection() throws SQLException;
    void clear();
}
