package com.yotpo.account;

/**
 * Unique Account identifier
 *
 * <p>
 * NOTE: we suspect that later {@link #accountId} is not going to be uniquely identifying an account,
 * as we might add human-readable account representation and some clearing codes for partners.
 */
public class AccountKey implements Comparable<AccountKey> {
    private final long accountId;

    private AccountKey(long accountId) {
        this.accountId = accountId;
    }

    public long getAccountId() {
        return accountId;
    }

    public static AccountKey valueOf(long accountId) {
        return new AccountKey(accountId);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AccountKey that = (AccountKey) o;
        return accountId == that.accountId;
    }

    @Override
    public int hashCode() {
        return Long.hashCode(accountId);
    }

    @Override
    public String toString() {
        return "AccountKey{accountId=" + accountId + '}';
    }

    @Override
    public int compareTo(AccountKey other) {
        return Long.compare(this.accountId, other.accountId);
    }
}
