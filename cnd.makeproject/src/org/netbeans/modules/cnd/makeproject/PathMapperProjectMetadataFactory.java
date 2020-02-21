/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2014, 2016 Oracle and/or its affiliates. All rights reserved.
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
package org.netbeans.modules.cnd.makeproject;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import org.netbeans.modules.cnd.makeproject.NativeProjectRelocationMapperProviderImpl.ProjectMapper;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfiguration;
import org.netbeans.modules.cnd.makeproject.spi.ProjectMetadataFactory;
import org.openide.filesystems.FileAttributeEvent;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 */
@ServiceProvider(service = ProjectMetadataFactory.class, path = "Projects/org-netbeans-modules-cnd-makeproject/" + ProjectMetadataFactory.LAYER_PATH, position = 90)
public class PathMapperProjectMetadataFactory implements ProjectMetadataFactory {

    public static final String NAME = "path_mapper.properties"; //NOI18N

    @Override
    public void read(FileObject projectDir) {
        FileObject nbproject = projectDir.getFileObject(MakeConfiguration.NBPROJECT_FOLDER);
        // check nbproject in case it was deleted while opening
        if (nbproject != null && nbproject.isValid()) {
            FileChangeListenerImpl fileChangeListener = new FileChangeListenerImpl(projectDir);
            nbproject.addFileChangeListener(fileChangeListener);
            initListeners(fileChangeListener, projectDir);
            reload(projectDir);
        }
    }

    private void initListeners(FileChangeListener fileChangeListener, FileObject projectDir) {
        FileObject nbproject = projectDir.getFileObject(MakeConfiguration.NBPROJECT_FOLDER);
        if (nbproject != null && nbproject.isValid()) {
            FileObject publicPathMapper = nbproject.getFileObject(NAME);
            if (publicPathMapper != null) {
                publicPathMapper.removeFileChangeListener(fileChangeListener);
                publicPathMapper.addFileChangeListener(fileChangeListener);
            }
            final FileObject privateNbFolder = projectDir.getFileObject(MakeConfiguration.NBPROJECT_PRIVATE_FOLDER);
            if (privateNbFolder != null && privateNbFolder.isValid()) {
                privateNbFolder.removeFileChangeListener(fileChangeListener);
                privateNbFolder.addFileChangeListener(fileChangeListener);
                FileObject privatePathMapper = privateNbFolder.getFileObject(NAME);
                if (privatePathMapper != null) {
                    privatePathMapper.removeFileChangeListener(fileChangeListener);
                    privatePathMapper.addFileChangeListener(fileChangeListener);
                }
            }
        }
    }

    @Override
    public void write(FileObject projectDir) {
    }

    private static void reload(FileObject projectDir) {
        ProjectMapper projectMapper = NativeProjectRelocationMapperProviderImpl.get(projectDir);
        Properties properties = new Properties();
        final FileObject nbProjectFolder = projectDir.getFileObject(MakeConfiguration.NBPROJECT_FOLDER);
        if (nbProjectFolder == null) {  
            return;
        }
        FileObject publicMappers = nbProjectFolder.getFileObject(NAME);
        final FileObject privateNbFolder = projectDir.getFileObject(MakeConfiguration.NBPROJECT_PRIVATE_FOLDER);
        FileObject privateMappers = null;
        if (privateNbFolder != null && privateNbFolder.isValid()) {
            privateMappers = privateNbFolder.getFileObject(NAME);
        }
        try {
            if (publicMappers != null && publicMappers.isValid()) {
                final InputStream inputStream = publicMappers.getInputStream();
                properties.load(inputStream);
                inputStream.close();
            }
            if (privateMappers != null && privateMappers.isValid()) {
                final InputStream inputStream = privateMappers.getInputStream();
                properties.load(inputStream);
                inputStream.close();
            }
        } catch (IOException ex) {
            //Exceptions.printStackTrace(ex);
        }
        projectMapper.load(properties, projectDir);
    }

    private class FileChangeListenerImpl implements FileChangeListener {

        private final FileObject projectDir;

        public FileChangeListenerImpl(FileObject projectDir) {
            this.projectDir = projectDir;
        }

        @Override
        public void fileFolderCreated(FileEvent fe) {
            //private folder could be created here, in this case: let's listen 
            initListeners(this, projectDir);
        }

        @Override
        public void fileDataCreated(FileEvent fe) {
            //listen for the new file: attach listener to the file to listen for changes
            initListeners(this, projectDir);
            reload(projectDir);
        }

        @Override
        public void fileChanged(FileEvent fe) {
            reload(projectDir);
        }

        @Override
        public void fileDeleted(FileEvent fe) {
            fe.getFile().removeFileChangeListener(this);
            reload(projectDir);
        }

        @Override
        public void fileRenamed(FileRenameEvent fe) {
            reload(projectDir);
        }

        @Override
        public void fileAttributeChanged(FileAttributeEvent fe) {
        }
    }
}
