/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
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
     */
    public final boolean isConstant() {
        return attributes.get("ConstantValue") != null;//NOI18N
    }

    /**
     * Returns the value object of this variable if it is a constant,
     * otherwise null.
     * @deprecated replaced by <code>Object getConstantValue()</code>.
     */
    @Deprecated
    public final Object getValue() {
	return getConstantValue();
    }
    
    /**
     * Returns the value object of this variable if it is a constant,
     * otherwise null.
     * @see #isConstant
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
     * Return a string in the form "<type> <name>".  Class types
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
