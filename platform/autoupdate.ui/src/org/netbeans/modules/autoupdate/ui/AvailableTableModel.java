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

package org.netbeans.modules.autoupdate.ui;

import java.awt.Image;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.table.JTableHeader;
import org.netbeans.api.autoupdate.OperationContainer;
import org.netbeans.api.autoupdate.OperationContainer.OperationInfo;
import org.netbeans.api.autoupdate.UpdateElement;
import org.netbeans.api.autoupdate.UpdateUnit;
import static org.netbeans.modules.autoupdate.ui.Bundle.*;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;

/**
 *
 * @author Jiri Rechtacek, Radek Matous
 */
public class AvailableTableModel extends UnitCategoryTableModel {
    //just prevents from gc, do not delete
    private OperationContainer container = Containers.forAvailable();
    private OperationContainer containerCustom = Containers.forCustomInstall ();
    private static String col0, col1, col2, col3;
    
    /** Creates a new instance of AvailableTableModel
     * @param units 
     */
    public AvailableTableModel (List<UpdateUnit> units) {
        setUnits(units);
    }
    
    @Override
    public final void setUnits (List<UpdateUnit> units) {
        setData(Utilities.makeAvailableCategories (units, false));
    }
    
    @Override
    @Messages({
        "# {0} - file path",
        "unit_already_installed=Plugin {0} is already installed."})
    public void setValueAt(Object anValue, int row, int col) {
        // second column is editable but doesn't want to edit its value
        if (isExpansionControlAtRow(row)) return;//NOI18N        
        if (col == 1) {
            return ;
        }
        super.setValueAt(anValue, row, col);
        if (anValue == null) {
            return ;
        }
        //assert getCategoryAtRow (row).isExpanded ();
        Unit.Available u = (Unit.Available) getUnitAtRow(row);
        assert anValue instanceof Boolean : anValue + " must be instanceof Boolean.";
        if (u == null) {
            return ;
        }
        boolean beforeMarked = u.isMarked();
        if ((Boolean) anValue != beforeMarked) {
            u.setMarked(! beforeMarked);
            if (u.isMarked() != beforeMarked) {
                fireButtonsChange();
            } else {
                if (u.getRelevantElement().getUpdateUnit().getInstalled() != null) {
                    // already installed => refresh model
                    fireUpdataUnitChange();
                    DialogDisplayer.getDefault().notifyLater(
                            new NotifyDescriptor.Message(unit_already_installed(u.getDisplayName()), NotifyDescriptor.WARNING_MESSAGE));
                } else {
                    //TODO: message should contain spec.version
                    String message = getBundle ("NotificationAlreadyPreparedToIntsall", u.getDisplayName ()); // NOI18N
                    DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(message));
                }
            }
        }
    }

    @Override
    public Object getValueAt(int row, int col) {
        Object res = null;
        if (isExpansionControlAtRow(row)) return "";//NOI18N
        
        Unit.Available u = (Unit.Available) getUnitAtRow(row);
        if (u == null) {
            return null;
        }
        switch (col) {
        case 0 :
            res = u.isMarked() ? Boolean.TRUE : Boolean.FALSE;
            break;
        case 1 :
            res = u.getDisplayName();
            break;
        case 2 :
            res = u.getCategoryName();
            break;
        case 3 :
            res = u.getSourceIcon();
            break;
        }
        
        return res;
    }

    @Override
    public int getColumnCount() {
        return 4;
    }
    
    @Override
    public Class getColumnClass(int c) {
        Class res = null;
        
        switch (c) {
        case 0 :
            res = Boolean.class;
            break;
        case 1 :
            res = String.class;
            break;
        case 2 :
            res = String.class;
            break;
        case 3 :
            res = Image.class;
            break;
        }
        
        return res;
    }

    @Override
    public String getColumnName(int column) {
        switch (column) {
            case 0 :
                if (col0 == null) {
                    col0 = getBundle ("AvailableTableModel_Columns_Install");
                }
                return col0;
            case 1 :
                if (col1 == null) {
                    col1 = getBundle ("AvailableTableModel_Columns_Name");
                }
                return col1;
            case 2 :
                if (col2 == null) {
                    col2 = getBundle("AvailableTableModel_Columns_Category");
                }
                return col2;
            case 3 :
                if (col3 == null) {
                    col3 = getBundle ("AvailableTableModel_Source_Category");
                }
                return col3;
        }
        
        assert false;
        return super.getColumnName( column );
    }

    @Override
    public String getToolTipText(int row, int col) {
        if (col == 3) {
            Unit.Available u = (Unit.Available) getUnitAtRow(row);
            return u.getSourceDescription();
        }
        return super.getToolTipText(row, col);
    }

    @Override
    public int getPreferredWidth(JTableHeader header, int col) {
        final int minWidth = super.getMinWidth(header, col);
        switch (col) {
        case 1:
            return minWidth*4;
        case 2:
            return minWidth*2;
        }
        return minWidth;
    }
    
    @Override
    public Type getType () {
        return UnitCategoryTableModel.Type.AVAILABLE;
    }
    
    @Override
    public boolean isSortAllowed(Object columnIdentifier) {
        boolean isInstall = getColumnName(0).equals(columnIdentifier);
        return isInstall  ? false : true;
    }

    @Override
    protected Comparator<Unit> getComparator(final Object columnIdentifier, final boolean sortAscending) {
        return new Comparator<Unit>(){
            @Override
            public int compare(Unit o1, Unit o2) {
                Unit unit1 = sortAscending ? o1 : o2;
                Unit unit2 = sortAscending ? o2 : o1;
                if (getColumnName(0).equals(columnIdentifier)) {
                    assert false : columnIdentifier.toString();
                } else if (getColumnName(1).equals(columnIdentifier)) {
                    return Unit.compareDisplayNames(unit1, unit2);
                } else if (getColumnName(2).equals(columnIdentifier)) {
                    return Unit.compareCategories(unit1, unit2);
                } else if (getColumnName(3).equals(columnIdentifier)) {
                    return Unit.Available.compareSourceCategories(unit1, unit2);
                }                
                return 0;
            }
        };
    }

    @SuppressWarnings ("unchecked")
    @Override
    public int getDownloadSize () {
        int res = 0;
        assert container != null || containerCustom != null: "OperationContainer found when asking for download size.";
        Set<OperationInfo> infos = new HashSet<OperationInfo> ();
        infos.addAll (container.listAll ());
        infos.addAll (containerCustom.listAll ());
        Set<UpdateElement> elements = new HashSet<UpdateElement> ();
        for (OperationInfo info : infos) {
            elements.add (info.getUpdateElement ());
            elements.addAll (info.getRequiredElements ());
        }
        for (UpdateElement el : elements) {
            res += el.getDownloadSize ();
        }
        return res;
    }
        
    private String getBundle (String key, Object... params) {
        return NbBundle.getMessage (this.getClass (), key, params);
    }

    @Override
    public String getTabTooltipText() {
        if (isTabEnabled()) {
            return super.getTabTooltipText(); 
        }
        return NbBundle.getMessage(PluginManagerUI.class, "PluginManagerUI_UnitTab_Available_ToolTip");
    }
    
    @Override
    public String getTabTitle() {
        return NbBundle.getMessage (PluginManagerUI.class, "PluginManagerUI_UnitTab_Available_Title");//NOI18N
    }

    @Override
    public int getTabIndex() {
        return PluginManagerUI.INDEX_OF_AVAILABLE_TAB;
    }

    @Override
    public boolean isTabEnabled() {
        return true;//getRawItemCount() > 0;
    }

    @Override
    public boolean needsRestart () {
        return false;
    }
}
