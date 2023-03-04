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
package org.netbeans.modules.options.colors;

import javax.swing.JComponent;
import org.netbeans.api.options.OptionsDisplayer;
import org.netbeans.modules.options.colors.spi.FontsColorsController;
import org.netbeans.spi.options.OptionsPanelController;

/**
 *
 * @since 1.39
 */
@OptionsPanelController.Keywords(keywords={"#KW_HighlightPanel"}, location=OptionsDisplayer.FONTSANDCOLORS, tabTitle= "#Editor_tab.displayName")
public class HighlightingPanelController implements FontsColorsController{

    private HighlightingPanel component = null;
    
    @Override
    public void update(ColorModel model) {
        getHighlightingPanel().update(model);
    }

    @Override
    public void setCurrentProfile(String profile) {
        getHighlightingPanel().setCurrentProfile(profile);
    }

    @Override
    public void deleteProfile(String profile) {
        getHighlightingPanel().deleteProfile(profile);
    }

    @Override
    public void applyChanges() {
        getHighlightingPanel().applyChanges();
    }

    @Override
    public void cancel() {
        getHighlightingPanel().cancel();
    }

    @Override
    public boolean isChanged() {
        return getHighlightingPanel().isChanged();
    }

    @Override
    public JComponent getComponent() {
        return getHighlightingPanel();
    }

    private synchronized HighlightingPanel getHighlightingPanel() {
        if (component == null) {
            component = new HighlightingPanel();
        }
        return component;
    }

}
