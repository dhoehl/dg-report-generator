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


import dhoehl.degiro.Depot;
import dhoehl.gui.DegiroDialog;
import dhoehl.utils.ExceptionUtils;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

public class DgReportGenerator {
    private static final boolean DEBUG = false;


    public static void main(String[] args) {
        try {
            if (DEBUG) {
                File transactions = new File("src\\Transactions(1).csv");
                Depot degiro = new Depot(transactions);
                //System.out.println(degiro.getAllTypeAssetsString(Asset.Type.STOCK));
                System.out.println(degiro.getAsset("DE000UV801K0"));
            } else if (args.length == 0) {
                DegiroDialog window = new DegiroDialog();
                window.open();
            } else {
                File transactions = new File(args[0]);
                Depot degiro = new Depot(transactions);
                FileUtils.writeStringToFile(new File(System.getProperty("user.dir") + "/Report.html"), degiro.toStringHtml());

            }
        } catch (Exception e) {
            //Quick and dirty catch all exceptions and write them to file
            try {
                FileUtils.writeStringToFile(new File(System.getProperty("user.dir") + "/log.txt"), ExceptionUtils.getStackTrace(e));
                e.printStackTrace();
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }


    }
}
