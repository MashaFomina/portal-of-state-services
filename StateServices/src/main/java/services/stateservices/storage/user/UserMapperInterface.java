package services.stateservices.storage.user;

import java.sql.SQLException;
import services.stateservices.storage.Mapper;

public interface UserMapperInterface<T> extends Mapper<T> {
    T findByLogin(String login) throws SQLException;
}