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

package org.netbeans.modules.php.phpunit.ui.options;

import java.awt.EventQueue;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.php.api.util.UiUtils;
import org.netbeans.modules.php.api.validation.ValidationResult;
import org.netbeans.modules.php.phpunit.commands.SkeletonGenerator;
import org.netbeans.modules.php.phpunit.options.PhpUnitOptions;
import org.netbeans.modules.php.phpunit.options.PhpUnitOptionsValidator;
import org.netbeans.spi.options.OptionsPanelController;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;

@UiUtils.PhpOptionsPanelRegistration(
    id=PhpUnitOptionsPanelController.ID,
    displayName="#PhpUnitOptionsPanel.name",
//    toolTip="#LBL_OptionsTooltip"
    position=150
)
public class PhpUnitOptionsPanelController extends OptionsPanelController implements ChangeListener {

    static final String ID = "PhpUnit"; // NOI18N
    public static final String OPTIONS_SUB_PATH = UiUtils.FRAMEWORKS_AND_TOOLS_SUB_PATH+"/"+ID; // NOI18N
    public static final String OPTIONS_PATH = UiUtils.OPTIONS_PATH + "/" + OPTIONS_SUB_PATH; // NOI18N

    private final PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);

    // @GuardedBy("EDT")
    private PhpUnitOptionsPanel phpUnitOptionsPanel = null;
    private volatile boolean changed = false;
    private boolean firstOpening = true;


    @Override
    public void update() {
        assert EventQueue.isDispatchThread();
        if(firstOpening || !isChanged()) { // if panel is not modified by the user and he switches back to this panel, set to default
            firstOpening = false;
            getPhpUnitOptionsPanel().setPhpUnit(getPhpUnitOptions().getPhpUnitPath());
            getPhpUnitOptionsPanel().setPhpUnitSkelGen(getPhpUnitOptions().getSkeletonGeneratorPath());
        }

        changed = false;
    }

    @Override
    public void applyChanges() {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                SkeletonGenerator.resetVersion();

                getPhpUnitOptions().setPhpUnitPath(getPhpUnitOptionsPanel().getPhpUnit());
                getPhpUnitOptions().setSkeletonGeneratorPath(getPhpUnitOptionsPanel().getPhpUnitSkelGen());

                changed = false;
            }
        });
    }

    @Override
    public void cancel() {
        if(isChanged()) { // if panel is modified by the user and options window closes, discard any changes
            getPhpUnitOptionsPanel().setPhpUnit(getPhpUnitOptions().getPhpUnitPath());
            getPhpUnitOptionsPanel().setPhpUnitSkelGen(getPhpUnitOptions().getSkeletonGeneratorPath());
        }
    }

    @Override
    public boolean isValid() {
        assert EventQueue.isDispatchThread();
        PhpUnitOptionsPanel panel = getPhpUnitOptionsPanel();
        ValidationResult result = new PhpUnitOptionsValidator()
                .validate(panel.getPhpUnit(), panel.getPhpUnitSkelGen())
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
        String saved = getPhpUnitOptions().getPhpUnitPath();
        String current = getPhpUnitOptionsPanel().getPhpUnit().trim();
        if(saved == null ? !current.isEmpty() : !saved.equals(current)) {
            return true;
        }
        saved = getPhpUnitOptions().getSkeletonGeneratorPath();
        current = getPhpUnitOptionsPanel().getPhpUnitSkelGen().trim();
        return saved == null ? !current.isEmpty() : !saved.equals(current);
    }

    @Override
    public JComponent getComponent(Lookup masterLookup) {
        assert EventQueue.isDispatchThread();
        return getPhpUnitOptionsPanel();
    }

    @Override
    public HelpCtx getHelpCtx() {
        // do not change, backward compatibility
        return new HelpCtx("org.netbeans.modules.php.project.phpunit.PhpUnit"); // NOI18N
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

    private PhpUnitOptionsPanel getPhpUnitOptionsPanel() {
        assert EventQueue.isDispatchThread();
        if (phpUnitOptionsPanel == null) {
            phpUnitOptionsPanel = new PhpUnitOptionsPanel();
            phpUnitOptionsPanel.addChangeListener(this);
        }
        return phpUnitOptionsPanel;
    }

    private PhpUnitOptions getPhpUnitOptions() {
        return PhpUnitOptions.getInstance();
    }

}
