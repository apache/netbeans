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
package org.netbeans.modules.web.common.spi;

import java.util.Collection;
import java.util.Objects;
import javax.swing.event.ChangeListener;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.openide.filesystems.FileObject;
import org.openide.util.Parameters;

/**
 * Information about important files.
 * <p>
 * Implementations are expected to be found in project's lookup.
 * @since 1.73
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
                throw new IllegalArgumentException("File cannot be a directory (given: " + file + ")");
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

        @Override
        public int hashCode() {
            int hash = 3;
            hash = 71 * hash + Objects.hashCode(this.file);
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final FileInfo other = (FileInfo) obj;
            if (!Objects.equals(this.file, other.file)) {
                return false;
            }
            return true;
        }

    }

}
