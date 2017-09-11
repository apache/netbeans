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

package org.netbeans.modules.viewmodel;

import java.beans.PropertyEditor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import javax.swing.table.TableCellEditor;
import org.netbeans.spi.viewmodel.ColumnModel;
import org.openide.awt.Actions;
import org.openide.awt.Mnemonics;
import org.openide.nodes.PropertySupport;

/**
 *
 * @author   Jan Jancura
 */
public class Column extends PropertySupport.ReadWrite {

    public static final String PROP_ORDER_NUMBER = "OrderNumberOutline"; // NOI18N

    private PropertyEditor propertyEditor;
    private ColumnModel columnModel;

    Column (
        ColumnModel columnModel
    ) {
        super (
            columnModel.getID (),
            columnModel.getType () == null ? 
                String.class : 
                columnModel.getType (),
            Actions.cutAmpersand(columnModel.getDisplayName ()),
            columnModel.getShortDescription ()
        );
        this.columnModel = columnModel;
        setValue (
            "SortableColumn",
            Boolean.valueOf (columnModel.isSortable ())
        );
        if (columnModel.getType () == null)
            // Default column!
            setValue (
                "TreeColumnTTV", 
                Boolean.TRUE
            );
        if (Mnemonics.findMnemonicAmpersand(columnModel.getDisplayName()) >= 0) {
            setValue("ColumnDisplayNameWithMnemonicTTV", columnModel.getDisplayName ()); // NOI18N
        }
        Character mnemonic = columnModel.getDisplayedMnemonic();
        if (mnemonic != null) {
            setValue("ColumnMnemonicCharTTV", mnemonic); // NOI18N
        }
        this.propertyEditor = columnModel.getPropertyEditor ();
    }

    int getColumnWidth () {
        return columnModel.getColumnWidth ();
    }
    
    void setColumnWidth (int width) {
        columnModel.setColumnWidth (width);
    }
    
    int getOrderNumber () {
        Object o = getValue ("OrderNumberTTV");
        if (o == null) return -1;
        return ((Integer) o).intValue ();
    }
    
    int getModelOrderNumber() {
        return columnModel.getCurrentOrderNumber();
    }
    
    boolean isDefault () {
        return columnModel.getType () == null;
    }

    @Override
    public boolean isHidden() {
        return !columnModel.isVisible();
    }

    @Override
    public void setHidden(boolean hidden) {
        columnModel.setVisible(!hidden);
    }

    public Object getValue () {
        return null;
    }
    
    public void setValue (Object obj) {
    }

    public Object getValue (String propertyName) {
        if (PROP_ORDER_NUMBER.equals (propertyName)) {
            int index = columnModel.getCurrentOrderNumber();
            return new Integer(index);
        }
        if ("InvisibleInTreeTableView".equals (propertyName)) 
            return Boolean.valueOf (!columnModel.isVisible ());
        if ("SortableColumn".equals (propertyName)) {
            return Boolean.valueOf (columnModel.isSortable());
        }
        if ("DescendingOrderTTV".equals (propertyName)) 
            return Boolean.valueOf (columnModel.isSortedDescending ());
        return super.getValue (propertyName);
    }
    
    public void setValue (String propertyName, Object newValue) {
        if (PROP_ORDER_NUMBER.equals (propertyName)) {
            int index = ((Integer) newValue).intValue();
            columnModel.setCurrentOrderNumber(index);
        } else
        /*if ("SortableColumn".equals (propertyName))
            columnModel.setSorted (
                ((Boolean) newValue).booleanValue ()
            );
        else*/
        if ("DescendingOrderTTV".equals (propertyName)) 
            columnModel.setSortedDescending (
                ((Boolean) newValue).booleanValue ()
            );
        else
        super.setValue (propertyName, newValue);
    }

    boolean isSorted() {
        return columnModel.isSorted();
    }

    boolean isSortedDescending() {
        return columnModel.isSortedDescending();
    }

    void setSorted(boolean sorted) {
        columnModel.setSorted(sorted);
    }

    void setSortedDescending(boolean descending) {
        columnModel.setSortedDescending(descending);
    }

    public PropertyEditor getPropertyEditor () {
        return propertyEditor;
    }

    TableCellEditor getTableCellEditor () {
        try {
            // [TODO] get rid off reflection after ColumnModel API is extended by getTableCellEditor() method
            Method method = columnModel.getClass().getMethod("getTableCellEditor");
            if (!TableCellEditor.class.isAssignableFrom(method.getReturnType())) {
                return null;
            }
            return (TableCellEditor) method.invoke(columnModel);
        } catch (IllegalAccessException ex) {
        } catch (IllegalArgumentException ex) {
        } catch (InvocationTargetException ex) {
        } catch (NoSuchMethodException ex) {
        } catch (SecurityException ex) {
        }
        return null;
    }

    @Override
    public String toString() {
        return super.toString() + " with ColumnModel "+columnModel;
    }
    
}

