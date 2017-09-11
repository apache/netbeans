/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.javafx2.scenebuilder.options;

import org.netbeans.modules.javafx2.scenebuilder.Settings;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.List;
import javax.swing.JComponent;
import org.netbeans.modules.javafx2.scenebuilder.Home;
import org.netbeans.modules.options.java.api.JavaOptions;
import org.netbeans.spi.options.OptionsPanelController;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.Parameters;

@OptionsPanelController.SubRegistration(location = JavaOptions.JAVA,
displayName = "#AdvancedOption_DisplayName_SB",
keywords = "#AdvancedOption_Keywords_SB",
keywordsCategory = JavaOptions.JAVA + "/JavaFX",
id=SBOptionsPanelController.SUBREG_ID)
@org.openide.util.NbBundle.Messages({"AdvancedOption_DisplayName_SB=JavaFX", "AdvancedOption_Keywords_SB=javafx"})
public final class SBOptionsPanelController extends OptionsPanelController {
    final public static String SUBREG_CAT = JavaOptions.JAVA;
    final public static String SUBREG_ID = "SceneBuilder"; // NOI18N
    private SBOptionsPanel panel;
    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
    private boolean changed;
    private Settings settings;
    
    @Override
    public void update() {
        settings = Settings.getInstance();
        Parameters.notNull("settings", settings); //NOI18N

        panel.load();
        changed = false;
    }

    @Override
    public void applyChanges() {
        panel.store();
        settings.store();
        changed = false;
    }

    @Override
    public void cancel() {
        // need not do anything special, if no changes have been persisted yet
    }

    @Override
    public boolean isValid() {
        return getPanel().valid();
    }

    @Override
    public boolean isChanged() {
        return getPanel().isChanged();
    }

    Home getDefaultSBHome() {
        return settings.getPredefinedHome();
    }
    
    List<Home> getUserDefinedHomes() {
        return settings.getUserDefinedHomes();
    }
    
    void setUserDefinedHomes(List<Home> userDefs) {
        settings.setUserDefinedHomes(userDefs);
    }
    
    public Home getSbHome() {
        return settings.getSelectedHome();
    }
    
    public void setSbHome(Home sbHome) {
        settings.setSelectedHome(sbHome);
    }
    
    public boolean isSaveBeforeLaunch() {
        return settings.isSaveBeforeLaunch();
    }
    
    public void setSaveBeforeLaunch(boolean val) {
        settings.setSaveBeforeLaunch(val);
    }

    @Override
    public HelpCtx getHelpCtx() {
        return null; // new HelpCtx("...ID") if you have a help set
    }

    @Override
    public JComponent getComponent(Lookup masterLookup) {
        return getPanel();
    }

    @Override
    public void addPropertyChangeListener(PropertyChangeListener l) {
        pcs.addPropertyChangeListener(l);
    }

    public synchronized void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        pcs.addPropertyChangeListener(propertyName, listener);
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener l) {
        pcs.removePropertyChangeListener(l);
    }

    private SBOptionsPanel getPanel() {
        if (panel == null) {
            panel = new SBOptionsPanel(this);
        }
        return panel;
    }

    void changed() {
        if (!changed) {
            changed = true;
            pcs.firePropertyChange(OptionsPanelController.PROP_CHANGED, false, true);
        }
        pcs.firePropertyChange(OptionsPanelController.PROP_VALID, null, null);
    }
    
}
