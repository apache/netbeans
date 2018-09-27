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

/**
 *	This generated bean class SafRemoteContextType matches the schema element 'saf-remote-context-type'.
 *  The root bean class is WeblogicJms
 *
 *	Generated on Tue Jul 25 03:26:59 PDT 2017
 * @Generated
 */

package org.netbeans.modules.j2ee.weblogic9.dd.jms1031;

import org.w3c.dom.*;
import org.netbeans.modules.schema2beans.*;
import java.beans.*;
import java.util.*;

// BEGIN_NOI18N

public class SafRemoteContextType extends org.netbeans.modules.j2ee.weblogic9.dd.jms1031.NamedEntityType
{

	static Vector comparators = new Vector();
	private static final org.netbeans.modules.schema2beans.Version runtimeVersion = new org.netbeans.modules.schema2beans.Version(5, 0, 0);
	;	// NOI18N

	static public final String NAME = "Name";	// NOI18N
	static public final String NOTES = "Notes";	// NOI18N
	static public final String SAF_LOGIN_CONTEXT = "SafLoginContext";	// NOI18N
	static public final String COMPRESSION_THRESHOLD = "CompressionThreshold";	// NOI18N
	static public final String REPLY_TO_SAF_REMOTE_CONTEXT_NAME = "ReplyToSafRemoteContextName";	// NOI18N

	public SafRemoteContextType() {
		this(Common.USE_DEFAULT_VALUES);
	}

	public SafRemoteContextType(int options)
	{
		super(comparators, runtimeVersion);
		// Properties (see root bean comments for the bean graph)
		initPropertyTables(4);
		this.createProperty("notes", 	// NOI18N
			NOTES, 
			Common.TYPE_0_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			java.lang.String.class);
		this.createProperty("saf-login-context", 	// NOI18N
			SAF_LOGIN_CONTEXT, 
			Common.TYPE_0_1 | Common.TYPE_BEAN | Common.TYPE_KEY, 
			SafLoginContextType.class);
		this.createProperty("compression-threshold", 	// NOI18N
			COMPRESSION_THRESHOLD, 
			Common.TYPE_0_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			Integer.class);
		this.createProperty("reply-to-saf-remote-context-name", 	// NOI18N
			REPLY_TO_SAF_REMOTE_CONTEXT_NAME, 
			Common.TYPE_0_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			java.lang.String.class);
		this.initialize(options);
	}

	// Setting the default values of the properties
	void initialize(int options) {

	}

	// This attribute is mandatory
	public void setName(java.lang.String value) {
		setAttributeValue(NAME, value);
	}

	//
	public java.lang.String getName() {
		return getAttributeValue(NAME);
	}

	// This attribute is optional
	public void setNotes(java.lang.String value) {
		this.setValue(NOTES, value);
	}

	//
	public java.lang.String getNotes() {
		return (java.lang.String)this.getValue(NOTES);
	}

	// This attribute is optional
	public void setSafLoginContext(SafLoginContextType value) {
		this.setValue(SAF_LOGIN_CONTEXT, value);
	}

	//
	public SafLoginContextType getSafLoginContext() {
		return (SafLoginContextType)this.getValue(SAF_LOGIN_CONTEXT);
	}

	// This attribute is optional
	public void setCompressionThreshold(int value) {
		this.setValue(COMPRESSION_THRESHOLD, java.lang.Integer.valueOf(value));
	}

	//
	public int getCompressionThreshold() {
		Integer ret = (Integer)this.getValue(COMPRESSION_THRESHOLD);
		if (ret == null)
			throw new RuntimeException(Common.getMessage(
				"NoValueForElt_msg",
				new Object[] {"COMPRESSION_THRESHOLD", "int"}));
		return ((java.lang.Integer)ret).intValue();
	}

	// This attribute is optional
	public void setReplyToSafRemoteContextName(java.lang.String value) {
		this.setValue(REPLY_TO_SAF_REMOTE_CONTEXT_NAME, value);
	}

	//
	public java.lang.String getReplyToSafRemoteContextName() {
		return (java.lang.String)this.getValue(REPLY_TO_SAF_REMOTE_CONTEXT_NAME);
	}

	/**
	 * Create a new bean using it's default constructor.
	 * This does not add it to any bean graph.
	 */
	public SafLoginContextType newSafLoginContextType() {
		return new SafLoginContextType();
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
		// Validating property name
		if (getName() == null) {
			throw new org.netbeans.modules.schema2beans.ValidateException("getName() == null", org.netbeans.modules.schema2beans.ValidateException.FailureType.NULL_VALUE, "name", this);	// NOI18N
		}
		// Validating property notes
		// Validating property safLoginContext
		if (getSafLoginContext() != null) {
			getSafLoginContext().validate();
		}
		// Validating property compressionThreshold
		// Validating property replyToSafRemoteContextName
	}

	// Dump the content of this bean returning it as a String
	public void dump(StringBuffer str, String indent){
		String s;
		Object o;
		org.netbeans.modules.schema2beans.BaseBean n;
		str.append(indent);
		str.append("Notes");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append("<");	// NOI18N
		o = this.getNotes();
		str.append((o==null?"null":o.toString().trim()));	// NOI18N
		str.append(">\n");	// NOI18N
		this.dumpAttributes(NOTES, 0, str, indent);

		str.append(indent);
		str.append("SafLoginContext");	// NOI18N
		n = (org.netbeans.modules.schema2beans.BaseBean) this.getSafLoginContext();
		if (n != null)
			n.dump(str, indent + "\t");	// NOI18N
		else
			str.append(indent+"\tnull");	// NOI18N
		this.dumpAttributes(SAF_LOGIN_CONTEXT, 0, str, indent);

		if (this.getValue(COMPRESSION_THRESHOLD) != null) {
			str.append(indent);
			str.append("CompressionThreshold");	// NOI18N
			str.append(indent+"\t");	// NOI18N
			str.append("<");	// NOI18N
			s = String.valueOf(this.getCompressionThreshold());
			str.append((s.trim()));	// NOI18N
			str.append(">\n");	// NOI18N
			this.dumpAttributes(COMPRESSION_THRESHOLD, 0, str, indent);
		}

		str.append(indent);
		str.append("ReplyToSafRemoteContextName");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append("<");	// NOI18N
		o = this.getReplyToSafRemoteContextName();
		str.append((o==null?"null":o.toString().trim()));	// NOI18N
		str.append(">\n");	// NOI18N
		this.dumpAttributes(REPLY_TO_SAF_REMOTE_CONTEXT_NAME, 0, str, indent);

	}
	public String dumpBeanNode(){
		StringBuffer str = new StringBuffer();
		str.append("SafRemoteContextType\n");	// NOI18N
		this.dump(str, "\n  ");	// NOI18N
		return str.toString();
	}}

// END_NOI18N

