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
/**
 *	This generated bean class FileEntry matches the schema element 'file-entry'.
 *  The root bean class is DeploymentPlan
 *
 *	Generated on Wed Sep 29 16:29:52 PDT 2004
 * @Generated
 */

package org.netbeans.modules.j2ee.sun.share.plan;

import org.w3c.dom.*;
import org.netbeans.modules.schema2beans.*;
import java.beans.*;
import java.util.*;

// BEGIN_NOI18N

public class FileEntry extends org.netbeans.modules.schema2beans.BaseBean
{

	static Vector comparators = new Vector();
	private static final org.netbeans.modules.schema2beans.Version runtimeVersion = new org.netbeans.modules.schema2beans.Version(3, 6, 1);

	static public final String NAME = "Name";	// NOI18N
	static public final String CONTENT = "Content";	// NOI18N
	static public final String URI = "Uri";	// NOI18N

	public FileEntry() {
		this(Common.USE_DEFAULT_VALUES);
	}

	public FileEntry(int options)
	{
		super(comparators, runtimeVersion);
		// Properties (see root bean comments for the bean graph)
		initPropertyTables(3);
		this.createProperty("name", 	// NOI18N
			NAME, Common.TYPE_VETOABLE |
			Common.TYPE_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			String.class);
		this.createProperty("content", 	// NOI18N
			CONTENT, Common.TYPE_VETOABLE |
			Common.TYPE_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			String.class);
		this.createProperty("uri", 	// NOI18N
			URI, Common.TYPE_VETOABLE |
			Common.TYPE_0_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			String.class);
		this.initialize(options);
	}

	// Setting the default values of the properties
	void initialize(int options) {

	}

	// This attribute is mandatory
	public void setName(String value) throws java.beans.PropertyVetoException {
		try
		{
			this.setValue(NAME, value);
		}
		catch(BaseProperty.VetoException ve)
		{
			throw ve.getPropertyVetoException();
		}
	}

	//
	public String getName() {
		return (String)this.getValue(NAME);
	}

	// This attribute is mandatory
	public void setContent(String value) throws java.beans.PropertyVetoException {
		try
		{
			this.setValue(CONTENT, value);
		}
		catch(BaseProperty.VetoException ve)
		{
			throw ve.getPropertyVetoException();
		}
	}

	//
	public String getContent() {
		return (String)this.getValue(CONTENT);
	}

	// This attribute is optional
	public void setUri(String value) throws java.beans.PropertyVetoException {
		try
		{
			this.setValue(URI, value);
		}
		catch(BaseProperty.VetoException ve)
		{
			throw ve.getPropertyVetoException();
		}
	}

	//
	public String getUri() {
		return (String)this.getValue(URI);
	}

	//
	public static void addComparator(org.netbeans.modules.schema2beans.BeanComparator c) {
		comparators.add(c);
	}

	//
	public static void removeComparator(org.netbeans.modules.schema2beans.BeanComparator c) {
		comparators.remove(c);
	}
	//
	public void addVetoableChangeListener(String n, java.beans.VetoableChangeListener l) {
		BeanProp p = this.beanProp(n);
		if (p != null)
			p.addVCListener(l);
	}

	//
	public void removeVetoableChangeListener(String n, java.beans.VetoableChangeListener l) {
		BeanProp p = this.beanProp(n);
		if (p != null)
			p.removeVCListener(l);
	}

	public void validate() throws org.netbeans.modules.schema2beans.ValidateException {
	}

	// Dump the content of this bean returning it as a String
	public void dump(StringBuffer str, String indent){
		String s;
		Object o;
		org.netbeans.modules.schema2beans.BaseBean n;
		str.append(indent);
		str.append("Name");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append("<");	// NOI18N
		o = this.getName();
		str.append((o==null?"null":o.toString().trim()));	// NOI18N
		str.append(">\n");	// NOI18N
		this.dumpAttributes(NAME, 0, str, indent);

		str.append(indent);
		str.append("Content");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append("<");	// NOI18N
		o = this.getContent();
		str.append((o==null?"null":o.toString().trim()));	// NOI18N
		str.append(">\n");	// NOI18N
		this.dumpAttributes(CONTENT, 0, str, indent);

		str.append(indent);
		str.append("Uri");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append("<");	// NOI18N
		o = this.getUri();
		str.append((o==null?"null":o.toString().trim()));	// NOI18N
		str.append(">\n");	// NOI18N
		this.dumpAttributes(URI, 0, str, indent);

	}
	public String dumpBeanNode(){
		StringBuffer str = new StringBuffer();
		str.append("FileEntry\n");	// NOI18N
		this.dump(str, "\n  ");	// NOI18N
		return str.toString();
	}}

// END_NOI18N


/*
		The following schema file has been used for generation:

<?xml version="1.0" encoding="UTF-8"?>

<!--
    Document   : deployment-plan.dtd
    Created on : April 7, 2003, 2:33 PM
    Author     : vkraemer
    Description:
        Purpose of the document follows.
-->

<!ELEMENT deployment-plan (file-entry*) >

<!ELEMENT file-entry (name, content, uri?) >

<!ELEMENT name (#PCDATA) >
<!ELEMENT content (#PCDATA) >
<!ELEMENT uri (#PCDATA) >

*/

