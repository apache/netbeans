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
/*
 * Servlet.java
 *
 * Created on November 15, 2004, 4:26 PM
 */

package org.netbeans.modules.j2ee.sun.dd.api.web;

import org.netbeans.modules.j2ee.sun.dd.api.common.WebserviceEndpoint;

public interface Servlet extends org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean {
        
    public static final String SERVLET_NAME = "ServletName";	// NOI18N
	public static final String PRINCIPAL_NAME = "PrincipalName";	// NOI18N
	static public final String PRINCIPALNAMECLASSNAME = "PrincipalNameClassName";	// NOI18N
	public static final String WEBSERVICE_ENDPOINT = "WebserviceEndpoint";	// NOI18N

	public void setServletName(String value);
	public String getServletName();
	public void setPrincipalName(String value);
	public String getPrincipalName();
	public void setPrincipalNameClassName(String value) throws org.netbeans.modules.j2ee.sun.dd.api.VersionNotSupportedException;
	public String getPrincipalNameClassName() throws org.netbeans.modules.j2ee.sun.dd.api.VersionNotSupportedException;
    
	public void setWebserviceEndpoint(int index, WebserviceEndpoint value);
	public WebserviceEndpoint getWebserviceEndpoint(int index);
	public int sizeWebserviceEndpoint();
	public void setWebserviceEndpoint(WebserviceEndpoint[] value);
	public WebserviceEndpoint[] getWebserviceEndpoint();
	public int addWebserviceEndpoint(WebserviceEndpoint value);
	public int removeWebserviceEndpoint(WebserviceEndpoint value);
	public WebserviceEndpoint newWebserviceEndpoint();

}
