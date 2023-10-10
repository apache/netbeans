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
 * AnnotationComponent:  a single annotation on a program element.
 *
 * @author  Thomas Ball
 */
public class AnnotationComponent {
    String name;
    ElementValue value;

    static AnnotationComponent load(DataInputStream in, ConstantPool pool,
				    boolean runtimeVisible)
	throws IOException {
	int iName = in.readUnsignedShort();
	String name = ((CPName)pool.get(iName)).getName();
	ElementValue value = ElementValue.load(in, pool, runtimeVisible);
	return new AnnotationComponent(name, value);
    }

    AnnotationComponent(String name, ElementValue value) {
	this.name = name;
	this.value = value;
    }

    /**
     * Returns the name of this component.
     * @return name of the component
     */
    public final String getName() {
	return name;
    }

    /**
     * Returns the value for this component.
     * @return value of the component
     */
    public final ElementValue getValue() {
	return value;
    }

    @Override
    public String toString() {
	return "name=" + name + ", value=" + value;
    }
}
