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
 *	This generated bean class OptionValuePair matches the schema element option-value-pair
 *
 *	Generated on Thu Sep 25 15:18:26 PDT 2003
 */

package org.netbeans.modules.j2ee.sun.sunresources.beans;

import org.w3c.dom.*;
import org.netbeans.modules.schema2beans.*;
import java.beans.*;
import java.util.*;

// BEGIN_NOI18N

public class OptionValuePair extends org.netbeans.modules.schema2beans.BaseBean
{

	static Vector comparators = new Vector();

	static public final String OPTION_NAME = "OptionName";	// NOI18N
	static public final String CONDITIONAL_VALUE = "ConditionalValue";	// NOI18N

	public OptionValuePair() {
		this(Common.USE_DEFAULT_VALUES);
	}

	public OptionValuePair(int options)
	{
		super(comparators, new org.netbeans.modules.schema2beans.Version(1, 2, 0));
		// Properties (see root bean comments for the bean graph)
		this.createProperty("option-name", 	// NOI18N
			OPTION_NAME, 
			Common.TYPE_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			String.class);
		this.createProperty("conditional-value", 	// NOI18N
			CONDITIONAL_VALUE, 
			Common.TYPE_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			String.class);
		this.initialize(options);
	}

	// Setting the default values of the properties
	void initialize(int options)
	{
		
	}

	// This attribute is mandatory
	public void setOptionName(String value) {
		this.setValue(OPTION_NAME, value);
	}

	//
	public String getOptionName() {
		return (String)this.getValue(OPTION_NAME);
	}

	// This attribute is mandatory
	public void setConditionalValue(String value) {
		this.setValue(CONDITIONAL_VALUE, value);
	}

	//
	public String getConditionalValue() {
		return (String)this.getValue(CONDITIONAL_VALUE);
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
		// Validating property optionName
		if (getOptionName() == null) {
			throw new org.netbeans.modules.schema2beans.ValidateException("getOptionName() == null", "optionName", this);	// NOI18N
		}
		// Validating property conditionalValue
		if (getConditionalValue() == null) {
			throw new org.netbeans.modules.schema2beans.ValidateException("getConditionalValue() == null", "conditionalValue", this);	// NOI18N
		}
	}

	// Dump the content of this bean returning it as a String
	public void dump(StringBuffer str, String indent){
		String s;
		str.append(indent);
		str.append("OptionName");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append("<");	// NOI18N
		s = this.getOptionName();
		str.append((s==null?"null":s.trim()));	// NOI18N
		str.append(">\n");	// NOI18N
		this.dumpAttributes(OPTION_NAME, 0, str, indent);

		str.append(indent);
		str.append("ConditionalValue");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append("<");	// NOI18N
		s = this.getConditionalValue();
		str.append((s==null?"null":s.trim()));	// NOI18N
		str.append(">\n");	// NOI18N
		this.dumpAttributes(CONDITIONAL_VALUE, 0, str, indent);

	}
	public String dumpBeanNode(){
		StringBuffer str = new StringBuffer();
		str.append("OptionValuePair\n");	// NOI18N
		this.dump(str, "\n  ");	// NOI18N
		return str.toString();
	}}

// END_NOI18N


/*
		The following schema file has been used for generation:

<!ELEMENT wizard (name, field-group+)>
<!ELEMENT field-group (name, field+)>
<!ELEMENT field (name, field-value, tag?)>
<!ATTLIST field  field-type                 CDATA     "string"
                 required                   CDATA     "true">
<!ELEMENT field-value (default-field-value, option-value-pair*)>
<!ELEMENT option-value-pair (option-name, conditional-value)>
<!ELEMENT name (#PCDATA)>
<!ELEMENT default-field-value (#PCDATA)>
<!ELEMENT option-name (#PCDATA)>
<!ELEMENT conditional-value (#PCDATA)>
<!ELEMENT tag (tag-item*)>
<!ELEMENT tag-item (#PCDATA)>



*/
