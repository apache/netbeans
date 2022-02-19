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
package org.netbeans.modules.cnd.lsp.compilationdb;

/**
 * ClangCDBGenerationCause represents possible causes that require a Clang
 * compilation database to be regenerated.
 *
 * @author antonio
 */
enum ClangCDBGenerationCause {
    /**
     * Some files were added to the project
     */
    FILES_ADDED,
    /**
     * Some files were removed from the project
     */
    FILES_REMOVED,
    /**
     * Some files were renamed in the project
     */
    FILES_RENAMED,
    /**
     * Project configuration changed
     */
    PROJECT_CONFIGURATION_CHANGED,
    /**
     * Project is opened.
     */
    PROJECT_OPENED, 
    /**
     * Include paths or macro defintions have changed for
     * some files or for the whole project.
     */
    INCLUDES_MACROS_CHANGED
}
