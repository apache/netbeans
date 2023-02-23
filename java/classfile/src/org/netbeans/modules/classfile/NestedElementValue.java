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
 * NestedElementValue:  an annotation on a program element that is
 * another annotation.  The value for this annotation is the
 * nested AnnotationComponent.
 *
 * @author  Thomas Ball
 */
public final class NestedElementValue extends ElementValue {
    Annotation value;

    NestedElementValue(ConstantPool pool, Annotation value) {
	this.value = value;
    }

    /**
     * Returns the value of this component, which is an Annotation.
     * @return the value of this component, which is an Annotation
     */
    public final Annotation getNestedValue() {
	return value;
    }

    @Override
    public String toString() {
	return "nested value=" + value;
    }
}
