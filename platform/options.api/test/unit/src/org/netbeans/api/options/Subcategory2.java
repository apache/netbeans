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

package org.netbeans.api.options;

import java.beans.PropertyChangeListener;
import javax.swing.JComponent;
import javax.swing.JLabel;
import org.netbeans.spi.options.AdvancedOption;
import org.netbeans.spi.options.OptionsPanelController;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;

/**
 */
public class Subcategory2 extends AdvancedOption {
    
    public static final String DISPLAY_NAME = "Subcategory2 display name";
    public static String currentSubpath;

    @SuppressWarnings("deprecation")
    public Subcategory2() {}

    @Override
    public String getDisplayName() {
        return DISPLAY_NAME;
    }

    @Override
    public String getTooltip() {
        return "Subcategory2 tooltip";
    }
    
    /** Needed just for tests. */
    public static String getSubpath() {
        return currentSubpath;
    }

    @Override
    public OptionsPanelController create() {
        return new OptionsPanelController() {

            @Override
            public void update() {
            }

            @Override
            public void applyChanges() {
            }

            @Override
            public void cancel() {
            }

            @Override
            public boolean isValid() {
                return true;
            }

            @Override
            public boolean isChanged() {
                return false;
            }

            @Override
            public JComponent getComponent(Lookup masterLookup) {
                return new JLabel();
            }

            @Override
            public void setCurrentSubcategory(String subpath) {
                currentSubpath = subpath;
            }
            
            @Override
            public HelpCtx getHelpCtx() {
                return null;
            }

            @Override
            public void addPropertyChangeListener(PropertyChangeListener l) {
            }

            @Override
            public void removePropertyChangeListener(PropertyChangeListener l) {
            }
        };
    }

}
