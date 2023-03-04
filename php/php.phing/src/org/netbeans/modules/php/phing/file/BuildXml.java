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
package org.netbeans.modules.php.phing.file;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.ChangeListener;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.FileUtil;
import org.openide.util.ChangeSupport;

public final class BuildXml {

    private static final Logger LOGGER = Logger.getLogger(BuildXml.class.getName());

    public static final String FILE_NAME = "build.xml"; // NOI18N

    private final FileObject directory;
    private final FileChangeListener directoryListener = new DirectoryListener();
    private final FileChangeListener buildXmlListener = new BuildXmlListener();
    private final ChangeSupport changeSupport = new ChangeSupport(this);

    // @GuardedBy("this")
    private File buildXml;


    public BuildXml(FileObject directory) {
        assert directory != null;
        assert directory.isFolder() : "Must be folder: " + directory;
        this.directory = directory;
        FileUtil.addFileChangeListener(directoryListener, FileUtil.toFile(directory));
    }

    public boolean exists() {
        return getBuildXml().isFile();
    }

    public File getFile() {
        return getBuildXml();
    }

    public void addChangeListener(ChangeListener listener) {
        changeSupport.addChangeListener(listener);
    }

    public void removeChangeListener(ChangeListener listener) {
        changeSupport.removeChangeListener(listener);
    }

    private synchronized File getBuildXml() {
        if (buildXml == null) {
            buildXml = new File(FileUtil.toFile(directory), FILE_NAME);
            try {
                FileUtil.addFileChangeListener(buildXmlListener, buildXml);
                LOGGER.log(Level.FINE, "Started listening to {0}", buildXml);
            } catch (IllegalArgumentException ex) {
                // ignore, already listening
                LOGGER.log(Level.FINE, "Already listening to {0}", buildXml);
            }
        }
        return buildXml;
    }

    void reset(boolean newFile) {
        if (newFile) {
            synchronized (this) {
                if (buildXml != null) {
                    try {
                        FileUtil.removeFileChangeListener(buildXmlListener, buildXml);
                        LOGGER.log(Level.FINE, "Stopped listening to {0}", buildXml);
                    } catch (IllegalArgumentException ex) {
                        // not listeneing yet, ignore
                        LOGGER.log(Level.FINE, "Not listening yet to {0}", buildXml);
                    }
                    buildXml = null;
                }
            }
        }
        // fire change
        changeSupport.fireChange();
    }

    //~ Inner classes

    private final class DirectoryListener extends FileChangeAdapter {

        @Override
        public void fileRenamed(FileRenameEvent fe) {
            reset(true);
        }

    }

    private final class BuildXmlListener extends FileChangeAdapter {

        @Override
        public void fileDataCreated(FileEvent fe) {
            reset(true);
        }

        @Override
        public void fileChanged(FileEvent fe) {
            reset(false);
        }

        @Override
        public void fileDeleted(FileEvent fe) {
            reset(true);
        }

        @Override
        public void fileRenamed(FileRenameEvent fe) {
            reset(true);
        }

    }

}
