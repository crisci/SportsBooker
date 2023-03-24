class BankAccount() {
    // TODO: implement read access to 'balance'
    var balance: Long = 0
        get() {check(open); return field}
        private set

    private var open = true


    fun adjustBalance(amount: Long){
        synchronized(this) {
            check(open)
            this.balance += amount
        }
    }
    fun close() {
        open = false
    }
}
