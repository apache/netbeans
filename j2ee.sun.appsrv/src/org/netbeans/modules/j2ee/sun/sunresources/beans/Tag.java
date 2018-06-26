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
 *	This generated bean class Tag matches the schema element tag
 *
 *	Generated on Thu Sep 25 15:18:26 PDT 2003
 */

package org.netbeans.modules.j2ee.sun.sunresources.beans;

import org.w3c.dom.*;
import org.netbeans.modules.schema2beans.*;
import java.beans.*;
import java.util.*;

// BEGIN_NOI18N

public class Tag extends org.netbeans.modules.schema2beans.BaseBean
{

	static Vector comparators = new Vector();

	static public final String TAG_ITEM = "TagItem";	// NOI18N

	public Tag() {
		this(Common.USE_DEFAULT_VALUES);
	}

	public Tag(int options)
	{
		super(comparators, new org.netbeans.modules.schema2beans.Version(1, 2, 0));
		// Properties (see root bean comments for the bean graph)
		this.createProperty("tag-item", 	// NOI18N
			TAG_ITEM, 
			Common.TYPE_0_N | Common.TYPE_STRING | Common.TYPE_KEY, 
			String.class);
		this.initialize(options);
	}

	// Setting the default values of the properties
	void initialize(int options)
	{
	
	}

	// This attribute is an array, possibly empty
	public void setTagItem(int index, String value) {
		this.setValue(TAG_ITEM, index, value);
	}

	//
	public String getTagItem(int index) {
		return (String)this.getValue(TAG_ITEM, index);
	}

	// This attribute is an array, possibly empty
	public void setTagItem(String[] value) {
		this.setValue(TAG_ITEM, value);
	}

	//
	public String[] getTagItem() {
		return (String[])this.getValues(TAG_ITEM);
	}

	// Return the number of properties
	public int sizeTagItem() {
		return this.size(TAG_ITEM);
	}

	// Add a new element returning its index in the list
	public int addTagItem(String value) {
		return this.addValue(TAG_ITEM, value);
	}

	//
	// Remove an element using its reference
	// Returns the index the element had in the list
	//
	public int removeTagItem(String value) {
		return this.removeValue(TAG_ITEM, value);
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
		// Validating property tagItem
		for (int _index = 0; _index < sizeTagItem(); ++_index) {
			getTagItem(_index);

		}
	}

	// Dump the content of this bean returning it as a String
	public void dump(StringBuffer str, String indent){
		String s;
		str.append(indent);
		str.append("TagItem["+this.sizeTagItem()+"]");	// NOI18N
		for(int i=0; i<this.sizeTagItem(); i++)
		{
			str.append(indent+"\t");
			str.append("#"+i+":");
			str.append(indent+"\t");	// NOI18N
			str.append("<");	// NOI18N
			s = this.getTagItem(i);
			str.append((s==null?"null":s.trim()));	// NOI18N
			str.append(">\n");	// NOI18N
			this.dumpAttributes(TAG_ITEM, i, str, indent);
		}

	}
	public String dumpBeanNode(){
		StringBuffer str = new StringBuffer();
		str.append("Tag\n");	// NOI18N
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
