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
package org.netbeans.spi.search.impl;

import java.io.File;
import java.net.URI;
import org.netbeans.api.queries.SharabilityQuery;
import org.netbeans.api.queries.SharabilityQuery.Sharability;
import org.netbeans.spi.search.SearchFilterDefinition;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Marian Petras
 */
public final class SharabilityFilter extends SearchFilterDefinition {

    private static final SharabilityFilter INSTANCE = new SharabilityFilter();

    private SharabilityFilter() {
    }

    /**
     */
    @Override
    public boolean searchFile(FileObject file)
            throws IllegalArgumentException {
        if (file.isFolder()) {
            throw new java.lang.IllegalArgumentException(
                    "file (not folder) expected");                      //NOI18N
        }
        File f = FileUtil.toFile(file);
        if (f == null && !file.canWrite()) {
            // non-standard file objects, e.g. ZIP archive items.
            return true;
        } else {
            return SharabilityQuery.getSharability(file)
                    != Sharability.NOT_SHARABLE;
        }
    }

    @Override
    public boolean searchFile(URI uri) {
        return SharabilityQuery.getSharability(uri)
                != Sharability.NOT_SHARABLE;
    }

    @Override
    public FolderResult traverseFolder(URI uri)
            throws IllegalArgumentException {
        switch (SharabilityQuery.getSharability(uri)) {
            case SHARABLE:
                return FolderResult.TRAVERSE_ALL_SUBFOLDERS;
            case MIXED:
                return FolderResult.TRAVERSE;
            case UNKNOWN:
                return FolderResult.TRAVERSE;
            case NOT_SHARABLE:
                return FolderResult.DO_NOT_TRAVERSE;
            default:
                return FolderResult.TRAVERSE;
        }
    }

    /**
     */
    @Override
    public FolderResult traverseFolder(FileObject folder)
            throws IllegalArgumentException {
        if (!folder.isFolder()) {
            throw new java.lang.IllegalArgumentException(
                    "folder expected");                                 //NOI18N
        }
        File f = FileUtil.toFile(folder);
        if (f == null && !folder.canWrite()) {
            // non-standard file objects, e.g. ZIP archive items.
            return FolderResult.TRAVERSE;
        } else {
            Sharability sharability = SharabilityQuery.getSharability(folder);
            switch (sharability) {
                case NOT_SHARABLE:
                    return FolderResult.DO_NOT_TRAVERSE;
                case SHARABLE:
                    return FolderResult.TRAVERSE_ALL_SUBFOLDERS;
                default:
                    return FolderResult.TRAVERSE;
            }
        }
    }

    public static SearchFilterDefinition getInstance() {
        return INSTANCE;
    }
}
