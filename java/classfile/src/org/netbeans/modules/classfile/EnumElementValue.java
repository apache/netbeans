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
 * EnumElementValue:  a single annotation on a program element for
 * those annotations that are enum constants.
 *
 * @author  Thomas Ball
 */
public final class EnumElementValue extends ElementValue {
    String enumType;
    String enumName;

    EnumElementValue(ConstantPool pool, int iEnumType, int iEnumName) {
	enumType = ((CPName)pool.get(iEnumType)).getName();
	enumName = ((CPName)pool.get(iEnumName)).getName();
    }

    // for 1.5 beta1 classfile incompatibility
    EnumElementValue(String type, String name) {
	enumType = type;
	enumName = name;
    }

    /**
     * Returns the enum type as a string, rather than a ClassName.
     * This is necessary because an enum may have a primitive type.
     * @return the enum type as a string
     */
    public final String getEnumType() {
	return enumType;
    }

    /**
     * Returns the name of the enum constant for this annotation
     * component.
     * @return the name of the enum constant
     */
    public final String getEnumName() {
	return enumName;
    }

    @Override
    public String toString() {
	return enumType + "." + enumName;
    }
}
