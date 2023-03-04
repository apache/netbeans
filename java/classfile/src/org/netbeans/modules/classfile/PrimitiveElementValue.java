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
 * A PrimitiveElementValue is the value portion of an annotation component
 * that has a primitive type or String constant.  Its value
 * is a constant pool entry of the same type as the primitive;
 * for example, an int constant would have a value of type CPIntegerInfo.
 *
 * @author  Thomas Ball
 */
public final class PrimitiveElementValue extends ElementValue {
    CPEntry value;

    PrimitiveElementValue(ConstantPool pool, int iValue) {
	this.value = pool.get(iValue);
    }

    /**
     * Returns the value of this component, as a constant pool entry.
     * @return the value of this component
     */
    public final CPEntry getValue() {
	return value;
    }

    @Override
    public String toString() {
	return "const=" + value.getValue();
    }
}
