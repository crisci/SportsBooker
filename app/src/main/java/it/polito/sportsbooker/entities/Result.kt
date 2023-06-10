package it.polito.sportsbooker.entities

class Result<out T> (
    val value: T?,
    val throwable: Throwable?
)