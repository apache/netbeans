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
/*
 * HTMLTable.java
 *
 * Created on October 17, 2002, 7:58 PM
 */

package org.netbeans.performance.spi.html;
import java.awt.Color;
import java.util.*;
/** Wrapper for an HTML table.  This wrapper tries to do a few clever things.
 * It uses the preferredWidth property of HTML elements added to it to determine
 * how to set the COLSPAN property for each elements' TD tags.  The number of
 * columns in the table is either set explicitly in the constructor, or defaults
 * to 2.  If an element's preferred width is greater than the number of columns
 * the table has, it will be normalized to the number of columns.  If an elements
 * preferred width makes it too wide to fit on the current row, the row will be
 * filled out with empty elements, and the new item will be added to the next
 * row.<P>HTML tables have titles.  For the case of nested tables, the background
 * color of each level of nesting is automaticall set differently, making nested
 * tables easier to read.
 */
public class HTMLTable extends AbstractHTMLContainer {
    int rowcount=2;
    public HTMLTable(String title, int rowCount) {
        super(title);
        rowcount = rowCount;
        this.title = title;
    }

    public HTMLTable(String title, int rowCount, int preferredWidth) {
        super(title, preferredWidth);
        this.rowcount = rowCount;
    }

    public HTMLTable(String title) {
        super(title);
    }
    
    public HTMLTable() {
    }
    
    public HTMLTable(int rowCount, int preferredWidth) {
        super (preferredWidth);
        this.rowcount =rowCount;
    }

    public HTMLTable(int rowCount) {
        this.rowcount = rowCount;
    }
    
    int nestingLevel = 0;
    /**Get the background color for the header.  This depends on the
     * value of nestingLevel.
     */
    private String getHeaderBgColor () {
        float[] hsb = new float[3];
        hsb = Color.RGBtoHSB(140, 140, 204, hsb);
        hsb[0] += .25f * nestingLevel;
        int color = Color.HSBtoRGB(hsb[0], hsb[1], hsb[2]);
        Color c = new Color (color);
        int[] rgb = new int[] {c.getRed(), c.getGreen(), c.getBlue() };
        StringBuffer sb = new StringBuffer("#");
        for (int i=0; i < rgb.length; i++) {
            sb.append (Integer.toHexString(rgb[i]));
        }
        return sb.toString();
    }

    public synchronized void toHTML(StringBuffer sb) {
        if (items.isEmpty()) return;
        HTMLIterator it = iterator();
        int idx=rowcount; 
        if (title != null) {
            String colorString = getHeaderBgColor ();
            genTableOpen(sb, title, rowcount, colorString);
        }
        while (it.hasNext()) {
            if (idx % rowcount == 0) {
                if (idx != rowcount) sb.append("\n</TR>");
                if (it.hasNext()) sb.append("<TR>");
            }
            HTML next = it.nextHTML();
            int width = next.getPreferredWidth();
            if (width == DONT_CARE) {
                sb.append("\n  <TD BGCOLOR=#FFFFFF>");
            } else {
                if (width == SINGLE_ROW) {
                    boolean rowFilled=false;
                    while (idx % rowcount != 0) {
                        genNullTableEntry(sb);
                        idx++;
                        rowFilled=true;
                    }
                    if (rowFilled) sb.append ("</TR>\n<TR>\n");
                    sb.append("\n  <TD BGCOLOR=#FFFFFF COLSPAN=");
                    sb.append(Integer.toString(rowcount));
                    sb.append(">\n");
                    idx+=rowcount-1;
                } else {
                    if (width + (idx % rowcount) > rowcount) {
                        boolean filled=false;
                        while (idx % rowcount != 0) {
                            genNullTableEntry(sb);
                            idx++;
                            filled=true;
                        }
                        if (filled) sb.append("</TR>\n<TR>");
                        int w = Math.min(width, rowcount);
                        sb.append("\n  <TD BGCOLOR=#FFFFFF COLSPAN=");
                        sb.append(Integer.toString(w));
                        sb.append(">");
                        idx +=w-1;
                    } else {
                        sb.append("\n  <TD BGCOLOR=#FFFFFF COLSPAN=");
                        sb.append(Integer.toString(width));
                        sb.append(">");
                        idx+=width-1;
                    }
                }
            }
            if (next instanceof HTMLTable) ((HTMLTable) next).nestingLevel+=nestingLevel+1;
            next.toHTML (sb);
            if (next instanceof HTMLTable) ((HTMLTable) next).nestingLevel-=nestingLevel+1;
            sb.append("</TD>");
            idx++;
        }
        while (idx % rowcount != 0) {
            genNullTableEntry(sb);
            idx++;
        }
        sb.append("</TR>");
        genTableClose(sb);
    }
}

