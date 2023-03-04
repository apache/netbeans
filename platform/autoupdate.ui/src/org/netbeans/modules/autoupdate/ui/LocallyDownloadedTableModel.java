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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import javax.swing.table.JTableHeader;
import org.netbeans.api.autoupdate.InstallSupport;
import org.netbeans.api.autoupdate.OperationContainer;
import org.netbeans.api.autoupdate.UpdateUnit;
import org.openide.util.NbBundle;

/**
 *
 * @author Jiri Rechtacek, Radek Matous
 */
public class LocallyDownloadedTableModel extends UnitCategoryTableModel {
    private OperationContainer<InstallSupport> availableNbmsContainer = Containers.forAvailableNbms();
    private OperationContainer<InstallSupport> updateNbmsContainer = Containers.forUpdateNbms();
    private LocalDownloadSupport localDownloadSupport = null;
    List<UpdateUnit> cachedUnits;
        
    /** Creates a new instance of InstalledTableModel */
    public LocallyDownloadedTableModel (LocalDownloadSupport localDownloadSupport) {        
        this.localDownloadSupport = localDownloadSupport;
    }
    
    @Override
    public final void setUnits(final List<UpdateUnit> unused) {
        getLocalDownloadSupport ().removeInstalledUnit ();
        Collection<UpdateUnit> units = getLocalDownloadSupport().getUpdateUnits();
        //do not compute if not necessary
        if (cachedUnits == null || !units.containsAll (cachedUnits) || !cachedUnits.containsAll (units)) {
            setData(makeCategories (new LinkedList<UpdateUnit> (units)));
            cachedUnits = new ArrayList<UpdateUnit>(units);
        }
    }
    
    void removeInstalledUnits () {
        getLocalDownloadSupport ().removeInstalledUnit ();
        cachedUnits = null;
        setUnits (null);
    }
            
    private List<UnitCategory> makeCategories(List<UpdateUnit> units) {
        final List<UnitCategory> categories = new ArrayList<UnitCategory>();        
        categories.addAll(Utilities.makeAvailableCategories(units, true));
        categories.addAll(Utilities.makeUpdateCategories(units, true));
        return categories;
    }
    
    LocalDownloadSupport getLocalDownloadSupport() {
        return localDownloadSupport;
    }
        
    @Override
    public void setValueAt(Object anValue, int row, int col) {
        super.setValueAt(anValue, row, col);
        if (anValue == null) {
            return ;
        }
        if (! (anValue instanceof Boolean)) {
            return ;
        }
        Unit u = getUnitAtRow(row);
        if (u != null) {
            assert anValue instanceof Boolean : anValue + " must be instanceof Boolean.";
            boolean beforeMarked = u.isMarked();
            u.setMarked(!beforeMarked);
            if (u.isMarked() != beforeMarked) {
                fireButtonsChange();
                if (u.isMarked ()) {
                   getLocalDownloadSupport ().checkUnit (u.updateUnit);
                } else {
                   getLocalDownloadSupport ().uncheckUnit (u.updateUnit); 
                }
            } else {
                assert false : u.getDisplayName();
            }
        }
    }

    
    @Override
    public Object getValueAt(int row, int col) {
        Object res = null;
        Unit u = getUnitAtRow(row);
        if (u != null) {
            boolean isAvailable = (u instanceof Unit.Available);
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
                if (isAvailable) {
                    res = ((Unit.Available)u).getAvailableVersion();
                } else {
                    res = ((Unit.Update)u).getAvailableVersion();
                }
                break;
            case 4 :
                if (isAvailable) {
                    res = ((Unit.Available)u).getSize();
                } else {
                    res = ((Unit.Update)u).getSize();
                }
                break;
            }
        }
        
        return res;
    }
    
    @Override
    public int getColumnCount() {
        return 2;
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
            res = String.class;
            break;
        case 4 :
            res = String.class;
            break;
        }
        
        return res;
    }

    @Override
    public String getColumnName(int column) {
        switch (column) {
            case 0 :
                return getBundle ("LocallyDownloadedTableModel_Columns_Install");
            case 1 :
                return getBundle ("LocallyDownloadedTableModel_Columns_Name");
            case 2 :
                return getBundle ("InstalledTableModel_Columns_Category");                                
            case 3 :
                return getBundle ("LocallyDownloadedTableModel_Columns_Version");
            case 4 :
                return getBundle ("LocallyDownloadedTableModel_Columns_Size");
        }
        
        assert false;
        return super.getColumnName( column );
    }

    @Override
    public int getPreferredWidth(JTableHeader header, int col) {
        switch (col) {
                case 1:
            return super.getMinWidth(header, col)*4;
        case 2:
            return super.getMinWidth(header, col)*2;
        }
        return super.getMinWidth(header, col);
    }
    
    @Override
    public Type getType () {
        return UnitCategoryTableModel.Type.LOCAL;
    }
    
    public class DisplayName {
        public DisplayName (String name) {
            
        }
    }
    @Override
    public boolean isSortAllowed(Object columnIdentifier) {
        boolean isInstall = getColumnName(0).equals(columnIdentifier);
        boolean isSize = getColumnName(4).equals(columnIdentifier);                        
        return isInstall || isSize ? false : true;
    }

    @Override
    protected Comparator<Unit> getDefaultComparator () {
        return new Comparator<Unit> ()  {
            @Override
            public int compare (Unit o1, Unit o2) {
                return Unit.compareDisplayNames(o1, o2);
            }           
        };
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
                    return Unit.compareDisplayVersions(unit1, unit2);
                } else if (getColumnName(4).equals(columnIdentifier)) {
                    assert false : columnIdentifier.toString();
                }                
                return 0;
            }
        };
    }

    @Override
    public int getDownloadSize () {
        // no need to download anything in Locally Downloaded tab
        return 0;
    }
    
    private String getBundle (String key) {
        return NbBundle.getMessage (this.getClass (), key);
    }

    @Override
    public String getTabTitle() {
        return NbBundle.getMessage (PluginManagerUI.class, "PluginManagerUI_UnitTab_Local_Title");//NOI18N
    }

    @Override
    public int getTabIndex() {
        return PluginManagerUI.INDEX_OF_DOWNLOAD_TAB;
    }
    
    @Override
    public boolean canBePrimaryTab() {
        return false;
    }

    @Override
    public boolean needsRestart () {
        return false;
    }
}
