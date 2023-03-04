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

/**
 * ArrayElementValue:  the value portion of an annotation element that
 * is an array of ElementValue instances.
 *
 * @author  Thomas Ball
 */
public final class ArrayElementValue extends ElementValue {
    ElementValue[] values;

    ArrayElementValue(ConstantPool pool, ElementValue[] values) {
	this.values = values;
    }

    /**
     * Returns the set of ElementValue instances for this component.
     * @return the set of ElementValue instances for this component
     */
    public ElementValue[] getValues() {
	return values.clone();
    }

    @Override
    public String toString() {
	StringBuffer sb = new StringBuffer("[");
	int n = values.length;
	for (int i = 0; i < n; i++) {
	    sb.append(values[i]);
	    if ((i + 1) < n)
		sb.append(',');
	}
	sb.append(']');
	return sb.toString();
    }
}
