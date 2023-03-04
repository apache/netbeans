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
