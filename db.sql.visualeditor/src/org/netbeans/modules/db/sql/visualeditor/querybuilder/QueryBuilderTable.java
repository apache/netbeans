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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

/**
 *
 * @author  Sanjay Dhamankar
 */

package org.netbeans.modules.db.sql.visualeditor.querybuilder;

import javax.swing.JTable;

import javax.swing.table.TableColumn;

import java.awt.*;


import org.netbeans.modules.db.sql.visualeditor.Log;
import org.openide.util.NbBundle;

// Represents the information presented inside a table node, which includes
// selected status, key status, and column name

public class QueryBuilderTable extends JTable {

    private boolean DEBUG = false;


    // Constructor

    public QueryBuilderTable( QueryBuilderTableModel model) {

        super();
        super.setModel( model );

        Log.getLogger().entering("QueryBuilderTable", "constructor", model); // NOI18N

        this.setAutoResizeMode (JTable.AUTO_RESIZE_OFF);

        // This may not be required afterall. We need to keep the size of the cell fixed.
        this.initColumnSizes(this, model);
        this.setShowHorizontalLines(false);
        this.setShowVerticalLines(false);
        this.setBackground(Color.white);
        this.setRowHeight(this.getRowHeight() + 2);
        this.setRowSelectionAllowed (false);
        this.setTableHeader (null);

        getAccessibleContext().setAccessibleName(NbBundle.getMessage(QueryBuilderTable.class, "ACS_QueryBuilderTableName"));
        getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(QueryBuilderTable.class, "ACS_QueryBuilderTableDescription"));
    }


    // Methods

    private void initColumnSizes(JTable table, QueryBuilderTableModel model) {

        TableColumn column = null;
        Component comp = null;
        int headerWidth = 0;
        int cellWidth = 0;

        for (int i = 0; i < getColumnCount(); i++) {

            column = table.getColumnModel().getColumn(i);

            comp = table.getDefaultRenderer(column.getClass()).
                getTableCellRendererComponent(
                    table, column.getHeaderValue(),
                    false, false, -1, 0);
            headerWidth = comp.getPreferredSize().width;

            try {
                comp = column.getHeaderRenderer().
                    getTableCellRendererComponent(
                        null, column.getHeaderValue(),
                        false, false, 0, 0);
                headerWidth = comp.getPreferredSize().width;
            } catch (NullPointerException e) {
            }

            if ( i  != 0 )
            {
                for (int j=0; j< table.getRowCount(); j++)
                {
                    comp = table.getDefaultRenderer(model.getColumnClass(i)).
                        getTableCellRendererComponent(
                            table, getValueAt(j, i),
                            false, false, 0, i);
                    int tmpCellWidth = comp.getPreferredSize().width;

                    if ( tmpCellWidth > cellWidth )
                        cellWidth = tmpCellWidth;
                }
            }

            //XXX: Before Swing 1.1 Beta 2, use setMinWidth instead.
            column.setPreferredWidth(Math.max(headerWidth+15, cellWidth+15));
        }

        table.addNotify();
    }
}
