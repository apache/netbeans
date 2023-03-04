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

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import javax.swing.JComponent;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.php.api.phpmodule.PhpModule;
import org.netbeans.modules.php.api.util.FileUtils;
import org.netbeans.modules.php.spi.framework.PhpModuleExtender;
import org.netbeans.modules.php.symfony2.commands.InstallerExecutable;
import org.netbeans.modules.php.symfony2.options.SymfonyOptions;
import org.netbeans.modules.php.symfony2.ui.wizards.NewProjectConfigurationPanel;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 * Symfony 2/3 PHP module extender.
 */
public class SymfonyPhpModuleExtender extends PhpModuleExtender {

    private static final Logger LOGGER = Logger.getLogger(SymfonyPhpModuleExtender.class.getName());

    static final String SYMFONY_ZIP_ENTRY_PREFIX = "Symfony/"; // NOI18N

    //@GuardedBy(this)
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
        return getPanel().getErrorMessage();
    }

    @Override
    public String getWarningMessage() {
        return null;
    }

    @NbBundle.Messages("MSG_NotExtended=<html>Symfony project not created!<br>(verify Symfony options in Tools > Options > PHP > Symfony 2/3 or review IDE log)")
    @Override
    public Set<FileObject> extend(PhpModule phpModule) throws ExtendingException {
        if (SymfonyOptions.getInstance().isUseInstaller()) {
            // use installer
            final InstallerExecutable installer = InstallerExecutable.getDefault(phpModule, false);
            assert installer != null;
            // [NETBEANS-1443] always use LTS
            Future<Integer> task = installer.run(true);
            try {
                task.get(30, TimeUnit.MINUTES);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            } catch (ExecutionException | TimeoutException ex) {
                LOGGER.log(Level.INFO, "Symfony installer failed.", ex);
                throw new ExtendingException(Bundle.MSG_NotExtended(), ex);
            }
            // move created files to project dir
            File sf2Directory = installer.getSymfony2Dir();
            LOGGER.log(Level.INFO, "Using Symfony 2/3 files from {0}", sf2Directory);
            FileObject sf2Dir = FileUtil.toFileObject(sf2Directory);
            assert sf2Dir != null : sf2Directory
                    + "[exists: " + sf2Directory.exists()
                    + ", isDir: " + sf2Directory.isDirectory()
                    + ", children: " + Arrays.toString(sf2Directory.list());
            final FileObject sourceDirectory = phpModule.getSourceDirectory();
            assert sourceDirectory != null : phpModule.getProjectDirectory();
            try {
                copyFiles(sf2Dir, sourceDirectory);
            } catch (IOException ex) {
                LOGGER.log(Level.INFO, "Moving Symfony files failed.", ex);
                throw new ExtendingException(Bundle.MSG_NotExtended(), ex);
            } finally {
                sourceDirectory.refresh();
            }
        } else {
            // use sandbox
            try {
                unpackSandbox(phpModule);
            } catch (IOException ex) {
                LOGGER.log(Level.INFO, "Cannot unpack Symfony Sandbox.", ex);
                throw new ExtendingException(Bundle.MSG_NotExtended(), ex);
            }
        }

        // prefetch commands
        SymfonyPhpFrameworkProvider.getInstance().getFrameworkCommandSupport(phpModule).refreshFrameworkCommandsLater(null);

        return getInitialFiles(phpModule);
    }

    private void copyFiles(FileObject source, FileObject destination) throws IOException {
        for (FileObject child : source.getChildren()) {
            if (child.isFolder()) {
                copyFiles(child, FileUtil.createFolder(destination, child.getName()));
            } else {
                assert child.isData() : child;
                assert destination.isFolder() : destination;
                FileUtil.copyFile(child, destination, child.getName());
            }
        }
    }

    private void unpackSandbox(PhpModule phpModule) throws IOException {
        String sandbox = SymfonyOptions.getInstance().getSandbox();
        final File sourceDir = FileUtil.toFile(phpModule.getSourceDirectory());
        FileUtils.unzip(sandbox, sourceDir, new FileUtils.ZipEntryFilter() {
            @Override
            public boolean accept(ZipEntry zipEntry) {
                return !SYMFONY_ZIP_ENTRY_PREFIX.equals(zipEntry.getName());
            }
            @Override
            public String getName(ZipEntry zipEntry) {
                String entryName = zipEntry.getName();
                if (entryName.startsWith(SYMFONY_ZIP_ENTRY_PREFIX)) {
                    entryName = entryName.replaceFirst(SYMFONY_ZIP_ENTRY_PREFIX, ""); // NOI18N
                }
                return entryName;
            }
        });
    }

    private Set<FileObject> getInitialFiles(PhpModule phpModule) {
        Set<FileObject> files = new HashSet<>();
        addSourceFile(files, phpModule, "app/config/parameters.yml"); // NOI18N
        addSourceFile(files, phpModule, "src/AppBundle/Controller/DefaultController.php"); // NOI18N
        addSourceFile(files, phpModule, "app/Resources/views/default/index.html.twig"); // NOI18N
        if (files.isEmpty()) {
            addSourceFile(files, phpModule, "web/app_dev"); // NOI18N
        }
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
        if (panel == null) {
            panel = new NewProjectConfigurationPanel();
        }
        return panel;
    }

}
