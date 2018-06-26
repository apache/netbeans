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

package org.netbeans.modules.php.symfony;

import java.util.EnumSet;
import java.util.prefs.Preferences;
import javax.swing.JComponent;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.php.api.phpmodule.PhpModule;
import org.netbeans.modules.php.spi.framework.PhpModuleCustomizerExtender;
import org.netbeans.modules.php.symfony.ui.customizer.SymfonyCustomizerPanel;
import org.netbeans.modules.php.symfony.ui.options.SymfonyOptions;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

public class SymfonyPhpModuleCustomizerExtender extends PhpModuleCustomizerExtender {
    public static final String IGNORE_CACHE_DIRECTORY = "ignore-cache-directory"; // NOI18N

    private final PhpModule phpModule;
    private final boolean originalState;

    private SymfonyCustomizerPanel component;

    SymfonyPhpModuleCustomizerExtender(PhpModule phpModule) {
        this.phpModule = phpModule;

        originalState = isCacheDirectoryIgnored(phpModule);
    }

    public static boolean isCacheDirectoryIgnored(PhpModule phpModule) {
        return getPreferences(phpModule).getBoolean(IGNORE_CACHE_DIRECTORY, SymfonyOptions.getInstance().getIgnoreCache());
    }

    @Override
    public String getDisplayName() {
        return NbBundle.getMessage(SymfonyPhpModuleCustomizerExtender.class, "LBL_Symfony");
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
        boolean newState = getPanel().isIgnoreCacheDirectory();
        if (newState != originalState) {
            getPreferences().putBoolean(IGNORE_CACHE_DIRECTORY, newState);
            return EnumSet.of(Change.IGNORED_FILES_CHANGE);
        }
        return null;
    }

    private SymfonyCustomizerPanel getPanel() {
        if (component == null) {
            component = new SymfonyCustomizerPanel();
            component.setIgnoreCacheDirectory(originalState);
        }
        return component;
    }

    private Preferences getPreferences() {
        return getPreferences(phpModule);
    }

    private static Preferences getPreferences(PhpModule module) {
        return module.getPreferences(SymfonyPhpFrameworkProvider.class, true);
    }
}
