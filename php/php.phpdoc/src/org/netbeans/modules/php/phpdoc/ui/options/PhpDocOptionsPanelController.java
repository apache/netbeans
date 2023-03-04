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

package org.netbeans.modules.php.phpdoc.ui.options;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import javax.swing.JComponent;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.php.api.util.UiUtils;
import org.netbeans.modules.php.phpdoc.PhpDocScript;
import org.netbeans.spi.options.OptionsPanelController;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;

/**
 * @author Tomas Mysik
 */
@UiUtils.PhpOptionsPanelRegistration(
    id=PhpDocScript.OPTIONS_ID,
    displayName="#LBL_PHPDocOptionsName",
//    toolTip="#LBL_OptionsTooltip"
    position=170
)
public class PhpDocOptionsPanelController extends OptionsPanelController implements ChangeListener {
    private final PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);

    private PhpDocOptionsPanel phpDocOptionsPanel = null;
    private volatile boolean changed = false;

    @Override
    public void update() {
        getComponent(null);
        phpDocOptionsPanel.setPhpDoc(getOptions().getPhpDoc());

        changed = false;
    }

    @Override
    public void applyChanges() {
        getComponent(null);
        getOptions().setPhpDoc(phpDocOptionsPanel.getPhpDoc());

        changed = false;
    }

    @Override
    public void cancel() {
    }

    @Override
    public boolean isValid() {
        getComponent(null);
        // warnings
        String warning = PhpDocScript.validate(phpDocOptionsPanel.getPhpDoc());
        if (warning != null) {
            phpDocOptionsPanel.setWarning(warning);
            return true;
        }

        // everything ok
        phpDocOptionsPanel.clearError();
        return true;
    }

    @Override
    public boolean isChanged() {
        return changed;
    }

    @Override
    public JComponent getComponent(Lookup masterLookup) {
        if (phpDocOptionsPanel == null) {
            phpDocOptionsPanel = new PhpDocOptionsPanel();
            phpDocOptionsPanel.addChangeListener(this);
        }
        return phpDocOptionsPanel;
    }

    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx("org.netbeans.modules.php.phpdoc.ui.options.PhpDocOptions"); // NOI18N
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

    private PhpDocOptions getOptions() {
        return PhpDocOptions.getInstance();
    }
}
