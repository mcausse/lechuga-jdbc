package org.lechuga.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.lechuga.jdbc.exception.EmptyResultException;
import org.lechuga.jdbc.exception.FrijolesException;
import org.lechuga.jdbc.exception.TooManyResultsException;
import org.lechuga.jdbc.extractor.ResultSetExtractor;
import org.lechuga.jdbc.queryobject.QueryObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JdbcDataAccesFacade implements DataAccesFacade {

    static final Logger LOG = LoggerFactory.getLogger(JdbcDataAccesFacade.class);

    final DataSource ds;

    @Override
    public DataSource getDataSource() {
        return ds;
    }

    static class Tx {

        final Connection c;
        final Throwable opened;

        public Tx(final Connection c) {
            super();
            this.c = c;
            this.opened = new Exception();
            configureConnection();
        }

        void configureConnection() {
            try {
                c.setAutoCommit(false);
                c.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
            } catch (final SQLException e) {
                throw new FrijolesException(e);
            }
        }

        public boolean isValid() throws SQLException {
            return !c.isClosed();
        }

        public Throwable getOpened() {
            return opened;
        }

    }

    static final ThreadLocal<Tx> threadton = new ThreadLocal<Tx>() {

        @Override
        protected Tx initialValue() {
            return null;
        };
    };

    public JdbcDataAccesFacade(final DataSource ds) {
        super();
        this.ds = ds;
    }

    @Override
    public void begin() {
        createConnection();
        LOG.debug("=> begin");
    }

    @Override
    public Connection getCurrentConnection() {
        return getConnection();
    }

    protected void createConnection() {
        if (isValidTransaction()) {
            throw new FrijolesException("transaction is yet active", threadton.get().getOpened());
        }
        try {
            threadton.set(new Tx(ds.getConnection()));
        } catch (final SQLException e) {
            throw new FrijolesException(e);
        }
    }

    protected Connection getConnection() {
        try {
            Tx tx = threadton.get();
            if (tx == null || !tx.isValid()) {
                throw new FrijolesException("not in a valid transaction");
            }
            return tx.c;
        } catch (final SQLException e) {
            throw new FrijolesException(e);
        }
    }

    @Override
    public boolean isValidTransaction() {
        try {
            return threadton.get() != null && threadton.get().isValid();
        } catch (SQLException e) {
            throw new FrijolesException(e);
        }
    }

    @Override
    public void commit() {
        final Connection c = getConnection();
        try {
            c.commit();
            c.close();
            LOG.debug("<= commit");
        } catch (final Exception e) {
            throw new FrijolesException(e);
        }
    }

    @Override
    public void rollback() {
        final Connection c = getConnection();
        try {
            c.rollback();
            c.close();
            LOG.debug("<= rollback");
        } catch (final Exception e) {
            throw new FrijolesException(e);
        }
    }

    protected void close() {
        final Connection c = getConnection();
        try {
            c.close();
            threadton.remove();
        } catch (final Exception e) {
            throw new FrijolesException(e);
        } finally {
            threadton.remove();
        }
    }

    PreparedStatement prepareStatement(final Connection c, final QueryObject q) throws SQLException {
        final PreparedStatement ps = c.prepareStatement(q.getSql());
        try {
            Object[] args = q.getArgs();
            for (int i = 0; i < args.length; i++) {
                ps.setObject(i + 1, args[i]);
            }
            return ps;
        } catch (final SQLException e) {
            closeResources(null, ps);
            throw e;
        }
    }

    void closeResources(final ResultSet rs, final PreparedStatement ps) {
        if (rs != null) {
            try {
                rs.close();
            } catch (final SQLException e) {
                throw new FrijolesException(e);
            }
        }
        if (ps != null) {
            try {
                ps.close();
            } catch (final SQLException e) {
                throw new FrijolesException(e);
            }
        }
    }

    @Override
    public <T> T loadUnique(final QueryObject q, final RowMapper<T> rowMapper)
            throws TooManyResultsException, EmptyResultException {

        LOG.debug("{}", q);
        Connection c;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            c = getConnection();
            ps = prepareStatement(c, q);
            rs = ps.executeQuery();
            if (!rs.next()) {
                throw new EmptyResultException(q.toString());
            }
            final T r = rowMapper.mapRow(rs);
            if (rs.next()) {
                throw new TooManyResultsException(q.toString());
            }
            return r;
        } catch (final EmptyResultException e) {
            throw e;
        } catch (final SQLException e) {
            throw new FrijolesException(q.toString(), e);
        } finally {
            closeResources(rs, ps);
        }
    }

    @Override
    public <T> List<T> load(final QueryObject q, final RowMapper<T> rowMapper) {
        LOG.debug("{}", q);
        Connection c;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            c = getConnection();
            ps = prepareStatement(c, q);
            rs = ps.executeQuery();
            final List<T> r = new ArrayList<T>();
            while (rs.next()) {
                r.add(rowMapper.mapRow(rs));
            }
            return r;
        } catch (final SQLException e) {
            throw new FrijolesException(q.toString(), e);
        } finally {
            closeResources(rs, ps);
        }
    }

    @Override
    public int update(final QueryObject q) {
        LOG.debug("{}", q);
        Connection c;
        PreparedStatement ps = null;
        try {
            c = getConnection();
            ps = prepareStatement(c, q);
            return ps.executeUpdate();
        } catch (final SQLException e) {
            throw new FrijolesException(q.toString(), e);
        } finally {
            closeResources(null, ps);
        }
    }

    @Override
    public <T> T extract(final QueryObject q, final ResultSetExtractor<T> extractor) {
        LOG.debug("{}", q);
        Connection c;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            c = getConnection();
            ps = c.prepareStatement(q.getSql(), ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            Object[] args = q.getArgs();
            for (int i = 0; i < args.length; i++) {
                ps.setObject(i + 1, args[i]);
            }
            rs = ps.executeQuery();

            return extractor.extract(rs);

        } catch (final SQLException e) {
            throw new FrijolesException(q.toString(), e);
        } finally {
            closeResources(rs, ps);
        }
    }

}
