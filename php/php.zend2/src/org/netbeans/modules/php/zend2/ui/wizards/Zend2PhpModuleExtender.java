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
package org.netbeans.modules.php.zend2.ui.wizards;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import javax.swing.JComponent;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.php.api.executable.InvalidPhpExecutableException;
import org.netbeans.modules.php.api.phpmodule.PhpModule;
import org.netbeans.modules.php.api.util.FileUtils;
import org.netbeans.modules.php.api.util.UiUtils;
import org.netbeans.modules.php.composer.api.Composer;
import org.netbeans.modules.php.spi.framework.PhpModuleExtender;
import org.netbeans.modules.php.zend2.options.Zend2Options;
import org.netbeans.modules.php.zend2.ui.options.Zend2OptionsPanelController;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 * Zend 2 framework extender.
 */
public class Zend2PhpModuleExtender extends PhpModuleExtender {

    private static final Logger LOGGER = Logger.getLogger(Zend2PhpModuleExtender.class.getName());

    static final String SKELETON_ZIP_ENTRY_PREFIX = "ZendSkeletonApplication-master/"; // NOI18N

    // @GuardedBy("this")
    private NewProjectConfigurationPanel panel = null;


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
        String error = getPanel().getErrorMessage();
        if (error != null) {
            return error;
        }
        try {
            // validate composer
            Composer.getDefault();
        } catch (InvalidPhpExecutableException ex) {
            return ex.getLocalizedMessage();
        }
        return null;
    }

    @Override
    public String getWarningMessage() {
        return getPanel().getWarningMessage();
    }

    @NbBundle.Messages("Zend2PhpModuleExtender.not.extended=<html>Zend 2 project not created!<br>(verify <i>Zend Application Skeleton</i> in Tools > Options > PHP > Zend 2 or review IDE log)")
    @Override
    public Set<FileObject> extend(PhpModule phpModule) throws ExtendingException {
        try {
            unpackSkeleton(phpModule);
        } catch (IOException ex) {
            LOGGER.log(Level.INFO, "Cannot unpack Zend Application Skeleton.", ex);
            throw new ExtendingException(Bundle.Zend2PhpModuleExtender_not_extended(), ex);
        }

        // install framework via composer
        try {
            Composer.getDefault().install(phpModule).get();
        } catch (InvalidPhpExecutableException ex) {
            assert false : "Should not happen since Composer is validated in the wizard panel";
            LOGGER.log(Level.INFO, "Composer is not valid so no install cannot be done.", ex);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        } catch (ExecutionException ex) {
            UiUtils.processExecutionException(ex, Zend2OptionsPanelController.OPTIONS_SUBPATH);
        }

        return getInitialFiles(phpModule);
    }

    private void unpackSkeleton(PhpModule phpModule) throws IOException {
        String skeleton = Zend2Options.getInstance().getSkeleton();
        final File sourceDir = FileUtil.toFile(phpModule.getSourceDirectory());
        FileUtils.unzip(skeleton, sourceDir, new FileUtils.ZipEntryFilter() {
            @Override
            public boolean accept(ZipEntry zipEntry) {
                return !SKELETON_ZIP_ENTRY_PREFIX.equals(zipEntry.getName());
            }
            @Override
            public String getName(ZipEntry zipEntry) {
                String entryName = zipEntry.getName();
                if (entryName.startsWith(SKELETON_ZIP_ENTRY_PREFIX)) {
                    entryName = entryName.replaceFirst(SKELETON_ZIP_ENTRY_PREFIX, ""); // NOI18N
                }
                return entryName;
            }
        });
    }

    private Set<FileObject> getInitialFiles(PhpModule phpModule) {
        Set<FileObject> files = new HashSet<>();
        addSourceFile(files, phpModule, "config/application.config.php"); // NOI18N
        addSourceFile(files, phpModule, "module/Application/src/Application/Controller/IndexController.php"); // NOI18N
        addSourceFile(files, phpModule, "module/Application/view/application/index/index.phtml"); // NOI18N
        return files;
    }

    private void addSourceFile(Set<FileObject> files, PhpModule phpModule, String relativePath) {
        FileObject sourceDirectory = phpModule.getSourceDirectory();
        if (sourceDirectory == null) {
            // broken project
            assert false : "Module extender for no sources of: " + phpModule.getName();
            return;
        }
        FileObject fileObject = sourceDirectory.getFileObject(relativePath);
        if (fileObject != null) {
            files.add(fileObject);
        }
    }

    private synchronized NewProjectConfigurationPanel getPanel() {
        assert Thread.holdsLock(this);
        if (panel == null) {
            panel = new NewProjectConfigurationPanel();
        }
        return panel;
    }

}
