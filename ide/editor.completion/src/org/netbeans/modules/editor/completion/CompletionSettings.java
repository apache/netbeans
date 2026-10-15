/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.editor.completion;

import java.awt.Dimension;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import javax.swing.text.JTextComponent;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.editor.settings.SimpleValueNames;
import org.netbeans.lib.editor.util.swing.DocumentUtilities;

/**
 * Maintenance of the editor settings related to the code completion.
 *
 * @author Miloslav Metelka
 * @version 1.00
 */

public final class CompletionSettings {
    
    // -----------------------------------------------------------------------
    // public implementation
    // -----------------------------------------------------------------------
    
    public static synchronized CompletionSettings getInstance(JTextComponent component) {
        return new CompletionSettings(component != null ? DocumentUtilities.getMimeType(component) : null);
    }

    public boolean completionAutoPopup() {
        return preferences.getBoolean(SimpleValueNames.COMPLETION_AUTO_POPUP, true);
    }
    
    public int completionAutoPopupDelay() {
        return preferences.getInt(SimpleValueNames.COMPLETION_AUTO_POPUP_DELAY, 0);
    }
    
    public boolean documentationAutoPopup() {
        return preferences.getBoolean(SimpleValueNames.JAVADOC_AUTO_POPUP, true);
    }

    /**
     * Whether documentation popup should be displayed next to completion popup
     * @return true if yes
     */
    boolean documentationPopupNextToCC() {
        return preferences.getBoolean(SimpleValueNames.JAVADOC_POPUP_NEXT_TO_CC, false);
    }

    public boolean completionDisplayTooltip() {
        return preferences.getBoolean(SimpleValueNames.COMPLETION_PARAMETER_TOOLTIP, true);
    }

    public int documentationAutoPopupDelay() {
        return preferences.getInt(SimpleValueNames.JAVADOC_AUTO_POPUP_DELAY, 200);
    }
    
    public Dimension completionPaneMaximumSize() {
        return parseDimension(preferences.get(SimpleValueNames.COMPLETION_PANE_MAX_SIZE, null), new Dimension(400, 300));
    }
    
    public Dimension documentationPopupPreferredSize() {
        return parseDimension(preferences.get(SimpleValueNames.JAVADOC_PREFERRED_SIZE, null), new Dimension(500, 300));
    }
    
    public boolean completionInstantSubstitution() {
        return preferences.getBoolean(SimpleValueNames.COMPLETION_INSTANT_SUBSTITUTION, true);
    }

    public boolean completionCaseSensitive() {
        return preferences.getBoolean(SimpleValueNames.COMPLETION_CASE_SENSITIVE, true);
    }
    
    // -----------------------------------------------------------------------
    // private implementation
    // -----------------------------------------------------------------------

    private static final Logger LOG = Logger.getLogger(CompletionSettings.class.getName());
    private Preferences preferences = null;

    private CompletionSettings(String mimeType) {
        this.preferences = (mimeType != null ? MimeLookup.getLookup(mimeType) : MimeLookup.getLookup(MimePath.EMPTY)).lookup(Preferences.class);
    }

    private static Dimension parseDimension(String s, Dimension d) {
        int arr[] = new int[2];
        int i = 0;
        
        if (s != null) {
            StringTokenizer st = new StringTokenizer(s, ","); // NOI18N

            while (st.hasMoreElements()) {
                if (i > 1) {
                    return d;
                }
                try {
                    arr[i] = Integer.parseInt(st.nextToken());
                } catch (NumberFormatException nfe) {
                    LOG.log(Level.WARNING, null, nfe);
                    return d;
                }
                i++;
            }
        }
        
        if (i != 2) {
            return d;
        } else {
            return new Dimension(arr[0], arr[1]);
        }
    }
}
