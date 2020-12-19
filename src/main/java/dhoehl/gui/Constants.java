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

package dhoehl.gui;

public interface Constants {
    String GIT_URL = "https://github.com/dhoehl/dg-report-generator";
    String DISCLAIMER_GUI = "Disclaimer: This tool will generate reports for Degiro csv files that are exported using the export function within your depot. It is not an official tool from or by DEGIRO(c). This tool is provided in good faith, however I make no representation or warranty of any kind, express or implied, regarding the accuracy, validity, reliability, availability or completeness of this tool. Use at your own risk.";
    String DISCLAIMER_HTML = "This report was generated using the inoffical dg report generator tool (<a href=\""+GIT_URL+"\">"+GIT_URL+"</a>). It is not an official report from or by DEGIRO(c). This report is created in good faith, however I make no representation or warranty of any kind, express or implied, regarding the accuracy, validity, reliability, availability or completeness of this report. Use at your own risk. It is also ONLY intended to get an overview. The offical report always is the one to use and only is provided by DEGIRO(c). Features and bug fixes may be reported by <a href=\"mailto:dhoehl@arcor.de?subject=DGRG_BUG_OR_FEATURE\">mail</a>";
    String APP_VERSION = "0.1b";
}
