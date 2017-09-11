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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
