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
 * A class representing the CONSTANT_MethodHandle constant pool type.
 *
 * @author Thomas Ball
 * @since 1.40
 */
public class CPMethodHandleInfo extends CPEntry {
    ReferenceKind referenceKind;
    int iReference;

    CPMethodHandleInfo(ConstantPool pool, int referenceKind,int iReference) {
	super(pool);
        this.referenceKind = ReferenceKind.from(referenceKind);
        this.iReference = iReference;
    }

    public int getTag() {
	return ConstantPool.CONSTANT_MethodHandle;
    }

    public ReferenceKind getReferenceKind() {
        return referenceKind;
    }

    public int getReference() {
        return iReference;
    }

    @Override
    public String toString() {
        return getClass().getName() + ": kind=" + referenceKind + //NOI18N
            ", index=" + iReference; //NOI18N
    }
    
    public enum ReferenceKind {
        getField(1),
        getStatic(2),
        putField(3),
        putStatic(4),
        invokeVirtual(5),
        invokeStatic(6),
        invokeSpecial(7),
        newInvokeSpecial(8),
        invokeInterface(9);
        
        private final int kindInt;

        private ReferenceKind(int kindInt) {
            this.kindInt = kindInt;
        }
        
        static ReferenceKind from(int referenceKind) {
            for (ReferenceKind k : values()) {
                if (k.kindInt == referenceKind) return k;
            }
            
            throw new IllegalStateException("Unknown ref kind: " + referenceKind);
        }
    }
}
