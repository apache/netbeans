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
package org.netbeans.modules.diff.options;

import org.netbeans.modules.diff.*;
import org.netbeans.modules.diff.builtin.provider.BuiltInDiffProvider;
import org.netbeans.spi.options.OptionsPanelController;
import org.openide.util.Lookup;
import org.openide.util.HelpCtx;

import javax.swing.*;
import java.beans.PropertyChangeListener;

/**
 * Diff module's Options Controller.
 * 
 * @author Maros Sandor
 */
@OptionsPanelController.SubRegistration(
    id=DiffOptionsController.OPTIONS_SUBPATH,
    displayName="#LBL_DiffOptions",
    keywords="#KW_DiffOptions",
    keywordsCategory="Advanced/Diff"
//    toolTip="#TT_DiffOptions"
)
public class DiffOptionsController extends OptionsPanelController {

    public static final String OPTIONS_SUBPATH = "Diff";

    private DiffOptionsPanel panel;
    
    @Override
    public void update() {
        panel.getIgnoreWhitespace().setSelected(DiffModuleConfig.getDefault().getOptions().ignoreLeadingAndtrailingWhitespace);
        panel.getIgnoreInnerWhitespace().setSelected(DiffModuleConfig.getDefault().getOptions().ignoreInnerWhitespace);
        panel.getIgnoreCase().setSelected(DiffModuleConfig.getDefault().getOptions().ignoreCase);
        panel.setChanged(false);
    }

    @Override
    public void applyChanges() {
        BuiltInDiffProvider.Options options = new BuiltInDiffProvider.Options();
        options.ignoreLeadingAndtrailingWhitespace = panel.getIgnoreWhitespace().isSelected();
        options.ignoreInnerWhitespace = panel.getIgnoreInnerWhitespace().isSelected();
        options.ignoreCase = panel.getIgnoreCase().isSelected();
        DiffModuleConfig.getDefault().setOptions(options);
        panel.setChanged(false);
    }

    @Override
    public void cancel() {
        // nothing to do
    }

    @Override
    public boolean isValid() {
        return true;
    }

    @Override
    public boolean isChanged() {
        return panel.isChanged();
    }

    @Override
    public JComponent getComponent(Lookup masterLookup) {
        if (panel == null) {
            panel = new DiffOptionsPanel(); 
        }
        return panel;
    }

    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx("org.netbeans.modules.diff.options.DiffOptionsController"); //NOI18N
    }

    @Override
    public void addPropertyChangeListener(PropertyChangeListener l) {
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener l) {
    }
}
