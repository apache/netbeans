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

import java.io.*;

/**
 * A Java field.  Unfortunately, the word "field" is generally used in
 * the Java documentation to mean either a variable, or both variables
 * and methods.  This class only describes variables.
 *
 * @author  Thomas Ball
 */
public final class Variable extends Field {

    private Object constValue = notLoadedConstValue;
    
    private static final Object notLoadedConstValue = new Object();

    static Variable[] loadFields(DataInputStream in, ConstantPool pool,
                                 ClassFile cls) 
      throws IOException {
          int count = in.readUnsignedShort();
          Variable[] variables = new Variable[count];
          for (int i = 0; i < count; i++)
              variables[i] = new Variable(in, pool, cls);
          return variables;
    }
    
    /** Creates new Variable */
    Variable(DataInputStream in, ConstantPool pool, ClassFile cls) 
	throws IOException {
        super(in, pool, cls, false);
    }

    /**
     * Returns true if the variable is a constant; that is, a final
     * static variable.
     * @see #getConstantValue
     * @return true if the variable is a constant
     */
    public final boolean isConstant() {
        return attributes.get("ConstantValue") != null;//NOI18N
    }

    /**
     * Returns the value object of this variable if it is a constant,
     * otherwise null.
     * @deprecated replaced by <code>Object getConstantValue()</code>.
     * @return the value object of this variable
     */
    @Deprecated
    public final Object getValue() {
	return getConstantValue();
    }
    
    /**
     * Returns the value object of this variable if it is a constant,
     * otherwise null.
     * @see #isConstant
     * @return the value object of this variable
     */
    public final Object getConstantValue() {
	if (constValue == notLoadedConstValue) {
	    DataInputStream in = attributes.getStream("ConstantValue"); // NOI18N
	    if (in != null) {
		try {
		    int index = in.readUnsignedShort();
		    CPEntry cpe = classFile.constantPool.get(index);
		    constValue = cpe.getValue();
		} catch (IOException e) {
		    throw new InvalidClassFileAttributeException("invalid ConstantValue attribute", e);
		}
	    }
	}
        return constValue;
    }
    
    /**
     * Return a string in the form "&lt;type&gt; &lt;name&gt;".  Class types
     * are shown in a "short" form; i.e. "Object" instead of
     * "java.lang.Object"j.
     *
     * @return string describing the variable and its type.
     */
    public final String getDeclaration() {
	StringBuffer sb = new StringBuffer();
	sb.append(CPFieldMethodInfo.getSignature(getDescriptor(), false));
	sb.append(' ');
	sb.append(getName());
	return sb.toString();
    }

    /**
     * Returns true if this field defines an enum constant.
     * @return true if this field defines an enum constant.
     */
    public final boolean isEnumConstant() {
	return (access & Access.ENUM) == Access.ENUM;
    }
            
    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer(super.toString());
        if (isConstant()) {
	    sb.append(", const value="); //NOI18N
	    sb.append(getValue());
	}
        return sb.toString();
    }
}
