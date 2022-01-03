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

public class Action extends Enum {
    private Action(String name) {
	super(name, Catalog.get(name));
    }

    public static final Action STOP =
	new Action("ActionStop");		// NOI18N
    public static final Action STOPINSTR =
	new Action("ActionStopInstr");		// NOI18N
    public static final Action TRACE =
	new Action("ActionTrace");		// NOI18N
    public static final Action TRACEINSTR =
	new Action("ActionTraceInstr");		// NOI18N
    public static final Action WHEN =
	new Action("ActionRunScript");		// NOI18N
    public static final Action WHENINSTR =
	new Action("ActionRunScriptInstr");	// NOI18N

    private static final Action[] enumeration =
	{STOP, STOPINSTR, TRACE, TRACEINSTR, WHEN, WHENINSTR};

    private static String[] tags;


    public static String[] getTags() {
	tags = makeTagsFrom(tags, enumeration);
	return tags;
    }

    public static Action byTag(String s) {
	return (Action) byTagHelp(enumeration, s);
    }

    public static Action valueOf(String s) {
	return (Action) valueOfHelp(enumeration, s);
    }
}
