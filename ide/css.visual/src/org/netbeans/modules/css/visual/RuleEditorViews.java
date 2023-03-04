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
package org.netbeans.modules.css.visual;

import javax.swing.JToggleButton;
import org.netbeans.modules.css.visual.RuleEditorPanel;
import org.netbeans.modules.css.visual.api.ViewMode;

/**
 *
 * @author mfukala@netbeans.org
 */
public final class RuleEditorViews {

    private final JToggleButton updatedOnlyToggleButton;
    private final JToggleButton categorizedToggleButton;
    private final JToggleButton allToggleButton;
    
    private final RuleEditorPanel ruleEditorPanel;

    public RuleEditorViews(RuleEditorPanel ruleEditorPanel) {
        this.ruleEditorPanel = ruleEditorPanel;

        updatedOnlyToggleButton = new JToggleButton(new ViewActionSupport.UpdatedOnlyViewAction(this));
        updatedOnlyToggleButton.setToolTipText(updatedOnlyToggleButton.getText());
        updatedOnlyToggleButton.setText(null);
        updatedOnlyToggleButton.setSelected(getViewMode() == ViewMode.UPDATED_ONLY);
        updatedOnlyToggleButton.setFocusable(false);

        categorizedToggleButton = new JToggleButton(new ViewActionSupport.CategorizedViewAction(this));
        categorizedToggleButton.setToolTipText(categorizedToggleButton.getText());
        categorizedToggleButton.setText(null);
        categorizedToggleButton.setSelected(getViewMode() == ViewMode.CATEGORIZED);
        categorizedToggleButton.setFocusable(false);

        allToggleButton = new JToggleButton(new ViewActionSupport.AllViewAction(this));
        allToggleButton.setToolTipText(allToggleButton.getText());
        allToggleButton.setText(null);
        allToggleButton.setSelected(getViewMode() == ViewMode.ALL);
        allToggleButton.setFocusable(false);

    }

    public JToggleButton getUpdatedOnlyToggleButton() {
        return updatedOnlyToggleButton;
    }

    public JToggleButton getCategorizedToggleButton() {
        return categorizedToggleButton;
    }

    public JToggleButton getAllToggleButton() {
        return allToggleButton;
    }

    void setViewMode(ViewMode mode) {
        ruleEditorPanel.setViewMode(mode);

        //update the toggle bottons
        updatedOnlyToggleButton.setSelected(mode == ViewMode.UPDATED_ONLY);
        categorizedToggleButton.setSelected(mode == ViewMode.CATEGORIZED);
        allToggleButton.setSelected(mode == ViewMode.ALL);
    }

    ViewMode getViewMode() {
        return ruleEditorPanel.getViewMode();
    }
}
