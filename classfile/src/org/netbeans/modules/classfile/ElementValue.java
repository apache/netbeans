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
