/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
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
