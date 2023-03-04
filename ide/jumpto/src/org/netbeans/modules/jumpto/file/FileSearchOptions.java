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

package org.netbeans.modules.jumpto.file;

import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import org.openide.util.Exceptions;
import org.openide.util.NbPreferences;

/**
 *
 * @author Andrei Badea, Petr Hrebejk
 */
public class FileSearchOptions  {
        
    private static final String CASE_SENSITIVE = "caseSensitive"; // NOI18N
    private static final String SHOW_HIDDEN_FILES = "showHiddenFiles"; // NOI18N 
    private static final String PREFER_MAIN_PROJECT = "preferMainProject"; // NOI18N    
    private static final String SEARCH_BY_FOLDERS = "searchByFolders"; // NOI18N
    private static final String WIDTH = "width"; // NOI18N
    private static final String HEIGHT = "height"; // NOI18N

    private static Preferences node;

    public static boolean getCaseSensitive() {
        return getNode().getBoolean(CASE_SENSITIVE, false);
    }

    public static void setCaseSensitive( boolean caseSensitive) {
        getNode().putBoolean(CASE_SENSITIVE, caseSensitive);
    }
    
    public static boolean getShowHiddenFiles() {
        return getNode().getBoolean(SHOW_HIDDEN_FILES, false);
    }

    public static void setShowHiddenFiles( boolean showHiddenFiles) {
        getNode().putBoolean(SHOW_HIDDEN_FILES, showHiddenFiles);
    }

    public static boolean getPreferMainProject() {
        return getNode().getBoolean(PREFER_MAIN_PROJECT, true);
    }

    public static void setPreferMainProject( boolean preferMainProject) {
        getNode().putBoolean(PREFER_MAIN_PROJECT, preferMainProject);
    }
    
    public static void setSearchByFolders(boolean preferMainProject) {
        getNode().putBoolean(SEARCH_BY_FOLDERS, preferMainProject);
    }

    public static boolean getSearchByFolders() {
        return getNode().getBoolean(SEARCH_BY_FOLDERS, false);
    }

    public static int getHeight() {
        return getNode().getInt(HEIGHT, 460);
    }

    public static void setHeight( int height ) {
        getNode().putInt(HEIGHT, height);
    }

    public static int getWidth() {
        return getNode().getInt(WIDTH, 740);
    }

    public static void setWidth( int width ) {
        getNode().putInt(WIDTH, width);
    }

    static void flush() {
        try {
            getNode().flush();
        }
        catch (BackingStoreException ex) {
            Exceptions.printStackTrace(ex);
        }
    }
    
    private static synchronized Preferences getNode() {
        if ( node == null ) {                
            node = NbPreferences.forModule(FileSearchOptions.class);
        }
        return node;
    }
    
}
