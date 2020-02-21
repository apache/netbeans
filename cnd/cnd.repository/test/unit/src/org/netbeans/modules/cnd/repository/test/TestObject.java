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

package org.netbeans.modules.cnd.repository.test;

import java.io.*;
import org.netbeans.modules.cnd.repository.impl.spi.LayerKey;
import org.netbeans.modules.cnd.repository.spi.Key;
import org.netbeans.modules.cnd.repository.spi.Persistent;
import org.netbeans.modules.cnd.repository.spi.RepositoryDataInput;
import org.netbeans.modules.cnd.repository.spi.RepositoryDataOutput;

/**
 * Test object to store in a SingleFileStorage
 */
public class TestObject implements Persistent {
    
    public TestKey key;
    public String[] sData;
    public int iData;
    public long lData;

    public TestObject(RepositoryDataInput in) throws IOException {
	read(in);
    }
    
    public TestObject(String key, int unitId, String unit, Key.Behavior behavior, String... data) {
	this.key = new TestKey(key, unitId, unit, behavior);
	this.sData = data;
    }
    
    public Key getKey() {
	return key;
    }

    public LayerKey getLayerKey() {
        return LayerKey.create(key, key.getUnitId());
    }
    
    public void write(RepositoryDataOutput out) throws IOException {
        key.write(out);
	if( sData == null ) {
	    out.writeInt(-1);
	}
	else {
	    out.writeInt(sData.length);
	    for (int i = 0; i < sData.length; i++) {
		out.writeUTF(sData[i]);
	    }
	}
	out.writeInt(iData);
	out.writeLong(lData);
    }
    
    private Persistent read(RepositoryDataInput in) throws IOException {
	key = new TestKey(in);
	int cnt = in.readInt();
	if( cnt == -1 ) {
	    sData = null;
	}
	else {
	    sData = new String[cnt];
	    for (int i = 0; i < sData.length; i++) {
		sData[i] = in.readUTF();
	    }
	}
	iData = in.readInt();
	lData = in.readLong();
        return this;
    }
    
    @Override
    public String toString() {
	StringBuilder sb = new StringBuilder("TestOBject @"); // NOI18N
	sb.append(hashCode());
	sb.append(" key="); // NOI18N
	sb.append(key);
	sb.append(" sData="); // NOI18N
	if( sData == null ) {
	    sb.append("null"); // NOI18N
	}
	else {
	    for (int i = 0; i < sData.length; i++) {
		if( i == 0) {
		    sb.append('[');
		}
		else {
		    sb.append(","); // NOI18N
		}
		sb.append(sData[i]);
	    }
	}
	sb.append("] iData="); // NOI18N
	sb.append(iData);
	sb.append(" lData="); // NOI18N
	sb.append(lData);
	return sb.toString();
    }

    @Override
    public int hashCode() {
	int hash = iData + (int) lData + key.hashCode();
	if( sData != null ) {
	    for (int i = 0; i < sData.length; i++) {
		hash += sData.hashCode();
	    }
	}
	return hash;
    }
    
    @Override
    public boolean equals(Object obj) {
	if( obj == null ) {
	    return false;
	}
	if( ! obj.getClass().equals(TestObject.class) ) {
	    return false;
	}
	TestObject other = (TestObject) obj;
	return	equals(this.key.getAt(0), other.key.getAt(0)) &&
		equals(this.sData, other.sData) &&
		this.lData == other.lData &&
		this.iData == other.iData;
    }
    
    private boolean equals(CharSequence s1, CharSequence s2) {
	if( s1 == null ) {
	    return s2 == null;
	}
	else {
	    return s1.equals(s2);
	}
    }
    
    private boolean equals(CharSequence[] s1, CharSequence[] s2) {
	if( s1 == null ) {
	    return s2 == null;
	}
	else if( s2 == null ) {
	    return false;
	}
	else {
	    if( s1.length != s2.length ) {
		return false;
	    }
	    else {
		for (int i = 0; i < s1.length; i++) {
		    if( ! equals(s1[i], s2[i]) ) {
			return false;
		    }
		}
	    }
	    return true;
	}
    }

    public boolean canWrite(Persistent obj) {
        return true;
    }
}
