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

/**
 * ClassElementValue:  the value part of a single element for
 * those annotations that are a class type.
 *
 * @author  Thomas Ball
 */
public final class ClassElementValue extends ElementValue {
    String name;

    ClassElementValue(ConstantPool pool, int iValue) {
	// getName() works for either the old CPClassInfo or now
	// CPUTF8Info entries, changed after 1.5 beta 1.
	this.name = ((CPName)pool.get(iValue)).getName();
    }

    /**
     * Returns the value of this component, as a class constant pool entry.
     * @return classname of this component
     */
    public final ClassName getClassName() {
	return ClassName.getClassName(name);
    }

    @Override
    public String toString() {
	return "class=" + name;
    }
}
