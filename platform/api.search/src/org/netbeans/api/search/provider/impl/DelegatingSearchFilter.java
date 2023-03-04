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
package org.netbeans.api.search.provider.impl;

import java.net.URI;
import org.netbeans.api.search.provider.SearchFilter;
import org.netbeans.spi.search.SearchFilterDefinition;
import org.openide.filesystems.FileObject;

/**
 *
 * @author jhavlin
 */
public class DelegatingSearchFilter extends SearchFilter {

    private SearchFilterDefinition definition;

    public DelegatingSearchFilter(SearchFilterDefinition definition) {
        this.definition = definition;
    }

    @Override
    public boolean searchFile(FileObject file) throws IllegalArgumentException {
        return definition.searchFile(file);
    }

    @Override
    public boolean searchFile(URI fileUri) {
        return definition.searchFile(fileUri);
    }

    @Override
    public FolderResult traverseFolder(URI folderUri)
            throws IllegalArgumentException {
        return definitionToClientFolderResult(
                definition.traverseFolder(folderUri));
    }

    @Override
    public FolderResult traverseFolder(FileObject folder)
            throws IllegalArgumentException {
        return definitionToClientFolderResult(
                definition.traverseFolder(folder));
    }

    private FolderResult definitionToClientFolderResult(
            SearchFilterDefinition.FolderResult result) {
        switch (result) {
            case DO_NOT_TRAVERSE:
                return FolderResult.DO_NOT_TRAVERSE;
            case TRAVERSE:
                return FolderResult.TRAVERSE;
            case TRAVERSE_ALL_SUBFOLDERS:
                return FolderResult.TRAVERSE_ALL_SUBFOLDERS;
            default:
                assert false;
                return FolderResult.TRAVERSE;
        }
    }
}
