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

public abstract class Enum {
    private final String name;
    private final String tag;	// possibly translated version of 'name'

    protected Enum(String name, String tag) {
	this.name = name;
	if (tag != null)
	    this.tag = tag;
	else
	    this.tag = name;
    }

    public String name() { return name; }

    @Override
    public String toString() { return tag; }



    /**
     * Helper for finding actual enum values by their name() value.
     */
    protected static Enum valueOfHelp(Enum [] enumeration, String s) {
	for (int ex = 0; ex < enumeration.length; ex++) {
	    if (enumeration[ex].name().equals(s))
		return enumeration[ex];
	}
	return null;
    }

    /**
     * Helper for finding actual enum values by their toString() value.
     */
    protected static Enum byTagHelp(Enum [] enumeration, String s) {
	for (int ex = 0; ex < enumeration.length; ex++) {
	    if (enumeration[ex].toString().equals(s))
		return enumeration[ex];
	}
	return null;
    }

    /**
     * Helper for creating a tags array from an enumeration
     */
    protected static String[] makeTagsFrom(String tags[], Enum enumeration[]) {
	if (tags != null)
	    return tags;
	tags = new String[enumeration.length];
	for (int ex = 0; ex < enumeration.length; ex++)
	    tags[ex] = enumeration[ex].toString();
	return tags;
    }



    // We can't really have abstract static so these are just to establish
    // the pattern:

    /**
     * Get names of all enum items
     */
    // abstract public static String[] getTags();

    /**
     * Convert a name to an enum value
     */
    // abstract public static FunctionSubEvent byTag(String s);

}
