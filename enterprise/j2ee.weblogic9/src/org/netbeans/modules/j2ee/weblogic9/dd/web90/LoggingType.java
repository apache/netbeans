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
 *	This generated bean class LoggingType matches the schema element 'loggingType'.
 *  The root bean class is WeblogicWebApp
 *
 *	Generated on Tue Jul 25 03:27:06 PDT 2017
 * @Generated
 */

package org.netbeans.modules.j2ee.weblogic9.dd.web90;

import org.w3c.dom.*;
import org.netbeans.modules.schema2beans.*;
import java.beans.*;
import java.util.*;

// BEGIN_NOI18N

public class LoggingType extends org.netbeans.modules.schema2beans.BaseBean
{

	static Vector comparators = new Vector();
	private static final org.netbeans.modules.schema2beans.Version runtimeVersion = new org.netbeans.modules.schema2beans.Version(5, 0, 0);
	;	// NOI18N

	static public final String ID = "Id";	// NOI18N
	static public final String LOG_FILENAME = "LogFilename";	// NOI18N
	static public final String LOGFILENAMEJ2EEID = "LogFilenameJ2eeId";	// NOI18N
	static public final String LOGGING_ENABLED = "LoggingEnabled";	// NOI18N
	static public final String ROTATION_TYPE = "RotationType";	// NOI18N
	static public final String ROTATIONTYPEJ2EEID = "RotationTypeJ2eeId";	// NOI18N
	static public final String NUMBER_OF_FILES_LIMITED = "NumberOfFilesLimited";	// NOI18N
	static public final String FILE_COUNT = "FileCount";	// NOI18N
	static public final String FILECOUNTJ2EEID = "FileCountJ2eeId";	// NOI18N
	static public final String FILE_SIZE_LIMIT = "FileSizeLimit";	// NOI18N
	static public final String FILESIZELIMITJ2EEID = "FileSizeLimitJ2eeId";	// NOI18N
	static public final String ROTATE_LOG_ON_STARTUP = "RotateLogOnStartup";	// NOI18N
	static public final String LOG_FILE_ROTATION_DIR = "LogFileRotationDir";	// NOI18N
	static public final String LOGFILEROTATIONDIRJ2EEID = "LogFileRotationDirJ2eeId";	// NOI18N
	static public final String ROTATION_TIME = "RotationTime";	// NOI18N
	static public final String ROTATIONTIMEJ2EEID = "RotationTimeJ2eeId";	// NOI18N
	static public final String FILE_TIME_SPAN = "FileTimeSpan";	// NOI18N
	static public final String FILETIMESPANJ2EEID = "FileTimeSpanJ2eeId";	// NOI18N

	public LoggingType() {
		this(Common.USE_DEFAULT_VALUES);
	}

	public LoggingType(int options)
	{
		super(comparators, runtimeVersion);
		// Properties (see root bean comments for the bean graph)
		initPropertyTables(10);
		this.createProperty("log-filename", 	// NOI18N
			LOG_FILENAME, 
			Common.TYPE_0_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			java.lang.String.class);
		this.createAttribute(LOG_FILENAME, "j2ee:id", "J2eeId", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
		this.createProperty("logging-enabled", 	// NOI18N
			LOGGING_ENABLED, 
			Common.TYPE_0_1 | Common.TYPE_BOOLEAN | Common.TYPE_SHOULD_NOT_BE_EMPTY | Common.TYPE_KEY, 
			Boolean.class);
		this.createProperty("rotation-type", 	// NOI18N
			ROTATION_TYPE, 
			Common.TYPE_0_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			java.lang.String.class);
		this.createAttribute(ROTATION_TYPE, "j2ee:id", "J2eeId", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
		this.createProperty("number-of-files-limited", 	// NOI18N
			NUMBER_OF_FILES_LIMITED, 
			Common.TYPE_0_1 | Common.TYPE_BOOLEAN | Common.TYPE_SHOULD_NOT_BE_EMPTY | Common.TYPE_KEY, 
			Boolean.class);
		this.createProperty("file-count", 	// NOI18N
			FILE_COUNT, 
			Common.TYPE_0_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			Long.class);
		this.createAttribute(FILE_COUNT, "j2ee:id", "J2eeId", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
		this.createProperty("file-size-limit", 	// NOI18N
			FILE_SIZE_LIMIT, 
			Common.TYPE_0_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			Long.class);
		this.createAttribute(FILE_SIZE_LIMIT, "j2ee:id", "J2eeId", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
		this.createProperty("rotate-log-on-startup", 	// NOI18N
			ROTATE_LOG_ON_STARTUP, 
			Common.TYPE_0_1 | Common.TYPE_BOOLEAN | Common.TYPE_SHOULD_NOT_BE_EMPTY | Common.TYPE_KEY, 
			Boolean.class);
		this.createProperty("log-file-rotation-dir", 	// NOI18N
			LOG_FILE_ROTATION_DIR, 
			Common.TYPE_0_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			java.lang.String.class);
		this.createAttribute(LOG_FILE_ROTATION_DIR, "j2ee:id", "J2eeId", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
		this.createProperty("rotation-time", 	// NOI18N
			ROTATION_TIME, 
			Common.TYPE_0_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			java.lang.String.class);
		this.createAttribute(ROTATION_TIME, "j2ee:id", "J2eeId", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
		this.createProperty("file-time-span", 	// NOI18N
			FILE_TIME_SPAN, 
			Common.TYPE_0_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			Long.class);
		this.createAttribute(FILE_TIME_SPAN, "j2ee:id", "J2eeId", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
		this.initialize(options);
	}

	// Setting the default values of the properties
	void initialize(int options) {

	}

	// This attribute is optional
	public void setId(java.lang.String value) {
		setAttributeValue(ID, value);
	}

	//
	public java.lang.String getId() {
		return getAttributeValue(ID);
	}

	// This attribute is optional
	public void setLogFilename(java.lang.String value) {
		this.setValue(LOG_FILENAME, value);
	}

	//
	public java.lang.String getLogFilename() {
		return (java.lang.String)this.getValue(LOG_FILENAME);
	}

	// This attribute is optional
	public void setLogFilenameJ2eeId(java.lang.String value) {
		// Make sure we've got a place to put this attribute.
		if (size(LOG_FILENAME) == 0) {
			setValue(LOG_FILENAME, "");
		}
		setAttributeValue(LOG_FILENAME, "J2eeId", value);
	}

	//
	public java.lang.String getLogFilenameJ2eeId() {
		// If our element does not exist, then the attribute does not exist.
		if (size(LOG_FILENAME) == 0) {
			return null;
		} else {
			return getAttributeValue(LOG_FILENAME, "J2eeId");
		}
	}

	// This attribute is optional
	public void setLoggingEnabled(boolean value) {
		this.setValue(LOGGING_ENABLED, (value ? java.lang.Boolean.TRUE : java.lang.Boolean.FALSE));
	}

	//
	public boolean isLoggingEnabled() {
		Boolean ret = (Boolean)this.getValue(LOGGING_ENABLED);
		if (ret == null)
			ret = (Boolean)Common.defaultScalarValue(Common.TYPE_BOOLEAN);
		return ((java.lang.Boolean)ret).booleanValue();
	}

	// This attribute is optional
	public void setRotationType(java.lang.String value) {
		this.setValue(ROTATION_TYPE, value);
	}

	//
	public java.lang.String getRotationType() {
		return (java.lang.String)this.getValue(ROTATION_TYPE);
	}

	// This attribute is optional
	public void setRotationTypeJ2eeId(java.lang.String value) {
		// Make sure we've got a place to put this attribute.
		if (size(LOG_FILENAME) == 0) {
			setValue(LOG_FILENAME, "");
		}
		setAttributeValue(LOG_FILENAME, "J2eeId", value);
	}

	//
	public java.lang.String getRotationTypeJ2eeId() {
		// If our element does not exist, then the attribute does not exist.
		if (size(LOG_FILENAME) == 0) {
			return null;
		} else {
			return getAttributeValue(LOG_FILENAME, "J2eeId");
		}
	}

	// This attribute is optional
	public void setNumberOfFilesLimited(boolean value) {
		this.setValue(NUMBER_OF_FILES_LIMITED, (value ? java.lang.Boolean.TRUE : java.lang.Boolean.FALSE));
	}

	//
	public boolean isNumberOfFilesLimited() {
		Boolean ret = (Boolean)this.getValue(NUMBER_OF_FILES_LIMITED);
		if (ret == null)
			ret = (Boolean)Common.defaultScalarValue(Common.TYPE_BOOLEAN);
		return ((java.lang.Boolean)ret).booleanValue();
	}

	// This attribute is optional
	public void setFileCount(long value) {
		this.setValue(FILE_COUNT, java.lang.Long.valueOf(value));
	}

	//
	public long getFileCount() {
		Long ret = (Long)this.getValue(FILE_COUNT);
		if (ret == null)
			throw new RuntimeException(Common.getMessage(
				"NoValueForElt_msg",
				new Object[] {"FILE_COUNT", "long"}));
		return ((java.lang.Long)ret).longValue();
	}

	// This attribute is optional
	public void setFileCountJ2eeId(java.lang.String value) {
		// Make sure we've got a place to put this attribute.
		if (size(FILE_COUNT) == 0) {
			setValue(FILE_COUNT, "");
		}
		setAttributeValue(FILE_COUNT, "J2eeId", value);
	}

	//
	public java.lang.String getFileCountJ2eeId() {
		// If our element does not exist, then the attribute does not exist.
		if (size(FILE_COUNT) == 0) {
			return null;
		} else {
			return getAttributeValue(FILE_COUNT, "J2eeId");
		}
	}

	// This attribute is optional
	public void setFileSizeLimit(long value) {
		this.setValue(FILE_SIZE_LIMIT, java.lang.Long.valueOf(value));
	}

	//
	public long getFileSizeLimit() {
		Long ret = (Long)this.getValue(FILE_SIZE_LIMIT);
		if (ret == null)
			throw new RuntimeException(Common.getMessage(
				"NoValueForElt_msg",
				new Object[] {"FILE_SIZE_LIMIT", "long"}));
		return ((java.lang.Long)ret).longValue();
	}

	// This attribute is optional
	public void setFileSizeLimitJ2eeId(java.lang.String value) {
		// Make sure we've got a place to put this attribute.
		if (size(FILE_COUNT) == 0) {
			setValue(FILE_COUNT, "");
		}
		setAttributeValue(FILE_COUNT, "J2eeId", value);
	}

	//
	public java.lang.String getFileSizeLimitJ2eeId() {
		// If our element does not exist, then the attribute does not exist.
		if (size(FILE_COUNT) == 0) {
			return null;
		} else {
			return getAttributeValue(FILE_COUNT, "J2eeId");
		}
	}

	// This attribute is optional
	public void setRotateLogOnStartup(boolean value) {
		this.setValue(ROTATE_LOG_ON_STARTUP, (value ? java.lang.Boolean.TRUE : java.lang.Boolean.FALSE));
	}

	//
	public boolean isRotateLogOnStartup() {
		Boolean ret = (Boolean)this.getValue(ROTATE_LOG_ON_STARTUP);
		if (ret == null)
			ret = (Boolean)Common.defaultScalarValue(Common.TYPE_BOOLEAN);
		return ((java.lang.Boolean)ret).booleanValue();
	}

	// This attribute is optional
	public void setLogFileRotationDir(java.lang.String value) {
		this.setValue(LOG_FILE_ROTATION_DIR, value);
	}

	//
	public java.lang.String getLogFileRotationDir() {
		return (java.lang.String)this.getValue(LOG_FILE_ROTATION_DIR);
	}

	// This attribute is optional
	public void setLogFileRotationDirJ2eeId(java.lang.String value) {
		// Make sure we've got a place to put this attribute.
		if (size(LOG_FILENAME) == 0) {
			setValue(LOG_FILENAME, "");
		}
		setAttributeValue(LOG_FILENAME, "J2eeId", value);
	}

	//
	public java.lang.String getLogFileRotationDirJ2eeId() {
		// If our element does not exist, then the attribute does not exist.
		if (size(LOG_FILENAME) == 0) {
			return null;
		} else {
			return getAttributeValue(LOG_FILENAME, "J2eeId");
		}
	}

	// This attribute is optional
	public void setRotationTime(java.lang.String value) {
		this.setValue(ROTATION_TIME, value);
	}

	//
	public java.lang.String getRotationTime() {
		return (java.lang.String)this.getValue(ROTATION_TIME);
	}

	// This attribute is optional
	public void setRotationTimeJ2eeId(java.lang.String value) {
		// Make sure we've got a place to put this attribute.
		if (size(LOG_FILENAME) == 0) {
			setValue(LOG_FILENAME, "");
		}
		setAttributeValue(LOG_FILENAME, "J2eeId", value);
	}

	//
	public java.lang.String getRotationTimeJ2eeId() {
		// If our element does not exist, then the attribute does not exist.
		if (size(LOG_FILENAME) == 0) {
			return null;
		} else {
			return getAttributeValue(LOG_FILENAME, "J2eeId");
		}
	}

	// This attribute is optional
	public void setFileTimeSpan(long value) {
		this.setValue(FILE_TIME_SPAN, java.lang.Long.valueOf(value));
	}

	//
	public long getFileTimeSpan() {
		Long ret = (Long)this.getValue(FILE_TIME_SPAN);
		if (ret == null)
			throw new RuntimeException(Common.getMessage(
				"NoValueForElt_msg",
				new Object[] {"FILE_TIME_SPAN", "long"}));
		return ((java.lang.Long)ret).longValue();
	}

	// This attribute is optional
	public void setFileTimeSpanJ2eeId(java.lang.String value) {
		// Make sure we've got a place to put this attribute.
		if (size(FILE_COUNT) == 0) {
			setValue(FILE_COUNT, "");
		}
		setAttributeValue(FILE_COUNT, "J2eeId", value);
	}

	//
	public java.lang.String getFileTimeSpanJ2eeId() {
		// If our element does not exist, then the attribute does not exist.
		if (size(FILE_COUNT) == 0) {
			return null;
		} else {
			return getAttributeValue(FILE_COUNT, "J2eeId");
		}
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
		// Validating property id
		if (getId() != null) {
			// has whitespace restriction
			if (restrictionFailure) {
				throw new org.netbeans.modules.schema2beans.ValidateException("getId() whiteSpace (collapse)", org.netbeans.modules.schema2beans.ValidateException.FailureType.DATA_RESTRICTION, "id", this);	// NOI18N
			}
		}
		// Validating property logFilename
		if (getLogFilename() != null) {
			// has whitespace restriction
			if (restrictionFailure) {
				throw new org.netbeans.modules.schema2beans.ValidateException("getLogFilename() whiteSpace (collapse)", org.netbeans.modules.schema2beans.ValidateException.FailureType.DATA_RESTRICTION, "logFilename", this);	// NOI18N
			}
		}
		// Validating property logFilenameJ2eeId
		if (getLogFilenameJ2eeId() != null) {
			// has whitespace restriction
			if (restrictionFailure) {
				throw new org.netbeans.modules.schema2beans.ValidateException("getLogFilenameJ2eeId() whiteSpace (collapse)", org.netbeans.modules.schema2beans.ValidateException.FailureType.DATA_RESTRICTION, "logFilenameJ2eeId", this);	// NOI18N
			}
		}
		// Validating property loggingEnabled
		{
			boolean patternPassed = false;
			if ((isLoggingEnabled() ? "true" : "false").matches("(true|True|TRUE|false|False|FALSE|yes|Yes|YES|Y|no|No|NO|N|1|0)")) {
				patternPassed = true;
			}
			restrictionFailure = !patternPassed;
		}
		if (restrictionFailure) {
			throw new org.netbeans.modules.schema2beans.ValidateException("isLoggingEnabled()", org.netbeans.modules.schema2beans.ValidateException.FailureType.DATA_RESTRICTION, "loggingEnabled", this);	// NOI18N
		}
		// Validating property rotationType
		if (getRotationType() != null) {
			// has whitespace restriction
			if (restrictionFailure) {
				throw new org.netbeans.modules.schema2beans.ValidateException("getRotationType() whiteSpace (collapse)", org.netbeans.modules.schema2beans.ValidateException.FailureType.DATA_RESTRICTION, "rotationType", this);	// NOI18N
			}
		}
		// Validating property rotationTypeJ2eeId
		if (getRotationTypeJ2eeId() != null) {
			// has whitespace restriction
			if (restrictionFailure) {
				throw new org.netbeans.modules.schema2beans.ValidateException("getRotationTypeJ2eeId() whiteSpace (collapse)", org.netbeans.modules.schema2beans.ValidateException.FailureType.DATA_RESTRICTION, "rotationTypeJ2eeId", this);	// NOI18N
			}
		}
		// Validating property numberOfFilesLimited
		{
			boolean patternPassed = false;
			if ((isNumberOfFilesLimited() ? "true" : "false").matches("(true|True|TRUE|false|False|FALSE|yes|Yes|YES|Y|no|No|NO|N|1|0)")) {
				patternPassed = true;
			}
			restrictionFailure = !patternPassed;
		}
		if (restrictionFailure) {
			throw new org.netbeans.modules.schema2beans.ValidateException("isNumberOfFilesLimited()", org.netbeans.modules.schema2beans.ValidateException.FailureType.DATA_RESTRICTION, "numberOfFilesLimited", this);	// NOI18N
		}
		// Validating property fileCount
		if (getFileCount() - 0L <= 0) {
			restrictionFailure = true;
		}
		if (restrictionFailure) {
			throw new org.netbeans.modules.schema2beans.ValidateException("getFileCount() minExclusive (0)", org.netbeans.modules.schema2beans.ValidateException.FailureType.DATA_RESTRICTION, "fileCount", this);	// NOI18N
		}
		// Validating property fileCountJ2eeId
		if (getFileCountJ2eeId() != null) {
			// has whitespace restriction
			if (restrictionFailure) {
				throw new org.netbeans.modules.schema2beans.ValidateException("getFileCountJ2eeId() whiteSpace (collapse)", org.netbeans.modules.schema2beans.ValidateException.FailureType.DATA_RESTRICTION, "fileCountJ2eeId", this);	// NOI18N
			}
		}
		// Validating property fileSizeLimit
		if (getFileSizeLimit() - 0L <= 0) {
			restrictionFailure = true;
		}
		if (restrictionFailure) {
			throw new org.netbeans.modules.schema2beans.ValidateException("getFileSizeLimit() minExclusive (0)", org.netbeans.modules.schema2beans.ValidateException.FailureType.DATA_RESTRICTION, "fileSizeLimit", this);	// NOI18N
		}
		// Validating property fileSizeLimitJ2eeId
		if (getFileSizeLimitJ2eeId() != null) {
			// has whitespace restriction
			if (restrictionFailure) {
				throw new org.netbeans.modules.schema2beans.ValidateException("getFileSizeLimitJ2eeId() whiteSpace (collapse)", org.netbeans.modules.schema2beans.ValidateException.FailureType.DATA_RESTRICTION, "fileSizeLimitJ2eeId", this);	// NOI18N
			}
		}
		// Validating property rotateLogOnStartup
		{
			boolean patternPassed = false;
			if ((isRotateLogOnStartup() ? "true" : "false").matches("(true|True|TRUE|false|False|FALSE|yes|Yes|YES|Y|no|No|NO|N|1|0)")) {
				patternPassed = true;
			}
			restrictionFailure = !patternPassed;
		}
		if (restrictionFailure) {
			throw new org.netbeans.modules.schema2beans.ValidateException("isRotateLogOnStartup()", org.netbeans.modules.schema2beans.ValidateException.FailureType.DATA_RESTRICTION, "rotateLogOnStartup", this);	// NOI18N
		}
		// Validating property logFileRotationDir
		if (getLogFileRotationDir() != null) {
			// has whitespace restriction
			if (restrictionFailure) {
				throw new org.netbeans.modules.schema2beans.ValidateException("getLogFileRotationDir() whiteSpace (collapse)", org.netbeans.modules.schema2beans.ValidateException.FailureType.DATA_RESTRICTION, "logFileRotationDir", this);	// NOI18N
			}
		}
		// Validating property logFileRotationDirJ2eeId
		if (getLogFileRotationDirJ2eeId() != null) {
			// has whitespace restriction
			if (restrictionFailure) {
				throw new org.netbeans.modules.schema2beans.ValidateException("getLogFileRotationDirJ2eeId() whiteSpace (collapse)", org.netbeans.modules.schema2beans.ValidateException.FailureType.DATA_RESTRICTION, "logFileRotationDirJ2eeId", this);	// NOI18N
			}
		}
		// Validating property rotationTime
		if (getRotationTime() != null) {
			// has whitespace restriction
			if (restrictionFailure) {
				throw new org.netbeans.modules.schema2beans.ValidateException("getRotationTime() whiteSpace (collapse)", org.netbeans.modules.schema2beans.ValidateException.FailureType.DATA_RESTRICTION, "rotationTime", this);	// NOI18N
			}
		}
		// Validating property rotationTimeJ2eeId
		if (getRotationTimeJ2eeId() != null) {
			// has whitespace restriction
			if (restrictionFailure) {
				throw new org.netbeans.modules.schema2beans.ValidateException("getRotationTimeJ2eeId() whiteSpace (collapse)", org.netbeans.modules.schema2beans.ValidateException.FailureType.DATA_RESTRICTION, "rotationTimeJ2eeId", this);	// NOI18N
			}
		}
		// Validating property fileTimeSpan
		if (getFileTimeSpan() - 0L <= 0) {
			restrictionFailure = true;
		}
		if (restrictionFailure) {
			throw new org.netbeans.modules.schema2beans.ValidateException("getFileTimeSpan() minExclusive (0)", org.netbeans.modules.schema2beans.ValidateException.FailureType.DATA_RESTRICTION, "fileTimeSpan", this);	// NOI18N
		}
		// Validating property fileTimeSpanJ2eeId
		if (getFileTimeSpanJ2eeId() != null) {
			// has whitespace restriction
			if (restrictionFailure) {
				throw new org.netbeans.modules.schema2beans.ValidateException("getFileTimeSpanJ2eeId() whiteSpace (collapse)", org.netbeans.modules.schema2beans.ValidateException.FailureType.DATA_RESTRICTION, "fileTimeSpanJ2eeId", this);	// NOI18N
			}
		}
	}

	// Dump the content of this bean returning it as a String
	public void dump(StringBuffer str, String indent){
		String s;
		Object o;
		org.netbeans.modules.schema2beans.BaseBean n;
		str.append(indent);
		str.append("LogFilename");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append("<");	// NOI18N
		o = this.getLogFilename();
		str.append((o==null?"null":o.toString().trim()));	// NOI18N
		str.append(">\n");	// NOI18N
		this.dumpAttributes(LOG_FILENAME, 0, str, indent);

		str.append(indent);
		str.append("LoggingEnabled");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append((this.isLoggingEnabled()?"true":"false"));
		this.dumpAttributes(LOGGING_ENABLED, 0, str, indent);

		str.append(indent);
		str.append("RotationType");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append("<");	// NOI18N
		o = this.getRotationType();
		str.append((o==null?"null":o.toString().trim()));	// NOI18N
		str.append(">\n");	// NOI18N
		this.dumpAttributes(ROTATION_TYPE, 0, str, indent);

		str.append(indent);
		str.append("NumberOfFilesLimited");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append((this.isNumberOfFilesLimited()?"true":"false"));
		this.dumpAttributes(NUMBER_OF_FILES_LIMITED, 0, str, indent);

		if (this.getValue(FILE_COUNT) != null) {
			str.append(indent);
			str.append("FileCount");	// NOI18N
			str.append(indent+"\t");	// NOI18N
			str.append("<");	// NOI18N
			s = String.valueOf(this.getFileCount());
			str.append((s.trim()));	// NOI18N
			str.append(">\n");	// NOI18N
			this.dumpAttributes(FILE_COUNT, 0, str, indent);
		}

		if (this.getValue(FILE_SIZE_LIMIT) != null) {
			str.append(indent);
			str.append("FileSizeLimit");	// NOI18N
			str.append(indent+"\t");	// NOI18N
			str.append("<");	// NOI18N
			s = String.valueOf(this.getFileSizeLimit());
			str.append((s.trim()));	// NOI18N
			str.append(">\n");	// NOI18N
			this.dumpAttributes(FILE_SIZE_LIMIT, 0, str, indent);
		}

		str.append(indent);
		str.append("RotateLogOnStartup");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append((this.isRotateLogOnStartup()?"true":"false"));
		this.dumpAttributes(ROTATE_LOG_ON_STARTUP, 0, str, indent);

		str.append(indent);
		str.append("LogFileRotationDir");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append("<");	// NOI18N
		o = this.getLogFileRotationDir();
		str.append((o==null?"null":o.toString().trim()));	// NOI18N
		str.append(">\n");	// NOI18N
		this.dumpAttributes(LOG_FILE_ROTATION_DIR, 0, str, indent);

		str.append(indent);
		str.append("RotationTime");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append("<");	// NOI18N
		o = this.getRotationTime();
		str.append((o==null?"null":o.toString().trim()));	// NOI18N
		str.append(">\n");	// NOI18N
		this.dumpAttributes(ROTATION_TIME, 0, str, indent);

		if (this.getValue(FILE_TIME_SPAN) != null) {
			str.append(indent);
			str.append("FileTimeSpan");	// NOI18N
			str.append(indent+"\t");	// NOI18N
			str.append("<");	// NOI18N
			s = String.valueOf(this.getFileTimeSpan());
			str.append((s.trim()));	// NOI18N
			str.append(">\n");	// NOI18N
			this.dumpAttributes(FILE_TIME_SPAN, 0, str, indent);
		}

	}
	public String dumpBeanNode(){
		StringBuffer str = new StringBuffer();
		str.append("LoggingType\n");	// NOI18N
		this.dump(str, "\n  ");	// NOI18N
		return str.toString();
	}}

// END_NOI18N

