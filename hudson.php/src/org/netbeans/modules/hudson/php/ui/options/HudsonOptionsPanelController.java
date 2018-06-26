/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.hudson.php.ui.options;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import javax.swing.JComponent;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.hudson.php.options.HudsonOptions;
import org.netbeans.modules.hudson.php.options.HudsonOptionsValidator;
import org.netbeans.modules.php.api.util.UiUtils;
import org.netbeans.spi.options.OptionsPanelController;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 * IDE options controller for Hudson PHP.
 */
@OptionsPanelController.SubRegistration(
    location=UiUtils.OPTIONS_PATH,
    id=HudsonOptionsPanelController.OPTIONS_SUBPATH,
    displayName="#LBL_HudsonPHPOptionsName",
//    toolTip="#LBL_OptionsTooltip"
    position=160
)
public class HudsonOptionsPanelController extends OptionsPanelController implements ChangeListener {

    public static final String OPTIONS_SUBPATH = "Hudson"; // NOI18N

    private final PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);

    private HudsonOptionsPanel hudsonOptionsPanel = null;
    private volatile boolean changed = false;


    public static String getOptionsPath() {
        return UiUtils.OPTIONS_PATH + "/" + OPTIONS_SUBPATH; // NOI18N
    }

    @Override
    public void update() {
        hudsonOptionsPanel.setBuildXml(getOptions().getBuildXml());
        hudsonOptionsPanel.setJobConfig(getOptions().getJobConfig());
        hudsonOptionsPanel.setPhpUnitConfig(getOptions().getPhpUnitConfig());

        changed = false;
    }

    @Override
    public void applyChanges() {
        getOptions().setBuildXml(hudsonOptionsPanel.getBuildXml());
        getOptions().setJobConfig(hudsonOptionsPanel.getJobConfig());
        getOptions().setPhpUnitConfig(hudsonOptionsPanel.getPhpUnitConfig());

        changed = false;
    }

    @Override
    public void cancel() {
    }

    @NbBundle.Messages("HudsonOptionsPanelController.warning.existingFiles=If build script or PHPUnit config file exists in project, it will be preferred.")
    @Override
    public boolean isValid() {
        // warnings
        String warning = HudsonOptionsValidator.validate(hudsonOptionsPanel.getBuildXml(),
                hudsonOptionsPanel.getJobConfig(), hudsonOptionsPanel.getPhpUnitConfig());
        if (warning != null) {
            hudsonOptionsPanel.setWarning(warning);
            return true;
        }
        // everything ok
        hudsonOptionsPanel.setWarning(Bundle.HudsonOptionsPanelController_warning_existingFiles());
        return true;
    }

    @Override
    public boolean isChanged() {
        String saved = getOptions().getBuildXml();
        String current = hudsonOptionsPanel.getBuildXml().trim();
        if (saved == null ? !current.isEmpty() : !saved.equals(current)) {
            return true;
        }
        saved = getOptions().getJobConfig();
        current = hudsonOptionsPanel.getJobConfig().trim();
        if (saved == null ? !current.isEmpty() : !saved.equals(current)) {
            return true;
        }
        saved = getOptions().getPhpUnitConfig();
        current = hudsonOptionsPanel.getPhpUnitConfig().trim();
        return saved == null ? !current.isEmpty() : !saved.equals(current);
    }

    @Override
    public JComponent getComponent(Lookup masterLookup) {
        if (hudsonOptionsPanel == null) {
            hudsonOptionsPanel = new HudsonOptionsPanel();
            hudsonOptionsPanel.addChangeListener(this);
        }
        return hudsonOptionsPanel;
    }

    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx("org.netbeans.modules.hudson.php.ui.options.Options"); // NOI18N
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

    private HudsonOptions getOptions() {
        return HudsonOptions.getInstance();
    }

}
