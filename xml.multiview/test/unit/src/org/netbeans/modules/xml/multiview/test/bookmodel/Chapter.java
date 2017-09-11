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
 *	This generated bean class Chapter matches the schema element 'chapter'.
 *  The root bean class is Book
 *
 *	Generated on Wed Mar 09 17:37:54 CET 2005
 * @Generated
 */

package org.netbeans.modules.xml.multiview.test.bookmodel;

import org.w3c.dom.*;
import org.netbeans.modules.schema2beans.*;
import java.beans.*;
import java.util.*;

// BEGIN_NOI18N

public class Chapter extends org.netbeans.modules.schema2beans.BaseBean
{

	static Vector comparators = new Vector();
	private static final org.netbeans.modules.schema2beans.Version runtimeVersion = new org.netbeans.modules.schema2beans.Version(4, 0, 0);

	static public final String TITLE = "Title";	// NOI18N
	static public final String SUMMARY = "Summary";	// NOI18N
	static public final String PARAGRAPH = "Paragraph";	// NOI18N

	public Chapter() {
		this(Common.USE_DEFAULT_VALUES);
	}

	public Chapter(int options)
	{
		super(comparators, runtimeVersion);
		// Properties (see root bean comments for the bean graph)
		initPropertyTables(3);
		this.createProperty("title", 	// NOI18N
			TITLE, 
			Common.TYPE_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			String.class);
		this.createAttribute(TITLE, "lang", "Lang", 
						AttrProp.CDATA | AttrProp.FIXED,
						null, "en");
		this.createProperty("summary", 	// NOI18N
			SUMMARY, 
			Common.TYPE_0_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			String.class);
		this.createProperty("paragraph", 	// NOI18N
			PARAGRAPH, 
			Common.TYPE_0_N | Common.TYPE_STRING | Common.TYPE_KEY, 
			String.class);
		this.initialize(options);
	}

	// Setting the default values of the properties
	void initialize(int options) {

	}

	// This attribute is mandatory
	public void setTitle(String value) {
		this.setValue(TITLE, value);
	}

	//
	public String getTitle() {
		return (String)this.getValue(TITLE);
	}

	// This attribute is optional
	public void setSummary(String value) {
		this.setValue(SUMMARY, value);
	}

	//
	public String getSummary() {
		return (String)this.getValue(SUMMARY);
	}

	// This attribute is an array, possibly empty
	public void setParagraph(int index, String value) {
		this.setValue(PARAGRAPH, index, value);
	}

	//
	public String getParagraph(int index) {
		return (String)this.getValue(PARAGRAPH, index);
	}

	// Return the number of properties
	public int sizeParagraph() {
		return this.size(PARAGRAPH);
	}

	// This attribute is an array, possibly empty
	public void setParagraph(String[] value) {
		this.setValue(PARAGRAPH, value);
	}

	//
	public String[] getParagraph() {
		return (String[])this.getValues(PARAGRAPH);
	}

	// Add a new element returning its index in the list
	public int addParagraph(String value) {
		int positionOfNewItem = this.addValue(PARAGRAPH, value);
		return positionOfNewItem;
	}

	//
	// Remove an element using its reference
	// Returns the index the element had in the list
	//
	public int removeParagraph(String value) {
		return this.removeValue(PARAGRAPH, value);
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
		boolean restrictionPassed = false;
		// Validating property title
		if (getTitle() == null) {
			throw new org.netbeans.modules.schema2beans.ValidateException("getTitle() == null", org.netbeans.modules.schema2beans.ValidateException.FailureType.NULL_VALUE, "title", this);	// NOI18N
		}
		// Validating property summary
		// Validating property paragraph
	}

	// Dump the content of this bean returning it as a String
	public void dump(StringBuffer str, String indent){
		String s;
		Object o;
		org.netbeans.modules.schema2beans.BaseBean n;
		str.append(indent);
		str.append("Title");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append("<");	// NOI18N
		o = this.getTitle();
		str.append((o==null?"null":o.toString().trim()));	// NOI18N
		str.append(">\n");	// NOI18N
		this.dumpAttributes(TITLE, 0, str, indent);

		str.append(indent);
		str.append("Summary");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append("<");	// NOI18N
		o = this.getSummary();
		str.append((o==null?"null":o.toString().trim()));	// NOI18N
		str.append(">\n");	// NOI18N
		this.dumpAttributes(SUMMARY, 0, str, indent);

		str.append(indent);
		str.append("Paragraph["+this.sizeParagraph()+"]");	// NOI18N
		for(int i=0; i<this.sizeParagraph(); i++)
		{
			str.append(indent+"\t");
			str.append("#"+i+":");
			str.append(indent+"\t");	// NOI18N
			str.append("<");	// NOI18N
			o = this.getParagraph(i);
			str.append((o==null?"null":o.toString().trim()));	// NOI18N
			str.append(">\n");	// NOI18N
			this.dumpAttributes(PARAGRAPH, i, str, indent);
		}

	}
	public String dumpBeanNode(){
		StringBuffer str = new StringBuffer();
		str.append("Chapter\n");	// NOI18N
		this.dump(str, "\n  ");	// NOI18N
		return str.toString();
	}}

// END_NOI18N


/*
		The following schema file has been used for generation:

<!ELEMENT book (title, chapter+, paperback?, price?) > 
<!ELEMENT title (#PCDATA) > 
<!ELEMENT chapter ( title, summary?, paragraph* ) > 
<!ELEMENT summary (#PCDATA) > 
<!ELEMENT paragraph (#PCDATA) > 
<!ELEMENT paperback EMPTY > 
<!ELEMENT price (#PCDATA) >
<!ATTLIST book instock (yes | no) "yes" > 
<!ATTLIST title lang CDATA #FIXED "en" > 
<!ATTLIST chapter length CDATA #IMPLIED >

*/
