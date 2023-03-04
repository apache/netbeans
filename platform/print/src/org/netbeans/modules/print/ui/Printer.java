/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.print.ui;

import java.awt.Graphics;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.util.List;

import org.netbeans.modules.print.util.Config;
import static org.netbeans.modules.print.util.UI.*;

/**
 * @author Vladimir Yaroslavskiy
 * @version 2005.12.21
 */
final class Printer implements Printable {

    void print(List<Paper> papers) {
        PrinterJob job = PrinterJob.getPrinterJob();
        myPapers = papers;
//out("SET PAPER: " + myPapers);

        if (job == null) {
            return;
        }
        job.setPrintable(this, Config.getDefault().getPageFormat());

        try {
            if (job.printDialog()) {
                job.print();
            }
        }
        catch (PrinterException e) {
            printError(i18n(Printer.class, "ERR_Printer_Problem", e.getLocalizedMessage())); // NOI18N
        }
        myPapers = null;
    }

    public int print(Graphics g, PageFormat pageFormat, int index) throws PrinterException {
//out("PAPER IS: " + myPapers.size());
        if (index == myPapers.size()) {
            return NO_SUCH_PAGE;
        }
//out("  print: " + index);
        myPapers.get(index).print(g);

        return PAGE_EXISTS;
    }

    private List<Paper> myPapers;
}
