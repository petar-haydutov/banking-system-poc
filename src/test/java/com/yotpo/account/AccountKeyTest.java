package com.yotpo.account;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class AccountKeyTest {

    @Test
    public void testValueOf() {
        AccountKey key = AccountKey.valueOf(123L);
        assertNotNull(key);
        assertEquals(123L, key.getAccountId());
    }

    @Test
    public void testEqualsWithSameId() {
        AccountKey key1 = AccountKey.valueOf(123L);
        AccountKey key2 = AccountKey.valueOf(123L);

        assertEquals(key1, key2);
        assertEquals(key2, key1);
    }

    @Test
    public void testEqualsWithDifferentId() {
        AccountKey key1 = AccountKey.valueOf(123L);
        AccountKey key2 = AccountKey.valueOf(456L);

        assertNotEquals(key1, key2);
        assertNotEquals(key2, key1);
    }

    @Test
    public void testEqualsWithSameInstance() {
        AccountKey key1 = AccountKey.valueOf(123L);

        assertEquals(key1, key1);
    }

    @Test
    public void testEqualsWithNull() {
        AccountKey key1 = AccountKey.valueOf(123L);

        assertNotEquals(key1, null);
    }

    @Test
    public void testEqualsWithDifferentClass() {
        AccountKey key1 = AccountKey.valueOf(123L);
        String notAnAccountKey = "not an account key";

        assertNotEquals(key1, notAnAccountKey);
    }

    @Test
    public void testHashCodeConsistency() {
        AccountKey key1 = AccountKey.valueOf(123L);
        AccountKey key2 = AccountKey.valueOf(123L);

        assertEquals(key1.hashCode(), key2.hashCode());
    }

    @Test
    public void testHashCodeDifferentForDifferentIds() {
        AccountKey key1 = AccountKey.valueOf(123L);
        AccountKey key2 = AccountKey.valueOf(456L);

        assertNotEquals(key1.hashCode(), key2.hashCode());
    }
}