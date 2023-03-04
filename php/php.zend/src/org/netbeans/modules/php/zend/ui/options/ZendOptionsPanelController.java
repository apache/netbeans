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

package org.netbeans.modules.php.zend.ui.options;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import javax.swing.JComponent;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.php.api.util.UiUtils;
import org.netbeans.modules.php.zend.ZendScript;
import org.netbeans.spi.options.OptionsPanelController;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;

/**
 * @author Tomas Mysik
 */
@UiUtils.PhpOptionsPanelRegistration(
    id=ZendScript.OPTIONS_ID,
    displayName="#LBL_PHPZendOptionsName",
//    toolTip="#LBL_OptionsTooltip"
    position=300
)
public class ZendOptionsPanelController extends OptionsPanelController implements ChangeListener {
    private final PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);

    private ZendOptionsPanel zendOptionsPanel = null;
    private volatile boolean changed = false;
    private boolean firstOpening = true;

    @Override
    public void update() {
        if(firstOpening || !isChanged()) { // if panel is not modified by the user and he switches back to this panel, set to default
            firstOpening = false;
            zendOptionsPanel.setZend(getOptions().getZend());
            zendOptionsPanel.setDefaultParamsForProject(getOptions().getDefaultParamsForProject());
        }

        changed = false;
    }

    @Override
    public void applyChanges() {
        getOptions().setZend(zendOptionsPanel.getZend());
        getOptions().setDefaultParamsForProject(zendOptionsPanel.getDefaultParamsForProject());

        changed = false;
    }

    @Override
    public void cancel() {
        if(isChanged()) { // if panel is modified by the user and options window closes, discard any changes
            zendOptionsPanel.setZend(getOptions().getZend());
            zendOptionsPanel.setDefaultParamsForProject(getOptions().getDefaultParamsForProject());
        }
    }

    @Override
    public boolean isValid() {
        // warnings
        String warning = ZendScript.validate(zendOptionsPanel.getZend());
        if (warning != null) {
            zendOptionsPanel.setWarning(warning);
            return true;
        }

        // everything ok
        zendOptionsPanel.clearError();
        return true;
    }

    @Override
    public boolean isChanged() {
        String saved = getOptions().getZend();
        String current = zendOptionsPanel.getZend().trim();
        return (saved == null ? !current.isEmpty() : !saved.equals(current))
                || !getOptions().getDefaultParamsForProject().equals(zendOptionsPanel.getDefaultParamsForProject().trim());
    }

    @Override
    public JComponent getComponent(Lookup masterLookup) {
        if (zendOptionsPanel == null) {
            zendOptionsPanel = new ZendOptionsPanel();
            zendOptionsPanel.addChangeListener(this);
        }
        return zendOptionsPanel;
    }

    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx("org.netbeans.modules.php.zend.ui.options.ZendOptions"); // NOI18N
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

    private ZendOptions getOptions() {
        return ZendOptions.getInstance();
    }
}
