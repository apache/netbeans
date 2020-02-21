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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.cnd.highlight.semantic.options;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Set;
import java.util.prefs.Preferences;
import org.openide.util.NbPreferences;
import org.openide.util.WeakSet;

/**
 *
 */
public final class SemanticHighlightingOptions {

    private SemanticHighlightingOptions() {}
    
    private static class Instantiator {
        public static SemanticHighlightingOptions instance = new SemanticHighlightingOptions();
        private Instantiator() {
        }
    }
    
    public static SemanticHighlightingOptions instance() {
        return Instantiator.instance;
    }

    private final Preferences preferences = NbPreferences.forModule(SemanticHighlightingOptions.class);

    private static final String ENABLE_MARK_OCCURRENCES = "EnableMarkOccurrences"; // NOI18N
    private static final String KEEP_MARKS = "KeepMarks"; // NOI18N

    public static final boolean SEMANTIC_ADVANCED = Boolean.getBoolean("cnd.semantic.advanced"); // NOI18N

    private final Set<PropertyChangeListener> listeners = new WeakSet<>();
    private final Object lock = new Object();

    public void addPropertyChangeListener(PropertyChangeListener listener){
        synchronized(lock) {
            listeners.add(listener);
        }
    }

    public void removePropertyChangeListener(PropertyChangeListener listener){
        synchronized(lock) {
            listeners.remove(listener);
        }
    }

    public void propertyChange(PropertyChangeEvent evt) {
        synchronized(lock) {
            for(PropertyChangeListener listener : listeners){
                listener.propertyChange(evt);
            }
        }
    }

    private boolean getOption(String key, boolean defaultValue) {
        return preferences.getBoolean(key, defaultValue);
    }

    private void setOption(String key, boolean value) {
        preferences.putBoolean(key, value);
    }
    
    public boolean getEnableMarkOccurrences() {
        return getOption(ENABLE_MARK_OCCURRENCES, true);
    }

    public void setEnableMarkOccurrences(boolean value) {
        setOption(ENABLE_MARK_OCCURRENCES, value);
    }

    public boolean getKeepMarks() {
        return getOption(KEEP_MARKS, true);
    }

    public void setKeepMarks(boolean value) {
        setOption(KEEP_MARKS, value);
    }
}
