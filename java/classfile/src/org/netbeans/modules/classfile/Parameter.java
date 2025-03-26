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

import java.io.*;
import java.util.*;

/**
 * A representation of a parameter to a method declaration.  A parameter
 * will not have a name, unless the classfile is compiled with local
 * variable tables (if not, then the name is an empty string).
 * A final modifier on a parameter is never reported,
 * since that modifier is not stored in a classfile.
 *
 * @author  Thomas Ball
 */
public final class Parameter extends Field {

    static Parameter[] makeParams(Method method) {
	List<Parameter> paramList = new ArrayList<Parameter>();
        for (Iterator<Parameter> it = new ParamIterator(method); it.hasNext();)
            paramList.add(it.next());
        return paramList.toArray(new Parameter[0]);
    }

    private static Parameter createParameter (String name, String type, ClassFile classFile,
            DataInputStream visibleAnnotations, DataInputStream invisibleAnnotations) {
        return new Parameter (name, type, classFile,
            visibleAnnotations, invisibleAnnotations);
    }
    
    /** Creates new Parameter */
    private Parameter(String name, String type, ClassFile classFile,
            DataInputStream visibleAnnotations, DataInputStream invisibleAnnotations) {
        super(name, type, classFile);
        loadParameterAnnotations(visibleAnnotations, invisibleAnnotations);
    }
    
    private void loadParameterAnnotations(DataInputStream visible, DataInputStream invisible) {
        super.loadAnnotations();
        if (annotations == null && (visible != null || invisible != null))
            annotations = new HashMap<ClassName,Annotation>(2);
        try {
            if (visible != null && visible.available() > 0)
                Annotation.load(visible, classFile.getConstantPool(), true, annotations);
        } catch (IOException e) {
            throw new InvalidClassFileAttributeException("invalid RuntimeVisibleParameterAnnotations attribute", e);
        }
        try {
            if (invisible != null && invisible.available() > 0)
                Annotation.load(invisible, classFile.getConstantPool(), false, annotations);
        } catch (IOException e) {
            throw new InvalidClassFileAttributeException("invalid RuntimeInvisibleParameterAnnotations attribute", e);
        }
    }

    /**
     * Return a string in the form {@code "<type> <name>"}.  Class types
     * are shown in a "short" form; i.e. "Object" instead of
     * "java.lang.Object"j.
     *
     * @return string describing the variable and its type.
     */
    public final String getDeclaration() {
	StringBuffer sb = new StringBuffer();
	sb.append(CPFieldMethodInfo.getSignature(getDescriptor(), false));
	String name = getName();
	if (name != null) {
	    sb.append(' ');
	    sb.append(name);
	}
	return sb.toString();
    }
    
    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer("name=");
	sb.append(getName());
        sb.append(" type="); //NOI18N
        sb.append(getDescriptor());
	if (getTypeSignature() != null) {
	    sb.append(", signature="); //NOI18N
	    sb.append(typeSignature);
	}
        loadAnnotations();
	if (annotations.size() > 0) {
	    Iterator<Annotation> iter = annotations.values().iterator();
	    sb.append(", annotations={ ");
	    while (iter.hasNext()) {
		sb.append(iter.next().toString());
		if (iter.hasNext())
		    sb.append(", ");
	    }
	    sb.append(" }");
	}
        return sb.toString();
    }

    private static class ParamIterator implements Iterator<Parameter> {
        ClassFile classFile;
        String signature;
        LocalVariableTableEntry[] localVars;

        /** the current local variable array position */
        int ivar;

        /** the current character in the type signature */
        int isig;
        
        /** annotation attributes */
        DataInputStream visibleAnnotations;
        DataInputStream invisibleAnnotations;
        
        /** 
         * @param method 
         */
        ParamIterator(Method method) {
            classFile = method.getClassFile();
            signature = method.getDescriptor();
            assert signature.charAt(0) == '(';
            isig = 1;  // skip '('
            ivar = method.isStatic() ? 0 : 1;
	    Code code = method.getCode();
            localVars = code != null ? 
		code.getLocalVariableTable() : 
		new LocalVariableTableEntry[0];
            AttributeMap attrs = method.getAttributes();
            try {
                visibleAnnotations = 
                    getParamAttr(attrs, "RuntimeVisibleParameterAnnotations"); //NOI18N
            } catch (IOException e) {
                throw new InvalidClassFileAttributeException("invalid RuntimeVisibleParameterAnnotations attribute", e);
            }
            try {
                invisibleAnnotations = 
                    getParamAttr(attrs, "RuntimeInvisibleParameterAnnotations"); //NOI18N
            } catch (IOException e) {
                throw new InvalidClassFileAttributeException("invalid RuntimeInvisibleParameterAnnotations attribute", e);
            }
        }
        
        private DataInputStream getParamAttr(AttributeMap attrs, String name) throws IOException {
            DataInputStream in = attrs.getStream(name);
            if (in != null)
                in.readByte(); // skip the redundant parameters number
            return in;
        }
        
        public boolean hasNext() {
            return signature.charAt(isig) != ')';
        }
        
        public Parameter next() {
            if (hasNext()) {
		String name = "";
		for (int i = 0; i < localVars.length; i++) {
		    LocalVariableTableEntry lvte = localVars[i];
		    // only parameters have a startPC of zero
		    if (lvte.index == ivar && lvte.startPC == 0) {
			name = localVars[i].getName();
			break;
		    }
		}
                ivar++;
                int sigStart = isig;
                while (isig < signature.length()) {
                    char ch = signature.charAt(isig);
                    switch (ch) {
                        case '[':
                            isig++;
                            break;
                        case 'B':
                        case 'C':
                        case 'F':
                        case 'I':
                        case 'S':
                        case 'Z':
                        case 'V': {
                            String type = signature.substring(sigStart, ++isig);
                            return Parameter.createParameter(name, type, classFile, 
                                    visibleAnnotations, invisibleAnnotations);
                        }
                        case 'D':
                        case 'J': {
                            ivar++;  // longs and doubles take two slots
                            String type = signature.substring(sigStart, ++isig);
                            return Parameter.createParameter(name, type, classFile, 
                                    visibleAnnotations, invisibleAnnotations);
                        }
                        case 'L': {
                            int end = signature.indexOf(';', isig) + 1;
                            String type = signature.substring(isig, end);
                            isig = end;
                            return Parameter.createParameter(name, type, classFile, 
                                    visibleAnnotations, invisibleAnnotations);
                        }

                    }
                }
            }
            throw new NoSuchElementException();
        }
        
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }
}
