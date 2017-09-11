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
 *	This generated bean class Book matches the schema element 'book'.
 *
 *	Generated on Wed Mar 09 17:37:54 CET 2005
 *
 *	This class matches the root element of the DTD,
 *	and is the root of the following bean graph:
 *
 *	book <book> : Book
 *		[attr: instock ENUM ( yes no ) yes]
 *		title <title> : String
 *			[attr: lang CDATA #FIXED en]
 *		chapter <chapter> : Chapter[1,n]
 *			[attr: length CDATA #IMPLIED ]
 *			title <title> : String
 *				[attr: lang CDATA #FIXED en]
 *			summary <summary> : String[0,1]
 *			paragraph <paragraph> : String[0,n]
 *		paperback <paperback> : boolean[0,1]
 *			EMPTY : String
 *		price <price> : String[0,1]
 *
 * @Generated
 */

package org.netbeans.modules.xml.multiview.test.bookmodel;

import org.w3c.dom.*;
import org.netbeans.modules.schema2beans.*;
import java.beans.*;
import java.util.*;
import java.io.*;

// BEGIN_NOI18N

public class Book extends org.netbeans.modules.schema2beans.BaseBean
{

	static Vector comparators = new Vector();
	private static final org.netbeans.modules.schema2beans.Version runtimeVersion = new org.netbeans.modules.schema2beans.Version(4, 0, 0);

	static public final String TITLE = "Title";	// NOI18N
	static public final String CHAPTER = "Chapter";	// NOI18N
	static public final String PAPERBACK = "Paperback";	// NOI18N
	static public final String PRICE = "Price";	// NOI18N

	public Book() {
		this(null, Common.USE_DEFAULT_VALUES);
	}

	public Book(org.w3c.dom.Node doc, int options) {
		this(Common.NO_DEFAULT_VALUES);
		try {
			initFromNode(doc, options);
		}
		catch (Schema2BeansException e) {
			throw new RuntimeException(e);
		}
	}
	protected void initFromNode(org.w3c.dom.Node doc, int options) throws Schema2BeansException
	{
		if (doc == null)
		{
			doc = GraphManager.createRootElementNode("book");	// NOI18N
			if (doc == null)
				throw new Schema2BeansException(Common.getMessage(
					"CantCreateDOMRoot_msg", "book"));
		}
		Node n = GraphManager.getElementNode("book", doc);	// NOI18N
		if (n == null)
			throw new Schema2BeansException(Common.getMessage(
				"DocRootNotInDOMGraph_msg", "book", doc.getFirstChild().getNodeName()));

		this.graphManager.setXmlDocument(doc);

		// Entry point of the createBeans() recursive calls
		this.createBean(n, this.graphManager());
		this.initialize(options);
	}
	public Book(int options)
	{
		super(comparators, runtimeVersion);
		initOptions(options);
	}
	protected void initOptions(int options)
	{
		// The graph manager is allocated in the bean root
		this.graphManager = new GraphManager(this);
		this.createRoot("book", "Book",	// NOI18N
			Common.TYPE_1 | Common.TYPE_BEAN, Book.class);

		// Properties (see root bean comments for the bean graph)
		initPropertyTables(4);
		this.createProperty("title", 	// NOI18N
			TITLE, 
			Common.TYPE_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			String.class);
		this.createAttribute(TITLE, "lang", "Lang", 
						AttrProp.CDATA | AttrProp.FIXED,
						null, "en");
		this.createProperty("chapter", 	// NOI18N
			CHAPTER, 
			Common.TYPE_1_N | Common.TYPE_BEAN | Common.TYPE_KEY, 
			Chapter.class);
		this.createAttribute(CHAPTER, "length", "Length", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
		this.createProperty("paperback", 	// NOI18N
			PAPERBACK, 
			Common.TYPE_0_1 | Common.TYPE_BOOLEAN | Common.TYPE_KEY, 
			Boolean.class);
		this.createProperty("price", 	// NOI18N
			PRICE, 
			Common.TYPE_0_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			String.class);
		this.createAttribute("instock", "Instock", 
						AttrProp.ENUM,
						new String[] {
							"yes",
							"no"
						}, "yes");
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

	// This attribute is an array containing at least one element
	public void setChapter(int index, Chapter value) {
		this.setValue(CHAPTER, index, value);
	}

	//
	public Chapter getChapter(int index) {
		return (Chapter)this.getValue(CHAPTER, index);
	}

	// Return the number of properties
	public int sizeChapter() {
		return this.size(CHAPTER);
	}

	// This attribute is an array containing at least one element
	public void setChapter(Chapter[] value) {
		this.setValue(CHAPTER, value);
	}

	//
	public Chapter[] getChapter() {
		return (Chapter[])this.getValues(CHAPTER);
	}

	// Add a new element returning its index in the list
	public int addChapter(org.netbeans.modules.xml.multiview.test.bookmodel.Chapter value) {
		int positionOfNewItem = this.addValue(CHAPTER, value);
		return positionOfNewItem;
	}

	//
	// Remove an element using its reference
	// Returns the index the element had in the list
	//
	public int removeChapter(org.netbeans.modules.xml.multiview.test.bookmodel.Chapter value) {
		return this.removeValue(CHAPTER, value);
	}

	// This attribute is optional
	public void setPaperback(boolean value) {
		this.setValue(PAPERBACK, (value ? java.lang.Boolean.TRUE : java.lang.Boolean.FALSE));
	}

	//
	public boolean isPaperback() {
		Boolean ret = (Boolean)this.getValue(PAPERBACK);
		if (ret == null)
			ret = (Boolean)Common.defaultScalarValue(Common.TYPE_BOOLEAN);
		return ((java.lang.Boolean)ret).booleanValue();
	}

	// This attribute is optional
	public void setPrice(String value) {
		this.setValue(PRICE, value);
	}

	//
	public String getPrice() {
		return (String)this.getValue(PRICE);
	}

	/**
	 * Create a new bean using it's default constructor.
	 * This does not add it to any bean graph.
	 */
	public Chapter newChapter() {
		return new Chapter();
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
	// This method returns the root of the bean graph
	// Each call creates a new bean graph from the specified DOM graph
	//
	public static Book createGraph(org.w3c.dom.Node doc) {
		return new Book(doc, Common.NO_DEFAULT_VALUES);
	}

	public static Book createGraph(java.io.File f) throws java.io.IOException {
		java.io.InputStream in = new java.io.FileInputStream(f);
		try {
			return createGraph(in, false);
		} finally {
			in.close();
		}
	}

	public static Book createGraph(java.io.InputStream in) {
		return createGraph(in, false);
	}

	public static Book createGraph(java.io.InputStream in, boolean validate) {
		try {
			Document doc = GraphManager.createXmlDocument(in, validate);
			return createGraph(doc);
		}
		catch (Exception t) {
			throw new RuntimeException(Common.getMessage(
				"DOMGraphCreateFailed_msg",
				t));
		}
	}

	//
	// This method returns the root for a new empty bean graph
	//
	public static Book createGraph() {
		return new Book();
	}

	public void validate() throws org.netbeans.modules.schema2beans.ValidateException {
		boolean restrictionFailure = false;
		boolean restrictionPassed = false;
		// Validating property title
		if (getTitle() == null) {
			throw new org.netbeans.modules.schema2beans.ValidateException("getTitle() == null", org.netbeans.modules.schema2beans.ValidateException.FailureType.NULL_VALUE, "title", this);	// NOI18N
		}
		// Validating property chapter
		if (sizeChapter() == 0) {
			throw new org.netbeans.modules.schema2beans.ValidateException("sizeChapter() == 0", org.netbeans.modules.schema2beans.ValidateException.FailureType.NULL_VALUE, "chapter", this);	// NOI18N
		}
		for (int _index = 0; _index < sizeChapter(); ++_index) {
			org.netbeans.modules.xml.multiview.test.bookmodel.Chapter element = getChapter(_index);
			if (element != null) {
				element.validate();
			}
		}
		// Validating property paperback
		// Validating property price
	}

	// Special serializer: output XML as serialization
	private void writeObject(java.io.ObjectOutputStream out) throws java.io.IOException{
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		write(baos);
		String str = baos.toString();;
		// System.out.println("str='"+str+"'");
		out.writeUTF(str);
	}
	// Special deserializer: read XML as deserialization
	private void readObject(java.io.ObjectInputStream in) throws java.io.IOException, ClassNotFoundException{
		try{
			init(comparators, runtimeVersion);
			String strDocument = in.readUTF();
			// System.out.println("strDocument='"+strDocument+"'");
			ByteArrayInputStream bais = new ByteArrayInputStream(strDocument.getBytes());
			Document doc = GraphManager.createXmlDocument(bais, false);
			initOptions(Common.NO_DEFAULT_VALUES);
			initFromNode(doc, Common.NO_DEFAULT_VALUES);
		}
		catch (Schema2BeansException e) {
			throw new RuntimeException(e);
		}
	}

	public void _setSchemaLocation(String location) {
		if (beanProp().getAttrProp("xsi:schemaLocation", true) == null) {
			createAttribute("xmlns:xsi", "xmlns:xsi", AttrProp.CDATA | AttrProp.IMPLIED, null, "http://www.w3.org/2001/XMLSchema-instance");
			setAttributeValue("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
			createAttribute("xsi:schemaLocation", "xsi:schemaLocation", AttrProp.CDATA | AttrProp.IMPLIED, null, location);
		}
		setAttributeValue("xsi:schemaLocation", location);
	}

	public String _getSchemaLocation() {
		if (beanProp().getAttrProp("xsi:schemaLocation", true) == null) {
			createAttribute("xmlns:xsi", "xmlns:xsi", AttrProp.CDATA | AttrProp.IMPLIED, null, "http://www.w3.org/2001/XMLSchema-instance");
			setAttributeValue("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
			createAttribute("xsi:schemaLocation", "xsi:schemaLocation", AttrProp.CDATA | AttrProp.IMPLIED, null, null);
		}
		return getAttributeValue("xsi:schemaLocation");
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
		str.append("Chapter["+this.sizeChapter()+"]");	// NOI18N
		for(int i=0; i<this.sizeChapter(); i++)
		{
			str.append(indent+"\t");
			str.append("#"+i+":");
			n = (org.netbeans.modules.schema2beans.BaseBean) this.getChapter(i);
			if (n != null)
				n.dump(str, indent + "\t");	// NOI18N
			else
				str.append(indent+"\tnull");	// NOI18N
			this.dumpAttributes(CHAPTER, i, str, indent);
		}

		str.append(indent);
		str.append("Paperback");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append((this.isPaperback()?"true":"false"));
		this.dumpAttributes(PAPERBACK, 0, str, indent);

		str.append(indent);
		str.append("Price");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append("<");	// NOI18N
		o = this.getPrice();
		str.append((o==null?"null":o.toString().trim()));	// NOI18N
		str.append(">\n");	// NOI18N
		this.dumpAttributes(PRICE, 0, str, indent);

	}
	public String dumpBeanNode(){
		StringBuffer str = new StringBuffer();
		str.append("Book\n");	// NOI18N
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
