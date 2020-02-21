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

package org.netbeans.modules.cnd.debugger.common2.values;

import org.openide.util.Utilities;

public class ProcessEvent extends Enum {
    private ProcessEvent(String name) {
	super(name, Catalog.get(name));
    }

    public static final ProcessEvent EXIT =
	new ProcessEvent("Process_exit");		// NOI18N
    public static final ProcessEvent NEXT =
	new ProcessEvent("Process_next");		// NOI18N
    public static final ProcessEvent STEP =
	new ProcessEvent("Process_step");		// NOI18N
    public static final ProcessEvent STOP =
	new ProcessEvent("Process_stop");		// NOI18N
    public static final ProcessEvent LASTRITES =
	new ProcessEvent("Process_lastrites");		// NOI18N
    public static final ProcessEvent GONE =
	new ProcessEvent("Process_gone");	// NOI18N
    public static final ProcessEvent LWP_EXIT =
	new ProcessEvent("Process_lwp_exit");	// NOI18N
    public static final ProcessEvent SYNC =
	new ProcessEvent("Process_sync");	// NOI18N
    public static final ProcessEvent SYNCRTLD =
	new ProcessEvent("Process_syncrtld");	// NOI18N

    private static final ProcessEvent[] linux_enumeration = {
	EXIT, NEXT, STEP, STOP, GONE, SYNCRTLD
    };

    private static final ProcessEvent[] enumeration = {
	EXIT, NEXT, STEP, STOP, LASTRITES, GONE, LWP_EXIT, SYNC, SYNCRTLD
    };

    private static String[] tags;


    public static String[] getTags() {
	if (Utilities.getOperatingSystem() == Utilities.OS_LINUX)
	    tags = makeTagsFrom(tags, linux_enumeration);
	else
	    tags = makeTagsFrom(tags, enumeration);
	return tags;
    }

    public static ProcessEvent byTag(String s) {
	if (Utilities.getOperatingSystem() == Utilities.OS_LINUX)
	    return (ProcessEvent) byTagHelp(linux_enumeration, s);
	else
	    return (ProcessEvent) byTagHelp(enumeration, s);
    }

    public static ProcessEvent valueOf(String s) {
	if (Utilities.getOperatingSystem() == Utilities.OS_LINUX)
	    return (ProcessEvent) valueOfHelp(linux_enumeration, s);
	else
	    return (ProcessEvent) valueOfHelp(enumeration, s);
    }
}
