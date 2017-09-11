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
 * The Original Software is the Accelerators module.
 * The Initial Developer of the Original Software is Andrei Badea.
 * Portions Copyright 2005-2006 Andrei Badea.
 * All Rights Reserved.
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
 *
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
