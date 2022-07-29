/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.bugtracking.spi;

import org.openide.filesystems.FileObject;

/**
 * Provides information if a file managed in the IDE is somehow associated with 
 * a bugtracking repository.
 * <p>
 * Note that this interface is not meant to be implemented by bugtracking plugins.
 * 
 * @author Tomas Stupka
 * @since 1.85
 */
public interface RepositoryQueryImplementation {
    
    /**
     * Determines the remote bugtracking repository url. 
     * 
     * @param fileObject
     * @return the remote repository url or null if not available
     * @since 1.85
     */
    public String getRepositoryUrl(FileObject fileObject);
}
