/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
 */
package org.netbeans.modules.php.spi.phpmodule;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import javax.swing.event.ChangeListener;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.util.ChangeSupport;
import org.openide.util.Parameters;
import org.openide.util.WeakListeners;

/**
 * Support for {@link ImportantFilesImplementation}.
 * @since 2.50
 */
public final class ImportantFilesSupport {

    private final FileObject directory;
    final List<String> fileNames;
    final FileChangeListener fileChangeListener = new FilesListener();
    private final ChangeSupport changeSupport = new ChangeSupport(this);


    private ImportantFilesSupport(FileObject directory, String... fileNames) {
        assert directory != null;
        assert fileNames != null;
        this.directory = directory;
        this.fileNames = new CopyOnWriteArrayList<>(fileNames);
    }

    /**
     * Creates new support for the given directory and file name(s).
     * @param directory directory
     * @param fileNames file name(s)
     * @return new support
     */
    public static ImportantFilesSupport create(FileObject directory, String... fileNames) {
        Parameters.notNull("directory", directory); // NOI18N
        Parameters.notNull("fileNames", fileNames); // NOI18N
        ImportantFilesSupport support = new ImportantFilesSupport(directory, fileNames);
        directory.addFileChangeListener(WeakListeners.create(FileChangeListener.class, support.fileChangeListener, directory));
        return support;
    }

    /**
     * Gets information about all important files.
     * @param fileInfoCreator custom {@link FileInfoCreator}, can be {@code null}
     *        (in such case, {@link ImportantFilesImplementation.FileInfo#FileInfo(FileObject)} is used)
     * @return information about all important files; can be empty but never {@code null}
     */
    public Collection<ImportantFilesImplementation.FileInfo> getFiles(@NullAllowed FileInfoCreator fileInfoCreator) {
        List<ImportantFilesImplementation.FileInfo> files = new ArrayList<>();
        for (String name : fileNames) {
            FileObject fo = directory.getFileObject(name);
            if (fo != null) {
                ImportantFilesImplementation.FileInfo info = null;
                if (fileInfoCreator != null) {
                    info = fileInfoCreator.create(fo);
                }
                if (info == null) {
                    info = new ImportantFilesImplementation.FileInfo(fo);
                }
                files.add(info);
            }
        }
        if (files.isEmpty()) {
            return Collections.emptyList();
        }
        return files;
    }

    /**
     * Adds listener to be notified when important files change.
     * @param listener listener to be notified when important files change
     */
    public void addChangeListener(ChangeListener listener) {
        changeSupport.addChangeListener(listener);
    }

    /**
     * Removes listener.
     * @param listener listener
     */
    public void removeChangeListener(ChangeListener listener) {
        changeSupport.removeChangeListener(listener);
    }

    void fireChange() {
        changeSupport.fireChange();
    }

    //~ Inner classes

    /**
     * {@link ImportantFilesImplementation.FileInfo} creator for the given {@link FileObject}.
     */
    public interface FileInfoCreator {

        /**
         * Creates {@link ImportantFilesImplementation.FileInfo} for the given {@link FileObject}.
         * @param fileObject FileObject to be used
         * @return {@link ImportantFilesImplementation.FileInfo} for the given {@link FileObject}, can be {@null}
         */
        @CheckForNull
        ImportantFilesImplementation.FileInfo create(FileObject fileObject);

    }

    private final class FilesListener extends FileChangeAdapter {

        @Override
        public void fileRenamed(FileRenameEvent fe) {
            check(fe.getFile().getNameExt());
            check(fe.getName() + "." + fe.getExt()); // NOI18N
        }

        @Override
        public void fileDeleted(FileEvent fe) {
            check(fe.getFile().getNameExt());
        }

        @Override
        public void fileDataCreated(FileEvent fe) {
            check(fe.getFile().getNameExt());
        }

        private void check(String filename) {
            if (fileNames.contains(filename)) {
                fireChange();
            }
        }

    }

}
