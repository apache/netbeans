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

package org.netbeans.modules.jumpto.type;

import java.util.prefs.Preferences;
import org.openide.util.NbPreferences;

/** Holds all the UI options, names etc, Uses innerclasses plainly as namespaces.
 *
 * @author phrebejk
 */
public final class UiOptions {
            
    /** Creates a new instance of UiOptions */
    private UiOptions() {}
    
    static final class GoToTypeDialog {
    
        private static final String GO_TO_TYPE_DIALOG = "GoToTypeDialog"; // NOI18N    
        
        private static final String CASE_SENSITIVE = "caseSensitive"; // NOI18N
        private static final String WIDTH = "width"; // NOI18N
        private static final String HEIGHT = "height"; // NOI18N
    
        private static Preferences node;
        
        public static boolean getCaseSensitive() {
            return getNode().getBoolean(CASE_SENSITIVE, false);
        }
        
        public static void setCaseSensitive( boolean caseSensitive) {
            getNode().putBoolean(CASE_SENSITIVE, caseSensitive);
        }
        
        public static int getHeight() {
            return getNode().getInt(HEIGHT, -1);
        }
        
        public static void setHeight( int height ) {
            getNode().putInt(HEIGHT, height);
        }
        
        public static int getWidth() {
            return getNode().getInt(WIDTH, -1);
        }
         
        public static void setWidth( int width ) {
            getNode().putInt(WIDTH, width);
        }
        
        private static synchronized Preferences getNode() {
            if ( node == null ) {                
                Preferences p = NbPreferences.forModule(UiOptions.class);
                node = p.node(GO_TO_TYPE_DIALOG);
            }
            return node;
        }
    }
    
    public static final class GoToSymbolDialog {
    
        private static final String GO_TO_SYMBOL_DIALOG = "GoToSymbolDialog"; // NOI18N    
        
        private static final String CASE_SENSITIVE = "caseSensitive"; // NOI18N
        private static final String WIDTH = "width"; // NOI18N
        private static final String HEIGHT = "height"; // NOI18N
    
        private static Preferences node;                       
        
        public static boolean getCaseSensitive() {
            return getNode().getBoolean(CASE_SENSITIVE, false);
        }
        
        public static void setCaseSensitive( boolean caseSensitive) {
            getNode().putBoolean(CASE_SENSITIVE, caseSensitive);
        }
        
        public static int getHeight() {
            return getNode().getInt(HEIGHT, -1);
        }
        
        public static void setHeight( int height ) {
            getNode().putInt(HEIGHT, height);
        }
        
        public static int getWidth() {
            return getNode().getInt(WIDTH, -1);
        }
         
        public static void setWidth( int width ) {
            getNode().putInt(WIDTH, width);
        }
        
        private static synchronized Preferences getNode() {
            if ( node == null ) {                
                Preferences p = NbPreferences.forModule(UiOptions.class);
                node = p.node(GO_TO_SYMBOL_DIALOG);
            }
            return node;
        }
    }
    
}
