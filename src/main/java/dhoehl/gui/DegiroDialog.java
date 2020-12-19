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


import dhoehl.degiro.Depot;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.*;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.URI;


public class DegiroDialog extends JFrame implements Constants {
    protected Shell shell;
    private Text tvCsvPath;

    public void open() {
        Display display = Display.getDefault();
        createContents();
        shell.open();
        shell.layout();
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) {
                display.sleep();
            }
        }
        SWTResourceManager.dispose();
    }

    protected void createContents() {
        shell = new Shell();
        shell.setSize(450, 300);
        shell.setText("DG report generator (v" + APP_VERSION + ", Author: Dennis Hoehl, dhoehl@arcor.de)");

        Button btnFileChoose = new Button(shell, SWT.NONE);
        btnFileChoose.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                FileFilter filter = new FileNameExtensionFilter("CSV files (*.csv)", "csv", "CSV");
                JFileChooser chooser = new JFileChooser();
                chooser.setFileFilter(filter);
                int userSelection = chooser.showOpenDialog(null);
                if (userSelection == JFileChooser.APPROVE_OPTION)
                    tvCsvPath.setText(chooser.getSelectedFile().getAbsolutePath());
            }
        });
        btnFileChoose.setBounds(332, 18, 75, 25);
        btnFileChoose.setText("Choose CSV");

        tvCsvPath = new Text(shell, SWT.BORDER);
        tvCsvPath.setText("Path to CSV");
        tvCsvPath.setBounds(41, 20, 250, 21);

        Button btnGenerate = new Button(shell, SWT.NONE);
        btnGenerate.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                File transactions = new File(tvCsvPath.getText().trim());
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setDialogTitle("Where do you want to save the report?");

                int userSelection = fileChooser.showSaveDialog(null);
                if (userSelection == JFileChooser.APPROVE_OPTION) {
                    File fileToSave = fileChooser.getSelectedFile();
                    Depot degiro = new Depot(transactions);
                    if (!FilenameUtils.getExtension(fileToSave.getName()).equalsIgnoreCase("html")) {
                        fileToSave = new File(fileToSave.toString() + ".html");
                    }
                    try {
                        FileUtils.writeStringToFile(fileToSave, degiro.toStringHtml());
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }

                    System.exit(0);

                }
            }
        });
        btnGenerate.setBounds(169, 72, 122, 25);
        btnGenerate.setText("Generate report");

        Button btnPaypal = new Button(shell, SWT.NONE);
        btnPaypal.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                openPaypal();
            }
        });
        btnPaypal.setImage(SWTResourceManager.getImage("..//src//paypal.png"));
        btnPaypal.setBounds(169, 143, 115, 31);

        Label tvPaypal = new Label(shell, SWT.NONE);
        tvPaypal.setBounds(105, 122, 250, 15);
        tvPaypal.setText("If you like the tool, please buy me a coffee :)");

        Label tvDisclaimer = new Label(shell, SWT.WRAP | SWT.BORDER);
        tvDisclaimer.setFont(SWTResourceManager.getFont("Segoe UI", 6, SWT.NORMAL));
        tvDisclaimer.setBounds(10, 202, 414, 49);
        tvDisclaimer.setText(DISCLAIMER_GUI);

        Label lblNewLabel = new Label(shell, SWT.NONE);
        lblNewLabel.setBounds(169, 103, 55, 15);

    }

    private void openPaypal() {
        Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
        if (desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
            try {
                desktop.browse(new URI("https://www.paypal.com/paypalme/dhoehl"));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
