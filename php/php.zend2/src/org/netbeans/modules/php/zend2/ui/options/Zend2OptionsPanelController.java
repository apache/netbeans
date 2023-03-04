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
package org.netbeans.modules.php.zend2.ui.options;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import javax.swing.JComponent;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.php.api.util.UiUtils;
import org.netbeans.modules.php.api.validation.ValidationResult;
import org.netbeans.modules.php.zend2.options.Zend2Options;
import org.netbeans.modules.php.zend2.validation.OptionsValidator;
import org.netbeans.spi.options.OptionsPanelController;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;

/**
 * Zend 2 IDE Options.
 */
@UiUtils.PhpOptionsPanelRegistration(
    id=Zend2OptionsPanelController.ID,
    displayName="#Zend2OptionsPanelController.options.name",
//    toolTip="#LBL_OptionsTooltip"
    position=299
)
public class Zend2OptionsPanelController extends OptionsPanelController implements ChangeListener {

    static final String ID = "Zend2"; // NOI18N
    public static final String OPTIONS_SUBPATH = UiUtils.FRAMEWORKS_AND_TOOLS_SUB_PATH+"/"+ID; // NOI18N

    private final PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);

    private Zend2OptionsPanel zend2OptionsPanel = null;
    private volatile boolean changed = false;
    private boolean firstOpening = true;


    public static String getOptionsPath() {
        return UiUtils.OPTIONS_PATH + "/" + OPTIONS_SUBPATH; // NOI18N
    }

    @Override
    public void update() {
        if(firstOpening || !isChanged()) { // if panel is not modified by the user and he switches back to this panel, set to default
            firstOpening = false;
            zend2OptionsPanel.setSkeleton(getOptions().getSkeleton());
        }

        changed = false;
    }

    @Override
    public void applyChanges() {
        getOptions().setSkeleton(zend2OptionsPanel.getSkeleton());

        changed = false;
    }

    @Override
    public void cancel() {
        if(isChanged()) { // if panel is modified by the user and options window closes, discard any changes
            zend2OptionsPanel.setSkeleton(getOptions().getSkeleton());
        }
    }

    @Override
    public boolean isValid() {
        // clean up
        zend2OptionsPanel.setError(" "); // NOI18N

        // validate
        ValidationResult validationResult = new OptionsValidator()
                .validate(zend2OptionsPanel.getSkeleton())
                .getResult();
        String warning = null;
        // get first message
        if (validationResult.hasErrors()) {
            for (ValidationResult.Message message : validationResult.getErrors()) {
                warning = message.getMessage();
                break;
            }
        } else if (validationResult.hasWarnings()) {
            for (ValidationResult.Message message : validationResult.getWarnings()) {
                warning = message.getMessage();
                break;
            }
        }
        zend2OptionsPanel.setWarning(warning);

        // everything ok
        return true;
    }

    @Override
    public boolean isChanged() {
        String saved = getOptions().getSkeleton();
        String current = zend2OptionsPanel.getSkeleton().trim();
        return (saved == null ? !current.isEmpty() : !saved.equals(current));
    }

    @Override
    public JComponent getComponent(Lookup masterLookup) {
        if (zend2OptionsPanel == null) {
            zend2OptionsPanel = new Zend2OptionsPanel();
            zend2OptionsPanel.addChangeListener(this);
        }
        return zend2OptionsPanel;
    }

    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx("org.netbeans.modules.php.zend2.options.Zend2Options"); // NOI18N
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

    private Zend2Options getOptions() {
        return Zend2Options.getInstance();
    }

}
