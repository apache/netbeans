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
 *	This generated bean class JspDescriptorType matches the schema element 'jsp-descriptorType'.
 *  The root bean class is WeblogicWebApp
 *
 *	Generated on Tue Jul 25 03:27:01 PDT 2017
 * @Generated
 */

package org.netbeans.modules.j2ee.weblogic9.dd.web1030;

import org.w3c.dom.*;
import org.netbeans.modules.schema2beans.*;
import java.beans.*;
import java.util.*;

// BEGIN_NOI18N

public class JspDescriptorType extends org.netbeans.modules.schema2beans.BaseBean
	 implements org.netbeans.modules.j2ee.weblogic9.dd.model.JspDescriptorType
{

	static Vector comparators = new Vector();
	private static final org.netbeans.modules.schema2beans.Version runtimeVersion = new org.netbeans.modules.schema2beans.Version(5, 0, 0);
	;	// NOI18N

	static public final String KEEPGENERATED = "Keepgenerated";	// NOI18N
	static public final String PACKAGE_PREFIX = "PackagePrefix";	// NOI18N
	static public final String SUPER_CLASS = "SuperClass";	// NOI18N
	static public final String PAGE_CHECK_SECONDS = "PageCheckSeconds";	// NOI18N
	static public final String PAGECHECKSECONDSJ2EEID = "PageCheckSecondsJ2eeId";	// NOI18N
	static public final String PRECOMPILE = "Precompile";	// NOI18N
	static public final String PRECOMPILE_CONTINUE = "PrecompileContinue";	// NOI18N
	static public final String VERBOSE = "Verbose";	// NOI18N
	static public final String WORKING_DIR = "WorkingDir";	// NOI18N
	static public final String PRINT_NULLS = "PrintNulls";	// NOI18N
	static public final String BACKWARD_COMPATIBLE = "BackwardCompatible";	// NOI18N
	static public final String ENCODING = "Encoding";	// NOI18N
	static public final String EXACT_MAPPING = "ExactMapping";	// NOI18N
	static public final String DEFAULT_FILE_NAME = "DefaultFileName";	// NOI18N
	static public final String RTEXPRVALUE_JSP_PARAM_NAME = "RtexprvalueJspParamName";	// NOI18N
	static public final String DEBUG = "Debug";	// NOI18N
	static public final String COMPRESS_HTML_TEMPLATE = "CompressHtmlTemplate";	// NOI18N
	static public final String OPTIMIZE_JAVA_EXPRESSION = "OptimizeJavaExpression";	// NOI18N

	public JspDescriptorType() {
		this(Common.USE_DEFAULT_VALUES);
	}

	public JspDescriptorType(int options)
	{
		super(comparators, runtimeVersion);
		// Properties (see root bean comments for the bean graph)
		initPropertyTables(17);
		this.createProperty("keepgenerated", 	// NOI18N
			KEEPGENERATED, 
			Common.TYPE_0_1 | Common.TYPE_BOOLEAN | Common.TYPE_SHOULD_NOT_BE_EMPTY | Common.TYPE_KEY, 
			Boolean.class);
		this.createProperty("package-prefix", 	// NOI18N
			PACKAGE_PREFIX, 
			Common.TYPE_0_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			java.lang.String.class);
		this.createProperty("super-class", 	// NOI18N
			SUPER_CLASS, 
			Common.TYPE_0_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			java.lang.String.class);
		this.createProperty("page-check-seconds", 	// NOI18N
			PAGE_CHECK_SECONDS, 
			Common.TYPE_0_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			java.math.BigInteger.class);
		this.createAttribute(PAGE_CHECK_SECONDS, "j2ee:id", "J2eeId", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
		this.createProperty("precompile", 	// NOI18N
			PRECOMPILE, 
			Common.TYPE_0_1 | Common.TYPE_BOOLEAN | Common.TYPE_SHOULD_NOT_BE_EMPTY | Common.TYPE_KEY, 
			Boolean.class);
		this.createProperty("precompile-continue", 	// NOI18N
			PRECOMPILE_CONTINUE, 
			Common.TYPE_0_1 | Common.TYPE_BOOLEAN | Common.TYPE_SHOULD_NOT_BE_EMPTY | Common.TYPE_KEY, 
			Boolean.class);
		this.createProperty("verbose", 	// NOI18N
			VERBOSE, 
			Common.TYPE_0_1 | Common.TYPE_BOOLEAN | Common.TYPE_SHOULD_NOT_BE_EMPTY | Common.TYPE_KEY, 
			Boolean.class);
		this.createProperty("working-dir", 	// NOI18N
			WORKING_DIR, 
			Common.TYPE_0_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			java.lang.String.class);
		this.createProperty("print-nulls", 	// NOI18N
			PRINT_NULLS, 
			Common.TYPE_0_1 | Common.TYPE_BOOLEAN | Common.TYPE_SHOULD_NOT_BE_EMPTY | Common.TYPE_KEY, 
			Boolean.class);
		this.createProperty("backward-compatible", 	// NOI18N
			BACKWARD_COMPATIBLE, 
			Common.TYPE_0_1 | Common.TYPE_BOOLEAN | Common.TYPE_SHOULD_NOT_BE_EMPTY | Common.TYPE_KEY, 
			Boolean.class);
		this.createProperty("encoding", 	// NOI18N
			ENCODING, 
			Common.TYPE_0_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			java.lang.String.class);
		this.createProperty("exact-mapping", 	// NOI18N
			EXACT_MAPPING, 
			Common.TYPE_0_1 | Common.TYPE_BOOLEAN | Common.TYPE_SHOULD_NOT_BE_EMPTY | Common.TYPE_KEY, 
			Boolean.class);
		this.createProperty("default-file-name", 	// NOI18N
			DEFAULT_FILE_NAME, 
			Common.TYPE_0_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			java.lang.String.class);
		this.createProperty("rtexprvalue-jsp-param-name", 	// NOI18N
			RTEXPRVALUE_JSP_PARAM_NAME, 
			Common.TYPE_0_1 | Common.TYPE_BOOLEAN | Common.TYPE_SHOULD_NOT_BE_EMPTY | Common.TYPE_KEY, 
			Boolean.class);
		this.createProperty("debug", 	// NOI18N
			DEBUG, 
			Common.TYPE_0_1 | Common.TYPE_BOOLEAN | Common.TYPE_SHOULD_NOT_BE_EMPTY | Common.TYPE_KEY, 
			Boolean.class);
		this.createProperty("compress-html-template", 	// NOI18N
			COMPRESS_HTML_TEMPLATE, 
			Common.TYPE_0_1 | Common.TYPE_BOOLEAN | Common.TYPE_SHOULD_NOT_BE_EMPTY | Common.TYPE_KEY, 
			Boolean.class);
		this.createProperty("optimize-java-expression", 	// NOI18N
			OPTIMIZE_JAVA_EXPRESSION, 
			Common.TYPE_0_1 | Common.TYPE_BOOLEAN | Common.TYPE_SHOULD_NOT_BE_EMPTY | Common.TYPE_KEY, 
			Boolean.class);
		this.initialize(options);
	}

	// Setting the default values of the properties
	void initialize(int options) {

	}

	// This attribute is optional
	public void setKeepgenerated(boolean value) {
		this.setValue(KEEPGENERATED, (value ? java.lang.Boolean.TRUE : java.lang.Boolean.FALSE));
	}

	//
	public boolean isKeepgenerated() {
		Boolean ret = (Boolean)this.getValue(KEEPGENERATED);
		if (ret == null)
			ret = (Boolean)Common.defaultScalarValue(Common.TYPE_BOOLEAN);
		return ((java.lang.Boolean)ret).booleanValue();
	}

	// This attribute is optional
	public void setPackagePrefix(java.lang.String value) {
		this.setValue(PACKAGE_PREFIX, value);
	}

	//
	public java.lang.String getPackagePrefix() {
		return (java.lang.String)this.getValue(PACKAGE_PREFIX);
	}

	// This attribute is optional
	public void setSuperClass(java.lang.String value) {
		this.setValue(SUPER_CLASS, value);
	}

	//
	public java.lang.String getSuperClass() {
		return (java.lang.String)this.getValue(SUPER_CLASS);
	}

	// This attribute is optional
	public void setPageCheckSeconds(java.math.BigInteger value) {
		this.setValue(PAGE_CHECK_SECONDS, value);
	}

	//
	public java.math.BigInteger getPageCheckSeconds() {
		return (java.math.BigInteger)this.getValue(PAGE_CHECK_SECONDS);
	}

	// This attribute is optional
	public void setPageCheckSecondsJ2eeId(java.lang.String value) {
		// Make sure we've got a place to put this attribute.
		if (size(PAGE_CHECK_SECONDS) == 0) {
			setValue(PAGE_CHECK_SECONDS, "");
		}
		setAttributeValue(PAGE_CHECK_SECONDS, "J2eeId", value);
	}

	//
	public java.lang.String getPageCheckSecondsJ2eeId() {
		// If our element does not exist, then the attribute does not exist.
		if (size(PAGE_CHECK_SECONDS) == 0) {
			return null;
		} else {
			return getAttributeValue(PAGE_CHECK_SECONDS, "J2eeId");
		}
	}

	// This attribute is optional
	public void setPrecompile(boolean value) {
		this.setValue(PRECOMPILE, (value ? java.lang.Boolean.TRUE : java.lang.Boolean.FALSE));
	}

	//
	public boolean isPrecompile() {
		Boolean ret = (Boolean)this.getValue(PRECOMPILE);
		if (ret == null)
			ret = (Boolean)Common.defaultScalarValue(Common.TYPE_BOOLEAN);
		return ((java.lang.Boolean)ret).booleanValue();
	}

	// This attribute is optional
	public void setPrecompileContinue(boolean value) {
		this.setValue(PRECOMPILE_CONTINUE, (value ? java.lang.Boolean.TRUE : java.lang.Boolean.FALSE));
	}

	//
	public boolean isPrecompileContinue() {
		Boolean ret = (Boolean)this.getValue(PRECOMPILE_CONTINUE);
		if (ret == null)
			ret = (Boolean)Common.defaultScalarValue(Common.TYPE_BOOLEAN);
		return ((java.lang.Boolean)ret).booleanValue();
	}

	// This attribute is optional
	public void setVerbose(boolean value) {
		this.setValue(VERBOSE, (value ? java.lang.Boolean.TRUE : java.lang.Boolean.FALSE));
	}

	//
	public boolean isVerbose() {
		Boolean ret = (Boolean)this.getValue(VERBOSE);
		if (ret == null)
			ret = (Boolean)Common.defaultScalarValue(Common.TYPE_BOOLEAN);
		return ((java.lang.Boolean)ret).booleanValue();
	}

	// This attribute is optional
	public void setWorkingDir(java.lang.String value) {
		this.setValue(WORKING_DIR, value);
	}

	//
	public java.lang.String getWorkingDir() {
		return (java.lang.String)this.getValue(WORKING_DIR);
	}

	// This attribute is optional
	public void setPrintNulls(boolean value) {
		this.setValue(PRINT_NULLS, (value ? java.lang.Boolean.TRUE : java.lang.Boolean.FALSE));
	}

	//
	public boolean isPrintNulls() {
		Boolean ret = (Boolean)this.getValue(PRINT_NULLS);
		if (ret == null)
			ret = (Boolean)Common.defaultScalarValue(Common.TYPE_BOOLEAN);
		return ((java.lang.Boolean)ret).booleanValue();
	}

	// This attribute is optional
	public void setBackwardCompatible(boolean value) {
		this.setValue(BACKWARD_COMPATIBLE, (value ? java.lang.Boolean.TRUE : java.lang.Boolean.FALSE));
	}

	//
	public boolean isBackwardCompatible() {
		Boolean ret = (Boolean)this.getValue(BACKWARD_COMPATIBLE);
		if (ret == null)
			ret = (Boolean)Common.defaultScalarValue(Common.TYPE_BOOLEAN);
		return ((java.lang.Boolean)ret).booleanValue();
	}

	// This attribute is optional
	public void setEncoding(java.lang.String value) {
		this.setValue(ENCODING, value);
	}

	//
	public java.lang.String getEncoding() {
		return (java.lang.String)this.getValue(ENCODING);
	}

	// This attribute is optional
	public void setExactMapping(boolean value) {
		this.setValue(EXACT_MAPPING, (value ? java.lang.Boolean.TRUE : java.lang.Boolean.FALSE));
	}

	//
	public boolean isExactMapping() {
		Boolean ret = (Boolean)this.getValue(EXACT_MAPPING);
		if (ret == null)
			ret = (Boolean)Common.defaultScalarValue(Common.TYPE_BOOLEAN);
		return ((java.lang.Boolean)ret).booleanValue();
	}

	// This attribute is optional
	public void setDefaultFileName(java.lang.String value) {
		this.setValue(DEFAULT_FILE_NAME, value);
	}

	//
	public java.lang.String getDefaultFileName() {
		return (java.lang.String)this.getValue(DEFAULT_FILE_NAME);
	}

	// This attribute is optional
	public void setRtexprvalueJspParamName(boolean value) {
		this.setValue(RTEXPRVALUE_JSP_PARAM_NAME, (value ? java.lang.Boolean.TRUE : java.lang.Boolean.FALSE));
	}

	//
	public boolean isRtexprvalueJspParamName() {
		Boolean ret = (Boolean)this.getValue(RTEXPRVALUE_JSP_PARAM_NAME);
		if (ret == null)
			ret = (Boolean)Common.defaultScalarValue(Common.TYPE_BOOLEAN);
		return ((java.lang.Boolean)ret).booleanValue();
	}

	// This attribute is optional
	public void setDebug(boolean value) {
		this.setValue(DEBUG, (value ? java.lang.Boolean.TRUE : java.lang.Boolean.FALSE));
	}

	//
	public boolean isDebug() {
		Boolean ret = (Boolean)this.getValue(DEBUG);
		if (ret == null)
			ret = (Boolean)Common.defaultScalarValue(Common.TYPE_BOOLEAN);
		return ((java.lang.Boolean)ret).booleanValue();
	}

	// This attribute is optional
	public void setCompressHtmlTemplate(boolean value) {
		this.setValue(COMPRESS_HTML_TEMPLATE, (value ? java.lang.Boolean.TRUE : java.lang.Boolean.FALSE));
	}

	//
	public boolean isCompressHtmlTemplate() {
		Boolean ret = (Boolean)this.getValue(COMPRESS_HTML_TEMPLATE);
		if (ret == null)
			ret = (Boolean)Common.defaultScalarValue(Common.TYPE_BOOLEAN);
		return ((java.lang.Boolean)ret).booleanValue();
	}

	// This attribute is optional
	public void setOptimizeJavaExpression(boolean value) {
		this.setValue(OPTIMIZE_JAVA_EXPRESSION, (value ? java.lang.Boolean.TRUE : java.lang.Boolean.FALSE));
	}

	//
	public boolean isOptimizeJavaExpression() {
		Boolean ret = (Boolean)this.getValue(OPTIMIZE_JAVA_EXPRESSION);
		if (ret == null)
			ret = (Boolean)Common.defaultScalarValue(Common.TYPE_BOOLEAN);
		return ((java.lang.Boolean)ret).booleanValue();
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
		// Validating property keepgenerated
		{
			boolean patternPassed = false;
			if ((isKeepgenerated() ? "true" : "false").matches("(true|True|TRUE|false|False|FALSE|yes|Yes|YES|Y|no|No|NO|N|1|0)")) {
				patternPassed = true;
			}
			restrictionFailure = !patternPassed;
		}
		if (restrictionFailure) {
			throw new org.netbeans.modules.schema2beans.ValidateException("isKeepgenerated()", org.netbeans.modules.schema2beans.ValidateException.FailureType.DATA_RESTRICTION, "keepgenerated", this);	// NOI18N
		}
		// Validating property packagePrefix
		// Validating property superClass
		// Validating property pageCheckSeconds
		// Validating property pageCheckSecondsJ2eeId
		if (getPageCheckSecondsJ2eeId() != null) {
			// has whitespace restriction
			if (restrictionFailure) {
				throw new org.netbeans.modules.schema2beans.ValidateException("getPageCheckSecondsJ2eeId() whiteSpace (collapse)", org.netbeans.modules.schema2beans.ValidateException.FailureType.DATA_RESTRICTION, "pageCheckSecondsJ2eeId", this);	// NOI18N
			}
		}
		// Validating property precompile
		{
			boolean patternPassed = false;
			if ((isPrecompile() ? "true" : "false").matches("(true|True|TRUE|false|False|FALSE|yes|Yes|YES|Y|no|No|NO|N|1|0)")) {
				patternPassed = true;
			}
			restrictionFailure = !patternPassed;
		}
		if (restrictionFailure) {
			throw new org.netbeans.modules.schema2beans.ValidateException("isPrecompile()", org.netbeans.modules.schema2beans.ValidateException.FailureType.DATA_RESTRICTION, "precompile", this);	// NOI18N
		}
		// Validating property precompileContinue
		{
			boolean patternPassed = false;
			if ((isPrecompileContinue() ? "true" : "false").matches("(true|True|TRUE|false|False|FALSE|yes|Yes|YES|Y|no|No|NO|N|1|0)")) {
				patternPassed = true;
			}
			restrictionFailure = !patternPassed;
		}
		if (restrictionFailure) {
			throw new org.netbeans.modules.schema2beans.ValidateException("isPrecompileContinue()", org.netbeans.modules.schema2beans.ValidateException.FailureType.DATA_RESTRICTION, "precompileContinue", this);	// NOI18N
		}
		// Validating property verbose
		{
			boolean patternPassed = false;
			if ((isVerbose() ? "true" : "false").matches("(true|True|TRUE|false|False|FALSE|yes|Yes|YES|Y|no|No|NO|N|1|0)")) {
				patternPassed = true;
			}
			restrictionFailure = !patternPassed;
		}
		if (restrictionFailure) {
			throw new org.netbeans.modules.schema2beans.ValidateException("isVerbose()", org.netbeans.modules.schema2beans.ValidateException.FailureType.DATA_RESTRICTION, "verbose", this);	// NOI18N
		}
		// Validating property workingDir
		// Validating property printNulls
		{
			boolean patternPassed = false;
			if ((isPrintNulls() ? "true" : "false").matches("(true|True|TRUE|false|False|FALSE|yes|Yes|YES|Y|no|No|NO|N|1|0)")) {
				patternPassed = true;
			}
			restrictionFailure = !patternPassed;
		}
		if (restrictionFailure) {
			throw new org.netbeans.modules.schema2beans.ValidateException("isPrintNulls()", org.netbeans.modules.schema2beans.ValidateException.FailureType.DATA_RESTRICTION, "printNulls", this);	// NOI18N
		}
		// Validating property backwardCompatible
		{
			boolean patternPassed = false;
			if ((isBackwardCompatible() ? "true" : "false").matches("(true|True|TRUE|false|False|FALSE|yes|Yes|YES|Y|no|No|NO|N|1|0)")) {
				patternPassed = true;
			}
			restrictionFailure = !patternPassed;
		}
		if (restrictionFailure) {
			throw new org.netbeans.modules.schema2beans.ValidateException("isBackwardCompatible()", org.netbeans.modules.schema2beans.ValidateException.FailureType.DATA_RESTRICTION, "backwardCompatible", this);	// NOI18N
		}
		// Validating property encoding
		// Validating property exactMapping
		{
			boolean patternPassed = false;
			if ((isExactMapping() ? "true" : "false").matches("(true|True|TRUE|false|False|FALSE|yes|Yes|YES|Y|no|No|NO|N|1|0)")) {
				patternPassed = true;
			}
			restrictionFailure = !patternPassed;
		}
		if (restrictionFailure) {
			throw new org.netbeans.modules.schema2beans.ValidateException("isExactMapping()", org.netbeans.modules.schema2beans.ValidateException.FailureType.DATA_RESTRICTION, "exactMapping", this);	// NOI18N
		}
		// Validating property defaultFileName
		// Validating property rtexprvalueJspParamName
		{
			boolean patternPassed = false;
			if ((isRtexprvalueJspParamName() ? "true" : "false").matches("(true|True|TRUE|false|False|FALSE|yes|Yes|YES|Y|no|No|NO|N|1|0)")) {
				patternPassed = true;
			}
			restrictionFailure = !patternPassed;
		}
		if (restrictionFailure) {
			throw new org.netbeans.modules.schema2beans.ValidateException("isRtexprvalueJspParamName()", org.netbeans.modules.schema2beans.ValidateException.FailureType.DATA_RESTRICTION, "rtexprvalueJspParamName", this);	// NOI18N
		}
		// Validating property debug
		{
			boolean patternPassed = false;
			if ((isDebug() ? "true" : "false").matches("(true|True|TRUE|false|False|FALSE|yes|Yes|YES|Y|no|No|NO|N|1|0)")) {
				patternPassed = true;
			}
			restrictionFailure = !patternPassed;
		}
		if (restrictionFailure) {
			throw new org.netbeans.modules.schema2beans.ValidateException("isDebug()", org.netbeans.modules.schema2beans.ValidateException.FailureType.DATA_RESTRICTION, "debug", this);	// NOI18N
		}
		// Validating property compressHtmlTemplate
		{
			boolean patternPassed = false;
			if ((isCompressHtmlTemplate() ? "true" : "false").matches("(true|True|TRUE|false|False|FALSE|yes|Yes|YES|Y|no|No|NO|N|1|0)")) {
				patternPassed = true;
			}
			restrictionFailure = !patternPassed;
		}
		if (restrictionFailure) {
			throw new org.netbeans.modules.schema2beans.ValidateException("isCompressHtmlTemplate()", org.netbeans.modules.schema2beans.ValidateException.FailureType.DATA_RESTRICTION, "compressHtmlTemplate", this);	// NOI18N
		}
		// Validating property optimizeJavaExpression
		{
			boolean patternPassed = false;
			if ((isOptimizeJavaExpression() ? "true" : "false").matches("(true|True|TRUE|false|False|FALSE|yes|Yes|YES|Y|no|No|NO|N|1|0)")) {
				patternPassed = true;
			}
			restrictionFailure = !patternPassed;
		}
		if (restrictionFailure) {
			throw new org.netbeans.modules.schema2beans.ValidateException("isOptimizeJavaExpression()", org.netbeans.modules.schema2beans.ValidateException.FailureType.DATA_RESTRICTION, "optimizeJavaExpression", this);	// NOI18N
		}
	}

	// Dump the content of this bean returning it as a String
	public void dump(StringBuffer str, String indent){
		String s;
		Object o;
		org.netbeans.modules.schema2beans.BaseBean n;
		str.append(indent);
		str.append("Keepgenerated");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append((this.isKeepgenerated()?"true":"false"));
		this.dumpAttributes(KEEPGENERATED, 0, str, indent);

		str.append(indent);
		str.append("PackagePrefix");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append("<");	// NOI18N
		o = this.getPackagePrefix();
		str.append((o==null?"null":o.toString().trim()));	// NOI18N
		str.append(">\n");	// NOI18N
		this.dumpAttributes(PACKAGE_PREFIX, 0, str, indent);

		str.append(indent);
		str.append("SuperClass");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append("<");	// NOI18N
		o = this.getSuperClass();
		str.append((o==null?"null":o.toString().trim()));	// NOI18N
		str.append(">\n");	// NOI18N
		this.dumpAttributes(SUPER_CLASS, 0, str, indent);

		str.append(indent);
		str.append("PageCheckSeconds");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append("<");	// NOI18N
		o = this.getPageCheckSeconds();
		str.append((o==null?"null":o.toString().trim()));	// NOI18N
		str.append(">\n");	// NOI18N
		this.dumpAttributes(PAGE_CHECK_SECONDS, 0, str, indent);

		str.append(indent);
		str.append("Precompile");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append((this.isPrecompile()?"true":"false"));
		this.dumpAttributes(PRECOMPILE, 0, str, indent);

		str.append(indent);
		str.append("PrecompileContinue");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append((this.isPrecompileContinue()?"true":"false"));
		this.dumpAttributes(PRECOMPILE_CONTINUE, 0, str, indent);

		str.append(indent);
		str.append("Verbose");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append((this.isVerbose()?"true":"false"));
		this.dumpAttributes(VERBOSE, 0, str, indent);

		str.append(indent);
		str.append("WorkingDir");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append("<");	// NOI18N
		o = this.getWorkingDir();
		str.append((o==null?"null":o.toString().trim()));	// NOI18N
		str.append(">\n");	// NOI18N
		this.dumpAttributes(WORKING_DIR, 0, str, indent);

		str.append(indent);
		str.append("PrintNulls");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append((this.isPrintNulls()?"true":"false"));
		this.dumpAttributes(PRINT_NULLS, 0, str, indent);

		str.append(indent);
		str.append("BackwardCompatible");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append((this.isBackwardCompatible()?"true":"false"));
		this.dumpAttributes(BACKWARD_COMPATIBLE, 0, str, indent);

		str.append(indent);
		str.append("Encoding");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append("<");	// NOI18N
		o = this.getEncoding();
		str.append((o==null?"null":o.toString().trim()));	// NOI18N
		str.append(">\n");	// NOI18N
		this.dumpAttributes(ENCODING, 0, str, indent);

		str.append(indent);
		str.append("ExactMapping");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append((this.isExactMapping()?"true":"false"));
		this.dumpAttributes(EXACT_MAPPING, 0, str, indent);

		str.append(indent);
		str.append("DefaultFileName");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append("<");	// NOI18N
		o = this.getDefaultFileName();
		str.append((o==null?"null":o.toString().trim()));	// NOI18N
		str.append(">\n");	// NOI18N
		this.dumpAttributes(DEFAULT_FILE_NAME, 0, str, indent);

		str.append(indent);
		str.append("RtexprvalueJspParamName");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append((this.isRtexprvalueJspParamName()?"true":"false"));
		this.dumpAttributes(RTEXPRVALUE_JSP_PARAM_NAME, 0, str, indent);

		str.append(indent);
		str.append("Debug");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append((this.isDebug()?"true":"false"));
		this.dumpAttributes(DEBUG, 0, str, indent);

		str.append(indent);
		str.append("CompressHtmlTemplate");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append((this.isCompressHtmlTemplate()?"true":"false"));
		this.dumpAttributes(COMPRESS_HTML_TEMPLATE, 0, str, indent);

		str.append(indent);
		str.append("OptimizeJavaExpression");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append((this.isOptimizeJavaExpression()?"true":"false"));
		this.dumpAttributes(OPTIMIZE_JAVA_EXPRESSION, 0, str, indent);

	}
	public String dumpBeanNode(){
		StringBuffer str = new StringBuffer();
		str.append("JspDescriptorType\n");	// NOI18N
		this.dump(str, "\n  ");	// NOI18N
		return str.toString();
	}}

// END_NOI18N

