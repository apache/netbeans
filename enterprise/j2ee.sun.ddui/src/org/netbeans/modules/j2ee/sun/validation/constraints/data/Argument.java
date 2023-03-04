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

package org.netbeans.modules.j2ee.sun.validation.constraints.data;

import org.w3c.dom.*;
import org.netbeans.modules.schema2beans.*;
import java.beans.*;
import java.util.*;

// BEGIN_NOI18N

public class Argument extends org.netbeans.modules.schema2beans.BaseBean
{

	static Vector comparators = new Vector();

	static public final String NAME = "Name";	// NOI18N
	static public final String TYPE = "Type";	// NOI18N

	public Argument() {
		this(Common.USE_DEFAULT_VALUES);
	}

	public Argument(int options)
	{
		super(comparators, new org.netbeans.modules.schema2beans.Version(1, 2, 0));
		// Properties (see root bean comments for the bean graph)
		this.createProperty("name", 	// NOI18N
			NAME, 
			Common.TYPE_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			String.class);
		this.createProperty("type", 	// NOI18N
			TYPE, 
			Common.TYPE_0_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			String.class);
		this.initialize(options);
	}

	// Setting the default values of the properties
	void initialize(int options)
	{
		
	}

	// This attribute is mandatory
	public void setName(String value) {
		this.setValue(NAME, value);
	}

	//
	public String getName() {
		return (String)this.getValue(NAME);
	}

	// This attribute is optional
	public void setType(String value) {
		this.setValue(TYPE, value);
	}

	//
	public String getType() {
		return (String)this.getValue(TYPE);
	}

	//
	public static void addComparator(org.netbeans.modules.schema2beans.BeanComparator c) {
		comparators.add(c);
	}

	//
	public static void removeComparator(org.netbeans.modules.schema2beans.BeanComparator c) {
		comparators.remove(c);
	}
	public void validate() throws org.netbeans.modules.schema2beans.ValidateException {
		boolean restrictionFailure = false;
		// Validating property name
		if (getName() == null) {
			throw new org.netbeans.modules.schema2beans.ValidateException("getName() == null", "name", this);	// NOI18N
		}
		// Validating property type
		if (getType() != null) {
		}
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
		s = this.getName();
		str.append((s==null?"null":s.trim()));	// NOI18N
		str.append(">\n");	// NOI18N
		this.dumpAttributes(NAME, 0, str, indent);

		str.append(indent);
		str.append("Type");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append("<");	// NOI18N
		s = this.getType();
		str.append((s==null?"null":s.trim()));	// NOI18N
		str.append(">\n");	// NOI18N
		this.dumpAttributes(TYPE, 0, str, indent);

	}
	public String dumpBeanNode(){
		StringBuffer str = new StringBuffer();
		str.append("Argument\n");	// NOI18N
		this.dump(str, "\n  ");	// NOI18N
		return str.toString();
	}}

// END_NOI18N


/*
		The following schema file has been used for generation:

<!--
  XML DTD for for constraints xml.
  constraints.xml is used to specify provide information of the
  Constraints to the Validation framework.
 
  $Revision$
-->


<!--
This is the root element.
-->
<!ELEMENT constraints (check-info*)>


<!--
This represents an information, about a particular Constraint.
Provides information of a Constraint represented by corresponding
<check> element in validation.xml.
Sub element <name> is used to link this element with the
corresponding <check> element in validation.xml.
-->
<!ELEMENT check-info (name, classname, arguments?)>


<!--
This element represents information of a Constraint class arguments.
Number of sub elements, <argument> should match with the number
of <parameter> sub elements, of corresponding <arguments> element
in validation.xml
-->
<!ELEMENT arguments (argument+)>


<!--
This element represents information of a single Constraint class
argument.
Sub elements <name> should match with the <name> sub element of
corresponding <parameter> element in constraints.xml
-->
<!ELEMENT argument (name, type?)>


<!--
Used in two elements <check-info> and <argument>
In <check-info>, it represents a Constraint name and is the linking
element between <check> element in validation.xml and <check-info>
element in constraints.xml.
In <argument>, it represents argument name and is the linking element
between <parameter> element in validation.xml and <argument> element
in constraints.xml.
-->
<!ELEMENT name (#PCDATA)>


<!--
This element represents Constraint class name.
Constraint class should provide the constructor with no arguments.
Constraint class should also provide the set* methods for all the
required arguments.
Constraint class is always created using default constructor and
then the arguments are set using set* methods.
-->
<!ELEMENT classname (#PCDATA)>


<!--
This element represents the type of an argument.
If not specified, it defaults to java.lang.String
-->
<!ELEMENT type (#PCDATA)>

*/
