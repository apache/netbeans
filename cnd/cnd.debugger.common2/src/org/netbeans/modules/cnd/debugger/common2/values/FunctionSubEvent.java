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

import org.openide.util.NbBundle;

public class FunctionSubEvent extends Enum {
    private FunctionSubEvent(String name) {
	super(name, NbBundle.getMessage(FunctionSubEvent.class, name));
    }

    public static final FunctionSubEvent IN =
	new FunctionSubEvent("FunctionSubEvent_IN");		// NOI18N
    public static final FunctionSubEvent INFUNCTION =
	new FunctionSubEvent("FunctionSubEvent_INFUNCTION");	// NOI18N
    public static final FunctionSubEvent RETURNS =
	new FunctionSubEvent("FunctionSubEvent_RETURNS");	// NOI18N

    private static final FunctionSubEvent[] enumeration =
	{IN, INFUNCTION, RETURNS};

    private static String[] tags;


    public static String[] getTags() {
	tags = makeTagsFrom(tags, enumeration);
	return tags;
    }

    public static FunctionSubEvent byTag(String s) {
	return (FunctionSubEvent) byTagHelp(enumeration, s);
    }

    public static FunctionSubEvent valueOf(String s) {
	return (FunctionSubEvent) valueOfHelp(enumeration, s);
    }
}
