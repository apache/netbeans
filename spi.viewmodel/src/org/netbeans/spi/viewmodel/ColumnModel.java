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

package org.netbeans.spi.viewmodel;

import java.beans.PropertyEditor;


/**
 * Defines model for one table view column. Can be used together with
 * {@link TreeModel} for tree table view representation.
 *
 * @author   Jan Jancura
 */
public abstract class ColumnModel implements Model {


    /**
     * Returns unique ID of this column.
     *
     * @return unique ID of this column
     */
    public abstract String getID ();

    /**
     * Returns display name of this column. The returned String can contain an
     * ampersand marking the location of the mnemonic.
     *
     * @return display name of this column, including an optional ampersand for mnemonic location.
     */
    public abstract String getDisplayName ();
    
    /** 
     * Returns the character, that indicates a mnemonic key
     * for this column name. Can be <code>null</code>.
     *
     * @return the mnemonic key or <code>null</code>.
     * @since 1.11
     * @deprecated Use ampersand in {@link #getDisplayName()}.
     */
    public Character getDisplayedMnemonic() {
        return null;
    }
    
    /**
     * Returns type of column items.
     *
     * @return type of column items
     */
    public abstract Class getType ();
    
    /**
     * Returns ID of column this column should be installed before or 
     * <code>null</code>. Defines default order of columns only. 
     * This default order can be changed by user, and 
     * {@link #getCurrentOrderNumber} and {@link #setCurrentOrderNumber} are
     * used for sorting after that.
     *
     * @return ID of column this column should be installed before or 
     * <code>null</code>
     * @deprecated Not used. See {@link #getCurrentOrderNumber}.
     */
    public String getPreviuosColumnID () {
        return null;
    }
    
    /**
     * Returns ID of column this column should be installed after or 
     * <code>null</code>. Defines default order of columns only. 
     * This default order can be changed by user, and 
     * {@link #getCurrentOrderNumber} and {@link #setCurrentOrderNumber} are
     * used for sorting after that.
     *
     * @return ID of column next to this one or <code>null</code>
     * @deprecated Not used. See {@link #getCurrentOrderNumber}.
     */
    public String getNextColumnID () {
        return null;
    }
    
    /**
     * Returns tooltip for given column. Default implementation returns 
     * <code>null</code> - do not use tooltip.
     *
     * @return  tooltip for given node or <code>null</code>
     */
    public String getShortDescription () {
        return null;
    }
    
    /**
     * True if column can be sorted. Default implementation returns 
     * <code>true</code>.
     *
     * @return true if column can be sorted
     */
    public boolean isSortable () {
        return true;
    }
    
    /**
     * True if column should be visible. Default implementation 
     * returns <code>true</code>.
     *
     * @return <code>true</code> if column should be visible
     */
    public boolean isVisible () {
        return true;
    }
    
    /**
     * Set true if column is to be visible. Default implementation does nothing.
     *
     * @param visible set true if column is to be visible
     */
    public void setVisible (boolean visible) {}
    
    /**
     * True if column is sorted.
     * Default implementation returns <code>false</code>.
     *
     * @return <code>true</code> if column is sorted.
     */
    public boolean isSorted () {
        return false;
    }
    
    /**
     * Set true if column is to be sorted. Default implementation does nothing.
     *
     * @param sorted set true if column is to be sorted
     */
    public void setSorted (boolean sorted) {}
    
    /**
     * True if column should be sorted in descending order.
     * Default implementation returns <code>false</code>.
     *
     * @return <code>true</code> if column should be sorted
     *         in descending order
     */
    public boolean isSortedDescending () {
        return false;
    }
    
    /**
     * Set true if column is to be sorted in descending order.
     * Default implementation does nothing.
     *
     * @param sortedDescending set true if column is to be sorted
     *        in descending order
     */
    public void setSortedDescending (boolean sortedDescending) {}
    
    /**
     * Should return current order number of this column. Default value is 
     * <code>-1</code>.
     *
     * @return current order number of this column or <code>-1</code>
     */
    public int getCurrentOrderNumber () {
        return -1;
    }
    
    /**
     * Is called when current order number of this column is changed.
     * Default implementation does nothing.
     *
     * @param newOrderNumber new order number
     */
    public void setCurrentOrderNumber (int newOrderNumber) {}
    
    /**
     * Return column width of this column.
     *
     * @return column width of this column
     */
    public int getColumnWidth () {
        return 20;
    }
    
    /**
     * Is called when column width of this column is changed.
     * Default implementation does nothing.
     *
     * @param newColumnWidth a new column width
     */
    public void setColumnWidth (int newColumnWidth) {}
    
    /**
     * Returns {@link java.beans.PropertyEditor} to be used for 
     * this column. Default implementation returns <code>null</code> - 
     * means use default PropertyEditor.
     *
     * @return {@link java.beans.PropertyEditor} to be used for 
     *         this column
     */
    public PropertyEditor getPropertyEditor () {
        return null;
    }
    
    /**
     * Rerturns {@link javax.swing.table.TableCellEditor} to be used for 
     * this column.
     *
     * @return {@link javax.swing.table.TableCellEditor} to be used for 
     *         this column
     */
//    public TableCellEditor getTableCellEditor () {
//        return null;
//    }
    
    /**
     * Rerturns {@link javax.swing.table.TableCellRenderer} to be used for 
     * this column.
     *
     * @return {@link javax.swing.table.TableCellRenderer} to be used for 
     *         this column
     */
//    public TableCellRenderer getTableCellRenderer () {
//        return null;
//    }
    
}
