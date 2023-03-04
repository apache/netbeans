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
