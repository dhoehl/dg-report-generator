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

import dhoehl.utils.MoneyUtils;
import dhoehl.wrapper.ExchangeFeeMapper;
import dhoehl.wrapper.FeeMapper;
import dhoehl.wrapper.ProfitLossMapper;
import dhoehl.wrapper.TransactionMapper;
import org.joda.money.BigMoney;
import org.joda.money.CurrencyUnit;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Set;

public class Asset implements Comparable<Asset> {
    private static final String TABLE_HEAD = "<tr>" +
            "<th>#</th>" +
            "<th>Type</th>" +
            "<th>Date/Time</th>" +
            "<th>Product</th>" +
            "<th>ISIN</th>\n" +
            "<th>Exchange</th>\n" +
            "<th>Quantity</th>\n" +
            "<th>Price</th>\n" +
            "<th>Amount local</th>\n" +
            "<th>Exchange rate</th>\n" +
            "<th>Exchange fee</th>\n" +
            "<th>Amount</th>\n" +
            "<th>Fee</th>\n" +
            "<th>Amount total</th>\n" +
            "</tr>\n";

    public boolean hasErrors() {
        return profitLoss.hasError();
    }

    public String getError() {
        return profitLoss.getError();
    }


    public enum Type {
        STOCK, OTHER;
        private static final String[] KNOCKOUT_KEYWORDS = new String[]{"CALL", "PUT", "TURBOL", "TURBOC", "TURBOS", "TURBOP", "TUBULL", "MINIL", "MINIS"};

        public static Type parseName(String name) {
            if (Arrays.stream(KNOCKOUT_KEYWORDS).anyMatch(name::contains)) return OTHER;
            else return STOCK;
        }
    }

    private final String isin;
    private final String name;
    private final Type type;
    private final FeeMapper fees;
    private final ExchangeFeeMapper exchangeFees;
    private final ProfitLossMapper profitLoss;
    private final TransactionMapper transactions = new TransactionMapper();


    private BigMoney overall;
    private BigDecimal quantity = BigDecimal.ZERO;


    public Asset(String isin, String name, CurrencyUnit currency) {
        this.isin = isin;
        this.name = name;
        this.type = Type.parseName(name);
        this.fees = new FeeMapper(currency);
        this.profitLoss = new ProfitLossMapper(currency);
        this.exchangeFees = new ExchangeFeeMapper(currency);
        this.overall = BigMoney.zero(currency);
    }

    public void addTransaction(Transaction t) {
        if (transactions.add(t)) {
            quantity = quantity.add(t.getQuantity());
            fees.add(t);
            profitLoss.add(t);
            exchangeFees.add(t);
            overall = overall.plus(t.getOverall());
        }
    }

    public BigMoney getProfitLoss(int year) {
        return profitLoss.getProfitLoss(year);
    }

    public BigMoney getOverall() {
        return overall;
    }

    public BigMoney getProfitLoss() {
        return profitLoss.getProfitLoss();
    }

    public BigMoney getProfitLossTotal(int year) {
        return getProfitLoss(year).plus(getPaidFees(year)).plus(getPaidExchangeFees(year));
    }

    public BigMoney getProfitLossTotal() {
        return getProfitLoss().plus(getPaidFees()).plus(getPaidExchangeFees());
    }

    public Type getType() {
        return type;
    }

    public String getIsin() {
        return isin;
    }

    public String getName() {
        return name;
    }

    public Set<Transaction> getTransactions(int year) {
        return transactions.getTransactions(year);
    }

    public Set<Transaction> getTransactions() {
        return transactions.getTransactions();
    }

    public BigMoney getPaidFees(int year) {
        return fees.getFees(year);
    }

    public BigMoney getPaidExchangeFees(int year) {
        return exchangeFees.getExchangeFees(year);
    }

    public BigMoney getPaidFees() {
        return fees.getFees();
    }

    public BigMoney getPaidExchangeFees() {
        return exchangeFees.getExchangeFees();
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public long getTradeCount(int year) {
        return transactions.getTradeCount(year);
    }

    public long getTradeCount() {
        return transactions.getTradeCount();
    }

    public long getTradeCount(Transaction.Type type) {
        return transactions.getTradeCount(type);
    }


    public long getTradeCount(Transaction.Type type, int year) {
        return transactions.getTradeCount(type, year);
    }


    @Override
    public String toString() {
        return toStringShort() +
                transactions;
    }

    public String toStringShort() {

        return type + ": " + name + " (" + isin + "), " + quantity + " pcs. Total (Fees included): " + MoneyUtils.toString(overall) + ", Payed fees: " + fees + ", Profit/Loss (Fees not included):" + profitLoss + "\n";
    }

    public String toStringHtml(int year, String navTag) {
        StringBuilder sb = new StringBuilder();
        sb.append("<h2><a href=\"#" + navTag + "\">" + getName() + "</a></h2>\n");
        if (hasErrors()) {
            sb.append("<p style=\"color:crimson;\">" + getError() + "<br></p>");
        }
        sb.append(
                "<p>Trade count (Sells): <b>" + getTradeCount(Transaction.Type.SELL, year) + "</b></p>\n" +
                        "<p>Trade count (Buys): <b>" + getTradeCount(Transaction.Type.BUY, year) + "</b></p>\n" +
                        "<p>Trade count: <b>" + getTradeCount(year) + "</b></p>\n" +
                        "<p>Profit/Loss: <b>" + MoneyUtils.toStringHtml(getProfitLoss(year)) + "</b></p>\n" +
                        "<p>Payed transaction fees: <b>" + MoneyUtils.toStringHtml(getPaidFees(year)) + "</b></p>\n" +
                        "<p>Payed currency exchange fees: <b>" + MoneyUtils.toStringHtml(getPaidExchangeFees(year)) + "</b></p>\n" +
                        "<p>Payed fees: <b>" + MoneyUtils.toStringHtml(getPaidFees(year).plus(getPaidExchangeFees(year))) + "</b></p>\n" +
                        "<p>Profit/Loss (Fees included): <b>" + MoneyUtils.toStringHtml(getProfitLossTotal(year)) + "</b></p>\n" +
                        "<p>Trades: </p>\n" +
                        "<table style=\"width:100%\">\n" +
                        TABLE_HEAD
        );
        int i = 1;
        for (Transaction t : getTransactions(year)) {
            sb.append(t.toStringHtml(i++)).append("\n");
        }
        sb.append("</table>\n\n");
        return sb.toString();
    }


    public String toStringHtml(String navTag) {
        StringBuilder sb = new StringBuilder();
        sb.append("<h2><a href=\"#" + navTag + "\">" + getName() + "</a></h2>\n");
        if (hasErrors()) {
            sb.append("<p style=\"color:crimson;\">" + getError() + "<br></p>");
        }
        sb.append("<p>Quantity: <b>" + quantity + " pcs. </b></p>\n" +
                "<p>Total (Open position inc. all P/L and fees): <b>" + MoneyUtils.toStringHtml(overall) + "</b></p>\n" +
                "<p>Trade count (Sells): <b>" + getTradeCount(Transaction.Type.SELL) + "</b></p>\n" +
                "<p>Trade count (Buys): <b>" + getTradeCount(Transaction.Type.BUY) + "</b></p>\n" +
                "<p>Trade count: <b>" + getTradeCount() + "</b></p>\n" +
                "<p>Profit/Loss: <b>" + MoneyUtils.toStringHtml(getProfitLoss()) + "</b></p>\n" +
                "<p>Payed transaction fees: <b>" + MoneyUtils.toStringHtml(getPaidFees()) + "</b></p>\n" +
                "<p>Payed currency exchange fees: <b>" + MoneyUtils.toStringHtml(getPaidExchangeFees()) + "</b></p>\n" +
                "<p>Payed fees: <b>" + MoneyUtils.toStringHtml(getPaidFees().plus(getPaidExchangeFees())) + "</b></p>\n" +
                "<p>Profit/Loss (Fees included): <b>" + MoneyUtils.toStringHtml(getProfitLossTotal()) + "</b></p>\n" +
                "<p>Trades: </p>\n" +
                "<table style=\"width:100%\">\n" +
                TABLE_HEAD
        );
        int i = 1;
        for (Transaction t : getTransactions()) {
            sb.append(t.toStringHtml(i++)).append("\n");
        }
        sb.append("</table>\n\n");
        return sb.toString();
    }

    @Override
    public int compareTo(Asset o) {
        return Comparator.comparing(Asset::getType).thenComparing(Asset::getName).thenComparing(Asset::getIsin).compare(this, o);
    }


}
