package com.ymastorak.maestros.persistence.DAO;

public interface LockDAO {
    void lockMember(int memberId);
    void lockConfiguration();
    void lockReports();
}
