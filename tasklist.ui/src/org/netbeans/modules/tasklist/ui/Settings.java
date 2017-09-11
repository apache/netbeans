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
