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
package org.netbeans.modules.php.doctrine2.ui.options;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import javax.swing.JComponent;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.php.api.util.FileUtils;
import org.netbeans.modules.php.api.util.UiUtils;
import org.netbeans.modules.php.doctrine2.options.Doctrine2Options;
import org.netbeans.spi.options.OptionsPanelController;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 * Doctrine2 IDE options.
 */
@UiUtils.PhpOptionsPanelRegistration(
    id=Doctrine2OptionsPanelController.ID,
    displayName="#LBL_PHPDoctrineOptionsName",
//    toolTip="#LBL_OptionsTooltip"
    position=700
)
public class Doctrine2OptionsPanelController extends OptionsPanelController implements ChangeListener {

    static final String ID = "Doctrine2"; // NOI18N
    public static final String OPTIONS_SUBPATH = UiUtils.FRAMEWORKS_AND_TOOLS_SUB_PATH+"/"+ID; // NOI18N

    private final PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);

    private Doctrine2OptionsPanel doctrine2OptionsPanel = null;
    private volatile boolean changed = false;
    private boolean firstOpening = true;


    public static String getOptionsPath() {
        return UiUtils.OPTIONS_PATH + "/" + OPTIONS_SUBPATH; // NOI18N
    }

    @Override
    public void update() {
        if(firstOpening || !isChanged()) { // if panel is not modified by the user and he switches back to this panel, set to default
            firstOpening = false;
            doctrine2OptionsPanel.setScript(getOptions().getScript());
        }

        changed = false;
    }

    @Override
    public void applyChanges() {
        getOptions().setScript(doctrine2OptionsPanel.getScript());

        changed = false;
    }

    @Override
    public void cancel() {
        if(isChanged()) { // if panel is modified by the user and options window closes, discard any changes
            doctrine2OptionsPanel.setScript(getOptions().getScript());
        }
    }

    @Override
    public boolean isValid() {
        // warnings
        String warning = validateScript(doctrine2OptionsPanel.getScript());
        if (warning != null) {
            doctrine2OptionsPanel.setWarning(warning);
            return true;
        }

        // everything ok
        doctrine2OptionsPanel.setError(" "); // NOI18N
        return true;
    }

    @NbBundle.Messages("Doctrine2OptionsPanelController.script=Doctrine2 script")
    public static String validateScript(String script) {
        return FileUtils.validateFile(Bundle.Doctrine2OptionsPanelController_script(), script, false);
    }

    @Override
    public boolean isChanged() {
        String saved = getOptions().getScript();
        String current = doctrine2OptionsPanel.getScript().trim();
        return saved == null ? !current.isEmpty() : !saved.equals(current);
    }

    @Override
    public JComponent getComponent(Lookup masterLookup) {
        if (doctrine2OptionsPanel == null) {
            doctrine2OptionsPanel = new Doctrine2OptionsPanel();
            doctrine2OptionsPanel.addChangeListener(this);
        }
        return doctrine2OptionsPanel;
    }

    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx("org.netbeans.modules.php.doctrine2.Options"); // NOI18N
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

    private Doctrine2Options getOptions() {
        return Doctrine2Options.getInstance();
    }

}
