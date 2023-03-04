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

package org.netbeans.modules.html.palette.items;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;
import org.netbeans.modules.html.palette.HtmlPaletteUtilities;
import org.openide.text.ActiveEditorDrop;


/**
 *
 * @author Libor Kotouc
 */
public class TABLE implements ActiveEditorDrop {

    private static final int ROWS_DEFAULT = 2;
    private static final int COLS_DEFAULT = 2;
    private static final int BORDER_DEFAULT = 1;
    private static final int WIDTH_DEFAULT = 0;
    private static final int CSPAC_DEFAULT = 0;
    private static final int CPADD_DEFAULT = 0;
    
    private int rows = ROWS_DEFAULT;
    private int cols = COLS_DEFAULT;
    private int border = BORDER_DEFAULT;
    private int width = WIDTH_DEFAULT;
    private int cspac = CSPAC_DEFAULT;
    private int cpadd = CPADD_DEFAULT;
            
    public TABLE() {
    }

    public boolean handleTransfer(JTextComponent targetComponent) {

        TABLECustomizer c = new TABLECustomizer(this);
        boolean accept = c.showDialog();
        if (accept) {
            String body = createBody();
            try {
                HtmlPaletteUtilities.insert(body, targetComponent);
            } catch (BadLocationException ble) {
                accept = false;
            }
        }
        
        return accept;
    }

    private String createBody() {
        
        String tHead = generateTHead();
        String tBody = generateTBody();
        
        String strBorder = " border=\"" + border + "\""; // NOI18N
        
        String strWidth = "";
        if (width != WIDTH_DEFAULT)
            strWidth = " width=\"" + width + "\""; // NOI18N

        String strCspac = "";
        if (cspac != CSPAC_DEFAULT)
            strCspac = " cellspacing=\"" + cspac + "\""; // NOI18N
        
        String strCpadd = "";
        if (cpadd != CPADD_DEFAULT)
            strCpadd = " cellpadding=\"" + cpadd + "\""; // NOI18N
        
        
        String body = 
                "<table" + strBorder + strWidth + strCspac + strCpadd + ">\n" + // NOI18N
                "<thead>\n" + tHead + "</thead>\n" + // NOI18N
                "<tbody>\n" + tBody + "</tbody>\n" + // NOI18N
                "</table>\n"; // NOI18N
        
        return body;
    }
    
    private String generateTHead() {

        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < cols; i++)
            sb.append("<th></th>\n"); // NOI18N
    
        String thead = "<tr>\n" + sb.toString() + "</tr>\n"; // NOI18N
        
        return thead;
    }
    
    private String generateTBody() {
        
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < rows; i++) {
            sb.append("<tr>\n"); // NOI18N
            for (int j = 0; j < cols; j++)
                sb.append("<td></td>\n"); // NOI18N
            sb.append("</tr>\n"); // NOI18N
        }
                
        String tBody = sb.toString();
        
        return tBody;
    }

    public void setRows(int rows) {
        this.rows = rows;
    }

    public void setCols(int cols) {
        this.cols = cols;
    }

    public void setBorder(int border) {
        this.border = border;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public void setCspac(int cspac) {
        this.cspac = cspac;
    }

    public void setCpadd(int cpadd) {
        this.cpadd = cpadd;
    }

    public int getRows() {
        return rows;
    }

    public int getCols() {
        return cols;
    }

    public int getBorder() {
        return border;
    }

    public int getWidth() {
        return width;
    }

    public int getCspac() {
        return cspac;
    }

    public int getCpadd() {
        return cpadd;
    }
    
}
