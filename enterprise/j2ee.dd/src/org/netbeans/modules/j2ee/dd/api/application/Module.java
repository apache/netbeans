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

/**
 * This interface has all of the bean info accessor methods.
 *
 * @Generated
 */

package org.netbeans.modules.j2ee.dd.api.application;

import org.netbeans.modules.j2ee.dd.api.common.CommonDDBean;

public interface Module extends CommonDDBean {
	public static final String CONNECTOR = "Connector";	// NOI18N
	public static final String EJB = "Ejb";	// NOI18N
	public static final String JAVA = "Java";	// NOI18N
	public static final String WEB = "Web";	// NOI18N
	public static final String ALT_DD = "AltDd";	// NOI18N
        
	public void setConnector(String value);

	public String getConnector();

	public void setEjb(String value);

	public String getEjb();

	public void setJava(String value);

	public String getJava();

	public void setWeb(Web value);

	public Web getWeb();

	public Web newWeb();

	public void setAltDd(String value);

	public String getAltDd();

}
