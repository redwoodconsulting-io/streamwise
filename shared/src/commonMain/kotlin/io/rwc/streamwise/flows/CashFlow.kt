package io.rwc.streamwise.flows

import com.ionspin.kotlin.bignum.decimal.BigDecimal
import com.ionspin.kotlin.bignum.decimal.toBigDecimal
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus

interface CashFlow {
  fun valueOn(date: LocalDate): BigDecimal
}

data class Fixed(val date: LocalDate, val amount: BigDecimal) : CashFlow {
  override fun valueOn(date: LocalDate): BigDecimal {
    return if (this@Fixed.date == date) {
      amount
    } else {
      0.toBigDecimal()
    }
  }
}

fun reifyFlows(flows: List<CashFlow>, startDate: LocalDate, endDate: LocalDate): List<Fixed> {
  val cashFlow = mutableListOf<Fixed>()

  var date = startDate
  while (date <= endDate) {
    val dayAmount = flows.fold(0.toBigDecimal()) { total, flow -> total + flow.valueOn(date) }
    cashFlow.add(Fixed(date, dayAmount))
    date = date.plus(1, DateTimeUnit.DAY)
  }

  return cashFlow
}

fun makeSomeBalances(startDate: LocalDate, endDate: LocalDate): Map<LocalDate, BigDecimal> {
  val flows = listOf(
    Fixed(LocalDate(2023, 1, 1), 1000.toBigDecimal()),
    Monthly("ebmud", 5, (-100).toBigDecimal()),
    Monthly("pg&e", -5, (-200).toBigDecimal()),
    Monthly("income", 15, 1000.toBigDecimal()),
    Monthly("stuff", -7, (-500).toBigDecimal()),
    Monthly("income", 30, 1000.toBigDecimal()),
  )

  val cash = reifyFlows(flows, startDate, endDate)
  var runningTotal = 0.toBigDecimal()
  return cash.associate {
    runningTotal += it.amount
    it.date to runningTotal
  }
}