package com.nirima.snowglobe.repository;

public interface ITransactionalRepositoryItem {
     void begin();
     void commit(String message);
}
