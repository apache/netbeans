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
    private static final int DEFAULT_DIVIDER_LOCATION_HORIZONTAL = 500;
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
