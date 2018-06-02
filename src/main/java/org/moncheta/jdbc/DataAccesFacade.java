package org.moncheta.jdbc;

import java.sql.Connection;
import java.util.List;

import javax.sql.DataSource;

import org.moncheta.jdbc.exception.EmptyResultException;
import org.moncheta.jdbc.exception.TooManyResultsException;
import org.moncheta.jdbc.extractor.ResultSetExtractor;
import org.moncheta.jdbc.queryobject.QueryObject;

/**
 * tota la comunicació amb la base de dades es fa a través d'aquesta façana, de
 * manera que es pot triar la tecnologia, ja sigui JDBC pelat (
 * {@link JdbcDataAccesFacade}), o Spring JdbcTemplates .
 *
 * @see JdbcDataAccesFacade
 * @see SpringDataAccesFacade
 */
public interface DataAccesFacade {

    DataSource getDataSource();

    <T> T loadUnique(QueryObject q, RowMapper<T> rowMapper) throws TooManyResultsException, EmptyResultException;

    <T> List<T> load(QueryObject q, RowMapper<T> rowMapper);

    int update(QueryObject q);

    <T> T extract(QueryObject q, ResultSetExtractor<T> extractor);

    void begin();

    void commit();

    void rollback();

    boolean isValidTransaction();

    Connection getCurrentConnection();

}