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

package dhoehl.degiro;


import dhoehl.model.ParsingError;
import dhoehl.model.Table;
import dhoehl.model.Transaction;
import dhoehl.utils.ExceptionUtils;
import org.joda.money.BigMoney;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class TransactionParser {
    private static final String DATE_TIME_PATTERN = "dd-MM-yyyyHH:mm";
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern(DATE_TIME_PATTERN);
    private Set<Transaction> mParsedTransactions;
    private List<ParsingError> mParsingErrors = new ArrayList<>();

    public TransactionParser(Table transactionTable){
        mParsedTransactions = parseTransactions(transactionTable);
    }

    private Set<Transaction> parseTransactions(Table transactionTable) {
        //Get rid of the Headline - No useful data
        transactionTable.deleteRow(0);
        Set<Transaction> transactionMap = new TreeSet<>();
        //We assume this table to have the same amount of columns for each row
        for (int i = 0; i < transactionTable.getRows(); i++) {
            //There is one transaction per row
            //Read all the fields
            String toParse = "";
            try {
                toParse = transactionTable.getCell(i, 0) + transactionTable.getCell(i, 1);
                LocalDateTime timestamp = LocalDateTime.parse(toParse, DATE_TIME_FORMATTER);

                toParse = transactionTable.getCell(i, 2);
                String product = toParse;

                toParse = transactionTable.getCell(i, 3);
                String isin = toParse;

                toParse = transactionTable.getCell(i, 4);
                String exchange = toParse;

                toParse = transactionTable.getCell(i, 5);
                BigDecimal quantity = new BigDecimal(toParse);

                toParse = transactionTable.getCell(i, 6) + transactionTable.getCell(i, 7);
                BigMoney price = toParse.equals("") ? null : BigMoney.parse(toParse);

                toParse = transactionTable.getCell(i, 8) + transactionTable.getCell(i, 9);
                BigMoney amountLocal = toParse.equals("") ? null : BigMoney.parse(toParse);

                toParse = transactionTable.getCell(i, 10) + transactionTable.getCell(i, 11);
                BigMoney amount = toParse.equals("") ? null : BigMoney.parse(toParse);

                toParse = transactionTable.getCell(i, 12);
                BigDecimal exchangeRate = toParse.equals("") ? BigDecimal.ONE : new BigDecimal(toParse);

                toParse = transactionTable.getCell(i, 13) + transactionTable.getCell(i, 14);
                BigMoney fee = toParse.equals("") ? null : BigMoney.parse(toParse);

                toParse = transactionTable.getCell(i, 15) + transactionTable.getCell(i, 16);
                BigMoney overall = toParse.equals("") ? null : BigMoney.parse(toParse);

                toParse = transactionTable.getCell(i, 17);
                String id = toParse;

                Transaction transaction = new Transaction(id, timestamp, product, isin, exchange, quantity, price, amount, amountLocal, exchangeRate, fee, overall);
                transactionMap.add(transaction);
            } catch (Exception e) {
                //To compare with the csv add i+2 (starts at zero and first row is stripped)
                ParsingError pe =  new ParsingError("Failed to parse " + toParse + " in row " + (i+2) + " (" + transactionTable.getRow(i) + ")\nError: " + e.getMessage(), ExceptionUtils.getStackTrace(e));
                mParsingErrors.add(pe);
                System.out.println(pe);
            }
        }
        return transactionMap;
    }

    public Set<Transaction> getParsedTransactions() {
        return mParsedTransactions;
    }

    public boolean hasParsingErrors(){
        return mParsingErrors.size() > 0;
    }
    public List<ParsingError> getParsingErrors() {
        return mParsingErrors;
    }
}
