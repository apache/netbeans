/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */

package org.netbeans.modules.remote.impl.fs.server;

import org.netbeans.modules.nativeexecution.api.util.Signal;

/**
 *
 */
public class FSSExitCodes {

    // see "exitcodes.h" in fs_server sources
    
    static final int FAILURE_LOCKING_MUTEX                  = 201;
    static final int FAILURE_UNLOCKING_MUTEX                = 202;
    static final int WRONG_ARGUMENT                         = 203;
    static final int FAILURE_GETTING_HOME_DIR               = 204;
    static final int FAILURE_CREATING_STORAGE_SUPER_DIR     = 205;
    static final int FAILURE_ACCESSING_STORAGE_SUPER_DIR    = 206;
    static final int FAILURE_CREATING_STORAGE_DIR           = 207;
    static final int FAILURE_ACCESSING_STORAGE_DIR          = 208;
    static final int FAILURE_CREATING_TEMP_DIR              = 209;
    static final int FAILURE_ACCESSING_TEMP_DIR             = 210;
    static final int FAILURE_CREATING_CACHE_DIR             = 211;
    static final int FAILURE_ACCESSING_CACHE_DIR            = 212;
    static final int NO_MEMORY_EXPANDING_DIRTAB             = 213;
    static final int FAILED_CHDIR                           = 214;
    static final int FAILURE_OPENING_LOCK_FILE              = 215;
    static final int FAILURE_LOCKING_LOCK_FILE              = 216;
    static final int FAILURE_DIRTAB_DOUBLE_CACHE_OPEN       = 217;

    static final int FS_SPECIFIC_START = FAILURE_LOCKING_MUTEX;
    static final int FS_SPECIFIC_END = FAILURE_DIRTAB_DOUBLE_CACHE_OPEN;

    static final int GENERAL_ERROR = 1;
    
    public static boolean isSignal(int exitCode) {
        return getSignal(exitCode) != null;
    }
    
    public static Signal getSignal(int exitCode) {
        return Signal.valueOf(exitCode - 128);
    }
}
