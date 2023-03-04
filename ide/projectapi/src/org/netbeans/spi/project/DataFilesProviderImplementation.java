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
package org.netbeans.spi.project;

import java.util.List;
import org.openide.filesystems.FileObject;

/**
 * Base for various Project Operations, allows to gather metadata and data files
 * for a project.
 *
 * @author Jan Lahoda
 * @since 1.7
 */
public interface DataFilesProviderImplementation {

    /**
     * Returns list of {@link FileObject}s that are considered to be metadata files
     * and folders belonging into this project.
     * See {@link org.netbeans.spi.project.support.ProjectOperations#getMetadataFiles(Project)} for more information.
     *
     * @return list of metadata files and folders
     */
    List<FileObject> getMetadataFiles();
    
    /**
     * Returns list of {@link FileObject}s that are considered to be data files and folders
     * belonging into this project.
     * See {@link org.netbeans.spi.project.support.ProjectOperations#getDataFiles(Project)} for more information.
     *
     * @return list of data files and folders
     */
    List<FileObject> getDataFiles();
    
}
