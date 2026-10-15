/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.php.nette2;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import javax.swing.event.ChangeListener;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.modules.php.api.phpmodule.PhpModule;
import org.netbeans.modules.php.nette2.utils.Constants;
import org.netbeans.modules.php.spi.phpmodule.ImportantFilesImplementation;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.FileUtil;
import org.openide.util.ChangeSupport;

public final class ConfigurationFiles extends FileChangeAdapter implements ImportantFilesImplementation {

    private final PhpModule phpModule;
    private final ChangeSupport changeSupport = new ChangeSupport(this);

    // @GuardedBy("this")
    private FileObject sourceDirectory = null;


    ConfigurationFiles(PhpModule phpModule) {
        assert phpModule != null;
        this.phpModule = phpModule;
    }

    @Override
    public Collection<FileInfo> getFiles() {
        FileObject sourceDir = getSourceDirectory();
        if (sourceDir == null) {
            return Collections.emptyList();
        }
        List<FileInfo> files = new ArrayList<>();
        FileObject bootstrap = sourceDir.getFileObject(Constants.COMMON_BOOTSTRAP_PATH);
        if (bootstrap != null) {
            files.add(new FileInfo(bootstrap));
        }
        FileObject commonIndex = sourceDir.getFileObject(Constants.COMMON_INDEX_PATH);
        if (commonIndex != null) {
            files.add(new FileInfo(commonIndex));
        }
        FileObject extraIndex = sourceDir.getFileObject(Constants.EXTRA_INDEX_PATH);
        if (extraIndex != null) {
            files.add(new FileInfo(extraIndex));
        }
        FileObject config = sourceDir.getFileObject(Constants.COMMON_CONFIG_PATH);
        if (config != null
                && config.isFolder()
                && config.isValid()) {
            List<FileInfo> configFiles = new ArrayList<>();
            for (FileObject child : config.getChildren()) {
                if (child.isData()) {
                    configFiles.add(new FileInfo(child));
                }
            }
            configFiles.sort(FileInfo.COMPARATOR);
            files.addAll(configFiles);
        }
        return files;
    }

    @Override
    public void addChangeListener(ChangeListener listener) {
        changeSupport.addChangeListener(listener);
    }

    @Override
    public void removeChangeListener(ChangeListener listener) {
        changeSupport.removeChangeListener(listener);
    }

    private void fireChange() {
        changeSupport.fireChange();
    }

    @CheckForNull
    private synchronized FileObject getSourceDirectory() {
        if (sourceDirectory == null) {
            sourceDirectory = phpModule.getSourceDirectory();
            if (sourceDirectory != null) {
                File sources = FileUtil.toFile(sourceDirectory);
                addListener(new File(sources, Constants.COMMON_BOOTSTRAP_PATH));
                addListener(new File(sources, Constants.COMMON_INDEX_PATH));
                addListener(new File(sources, Constants.EXTRA_INDEX_PATH));
                addListener(new File(sources, Constants.COMMON_CONFIG_PATH));
            }
        }
        return sourceDirectory;
    }

    private void addListener(File path) {
        try {
            FileUtil.addFileChangeListener(this, path);
        } catch (IllegalArgumentException ex) {
            // noop, already listening...
            assert false : path;
        }
    }

    //~ FS

    @Override
    public void fileRenamed(FileRenameEvent fe) {
        fireChange();
    }

    @Override
    public void fileDeleted(FileEvent fe) {
        fireChange();
    }

    @Override
    public void fileDataCreated(FileEvent fe) {
        fireChange();
    }

    @Override
    public void fileFolderCreated(FileEvent fe) {
        fireChange();
    }

}
