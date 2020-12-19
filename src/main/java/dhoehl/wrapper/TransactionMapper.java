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

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

public class TransactionMapper {
    private final Map<Integer, Set<Transaction>> mTransactionMap = new HashMap<>();


    public boolean add(Transaction t) {
        int year = t.getTimestamp().getYear();
        return mTransactionMap.computeIfAbsent(year, k -> new TreeSet<>()).add(t);
    }

    public Set<Transaction> getTransactions(int year) {
        return mTransactionMap.get(year);
    }

    public Set<Transaction> getTransactions() {
        final Set<Transaction> allTransactions = new TreeSet<>();
        mTransactionMap.values().forEach(allTransactions::addAll);
        return allTransactions;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Transaction t : getTransactions()) {
            sb.append(t).append("\n");
        }
        return sb.toString();
    }

    public long getTradeCount(int year) {
        return getTransactions(year).size();
    }

    public long getTradeCount() {
        return getTransactions().size();
    }

    public long getTradeCount(Transaction.Type type) {
        return getTransactions().stream().filter(t -> t.getType() == type).count();
    }

    public long getTradeCount(Transaction.Type type,int year) {
         return getTransactions(year).stream().filter(t -> t.getType() == type).count();
    }
}
