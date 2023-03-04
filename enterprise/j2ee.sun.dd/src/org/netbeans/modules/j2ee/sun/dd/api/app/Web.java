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
 * SunApplication.java
 *
 * Created on November 21, 2004, 12:47 AM
 */


package org.netbeans.modules.j2ee.sun.dd.api.app;

public interface Web extends org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean {

        public static final String WEB_URI = "WebUri";	// NOI18N
	public static final String CONTEXT_ROOT = "ContextRoot";	// NOI18N

        /** Setter for web-uri property
        * @param value property value
        */
	public void setWebUri(String value);
        /** Getter for web-uri property.
        * @return property value
        */
	public String getWebUri();
        /** Setter for context-root property
        * @param value property value
        */
	public void setContextRoot(String value);
        /** Getter for context-root property.
        * @return property value
        */
	public String getContextRoot();

}
