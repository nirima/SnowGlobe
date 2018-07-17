package com.nirima.snowglobe.repository;

public interface ITransactionalRepositoryItem {
     void update();
     void begin();
     void commit(String message);
}
