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

package org.netbeans.modules.php.project.ui.options;

import javax.swing.JComponent;
import org.netbeans.modules.php.api.executable.InvalidPhpExecutableException;
import org.netbeans.modules.php.api.executable.PhpInterpreter;
import org.netbeans.modules.php.api.util.UiUtils;
import org.netbeans.spi.options.OptionsPanelController;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;

/**
 * @author Tomas Mysik
 */
@OptionsPanelController.SubRegistration(
    displayName="#LBL_GeneralOptions",
//    toolTip="#LBL_GeneralOptionsTooltip",
    id=PhpOptionsPanelController.ID,
    location=UiUtils.OPTIONS_PATH,
    position=100
)
public class PhpOptionsPanelController extends BaseOptionsPanelController {

    public static final String ID = UiUtils.GENERAL_OPTIONS_SUBCATEGORY;

    private PhpOptionsPanel phpOptionsPanel = null;

    @Override
    public void updateInternal() {
        phpOptionsPanel.setPhpInterpreter(getPhpOptions().getPhpInterpreter());
        phpOptionsPanel.setOpenResultInOutputWindow(getPhpOptions().isOpenResultInOutputWindow());
        phpOptionsPanel.setOpenResultInBrowser(getPhpOptions().isOpenResultInBrowser());
        phpOptionsPanel.setOpenResultInEditor(getPhpOptions().isOpenResultInEditor());
    }

    @Override
    public void applyChangesInternal() {
        getPhpOptions().setPhpInterpreter(phpOptionsPanel.getPhpInterpreter());
        getPhpOptions().setOpenResultInOutputWindow(phpOptionsPanel.isOpenResultInOutputWindow());
        getPhpOptions().setOpenResultInBrowser(phpOptionsPanel.isOpenResultInBrowser());
        getPhpOptions().setOpenResultInEditor(phpOptionsPanel.isOpenResultInEditor());

        getPhpOptions().setPhpGlobalIncludePath(phpOptionsPanel.getPhpGlobalIncludePath());
    }
    
    @Override
    protected boolean areOptionsChanged() {
        return phpOptionsPanel == null ? false : !phpOptionsPanel.getPhpInterpreter().equals(getPhpOptions().getPhpInterpreter())
                || getPhpOptions().isOpenResultInOutputWindow() != phpOptionsPanel.isOpenResultInOutputWindow()
                || getPhpOptions().isOpenResultInBrowser() != phpOptionsPanel.isOpenResultInBrowser()
                || getPhpOptions().isOpenResultInEditor() != phpOptionsPanel.isOpenResultInEditor()
                || !getPhpOptions().getPhpGlobalIncludePath().equals(phpOptionsPanel.getPhpGlobalIncludePath());
    }

    @Override
    public JComponent getComponent(Lookup masterLookup) {
         if (phpOptionsPanel == null) {
            phpOptionsPanel = new PhpOptionsPanel();
            phpOptionsPanel.addChangeListener(this);
        }
        return phpOptionsPanel;
    }

    @Override
    protected boolean validateComponent() {
        // errors

        // warnings
        // #144680
        try {
            PhpInterpreter.getCustom(phpOptionsPanel.getPhpInterpreter());
        } catch (InvalidPhpExecutableException ex) {
            phpOptionsPanel.setWarning(ex.getLocalizedMessage());
            return true;
        }

        // everything ok
        phpOptionsPanel.setError(" "); // NOI18N
        return true;
    }

    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx("org.netbeans.modules.php.project.ui.options.PhpOptionsPanelController"); // NOI18N
    }

}
