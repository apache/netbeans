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

package org.openide.util;

import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;

/**
 * Provides an implementation of the Preferences API which may be backed by
 * a NetBeans-specific implementation.
 * @see <a href="doc-files/preferences.html">Preferences API in NetBeans</a>
 * @since org.openide.util 7.4
 * @author Radek Matous
 */
public final class NbPreferences {
    private static Provider PREFS_IMPL;
    
    private  NbPreferences() {}
    
    /**
     * Returns user preference node . {@link Preferences#absolutePath} of such
     * a node depends whether class provided as a parameter was loaded as a part of any module
     * or not. If so, then absolute path corresponds to slashified code name base of module.
     * If not, then absolute path corresponds to class's package.
     *
     * @param cls the class for which a user preference node is desired.
     * @return the user preference node
     */
    public static Preferences forModule(Class cls) {
          if (PREFS_IMPL == null) {
                PREFS_IMPL = getPreferencesProvider();
          }
          return PREFS_IMPL.preferencesForModule(cls);
    }
    
    /**
     * Returns the root preference node.
     *
     * @return the root preference node.
     */
    public static Preferences root() {
          if (PREFS_IMPL == null) {
                PREFS_IMPL = getPreferencesProvider();
          }
          return PREFS_IMPL.preferencesRoot();
    }    
         
    private static Provider getPreferencesProvider() {
        Provider retval = Lookup.getDefault().lookup(Provider.class);
        if (retval == null) {
             retval = new Provider() {
                  public Preferences preferencesForModule(Class cls) {
                       return Preferences.userNodeForPackage(cls);
                  }

                  public Preferences preferencesRoot() {
                       return Preferences.userRoot();
                  }                         
             };
             // Avoided warning in case it is set 
             //(e.g. from NbTestCase - org.netbeans.junit.internal.MemoryPreferencesFactory).
             String prefsFactory = System.getProperty("java.util.prefs.PreferencesFactory");//NOI18N
             if (!"org.netbeans.junit.internal.MemoryPreferencesFactory".equals(prefsFactory)) {//NOI18N
                 Logger logger = Logger.getLogger(NbPreferences.class.getName());
                 logger.log(prefsFactory == null ? Level.WARNING : Level.FINE,
                         "NetBeans implementation of Preferences not found");//NOI18N
             } 
        }
        return retval;
    }    

    /**
     * Implementation of {@link NbPreferences} methods.
     * Not intended for use outside the NetBeans Platform.
     * @since org.openide.util 8.1
     */
    public interface Provider {
        /**
         * Returns user preference node. {@link Preferences#absolutePath} of such
         * a node depends whether class provided as a parameter was loaded as a part of any module
         * or not. If so, then absolute path corresponds to slashified code name base of module.
         * If not, then absolute path corresponds to class's package.
         *
         * @param cls the class for which a user preference node is desired.
         * @return the user preference node
         */
        Preferences preferencesForModule(Class cls);
        /**
         * Returns the root preference node.
         *
         * @return the root preference node.
         */
        Preferences preferencesRoot();
    }

}
