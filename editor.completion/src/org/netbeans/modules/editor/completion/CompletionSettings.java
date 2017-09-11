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
