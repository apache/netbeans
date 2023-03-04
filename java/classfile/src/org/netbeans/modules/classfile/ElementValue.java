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

import java.io.DataInputStream;
import java.io.IOException;

/**
 * ElementValue:  the value portion of the name-value pair of a
 * single annotation element.
 *
 * @author  Thomas Ball
 */
public abstract class ElementValue {
    static ElementValue load(DataInputStream in, ConstantPool pool,
			     boolean runtimeVisible)
	throws IOException {
	char tag = (char)in.readByte();
	switch (tag) {
	case 'e': return loadEnumValue(in, pool);
	  case 'c': {
	      int classType = in.readUnsignedShort();
	      return new ClassElementValue(pool, classType);
	  }
	  case '@': {
	      Annotation value = 
		  Annotation.loadAnnotation(in, pool, runtimeVisible);
	      return new NestedElementValue(pool, value);
	  }
	  case '[': {
	      ElementValue[] values = new ElementValue[in.readUnsignedShort()];
	      for (int i = 0; i < values.length; i++)
		  values[i] = load(in, pool, runtimeVisible);
	      return new ArrayElementValue(pool, values);
	  }
	  default:
	      assert "BCDFIJSZs".indexOf(tag) >= 0 : "invalid annotation tag";
	      return new PrimitiveElementValue(pool, in.readUnsignedShort());
	}
    }

    private static ElementValue loadEnumValue(DataInputStream in, 
					      ConstantPool pool) 
	throws IOException {
	int type = in.readUnsignedShort();
	CPEntry cpe = pool.get(type);
	if (cpe.getTag() == ConstantPool.CONSTANT_FieldRef) {
	    // workaround for 1.5 beta 1 and earlier builds
	    CPFieldInfo fe = (CPFieldInfo)cpe;
	    String enumType = fe.getClassName().getInternalName();
	    String enumName = fe.getFieldName();
	    return new EnumElementValue(enumType, enumName);
	} else {
	    int name = in.readUnsignedShort();
	    return new EnumElementValue(pool, type, name);
	}
    }

    /* Package-private constructor so that only classes in this
     * package can subclass.
     */
    ElementValue() {
    }
}
