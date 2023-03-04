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
import java.util.*;

/**
 * Base class for variables and methods.
 *
 * @author  Thomas Ball
 */
public abstract class Field {

    private int iName;
    private int iType;
    private String _name;
    private String _type;

    int access;
    ClassFile classFile;
    Map<ClassName,Annotation> annotations;
    String typeSignature;
    AttributeMap attributes;

    /** Creates new Field */
    Field(DataInputStream in, ConstantPool pool, ClassFile classFile, 
	  boolean includeCode) throws IOException {
        access = in.readUnsignedShort();
	iName = in.readUnsignedShort();
	iType = in.readUnsignedShort();
	this.classFile = classFile;
        attributes = AttributeMap.load(in, pool, includeCode);
    }

    Field(String name, String type, ClassFile classFile) {
	access = 0;
	_name = name;
	_type = type;
	this.classFile = classFile;
	attributes = new AttributeMap(new HashMap<String,byte[]>(1));
    }
    
    public final String getName() {
	if (_name == null && iName != 0) {
	    CPUTF8Info utfName = (CPUTF8Info)classFile.constantPool.get(iName);
            _name = utfName.getName();
	}
        return _name;
    }

    public final String getDescriptor() {
	if (_type == null && iType != 0) {
	    CPUTF8Info utfType = (CPUTF8Info)classFile.constantPool.get(iType);
            _type = utfType.getName();
	}
        return _type;
    }

    public abstract String getDeclaration();
    
    public final int getAccess() {
        return access;
    }
    
    public final boolean isStatic() {
        return Access.isStatic(access);
    }

    public final boolean isPublic() {
        return Access.isPublic(access);
    }

    public final boolean isProtected() {
        return Access.isProtected(access);
    }

    public final boolean isPackagePrivate() {
        return Access.isPackagePrivate(access);
    }

    public final boolean isPrivate() {
        return Access.isPrivate(access);
    }

    public final boolean isDeprecated() {
	return attributes.get("Deprecated") != null;
    }
    
    public final boolean isSynthetic() {
        return attributes.get("Synthetic") != null;
    }

    /**
     * Returns the class file this field is defined in.
     * @return the class file of this field.
     */
    public final ClassFile getClassFile() {
        return classFile;
    }
    
    /**
     * Returns the generic type information associated with this field.  
     * If this field does not have generic type information, then null 
     * is returned.
     * @return the generic type information associated with this field
     */
    public String getTypeSignature() {
	if (typeSignature == null) {
	    DataInputStream in = attributes.getStream("Signature"); // NOI18N
	    if (in != null) {
		try {
		    int index = in.readUnsignedShort();
		    CPUTF8Info entry = 
			(CPUTF8Info)classFile.constantPool.get(index);
		    typeSignature = entry.getName();
		    in.close();
		} catch (IOException e) {
		    throw new InvalidClassFileAttributeException("invalid Signature attribute", e);
		}
	    }
	}
	return typeSignature;
    }

    void setTypeSignature(String sig) {
	typeSignature = sig;
    }

    /**
     * Returns all &lt;ClassName,Annotation&gt; runtime annotations defined for this field.  Inherited
     * annotations are not included in this collection.
     * @return all annotations
     */
    public final Collection<Annotation> getAnnotations() {
	loadAnnotations();
	return annotations.values();
    }

    /**
     * Returns the annotation for a specified annotation type, or null if
     * no annotation of that type exists for this field.
     * @param annotationClass type of annotation
     * @return annotation
     */
    public final Annotation getAnnotation(final ClassName annotationClass) {
	loadAnnotations();
	return annotations.get(annotationClass);
    }
    
    /**
     * Returns true if an annotation of the specified type is defined for
     * this field.
     * @param annotationClass type of annotation
     * @return true if annotation is present
     */
    public final boolean isAnnotationPresent(final ClassName annotationClass) {
	loadAnnotations();
	return annotations.get(annotationClass) != null;
    }
    
    /**
     * Returns a map of the raw attributes for this field.  The
     * keys for this map are the names of the attributes (as Strings,
     * not constant pool indexes).  The values are byte arrays that
     * hold the contents of the attribute.  If the ClassFile was
     * created with an <code>includeCode</code> parameter that is
     * false, then <b>Code</b> attributes are not included in this map.
     * @return a map of attribute for this field
     * @see org.netbeans.modules.classfile.ClassFile#getAttributes
     */
    public final AttributeMap getAttributes(){
        return attributes;
    }
    
    void loadAnnotations() {
	if (annotations == null)
	    annotations = ClassFile.buildAnnotationMap(classFile.constantPool, 
						       attributes);
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
	String name = getName();
	if (name != null) {
	    sb.append(getName());
	    sb.append(' ');
	}
        if (isSynthetic())
            sb.append("(synthetic)"); //NOI18N
        if (isDeprecated())
            sb.append("(deprecated)"); //NOI18N
        sb.append("type="); //NOI18N
        sb.append(getDescriptor());
	if (getTypeSignature() != null) {
	    sb.append(", signature="); //NOI18N
	    sb.append(typeSignature);
	}
        sb.append(", access="); //NOI18N
        sb.append(Access.toString(access));
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
}
