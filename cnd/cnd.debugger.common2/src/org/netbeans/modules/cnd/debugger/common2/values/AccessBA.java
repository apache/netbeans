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

/*
 * Access breakpoint before/after enumeration
 */
public class AccessBA extends Enum {
    private AccessBA(String name) {
	super(name, NbBundle.getMessage(AccessBA.class, name));
    }

    public static final AccessBA BEFORE =
	new AccessBA("AccessBA_BEFORE");	// NOI18N
    public static final AccessBA AFTER =
	new AccessBA("AccessBA_AFTER");		// NOI18N

    private static final AccessBA[] enumeration =
	{BEFORE, AFTER};

    private static String[] tags;


    public static String[] getTags() {
	tags = makeTagsFrom(tags, enumeration);
	return tags;
    }

    public static AccessBA byTag(String s) {
	return (AccessBA) byTagHelp(enumeration, s);
    }

    public static AccessBA valueOf(String s) {
	return (AccessBA) valueOfHelp(enumeration, s);
    }
}
