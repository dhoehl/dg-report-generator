/*
 * Copyright (C) 2020.  Dennis Hoehl
 * This file is part of DG report generator <https://github.com/dhoehl/dg-report-generator>.
 *
 * DG report generator is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * DG report generator is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with DG report generator.  If not, see <http://www.gnu.org/licenses/>.
 */

package dhoehl.model;


import dhoehl.utils.DateUtils;
import dhoehl.utils.MoneyUtils;
import org.joda.money.BigMoney;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.Objects;

import static dhoehl.utils.DateUtils.getLocaleDateTimeStringShort;

public class Transaction implements Comparable<Transaction> {
    public static final String COLOR_SELL = "#B2001D";
    public static final String COLOR_BUY = "#008e00"; //008e00 //00B200


    public enum Type {
        BUY, SELL;

        String getHtml() {
            if (this == BUY) return "<td style=\"color:" + COLOR_BUY + "\">" + name() + "</td>";
            else return "<td style=\"color:" + COLOR_SELL + "\">" + name() + "</td>";
        }
    }

    private final String id;
    private final LocalDateTime timestamp;
    private final String product;
    private final String isin;
    private final String exchange;
    private final BigDecimal quantity;
    private final BigMoney price;
    private final BigMoney amount;
    private final BigMoney amountLocal;
    private final BigDecimal exchangeRate;
    private final BigMoney fee;
    private final BigMoney overall;
    private final Type type;
    private final BigMoney exchangeFee;

    public Transaction(String id, LocalDateTime timestamp, String product, String isin, String exchange, BigDecimal quantity, BigMoney price, BigMoney amount, BigMoney amountLocal, BigDecimal exchangeRate, BigMoney fee, BigMoney overall) {
        this.id = id;
        this.timestamp = timestamp;
        this.product = product;
        this.isin = isin;
        this.exchange = exchange;
        this.quantity = quantity;
        this.price = price;
        this.amount = amount;
        this.amountLocal = amountLocal;
        this.exchangeRate = exchangeRate;
        this.fee = fee;
        this.overall = overall;
        //For convenience
        if (quantity.compareTo(BigDecimal.ZERO) > 0) type = Type.BUY;
        else type = Type.SELL;
        this.exchangeFee = calculateExchangeFee();
    }

    public String getId() {
        return id;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public String getProduct() {
        return product;
    }

    public String getIsin() {
        return isin;
    }

    public String getExchange() {
        return exchange;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public BigMoney getPrice() {
        return price;
    }

    public Type getType() {
        return type;
    }

    public BigMoney getAmount() {
        return amount;
    }

    public BigMoney getAmountLocal() {
        return amountLocal;
    }

    public BigDecimal getExchangeRate() {
        return exchangeRate;
    }

    public BigMoney getFee() {
        BigMoney total = BigMoney.zero(overall.getCurrencyUnit());

        return fee != null ? total.plus(fee) : total;
    }

    public BigMoney getExchangeFee() {
        return exchangeFee;
    }

    public BigMoney calculateExchangeFee() {
        //These are "hidden" costs by DeGiro that are listed in their price guide but are not listet in any report clearly
        //If the currencies are different
        if (getAmountLocal().getCurrencyUnit().compareTo(getOverall().getCurrencyUnit()) != 0) {

            BigMoney totalAmountExchanged = getAmountLocal().convertedTo(overall.getCurrencyUnit(), BigDecimal.ONE.setScale(getExchangeRate().scale(), RoundingMode.HALF_EVEN).divide(getExchangeRate(), RoundingMode.HALF_EVEN));
            if (getType() == Transaction.Type.BUY) {
                return totalAmountExchanged.minus(getAmount());
            } else {
                return getAmount().minus(totalAmountExchanged);
            }
        } else {
            return BigMoney.zero(overall.getCurrencyUnit());
        }
    }

    public BigMoney getOverall() {
        return overall;
    }

    public Transaction reduceQuantityBy(BigDecimal quantity) {
        return new Transaction(id, timestamp, product, isin, exchange, quantity.subtract(quantity), price, amount, amountLocal, exchangeRate, fee, overall);
    }

    @Override
    public String toString() {
        String feeString = fee == null ? MoneyUtils.toString(BigMoney.zero(overall.getCurrencyUnit())) : MoneyUtils.toString(fee);
        return type + ": " + getLocaleDateTimeStringShort(timestamp) + " (" + product + ", " + isin + ", " + id + ") - " +
                exchange + ", " + quantity + " pcs. at " + MoneyUtils.toString(price) + " p.p. " +
                "Total: " + MoneyUtils.toString(amount) + ", Local: " + MoneyUtils.toString(amountLocal) + ", Exchange rate: " + exchangeRate +
                " Fee: " + feeString + " Overall: " + MoneyUtils.toString(overall);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Transaction that = (Transaction) o;

        if (!Objects.equals(id, that.id)) return false;
        return timestamp.equals(that.timestamp);
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + timestamp.hashCode();
        return result;
    }

    @Override
    public int compareTo(Transaction o) {
        return Comparator.comparing(Transaction::getTimestamp).thenComparing(Transaction::getId).compare(this,o);
    }

    public String toStringHtml(int nr) {
        return "<tr>" +
                "<td>" + nr + "</td>" +
                getType().getHtml() +
                "<td>" + getLocaleDateTimeStringShort(getTimestamp()) + "</td>" +
                "<td>" + getProduct() + "</td>" +
                "<td>" + getIsin() + "</td>" +
                "<td>" + getExchange() + "</td>" +
                "<td>" + getQuantity() + "</td>" +
                "<td>" + MoneyUtils.toString(getPrice()) + "</td>" +
                "<td>" + MoneyUtils.toString(getAmountLocal()) + "</td>" +
                "<td>" + getExchangeRate() + "</td>" +
                "<td>" + MoneyUtils.toString(getExchangeFee()) + "</td>" +
                "<td>" + MoneyUtils.toString(getAmount()) + "</td>" +
                "<td>" + MoneyUtils.toString(getFee()) + "</td>" +
                "<td>" + MoneyUtils.toStringHtml(getOverall()) + "</td>" +
                "</tr>";
    }
}
