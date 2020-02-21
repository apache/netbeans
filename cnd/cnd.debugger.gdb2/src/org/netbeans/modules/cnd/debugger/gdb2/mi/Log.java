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

package org.netbeans.modules.cnd.debugger.gdb2.mi;

public class Log extends org.netbeans.modules.cnd.debugger.common2.utils.LogSupport {

    public static class MI {
	public static final boolean finish =
	    booleanProperty("cnd.nativedebugger.MI.finish", false); // NOI18N
	public static final boolean ui =
	    booleanProperty("cnd.nativedebugger.MI.userinteraction", false); // NOI18N
	public static final boolean time =
	    booleanProperty("cnd.nativedebugger.MI.time", false); // NOI18N
	public static final boolean echo =
	    booleanProperty("cnd.nativedebugger.MI.echo", false); // NOI18N
	public static final boolean ttrace =
	    booleanProperty("cnd.nativedebugger.MI.ttrace", false); // NOI18N
	public static final boolean mimicmac =
	    booleanProperty("cnd.nativedebugger.MI.mimicmac", false); // NOI18N
    }
}
