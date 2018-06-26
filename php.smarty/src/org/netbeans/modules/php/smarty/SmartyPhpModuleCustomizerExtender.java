/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
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
