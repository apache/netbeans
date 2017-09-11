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

package org.netbeans.modules.options.editor.completion;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.*;
import java.util.prefs.Preferences;
import org.netbeans.modules.editor.settings.storage.api.EditorSettings;
import org.netbeans.modules.options.editor.spi.PreferencesCustomizer;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;


public final class CodeCompletionOptionsSelector {

    public static final String CODE_COMPLETION_CUSTOMIZERS_FOLDER = "OptionsDialog/Editor/CodeCompletion/"; //NOI18N

    private final Map<String, PreferencesCustomizer> allCustomizers = new HashMap<String, PreferencesCustomizer>();
    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
    private final CodeCompletionOptionsPanelController.PreferencesFactory pf;
    private Set<String> mimeTypes;
    private String selectedMimeType;

    CodeCompletionOptionsSelector(CodeCompletionOptionsPanelController.PreferencesFactory pf) {
        this.pf = pf;
    }

    public synchronized Set<? extends String> getMimeTypes() {
        if (mimeTypes == null) {
            mimeTypes = new HashSet<String>();
            mimeTypes.add(""); //NOI18N

            // filter out mime types that don't supply customizers
            for(String mimeType : EditorSettings.getDefault().getAllMimeTypes()) {
                Lookup l = Lookups.forPath(CODE_COMPLETION_CUSTOMIZERS_FOLDER + mimeType);
                if (l.lookup(PreferencesCustomizer.Factory.class) != null)
                    mimeTypes.add(mimeType);
            }
        }
        return mimeTypes;
    }

    public synchronized void setSelectedMimeType(String mimeType) {
        assert getMimeTypes().contains(mimeType) : "'" + mimeType + "' is not among " + getMimeTypes(); //NOI18N
        if (selectedMimeType == null || !selectedMimeType.equals(mimeType)) {
            String old = selectedMimeType;
            selectedMimeType = mimeType;
            pcs.firePropertyChange(null, old, mimeType);
        }
    }

    public synchronized PreferencesCustomizer getSelectedCustomizer() {
        return getCustomizer(selectedMimeType);
    }

    public void addPropertyChangeListener(PropertyChangeListener l) {
        pcs.addPropertyChangeListener(l);
    }

    public void removePropertyChangeListener(PropertyChangeListener l) {
        pcs.removePropertyChangeListener(l);
    }

    String getSavedValue(String mimeType, String key) {
        PreferencesCustomizer prefsCustomizer = getCustomizer(mimeType);
        if (prefsCustomizer != null) {
            Lookup l = Lookups.forPath(CODE_COMPLETION_CUSTOMIZERS_FOLDER + mimeType);
            PreferencesCustomizer.CustomCustomizer customizer;
            if (mimeType.isEmpty()) {
                customizer = l.lookup(GeneralCompletionOptionsPanelController.CustomCustomizerImpl.class);
            } else {
                customizer = l.lookup(PreferencesCustomizer.CustomCustomizer.class);
            }
            if (customizer != null) {
                return customizer.getSavedValue(prefsCustomizer, key);
            }
        }
        return null;
    }

    private PreferencesCustomizer getCustomizer(String mimeType) {
        PreferencesCustomizer customizer = allCustomizers.get(mimeType);
        if (customizer == null) {
            Preferences prefs = pf.getPreferences(mimeType);
            if (mimeType.length() > 0) {
                Lookup l = Lookups.forPath(CODE_COMPLETION_CUSTOMIZERS_FOLDER + mimeType);
                PreferencesCustomizer.Factory factory = l.lookup(PreferencesCustomizer.Factory.class);
                if (factory != null)
                    customizer = factory.create(prefs);
            } else {
                customizer = new GeneralCompletionOptionsPanelController(prefs);
            }
            allCustomizers.put(selectedMimeType, customizer);
        }
        return customizer;
    }
}
