/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2013, 2016 Oracle and/or its affiliates. All rights reserved.
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
 */
package org.netbeans.modules.cnd.makeproject.api.launchers;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfiguration;
import org.netbeans.modules.cnd.makeproject.spi.ProjectMetadataFactory;
import org.netbeans.modules.cnd.spi.utils.CndNotifier;
import org.netbeans.modules.cnd.utils.UIGesturesSupport;
import org.openide.filesystems.FileAttributeEvent;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.util.NbBundle.Messages;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 */
@ServiceProvider(service = ProjectMetadataFactory.class, path = "Projects/org-netbeans-modules-cnd-makeproject/" + ProjectMetadataFactory.LAYER_PATH, position = 100)
public class LaunchersProjectMetadataFactory implements ProjectMetadataFactory {

    public static final String NAME = "launcher.properties"; //NOI18N
    private static final String USG_CND_LAUNCHERS = "USG_CND_LAUNCHERS"; //NOI18N

    @Override
    public void read(FileObject projectDir) {
        if (projectDir == null || !projectDir.isValid()) {
            //there could be the situation when nbproject is invalid, for example when
            //project is remote and it was opened, no cache exists and opening IDE no connection is established
            return;
        }
        FileObject nbproject = projectDir.getFileObject(MakeConfiguration.NBPROJECT_FOLDER);
        if (nbproject != null && nbproject.isValid()) {
            FileChangeListenerImpl fileChangeListener = new FileChangeListenerImpl(projectDir);
            nbproject.addRecursiveListener(fileChangeListener);
            LaunchersRegistryFactory.getInstance(projectDir).setPrivateLaucnhersListener(fileChangeListener);  //for debugging purposes only
        }
        reload(projectDir);
    }
    
    @Override
    public void write(FileObject projectDir) {
    }

    @Messages({
        "illegal.string=Illegal string in the file {0}.\n{1}"
    })
    private static void reload(FileObject projectDir) {
        LaunchersRegistry launchersRegistry = LaunchersRegistryFactory.getInstance(projectDir);
        Properties properties = new Properties();
        final FileObject nbProjectFolder = projectDir.getFileObject(MakeConfiguration.NBPROJECT_FOLDER);
        if (nbProjectFolder == null || !nbProjectFolder.isValid()) {  // LaunchersRegistry shouldn't be updated in case the project has been deleted.
            return;
        }
        final FileObject publicLaunchers = nbProjectFolder.getFileObject(NAME);
        final FileObject privateNbFolder = projectDir.getFileObject(MakeConfiguration.NBPROJECT_PRIVATE_FOLDER);
        final FileObject privateLaunchers;
        if (privateNbFolder != null && privateNbFolder.isValid()) {
            privateLaunchers = privateNbFolder.getFileObject(NAME);
        } else {
            privateLaunchers = null;
        }
        if (publicLaunchers != null && publicLaunchers.isValid()) {
            try (InputStream inputStream = publicLaunchers.getInputStream()) {
                properties.load(inputStream);
            } catch (IOException ex) {
                //Exceptions.printStackTrace(ex);
            } catch (final IllegalArgumentException ex) {
                CndNotifier.getDefault().notifyErrorLater(Bundle.illegal_string(publicLaunchers.getPath(), ex.getMessage()));
            }
        }
        if (privateLaunchers != null && privateLaunchers.isValid()) {
            try (InputStream inputStream = privateLaunchers.getInputStream()) {
                properties.load(inputStream);
            } catch (IOException ex) {
                //Exceptions.printStackTrace(ex);
            } catch (final IllegalArgumentException ex) {
                CndNotifier.getDefault().notifyErrorLater(Bundle.illegal_string(privateLaunchers.getPath(), ex.getMessage()));
            }
        }
        
        if (launchersRegistry.load(properties)) {
            UIGesturesSupport.submit(USG_CND_LAUNCHERS, launchersRegistry.getLaunchers().size());
        }
    }

    private static class FileChangeListenerImpl implements FileChangeListener {

        private final FileObject projectDir;

        public FileChangeListenerImpl(FileObject projectDir) {
            this.projectDir = projectDir;
        }

        @Override
        public void fileFolderCreated(FileEvent fe) {
        }

        @Override
        public void fileDataCreated(FileEvent fe) {
            if (fe.getFile().getPath().endsWith(NAME)) {
                reload(projectDir);
            }
        }

        @Override
        public void fileChanged(FileEvent fe) {
            if (fe.getFile().getPath().endsWith(NAME)) {
                reload(projectDir);
            }
        }

        @Override
        public void fileDeleted(FileEvent fe) {
            if (fe.getFile().getPath().endsWith(NAME)) {
                reload(projectDir);
            }
        }

        @Override
        public void fileRenamed(FileRenameEvent fe) {
            if (fe.getFile().getPath().endsWith(NAME)) {
                reload(projectDir);
            }
        }

        @Override
        public void fileAttributeChanged(FileAttributeEvent fe) {
        }
    }
}
