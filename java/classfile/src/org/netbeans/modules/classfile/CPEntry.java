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
 * A class representing an entry in a ConstantPool.
 *
 * @author Thomas Ball
 */
public abstract class CPEntry {

    ConstantPool pool;
    Object value;

    CPEntry(ConstantPool pool) {
	this.pool = pool;
    }

    void resolve(CPEntry[] pool) {
        // already resolved by default
    }

    /* The VM doesn't allow the next constant pool slot to be used
     * for longs and doubles.
     */
    boolean usesTwoSlots() {
	return false;
    }
    
    public Object getValue() {
        return value;
    }

    /**
     * Returns the constant type value, or tag, as defined by
     * table 4.3 of the Java Virtual Machine specification.
     * @return constant type value
     */
    public abstract int getTag();
}

