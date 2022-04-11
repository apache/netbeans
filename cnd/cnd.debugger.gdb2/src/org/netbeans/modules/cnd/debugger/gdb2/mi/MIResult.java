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

package org.netbeans.modules.cnd.debugger.gdb2.mi;


/**
 * Representation of a 'result' sub-tree from the MI spec.
 * A 'result' always looks like this:
 * <ul>
 * <li> variable "=" value
 * </ul>
 */

public class MIResult extends MITListItem {
    private final String variable;
    private final MIValue value;

    MIResult(String variable, MIValue value) {
	assert variable != null;
	assert value != null;
	this.variable = variable;
	this.value = value;
    }

    @Override
    public String toString() {
	return variable + "=" + value.toString(); // NOI18N
    } 


    /**
     * Retrieve 'variable' part.
     */

    public String variable() {
	return variable;
    } 

    /**
     * Retrieve 'value' part.
     */

    public MIValue value() {
	return value;
    } 


    /**
     * Return true if this 'result' is of the given variable.
     */

    public boolean matches(String variable) {
	return this.variable.equals(variable);
    } 
}
