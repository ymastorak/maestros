package com.ymastorak.maestros.persistence.DAO;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Types;

@Repository
@RequiredArgsConstructor
public class LockDAOImpl implements LockDAO {

    private static final String SQL_ADVISORY_LOCK = "SELECT pg_advisory_xact_lock(hashtext(?))";

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void lockMember(int memberId) {
        lock("member-"+memberId);
    }

    @Override
    public void lockConfiguration() {
        lock("configuration");
    }

    @Override
    public void lockReports() {
        lock("reports");
    }

    private void lock(String lockKey) {
        jdbcTemplate.query(SQL_ADVISORY_LOCK, new Object[]{lockKey}, new int[]{Types.VARCHAR}, rs -> null);
    }
}
