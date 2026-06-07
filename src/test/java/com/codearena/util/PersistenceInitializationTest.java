package com.codearena.util;

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.codearena.TestDatabase;
import org.junit.jupiter.api.Test;

class PersistenceInitializationTest {

    @Test
    void freshDatabaseInitializesFromSchemaAndSeedResources() throws Exception {
        TestDatabase.reset();

        assertTrue(TestDatabase.countRows("users") >= 4);
        assertTrue(TestDatabase.countRows("problems") >= 6);
        assertTrue(TestDatabase.countRows("test_cases") >= 12);
        assertTrue(TestDatabase.countRows("badges") >= 24);
    }
}
