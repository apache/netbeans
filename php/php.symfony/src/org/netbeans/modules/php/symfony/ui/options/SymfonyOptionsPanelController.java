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

package org.netbeans.modules.php.symfony.ui.options;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import javax.swing.JComponent;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.php.api.util.UiUtils;
import org.netbeans.modules.php.symfony.SymfonyScript;
import org.netbeans.spi.options.OptionsPanelController;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;

/**
 * @author Tomas Mysik
 */
@UiUtils.PhpOptionsPanelRegistration(
    id=SymfonyScript.OPTIONS_ID,
    displayName="#LBL_PHPSymfonyOptionsName",
//    toolTip="#LBL_OptionsTooltip"
    position=200
)
public class SymfonyOptionsPanelController extends OptionsPanelController implements ChangeListener {
    private final PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);

    private SymfonyOptionsPanel symfonyOptionsPanel = null;
    private volatile boolean changed = false;
    private boolean firstOpening = true;

    @Override
    public void update() {
        if(firstOpening || !isChanged()) { // if panel is not modified by the user and he switches back to this panel, set to default
            firstOpening = false;
            symfonyOptionsPanel.setSymfony(getOptions().getSymfony());
            symfonyOptionsPanel.setIgnoreCache(getOptions().getIgnoreCache());
            symfonyOptionsPanel.setDefaultParamsForProject(getOptions().getDefaultParamsForProject());
            symfonyOptionsPanel.setDefaultParamsForApps(getOptions().getDefaultParamsForApps());
        }

        changed = false;
    }

    @Override
    public void applyChanges() {
        getOptions().setSymfony(symfonyOptionsPanel.getSymfony());
        getOptions().setIgnoreCache(symfonyOptionsPanel.getIgnoreCache());
        getOptions().setDefaultParamsForProject(symfonyOptionsPanel.getDefaultParamsForProject());
        getOptions().setDefaultParamsForApps(symfonyOptionsPanel.getDefaultParamsForApps());

        changed = false;
    }

    @Override
    public void cancel() {
        if (isChanged()) { // if panel is modified by the user and options window closes, discard any changes
            symfonyOptionsPanel.setSymfony(getOptions().getSymfony());
            symfonyOptionsPanel.setIgnoreCache(getOptions().getIgnoreCache());
            symfonyOptionsPanel.setDefaultParamsForProject(getOptions().getDefaultParamsForProject());
            symfonyOptionsPanel.setDefaultParamsForApps(getOptions().getDefaultParamsForApps());
        }
    }

    @Override
    public boolean isValid() {
        // warnings
        String warning = SymfonyScript.validate(symfonyOptionsPanel.getSymfony());
        if (warning != null) {
            symfonyOptionsPanel.setWarning(warning);
            return true;
        }

        // everything ok
        symfonyOptionsPanel.setError(" "); // NOI18N
        return true;
    }

    @Override
    public boolean isChanged() {
        String saved = getOptions().getSymfony();
        String current = symfonyOptionsPanel.getSymfony().trim();
        if (saved == null ? !current.isEmpty() : !saved.equals(current)) {
            return true;
        }
        return getOptions().getIgnoreCache() != symfonyOptionsPanel.getIgnoreCache()
                || !getOptions().getDefaultParamsForProject().equals(symfonyOptionsPanel.getDefaultParamsForProject().trim())
                || !getOptions().getDefaultParamsForApps().equals(symfonyOptionsPanel.getDefaultParamsForApps().trim());
    }

    @Override
    public JComponent getComponent(Lookup masterLookup) {
        if (symfonyOptionsPanel == null) {
            symfonyOptionsPanel = new SymfonyOptionsPanel();
            symfonyOptionsPanel.addChangeListener(this);
        }
        return symfonyOptionsPanel;
    }

    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx("org.netbeans.modules.php.symfony.ui.options.SymfonyOptions"); // NOI18N
    }

    @Override
    public void addPropertyChangeListener(PropertyChangeListener l) {
        propertyChangeSupport.addPropertyChangeListener(l);
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener l) {
        propertyChangeSupport.removePropertyChangeListener(l);
    }

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
