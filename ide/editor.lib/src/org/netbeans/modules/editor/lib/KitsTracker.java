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

package org.netbeans.modules.editor.lib;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.openide.util.Lookup;

/**
 *
 * @author vita
 */
public abstract class KitsTracker {
        
    private static final Logger LOG = Logger.getLogger(KitsTracker.class.getName());
    private static final Set<String> ALREADY_LOGGED = Collections.synchronizedSet(new HashSet<String>(10));
    
    private static KitsTracker instance = null;
    
    /**
     * Gets the <code>KitsTracker</code> singleton instance.
     * @return The <code>KitsTracker</code> instance.
     */
    public static synchronized KitsTracker getInstance() {
        if (instance == null) {
            instance = Lookup.getDefault().lookup(KitsTracker.class);
            if (instance == null) {
                instance = new KitsTracker() {
                    private final ThreadLocal<String> context = new ThreadLocal<String>();
                    
                    @Override
                    public List<String> getMimeTypesForKitClass(Class kitClass) {
                        String mimeType = findMimeType(kitClass);
                        if (mimeType != null) {
                            return Collections.singletonList(mimeType);
                        } else {
                            return Collections.<String>emptyList();
                        }
                    }

                    @Override
                    public String findMimeType(Class kitClass) {
                        if (kitClass != null) {
                            return context.get();
                        } else {
                            return ""; //NOI18N
                        }
                    }

                    @Override
                    public Class<?> findKitClass(String mimeType) {
                        return null;
                    }

                    @Override
                    public Set<String> getMimeTypes() {
                        return Collections.<String>emptySet();
                    }

                    @Override
                    public String setContextMimeType(String mimeType) {
                        if (mimeType != null && MimePath.validate(mimeType)) {
                            assert false : "Invalid mimeType: '" + mimeType + "'"; //NOI18N
                        }
                        
                        String previous = context.get();
                        context.set(mimeType);
                        return previous;
                    }
                };
            }
        }
        return instance;
    }
    
    public static String getGenericPartOfCompoundMimeType(String mimeType) {
        int plusIdx = mimeType.lastIndexOf('+'); //NOI18N
        if (plusIdx != -1 && plusIdx < mimeType.length() - 1) {
            int slashIdx = mimeType.indexOf('/'); //NOI18N
            String prefix = mimeType.substring(0, slashIdx + 1);
            String suffix = mimeType.substring(plusIdx + 1);

            // fix for #61245
            if (suffix.equals("xml")) { //NOI18N
                prefix = "text/"; //NOI18N
            }

            return prefix + suffix;
        } else {
            return null;
        }
    }
    
    /**
     * Gets the list of mime types (<code>String</code>s) that use the given
     * class as an editor kit implementation.
     * 
     * @param kitClass The editor kit class to get mime types for.
     * @return The <code>List&lt;String&gt;</code> of mime types.
     */
    public abstract List<String> getMimeTypesForKitClass(Class kitClass);

    /**
     * Find mime type for a given editor kit implementation class.
     * 
     * @param kitClass The editor kit class to get the mime type for.
     * @return The mime type or <code>null</code> if the mime type can't be
     *   resolved for the given kit class.
     */
    public abstract String findMimeType(Class kitClass);

    public abstract Class findKitClass(String mimeType);
    
    /**
     * Gets all know mime types registered in the system.
     * 
     * @return The set of registered mimne types.
     */
    @SuppressWarnings("unchecked")
    public abstract Set<String> getMimeTypes();
    
    public abstract String setContextMimeType(String mimeType);
    
    public final void addPropertyChangeListener(PropertyChangeListener l) {
        PCS.addPropertyChangeListener(l);
    }
    
    public final void removePropertyChangeListener(PropertyChangeListener l) {
        PCS.removePropertyChangeListener(l);
    }

    protected final void firePropertyChange(String name, Object oldValue, Object newValue) {
        PCS.firePropertyChange(name, oldValue, newValue);
    }
    
    protected KitsTracker() {

    }
    
    // ------------------------------------------------------------------
    // private implementation
    // ------------------------------------------------------------------
    
    private final PropertyChangeSupport PCS = new PropertyChangeSupport(this);

}
