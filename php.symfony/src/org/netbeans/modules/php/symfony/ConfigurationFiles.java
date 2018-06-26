/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2015 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Portions Copyrighted 2015 Sun Microsystems, Inc.
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
            Collections.sort(fileObjects, new Comparator<FileObject>() {
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
