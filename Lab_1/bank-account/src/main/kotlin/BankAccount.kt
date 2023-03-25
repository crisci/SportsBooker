class BankAccount {

    private var _balance = 0
    private var isClose = false

    var balance: Int
        @Synchronized
        get() {
            if (isClose) throw IllegalStateException("Invalid operation")
            return _balance
        }
        @Synchronized
        set(newBalance) {
            if (isClose) throw IllegalStateException("Invalid operation")
            _balance = newBalance
        }

    @Synchronized
    fun adjustBalance(amount: Int) {
        balance += amount
    }

    @Synchronized
    fun close() {
        isClose = true
    }
}
