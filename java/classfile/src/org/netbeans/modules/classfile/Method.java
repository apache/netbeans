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
import java.util.List;
import java.util.Arrays;

/**
 * A Java method object.
 *
 * @author  Thomas Ball
 */
public final class Method extends Field {

    private Code code;
    private CPClassInfo[] exceptions;
    private Parameter[] parameters;
    private ElementValue annotationDefault;
    
    /** Marker which indicates that the annotationDefault has not been loaded yet. */
    private static final ElementValue notloadedAnnotationDefault = new ElementValue() {};
    
    static Method[] loadMethods(DataInputStream in, ConstantPool pool,
				ClassFile cls, boolean includeCode) 
      throws IOException {
	int count = in.readUnsignedShort();
	Method[] methods = new Method[count];
	for (int i = 0; i < count; i++)
	    methods[i] = new Method(in, pool, cls, includeCode);
	return methods;
    }
    
    /** Creates new Method */
    Method(DataInputStream in, ConstantPool pool, ClassFile cls, 
	   boolean includeCode) throws IOException {
        super(in, pool, cls, includeCode);
        annotationDefault = notloadedAnnotationDefault;
    }

    /** 
     * Get the bytecodes of this method.  This method returns null if
     * the method is abstract, or if the ClassFile instance was created
     * with a includeCode parameter of false.
     *
     * @return the Code object, or null.
     */    
    public final Code getCode() {
	if (code == null) {
	    DataInputStream in = attributes.getStream("Code"); // NOI18N
	    if (in != null) {
		try {
		    code = new Code(in, classFile.constantPool);
		    in.close();
		} catch (IOException e) {
		    throw new InvalidClassFileAttributeException("invalid Code attribute", e);
		}
	    }
	}
        return code;  // will be null for abstract methods
    }
    
    public final CPClassInfo[] getExceptionClasses() {
        if (exceptions == null) {
	    DataInputStream in = attributes.getStream("Exceptions"); // NOI18N
	    if (in != null) {
		try {
		    exceptions = 
			ClassFile.getCPClassList(in, classFile.constantPool);
		    in.close();
		} catch (IOException e) {
		    throw new InvalidClassFileAttributeException("invalid Exceptions attribute", e);
		}
	    }
	    if (exceptions == null)
		exceptions = new CPClassInfo[0];
	}
        return exceptions.clone();
    }
    
    /**
     * Returns true if this method is a generics bridge method defined
     * by the compiler.
     * @return true if this method is a generic bridge method
     */
    public final boolean isBridge() {
	return (access & Access.BRIDGE) == Access.BRIDGE;
    }
            
    /**
     * Returns true if this method is declared with a variable number
     * of arguments.
     * @return true if this method is declared with a variable number 
     * of argument
     */
    public final boolean isVarArgs() {
	return (access & Access.VARARGS) == Access.VARARGS;
    }

    /**
     * Returns true if this method is declared synchronized.
     * @return true if this method is declared synchronized
     */
    public final boolean isSynchronized() {
	return (access & Access.SYNCHRONIZED) == Access.SYNCHRONIZED;
    }

    /**
     * Returns true if this method is declared native.
     * @return true if this method is declated native
     */
    public final boolean isNative() {
	return (access & Access.NATIVE) == Access.NATIVE;
    }

    /**
     * Returns true if this method is declared abstract.
     * @return true if this method is declared abstract
     */
    public final boolean isAbstract() {
	return (access & Access.ABSTRACT) == Access.ABSTRACT;
    }

    /**
     * Returns the parameters for this method as a declaration-ordered list.
     * @return list parameter per declaration order
     */
    public final List<Parameter> getParameters() {
	if (parameters == null)
	    parameters = Parameter.makeParams(this);
	return Arrays.asList(parameters);
    }

    /**
     * Returns the method's return type in the type format defined by
     * the JVM Specification for Field Descriptors (section 4.3.2).
     * @return the return type
     */
    public final String getReturnType() {
	String desc = getDescriptor();
	int i = desc.indexOf(')') + 1;
	return desc.substring(i);
    }

    /**
     * Returns the method's return type as it would be defined in Java
     * source code format.
     * @return methode return type as string
     */
    public final String getReturnSignature() {
	String type = getReturnType();
	return CPFieldMethodInfo.getSignature(type, true);
    }

    /**
     * Returns the default annotation value for the element
     * defined by this method.  Null is returned if no default 
     * is specified for this element, or if the class that contains 
     * this method does not define an annotation type.
     * @return the default annotatin value for the element 
     * defined by this method
     */
    public ElementValue getAnnotationDefault() {
	if (annotationDefault == notloadedAnnotationDefault) {
            annotationDefault = null;
	    DataInputStream in = 
		attributes.getStream("AnnotationDefault"); // NOI18N
	    if (in != null) {
		try {
		    annotationDefault = 
			ElementValue.load(in, classFile.constantPool, false);
		    in.close();
		} catch (IOException e) {
		    throw new InvalidClassFileAttributeException("invalid AnnotationDefault attribute", e);
		}
	    }
	}
        return annotationDefault;
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer(super.toString());
        sb.append(", params (");
        getParameters();
        for (int i = 0; i < parameters.length; i++) {
            sb.append(parameters[i].toString());
            if (i+1 < parameters.length)
                sb.append(", ");
        }
        sb.append("), returns ");
	sb.append(getReturnSignature());
        CPClassInfo[] ec = getExceptionClasses();
        if (ec.length > 0) {
            sb.append(", throws"); //NOI18N
            for (int i = 0; i < ec.length; i++) {
                sb.append(' '); //NOI18N
                sb.append(ec[i].getName());
            }
	}
	if (getAnnotationDefault() != null) {
	    sb.append(", default \"");
	    sb.append(annotationDefault.toString());
	    sb.append("\" ");
	}
	Code c = getCode();
	if (c != null) {
            sb.append(' ');
	    sb.append(c.toString());
        }
        return sb.toString();
    }

    public final String getDeclaration() {
        return CPMethodInfo.getFullMethodName(getName(), getDescriptor());
    }
}
