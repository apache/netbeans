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
package org.netbeans.modules.php.composer.ui.options;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import javax.swing.JComponent;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.php.api.util.UiUtils;
import org.netbeans.modules.php.api.validation.ValidationResult;
import org.netbeans.modules.php.composer.options.ComposerOptions;
import org.netbeans.modules.php.composer.options.ComposerOptionsValidator;
import org.netbeans.spi.options.OptionsPanelController;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;

@UiUtils.PhpOptionsPanelRegistration(
	id=ComposerOptionsPanelController.ID,
        displayName="#Options.name",
        // toolTip="#LBL_OptionsTooltip"
        position=180
)
public class ComposerOptionsPanelController extends OptionsPanelController implements ChangeListener {

    static final String ID = "Composer"; // NOI18N
    public static final String OPTIONS_SUBPATH = UiUtils.FRAMEWORKS_AND_TOOLS_SUB_PATH+"/"+ID; // NOI18N

    private final PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);

    private ComposerOptionsPanel composerOptionsPanel = null;
    private volatile boolean changed = false;
    private boolean firstOpening = true;


    public static String getOptionsPath() {
        return UiUtils.OPTIONS_PATH + "/" + OPTIONS_SUBPATH; // NOI18N
    }

    @Override
    public void update() {
        if (firstOpening || !isChanged()) { // if panel is not modified by the user and he switches back to this panel, set to default
            firstOpening = false;
            composerOptionsPanel.setComposerPath(getOptions().getComposerPath());
            composerOptionsPanel.setVendor(getOptions().getVendor());
            composerOptionsPanel.setAuthorName(getOptions().getAuthorName());
            composerOptionsPanel.setAuthorEmail(getOptions().getAuthorEmail());
            composerOptionsPanel.setIgnoreVendor(getOptions().isIgnoreVendor());
        }

        changed = false;
    }

    @Override
    public void applyChanges() {
        getOptions().setComposerPath(composerOptionsPanel.getComposerPath());
        getOptions().setVendor(composerOptionsPanel.getVendor());
        getOptions().setAuthorName(composerOptionsPanel.getAuthorName());
        getOptions().setAuthorEmail(composerOptionsPanel.getAuthorEmail());
        getOptions().setIgnoreVendor(composerOptionsPanel.isIgnoreVendor());

        changed = false;
    }

    @Override
    public void cancel() {
        if (isChanged()) { // if panel is modified by the user and options window closes, discard any changes
            composerOptionsPanel.setComposerPath(getOptions().getComposerPath());
            composerOptionsPanel.setVendor(getOptions().getVendor());
            composerOptionsPanel.setAuthorName(getOptions().getAuthorName());
            composerOptionsPanel.setAuthorEmail(getOptions().getAuthorEmail());
            composerOptionsPanel.setIgnoreVendor(getOptions().isIgnoreVendor());
        }
    }

    @Override
    public boolean isValid() {
        ValidationResult result = new ComposerOptionsValidator()
                .validate(composerOptionsPanel.getComposerPath(), composerOptionsPanel.getVendor(),
                        composerOptionsPanel.getAuthorName(), composerOptionsPanel.getAuthorEmail())
                .getResult();
        // errors
        if (result.hasErrors()) {
            composerOptionsPanel.setError(result.getErrors().get(0).getMessage());
            return false;
        }
        // warnings
        if (result.hasWarnings()) {
            composerOptionsPanel.setWarning(result.getWarnings().get(0).getMessage());
            return true;
        }
        // everything ok
        composerOptionsPanel.setError(" "); // NOI18N
        return true;
    }

    @Override
    public boolean isChanged() {
        String saved = getOptions().getComposerPath();
        String current = composerOptionsPanel.getComposerPath().trim();
        if (saved == null ? !current.isEmpty() : !saved.equals(current)) {
            return true;
        }
        saved = getOptions().getVendor();
        current = composerOptionsPanel.getVendor().trim();
        if (saved == null ? !current.isEmpty() : !saved.equals(current)) {
            return true;
        }
        saved = getOptions().getAuthorName();
        current = composerOptionsPanel.getAuthorName().trim();
        if (saved == null ? !current.isEmpty() : !saved.equals(current)) {
            return true;
        }
        saved = getOptions().getAuthorEmail();
        current = composerOptionsPanel.getAuthorEmail().trim();
        if (saved == null ? !current.isEmpty() : !saved.equals(current)) {
            return true;
        }
        return getOptions().isIgnoreVendor() != composerOptionsPanel.isIgnoreVendor();
    }

    @Override
    public JComponent getComponent(Lookup masterLookup) {
        if (composerOptionsPanel == null) {
            composerOptionsPanel = new ComposerOptionsPanel();
            composerOptionsPanel.addChangeListener(this);
        }
        return composerOptionsPanel;
    }

    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx("org.netbeans.modules.php.composer.ui.options.Options"); // NOI18N
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

    private ComposerOptions getOptions() {
        return ComposerOptions.getInstance();
    }

}
