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
package org.netbeans.modules.php.symfony;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.event.ChangeListener;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.queries.VisibilityQuery;
import org.netbeans.modules.php.api.phpmodule.PhpModule;
import org.netbeans.modules.php.api.util.FileUtils;
import org.netbeans.modules.php.spi.phpmodule.ImportantFilesImplementation;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.FileUtil;
import org.openide.util.ChangeSupport;

public final class ConfigurationFiles extends FileChangeAdapter implements ImportantFilesImplementation {

    private static final String CONFIG_DIRECTORY = "config"; // NOI18N
    private static final Set<String> CONFIG_FILE_EXTENSIONS = new HashSet<>();

    private final PhpModule phpModule;
    private final ChangeSupport changeSupport = new ChangeSupport(this);

    // @GuardedBy("this")
    private FileObject sourceDirectory = null;


    static {
        CONFIG_FILE_EXTENSIONS.add("ini"); // NOI18N
        CONFIG_FILE_EXTENSIONS.add("yml"); // NOI18N
    }

    ConfigurationFiles(PhpModule phpModule) {
        assert phpModule != null;
        this.phpModule = phpModule;
    }

    @Override
    public Collection<FileInfo> getFiles() {
        FileObject sourceDir = getSourceDirectory();
        if (sourceDir == null) {
            // broken project
            return Collections.emptyList();
        }
        List<FileInfo> files = new ArrayList<>();
        FileObject configDir = sourceDir.getFileObject(CONFIG_DIRECTORY);
        if (configDir != null
                && configDir.isFolder()
                && configDir.isValid()) {
            List<FileObject> fileObjects = getConfigFilesRecursively(configDir);
            fileObjects.sort(new Comparator<FileObject>() {
                @Override
                public int compare(FileObject o1, FileObject o2) {
                    // php files go last
                    boolean phpFile1 = FileUtils.isPhpFile(o1);
                    boolean phpFile2 = FileUtils.isPhpFile(o2);
                    if (phpFile1 && phpFile2) {
                        return o1.getNameExt().compareTo(o2.getNameExt());
                    } else if (phpFile1) {
                        return 1;
                    } else if (phpFile2) {
                        return -1;
                    }

                    // compare extensions, then full names
                    String ext1 = o1.getExt();
                    String ext2 = o2.getExt();
                    if (ext1.equals(ext2)) {
                        return o1.getNameExt().compareToIgnoreCase(o2.getNameExt());
                    }
                    return ext1.compareToIgnoreCase(ext2);
                }
            });
            for (FileObject fo : fileObjects) {
                files.add(new FileInfo(fo));
            }
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
                addListener(new File(sources, CONFIG_DIRECTORY));
            }
        }
        return sourceDirectory;
    }

    private List<FileObject> getConfigFilesRecursively(FileObject parent) {
        List<FileObject> result = new ArrayList<>();
        for (FileObject child : parent.getChildren()) {
            if (VisibilityQuery.getDefault().isVisible(child)) {
                if (child.isData()
                        && (CONFIG_FILE_EXTENSIONS.contains(child.getExt().toLowerCase()) || FileUtils.isPhpFile(child))) {
                    result.add(child);
                } else if (child.isFolder()) {
                    result.addAll(getConfigFilesRecursively(child));
                }
            }
        }
        return result;
    }

    private void addListener(File path) {
        try {
            FileUtil.addRecursiveListener(this, path);
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
