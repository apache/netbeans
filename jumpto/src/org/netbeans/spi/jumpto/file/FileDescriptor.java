/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.netbeans.spi.jumpto.file;

import javax.swing.Icon;
import org.netbeans.api.project.SourceGroup;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 * A FileDescriptor describes a file to show in the Go To File dialog.
 * @since 1.15
 * @author Tomas Zezula
 *
 */
public abstract class FileDescriptor {

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
