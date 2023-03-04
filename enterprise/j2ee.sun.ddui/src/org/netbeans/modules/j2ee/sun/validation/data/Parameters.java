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

/**
 *	This generated bean class Parameters matches the schema element parameters
 *
 */

package org.netbeans.modules.j2ee.sun.validation.data;

import org.w3c.dom.*;
import org.netbeans.modules.schema2beans.*;
import java.beans.*;
import java.util.*;

// BEGIN_NOI18N

public class Parameters extends org.netbeans.modules.schema2beans.BaseBean
{

	static Vector comparators = new Vector();

	static public final String PARAMETER = "Parameter";	       // NOI18N

	public Parameters() {
		this(Common.USE_DEFAULT_VALUES);
	}

	public Parameters(int options)
	{
		super(comparators, new org.netbeans.modules.schema2beans.Version(1, 2, 0));
		// Properties (see root bean comments for the bean graph)
		this.createProperty("parameter", 	// NOI18N
			PARAMETER, 
			Common.TYPE_1_N | Common.TYPE_BEAN | Common.TYPE_KEY, 
			Parameter.class);
		this.initialize(options);
	}

	// Setting the default values of the properties
	void initialize(int options)
	{

	}

	// This attribute is an array containing at least one element
	public void setParameter(int index, Parameter value) {
		this.setValue(PARAMETER, index, value);
	}

	//
	public Parameter getParameter(int index) {
		return (Parameter)this.getValue(PARAMETER, index);
	}

	// This attribute is an array containing at least one element
	public void setParameter(Parameter[] value) {
		this.setValue(PARAMETER, value);
	}

	//
	public Parameter[] getParameter() {
		return (Parameter[])this.getValues(PARAMETER);
	}

	// Return the number of properties
	public int sizeParameter() {
		return this.size(PARAMETER);
	}

	// Add a new element returning its index in the list
	public int addParameter(org.netbeans.modules.j2ee.sun.validation.data.Parameter value) {
		return this.addValue(PARAMETER, value);
	}

	//
	// Remove an element using its reference
	// Returns the index the element had in the list
	//
	public int removeParameter(org.netbeans.modules.j2ee.sun.validation.data.Parameter value) {
		return this.removeValue(PARAMETER, value);
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
		// Validating property parameter
		if (sizeParameter() == 0) {
			throw new org.netbeans.modules.schema2beans.ValidateException("sizeParameter() == 0", "parameter", this);	// NOI18N
		}
		for (int _index = 0; _index < sizeParameter(); ++_index) {
			org.netbeans.modules.j2ee.sun.validation.data.Parameter element = getParameter(_index);
			if (element != null) {
				element.validate();
			}
		}
	}

	// Dump the content of this bean returning it as a String
	public void dump(StringBuffer str, String indent){
		String s;
		Object o;
		org.netbeans.modules.schema2beans.BaseBean n;
		str.append(indent);
		str.append("Parameter["+this.sizeParameter()+"]");	// NOI18N
		for(int i=0; i<this.sizeParameter(); i++)
		{
			str.append(indent+"\t");
			str.append("#"+i+":");
			n = (org.netbeans.modules.schema2beans.BaseBean) this.getParameter(i);
			if (n != null)
				n.dump(str, indent + "\t");	// NOI18N
			else
				str.append(indent+"\tnull");	// NOI18N
			this.dumpAttributes(PARAMETER, i, str, indent);
		}

	}
	public String dumpBeanNode(){
		StringBuffer str = new StringBuffer();
		str.append("Parameters\n");	// NOI18N
		this.dump(str, "\n  ");	// NOI18N
		return str.toString();
	}}

// END_NOI18N


/*
		The following schema file has been used for generation:

<!--
  XML DTD for for validation xml.
  validation.xml is used to specify Constraints to be applied to
  elements.
 
  $Revision$
-->


<!--
This is the root element
-->
<!ELEMENT validation  (element*) >
<!ATTLIST validation
        validate    CDATA        (true | false)     "true">


<!--
This element represents, the set of Constraints to be applied to
the given element.
-->
<!ELEMENT element (name, check*)>


<!--
This element represents, a particular Constraint.
Note : Information about this Constraint must be provided through
corresponding <check-info> object in constraints.xml Sub element
<name> should match with <name> of corresponding <check-info>
element defined in constraints.xml.
-->
<!ELEMENT check (name, parameters?)>


<!--
This element represent, Constraint parameters.
Number of sub elements, <parameter> should match with the number
of <argument> sub elements, of corresponding <arguments> element
in constraints.xml
-->
<!ELEMENT parameters (parameter+)>


<!--
This element represents, a Constraint parameter.
Sub elements <name> should match with the <name> sub element of
corresponding <argument> element in constraints.xml
<value> could be one or more. In case of an variable array
argument, multiple <value> elements will be used.
Example : InConstraint
-->
<!ELEMENT parameter (name, value+)>


<!--
Used in elements : <element>, <check> and <parameter>
In <element> , it represents the name(xpath - complete absolute
name of an element(leaf).
In <check> , it represents name of a Constraint. This is the
linking element for <check> element in validation.xml and
<check-info> element in constraints.xml.
In <parameter>, it represents name of parameter. This is the
linking element for <parameter> element in validation.xml and
<argument> element in constraints.xml.
-->
<!ELEMENT name (#PCDATA)>


<!--
This element represents the value of a parameter.
-->
<!ELEMENT value (#PCDATA)>

*/
