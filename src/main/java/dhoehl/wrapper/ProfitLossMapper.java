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

package dhoehl.wrapper;


import dhoehl.model.Transaction;
import dhoehl.utils.MoneyUtils;
import org.joda.money.BigMoney;
import org.joda.money.CurrencyUnit;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;

public class ProfitLossMapper {
    private final HashMap<Integer, BigMoney> mProfitLossMap = new HashMap<>();
    //This stores all buys, if there is a sell the first buy in the queue is the one that has to be used against the sell
    private final TreeSet<Transaction> mSellQueue = new TreeSet<>();

    private final CurrencyUnit currency;

    private String error;

    public ProfitLossMapper(CurrencyUnit currency) {
        this.currency = currency;
    }

    public void add(Transaction t) {
        //If this was a buy, add it to the queue, as this will be used to sell and calculate profit loss
        if (t.getType() == Transaction.Type.BUY) mSellQueue.add(t);
        else addProfitLoss(t);
    }

    public boolean hasError(){
        return error != null;
    }

    public String getError(){
        return error;
    }

    private void addProfitLoss(Transaction t) {
        //Some transactions (Auto generated ones) might have a quantity of zero. This is not really a sell
        if (t.getType() == Transaction.Type.SELL && t.getQuantity().abs().compareTo(BigDecimal.ZERO) > 0) {
            //Now we have multiple possible scenarios
            //1. Top quantity is > sell quantity -> Sell and deduct quantity from top item
            //2. Top quantity is == sell quantity -> Sell and remove top item
            //3. Top quantity is < sell quantity -> Sell top quantity, deduct sold quantity from to sell quantity, remove top item, repeat process with next item (if there is a next item)
            while (mSellQueue.size() > 0 && t.getQuantity().abs().compareTo(BigDecimal.ZERO) > 0) {
                //Get the first element, size is >0 so this does exist
                Transaction top = mSellQueue.first();
                int year = t.getTimestamp().getYear();
                //Scenario 2
                if (top.getQuantity().abs().compareTo(t.getQuantity().abs()) == 0) {
                    mProfitLossMap.computeIfAbsent(year, k -> BigMoney.zero(top.getAmount().getCurrencyUnit()));
                    mProfitLossMap.put(year, mProfitLossMap.get(year).plus(top.getAmount()).plus(t.getAmount()));
                    //Remove the old one, add the reduced new one
                    mSellQueue.remove(top);
                    t = t.reduceQuantityBy(t.getQuantity().abs());
                    //Scenario 1
                } else if (top.getQuantity().abs().compareTo(t.getQuantity().abs()) > 0) {
                    BigMoney lossProfit = top.getPrice().multipliedBy(t.getQuantity()).convertedTo(t.getAmount().getCurrencyUnit(), BigDecimal.ONE.setScale(top.getExchangeRate().scale(), RoundingMode.HALF_EVEN).divide(top.getExchangeRate(), RoundingMode.HALF_EVEN));
                    mProfitLossMap.computeIfAbsent(year, k -> BigMoney.zero(top.getAmount().getCurrencyUnit()));
                    mProfitLossMap.put(year, mProfitLossMap.get(year).plus(lossProfit).plus(t.getAmount()));
                    //Remove the old one, add the reduced new one
                    mSellQueue.remove(top);
                    t = t.reduceQuantityBy(t.getQuantity().abs());
                    mSellQueue.add(top.reduceQuantityBy(t.getQuantity().abs()));
                    //Scenario 3
                } else {
                    BigMoney lossProfit = t.getPrice().multipliedBy(top.getQuantity()).convertedTo(t.getAmount().getCurrencyUnit(), BigDecimal.ONE.setScale(t.getExchangeRate().scale(), RoundingMode.HALF_EVEN).divide(t.getExchangeRate(), RoundingMode.HALF_EVEN));
                    mProfitLossMap.computeIfAbsent(year, k -> BigMoney.zero(top.getAmount().getCurrencyUnit()));
                    mProfitLossMap.put(year, mProfitLossMap.get(year).plus(top.getAmount()).plus(lossProfit));
                    mSellQueue.remove(top);
                    t = t.reduceQuantityBy(top.getQuantity().abs());
                }
            }
            if (t.getQuantity().compareTo(BigDecimal.ZERO) < 0) {
                error = "Not enough past buys to complete all sells ("+t.getProduct()+", "+t.getIsin()+"). Profit Loss calculation for this ISIN WILL BE INCORRECT - Please provide a csv that reaches longer into the past";
                System.out.println(error);
            }
        }
    }

    public BigMoney getProfitLoss(int year) {
        BigMoney total = BigMoney.zero(currency);
        return mProfitLossMap.get(year) == null ? total : total.plus(mProfitLossMap.get(year));
    }

    public BigMoney getProfitLoss() {
        BigMoney total = BigMoney.zero(currency);
        for (BigMoney m : mProfitLossMap.values()) {
            total = total.plus(m);
        }
        return total;
    }

    /*

     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (mProfitLossMap.size() == 0)
            sb.append(" 0");
        for (Map.Entry<Integer, BigMoney> pl : mProfitLossMap.entrySet()) {
            sb.append(" ").append(pl.getKey()).append(": ").append(MoneyUtils.toString(pl.getValue()));
        }
        return sb.toString();
    }
}
