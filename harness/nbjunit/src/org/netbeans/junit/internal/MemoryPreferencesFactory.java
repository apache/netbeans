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

package org.netbeans.junit.internal;

import java.util.Properties;
import java.util.prefs.AbstractPreferences;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import java.util.prefs.PreferencesFactory;

/**
 *
 * @author Radek Matous
 */
public class MemoryPreferencesFactory implements PreferencesFactory {
    /** Creates a new instance  */
    public MemoryPreferencesFactory() {}
    
    public Preferences userRoot() {
        return NbPreferences.userRootImpl();
    }
    
    public Preferences systemRoot() {
        return NbPreferences.systemRootImpl();
    }
        
    private static class NbPreferences extends AbstractPreferences {
        private static Preferences USER_ROOT;
        private static Preferences SYSTEM_ROOT;
        
        /*private*/Properties properties;
        
        static Preferences userRootImpl() {
            if (USER_ROOT == null) {
                USER_ROOT = new NbPreferences();
            }
            return USER_ROOT;
        }
        
        static Preferences systemRootImpl() {
            if (SYSTEM_ROOT == null) {
                SYSTEM_ROOT = new NbPreferences();
            }
            return SYSTEM_ROOT;
        }
        
        
        private NbPreferences() {
            super(null, "");
        }
        
        /** Creates a new instance of PreferencesImpl */
        private  NbPreferences(NbPreferences parent, String name)  {
            super(parent, name);
            newNode = true;
        }
        
        protected final String getSpi(String key) {
            return properties().getProperty(key);
        }
        
        protected final String[] childrenNamesSpi() throws BackingStoreException {
            return new String[0];
        }
        
        protected final String[] keysSpi() throws BackingStoreException {
            return properties().keySet().toArray(new String[0]);
        }
        
        protected final void putSpi(String key, String value) {
            properties().put(key,value);
        }
        
        protected final void removeSpi(String key) {
            properties().remove(key);
        }
        
        protected final void removeNodeSpi() throws BackingStoreException {}
        protected  void flushSpi() throws BackingStoreException {}
        protected void syncSpi() throws BackingStoreException {
            properties().clear();
        }
        
        @Override
        public void put(String key, String value) {
            try {
                super.put(key, value);
            } catch (IllegalArgumentException iae) {
                if (iae.getMessage().contains("too long")) {
                    // Not for us!
                    putSpi(key, value);
                } else {
                    throw iae;
                }
            }
        }
        
        Properties properties()  {
            if (properties == null) {
                properties = new Properties();
            }
            return properties;
        }
        
        protected AbstractPreferences childSpi(String name) {
            return new NbPreferences(this, name);
        }
    }
    
}
