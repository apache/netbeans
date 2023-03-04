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
 * A class representing the CONSTANT_InvokeDynamic constant pool type.
 *
 * @author Thomas Ball
 * @since 1.40
 */
public class CPInvokeDynamicInfo extends CPEntry {
    int iBootstrapMethod;
    int iNameAndType;

    CPInvokeDynamicInfo(ConstantPool pool,int iBootstrapMethod,int iNameAndType) {
	super(pool);
        this.iBootstrapMethod = iBootstrapMethod;
        this.iNameAndType = iNameAndType;
    }

    public int getTag() {
	return ConstantPool.CONSTANT_InvokeDynamic;
    }
    
    public int getBootstrapMethod() {
        return iBootstrapMethod;
    }
    
    public int getNameAndType() {
        return iNameAndType;
    }

    @Override
    public String toString() {
        return getClass().getName() + ": bootstrapMethod=" + iBootstrapMethod + //NOI18N
            ", nameAndType=" + iNameAndType; //NOI18N
    }
}
