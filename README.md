## Task 1: Code Review and Performance Optimization

### Issues Identified and mentioned in PR review:

1. **Performance Issue (Critical):**
    - Original used ArrayList with O(n) lookup time
    - Changed to ConcurrentHashMap for O(1) lookups
    - Trade-off: Slightly more memory, dramatically better performance

2. **Missing Implementation (Blocker):**
    - AccountKey lacked equals() and hashCode()
    - Required for HashMap to work correctly
    - Added proper implementations based on accountId

3. **Thread Safety:**
    - Used ConcurrentHashMap to support concurrent access
    - Prepares for multi-threaded operations in Task 3

4. **Missing Validation:**
    - Added null checks and field validation
    - Proper exception handling per interface contract

### Changes Made:
- Implemented AccountKey.equals(), hashCode(), toString()
- Changed storage from ArrayList to ConcurrentHashMap
- Added comprehensive input validation in createAccount()
- Added test coverage for all functionality

---

## Task 2: Single-Threaded Money Transfer Implementation

### Implementation Details:

**Transfer Flow:**
1. Validate transfer parameters (null checks, same-account check, non-negative amount)
2. Retrieve both source and target accounts
3. Verify sufficient balance in source account
4. Execute transfer by updating both account balances

**Validation Rules:**
- Source and target must not be null
- Source and target must be different accounts
- Amount must be positive (> 0)
- Source account must have sufficient balance
- Both accounts must exist

**Exception Handling:**
- `InvalidTransferException` - for validation failures (same account, invalid amount, null parameters)
- `AccountNotFoundException` - when either account doesn't exist
- `InsufficientBalanceException` - when source balance < transfer amount

### Known Limitations (Per Assignment Scope)

#### 1. Double Type for Monetary Values
**Issue:** The `Account` class uses `Double` for the balance field, which is inappropriate for financial calculations due to floating-point precision issues.

#### 2. No Transaction Guarantees
**Issue:**
The in-memory implementation doesn't provide atomicity guarantees for transfers.
In a production system, this would require:
- Database transactions (ACID guarantees)
- Write-ahead logging
- Crash recovery mechanisms

However, per assignment instructions, persistence concerns are explicitly out of scope.

---

## Task 3: Multi-Threaded Transfer Implementation

### Concurrency Challenges

**Race Conditions:**
Multiple threads reading/writing same account balance can cause lost updates.

**Deadlocks:**
Thread 1 locks A→B while Thread 2 locks B→A causes circular wait.

### Solution: Fine-Grained Locking with Lock Ordering

**Implementation:**
- Each account has its own `ReentrantLock` in `ConcurrentHashMap<AccountKey, ReentrantLock>`
- Locks always acquired in deterministic order based on accountId
- Prevents deadlock: both A→B and B→A transfer threads lock account 1 first, then account 2

**Benefits:**
- Independent transfers run in parallel (A→B doesn't block C→D)
- No deadlocks due to consistent ordering
- Scales with number of account pairs

**Notes:**

ConcurrentHashMap alone is not enough:
- Protects map structure operations (get/put)
- Does NOT protect Account object mutations
- Need account-level locks for balance updates

**Test Coverage:**
- Concurrent transfers from same account (race condition test)
- Bidirectional transfers A→B and B→A (deadlock prevention)
- Random transfers between multiple accounts (money conservation)
- 1000+ concurrent threads (stress test)

### Alternatives Considered
**Synchronized method:** Simpler but only one transfer globally (poor performance and scalability).

**Optimistic locking:** Good for low contention, poor for banking (high retry cost).

---
