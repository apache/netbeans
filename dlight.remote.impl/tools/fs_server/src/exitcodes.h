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
 */

#ifndef EXITCODES_H
#define	EXITCODES_H

#ifndef FS_COMMON_H
#error fs_common.h MUST be included before any other file
#endif

#define FAILURE_LOCKING_MUTEX                   201
#define FAILURE_UNLOCKING_MUTEX                 202
#define WRONG_ARGUMENT                          203
#define FAILURE_GETTING_HOME_DIR                204
#define FAILURE_CREATING_STORAGE_SUPER_DIR      205
#define FAILURE_ACCESSING_STORAGE_SUPER_DIR     206
#define FAILURE_CREATING_STORAGE_DIR            207
#define FAILURE_ACCESSING_STORAGE_DIR           208
#define FAILURE_CREATING_TEMP_DIR               209
#define FAILURE_ACCESSING_TEMP_DIR              210
#define FAILURE_CREATING_CACHE_DIR              211
#define FAILURE_ACCESSING_CACHE_DIR             212
#define NO_MEMORY_EXPANDING_DIRTAB              213
#define FAILED_CHDIR                            214
#define FAILURE_OPENING_LOCK_FILE               215
#define FAILURE_LOCKING_LOCK_FILE               216
#define FAILURE_DIRTAB_DOUBLE_CACHE_OPEN        217
#define DIRTAB_DOUBLE_INITIALIZATION                218
#define DIRTAB_SET_PERSIST_DIR_AFTER_INITIALIZATION 219
//#define DIRTAB_SET_PERSIST_INEXISTENT               220
#define FAILURE_SETTING_SIGNAL_HANDLER              221
#define FAILURE_SETTING_EXIT_FUNCTION               222
#define FAILURE_ILLEGAL_ARGUMENT                    223
#define FAILURE_ALLOCATE_MEMORY                     224
#define FAILURE_REALLOCATE_MEMORY                   225

#endif	/* EXITCODES_H */

