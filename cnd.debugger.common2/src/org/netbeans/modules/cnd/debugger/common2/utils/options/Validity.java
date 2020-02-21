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

package org.netbeans.modules.cnd.debugger.common2.utils.options;

public abstract class Validity {
    private final String why;

    private Validity(String why) {
	this.why = why;
    }

    public static final Validity TRUE = new TRUE(Catalog.get("AnyValueisValid")); // NOI18N

    public abstract boolean isValid();

    public String why() { return why; }

    public static Validity TRUE(String why) {
	return new TRUE(why);
    }

    public static Validity FALSE(String why) {
	return new FALSE(why);
    }

    private static class TRUE extends Validity {
	TRUE(String why) {
	    super(why);
	}
        @Override
	public boolean isValid() { return true; }
    }

    private static class FALSE extends Validity {
	FALSE(String why) {
	    super(why);
	}
        @Override
	public boolean isValid() { return false; }
    }
}
