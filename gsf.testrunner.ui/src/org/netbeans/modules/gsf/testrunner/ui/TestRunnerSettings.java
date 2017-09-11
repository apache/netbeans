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
package org.netbeans.modules.gsf.testrunner.ui;

import java.util.prefs.Preferences;
import javax.swing.JSplitPane;
import org.openide.util.NbPreferences;

/**
 * Settings for the test runner. 
 * 
 * @author Erno Mononen
 */
public final class TestRunnerSettings {

    private static final String RESULTS_SPLITPANE_DIVIDER_VERTICAL = "resultsSplitDividerVertical"; //NOI18N
    private static final String RESULTS_SPLITPANE_DIVIDER_HORIZONTAL = "resultsSplitDividerHorizontal"; //NOI18N
    private static final String RESULTS_SPLITPANE_ORIENTATION = "resultsSplitOrientation"; //NOI18N
    private static final int DEFAULT_DIVIDER_LOCATION_VERTICAL = 120;
    private static final int DEFAULT_DIVIDER_LOCATION_HORIZONTAL = 300;
    private static final int DEFAULT_DIVIDER_ORIENTATION = JSplitPane.HORIZONTAL_SPLIT;

    private static final TestRunnerSettings INSTANCE = new TestRunnerSettings();

    private TestRunnerSettings() {
    }

    public static TestRunnerSettings getDefault() {
        return INSTANCE;
    }
    
    private Preferences getPreferences() {
        return NbPreferences.forModule(TestRunnerSettings.class);
    }

    public DividerSettings getDividerSettings(Integer orientation) {
        if (orientation == null) {
            orientation = getResultsSplitPaneOrientation();
        }
        return new DividerSettings(orientation, getResultsSplitPaneDivider(orientation));
    }
    
    public void setDividerSettings(DividerSettings settings) {
        setResultsSplitPaneOrientation(settings.getOrientation());
        setResultsSplitPaneDivider(settings.getLocation(), settings.getOrientation());
    }
    
    private int getResultsSplitPaneDivider(int orientation) {        
        if (JSplitPane.VERTICAL_SPLIT == orientation) {
            return getPreferences().getInt(RESULTS_SPLITPANE_DIVIDER_VERTICAL, DEFAULT_DIVIDER_LOCATION_VERTICAL);
        } else {
            return getPreferences().getInt(RESULTS_SPLITPANE_DIVIDER_HORIZONTAL, DEFAULT_DIVIDER_LOCATION_HORIZONTAL);
        }
    }

    private void setResultsSplitPaneDivider(int dividerLocation, int orientation) {
        if (JSplitPane.VERTICAL_SPLIT == orientation) {
            getPreferences().putInt(RESULTS_SPLITPANE_DIVIDER_VERTICAL, dividerLocation);
        } else {
            getPreferences().putInt(RESULTS_SPLITPANE_DIVIDER_HORIZONTAL, dividerLocation);
        }
    }    
    
    private int getResultsSplitPaneOrientation() {        
        return getPreferences().getInt(RESULTS_SPLITPANE_ORIENTATION, DEFAULT_DIVIDER_ORIENTATION);
    }

    private void setResultsSplitPaneOrientation(int dividerOrientation) {
        getPreferences().putInt(RESULTS_SPLITPANE_ORIENTATION, dividerOrientation);
    }    
    
    public static final class DividerSettings {
        
        private final int orientation;
        private final int location;

        public DividerSettings(int orientation, int location) {
            assert orientation == JSplitPane.HORIZONTAL_SPLIT || orientation == JSplitPane.VERTICAL_SPLIT;
            this.orientation = orientation;
            this.location = location;
        }

        public int getOrientation() {
            return orientation;
        }

        public int getLocation() {
            return location;
        }
        
    }
}
