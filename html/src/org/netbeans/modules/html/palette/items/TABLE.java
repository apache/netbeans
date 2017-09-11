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
