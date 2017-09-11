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
 * License. When distributing the software, include this License Header
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
package org.netbeans.modules.print.provider;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;
import java.util.Date;

import javax.swing.JComponent;

import org.netbeans.spi.print.PrintPage;
import org.netbeans.spi.print.PrintProvider;
import org.netbeans.modules.print.util.Percent;
import static org.netbeans.modules.print.util.UI.*;

/**
 * @author Vladimir Yaroslavskiy
 * @version 2006.01.12
 */
public class ComponentProvider implements PrintProvider {

    public ComponentProvider(List<JComponent> components, String name, Date lastModified) {
        myName = name;
        myLastModified = lastModified;

        if (components != null) {
            myComponent = new ComponentPanel(components);
        }
    }

    protected JComponent getComponent() {
        return myComponent;
    }

    public PrintPage[][] getPages(int pageWidth, int pageHeight, double pageZoom) {
        List<ComponentPage> pages = new ArrayList<ComponentPage>();
        JComponent component = getComponent();

        if (component == null) {
            return new PrintPage[0][0];
        }
        int componentWidth = component.getWidth();
        int componentHeight = component.getHeight();

        double zoom = getZoom(pageZoom, pageWidth, pageHeight, componentWidth, componentHeight);

        componentWidth = (int) Math.floor(componentWidth * zoom);
        componentHeight = (int) Math.floor(componentHeight * zoom);

        int row = 0;
        int column = 0;

        for (int h = 0; h < componentHeight; h += pageHeight) {
            row++;
            column = 0;

            for (int w = 0; w < componentWidth; w += pageWidth) {
                column++;
                Rectangle piece = new Rectangle((column - 1) * pageWidth, (row - 1) * pageHeight, pageWidth, pageHeight);
                pages.add(new ComponentPage(component, piece, zoom, row - 1, column - 1));
            }
        }
        PrintPage[][] printPages = new PrintPage[row][column];

        for (ComponentPage page : pages) {
            printPages[page.getRow()][page.getColumn()] = page;
        }
        return printPages;
    }

    private double getZoom(double zoom, int pageWidth, int pageHeight, int componentWidth, int componentHeight) {
        double factor = Percent.getZoomFactor(zoom, -1.0);

        if (0 < factor) {
            return factor;
        }
        if (Percent.isZoomPage(zoom)) {
            factor = 0.0;
        }
        int zoomWidth = Percent.getZoomWidth(zoom, -1);
        int zoomHeight = Percent.getZoomHeight(zoom, -1);

        if (factor == 0.0) {
            zoomWidth = 1;
            zoomHeight = 1;
        }
        return getZoom((double) (pageWidth * zoomWidth) / (double) componentWidth, (double) (pageHeight * zoomHeight) / (double) componentHeight);
    }

    private double getZoom(double widthZoom, double heightZoom) {
        if (widthZoom > 0 && heightZoom > 0) {
            return Math.min(widthZoom, heightZoom);
        }
        if (widthZoom < 0 && heightZoom > 0) {
            return heightZoom;
        }
        if (widthZoom > 0 && heightZoom < 0) {
            return widthZoom;
        }
        return 1.0;
    }

    public String getName() {
        return myName;
    }

    public Date lastModified() {
        return myLastModified;
    }

    private String myName;
    private Date myLastModified;
    private JComponent myComponent;
}
