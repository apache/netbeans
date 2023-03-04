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
 * A class representing the CONSTANT_NameAndType constant pool type.
 *
 * @author Thomas Ball
 */
public class CPNameAndTypeInfo extends CPEntry {
    int iName;
    int iDesc;

    CPNameAndTypeInfo(ConstantPool pool,int iName,int iDesc) {
	super(pool);
        this.iName = iName;
        this.iDesc = iDesc;
    }

    protected CPNameAndTypeInfo(ConstantPool pool) {
        super(pool);
        iName = CPName.INVALID_INDEX;
        iDesc = CPName.INVALID_INDEX;
    }

    public final String getName() {
	return ((CPName)pool.cpEntries[iName]).getName();
    }

    void setNameIndex(int index) {
	iName = index;
    }

    public final String getDescriptor() {
	return ((CPName)pool.cpEntries[iDesc]).getName();
    }

    void setDescriptorIndex(int index) {
	iDesc = index;
    }

    public int getTag() {
	return ConstantPool.CONSTANT_NameAndType;
    }

    @Override
    public String toString() {
        return getClass().getName() + ": name=" + getName() + //NOI18N
            ", descriptor=" + getDescriptor(); //NOI18N
    }
}
