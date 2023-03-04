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
package org.netbeans.modules.php.symfony2.ui.options;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import javax.swing.JComponent;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.php.api.util.UiUtils;
import org.netbeans.modules.php.api.validation.ValidationResult;
import org.netbeans.modules.php.symfony2.options.SymfonyOptions;
import org.netbeans.modules.php.symfony2.options.SymfonyOptionsValidator;
import org.netbeans.spi.options.OptionsPanelController;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 * Symfony 2/3 IDE options.
 */
@UiUtils.PhpOptionsPanelRegistration(
    id = SymfonyOptionsPanelController.ID,
    displayName = "#SymfonyOptionsPanelController.name",
//    toolTip = "#LBL_OptionsTooltip"
    position = 190
)
@NbBundle.Messages("SymfonyOptionsPanelController.name=Symfony 2/3")
public class SymfonyOptionsPanelController extends OptionsPanelController implements ChangeListener {

    static final String ID = "Symfony2"; // NOI18N
    public static final String OPTIONS_SUBPATH = UiUtils.FRAMEWORKS_AND_TOOLS_SUB_PATH + "/" + ID; // NOI18N

    private final PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);

    private SymfonyOptionsPanel symfony2OptionsPanel = null;
    private volatile boolean changed = false;
    private boolean firstOpening = true;


    public static String getOptionsPath() {
        return UiUtils.OPTIONS_PATH + "/" + OPTIONS_SUBPATH; // NOI18N
    }

    @Override
    public void update() {
        if (firstOpening || !isChanged()) { // if panel is not modified by the user and he switches back to this panel, set to default
            firstOpening = false;
            symfony2OptionsPanel.setUseInstaller(getOptions().isUseInstaller());
            symfony2OptionsPanel.setInstaller(getOptions().getInstaller());
            symfony2OptionsPanel.setSandbox(getOptions().getSandbox());
            symfony2OptionsPanel.setIgnoreCache(getOptions().getIgnoreCache());
        }

        changed = false;
    }

    @Override
    public void applyChanges() {
        getOptions().setUseInstaller(symfony2OptionsPanel.isUseInstaller());
        getOptions().setInstaller(symfony2OptionsPanel.getInstaller());
        getOptions().setSandbox(symfony2OptionsPanel.getSandbox());
        getOptions().setIgnoreCache(symfony2OptionsPanel.getIgnoreCache());

        changed = false;
    }

    @Override
    public void cancel() {
        if (isChanged()) { // if panel is modified by the user and options window closes, discard any changes
            symfony2OptionsPanel.setUseInstaller(getOptions().isUseInstaller());
            symfony2OptionsPanel.setInstaller(getOptions().getInstaller());
            symfony2OptionsPanel.setSandbox(getOptions().getSandbox());
            symfony2OptionsPanel.setIgnoreCache(getOptions().getIgnoreCache());
        }
    }

    @Override
    public boolean isValid() {
        ValidationResult result = new SymfonyOptionsValidator()
                .validate(symfony2OptionsPanel.isUseInstaller(), symfony2OptionsPanel.getInstaller(), symfony2OptionsPanel.getSandbox())
                .getResult();
        // errors
        if (result.hasErrors()) {
            symfony2OptionsPanel.setError(result.getErrors().get(0).getMessage());
            return false;
        }
        // warnings
        if (result.hasWarnings()) {
            symfony2OptionsPanel.setWarning(result.getWarnings().get(0).getMessage());
            return true;
        }
        // everything ok
        symfony2OptionsPanel.setError(" "); // NOI18N
        return true;
    }

    @Override
    public boolean isChanged() {
        if (getOptions().isUseInstaller() != symfony2OptionsPanel.isUseInstaller()) {
            return true;
        }
        String saved = getOptions().getInstaller();
        String current = symfony2OptionsPanel.getInstaller().trim();
        if (saved == null ? !current.isEmpty() : !saved.equals(current)) {
            return true;
        }
        saved = getOptions().getSandbox();
        current = symfony2OptionsPanel.getSandbox().trim();
        if (saved == null ? !current.isEmpty() : !saved.equals(current)) {
            return true;
        }
        return getOptions().getIgnoreCache() != symfony2OptionsPanel.getIgnoreCache();
    }

    @Override
    public JComponent getComponent(Lookup masterLookup) {
        if (symfony2OptionsPanel == null) {
            symfony2OptionsPanel = new SymfonyOptionsPanel();
            symfony2OptionsPanel.addChangeListener(this);
        }
        return symfony2OptionsPanel;
    }

    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx("org.netbeans.modules.php.symfony2.options.Symfony2Options"); // NOI18N
    }

    @Override
    public void addPropertyChangeListener(PropertyChangeListener l) {
        propertyChangeSupport.addPropertyChangeListener(l);
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener l) {
        propertyChangeSupport.removePropertyChangeListener(l);
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        if (!changed) {
            changed = true;
            propertyChangeSupport.firePropertyChange(OptionsPanelController.PROP_CHANGED, false, true);
        }
        propertyChangeSupport.firePropertyChange(OptionsPanelController.PROP_VALID, null, null);
    }

    private SymfonyOptions getOptions() {
        return SymfonyOptions.getInstance();
    }

}
