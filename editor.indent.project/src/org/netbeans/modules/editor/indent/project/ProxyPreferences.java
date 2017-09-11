/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.editor.indent.project;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.AbstractPreferences;
import java.util.prefs.BackingStoreException;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import java.util.prefs.Preferences;
import org.openide.util.WeakListeners;

/**
 *
 * @author vita
 */
public final class ProxyPreferences extends AbstractPreferences {

    public ProxyPreferences(Preferences... delegates) {
        this("", null, delegates); //NOI18N
    }

    @Override
    protected void putSpi(String key, String value) {
        checkDelegates();
        for(int i = 0; i < delegates.length; i++) {
            if (delegates[i] != null) {
                delegates[i].put(key, value);
                return;
            }
        }
    }

    @Override
    protected String getSpi(String key) {
        checkDelegates();
        for(int i = 0; i < delegates.length; i++) {
            if (delegates[i] != null) {
                try {
                    String value = delegates[i].get(key, null);
                    if (value != null) {
                        return value;
                    }
                } catch (Exception e) {
                    // mark the delegate as invalid
                    delegates[i] = null;
                }
            }
        }
        return null;
    }

    @Override
    protected void removeSpi(String key) {
        throw new UnsupportedOperationException("Not supported yet."); //NOI18N
    }

    @Override
    protected void removeNodeSpi() throws BackingStoreException {
        throw new UnsupportedOperationException("Not supported yet."); //NOI18N
    }

    @Override
    protected String[] keysSpi() throws BackingStoreException {
        Set<String> keys = new HashSet<String>();
        checkDelegates();
        for(int i = 0; i < delegates.length; i++) {
            if (delegates[i] != null) {
                try {
                    keys.addAll(Arrays.asList(delegates[i].keys()));
                } catch (Exception e) {
                    // mark the delegate as invalid
                    delegates[i] = null;
                }
            }
        }
        return keys.toArray(new String[ keys.size() ]);
    }

    @Override
    protected String[] childrenNamesSpi() throws BackingStoreException {
//        Set<String> names = new HashSet<String>();
//        for(Preferences d : delegates) {
//            names.addAll(Arrays.asList(d.childrenNames()));
//        }
//        return names.toArray(new String[ names.size() ]);
        return EMPTY;
    }

    @Override
    protected AbstractPreferences childSpi(String name) {
//        Preferences [] nueDelegates = new Preferences[delegates.length];
//        for(int i = 0; i < delegates.length; i++) {
//            nueDelegates[i] = delegates[i].node(name);
//        }
//        return new ProxyPreferences(name, this, nueDelegates);
        throw new UnsupportedOperationException("Not supported yet."); //NOI18N
    }

    @Override
    protected void syncSpi() throws BackingStoreException {
//        delegates[0].sync();
        throw new UnsupportedOperationException("Not supported yet."); //NOI18N
    }

    @Override
    protected void flushSpi() throws BackingStoreException {
//        delegates[0].flush();
        throw new UnsupportedOperationException("Not supported yet."); //NOI18N
    }

    // -----------------------------------------------------------------------
    // private implementation
    // -----------------------------------------------------------------------

    private static final Logger LOG = Logger.getLogger(ProxyPreferences.class.getName());

    private static final String [] EMPTY = new String[0];
    
    private final Preferences [] delegates;
    private final Preferences [] roots;
    private final String [] paths;
    private final PreferenceChangeListener [] prefTrackers;

    private ProxyPreferences(String name, ProxyPreferences parent, Preferences... delegates) {
        super(parent, name); //NOI18N
        assert delegates.length > 0 : "There must be at least one delegate"; //NOI18N
        this.delegates = delegates;
        this.roots = new Preferences[delegates.length];
        this.paths = new String[delegates.length];
        this.prefTrackers = new PreferenceChangeListener[delegates.length];

        for(int i = 0; i < delegates.length; i++) {
            roots[i] = delegates[i].node("/"); //NOI18N
            paths[i] = delegates[i].absolutePath();
            prefTrackers[i] = new PrefTracker(i);
            delegates[i].addPreferenceChangeListener(WeakListeners.create(PreferenceChangeListener.class, prefTrackers[i], delegates[i]));
        }
    }

    private void checkDelegates() {
        for(int i = 0; i < delegates.length; i++) {
            if (delegates[i] != null) {
                try {
                    if (delegates[i].nodeExists("")) { //NOI18N
                        continue;
                    }
                } catch (BackingStoreException bse) {
                    // ignore
                }
                delegates[i] = null;
            }

            assert delegates[i] == null;
            try {
                if (roots[i].nodeExists(paths[i])) {
                    delegates[i] = roots[i].node(paths[i]);
                }
            } catch (BackingStoreException bse) {
                // ignore
            }
        }
    }

    private void firePrefChange(String key, String newValue) {
        try {
            Method m = AbstractPreferences.class.getDeclaredMethod("enqueuePreferenceChangeEvent", String.class, String.class); //NOI18N
            m.setAccessible(true);
            m.invoke(this, key, newValue);
        } catch (Exception e) {
            LOG.log(Level.WARNING, null, e);
        }
    }
    
    private class PrefTracker implements PreferenceChangeListener {

        private final int delegateIdx;

        public PrefTracker(int idx) {
            this.delegateIdx = idx;
        }
        
        public void preferenceChange(PreferenceChangeEvent evt) {
            synchronized (ProxyPreferences.this.lock) {
                if (evt.getKey() != null) {
                    checkDelegates();
                    for(int i = 0; i < delegateIdx; i++) {
                        if (delegates[i] != null && delegates[i].get(evt.getKey(), null) != null) {
                            // ignore
                            return;
                        }
                    }
                }
            }

            firePrefChange(evt.getKey(), evt.getNewValue());
        }

    } // End of PrefTracker class
}
