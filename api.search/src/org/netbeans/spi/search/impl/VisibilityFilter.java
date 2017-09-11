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

import java.net.URI;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.queries.VisibilityQuery;
import org.netbeans.spi.search.SearchFilterDefinition;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;

/**
 * Visiblity filter that uses VisibilityQuery and check for primary files.
 *
 * @author Marian Petras
 */
public final class VisibilityFilter extends SearchFilterDefinition {

    private static final VisibilityFilter INSTANCE = new VisibilityFilter();

    private VisibilityFilter() {
    }

    /**
     */
    @Override
    public boolean searchFile(FileObject file)
            throws IllegalArgumentException {
        if (file.isFolder()) {
            throw new java.lang.IllegalArgumentException(
                    "file (not folder) expected");                      //NOI18N
        } else {
            return isPermittedByQuery(file) && isPrimaryFile(file);
        }
    }

    /**
     * Test that all visibility queries allow showing a file object.
     */
    private boolean isPermittedByQuery(FileObject file) {
        return VisibilityQuery.getDefault().isVisible(file);
    }

    /**
     * Test that a file is the primary file of its DataObject.
     */
    private boolean isPrimaryFile(FileObject file) {
        try {
            DataObject dob = DataObject.find(file);
            if (dob.getPrimaryFile().equals(file)) {
                return true;
            } else {
                return false;
            }
        } catch (DataObjectNotFoundException ex) {
            String msg = "DataObject not found for file:" + file;       //NOI18N
            Logger logger = Logger.getLogger(VisibilityFilter.class.getName());
            logger.log(Level.INFO, msg, ex);
            return true;
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
        return VisibilityQuery.getDefault().isVisible(folder)
                ? FolderResult.TRAVERSE
                : FolderResult.DO_NOT_TRAVERSE;
    }

    public static SearchFilterDefinition getInstance() {
        return INSTANCE;
    }
}
