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

/*
 * Exception breakpoint specification.
 */

public class ExceptionSpec extends Enum {
    private ExceptionSpec(String name) {
	super(name, name);
    }

    public static final ExceptionSpec ALL =
	new ExceptionSpec(Catalog.get("ExceptionSpec_ALL"));	// NOI18N
    public static final ExceptionSpec UNCAUGHT =
	new ExceptionSpec(Catalog.get("ExceptionSpec_UNCAUGHT"));// NOI18N
    public static final ExceptionSpec UNEXPECTED =
	new ExceptionSpec(Catalog.get("ExceptionSpec_UNEXPECTED"));// NOI18N

    private static final Enum[] enumeration = {
	ALL, UNCAUGHT, UNEXPECTED
    };

    private static String[] tags;


    public static String[] getTags() {
	tags = makeTagsFrom(tags, enumeration);
	return tags;
    }

    public static ExceptionSpec byTag(String s) {
	s = s.trim();
	ExceptionSpec es = (ExceptionSpec) byTagHelp(enumeration, s);
	if (es == null)
	    es = new ExceptionSpec(s);
	return es;
    }

    public static ExceptionSpec valueOf(String s) {
	ExceptionSpec es = (ExceptionSpec) valueOfHelp(enumeration, s);
	if (es == null)
	    es = new ExceptionSpec(s);
	return es;
    }
}
