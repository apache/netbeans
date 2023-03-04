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
package org.netbeans.modules.php.symfony2;

import java.util.EnumSet;
import javax.swing.JComponent;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.php.api.phpmodule.PhpModule;
import org.netbeans.modules.php.api.util.StringUtils;
import org.netbeans.modules.php.spi.framework.PhpModuleCustomizerExtender;
import org.netbeans.modules.php.symfony2.commands.SymfonyScript;
import org.netbeans.modules.php.symfony2.preferences.SymfonyPreferences;
import org.netbeans.modules.php.symfony2.ui.customizer.SymfonyCustomizerPanel;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle.Messages;

/**
 * Customizer extender.
 */
public class SymfonyPhpModuleCustomizerExtender extends PhpModuleCustomizerExtender {

    private final PhpModule phpModule;
    private final boolean originalEnabled;
    private final String originalAppDir;
    private final boolean originalCacheDirIgnored;

    // @GuardedBy(EDT)
    private SymfonyCustomizerPanel component;
    // @GuardedBy(EDT)
    private boolean valid = false;
    // @GuardedBy(EDT)
    private String errorMessage = null;


    SymfonyPhpModuleCustomizerExtender(PhpModule phpModule) {
        this.phpModule = phpModule;

        originalEnabled = SymfonyPhpFrameworkProvider.getInstance().isInPhpModule(phpModule);
        originalAppDir = SymfonyPreferences.getAppDir(phpModule);
        originalCacheDirIgnored = SymfonyPreferences.isCacheDirIgnored(phpModule);
    }

    @Messages("LBL_Symfony2=Symfony 2/3")
    @Override
    public String getDisplayName() {
        return Bundle.LBL_Symfony2();
    }

    @Override
    public String getDisplayName(PhpModule phpModule) {
        SymfonyVersion symfonyVersion = SymfonyVersion.forPhpModule(phpModule);
        if (symfonyVersion != null) {
            return symfonyVersion.getFrameworkName(true);
        }
        return getDisplayName();
    }

    @Override
    public void addChangeListener(ChangeListener listener) {
        getPanel().addChangeListener(listener);
    }

    @Override
    public void removeChangeListener(ChangeListener listener) {
        getPanel().removeChangeListener(listener);
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
        validate();
        return valid;
    }

    @Override
    public String getErrorMessage() {
        validate();
        return errorMessage;
    }

    @Override
    public EnumSet<Change> save(PhpModule phpModule) {
        EnumSet<Change> changes = EnumSet.noneOf(Change.class);
        saveEnabled(changes);
        saveAppDir(changes);
        saveCacheIgnored(changes);
        if (changes.isEmpty()) {
            return null;
        }
        return changes;
    }

    private void saveEnabled(EnumSet<Change> changes) {
        boolean newEnabled = getPanel().isSupportEnabled();
        if (newEnabled != originalEnabled) {
            SymfonyPreferences.setEnabled(phpModule, newEnabled);
            changes.add(Change.FRAMEWORK_CHANGE);
        }
    }

    private void saveAppDir(EnumSet<Change> changes) {
        String newAppDir = getPanel().getAppDirectory();
        if (!newAppDir.equals(originalAppDir)) {
            SymfonyPreferences.setAppDir(phpModule, newAppDir);
            changes.add(Change.FRAMEWORK_CHANGE);
        }
    }

    private void saveCacheIgnored(EnumSet<Change> changes) {
        boolean newIgnored = getPanel().isIgnoreCacheDirectory();
        if (newIgnored != originalCacheDirIgnored) {
            SymfonyPreferences.setCacheDirIgnored(phpModule, newIgnored);
            changes.add(Change.IGNORED_FILES_CHANGE);
        }
    }

    private SymfonyCustomizerPanel getPanel() {
        if (component == null) {
            component = new SymfonyCustomizerPanel(phpModule.getSourceDirectory());
            component.setSupportEnabled(originalEnabled);
            component.setAppDirectory(originalAppDir);
            component.setIgnoreCacheDirectory(originalCacheDirIgnored);
        }
        return component;
    }

    @Messages({
        "SymfonyPhpModuleCustomizerExtender.error.sources.invalid=Source Files are invalid.",
        "SymfonyPhpModuleCustomizerExtender.error.appDir.empty=App directory must be set.",
        "SymfonyPhpModuleCustomizerExtender.error.appDir.notChild=App directory must be underneath Source Files.",
    })
    private void validate() {
        SymfonyCustomizerPanel panel = getPanel();
        if (!panel.isSupportEnabled()) {
            // nothing to validate
            valid = true;
            errorMessage = null;
            return;
        }
        // check app dir
        String appDir = panel.getAppDirectory();
        if (!StringUtils.hasText(appDir)) {
            valid = false;
            errorMessage = Bundle.SymfonyPhpModuleCustomizerExtender_error_appDir_empty();
            return;
        }
        FileObject sources = phpModule.getSourceDirectory();
        if (sources == null) {
            // broken project
            assert false : "Customizer extender for no sources of: " + phpModule.getName();
            valid = false;
            errorMessage = Bundle.SymfonyPhpModuleCustomizerExtender_error_sources_invalid();
            return;
        }
        FileObject fo = sources.getFileObject(appDir);
        if (fo == null
                || !FileUtil.isParentOf(sources, fo)) {
            valid = false;
            errorMessage = Bundle.SymfonyPhpModuleCustomizerExtender_error_appDir_notChild();
            return;
        }
        // everything ok
        valid = true;
        errorMessage = null;
    }

}
