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
