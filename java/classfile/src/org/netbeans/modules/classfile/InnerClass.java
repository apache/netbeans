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

import java.io.DataInputStream;
import java.io.IOException;

/**
 * An InnerClass attribute of a classfile.
 *
 * @author  Thomas Ball
 */
public final class InnerClass {

    ClassName name;
    ClassName outerClassName;
    String simpleName;
    int access;

    static InnerClass[] loadInnerClasses(DataInputStream in, ConstantPool pool)
      throws IOException {
        int n = in.readUnsignedShort();
        InnerClass[] innerClasses = new InnerClass[n];
        for (int i = 0; i < n; i++)
            innerClasses[i] = new InnerClass(in, pool);
        return innerClasses;
    }

    InnerClass(DataInputStream in, ConstantPool pool) 
      throws IOException {
        loadInnerClass(in, pool);
    }

    private void loadInnerClass(DataInputStream in, ConstantPool pool) 
      throws IOException {
        int index = in.readUnsignedShort();
        name = (index > 0) ? pool.getClass(index).getClassName() : null;
        index = in.readUnsignedShort();
	outerClassName = (index > 0) ? pool.getClass(index).getClassName() : null;
        index = in.readUnsignedShort();
        if (index > 0) {
            CPUTF8Info entry = (CPUTF8Info)pool.get(index);
            simpleName = entry.getName();
        }
        access = in.readUnsignedShort();
    }

    /** Returns the name of this class, including its package (if any).
     * If the compiler didn't define this value, the string 
     * "&lt;not defined&gt;" is returned.
     * @return the name of this class.
     */    
    public final ClassName getName() {
        return name;
    }
    
    /** Returns the name of the enclosing outer class, including 
     *  its package (if any).  
     * @return the name of this class, or null if not available.
     */    
    public final ClassName getOuterClassName() {
        return outerClassName;
    }

    /**
     * Returns the original simple name as given in the source code.
     * If this is an anonymous class, null is returned instead.
     * @return the simple name of this class, or null if anonymous.
     */
    public final String getSimpleName() {
        return simpleName;
    }

    /**
     * Returns the access flags of this class.
     * @return access flags of this class 
     */
    public final int getAccess() {
        return access;
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("innerclass=");
        sb.append(name);
        if (simpleName != null) {
            sb.append(" (");
            sb.append(simpleName);
            sb.append(')');
        }
        sb.append(", outerclass=");
        sb.append(outerClassName);
        return sb.toString();
    }
}
