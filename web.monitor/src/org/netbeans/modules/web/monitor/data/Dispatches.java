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
 */

package org.netbeans.modules.web.monitor.data;

import org.w3c.dom.*;
import org.netbeans.modules.schema2beans.*;
import java.beans.*;
import java.util.*;

public class Dispatches extends BaseBean {

    static Vector comparators = new Vector();

    static public final String DISPATCHDATA = "DispatchData"; // NOI18N

    public Dispatches() {
	this(Common.USE_DEFAULT_VALUES);
    }

    public Dispatches(int options) {
	super(comparators, new org.netbeans.modules.schema2beans.Version(1, 0, 8));
	// Properties (see root bean comments for the bean graph)
	this.createProperty("DispatchData", 	// NOI18N
			    DISPATCHDATA, 
			    Common.TYPE_0_N | Common.TYPE_BEAN | Common.TYPE_KEY, 
			    DispatchData.class);
	this.createAttribute(DISPATCHDATA, "resource", "Resource", //NOI18N
			     AttrProp.CDATA | AttrProp.REQUIRED,
			     null, null);
	this.initialize(options);
    }

    // Setting the default values of the properties
    void initialize(int options) { }

    // This attribute is an array, possibly empty
    public void setDispatchData(int index, DispatchData value) {
	this.setValue(DISPATCHDATA, index, value);
    }

    //
    public DispatchData getDispatchData(int index) {
	return (DispatchData)this.getValue(DISPATCHDATA, index);
    }

    // This attribute is an array, possibly empty
    public void setDispatchData(DispatchData[] value) {
	this.setValue(DISPATCHDATA, value);
    }

    //
    public DispatchData[] getDispatchData() {
	return (DispatchData[])this.getValues(DISPATCHDATA);
    }

    // Return the number of properties
    public int sizeDispatchData() {
	return this.size(DISPATCHDATA);
    }

    // Add a new element returning its index in the list
    public int addDispatchData(DispatchData value) {
	return this.addValue(DISPATCHDATA, value);
    }

    // Remove an element using its reference
    // Returns the index the element had in the list
    public int removeDispatchData(DispatchData value) {
	return this.removeValue(DISPATCHDATA, value);
    }

    // This method verifies that the mandatory properties are set
    public boolean verify() {
	return true;
    }

    //
    static public void addComparator(BeanComparator c) {
	comparators.add(c);
    }

    //
    static public void removeComparator(BeanComparator c) {
	comparators.remove(c);
    }
    // Dump the content of this bean returning it as a String
    public void dump(StringBuffer str, String indent) {
	String s;
	Object o;
	BaseBean n;
	str.append(indent);
	str.append("DispatchData["+this.sizeDispatchData()+"]");	// NOI18N
	for(int i=0; i<this.sizeDispatchData(); i++) {
	    str.append(indent+"\t"); // NOI18N
	    str.append("#"+i+":"); // NOI18N
	    n = this.getDispatchData(i);
	    if (n != null)
		n.dump(str, indent + "\t");	// NOI18N
	    else
		str.append(indent+"\tnull");	// NOI18N
	    this.dumpAttributes(DISPATCHDATA, i, str, indent);
	}

    }
    public String dumpBeanNode() {
	StringBuffer str = new StringBuffer();
	str.append("Dispatches\n");	// NOI18N
	this.dump(str, "\n  ");	// NOI18N
	return str.toString();
    }
}



