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
 * A class representing the enclosing method of an inner class.  An
 * enclosing method is similar to a CPMethodInfo type, but differs
 * in two respects.  First, the classfile stores this information in
 * an "EnclosingMethod" attribute, rather than in the constant pool
 * Second, an enclosing method attribute may not actually have a
 * method reference (only a class reference).  This is because the
 * inner class is defined in an init block instead of an actual
 * method.  
 *
 * @see org.netbeans.modules.classfile.ClassFile#getEnclosingMethod
 * @author Thomas Ball
 */
public final class EnclosingMethod {
    final CPClassInfo classInfo;
    final CPNameAndTypeInfo methodInfo;

    EnclosingMethod(ConstantPool pool, CPClassInfo classInfo, int iMethod) {
	this.classInfo = classInfo;
	methodInfo = iMethod > 0 ? (CPNameAndTypeInfo)pool.get(iMethod) : null;
    }

    public ClassName getClassName() {
        return classInfo.getClassName();
    }

    /**
     * Returns the constant pool entry for the enclosing class.
     * @return the constant pool entry for the enclosing class
     */
    public CPClassInfo getClassInfo() {
	return classInfo;
    }

    /**
     * Returns whether the enclosing method attribute describes a method
     * the inner class is defined within.  If false, then the inner
     * class was defined in an init block (or statement) in the class,
     * outside of any method or constructor bodies.
     * @return true if the enclosing method attribute describes a method the inner class 
     * is defined within
     */
    public boolean hasMethod() {
	return methodInfo != null;
    }

    /**
     * Returns the constant pool entry for the enclosing method, or
     * null if the inner class was defined outside of any method or
     * constructor bodies.
     *
     * Note: a CPNameAndTypeInfo instance is returned because the method
     * is external to the enclosed class.  Do not attempt to cast it to a
     * CPMethodInfo type, which is an internal method structure.
     * @return the constant pool entry for the enclosing method
     */
    public CPNameAndTypeInfo getMethodInfo() {
	return methodInfo;
    }

    @Override
    public String toString() {
	String methodString = methodInfo != null 
	    ? methodInfo.toString() : "<no method>";
        return "enclosing method: class=" + getClassName() + //NOI18N
	    ", method=" + methodString;   //NOI18N
    }
}
