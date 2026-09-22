package com.josyantl.joblens.document.application;

public interface DocumentStorage {
    void store(String key, byte[] content);
    byte[] load(String key);
    void delete(String key);
}
