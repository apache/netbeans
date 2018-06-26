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
/**
 * MappingTable.java
 *
 * @author Ana von Klopp
 */
package org.netbeans.modules.web.wizards;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.util.Iterator;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
import javax.swing.table.AbstractTableModel;
import javax.swing.event.TableModelListener;
import org.openide.util.NbBundle;

class MappingTable extends JTable {

    // Handle resizing for larger fonts
    private boolean fontChanged = true;
    private int margin = 6;
    private static final long serialVersionUID = 3482048644419079279L;

    MappingTable(String filterName, List<FilterMappingData> filterMappings) {
        super();
        this.setModel(new MappingTableModel(filterName, filterMappings));

        TableColumnModel tcm = this.getColumnModel();

        // The filter name - this one is never editable
        tcm.getColumn(0).setPreferredWidth(72);

        // The pattern or servlet that we match to
        // This editor depends on whether the value of the other is
        // URL or Servlet
        tcm.getColumn(1).setPreferredWidth(72);
        this.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        //setColors(false);
        setIntercellSpacing(new Dimension(margin, margin));
    }

    List<FilterMappingData> getFilterMappings() {
        return ((MappingTableModel) this.getModel()).getFilterMappings();
    }

    void setFilterName(String name) {
        ((MappingTableModel) this.getModel()).setFilterName(name);
        this.invalidate();
    }

    void addRow(FilterMappingData fmd) {
        this.invalidate();
        ((MappingTableModel) getModel()).addRow(fmd);
    }

    void addRow(int row, FilterMappingData fmd) {
        this.invalidate();
        ((MappingTableModel) getModel()).addRow(row, fmd);
    }

    void setRow(int row, FilterMappingData fmd) {
        this.invalidate();
        ((MappingTableModel) getModel()).setRow(row, fmd);
    }

    FilterMappingData getRow(int row) {
        return ((MappingTableModel) getModel()).getRow(row);
    }

    void moveRowUp(int row) {
        ((MappingTableModel) getModel()).moveRowUp(row);
        getSelectionModel().setSelectionInterval(row - 1, row - 1);
        this.invalidate();
    }

    void moveRowDown(int row) {
        ((MappingTableModel) getModel()).moveRowUp(row + 1);
        getSelectionModel().setSelectionInterval(row + 1, row + 1);
        this.invalidate();
    }

    void removeRow(int row) {
        ((MappingTableModel) getModel()).removeRow(row);
        this.invalidate();
        return;
    }

    @Override
    public void setValueAt(Object o, int row, int col) {
        return;
    }

    private void setColors(boolean editable) {
        Color bg;
        this.setBorder(BorderFactory.createLoweredBevelBorder());
        if (!editable) {
            bg = this.getBackground().darker();
        } else {
            bg = Color.white;
        }
        this.setBackground(bg);
    }

    void addTableModelListener(TableModelListener tml) {
        TableModel tableModel = getModel();
        if (tableModel != null) {
            tableModel.addTableModelListener(tml);
        }
    }

    void removeTableModelListener(TableModelListener tml) {
        TableModel tableModel = getModel();
        if (tableModel != null) {
            tableModel.removeTableModelListener(tml);
        }
    }

    @Override
    public void setFont(Font f) {
        fontChanged = true;
        super.setFont(f);
    }

    /** 
     * When paint is first invoked, we set the rowheight based on the
     * size of the font. */
    @Override
    public void paint(Graphics g) {
        if (fontChanged) {
            fontChanged = false;

            int height = 0;
            FontMetrics fm = g.getFontMetrics(getFont());
            height = fm.getHeight() + margin;
            if (height > rowHeight) {
                rowHeight = height;
            }
            //triggers paint, just return afterwards
            this.setRowHeight(rowHeight);
            return;
        }
        super.paint(g);
    }

    private static class MappingTableModel extends AbstractTableModel {

        private final String[] colheaders = {
            NbBundle.getMessage(MappingTable.class, "LBL_filter_name"),
            NbBundle.getMessage(MappingTable.class, "LBL_applies_to"),};
        private List<FilterMappingData> filterMappings = null;
        private String filterName;
        private static final long serialVersionUID = 2845252365404044474L;

        MappingTableModel(String filterName, List<FilterMappingData> filterMappings) {
            this.filterName = filterName;
            this.filterMappings = filterMappings;
        }

        List<FilterMappingData> getFilterMappings() {
            return filterMappings;
        }

        void setFilterName(String name) {
            Iterator<FilterMappingData> i = filterMappings.iterator();
            FilterMappingData fmd;
            while (i.hasNext()) {
                fmd = i.next();
                if (fmd.getName().equals(filterName)) {
                    fmd.setName(name);
                }
            }
            this.filterName = name;
        }

        public int getColumnCount() {
            return colheaders.length;
        }

        public int getRowCount() {
            return filterMappings.size();
        }

        @Override
        public String getColumnName(int col) {
            return colheaders[col];
        }

        public Object getValueAt(int row, int col) {
            FilterMappingData fmd = filterMappings.get(row);
            if (col == 0) {
                return fmd.getName();
            } else {
                return fmd.getPattern();
            }
        }

        @Override
        public Class getColumnClass(int c) {
            return String.class;
        }

        @Override
        public boolean isCellEditable(int row, int col) {
            return false;
        }

        @Override
        public void setValueAt(Object value, int row, int col) {
            return;
        }

        void addRow(int row, FilterMappingData fmd) {
            filterMappings.add(row, fmd);
        }

        void addRow(FilterMappingData fmd) {
            filterMappings.add(fmd);
        }

        FilterMappingData getRow(int row) {
            return filterMappings.get(row);
        }

        void setRow(int row, FilterMappingData fmd) {
            filterMappings.set(row, fmd);
        }

        void moveRowUp(int row) {
            FilterMappingData o = filterMappings.remove(row);
            filterMappings.add(row - 1, o);
        }

        void removeRow(int row) {
            filterMappings.remove(row);
        }
    } // MappingTableModel
} // MappingTable
