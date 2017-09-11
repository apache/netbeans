/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.db.dataview.table.celleditor;

import java.awt.Component;
import javax.swing.BorderFactory;
import javax.swing.InputVerifier;
import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.TableModel;
import javax.swing.text.JTextComponent;
import org.netbeans.modules.db.dataview.meta.DBColumn;
import org.netbeans.modules.db.dataview.meta.DBException;
import org.netbeans.modules.db.dataview.table.ResultSetTableCellEditor;
import org.netbeans.modules.db.dataview.table.ResultSetTableModel;
import org.netbeans.modules.db.dataview.util.DBReadWriteHelper;
import org.openide.util.Exceptions;

public class NumberFieldEditor extends ResultSetTableCellEditor {
    private final JTextField textField;
    private DBColumn dbColumn;
    private Object oldValue;
    private final InputVerifier verifier = new InputVerifier() {
        @Override
        public boolean verify(JComponent input) {
            if (dbColumn != null && input instanceof JTextComponent) {
                String inputText = ((JTextComponent) input).getText();
                try {
                    DBReadWriteHelper.validate(inputText, dbColumn);
                } catch (DBException ex) {
                    return false;
                }
                return true;
            } else {
                return true;
            }
        }
    };

    public NumberFieldEditor(final JTextField textField) {
        super(textField);
        this.textField = textField;
        ((JTextField) getComponent()).setHorizontalAlignment(JTextField.RIGHT);
    }

    @Override
    public Component getTableCellEditorComponent(final JTable table, Object value, boolean isSelected, int row, int column) {
        oldValue = value;
        int modelColumn = table.convertColumnIndexToModel(column);
        TableModel tm = table.getModel();
        dbColumn = null;
        if (tm instanceof ResultSetTableModel) {
            textField.setInputVerifier(verifier);
            dbColumn = ((ResultSetTableModel) tm).getColumn(modelColumn);
        } else {
            textField.setInputVerifier(null);
        }
        Component c = super.getTableCellEditorComponent(table, value, isSelected, row, column);
        if (suppressEditorBorder && c instanceof JComponent) {
            ((JComponent) c).setBorder(BorderFactory.createEmptyBorder());
        }
        return c;
    }

    /**
     * Override getCellEditorValue to build a number.
     */
    @Override
    public Object getCellEditorValue() {
        try {
            Object superVal = super.getCellEditorValue();
            if (dbColumn != null) {
                try {
                    return DBReadWriteHelper.validate(superVal, dbColumn);
                } catch (DBException ex) {
                    Exceptions.printStackTrace(ex);
                    return oldValue;
                }
            } else {
                return superVal;
            }
        } finally {
            oldValue = null;
        }
    }

    @Override
    public boolean stopCellEditing() {
        try {
            Object value = super.getCellEditorValue();
            DBReadWriteHelper.validate(value, dbColumn);
            return super.stopCellEditing();
        } catch (DBException ex) {
            return false;
        }
    }

    @Override
    public void cancelCellEditing() {
        oldValue = null;
        super.cancelCellEditing();
    }
}
