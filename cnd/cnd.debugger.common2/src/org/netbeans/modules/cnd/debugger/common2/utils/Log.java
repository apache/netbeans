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

package org.netbeans.modules.cnd.debugger.common2.utils;

import org.netbeans.modules.cnd.debugger.common2.utils.LogSupport;

public class Log extends LogSupport {

    public static class Executor {
        public static final boolean debug =
            booleanProperty("cnd.nativedebugger.Executor.debug", false); // NOI18N
    }
    public static class Ps {
        public static final boolean debug =
            booleanProperty("cnd.nativedebugger.Ps.debug", false); // NOI18N
        public static final boolean null_uid =
            booleanProperty("cnd.nativedebugger.Ps.null_uid", false); // NOI18N
    }
    public static class Progress {
        public static final boolean debug =
            booleanProperty("cnd.nativedebugger.Progress.debug", false); // NOI18N
    }
}
