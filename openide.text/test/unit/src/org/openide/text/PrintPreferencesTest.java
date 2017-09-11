/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
