/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development and
 * Distribution License("CDDL") (collectively, the "License"). You may not use
 * this file except in compliance with the License. You can obtain a copy of
 * the License at http://www.netbeans.org/cddl-gplv2.html or
 * nbbuild/licenses/CDDL-GPL-2-CP. See the License for the specific language
 * governing permissions and limitations under the License. When distributing
 * the software, include this License Header Notice in each file and include
 * the License file at nbbuild/licenses/CDDL-GPL-2-CP. Oracle designates this
 * particular file as subject to the "Classpath" exception as provided by
 * Oracle in the GPL Version 2 section of the License file that accompanied
 * this code. If applicable, add the following below the License Header, with
 * the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license." If you do not indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to its
 * licensees as provided above. However, if you add GPL Version 2 code and
 * therefore, elected the GPL Version 2 license, then the option applies only
 * if the new code is made subject to such option by the copyright holder.
 */
package org.netbeans.modules.java.api.common.project.ui.customizer.vmo;

import org.antlr.runtime.ANTLRStringStream;
import org.antlr.runtime.CommonTokenStream;
import org.netbeans.modules.java.api.common.project.ui.customizer.vmo.gen.CommandLineLexer;
import org.netbeans.modules.java.api.common.project.ui.customizer.vmo.gen.CommandLineParser;

import javax.swing.table.AbstractTableModel;
import java.util.*;

/**
 * @author Rastislav Komara
 * xxx: Wrong usage of generic types -> heap pollution.
 */
public class VMOptionsTableModel extends AbstractTableModel {
    private List<JavaVMOption<?>> rows = new ArrayList<JavaVMOption<?>>();
    private String[] columns = {"name", "value"}; //NOI18N
    private static final UserPropertyNode USER_PROPERTY_NODE = new UserPropertyNode() {
        @Override
        public void setName(String name) {
        }

        @Override
        public void setValue(OptionValue<Map.Entry<String, String>> value) {
        }

        @Override
        public boolean isValid() {
            return false;
        }
    };

    public int getRowCount() {
        return rows.size() + 1; //we are providing one additional row for user to insert custom -D properties.
    }

    public int getColumnCount() {
        return columns.length;
    }

    @Override
    public String getColumnName(int column) {
        if (0 <= column && column < columns.length) {
            return org.openide.util.NbBundle.getMessage(VMOptionsTableModel.class, "VMOptionTableModel." + columns[column] + ".text");  //NOI18N
        }
        throw new IllegalStateException("Column index out of range."); //NOI18N
    }

    public Object getValueAt(int rowIndex, int columnIndex) {
        if (rowOk(rowIndex)) {
            return rows.get(rowIndex);
        } else if (rowIndex == rows.size()) {
            return USER_PROPERTY_NODE;
        }
        throw new IllegalArgumentException("Row index out of range."); //NOI18N
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        setValue(rowIndex, (OptionValue<Object>) aValue);
    }

    public <T> void setValue(int row, OptionValue<T> value) {
        if (rowOk(row)) {
            JavaVMOption<OptionValue<T>> option = (JavaVMOption<OptionValue<T>>) rows.get(row);
            if (option == null) throw new IllegalStateException("The selected row contains null option  ."); //NOI18N
            option.setValue(value);
            fireTableRowsUpdated(row, row);
        } else if (row == rows.size()) {
            UserPropertyNode upn = new UserPropertyNode();
            upn.setValue((OptionValue<Map.Entry<String, String>>) value);
            rows.add(upn);
            fireTableRowsInserted(rows.size() - 2, rows.size() - 1);
        }

    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        if (columnIndex == 1) return true;
        if (rowIndex == rows.size()) return true;
        if (rowOk(rowIndex)) {
            JavaVMOption<?> option = rows.get(rowIndex);
            return option instanceof UserPropertyNode;
        }
        return false;
    }

    private boolean rowOk(int rowIndex) {
        return 0 <= rowIndex && rowIndex < rows.size();
    }


    public void fill(String str) throws Exception {
        rows.clear();
        final CommandLineLexer lexer = new CommandLineLexer(new ANTLRStringStream(str));
        final CommonTokenStream cts = new CommonTokenStream(lexer);
        final CommandLineParser parser = new CommandLineParser(cts);
        parser.setTreeAdaptor(new VMOptionTreeAdaptor());
        rows.addAll(parser.parse());
        Collections.sort(rows);
        fireTableDataChanged();
    }

    public List<JavaVMOption<?>> getValidOptions() {
        List<JavaVMOption<?>> result = new LinkedList<JavaVMOption<?>>();
        for (JavaVMOption<?> row : rows) {
            final OptionValue<?> value = row.getValue();
            if (value != null && value.isPresent()) {
                result.add(row);
            }
        }
        return result;
    }
}
