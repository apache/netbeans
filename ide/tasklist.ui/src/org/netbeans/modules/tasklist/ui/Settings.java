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

package org.netbeans.modules.tasklist.ui;

import java.util.prefs.Preferences;
import org.netbeans.modules.tasklist.impl.ScanningScopeList;
import org.netbeans.spi.tasklist.TaskScanningScope;
import org.openide.util.NbPreferences;

/**
 *
 * @author S. Aubrecht
 */
public class Settings {
    
    private static final String PREF_FOLDING_WIDTH = "folding_table_col_"; //NOI18N
    private static final String PREF_REGULAR_WIDTH = "table_col_"; //NOI18N
    
    private static Settings theInstance;
    
    private Preferences prefs;
    
    /** Creates a new instance of Settings */
    private Settings() {
        prefs = NbPreferences.forModule( Settings.class );
    }
    
    public static Settings getDefault() {
        if( null == theInstance )
            theInstance = new Settings();
        return theInstance;
    }
    
    public int getSortingColumn() {
        return prefs.getInt( "sortingColumn", -1 ); //NOI18N
    }
    
    public void setSortingColumn( int col ) {
        prefs.putInt( "sortingColumn", col ); //NOI18N        
    }
    
    public boolean isAscendingSort() {
        return prefs.getBoolean( "sortingAscending", true ); //NOI18N
    }
    
    public void setAscendingSort( boolean asc ) {
        prefs.putBoolean( "sortingAscending", asc ); //NOI18N
    }
    
    public float getPreferredColumnWidth( int col, boolean foldingTable, float defaultValue ) {
        return prefs.getFloat( (foldingTable ? PREF_FOLDING_WIDTH : PREF_REGULAR_WIDTH) +col, defaultValue );
    }
    
    public void setPreferredColumnWidth( int col, boolean foldingTable, float colWidth ) {
        prefs.putFloat( (foldingTable ? PREF_FOLDING_WIDTH : PREF_REGULAR_WIDTH)+col, colWidth );
    }
    
    public boolean isGroupExpanded( String groupName ) {
        return prefs.getBoolean( "expanded_"+groupName, true ); //NOI18N        
    }
    
    public void setGroupExpanded( String groupName, boolean expand ) {
        prefs.putBoolean( "expanded_"+groupName, expand ); //NOI18N        
    }
    
    public void setActiveScanningScope( TaskScanningScope scope ) {
        prefs.put( "activeScanningScope", scope.getClass().getName() ); //NOI18N
    }
    
    public TaskScanningScope getActiveScanningScope() {
        String clazzName = prefs.get( "activeScanningScope", null ); //NOI18N
        if( null != clazzName ) {
            for( TaskScanningScope scope : ScanningScopeList.getDefault().getTaskScanningScopes() ) {
                if( scope.getClass().getName().equals( clazzName ) )
                    return scope;
            }
        }
        return null;
    }
    
    public void setGroupTasksByCategory( boolean group ) {
        prefs.putBoolean( "groupTasksByCategory", group ); //NOI18N
    }
    
    public boolean getGroupTasksByCategory() {
        return prefs.getBoolean( "groupTasksByCategory", false ); //NOI18N
    }
}
