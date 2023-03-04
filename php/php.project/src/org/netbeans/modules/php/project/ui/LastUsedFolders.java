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

package org.netbeans.modules.php.project.ui;

/**
 * Helper class to hold last used directories (directory keys).
 */
public final class LastUsedFolders {

    // general
    public static final String PHP_INTERPRETER = "nb.php.interpreter";
    public static final String TEST_DIR = "nb.php.test.dir";
    public static final String NEW_PROJECT = "nb.php.new.project";
    public static final String EXISTING_SOURCES = "nb.php.sources.existing";
    // local server
    public static final String DOCUMENT_ROOT = "nb.php.localServer.documentRoot";
    public static final String COPY_TARGET = "nb.php.localServer.copyTarget";
    // include path
    public static final String GLOBAL_INCLUDE_PATH = "nb.php.global.includePath";
    public static final String PROJECT_INCLUDE_PATH = "nb.php.project.includePath";
    public static final String PROJECT_PRIVATE_INCLUDE_PATH = "nb.php.project.includePath.private";
    // debugger
    public static final String DEBUGGER_PATH_MAPPING = "nb.php.debugger.pathMapping";
    // remote connections
    public static final String REMOTE_SFTP_IDENTITY_FILE = "nb.php.remote.sftp.identityFile";
    public static final String REMOTE_SFTP_KNOWN_HOSTS = "nb.php.remote.sftp.knownHosts";


    private LastUsedFolders() {
    }

}
