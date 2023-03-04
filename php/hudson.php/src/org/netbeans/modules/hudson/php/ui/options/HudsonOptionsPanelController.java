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
package org.netbeans.modules.hudson.php.ui.options;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import javax.swing.JComponent;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.hudson.php.options.HudsonOptions;
import org.netbeans.modules.hudson.php.options.HudsonOptionsValidator;
import org.netbeans.modules.php.api.util.UiUtils;
import org.netbeans.spi.options.OptionsPanelController;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 * IDE options controller for Hudson PHP.
 */
@OptionsPanelController.SubRegistration(
    location=UiUtils.OPTIONS_PATH,
    id=HudsonOptionsPanelController.OPTIONS_SUBPATH,
    displayName="#LBL_HudsonPHPOptionsName",
//    toolTip="#LBL_OptionsTooltip"
    position=160
)
public class HudsonOptionsPanelController extends OptionsPanelController implements ChangeListener {

    public static final String OPTIONS_SUBPATH = "Hudson"; // NOI18N

    private final PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);

    private HudsonOptionsPanel hudsonOptionsPanel = null;
    private volatile boolean changed = false;


    public static String getOptionsPath() {
        return UiUtils.OPTIONS_PATH + "/" + OPTIONS_SUBPATH; // NOI18N
    }

    @Override
    public void update() {
        hudsonOptionsPanel.setBuildXml(getOptions().getBuildXml());
        hudsonOptionsPanel.setJobConfig(getOptions().getJobConfig());
        hudsonOptionsPanel.setPhpUnitConfig(getOptions().getPhpUnitConfig());

        changed = false;
    }

    @Override
    public void applyChanges() {
        getOptions().setBuildXml(hudsonOptionsPanel.getBuildXml());
        getOptions().setJobConfig(hudsonOptionsPanel.getJobConfig());
        getOptions().setPhpUnitConfig(hudsonOptionsPanel.getPhpUnitConfig());

        changed = false;
    }

    @Override
    public void cancel() {
    }

    @NbBundle.Messages("HudsonOptionsPanelController.warning.existingFiles=If build script or PHPUnit config file exists in project, it will be preferred.")
    @Override
    public boolean isValid() {
        // warnings
        String warning = HudsonOptionsValidator.validate(hudsonOptionsPanel.getBuildXml(),
                hudsonOptionsPanel.getJobConfig(), hudsonOptionsPanel.getPhpUnitConfig());
        if (warning != null) {
            hudsonOptionsPanel.setWarning(warning);
            return true;
        }
        // everything ok
        hudsonOptionsPanel.setWarning(Bundle.HudsonOptionsPanelController_warning_existingFiles());
        return true;
    }

    @Override
    public boolean isChanged() {
        String saved = getOptions().getBuildXml();
        String current = hudsonOptionsPanel.getBuildXml().trim();
        if (saved == null ? !current.isEmpty() : !saved.equals(current)) {
            return true;
        }
        saved = getOptions().getJobConfig();
        current = hudsonOptionsPanel.getJobConfig().trim();
        if (saved == null ? !current.isEmpty() : !saved.equals(current)) {
            return true;
        }
        saved = getOptions().getPhpUnitConfig();
        current = hudsonOptionsPanel.getPhpUnitConfig().trim();
        return saved == null ? !current.isEmpty() : !saved.equals(current);
    }

    @Override
    public JComponent getComponent(Lookup masterLookup) {
        if (hudsonOptionsPanel == null) {
            hudsonOptionsPanel = new HudsonOptionsPanel();
            hudsonOptionsPanel.addChangeListener(this);
        }
        return hudsonOptionsPanel;
    }

    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx("org.netbeans.modules.hudson.php.ui.options.Options"); // NOI18N
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

    private HudsonOptions getOptions() {
        return HudsonOptions.getInstance();
    }

}
