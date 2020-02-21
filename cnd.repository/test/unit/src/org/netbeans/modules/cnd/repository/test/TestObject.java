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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
