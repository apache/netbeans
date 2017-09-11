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
 * Software is Sun Microsystems, Inc. Portions Copyright 2000-2001 Sun
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
 * Class representing a map of classfile attributes.  The
 * keys for this map are the names of the attributes (as Strings,
 * not constant pool indexes).  The values are byte arrays that
 * hold the contents of the attribute.
 *
 * Note: if a ClassFile is created with includeCode parameter set to
 * false, then no AttributeMaps for the classfile's methods will
 * have Code attributes.
 *
 * @author Thomas Ball
 */
public final class AttributeMap {

    Map<String,byte[]> map;

    /**
     * Load zero or more attributes from a class, field or method.
     */
    static AttributeMap load(DataInputStream in, ConstantPool pool) 
      throws IOException {
	return load(in, pool, false);
    }

    static AttributeMap load(DataInputStream in, ConstantPool pool,
			     boolean includeCode) throws IOException {        
        int count = in.readUnsignedShort();
        Map<String,byte[]> map = new HashMap<String,byte[]>(count + 1, (float)1.0);
        for (int i = 0; i < count; i++) {
	    Object o = pool.get(in.readUnsignedShort());
            if (!(o instanceof CPUTF8Info))
                throw new InvalidClassFormatException();
	    CPUTF8Info entry = (CPUTF8Info)o;
	    String name = entry.getName();
	    int len = in.readInt();
	    if (!includeCode && "Code".equals(name)) {
		int n;
		while ((n = (int)in.skip(len)) > 0 && n < len)
		    len -= n;
	    } else {
		byte[] attr = new byte[len];
		in.readFully(attr);
		map.put(name, attr);
	    }
        }
	return new AttributeMap(map);
    }

    AttributeMap(Map<String,byte[]> attributes) {
	this.map = attributes;
    }

    DataInputStream getStream(String name) {
	byte[] attr = map.get(name);
	return attr != null ? 
	    new DataInputStream(new ByteArrayInputStream(attr)) : null;	    
    }

    /**
     * Returns an array containing the bytes in a specified attribute.
     * If the attribute exists but doesn't have any length (such as
     * the <code>Deprecated</code> attribute), then a zero-length
     * array is returned.  If the attribute doesn't exist, then null 
     * is returned.
     */
    byte[] get(String name) {
	return map.get(name);
    }

    /**
     * Returns the number of attributes in this map.
     */
    public int size() {
	return map.size();
    }

    /**
     * Returns true if no attributes exist in this map.
     */
    public boolean isEmpty() {
	return map.isEmpty();
    }

    /**
     * Returns true if an attribute of the specified name exists in this map.
     */
    public boolean containsAttribute(String key) {
	return map.containsKey(key);
    }

    /**
     * Returns a set of names of all of the attributes in this map.
     */
    public Set<String> keySet() {
	return map.keySet();
    }

}
