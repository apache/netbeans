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

package org.netbeans.modules.cnd.debugger.gdb2;

public class Log extends org.netbeans.modules.cnd.debugger.common2.utils.LogSupport {

    public static class Gdb {
	public static final boolean pid =
	    booleanProperty("cnd.nativedebugger.Gdb.pid", false); // NOI18N
    }

    public static class Startup {
	public static final boolean nopty =
	    booleanProperty("cnd.nativedebugger.Startup.nopty", false); // NOI18N
    }

    public static class Variable {
	public static final boolean mi_vars =
	    booleanProperty("cnd.nativedebugger.Variable.mi_vars", false); // NOI18N
	public static final boolean mi_threads =
	    booleanProperty("cnd.nativedebugger.Variable.mi_threads", false); // NOI18N
	public static final boolean mi_frame =
	    booleanProperty("cnd.nativedebugger.Variable.mi_frame", false); // NOI18N
    }

    public static class Bpt {
	public static final boolean fix6810534 =
	    booleanProperty("cnd.nativedebugger.Bpt.fix6810534", true); // NOI18N
    }
}
