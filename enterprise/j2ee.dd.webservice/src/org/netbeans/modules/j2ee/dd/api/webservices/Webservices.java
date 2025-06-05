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
/*
 * This interface has all of the bean info accessor methods.
 *
 * @Generated
 */

package org.netbeans.modules.j2ee.dd.api.webservices;

public interface Webservices extends org.netbeans.modules.j2ee.dd.api.common.RootInterface {
        public static final String PROPERTY_VERSION="dd_version"; //NOI18N
        public static final String VERSION_1_1="1.1"; //NOI18N
        public static final String VERSION_1_2="1.2"; //NOI18N
        public static final int STATE_VALID=0;
        public static final int STATE_INVALID_PARSABLE=1;
        public static final int STATE_INVALID_UNPARSABLE=2;
        public static final String PROPERTY_STATUS="dd_status"; //NOI18N
        
        public static final String VERSION = "Version";	// NOI18N
        public static final String WEBSERVICE_DESCRIPTION = "WebserviceDescription";	// NOI18N
        /** Getter for SAX Parse Error property. 
         * Used when deployment descriptor is in invalid state.
         * @return property value or null if in valid state
         */        
	public org.xml.sax.SAXParseException getError();      
        /** Getter for status property.
         * @return property value
         */        
	public int getStatus();      
        
        //public void setVersion(java.math.BigDecimal value);

	public java.math.BigDecimal getVersion();

	public void setWebserviceDescription(int index, WebserviceDescription value);

	public WebserviceDescription getWebserviceDescription(int index);

	public int sizeWebserviceDescription();

	public void setWebserviceDescription(WebserviceDescription[] value);

	public WebserviceDescription[] getWebserviceDescription();

	public int addWebserviceDescription(org.netbeans.modules.j2ee.dd.api.webservices.WebserviceDescription value);

	public int removeWebserviceDescription(org.netbeans.modules.j2ee.dd.api.webservices.WebserviceDescription value);

	public WebserviceDescription newWebserviceDescription();

}
