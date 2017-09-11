/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2013 Oracle and/or its affiliates. All rights reserved.
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2008 Sun
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

package org.netbeans.modules.autoupdate.ui;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.SwingUtilities;
import javax.swing.table.AbstractTableModel;
import org.netbeans.api.autoupdate.UpdateUnitProvider;
import org.netbeans.api.autoupdate.UpdateUnitProviderFactory;
import org.openide.util.NbBundle;

/**
 *
 * @author Radek Matous
 */
public class SettingsTableModel extends AbstractTableModel {
    private static final String[] COLUMN_NAME_KEYS = new String[] {
        "SettingsTable_ActiveColumn",
        "SettingsTable_NameColumn",
        /*"SettingsTable_URLColumn"*/
    };
    
    private static final Class[] COLUMN_TYPES = new Class[] {
        Boolean.class,
        UpdateUnitProvider.class,
        /*String.class*/
    };
    private List<UpdateUnitProvider> updateProviders;
    private Set<String> originalProviders;
    private SettingsTab settingsTab = null;
    
    /** Creates a new instance of SettingsTableModel */
    public SettingsTableModel () {
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                refreshModel();
            }
        });
    }
    
    void setSettingsTab (SettingsTab settingsTab) {
        this.settingsTab = settingsTab;
    }
    
    SettingsTab getSettingsTab () {
        return settingsTab;
    }
        
    void refreshModel () {
        Set<String> oldValue = originalProviders;
        Set<String> newValue = new HashSet<String> ();
        final List<UpdateUnitProvider> forRefresh = new ArrayList<UpdateUnitProvider> ();
        List<UpdateUnitProvider> providers = UpdateUnitProviderFactory.getDefault ().getUpdateUnitProviders (false);
        for (UpdateUnitProvider p : providers) {
            if (oldValue != null && !oldValue.contains (p.getName ())) {
                // new one provider
                if (p.isEnabled ()) {
                    forRefresh.add (p);
                }
            }
            newValue.add (p.getName ());
        }
        if (! forRefresh.isEmpty ()) {
            getSettingsTab ().setWaitingState (true);
            Utilities.startAsWorkerThread (new Runnable () {
                @Override
                public void run () {
                    try {
                        Utilities.presentRefreshProviders (forRefresh, getSettingsTab ().getPluginManager (), true);
                        getSettingsTab ().getPluginManager ().updateUnitsChanged ();
                    } finally {
                        getSettingsTab ().setWaitingState (false);
                    }
                }
            });
        }
        // check removed providers
        if (oldValue != null && ! oldValue.isEmpty () && ! newValue.containsAll (oldValue)) {
            getSettingsTab ().setNeedRefresh ();
        }
        updateProviders = new ArrayList<UpdateUnitProvider> ();
        for (UpdateUnitProvider p : providers) {
            if (p.getDisplayName() != null) {
                updateProviders.add(p);
            }
        }
        originalProviders = newValue;
        sortAlphabetically (updateProviders);
        fireTableDataChanged ();
    }
    
    public void remove (int rowIndex) {
        UpdateUnitProvider unitProvider = getUpdateUnitProvider (rowIndex);
        if (unitProvider != null) {
            UpdateUnitProviderFactory.getDefault ().remove (unitProvider);
        }
        getSettingsTab ().setNeedRefresh ();
    }
    
    public void add (String name, String displayName, URL url, boolean state) {
        final UpdateUnitProvider uup = UpdateUnitProviderFactory.getDefault ().create (name, displayName, url);
        uup.setEnable (state);
    }
    
    public UpdateUnitProvider getUpdateUnitProvider (int rowIndex) {
        return (rowIndex >= 0 && rowIndex <  updateProviders.size ()) ? updateProviders.get (rowIndex) : null;
    }
    
    @Override
    public boolean isCellEditable (int rowIndex, int columnIndex) {
        return columnIndex == 0;
    }
    
    @Override
    public int getRowCount () {
        return updateProviders == null ? 0 : updateProviders.size ();
    }
    
    @Override
    public int getColumnCount () {
        return COLUMN_NAME_KEYS.length;
    }
    
    @Override
    public void setValueAt (Object aValue, int rowIndex, int columnIndex) {
        final UpdateUnitProvider unitProvider = getUpdateUnitProvider (rowIndex);
        switch(columnIndex) {
        case 0:
            boolean oldValue = unitProvider.isEnabled ();
            boolean newValue = ((Boolean) aValue).booleanValue ();
            if (oldValue != newValue) {
                unitProvider.setEnable (newValue);
                if (newValue) {
                    // was not enabled and will be -> add it to model and read its content
                    getSettingsTab ().refreshProvider (unitProvider, false);
                } else {
                    // was enabled -> remove from model and refresh
                    // getSettingsTab ().setNeedRefresh ();
                    getSettingsTab ().refreshProvider (unitProvider, false);
                }
            }
            break;
        }
    }
    
    @Override
    public Object getValueAt (int rowIndex, int columnIndex) {
        Object retval = null;
        UpdateUnitProvider unitProvider = updateProviders.get (rowIndex);
        if (unitProvider == null) {
            return null;
        }
        switch(columnIndex) {
        case 0: retval = unitProvider.isEnabled ();break;
        case 1: retval = unitProvider;break;
            /*case 2: URL url = unitProvider.getProviderURL();
            retval = (url != null) ? url.toExternalForm() : "";//NOI18N
            break;*/
        }
        return retval;
    }
    
    @Override
    public Class<?> getColumnClass (int columnIndex) {
        return COLUMN_TYPES[columnIndex];
    }
    
    @Override
    public String getColumnName (int columnIndex) {
        return NbBundle.getMessage (SettingsTableModel.class, COLUMN_NAME_KEYS[columnIndex]);
    }
    private static void sortAlphabetically (List<UpdateUnitProvider> res) {
        Collections.sort (res, new Comparator<UpdateUnitProvider>() {
            @Override
            public int compare(UpdateUnitProvider p1, UpdateUnitProvider p2) {
                return p1.getDisplayName().compareTo(p2.getDisplayName());
            }
        });
        
    }
    
}
