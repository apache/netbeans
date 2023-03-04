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
package org.netbeans.modules.php.nette2.ui.options;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import javax.swing.JComponent;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.php.api.util.FileUtils;
import org.netbeans.modules.php.api.util.UiUtils;
import org.netbeans.modules.php.nette2.options.Nette2Options;
import org.netbeans.spi.options.OptionsPanelController;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 *
 * @author Ondrej Brejla <obrejla@netbeans.org>
 */
@UiUtils.PhpOptionsPanelRegistration(
    id = Nette2OptionsPanelController.ID,
    displayName = "#LBL_Nette2OptionsName",
    position=401
)
public class Nette2OptionsPanelController extends OptionsPanelController implements ChangeListener {
    static final String ID = "Nette2"; //NOI18N
    public static final String OPTIONS_SUBPATH = UiUtils.FRAMEWORKS_AND_TOOLS_SUB_PATH+"/"+ID; // NOI18N
    private static final String LOADER_FILE = "loader.php"; //NOI18N
    private final PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);
    private volatile boolean changed = false;
    private Nette2OptionsPanel nette2OptionsPanel;
    private boolean firstOpening = true;

    public static String getOptionsPath() {
        return UiUtils.OPTIONS_PATH + "/" + OPTIONS_SUBPATH; //NOI18N
    }

    @Override
    public void update() {
        if(firstOpening || !isChanged()) { // if panel is not modified by the user and he switches back to this panel, set to default
            firstOpening = false;
            nette2OptionsPanel.setSandbox(getOptions().getSandbox());
            nette2OptionsPanel.setNetteDirectory(getOptions().getNetteDirectory());
        }
        changed = false;
    }

    private Nette2Options getOptions() {
        return Nette2Options.getInstance();
    }

    @Override
    public void applyChanges() {
        getOptions().setSandbox(nette2OptionsPanel.getSandbox());
        getOptions().setNetteDirectory(nette2OptionsPanel.getNetteDirectory());
        changed = false;
    }

    @Override
    public void cancel() {
        if(isChanged()) { // if panel is modified by the user and options window closes, discard any changes
            nette2OptionsPanel.setSandbox(getOptions().getSandbox());
            nette2OptionsPanel.setNetteDirectory(getOptions().getNetteDirectory());
        }
    }

    @Override
    public boolean isValid() {
        String warningNette = validateNetteDirectory(nette2OptionsPanel.getNetteDirectory());
        if (warningNette != null) {
            nette2OptionsPanel.setWarning(warningNette);
            return true;
        }
        String warningSandbox = validateSandbox(nette2OptionsPanel.getSandbox());
        if (warningSandbox != null) {
            nette2OptionsPanel.setWarning(warningSandbox);
            return true;
        }
        nette2OptionsPanel.setError(" "); //NOI18N
        return true;
    }

    @NbBundle.Messages({
        "Nette2ValidationDirectory=Nette2 Directory",
        "# {0} - File in a root of Nette sources directory",
        "Nette2DirectoryValidationWarning=Nette2 Directory does not contain {0} file."
    })
    public static String validateNetteDirectory(String netteDirectory) {
        String result = FileUtils.validateDirectory(Bundle.Nette2ValidationDirectory(), netteDirectory, false);
        if (result == null) {
            File loaderPhp = new File(netteDirectory, LOADER_FILE);
            if (!loaderPhp.exists() || loaderPhp.isDirectory()) {
                result = Bundle.Nette2DirectoryValidationWarning(LOADER_FILE);
            }
        }
        return result;
    }

    @NbBundle.Messages("Nette2ValidationSandbox=Nette2 Sandbox")
    public static String validateSandbox(String sandbox) {
        return FileUtils.validateDirectory(Bundle.Nette2ValidationSandbox(), sandbox, false);
    }

    @Override
    public boolean isChanged() {
        String saved = getOptions().getNetteDirectory();
        String current = nette2OptionsPanel.getNetteDirectory().trim();
        if(saved == null ? !current.isEmpty() : !saved.equals(current)) {
            return true;
        }
        saved = getOptions().getSandbox();
        current = nette2OptionsPanel.getSandbox().trim();
        return saved == null ? !current.isEmpty() : !saved.equals(current);
    }

    @Override
    public JComponent getComponent(Lookup masterLookup) {
        if (nette2OptionsPanel == null) {
            nette2OptionsPanel = new Nette2OptionsPanel();
            nette2OptionsPanel.addChangeListener(this);
        }
        return nette2OptionsPanel;
    }

    @Override
    public HelpCtx getHelpCtx() {
        return null;
    }

    @Override
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.addPropertyChangeListener(listener);
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.removePropertyChangeListener(listener);
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
