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
 * A class representing the CONSTANT_MethodType constant pool type.
 *
 * @author Thomas Ball
 * @since 1.40
 */
public class CPMethodTypeInfo extends CPEntry {
    int iDescriptor;

    CPMethodTypeInfo(ConstantPool pool, int iDescriptor) {
	super(pool);
        this.iDescriptor = iDescriptor;
    }

    public int getTag() {
	return ConstantPool.CONSTANT_MethodType;
    }

    public int getDescriptor() {
        return iDescriptor;
    }

    @Override
    public String toString() {
        return getClass().getName() + ": descriptor=" + iDescriptor; //NOI18N
    }
}
