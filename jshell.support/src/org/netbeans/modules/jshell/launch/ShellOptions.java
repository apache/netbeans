/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2016 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2016 Sun Microsystems, Inc.
 */
package org.netbeans.modules.jshell.launch;

import java.util.prefs.Preferences;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.api.java.platform.Specification;
import org.openide.modules.SpecificationVersion;
import org.openide.util.NbPreferences;

/**
 *
 * @author sdedic
 */
public final class ShellOptions {
    public static final SpecificationVersion MIN_SPEC_VERSION = new SpecificationVersion("1.6"); // NOI18N
    
    private static final String PREF_PLATFORM = "launchPlatformId"; // NOI18N
    private static final String PREF_OPEN_CONSOLE = "openConsoleOnLaunch"; // NOI18N
    private static final String PREF_REUSE_CONSOLES = "reuseDeadConsoles"; // NOI18N
    private static final String PREF_HISTORY_LINES = "historyLines"; // NOI18N
    private Preferences prefs;
    
    private Preferences prefs() {
        if (prefs != null) {
            return prefs;
        }
        prefs = NbPreferences.forModule(ShellOptions.class);
        return prefs;
    }
    
    public int getHistoryLines() {
        return prefs().getInt(PREF_HISTORY_LINES, 50);
    }
    
    public void setHistoryLines(int val) {
        prefs().putInt(PREF_HISTORY_LINES, val);
    }
    
    public void setOpenConsole(boolean open) {
        prefs().putBoolean(PREF_OPEN_CONSOLE, open);
    }
    
    public void setReuseDeadConsoles(boolean reuse) {
        prefs().putBoolean(PREF_REUSE_CONSOLES, reuse);
    }
    
    public boolean isOpenConsole() {
        return prefs().getBoolean(PREF_OPEN_CONSOLE, false);
    }
    
    public boolean isReuseDeadConsoles() {
        return prefs().getBoolean(PREF_REUSE_CONSOLES, true);
    }
    
    public boolean isPlatformSet() {
        return prefs().get(PREF_PLATFORM, null) != null;
    }
    
    public void setSelectedPlatform(JavaPlatform p) {
        prefs().put(PREF_PLATFORM, p.getDisplayName());
    }

    public boolean setSelectedPlatform(String name) {
        JavaPlatform[] candidates;
        candidates = JavaPlatformManager.getDefault().getPlatforms(name, null);
        if (candidates != null && candidates.length == 1) {
            prefs().put(PREF_PLATFORM, name);
            return true;
        } else {
            return false;
        }
    }
    
    public JavaPlatform getSelectedPlatform() {
        String platformId = prefs().get(PREF_PLATFORM, null);
        if (platformId == null) {
            return getDefaultPlatform();
        }
        JavaPlatform[] candidates;
        candidates = JavaPlatformManager.getDefault().getPlatforms(platformId, null);
        if (candidates == null || candidates.length == 0) {
            return getDefaultPlatform();
        } else {
            return candidates[0];
        }
    }
    
    public JavaPlatform getDefaultPlatform() {
        JavaPlatform check = JavaPlatform.getDefault();
        if (check.getSpecification().getVersion().compareTo(MIN_SPEC_VERSION) >= 0) {
            return check;
        }
        JavaPlatform[] candidates = JavaPlatformManager.getDefault().getPlatforms(null, 
            new Specification("J2SE", MIN_SPEC_VERSION));
        return candidates == null || candidates.length == 0 ? null : candidates[0];
    }
    
    public static ShellOptions get() {
        return new ShellOptions();
    }
    
    public boolean accepts(JavaPlatform p) {
        return p.getSpecification().getVersion().compareTo(MIN_SPEC_VERSION) >= 0;
    }
}
