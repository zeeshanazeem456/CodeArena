package com.codearena.dao;

import com.codearena.model.BaseEntity;

public abstract class BaseDAO<T extends BaseEntity> {

    public abstract T getById(int id);

    public abstract void save(T entity);

    public abstract void delete(int id);
}
