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

package org.netbeans.modules.cnd.debugger.common2.debugger.breakpoints;

class Log extends org.netbeans.modules.cnd.debugger.common2.utils.LogSupport {

    public static class Bpt {
	public static final boolean enabling =
	    booleanProperty("cnd.nativedebugger.Bpt.enabling", false); // NOI18N
	public static final boolean hierarchy =
	    booleanProperty("cnd.nativedebugger.Bpt.hierarchy", false); // NOI18N
	public static final boolean xml =
	    booleanProperty("cnd.nativedebugger.Bpt.xml", false); // NOI18N
	public static final boolean pathway =
	    booleanProperty("cnd.nativedebugger.Bpt.pathway", false); // NOI18N
	public static final boolean embellish =
	    booleanProperty("cnd.nativedebugger.Bpt.embellish", false); // NOI18N
	public static final boolean model =
	    booleanProperty("cnd.nativedebugger.Bpt.model", false); // NOI18N
	public static final boolean ghostbuster =
	    booleanProperty("cnd.nativedebugger.Bpt.ghostbuster", false); // NOI18N
	public static final boolean pertarget =
	    booleanProperty("cnd.nativedebugger.Bpt.pertarget", false); // NOI18N
    }
}
