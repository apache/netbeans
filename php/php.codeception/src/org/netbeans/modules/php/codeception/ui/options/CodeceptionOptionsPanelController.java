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
package org.netbeans.modules.php.codeception.ui.options;

import java.awt.EventQueue;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.php.api.util.UiUtils;
import org.netbeans.modules.php.api.validation.ValidationResult;
import org.netbeans.modules.php.codeception.options.CodeceptionOptions;
import org.netbeans.modules.php.codeception.options.CodeceptionOptionsValidator;
import org.netbeans.spi.options.OptionsPanelController;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;

@UiUtils.PhpOptionsPanelRegistration(
        id = CodeceptionOptionsPanelController.ID,
        displayName = "#CodeceptionOptionsPanelController.displayName",
        //    toolTip="#LBL_OptionsTooltip"
        position = 800
)
@org.openide.util.NbBundle.Messages("CodeceptionOptionsPanelController.displayName=Codeception")
public final class CodeceptionOptionsPanelController extends OptionsPanelController implements ChangeListener {

    static final String ID = "Codeception"; // NOI18N
    public static final String OPTIONS_SUB_PATH = UiUtils.FRAMEWORKS_AND_TOOLS_SUB_PATH + "/" + ID; // NOI18N
    public static final String OPTIONS_PATH = UiUtils.OPTIONS_PATH + "/" + OPTIONS_SUB_PATH; // NOI18N

    private final PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);

    // @GuardedBy("EDT")
    private CodeceptionOptionsPanel codeceptionOptionsPanel = null;
    private volatile boolean changed = false;
    private boolean firstOpening = true;

    @Override
    public void update() {
        assert EventQueue.isDispatchThread();
        if (firstOpening || !isChanged()) { // if panel is not modified by the user and he switches back to this panel, set to default
            firstOpening = false;
            getCodeceptionOptionsPanel().setCodeception(getCodeceptionOptions().getCodeceptionPath());
        }
        changed = false;
    }

    @Override
    public void applyChanges() {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                getCodeceptionOptions().setCodeceptionPath(getCodeceptionOptionsPanel().getCodeception());
                changed = false;
            }
        });
    }

    @Override
    public void cancel() {
        if (isChanged()) { // if panel is modified by the user and options window closes, discard any changes
            getCodeceptionOptionsPanel().setCodeception(getCodeceptionOptions().getCodeceptionPath());
        }
    }

    @Override
    public boolean isValid() {
        assert EventQueue.isDispatchThread();
        CodeceptionOptionsPanel panel = getCodeceptionOptionsPanel();
        ValidationResult result = new CodeceptionOptionsValidator()
                .validateCodeceptionPath(panel.getCodeception())
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
        String saved = getCodeceptionOptions().getCodeceptionPath();
        String current = getCodeceptionOptionsPanel().getCodeception().trim();
        return saved == null ? !current.isEmpty() : !saved.equals(current);
    }

    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx("org.netbeans.modules.php.codeception.ui.options.CodeceptionOptionsPanelController"); // NOI18N
    }

    @Override
    public JComponent getComponent(Lookup masterLookup) {
        assert EventQueue.isDispatchThread();
        return getCodeceptionOptionsPanel();
    }

    @Override
    public void addPropertyChangeListener(PropertyChangeListener l) {
        propertyChangeSupport.addPropertyChangeListener(l);
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener l) {
        propertyChangeSupport.removePropertyChangeListener(l);
    }

    private CodeceptionOptionsPanel getCodeceptionOptionsPanel() {
        assert EventQueue.isDispatchThread();
        if (codeceptionOptionsPanel == null) {
            codeceptionOptionsPanel = new CodeceptionOptionsPanel();
            codeceptionOptionsPanel.addChangeListener(this);
        }
        return codeceptionOptionsPanel;
    }

    private CodeceptionOptions getCodeceptionOptions() {
        return CodeceptionOptions.getInstance();
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        if (!changed) {
            changed = true;
            propertyChangeSupport.firePropertyChange(OptionsPanelController.PROP_CHANGED, false, true);
        }
        propertyChangeSupport.firePropertyChange(OptionsPanelController.PROP_VALID, null, null);
    }

}
