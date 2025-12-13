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
package org.netbeans.modules.java.source;

import javax.lang.model.SourceVersion;

/**
 *
 * @author lahvac
 */
public class NoJavacHelper {

    public static final int REQUIRED_JAVAC_VERSION = 26; // <- TODO: increment on every release
    private static final boolean HAS_WORKING_JAVAC;

    static {
        boolean res;
        try {
            SourceVersion.valueOf("RELEASE_"+REQUIRED_JAVAC_VERSION);
            res = true;
        } catch (IllegalArgumentException ex) {
            res = false;
        }
        HAS_WORKING_JAVAC = res;
    }

    public static boolean hasWorkingJavac() {
        return HAS_WORKING_JAVAC;
    }
}
