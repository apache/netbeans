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
 * This interface has all of the bean info accessor methods.
 *
 * @Generated
 */

package org.netbeans.modules.j2ee.dd.api.application;

import org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException;

public interface Application extends org.netbeans.modules.j2ee.dd.api.common.RootInterface {
	
        public static final String MODULE = "Module";	// NOI18N
	public static final String SECURITY_ROLE = "SecurityRole";	// NOI18N

        public static final String PROPERTY_VERSION="dd_version"; //NOI18N
        public static final String VERSION_1_4="1.4"; //NOI18N
        public static final String VERSION_5="5"; //NOI18N
        public static final String VERSION_6="6"; //NOI18N

        /**
         * application.xml DD version for Java EE 7
         * @since 1.29
         */
        public static final String VERSION_7 = "7"; //NOI18N
        /**
         * application.xml DD version for Java EE 8/Jakarta EE 8
         * @since 2
         */
        public static final String VERSION_8 = "8"; //NOI18N
        /**
         * application.xml DD version for Jakarta EE 9/Jakarta EE 9.1
         * @since 2
         */
        public static final String VERSION_9 = "9"; //NOI18N
        /**
         * application.xml DD version for Jakarta EE 10
         */
        public static final String VERSION_10 = "10"; //NOI18N
        /**
         * application.xml DD version for Jakarta EE 11
         */
        public static final String VERSION_11 = "11"; //NOI18N
        public static final int STATE_VALID=0;
        public static final int STATE_INVALID_PARSABLE=1;
        public static final int STATE_INVALID_UNPARSABLE=2;
        public static final String PROPERTY_STATUS="dd_status"; //NOI18N
    
        //public void setVersion(java.lang.String value);
        /** Getter for version property.
         * @return property value
         */
        public java.math.BigDecimal getVersion();
        /** Getter for SAX Parse Error property.
         * Used when deployment descriptor is in invalid state.
         * @return property value or null if in valid state
         */
        public org.xml.sax.SAXParseException getError();
        /** Getter for status property.
         * @return property value
         */
        public int getStatus();
    
	public void setModule(int index, Module value);

	public Module getModule(int index);

	public int sizeModule();

	public void setModule(Module[] value);

	public Module[] getModule();

	public int addModule(org.netbeans.modules.j2ee.dd.api.application.Module value);

	public int removeModule(org.netbeans.modules.j2ee.dd.api.application.Module value);

	public Module newModule();

	public void setSecurityRole(int index, org.netbeans.modules.j2ee.dd.api.common.SecurityRole value);

	public org.netbeans.modules.j2ee.dd.api.common.SecurityRole getSecurityRole(int index);

	public int sizeSecurityRole();

	public void setSecurityRole(org.netbeans.modules.j2ee.dd.api.common.SecurityRole[] value);

	public org.netbeans.modules.j2ee.dd.api.common.SecurityRole[] getSecurityRole();

	public int addSecurityRole(org.netbeans.modules.j2ee.dd.api.common.SecurityRole value);

	public int removeSecurityRole(org.netbeans.modules.j2ee.dd.api.common.SecurityRole value);

	public org.netbeans.modules.j2ee.dd.api.common.SecurityRole newSecurityRole();

        
        //1.4
        public void setIcon(int index, org.netbeans.modules.j2ee.dd.api.common.Icon value) throws VersionNotSupportedException;

	public org.netbeans.modules.j2ee.dd.api.common.Icon getIcon(int index) throws VersionNotSupportedException;

	public int sizeIcon() throws VersionNotSupportedException;

	public void setIcon(org.netbeans.modules.j2ee.dd.api.common.Icon[] value) throws VersionNotSupportedException;

	//public org.netbeans.modules.j2ee.dd.api.common.Icon[] getIcon() throws VersionNotSupportedException;

	public int addIcon(org.netbeans.modules.j2ee.dd.api.common.Icon value)  throws VersionNotSupportedException;

	public int removeIcon(org.netbeans.modules.j2ee.dd.api.common.Icon value) throws VersionNotSupportedException;

	public org.netbeans.modules.j2ee.dd.api.common.Icon newIcon(); 

}

