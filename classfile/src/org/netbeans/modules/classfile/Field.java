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
     * Returns al<ClassName,Annotation>l runtime annotations defined for this field.  Inherited
     * annotations are not included in this collection.
     */
    public final Collection<Annotation> getAnnotations() {
	loadAnnotations();
	return annotations.values();
    }

    /**
     * Returns the annotation for a specified annotation type, or null if
     * no annotation of that type exists for this field.
     */
    public final Annotation getAnnotation(final ClassName annotationClass) {
	loadAnnotations();
	return annotations.get(annotationClass);
    }
    
    /**
     * Returns true if an annotation of the specified type is defined for
     * this field.
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
     *
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
