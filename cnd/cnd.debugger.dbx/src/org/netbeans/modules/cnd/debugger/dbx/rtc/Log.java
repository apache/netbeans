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

package org.netbeans.modules.cnd.debugger.dbx.rtc;

public class Log extends org.netbeans.modules.cnd.debugger.common2.utils.LogSupport {

    public static class Rtc {
	public static final boolean debug =
	    booleanProperty("cnd.nativedebugger.Rtc.debug", false); // NOI18N
	public static final boolean traffic =
	    booleanProperty("cnd.nativedebugger.Rtc.traffic", false); // NOI18N
	public static final boolean hyperlink =
	    booleanProperty("cnd.nativedebugger.Rtc.hyperlink", false); // NOI18N
	public static final boolean godmode =
	    booleanProperty("cnd.nativedebugger.Rtc.godmode", false); // NOI18N
	public static final boolean batch =
	    booleanProperty("cnd.nativedebugger.Rtc.batch", false); // NOI18N
	public static final boolean atd =
	    booleanProperty("cnd.nativedebugger.Rtc.atd", false); // NOI18N
	public static final boolean progress =
	    booleanProperty("cnd.nativedebugger.Rtc.progress", false); // NOI18N
    }
}
