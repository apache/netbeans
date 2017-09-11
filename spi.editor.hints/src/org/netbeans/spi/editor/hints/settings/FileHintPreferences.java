/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */
package org.netbeans.spi.editor.hints.settings;

import java.util.prefs.AbstractPreferences;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import javax.swing.event.ChangeListener;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.modules.editor.hints.settings.friend.FileHintPreferencesProvider;
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
