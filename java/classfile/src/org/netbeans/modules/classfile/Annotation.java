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
     * @return annotation type
     */
    public final ClassName getType() {
	return type;
    }

    /**
     * Returns the named components for this annotation, as an
     * array of AnnotationComponents.
     * @return named component for this annotation
     */
    public final AnnotationComponent[] getComponents() {
	return components.clone();
    }

    /**
     * Returns the named component for this annotation, or null if 
     * no component with that name exists.
     * @param name of component
     * @return named component for this annotation 
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
     * @return true if this annoation is loaded
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
