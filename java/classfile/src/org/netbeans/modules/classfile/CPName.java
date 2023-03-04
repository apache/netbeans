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
 * Contributor(s): Thomas Ball
 */

package org.netbeans.modules.classfile;

/**
 * The base class for all constant pool types which store strings.
 *
 * @author Thomas Ball
 */
abstract class CPName extends CPEntry {

    static final int INVALID_INDEX = -1;

    int index;
    private String name;

    CPName(ConstantPool pool,int index) {
	super(pool);
        this.index = index;
    }

    CPName(ConstantPool pool) {
	super(pool);
	index = INVALID_INDEX;
    }

    public String getName() {
	if (index == INVALID_INDEX) {
	    return null;
        }            
        if (name == null) {
            name = ((CPName)pool.cpEntries[index]).getName();
        }
        return name;
    }
    
    @Override
    public Object getValue() {
        return getName();
    }

    void setNameIndex(int index) {
	this.index = index;
        name = null;
    }

    @Override
    public String toString() {
	return getClass().getName() + ": name=" + 
	    (index == INVALID_INDEX ? "<unresolved>" :  //NOI18N
	     ((CPName)pool.cpEntries[index]).getName());
    }
}
