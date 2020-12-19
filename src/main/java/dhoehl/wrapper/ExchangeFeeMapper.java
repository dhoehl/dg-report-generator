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

import java.util.HashMap;

public class ExchangeFeeMapper {
    private final HashMap<Integer, BigMoney> mExchangeFeeMap = new HashMap<>();
    private final CurrencyUnit currency;

    public ExchangeFeeMapper(CurrencyUnit currency) {
        this.currency = currency;
    }


    public void add(Transaction t) {
        int year = t.getTimestamp().getYear();
        mExchangeFeeMap.computeIfAbsent(year, k -> BigMoney.zero(t.getExchangeFee().getCurrencyUnit()));
        mExchangeFeeMap.put(year, mExchangeFeeMap.get(year).plus(t.getExchangeFee()));
    }


    public BigMoney getExchangeFees(int year) {
        BigMoney total = BigMoney.zero(currency);
        return mExchangeFeeMap.get(year) == null ? total : total.plus(mExchangeFeeMap.get(year));
    }

    public BigMoney getExchangeFees() {
        BigMoney total = BigMoney.zero(currency);
        for (BigMoney m : mExchangeFeeMap.values()) {
            total = total.plus(m);
        }
        return total;
    }

    @Override
    public String toString() {
        return MoneyUtils.toString(getExchangeFees().negated());
    }
}
