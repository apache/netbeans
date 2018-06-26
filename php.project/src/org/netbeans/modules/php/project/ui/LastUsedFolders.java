/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
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
