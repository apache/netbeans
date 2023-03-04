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
package org.netbeans.modules.jshell.support;

import org.openide.filesystems.FileObject;

/**
 *
 * @author sdedic
 */
public interface SnippetStorage {
    static final String ACTION_RUN = "run";
    static final String ACTION_DEBUG = "debug";
    static final String ACTION_LAUNCH = "launch";
    
    /**
     * Existing storage folder. For project systems, some existing folder underneath
     * the project; possibly project root.
     * @return 
     */
    public FileObject   getStorageFolder(boolean createIfMissing);
    
    /**
     * Path prefix where the snippets reside. Path prefix is provided in order to avoid
     * premature creation of folders. All files listed from or stored to this storage
     * will be located under <code>{@link #getStorageFolder()}.createFolder(resourcePrefix)</code>
     * @return resouce prefix within the storage folder.
     */
    public String       resourcePrefix();
    
    /**
     * Name of a folder which holds classes (snippets), that should be run
     * during shell startup. Null means the startup folder is not defined.
     * 'runAction' parameter determines the startup mode for the JShell VM. Currently,
     * it may be one of the ACTION_* constants. The implementation may or may not use
     * this value to select the startup folder.
     * 
     * @return startup snippet folder, or {@code null}.
     */
    public String       startupSnippets(String runAction);
}
