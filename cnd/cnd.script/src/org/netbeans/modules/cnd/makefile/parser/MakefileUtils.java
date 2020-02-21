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

package org.netbeans.modules.cnd.makefile.parser;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 *
 */
public final class MakefileUtils {

    private MakefileUtils() {}

    private static final Set<String> PREFERRED_TARGETS = new HashSet<String>(Arrays.asList(
            // see http://www.gnu.org/prep/standards/html_node/Standard-Targets.html
            "all", // NOI18N
            "install", // NOI18N
            "uninstall", // NOI18N
            "clean", // NOI18N
            "distclean", // NOI18N
            "dist", // NOI18N
            "check", // NOI18N

            // targets written by CND
            "build", // NOI18N
            "build-tests", // NOI18N
            "clobber", // NOI18N
            "help", // NOI18N
            "test")); // NOI18N

    public static boolean isPreferredTarget(String target) {
        return PREFERRED_TARGETS.contains(target);
    }

    public static boolean isRunnableTarget(String target) {
        return 0 < target.length() && target.charAt(0) != '.' && !target.contains("%"); // NOI18N
    }
}
