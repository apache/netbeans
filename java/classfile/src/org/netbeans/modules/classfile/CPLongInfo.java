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
/*
 * Contributor(s): Thomas Ball
 */

package org.netbeans.modules.classfile;


/**
 * A class representing the CONSTANT_Long constant pool type.
 *
 * @author Thomas Ball
 */
public final class CPLongInfo extends CPEntry {

    CPLongInfo(ConstantPool pool, long v) {
	super(pool);
        value = v;
    }

    /* The VM doesn't allow the next constant pool slot to be used
     * for longs and doubles.
     */
    @Override
    boolean usesTwoSlots() {
	return true;
    }

    @Override
    public final int getTag() {
	return ConstantPool.CONSTANT_Long;
    }
    
    @Override
    public String toString() {
        return getClass().getName() + ": value=" + value; //NOI18N
    }
}
