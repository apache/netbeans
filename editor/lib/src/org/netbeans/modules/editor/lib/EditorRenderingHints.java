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

package org.netbeans.modules.editor.lib;

import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.logging.Logger;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import java.util.prefs.Preferences;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.openide.util.Utilities;
import org.openide.util.WeakListeners;

/**
 *
 * @author vita
 */
public final class EditorRenderingHints {

    public static final String TEXT_ANTIALIASING_PROP = "textAntialiasing"; // NOI18N
    public static final String PROP_HINTS = "EditorRenderingHints.PROP_HINTS"; //NOI18N
    
    public static synchronized EditorRenderingHints get(MimePath mimePath) {
        EditorRenderingHints erh = CACHE.get(mimePath);
        if (erh == null) {
            Preferences prefs = MimeLookup.getLookup(mimePath).lookup(Preferences.class);
            erh = new EditorRenderingHints(prefs);
            CACHE.put(mimePath, erh);
        }
        return erh;
    }

    public static boolean isAAOnByDefault() {
        Map systemHints = (Map) Toolkit.getDefaultToolkit().getDesktopProperty("awt.font.desktophints"); //NOI18N
        if (systemHints != null) {
            Object o = systemHints.get(RenderingHints.KEY_TEXT_ANTIALIASING);
            boolean result = o != null && 
                             o != RenderingHints.VALUE_TEXT_ANTIALIAS_OFF &&  
                             o != RenderingHints.VALUE_TEXT_ANTIALIAS_DEFAULT;
            return result;
        } else {
            return false;
        }
    }
    
    public Map<?, ?> getHints() {
        synchronized (this) {
            if (hints == null) {
                Map<?, ?> desktopHints = (Map<?, ?>) Toolkit.getDefaultToolkit().getDesktopProperty("awt.font.desktophints"); //NOI18N
                Boolean aaOn = null;
                String reason = null;
                
                String aaOnString = prefs.get(TEXT_ANTIALIASING_PROP, null);
                if (aaOnString != null) {
                    aaOn = Boolean.valueOf(aaOnString);
                    reason = "editor preferences property '" + TEXT_ANTIALIASING_PROP + "'"; //NOI18N
                } else {
                    String systemProperty = System.getProperty("javax.aatext"); //NOI18N
                    if (systemProperty == null) {
                        systemProperty = System.getProperty("swing.aatext"); //NOI18N
                    }
                    
                    if (systemProperty != null) {
                        aaOn = Boolean.valueOf(systemProperty);
                        reason = "system property 'javax.aatext' or 'swing.aatext'"; //NOI18N
                    } else {
                        if (desktopHints != null) {
                            Object value = desktopHints.get(RenderingHints.KEY_TEXT_ANTIALIASING);
                            aaOn = Boolean.valueOf(
                                value != null && 
                                value != RenderingHints.VALUE_TEXT_ANTIALIAS_OFF &&  
                                value != RenderingHints.VALUE_TEXT_ANTIALIAS_DEFAULT
                            );
                            reason = "desktop hints"; //NOI18N
                        } else {
                            if (Utilities.isMac()) {
                                aaOn = Boolean.TRUE;
                                reason = "running on Mac OSX";//NOI18N
                            }
                        }
                    }
                }
                
                if (aaOn == null) {
                    LOG.fine("Text Antialiasing setting was not determined, using defaults."); //NOI18N
                    if (desktopHints != null) {
                        hints = new HashMap<Object, Object>(desktopHints);
                    } else {
                        hints = Collections.<Object, Object>singletonMap(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_DEFAULT);
                    }
                } else {
                    LOG.fine("Text Antialiasing was set " + (aaOn.booleanValue() ? "ON" : "OFF") + " by " + reason + "."); //NOI18N
                    if (desktopHints != null) {
                        hints = new  HashMap<Object, Object>(desktopHints);
                    } else {
                        hints = new  HashMap<Object, Object>();
                    }
                    hints.put(
                        RenderingHints.KEY_TEXT_ANTIALIASING, 
                        aaOn.booleanValue() ? RenderingHints.VALUE_TEXT_ANTIALIAS_ON : RenderingHints.VALUE_TEXT_ANTIALIAS_OFF
                    );
                }
            }
            
            return hints;
        }
    }
    
    public void addPropertyChangeListener(PropertyChangeListener pcl) {
        pcs.addPropertyChangeListener(pcl);
    }
    
    public void removePropertyChangeListener(PropertyChangeListener pcl) {
        pcs.removePropertyChangeListener(pcl);
    }
    
    // ----------------------------------------------------------------------
    // private implementation
    // ----------------------------------------------------------------------

    private static final Logger LOG = Logger.getLogger(EditorRenderingHints.class.getName());
    
    private static final Map<MimePath, EditorRenderingHints> CACHE = new  WeakHashMap<MimePath, EditorRenderingHints>();
    
    private final Preferences prefs;
    private final PreferenceChangeListener prefsListener = new PreferenceChangeListener() {
        public void preferenceChange(PreferenceChangeEvent evt) {
            hints = null;
            pcs.firePropertyChange(PROP_HINTS, null, null);
        }
    };
    
    private volatile Map<Object, Object> hints = null;
    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
    
    private EditorRenderingHints(Preferences prefs) {
        this.prefs = prefs;
        this.prefs.addPreferenceChangeListener(WeakListeners.create(PreferenceChangeListener.class, prefsListener, this.prefs));
    }
    
}
