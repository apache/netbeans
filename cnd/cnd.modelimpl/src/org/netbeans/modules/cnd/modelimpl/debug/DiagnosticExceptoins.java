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
package org.netbeans.modules.cnd.modelimpl.debug;

import org.netbeans.modules.cnd.api.model.CsmUID;
import org.netbeans.modules.cnd.modelimpl.uid.KeyBasedUID;
import org.netbeans.modules.cnd.repository.spi.Key;
import org.netbeans.modules.cnd.repository.support.RepositoryTestUtils;
import org.netbeans.modules.cnd.utils.CndUtils;

/**
 * Allows to get control as soon as an exception occurs
 * in one of the code model threads
 * (parser thread, repository writing thread, code model request processor)
 * 
 * Use for testing purposes only
 * 
 */
public class DiagnosticExceptoins {

    public static final int LimitMultiplyDiagnosticExceptions = 3;

    private DiagnosticExceptoins() {
    }

    public interface Hook {

        /**
         * Is called whenether an exception or error occurs
         * in one of the code model threads
         * (parser thread, repository writing thread,
         * code model request processor)
         */
        void exception(Throwable thr);
    }
    private static Hook hook;

    public static void setHook(Hook aHook) {
        hook = aHook;
    }

    /**
     * This method is called from within catch(...) in code model threads.
     * See Hook.exception description for more details
     */
    public static void register(Throwable thr) {
        CndUtils.printStackTraceOnce(thr);
        Hook aHook = hook;
        if (aHook != null) {
            hook.exception(thr);
        }
    }
    
    public static void registerIllegalRepositoryStateException(String text, Key key) {
        register(new IllegalRepositoryStateException(text, key));
        RepositoryTestUtils.debugDump(key);
    }
    
    public static void registerIllegalRepositoryStateException(String text, CsmUID uid) {
        if (uid instanceof KeyBasedUID) {
            registerIllegalRepositoryStateException(text, ((KeyBasedUID) uid).getKey());
        } else {
            register(new IllegalRepositoryStateException(text, uid));
        }
    }
}
