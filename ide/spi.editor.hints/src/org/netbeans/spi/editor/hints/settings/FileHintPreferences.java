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
package org.netbeans.spi.editor.hints.settings;

import java.util.prefs.AbstractPreferences;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import javax.swing.event.ChangeListener;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.modules.editor.hints.settings.friend.FileHintPreferencesProvider;
import org.netbeans.modules.editor.hints.settings.friend.OpenGlobalPreferences;
import org.openide.filesystems.FileObject;
import org.openide.util.ChangeSupport;
import org.openide.util.Lookup;
import org.openide.util.RequestProcessor;

/**Get hints settings for given {@link FileObject}
 *
 * @author lahvac
 * @since 1.31
 */
public class FileHintPreferences {
    
    /**
     * 
     * @param file
     * @param preferencesMimeType
     * @return 
     */
    public static Preferences getFilePreferences(FileObject file, String preferencesMimeType) {
        for (FileHintPreferencesProvider p : Lookup.getDefault().lookupAll(FileHintPreferencesProvider.class)) {
            Preferences prefs = p.getFilePreferences(file, preferencesMimeType);
            
            if (prefs != null) {
                return prefs;
            }
        }
        
        for (GlobalHintPreferencesProvider p : MimeLookup.getLookup(preferencesMimeType).lookupAll(GlobalHintPreferencesProvider.class)) {
            Preferences prefs = p.getGlobalPreferences();
            
            if (prefs != null) {
                return prefs;
            }
        }
        
        throw new IllegalStateException("Must have some working GlobalHintPreferencesProvider!");
    }
    
    /**Open hint settings for the specific file and hint. Will open either the
     * global or project-specific settings.
     *
     * @param file file for which the settings should be opened
     * @param preferencesMimeType mime type for which the settings should be opened
     * @param hintId hint id that should be opened
     * @since 1.57
     */
    public static void openFilePreferences(FileObject file, String preferencesMimeType, String hintId) {
        for (FileHintPreferencesProvider p : Lookup.getDefault().lookupAll(FileHintPreferencesProvider.class)) {
            if (p.openFilePreferences(file, preferencesMimeType, hintId)) {
                return ;
            }
        }

        for (OpenGlobalPreferences p : Lookup.getDefault().lookupAll(OpenGlobalPreferences.class)) {
            if (p.openFilePreferences(preferencesMimeType, hintId)) {
                return ;
            }
        }

        throw new IllegalStateException("Must have some working GlobalHintPreferencesProvider!");
    }

    private static final ChangeSupport cs = new ChangeSupport(FileHintPreferences.class);
    
    /**Register listener to be notified about any change in the hints settings.
     * 
     * @param l the listener
     */
    public static void addChangeListener(ChangeListener l) {
        cs.addChangeListener(l);
    }

    /**Unregister a listener previously registered by {@link #addChangeListener(javax.swing.event.ChangeListener) }.
     * 
     * @param l the listener
     */
    public static void removeChangeListener(ChangeListener l) {
        cs.removeChangeListener(l);
    }
    
    /**Fire event about changed hints settings.*/
    public static void fireChange() {
        cs.fireChange();
    }
    
    private static void fireChangeEventually() {
        FIRE_WORKER.post(new Runnable() {
            @Override public void run() {
                fireChange();
            }
        });
    }
    
    /**Getter for global hints settings. Used as a fallback by {@link #getFilePreferences(org.openide.filesystems.FileObject, java.lang.String) },
     * when there are no more specific settings for the files. To be registered in
     * the {@link MimeLookup} for the given mime-type.
     * 
     */
    public interface GlobalHintPreferencesProvider {
        /**Return the global preferences for the given hint mime-type.
         * 
         * @return the global preferences for the given hint mime-type
         */
        public Preferences getGlobalPreferences();
    }
    
    private static final RequestProcessor FIRE_WORKER = new RequestProcessor(FileHintPreferences.class.getName(), 1, false, false);
    
    private static final class WrapperPreferences extends AbstractPreferences {

        private final Preferences delegate;

        public WrapperPreferences(WrapperPreferences parent, String name, Preferences delegate) {
            super(parent, name);
            this.delegate = delegate;
        }

        @Override
        protected void putSpi(String key, String value) {
            delegate.put(key, value);
            fireChangeEventually();
        }

        @Override
        protected String getSpi(String key) {
            return delegate.get(key, null);
        }

        @Override
        protected void removeSpi(String key) {
            delegate.remove(key);
            fireChangeEventually();
        }

        @Override
        protected void removeNodeSpi() throws BackingStoreException {
            delegate.removeNode();
            fireChangeEventually();
        }

        @Override
        protected String[] keysSpi() throws BackingStoreException {
            return delegate.keys();
        }

        @Override
        protected String[] childrenNamesSpi() throws BackingStoreException {
            return delegate.childrenNames();
        }

        @Override
        protected AbstractPreferences childSpi(String name) {
            return new WrapperPreferences(this, name, delegate.node(name));
        }

        @Override
        protected void syncSpi() throws BackingStoreException {
            delegate.sync();
        }

        @Override
        protected void flushSpi() throws BackingStoreException {
            delegate.flush();
        }
    
    }
    
}
