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
package org.netbeans.modules.php.nette2;

import org.netbeans.modules.php.nette2.utils.FileUtils;
import java.io.File;
import java.util.HashSet;
import java.util.Set;
import javax.swing.JComponent;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.php.api.phpmodule.PhpModule;
import org.netbeans.modules.php.nette2.options.Nette2Options;
import org.netbeans.modules.php.nette2.ui.wizards.NewNette2ProjectPanel;
import org.netbeans.modules.php.nette2.utils.Constants;
import org.netbeans.modules.php.spi.framework.PhpModuleExtender;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.HelpCtx;

/**
 *
 * @author Ondrej Brejla <obrejla@netbeans.org>
 */
class Nette2PhpModuleExtender extends PhpModuleExtender {
    //@GuardedBy("this")
    private NewNette2ProjectPanel netteProjectPanel;

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
        return getErrorMessage() == null;
    }

    @Override
    public String getErrorMessage() {
        return getPanel().getErrorMessage();
    }

    @Override
    public String getWarningMessage() {
        return null;
    }

    @Override
    public Set<FileObject> extend(PhpModule phpModule) throws ExtendingException {
        Set<FileObject> result = new HashSet<>();
        FileObject sourceDirectory = phpModule.getSourceDirectory();
        if (sourceDirectory != null) {
            String projectDirectory = sourceDirectory.getPath();
            FileUtils.copyDirectory(new File(Nette2Options.getInstance().getSandbox()), new File(projectDirectory));
            File netteLibsDirectory = new File(projectDirectory, Constants.NETTE_LIBS_DIR);
            if (isValidNetteLibsDir(netteLibsDirectory) && getPanel().isCopyNetteCheckboxSelected()) {
                FileUtils.copyDirectory(new File(Nette2Options.getInstance().getNetteDirectory()), netteLibsDirectory);
            }
            FileObject bootstrap = FileUtil.toFileObject(new File(projectDirectory, Constants.COMMON_BOOTSTRAP_PATH));
            if (bootstrap != null && !bootstrap.isFolder() && bootstrap.isValid()) {
                result.add(bootstrap);
            }
            FileObject tempDir = sourceDirectory.getFileObject(Constants.NETTE_TEMP_DIR);
            if (tempDir != null) {
                FileUtils.chmod777Recursively(tempDir);
            }
        }
        return result;
    }

    private boolean isValidNetteLibsDir(File netteLibsDirectory) {
        return !netteLibsDirectory.exists() || (netteLibsDirectory.exists() && netteLibsDirectory.isDirectory() && netteLibsDirectory.list() == null);
    }

    private synchronized NewNette2ProjectPanel getPanel() {
        if (netteProjectPanel == null) {
            netteProjectPanel = new NewNette2ProjectPanel();
        }
        return netteProjectPanel;
    }

}
