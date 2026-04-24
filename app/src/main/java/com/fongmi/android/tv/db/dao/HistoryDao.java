package com.fongmi.android.tv.db.dao;

import androidx.room.Dao;
import androidx.room.Query;

import com.fongmi.android.tv.bean.History;

import java.util.List;

@Dao
public abstract class HistoryDao extends BaseDao<History> {

    @Query("SELECT * FROM History")
    public abstract List<History> findAll();

    @Query("SELECT * FROM History WHERE createTime >= :createTime ORDER BY createTime DESC LIMIT 60")
    public abstract List<History> find(long createTime);

    @Query("SELECT * FROM History WHERE `key` = :key")
    public abstract History find(String key);

    @Query("SELECT * FROM History WHERE vodName = :vodName ORDER BY createTime DESC")
    public abstract List<History> findByName(String vodName);

    @Query("DELETE FROM History WHERE `key` = :key")
    public abstract void deleteByKey(String key);

    @Query("DELETE FROM History")
    public abstract void delete();
}
