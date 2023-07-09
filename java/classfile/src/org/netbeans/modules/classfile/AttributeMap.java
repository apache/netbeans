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
     * @return the number of attributes in this map
     */
    public int size() {
	return map.size();
    }

    /**
     * Returns true if no attributes exist in this map.
     * @return true if no attributes exist in this map
     */
    public boolean isEmpty() {
	return map.isEmpty();
    }

    /**
     * Returns true if an attribute of the specified name exists in this map.
     * @param key attribute name
     * @return true if attribute exists
     */
    public boolean containsAttribute(String key) {
	return map.containsKey(key);
    }

    /**
     * Returns a set of names of all of the attributes in this map.
     * @return a set of names of all of the attributes in this map
     */
    public Set<String> keySet() {
	return map.keySet();
    }

}
