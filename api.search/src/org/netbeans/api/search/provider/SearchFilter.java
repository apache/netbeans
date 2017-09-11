/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.api.search.provider;

import java.net.URI;
import org.netbeans.api.annotations.common.NonNull;
import org.openide.filesystems.FileObject;

/**
 * Implementation of search filter that is associated with a search root.
 *
 * All search providers should respect search filters. It is only relevant if
 * custom algorithm for for traversing is used instead of standard iterating.
 *
 * @author jhavlin
 */
public abstract class SearchFilter {

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
     * @return
     * <code>true</code> if the given file should be searched;
     * <code>false</code> if not
     * @exception java.lang.IllegalArgumentException if the passed
     * <code>FileObject</code> is a folder
     */
    public abstract boolean searchFile(@NonNull FileObject file)
            throws IllegalArgumentException;

    /**
     * Answers a question whether a given URI should be searched. The URI must
     * stand for a plain file (not folder).
     *
     * @return
     * <code>true</code> if the given file should be searched;
     * <code>false</code> if not
     * @exception java.lang.IllegalArgumentException if the passed
     * <code>URI</code> is a folder
     *
     * @since org.netbeans.api.search/1.4
     */
    public abstract boolean searchFile(@NonNull URI fileUri);

    /**
     * Answers a questions whether a given folder should be traversed (its
     * contents searched). The passed argument must be a folder.
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
     * Answers a questions whether a given URI should be traversed (its
     * contents searched). The passed URI must stand for a folder.
     *
     * @return One of constants of {@link FolderResult}. If
     * <code>TRAVERSE_ALL_SUBFOLDERS</code> is returned, this filter will not be
     * applied on the folder's children (both direct and indirect, both files
     * and folders)
     * @exception java.lang.IllegalArgumentException if the passed
     * <code>URI</code> is not a folder
     *
     * @since org.netbeans.api.search/1.4
     */
    public abstract @NonNull FolderResult traverseFolder(
            @NonNull URI folderUri) throws IllegalArgumentException;
}
