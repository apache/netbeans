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
package org.netbeans.modules.openide.util;

import org.openide.util.Lookup;

public class GlobalLookup {
    private static final ThreadLocal<Lookup> CURRENT = new ThreadLocal<Lookup>();
    
    /**
     * The true system Lookup, the main, holy and only.
     */
    private static volatile Lookup systemLookup;
    
    private GlobalLookup() {
    }

    @SuppressWarnings("AssignmentToMethodParameter")
    public static boolean execute(Lookup defaultLookup, Runnable r) {
        if (defaultLookup == null) {
            defaultLookup = systemLookup;
        }
        Lookup prev = CURRENT.get();
        if (prev == defaultLookup) {
            return false;
        }
        try {
            CURRENT.set(defaultLookup);
            r.run();
        } finally {
            CURRENT.set(prev);
        }
        return true;
    }
    
    public static Lookup current() {
        return CURRENT.get();
    }
    
    public static void setSystemLookup(Lookup lkp) {
        systemLookup = lkp;
    }
}
