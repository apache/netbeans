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
        return prefs.getBoolean( PROP_SORT_DOCUMENT_LIST_BY_PROJECT, false );
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
