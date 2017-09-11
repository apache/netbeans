/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 2004 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */
package org.netbeans.spi.search;

import java.io.File;
import java.net.URI;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.annotations.common.NonNull;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 * Implementations of this class define which files and folders should be
 * searched and which should be skipped during search over a directory
 * structure.
 *
 * @author Marian Petras, jhavlin
 */
public abstract class SearchFilterDefinition {

    private static final Logger LOG =
            Logger.getLogger(SearchFilterDefinition.class.getName());
    /**
     * Result of filtering a folder.
     */
    public static enum FolderResult {

        /**
         * Constant representing answer &quot;do not traverse the folder&quot;.
         */
        DO_NOT_TRAVERSE,
        /**
         * Constant representing answer &quot;traverse the folder&quot;.
         */
        TRAVERSE,
        /**
         * Constant representing answer &quot;traverse the folder and all its
         * direct and indirect children (both files and subfolders)&quot;.
         */
        TRAVERSE_ALL_SUBFOLDERS
    }

    /**
     * Answers a question whether a given file should be searched. The file must
     * be a plain file (not folder).
     *
     * Every file matching SearchScopeOptions criteria will be passed to this
     * method. Please make sure that the computation is as fast as possible.
     *
     * @return
     * <code>true</code> if the given file should be searched;
     * <code>false</code> if not
     * @exception java.lang.IllegalArgumentException if the passed
     * <code>FileObject</code> is a folder
     */
    public abstract boolean searchFile(@NonNull FileObject file)
            throws IllegalArgumentException;

    /**
     * Answers a questions whether a given folder should be traversed (its
     * contents searched). The passed argument must be a folder.
     *
     * Every traversed folder will be passed to this
     * method. Please make sure the computation is as fast as possible!
     *
     * @return One of constants of {@link FolderResult}. If
     * <code>TRAVERSE_ALL_SUBFOLDERS</code> is returned, this filter will not be
     * applied on the folder's children (both direct and indirect, both files
     * and folders)
     * @exception java.lang.IllegalArgumentException if the passed
     * <code>FileObject</code> is not a folder
     */
    public abstract @NonNull FolderResult traverseFolder(
            @NonNull FileObject folder) throws IllegalArgumentException;

    /**
     * Answers a question whether a file with given URI should be searched. The
     * file must be URI of a plain file (not folder).
     *
     * The default implementation creates a {@link FileObject} instance for each
     * URI and passes it to {@link #searchFile(FileObject)}. Override to improve
     * performance.
     *
     * @return
     * <code>true</code> if the given file should be searched;
     * <code>false</code> if not
     * @exception java.lang.IllegalArgumentException if the passed
     * <code>FileObject</code> is a folder
     *
     * @since org.netbeans.api.search/1.4
     */
    public boolean searchFile(@NonNull URI uri) {
        File f = null;
        try {
            f = new File(uri);
        } catch (IllegalArgumentException iae) {
            LOG.log(Level.INFO, null, iae);
            return false;
        }
        FileObject fo = FileUtil.toFileObject(f);
        if (fo == null) {
            return false;
        } else {
            return searchFile(fo);
        }
    }

    /**
     * Answers a questions whether a folder with given URI should be traversed
     * (its contents searched). The passed argument must be URI of a folder.
     *
     * The default implementation creates a {@link FileObject} instance for each
     * URI and passes it to {@link #traverseFolder(FileObject)}. Override to
     * improve performance.
     *
     * @return One of constants of {@link FolderResult}. If
     * <code>TRAVERSE_ALL_SUBFOLDERS</code> is returned, this filter will not be
     * applied on the folder's children (both direct and indirect, both files
     * and folders)
     * @exception java.lang.IllegalArgumentException if the passed
     * <code>FileObject</code> is not a folder
     *
     * @since org.netbeans.api.search/1.4
     */
    public @NonNull FolderResult traverseFolder(
            @NonNull URI uri) throws IllegalArgumentException {
        File f = null;
        try {
            f = new File(uri);
        } catch (IllegalArgumentException iae) {
            LOG.log(Level.INFO, null, iae);
            return FolderResult.DO_NOT_TRAVERSE;
        }
        FileObject fo = FileUtil.toFileObject(f);
        if (fo == null) {
            return FolderResult.DO_NOT_TRAVERSE;
        } else {
            return traverseFolder(fo);
        }
    }
}
