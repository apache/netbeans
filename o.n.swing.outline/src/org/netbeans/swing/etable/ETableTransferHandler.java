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
package org.netbeans.swing.etable;

import java.awt.datatransfer.Transferable;
import javax.swing.JComponent;
import javax.swing.TransferHandler;

/**
 * Used for creating a proper clipboard representation.
 * @author David Strupl
 */
public class ETableTransferHandler extends TransferHandler {
    
    @Override
    protected Transferable createTransferable(JComponent c) {
        if (c instanceof ETable) {
            ETable table = (ETable) c;
            int[] rows;
            int[] cols;
            
            if (!table.getRowSelectionAllowed() && !table.getColumnSelectionAllowed()) {
                return null;
            }
            
            if (!table.getRowSelectionAllowed()) {
                int rowCount = table.getRowCount();
                
                rows = new int[rowCount];
                for (int counter = 0; counter < rowCount; counter++) {
                    rows[counter] = counter;
                }
            } else {
                rows = table.getSelectedRows();
            }
            
            if (!table.getColumnSelectionAllowed()) {
                int colCount = table.getColumnCount();
                
                cols = new int[colCount];
                for (int counter = 0; counter < colCount; counter++) {
                    cols[counter] = counter;
                }
            } else {
                cols = table.getSelectedColumns();
            }
            
            if (rows == null || cols == null || rows.length == 0 || cols.length == 0) {
                return null;
            }
            
            StringBuffer plainBuf = new StringBuffer();
            String itemDelim = table.getTransferDelimiter(false);
            String lineDelim = table.getTransferDelimiter(true);
            
            for (int row = 0; row < rows.length; row++) {
                for (int col = 0; col < cols.length; col++) {
                    Object obj = table.getValueAt(rows[row], cols[col]);
                    String val = table.convertValueToString(obj);
                    plainBuf.append(val + itemDelim);
                }
                // we want a newline at the end of each line and not a tab
                plainBuf.delete(plainBuf.length() - itemDelim.length(), plainBuf.length()-1);
                plainBuf.append(lineDelim);
            }
            // remove the last newline
            plainBuf.deleteCharAt(plainBuf.length() - 1);
            return new ETableTransferable(plainBuf.toString());
        }
        
        return null;
    }
    
    @Override
    public int getSourceActions(JComponent c) {
        return COPY;
    }
}
