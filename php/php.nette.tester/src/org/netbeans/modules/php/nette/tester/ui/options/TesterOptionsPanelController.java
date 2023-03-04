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
package org.netbeans.modules.php.nette.tester.ui.options;

import java.awt.EventQueue;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import javax.swing.JComponent;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.php.api.util.UiUtils;
import org.netbeans.modules.php.api.validation.ValidationResult;
import org.netbeans.modules.php.nette.tester.options.TesterOptions;
import org.netbeans.modules.php.nette.tester.options.TesterOptionsValidator;
import org.netbeans.spi.options.OptionsPanelController;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;

@UiUtils.PhpOptionsPanelRegistration(
    id=TesterOptionsPanelController.ID,
    displayName="#TesterOptionsPanel.name",
//    toolTip="#LBL_OptionsTooltip"
    position=410
)
public class TesterOptionsPanelController extends OptionsPanelController implements ChangeListener {

    static final String ID = "nette-tester"; // NOI18N
    public static final String OPTIONS_SUB_PATH = UiUtils.FRAMEWORKS_AND_TOOLS_SUB_PATH + "/" + ID; // NOI18N
    public static final String OPTIONS_PATH = UiUtils.OPTIONS_PATH + "/" + OPTIONS_SUB_PATH; // NOI18N

    private final PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);

    // @GuardedBy("EDT")
    private TesterOptionsPanel testerOptionsPanel = null;
    private volatile boolean changed = false;
    private boolean firstOpening = true;


    @Override
    public void update() {
        assert EventQueue.isDispatchThread();
        if (firstOpening || !isChanged()) { // if panel is not modified by the user and he switches back to this panel, set to default
            firstOpening = false;
            getPanel().setTesterPath(getTesterOptions().getTesterPath());
            getPanel().setPhpIniPath(getTesterOptions().getPhpIniPath());
            getPanel().setBinaryExecutable(getTesterOptions().getBinaryExecutable());
        }
        changed = false;
    }

    @Override
    public void applyChanges() {
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                getTesterOptions().setTesterPath(getPanel().getTesterPath());
                getTesterOptions().setPhpIniPath(getPanel().getPhpIniPath());
                getTesterOptions().setBinaryExecutable(getPanel().getBinaryExecutable());
                changed = false;
            }
        });
    }

    @Override
    public void cancel() {
        if (isChanged()) { // if panel is modified by the user and options window closes, discard any changes
            getPanel().setTesterPath(getTesterOptions().getTesterPath());
            getPanel().setPhpIniPath(getTesterOptions().getPhpIniPath());
            getPanel().setBinaryExecutable(getTesterOptions().getBinaryExecutable());
        }
    }

    @Override
    public boolean isValid() {
        assert EventQueue.isDispatchThread();
        TesterOptionsPanel panel = getPanel();
        ValidationResult result = new TesterOptionsValidator()
                .validate(panel.getTesterPath(), panel.getPhpIniPath())
                .getResult();
        // errors
        if (result.hasErrors()) {
            panel.setError(result.getErrors().get(0).getMessage());
            return false;
        }
        // warnings
        if (result.hasWarnings()) {
            panel.setWarning(result.getWarnings().get(0).getMessage());
            return true;
        }
        // everything ok
        panel.setError(" "); // NOI18N
        return true;
    }

    @Override
    public boolean isChanged() {
        String saved = getTesterOptions().getTesterPath();
        String current = getPanel().getTesterPath().trim();
        if (saved == null ? !current.isEmpty() : !saved.equals(current)) {
            return true;
        }
        saved = getTesterOptions().getPhpIniPath();
        current = getPanel().getPhpIniPath().trim();
        if (saved == null ? !current.isEmpty() : !saved.equals(current)) {
            return true;
        }
        saved = getTesterOptions().getBinaryExecutable();
        current = getPanel().getBinaryExecutable();
        if (current != null) {
            current = current.trim();
        }
        return saved == null ? current != null && !current.isEmpty() : !saved.equals(current);
    }

    @Override
    public JComponent getComponent(Lookup masterLookup) {
        assert EventQueue.isDispatchThread();
        return getPanel();
    }

    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx("org.netbeans.modules.php.nette.tester.ui.options.TesterOptionsPanelController"); // NOI18N
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

    private TesterOptionsPanel getPanel() {
        assert EventQueue.isDispatchThread();
        if (testerOptionsPanel == null) {
            testerOptionsPanel = new TesterOptionsPanel();
            testerOptionsPanel.addChangeListener(this);
        }
        return testerOptionsPanel;
    }

    private TesterOptions getTesterOptions() {
        return TesterOptions.getInstance();
    }

}
