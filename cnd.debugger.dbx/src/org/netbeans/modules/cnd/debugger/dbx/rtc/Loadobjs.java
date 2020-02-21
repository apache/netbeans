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

package org.netbeans.modules.cnd.debugger.dbx.rtc;

import java.util.Arrays;
import java.util.Vector;

import org.netbeans.modules.cnd.debugger.common2.utils.IpeUtils;

/**
 * Loadobjs lists
 *
 * was: ... inlined in RunConfig
 */

public class Loadobjs extends RtcProfile.ProfileCategory {

    public Loadobj[] los = new Loadobj[0];
    public Loadobj[] xml_los = new Loadobj[0];

    Loadobjs(RtcProfile owner) {
	super(owner, RtcProfile.PROP_RTC_LOADOBJS);
    } 

    @Override
    public boolean equals(Object thatObject) {
	// canonical part
	if (this == thatObject)
	    return true;
	if (! (thatObject instanceof Loadobjs))
	    return false;
	Loadobjs that = (Loadobjs) thatObject;

	// per-field part
	if (this.los == that.los)
	    return true;
	if (this.los == null || that.los == null)
	    return false;
	if (this.los.length != that.los.length)
	    return false;

	for (int px = 0; px < this.los.length; px++) {
	    Loadobj a = this.los[px];
	    Loadobj b = that.los[px];

	    if (! (IpeUtils.sameString(a.lo, b.lo) &&
		   a.skip == b.skip) ) {
		return false;
	    }
	}

	return true;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 59 * hash + Arrays.deepHashCode(this.los);
        return hash;
    }

    @Override
    public Object clone() {
	Loadobjs clone = new Loadobjs(null);

	if (los != null) {
	    clone.los = new Loadobj[los.length];
	    System.arraycopy(this.los, 0,
			     clone.los, 0,
			     los.length);
	}

	return clone;
    }

    public void assign(Object thatObject) {
	// canonical part
	if (this.equals(thatObject))
	    return;

	if (! (thatObject instanceof Loadobjs))
	    return;
	Loadobjs that = (Loadobjs) thatObject;

	// per-field part
	Loadobj[] oldLoadobjs = this.los;

	// fixe NPE problem when remove all entries in loadobjs
	if (that.los != null) {
	    this.los = new Loadobj[that.los.length];
	    System.arraycopy(that.los, 0,
			     this.los, 0,
			     that.los.length);

	    // 6519195
	    //delta(oldLoadobjs, this.los);
	} else {
	    this.los = null;
	}
	// 6519195
	delta(oldLoadobjs, this.los);
    }


    public Loadobj[] getLoadobjs() {
	return los;
    }

    public void setLoadobjs(Loadobj [] newlo) {
	los = newlo;
    }

    public void setXMLLoadobjs(Loadobj[] p) {
        xml_los = p;
    }

    public void mergeLoadobjs(Loadobj[] newLos) {
	if (newLos.length == 0)
	    return;

	Vector<Loadobj> merged_loadobjs = new Vector<Loadobj>();

	if (newLos.length != 0) {
            for (int sx = 0; sx < newLos.length; sx++) {
		merged_loadobjs.add(newLos[sx])	;
	    }
	}

	if (los.length == 0) {
	    Loadobj [] vars = new Loadobj[merged_loadobjs.size()];
	    los = merged_loadobjs.toArray(vars);
	    return;
	} else {
	    for (int i = 0; i < los.length; i++) {
		boolean found = false;
		int sx;
		for (sx = 0; sx < newLos.length; sx++) {
		    int begin = newLos[sx].lo.lastIndexOf("/"); // NOI18N
		    int end = newLos[sx].lo.indexOf(".so"); // NOI18N
		    String newUname = null;
		    if (end != -1)
			    newUname = newLos[sx].lo.substring(begin+1, end);
		    else if (begin != -1)
			    newUname = newLos[sx].lo.substring(begin+1);
			else
			    newUname = newLos[sx].lo;

		    begin = los[i].lo.lastIndexOf("/"); // NOI18N
		    end = los[i].lo.indexOf(".so"); // NOI18N
		    String losUname = null;
		    if (end != -1)
		        losUname = los[i].lo.substring(begin+1, end);
		    else if (begin != -1)
			    losUname = los[i].lo.substring(begin+1);
			else
			    losUname = los[i].lo;

		    if ( (IpeUtils.sameString(newUname, losUname) )) {
			found = true;
			if (newLos[sx].skip != los[i].skip)
			    newLos[sx].skip = true;
			break;
		    }
		}
		if (!found) 
		    merged_loadobjs.add(los[i])	;
	    }
	    Loadobj [] vars = new Loadobj[merged_loadobjs.size()];
	    los = merged_loadobjs.toArray(vars);
	}
    }
	
    public void adjustLoadobjs() {
	if (xml_los.length == 0)
	    return;

	mergeLoadobjs(xml_los);

/*
	Vector merged_loadobjs = new Vector();

	if (los.length == 0 && xml_los.length != 0) {
	    los = xml_los;
	    return;
	}

	for (int i = 0; i < los.length; i++) {
	    boolean found = false;
	    int sx;
	    for (sx = 0; sx < xml_los.length; sx++) {
		if ((IpeUtils.sameString(xml_los[sx].lo, los[i].lo) &&
		   xml_los[sx].skip == los[i].skip) ) {
		    found = true;
		    break;
		}
	    }
	    if (!found && sx != xml_los.length)
		merged_loadobjs.add(xml_los[sx])	;
	    merged_loadobjs.add(los[i])	;
	}
	Loadobj [] vars = new Loadobj[merged_loadobjs.size()];
	los = (Loadobj[])merged_loadobjs.toArray(vars);
    */
    }

    @Override
    public String toString() {
	boolean addSep = false;
	String ret = "";
	if (getLoadobjs() != null && getLoadobjs().length > 0) {
	    for (int i = 0; i < getLoadobjs().length; i++) {
		if (getLoadobjs()[i] != null && getLoadobjs()[i].skip){
		    if (addSep)
			ret += " "; // NOI18N
		    ret += getLoadobjs()[i].lo ; // NOI18N
		}
		addSep = true;
	    }
	}
	return ret;
    }

}

