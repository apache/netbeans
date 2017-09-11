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
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.hibernate.loaders.cfg.multiview;

import javax.swing.table.AbstractTableModel;
import org.netbeans.modules.hibernate.cfg.model.SessionFactory;
import org.openide.util.NbBundle;

/**
 * Table model for the collection cache table
 * 
 * @author Dongmei Cao
 */
public class CollectionCachesTableModel extends AbstractTableModel {

    private static final String[] columnNames = {
        NbBundle.getMessage(SecurityTableModel.class, "LBL_Collection"),
        NbBundle.getMessage(SecurityTableModel.class, "LBL_Region"),
        NbBundle.getMessage(SecurityTableModel.class, "LBL_Usage")
    };
    // Matches the attribute name used in org.netbeans.modules.hibernate.cfg.model.SessionFactory
    private static final String attrNames[] = new String[]{"Collection", "Region", "Usage"};
    private SessionFactory sessionFactory;

    public CollectionCachesTableModel(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Override
    public String getColumnName(int column) {
        return columnNames[column];
    }

    @Override
    public void setValueAt(Object value, int row, int column) {
    // TODO
    }

    public Object getValueAt(int row, int column) {

        if (sessionFactory == null) {
            return null;
        } else {
            String attrValue = sessionFactory.getAttributeValue(SessionFactory.COLLECTION_CACHE, row, attrNames[column]);
            return attrValue;
        }
    }

    public int getRowCount() {
        if (sessionFactory == null) {
            return 0;
        } else {
            return sessionFactory.sizeCollectionCache();
        }
    }

    public int getColumnCount() {
        return columnNames.length;
    }

    @Override
    public boolean isCellEditable(int row, int column) {
        return (false);
    }

    /**
     * Add a new collection cache element
     * 
     * @param values Contains the attribute vaues for the collection cache element. 
     *               The values must be in the order of collection, region, usage
     */
    public void addRow(String[] values) {
        
        int index = sessionFactory.addCollectionCache(true);
        for (int i = 0; i < values.length; i++) {
            if( values[i] != null && values[i].length() > 0)
                sessionFactory.setAttributeValue(SessionFactory.COLLECTION_CACHE, index, attrNames[i], values[i]);
        }

        fireTableRowsInserted(getRowCount() - 1, getRowCount() - 1);
    }
    
    /**
     * Modify an existing collection cache element
     * @param row The row to be modified
     * @param values Contains the attribute vaues for the collection cache element. 
     *               The values must be in the order of collection, region, usage
     */

    public void editRow(int row, String[] values) {
        for (int i = 0; i < values.length; i++) {
            sessionFactory.setAttributeValue(SessionFactory.COLLECTION_CACHE, row, attrNames[i], values[i]);
        }
        
        fireTableRowsUpdated(row, row);
    }

    public void removeRow(int row) {
        sessionFactory.removeCollectionCache(row);
        
        fireTableRowsDeleted(row, row);
    }
}
