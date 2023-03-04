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
package org.netbeans.modules.css.prep.util;

import org.netbeans.modules.css.prep.CssPreprocessorType;

/**
 * Utility class for UI warnings.
 */
public final class Warnings {

    // @GuardedBy("Warnings.class")
    private static final boolean[] WARNING_SHOWN = new boolean[CssPreprocessorType.values().length];


    private Warnings() {
    }

    public static synchronized boolean showWarning(CssPreprocessorType type) {
        if (WARNING_SHOWN[type.ordinal()]) {
            return false;
        }
        WARNING_SHOWN[type.ordinal()] = true;
        return true;
    }

    public static synchronized void resetWarning(CssPreprocessorType type) {
        WARNING_SHOWN[type.ordinal()] = false;
    }

}
