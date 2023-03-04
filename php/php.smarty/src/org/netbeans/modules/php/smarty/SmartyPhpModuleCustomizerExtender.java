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
package org.netbeans.modules.php.smarty;

import java.util.EnumSet;
import java.util.prefs.Preferences;
import javax.swing.JComponent;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.php.api.phpmodule.PhpModule;
import org.netbeans.modules.php.smarty.editor.utlis.LexerUtils;
import org.netbeans.modules.php.smarty.ui.customizer.SmartyCustomizerPanel;
import org.netbeans.modules.php.spi.framework.PhpModuleCustomizerExtender;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

public class SmartyPhpModuleCustomizerExtender extends PhpModuleCustomizerExtender {

    public static final String CUSTOM_OPEN_DELIMITER = "custom-open-delimiter"; // NOI18N
    public static final String CUSTOM_CLOSE_DELIMITER = "custom-close-delimiter"; // NOI18N

    private final PhpModule phpModule;
    private final String customOpenDelimiter;
    private final String customCloseDelimiter;
    private final boolean originalEnabled;

    private SmartyCustomizerPanel component;

    SmartyPhpModuleCustomizerExtender(PhpModule phpModule) {
        this.phpModule = phpModule;
        customOpenDelimiter = getCustomOpenDelimiter(phpModule);
        customCloseDelimiter = getCustomCloseDelimiter(phpModule);
        originalEnabled = SmartyPhpFrameworkProvider.getInstance().isInPhpModule(phpModule);
    }

    public static String getCustomOpenDelimiter(PhpModule phpModule) {
        return getPreferences(phpModule).get(CUSTOM_OPEN_DELIMITER, "");
    }

    public static String getCustomCloseDelimiter(PhpModule phpModule) {
        return getPreferences(phpModule).get(CUSTOM_CLOSE_DELIMITER, "");
    }

    @Override
    public String getDisplayName() {
        return NbBundle.getMessage(SmartyPhpModuleCustomizerExtender.class, "LBL_Smarty");
    }

    @Override
    public void addChangeListener(ChangeListener listener) {
        // always valid
    }

    @Override
    public void removeChangeListener(ChangeListener listener) {
        // always valid
    }

    @Override
    public JComponent getComponent() {
        return getPanel();
    }

    @Override
    public HelpCtx getHelp() {
        return null;
    }

    @Override
    public boolean isValid() {
        // always valid
        return true;
    }

    @Override
    public String getErrorMessage() {
        // always valid
        return null;
    }

    @Override
    public EnumSet<Change> save(PhpModule phpModule) {
        EnumSet<Change> changes = EnumSet.noneOf(Change.class);

        // Smarty support enabled
        boolean newEnabled = getPanel().isSupportEnabled();
        if (newEnabled != originalEnabled) {
            getPreferences().putBoolean(SmartyPhpFrameworkProvider.PROP_SMARTY_AVAILABLE, newEnabled);
            changes.add(Change.FRAMEWORK_CHANGE);
        }

        // Custom delimiters
        if (!getPanel().getCustomOpenDelimiterTextField().equals(customOpenDelimiter)
                || !getPanel().getCustomCloseDelimiterTextField().equals(customCloseDelimiter)) {
            getPreferences().put(CUSTOM_OPEN_DELIMITER, getPanel().getCustomOpenDelimiterTextField());
            getPreferences().put(CUSTOM_CLOSE_DELIMITER, getPanel().getCustomCloseDelimiterTextField());

            // Manual relexing of all opened documents in editor
            LexerUtils.relexerOpenedTpls();
        }

        return changes;
    }

    private SmartyCustomizerPanel getPanel() {
        if (component == null) {
            component = new SmartyCustomizerPanel();
            component.setSupportEnabled(originalEnabled);
            component.setCustomOpenDelimiterText(customOpenDelimiter);
            component.setCustomCloseDelimiterText(customCloseDelimiter);
        }
        return component;
    }

    private Preferences getPreferences() {
        return getPreferences(phpModule);
    }

    private static Preferences getPreferences(PhpModule module) {
        return module.getPreferences(SmartyPhpFrameworkProvider.class, true);
    }
}
