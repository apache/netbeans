/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

/*
 * "InterpKit.java"
 * InterpKit.java 1.3 01/07/23
 * The abstract operations the terminal can perform.
 */

package org.netbeans.lib.terminalemulator;


/*
 * Registry and locator for various built-in Interp's
 */

abstract class InterpKit {
    static Interp forName(String name, Ops ops) {
	switch (name) {
	    case "xterm":	// NOI18N
	    case "xterm-16color":	// NOI18N
		return new InterpXTerm(ops);
	    case "dumb":	// NOI18N
		return new InterpDumb(ops);
	    case "ansi":	// NOI18N
		return new InterpANSI(ops);
	    case "dtterm":	// NOI18N
		return new InterpDtTerm(ops);
	    default:
		return null;
	}
    } 
}
