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

package dhoehl.html;

public interface ReportElements {
  String DEGIRO_HEADER = "<!doctype html>\n" +
          "<html lang=\"de\">\n" +
          "\n" +
          "<head>\n" +
          "  <meta charset=\"utf-8\">\n" +
          "  <meta name=\"description\" content=\"Degiro inofficial report\">\n" +
          "  <title>Degiro report (inofficial)</title>\n" +
          "  <style>\n" +
          "    table {\n" +
          "      width: 100%;\n" +
          "    }\n" +
          "\n" +
          "    table,\n" +
          "    th,\n" +
          "    td {\n" +
          "      border: 1px solid black;\n" +
          "      border-collapse: collapse;\n" +
          "    }\n" +
          "\n" +
          "    th,\n" +
          "    td {\n" +
          "      padding: 15px;\n" +
          "      text-align: left;\n" +
          "    }\n" +
          "\n" +
          "    #t01 tr:nth-child(even) {\n" +
          "      background-color: #eee;\n" +
          "    }\n" +
          "\n" +
          "    #t01 tr:nth-child(odd) {\n" +
          "      background-color: #fff;\n" +
          "    }\n" +
          "\n" +
          "    #t01 th {\n" +
          "      background-color: sandybrown;\n" +
          "      color: white;\n" +
          "    }\n" +
          "  </style>\n" +
          "</head>";

  String DEGIRO_ERROR = "<h4 style=\"color:crimson;\">Not all rows could be interpreted correctly. The respective sections are marked as such. A\n" +
          "    complete list of all faults can be found at the end of this document. It is appreciated if you send your csv (of\n" +
          "    course you can alter the data and only include the ones with the failure) by <a href=\"mailto:dhoehl@arcor.de\">\n" +
          "      mail</a>. I will try my best to bugfix things asap.<br><br> </h4>";


}
