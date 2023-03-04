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

package org.netbeans.spi.jumpto.file;

import javax.swing.Icon;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.spi.jumpto.support.Descriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 * A FileDescriptor describes a file to show in the Go To File dialog.
 * @since 1.15
 * @author Tomas Zezula
 *
 */
public abstract class FileDescriptor extends Descriptor {

    private boolean preferred;
    private int lineNr = -1;

    /**
     * Returns a file display name
     * @return The file display name.
     */
    public abstract String getFileName();

    /**
     * The path to the file owner (the directory
     * where the file is located). If possible relative
     * to {@link SourceGroup}.
     * @return The path to owning directory.
     */
    public abstract String getOwnerPath();

    /**
     * Return an icon that should be shown for this {@link FileDescriptor}.
     * @return An Icon to be shown on the left hand side with the file entry.
     */
    public abstract  Icon getIcon();

    /**
     * Return the display name of the project owning the file containing the
     * symbol declaration.
     * @return The display name of the project owning the file.
     */
    public abstract String getProjectName();

    /**
     * Return an icon that is applicable for the project owning the file containing the
     * symbol declaration.
     * Generally, this should be the same as the project icon.  This method will only
     * be called if {@link #getProjectName} returned a non-null value.
     * @return A project icon corresponding to the project owning the file
     */
    public abstract  Icon getProjectIcon();

    /**
     * Opens the file in the editor.
     */
    public abstract void open();

    /**
     * Return a FileObject for this {@link FileDescriptor}.
     * This will only be called when the dialog is opening the type or when
     * the user selects the file, so it does not have to be as fast as the other
     * descriptor attributes.
     *
     * @return The file object.
     */
    public abstract FileObject getFileObject();

    /**
     * Returns a display name of the {@link FileDescriptor}.
     * Default implementation uses
     * <code>
     * FileUtil.getFileDisplayName(getFileObject())
     * </code>.
     * Could be overridden if a more efficient way could be provided.
     * Threading: This method is invoked in the EDT.
     *
     * @return The string representation of the path of the associated file.
     * @since 1.33
     */
     public String getFileDisplayPath() {
        final FileObject fo = getFileObject();
        return fo == null ? "" : FileUtil.getFileDisplayName(fo); // NOI18N
    }

     /**
      * Returns a line number on which the file should be opened.
      * @return the preferred line number or -1 if no preferred line number is given.
      * @since 1.47
      */
     protected final int getLineNumber() {
         return lineNr;
     }

    //<editor-fold defaultstate="collapsed" desc="Package private methods">
    final boolean isFromCurrentProject() {
        return preferred;
    }

    final void setFromCurrentProject(final boolean preferred) {
        this.preferred = preferred;
    }

    final void setLineNumber(final int lineNr) {
        this.lineNr = lineNr;
    }
    //</editor-fold>

}
