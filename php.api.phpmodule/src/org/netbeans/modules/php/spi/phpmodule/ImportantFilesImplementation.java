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

import java.util.Collection;
import java.util.Comparator;
import javax.swing.event.ChangeListener;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.openide.filesystems.FileObject;
import org.openide.util.Parameters;

/**
 * Information about important files.
 * <p>
 * Implementations are expected to be found in project's lookup (using @ProjectServiceProvider).
 * @since 2.50
 * @see ImportantFilesSupport
 */
public interface ImportantFilesImplementation {

    /**
     * Gets information about all important files.
     * @return information about all important files; can be empty but never {@code null}
     */
    Collection<FileInfo> getFiles();

    /**
     * Adds listener to be notified when important files change.
     * @param listener listener to be notified when important files change
     */
    void addChangeListener(ChangeListener listener);

    /**
     * Removes listener.
     * @param listener listener
     */
    void removeChangeListener(ChangeListener listener);

    //~ Inner classes

    /**
     * Information about important file.
     */
    final class FileInfo {

        /**
         * Case-insensitive comparator by {@link #getDisplayName()} if possible, {@link FileObject#getNameExt()} otherwise.
         * @since 2.51
         */
        public static final Comparator<FileInfo> COMPARATOR = new Comparator<FileInfo>() {
            @Override
            public int compare(FileInfo o1, FileInfo o2) {
                String name1 = o1.getDisplayName();
                if (name1 == null) {
                    name1 = o1.getFile().getNameExt();
                }
                String name2 = o2.getDisplayName();
                if (name2 == null) {
                    name2 = o2.getFile().getNameExt();
                }
                return name1.compareToIgnoreCase(name2);
            }
        };

        private final FileObject file;
        private final String displayName;
        private final String description;


        /**
         * Creates information for the given file.
         * @param file file, cannot be a directory
         */
        public FileInfo(FileObject file) {
            this(file, null, null);
        }

        /**
         * Creates information for the given file with custom display name
         * and/or custom description.
         * @param file file, cannot be a directory
         * @param displayName custom display name, can be {@code null}
         * @param description custom description, can be {@code null}
         */
        public FileInfo(FileObject file, @NullAllowed String displayName, @NullAllowed String description) {
            Parameters.notNull("file", file); // NOI18N
            if (file.isFolder()) {
                throw new IllegalArgumentException("File cannot be a directory");
            }
            this.file = file;
            this.displayName = displayName;
            this.description = description;
        }

        /**
         * Gets file.
         * @return file
         */
        public FileObject getFile() {
            return file;
        }

        /**
         * Gets display name, can be {@code null}.
         * @return display name, can be {@code null}
         */
        @CheckForNull
        public String getDisplayName() {
            return displayName;
        }

        /**
         * Gets description, can be {@code null}.
         * @return description, can be {@code null}
         */
        @CheckForNull
        public String getDescription() {
            return description;
        }

        @Override
        public String toString() {
            return "FileInfo{" + "file=" + file + ", displayName=" + displayName + ", description=" + description + '}'; // NOI18N
        }

    }

}
