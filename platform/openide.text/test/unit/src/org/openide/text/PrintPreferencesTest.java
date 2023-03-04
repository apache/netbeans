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

package org.openide.text;

import java.awt.Font;
import java.awt.font.TextAttribute;
import java.util.Iterator;
import java.util.Map;
import org.netbeans.junit.NbTestCase;
import junit.framework.*;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import java.awt.print.PageFormat;
import java.awt.print.Paper;
import java.awt.print.PrinterJob;
import java.util.prefs.Preferences;
import org.openide.nodes.BeanNode;
import org.openide.util.NbPreferences;

/**
 * @author Radek Matous
 */
public class PrintPreferencesTest extends NbTestCase {
    static {
        System.setProperty("org.openide.windows.DummyWindowManager.VISIBLE", "false");
    }
    public PrintPreferencesTest(String testName) {
        super(testName);
    }
    
    public void testGetHeaderFont() {
        Font expResult = PrintPreferences.getHeaderFont();
        PrintPreferences.setHeaderFont(expResult);
        Font result = PrintPreferences.getHeaderFont();
        assertEquals(expResult, result);
        Font derived = result.deriveFont(java.awt.Font.BOLD,8);
        PrintPreferences.setHeaderFont(derived);
        assertEquals(derived, PrintPreferences.getHeaderFont());
    }
    
    public void testGetFooterFont() {
        Font expResult = PrintPreferences.getFooterFont();
        PrintPreferences.setFooterFont(expResult);
        Font result = PrintPreferences.getFooterFont();
        assertEquals(expResult, result);
        Font derived = result.deriveFont(java.awt.Font.BOLD,8);
        PrintPreferences.setFooterFont(derived);
        assertEquals(derived, PrintPreferences.getFooterFont());
    }
    
    public void testGetPageFormat() {
        PrinterJob pj = PrinterJob.getPrinterJob();
        PageFormat expResult = PrintPreferences.getPageFormat(pj);
        PrintPreferences.setPageFormat(expResult);
        PageFormat result = PrintPreferences.getPageFormat(pj);
        assertEquals(expResult.getHeight(), result.getHeight());
        assertEquals(expResult.getWidth(), result.getWidth());
        assertEquals(expResult.getOrientation(), result.getOrientation());
        assertEquals(expResult.getPaper().getHeight(), result.getPaper().getHeight());
        assertEquals(expResult.getPaper().getWidth(), result.getPaper().getWidth());
        assertEquals(expResult.getPaper().getImageableHeight(), result.getPaper().getImageableHeight());
        assertEquals(expResult.getPaper().getImageableWidth(), result.getPaper().getImageableWidth());
        assertEquals(expResult.getPaper().getImageableX(), result.getPaper().getImageableX());
        assertEquals(expResult.getPaper().getImageableY(), result.getPaper().getImageableY());
        
        double w = expResult.getPaper().getWidth() + 10;
        double h = expResult.getPaper().getHeight() + 10;
        Paper p = expResult.getPaper();
        double ix = p.getImageableX() + 10;
        double iy = p.getImageableY() + 10;
        double iw = p.getImageableWidth() + 10;
        double ih = p.getImageableHeight() + 10;
        p.setImageableArea(ix, iy, iw, ih);
        p.setSize(w, h);
        expResult.setPaper(p);
        PrintPreferences.setPageFormat(expResult);
        assertEquals(h, PrintPreferences.getPageFormat(pj).getHeight());
        assertEquals(w, PrintPreferences.getPageFormat(pj).getWidth());
        assertEquals(ix, PrintPreferences.getPageFormat(pj).getPaper().getImageableX());
        assertEquals(iy, PrintPreferences.getPageFormat(pj).getPaper().getImageableY());
        assertEquals(iw, PrintPreferences.getPageFormat(pj).getPaper().getImageableWidth());
        assertEquals(ih, PrintPreferences.getPageFormat(pj).getPaper().getImageableHeight());
        
        expResult.setOrientation(PageFormat.REVERSE_LANDSCAPE);
        PrintPreferences.setPageFormat(expResult);
        assertEquals(PageFormat.REVERSE_LANDSCAPE, PrintPreferences.getPageFormat(pj).getOrientation());
    }
    
    public void testGetWrap() {
        PrintPreferences.setWrap(true);
        assertEquals(true, PrintPreferences.getWrap());
        PrintPreferences.setWrap(false);
        assertEquals(false, PrintPreferences.getWrap());
    }
    
    public void testGetHeaderFormat() {
        String expResult = "my header format";
        PrintPreferences.setHeaderFormat(expResult);
        String result = PrintPreferences.getHeaderFormat();
        assertEquals(expResult, result);
    }
    
    public void testGetFooterFormat() {
        String expResult = "my footer format";
        PrintPreferences.setFooterFormat(expResult);
        String result = PrintPreferences.getFooterFormat();
        assertEquals(expResult, result);
    }
    
    public void testGetHeaderAlignment() {
        PrintPreferences.Alignment expResult = PrintPreferences.Alignment.LEFT;
        PrintPreferences.setHeaderAlignment(expResult);
        PrintPreferences.Alignment result = PrintPreferences.getHeaderAlignment();
        assertEquals(expResult, result);
        
        expResult = PrintPreferences.Alignment.RIGHT;
        PrintPreferences.setHeaderAlignment(expResult);
        result = PrintPreferences.getHeaderAlignment();
        assertEquals(expResult, result);
        
        expResult = PrintPreferences.Alignment.CENTER;
        PrintPreferences.setHeaderAlignment(expResult);
        result = PrintPreferences.getHeaderAlignment();
        assertEquals(expResult, result);
    }
    
    public void testGetFooterAlignment() {
        PrintPreferences.Alignment expResult = PrintPreferences.Alignment.LEFT;
        PrintPreferences.setFooterAlignment(expResult);
        PrintPreferences.Alignment result = PrintPreferences.getFooterAlignment();
        assertEquals(expResult, result);
        
        expResult = PrintPreferences.Alignment.RIGHT;
        PrintPreferences.setFooterAlignment(expResult);
        result = PrintPreferences.getFooterAlignment();
        assertEquals(expResult, result);
        
        expResult = PrintPreferences.Alignment.CENTER;
        PrintPreferences.setFooterAlignment(expResult);
        result = PrintPreferences.getFooterAlignment();
        assertEquals(expResult, result);
        
    }
        
    public void testGetLineAscentCorrection() {
        float expResult = 0.0F;
        for (int i = 0; i < 10; i++) {
            expResult += 1;
            PrintPreferences.setLineAscentCorrection(expResult);
            float result = PrintPreferences.getLineAscentCorrection();
            assertEquals(expResult, result);
        }
    }
}
