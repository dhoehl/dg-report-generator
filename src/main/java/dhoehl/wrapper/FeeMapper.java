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

public class FeeMapper {
    private final HashMap<Integer, BigMoney> mFeeMap = new HashMap<>();
    private CurrencyUnit currency;

    public FeeMapper(CurrencyUnit currency) {
        this.currency = currency;
    }


    public void add(Transaction t) {
        int year = t.getTimestamp().getYear();

        if (t.getFee() != null) {
            mFeeMap.computeIfAbsent(year, k -> BigMoney.zero(t.getFee().getCurrencyUnit()));
            mFeeMap.put(year, mFeeMap.get(year).plus(t.getFee()));
        }
    }

    public BigMoney getFees(int year) {
        BigMoney total = BigMoney.zero(currency);
        return mFeeMap.get(year) == null ? total : total.plus(mFeeMap.get(year));
    }

    public BigMoney getFees() {
        BigMoney total = BigMoney.zero(currency);
        for (BigMoney m : mFeeMap.values()) {
            total = total.plus(m);
        }
        return total;
    }

    @Override
    public String toString() {
        return MoneyUtils.toString(getFees().negated());
    }
}
