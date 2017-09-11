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
 * Annotation:  a single annotation on a program element.
 *
 * @author  Thomas Ball
 */
public class Annotation {
    ClassName type;
    AnnotationComponent[] components;
    boolean runtimeVisible;

    /**
     * Reads a classfile annotation section and adds its annotations to
     * a specified map.
     */
    static void load(DataInputStream in, ConstantPool pool,
		     boolean visible, Map<ClassName,Annotation> map) throws IOException {
	int nattrs = in.readUnsignedShort();
	for (int i = 0; i < nattrs; i++) {
	    Annotation ann = loadAnnotation(in, pool, visible);
	    map.put(ann.getType(), ann);
	}
    }

    static Annotation loadAnnotation(DataInputStream in, ConstantPool pool, 
				     boolean visible) throws IOException {
	final ClassName type;
	CPEntry entry = pool.get(in.readUnsignedShort());
	if (entry.getTag() == ConstantPool.CONSTANT_Class)
	    // 1.5 build 51 and earlier
	    type = ((CPClassInfo)entry).getClassName();
	else {
	    String s = ((CPName)entry).getName();
	    type = ClassName.getClassName(s);
	}
	int npairs = in.readUnsignedShort();
	List<AnnotationComponent> pairList = new ArrayList<AnnotationComponent>();
	for (int j = 0; j < npairs; j++)
	    pairList.add(AnnotationComponent.load(in, pool, visible));
	AnnotationComponent[] acs = 
	    new AnnotationComponent[pairList.size()];
	pairList.toArray(acs);
	return new Annotation(pool, type, acs, visible);
    }

    Annotation(ConstantPool pool, ClassName type, 
	       AnnotationComponent[] components, boolean runtimeVisible) {
	this.type = type;
	this.components = components;
	this.runtimeVisible = runtimeVisible;
    }

    /**
     * Returns the annotation type.
     */
    public final ClassName getType() {
	return type;
    }

    /**
     * Returns the named components for this annotation, as an
     * array of AnnotationComponents.
     */
    public final AnnotationComponent[] getComponents() {
	return components.clone();
    }

    /**
     * Returns the named component for this annotation, or null if 
     * no component with that name exists.
     */
    public final AnnotationComponent getComponent(String name) {
	for (int i = 0; i < components.length; i++) {
	    AnnotationComponent comp = components[i];
	    if (comp.getName().equals(name))
		return comp;
	}
	return null;
    }

    /**
     * Returns true if this annotation is loaded by the Java Virtual
     * Machine to be available via the Java reflection facility.
     */
    public boolean isRuntimeVisible() {
	return runtimeVisible;
    }

    @Override
    public String toString() {
	StringBuffer sb = new StringBuffer("@");
	sb.append(type);
	sb.append(" runtimeVisible=");
	sb.append(runtimeVisible);
	int n = components.length;
	if (n > 0) {
	    sb.append(" { ");
	    for (int i = 0; i < n; i++) {
		sb.append(components[i]);
		if (i < n - 1)
		    sb.append(", ");
	    }
	    sb.append(" }");
	}
	return sb.toString();
    }
}
