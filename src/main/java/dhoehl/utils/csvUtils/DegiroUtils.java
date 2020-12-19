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

package dhoehl.utils.csvUtils;

import java.security.SecureRandom;

public class DegiroUtils {
    private static final String ID_TEMPLATE = "abcdef0123456789";
    private static final String ID_PATTERN = "xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx";
    private static final int ID_LENGTH = 36;

    public static String alterRow(String row) {
        row = removeQuotes(row);
        row = addId(row);
        return row;
    }

    /*
    Some trades (Everything not initiated by the user himself) do not get an id but "".
    We just assign an id to the transaction using degiro pattern.
     */
    private static String addId(String row) {
        String[] splitted = row.split(",");
        if (splitted.length == 18) return row;

        SecureRandom sr = new SecureRandom();
        StringBuilder sb = new StringBuilder(row);
        for (int i = 0; i < ID_LENGTH; i++) {
            if (ID_PATTERN.charAt(i) == '-') sb.append("-");
            else sb.append(ID_TEMPLATE.charAt(sr.nextInt(ID_TEMPLATE.length())));
        }
        return sb.toString();
    }

    /*
    For some reason Degiro uses quotes around name cells that contain a comma where the comma already is the seperator inside the csv
    To make String.split work this methods replaces all commas within the name tag with dots and removes the quotation marks
     */
    private static String removeQuotes(String row) {
        String[] splitted = row.split("\"");
        if (splitted.length == 1) return row;

        if (splitted.length == 3) {
            splitted[1] = splitted[1].replaceAll(",", ".");
        } else {
            throw new UnsupportedOperationException("Unsupported CSV document found");
        }
        return splitted[0] + splitted[1] + splitted[2];
    }
}
