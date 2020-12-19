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

package dhoehl.utils;


import dhoehl.model.Transaction;
import org.joda.money.BigMoney;
import org.joda.money.Money;
import org.joda.money.format.MoneyFormatter;
import org.joda.money.format.MoneyFormatterBuilder;

import java.math.RoundingMode;
import java.util.Locale;

public class MoneyUtils {
    private static MoneyFormatter getDefaultLocaleFormatter() {
        return new MoneyFormatterBuilder()
                //.appendAmount(MoneyAmountStyle.LOCALIZED_NO_GROUPING)
                .appendAmountLocalized()
                .appendLiteral(" ")
                .appendCurrencySymbolLocalized()
                .toFormatter().withLocale(Locale.getDefault());
    }

    public static String toString(BigMoney money) {
        if (money == null) return "";
        MoneyFormatter f = getDefaultLocaleFormatter();
        //Display with normal currency decimal places
        return f.print(Money.of(money, RoundingMode.HALF_EVEN));
    }

    public static String toStringHtml(BigMoney money) {
        if (money == null) return "";
        MoneyFormatter f = getDefaultLocaleFormatter();
        //Display with normal currency decimal places
        if (money.isNegative())
            return "<span  style=\"color:" + Transaction.COLOR_SELL + "\">" + f.print(Money.of(money, RoundingMode.HALF_EVEN)) + "</span >";
        else if (money.isPositive())
            return "<span  style=\"color:" + Transaction.COLOR_BUY + "\">" + f.print(Money.of(money, RoundingMode.HALF_EVEN)) + "</span >";
        else return "<span >" + f.print(Money.of(money, RoundingMode.HALF_EVEN)) + "</span >";

    }
}
