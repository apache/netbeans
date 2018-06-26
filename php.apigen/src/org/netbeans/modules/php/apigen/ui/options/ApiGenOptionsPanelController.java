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
package org.netbeans.modules.php.apigen.ui.options;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import javax.swing.JComponent;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.php.api.util.UiUtils;
import org.netbeans.modules.php.apigen.commands.ApiGenScript;
import org.netbeans.modules.php.apigen.options.ApiGenOptions;
import org.netbeans.spi.options.OptionsPanelController;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;

/**
 * IDE options controller for ApiGen.
 */
@UiUtils.PhpOptionsPanelRegistration(
    id=ApiGenOptionsPanelController.ID,
    displayName="#LBL_PHPGenOptionsName",
//    toolTip="#LBL_OptionsTooltip"
    position=165
)
public class ApiGenOptionsPanelController extends OptionsPanelController implements ChangeListener {

    static final String ID = "ApiGen"; // NOI18N
    public static final String OPTIONS_SUBPATH = UiUtils.FRAMEWORKS_AND_TOOLS_SUB_PATH+"/" + ID; // NOI18N

    private final PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);

    private ApiGenOptionsPanel apiGenOptionsPanel = null;
    private volatile boolean changed = false;
    private boolean firstOpening = true;


    public static String getOptionsPath() {
        return UiUtils.OPTIONS_PATH + "/" + OPTIONS_SUBPATH; // NOI18N
    }

    @Override
    public void update() {
        if(firstOpening || !isChanged()) { // if panel is not modified by the user and he switches back to this panel, set to default
            firstOpening = false;
            apiGenOptionsPanel.setApiGen(getOptions().getApiGen());
        }

        changed = false;
    }

    @Override
    public void applyChanges() {
        getOptions().setApiGen(apiGenOptionsPanel.getApiGen());

        changed = false;
    }

    @Override
    public void cancel() {
        if(isChanged()) { // if panel is modified by the user and options window closes, discard any changes
            apiGenOptionsPanel.setApiGen(getOptions().getApiGen());
        }
    }

    @Override
    public boolean isValid() {
        // warnings
        String warning = ApiGenScript.validate(apiGenOptionsPanel.getApiGen());
        if (warning != null) {
            apiGenOptionsPanel.setWarning(warning);
            return true;
        }

        // everything ok
        apiGenOptionsPanel.setError(" "); // NOI18N
        return true;
    }

    @Override
    public boolean isChanged() {
        String saved = getOptions().getApiGen();
        String current = apiGenOptionsPanel.getApiGen().trim();
        return saved == null ? !current.isEmpty() : !saved.equals(current);
    }

    @Override
    public JComponent getComponent(Lookup masterLookup) {
        if (apiGenOptionsPanel == null) {
            apiGenOptionsPanel = new ApiGenOptionsPanel();
            apiGenOptionsPanel.addChangeListener(this);
        }
        return apiGenOptionsPanel;
    }

    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx("org.netbeans.modules.php.apigen.ui.options.Options"); // NOI18N
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

    private ApiGenOptions getOptions() {
        return ApiGenOptions.getInstance();
    }

}
