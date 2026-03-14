/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.core.multitabs.prefs;

import java.util.prefs.Preferences;
import javax.swing.JTabbedPane;
import org.netbeans.core.multitabs.impl.ProjectColorTabDecorator;
import org.netbeans.core.multitabs.impl.ProjectSupport;
import org.netbeans.core.windows.options.WinSysPanel;
import org.netbeans.core.windows.options.WinSysPrefs;
import org.openide.util.NbPreferences;

/**
 *
 * @author S. Aubrecht
 */
public class SettingsImpl {

    private final Preferences prefs = NbPreferences.forModule( SettingsImpl.class ).node( "multi-tabs" ); //NOI18N

    static final String PROP_SHOW_FOLDER_NAME = "show-folder-name"; //NOI18N
    static final String PROP_SHOW_FULL_PATH = "show-full-path"; //NOI18N
    static final String PROP_SAME_PROJECT_SAME_COLOR = "same-project-same-color"; //NOI18N
    static final String PROP_SORT_DOCUMENT_LIST_BY_PROJECT = "sort-document-list-by-project"; //NOI18N
    static final String PROP_ROW_COUNT = "row-count"; //NOI18N
    static final String PROP_TAB_ROW_PER_PROJECT = "tab-row-per-project"; //NOI18N

    private Preferences getWinSysPrefs() {
        return NbPreferences.forModule( WinSysPanel.class );
    }
    public int getTabsLocation() {
        return getWinSysPrefs().getInt( WinSysPrefs.DOCUMENT_TABS_PLACEMENT, JTabbedPane.TOP );
    }

    boolean setTabsLocation( int newLocation ) {
        boolean change = newLocation != getTabsLocation();
        getWinSysPrefs().putInt( WinSysPrefs.DOCUMENT_TABS_PLACEMENT, newLocation );
        return change;
    }

    public boolean isEnabled() {
        boolean res = false;
        ProjectSupport projectSupport = ProjectSupport.getDefault();
        res |= isShowFolderName();
        res |= isSameProjectSameColor() && projectSupport.isEnabled();
        res |= isShowFullPath();
        res |= isSortDocumentListByProject() && projectSupport.isEnabled();
        res |= getRowCount() > 1;
        res |= isTabRowPerProject() && projectSupport.isEnabled();
        res |= getTabsLocation() != JTabbedPane.TOP;
        return res;
    }

    public boolean isShowFolderName() {
        return prefs.getBoolean( PROP_SHOW_FOLDER_NAME, false );
    }

    boolean setShowFolderName( boolean showFolders ) {
        boolean change = showFolders != isShowFolderName();
        prefs.putBoolean( PROP_SHOW_FOLDER_NAME, showFolders );
        return change;
    }

    public boolean isShowFullPath() {
        return prefs.getBoolean( PROP_SHOW_FULL_PATH, false );
    }

    boolean setShowFullPath( boolean show ) {
        boolean change = show != isShowFullPath();
        prefs.putBoolean( PROP_SHOW_FULL_PATH, show );
        return change;
    }

    public boolean isSameProjectSameColor() {
        return prefs.getBoolean( PROP_SAME_PROJECT_SAME_COLOR, false );
    }

    boolean setSameProjectSameColor( boolean enable ) {
        boolean change = enable != isSameProjectSameColor();
        prefs.putBoolean( PROP_SAME_PROJECT_SAME_COLOR, enable );
        ProjectColorTabDecorator.setActive( enable );
        return change;
    }

    boolean setSortDocumentListByProject( boolean sort ) {
        boolean change = sort != isSortDocumentListByProject();
        prefs.putBoolean( PROP_SORT_DOCUMENT_LIST_BY_PROJECT, sort );
        return change;
    }

    public boolean isSortDocumentListByProject() {
        return prefs.getBoolean( PROP_SORT_DOCUMENT_LIST_BY_PROJECT, true );
    }

    public boolean isTabRowPerProject() {
        return prefs.getBoolean( PROP_TAB_ROW_PER_PROJECT, false );
    }

    boolean setTabRowPerProject( boolean enable ) {
        boolean change = enable != isTabRowPerProject();
        prefs.putBoolean( PROP_TAB_ROW_PER_PROJECT, enable );
        return change;
    }

    boolean setRowCount( int rowCount ) {
        boolean change = rowCount != getRowCount();
        prefs.putInt( PROP_ROW_COUNT, rowCount );
        getWinSysPrefs().putBoolean( WinSysPrefs.DOCUMENT_TABS_MULTIROW, rowCount > 1 );
        return change;
    }

    public int getRowCount() {
        int defRowCount = 1;
        if( getWinSysPrefs().getBoolean( WinSysPrefs.DOCUMENT_TABS_MULTIROW, false ) )
            defRowCount = 3;
        return prefs.getInt( PROP_ROW_COUNT, defRowCount );
    }
}
