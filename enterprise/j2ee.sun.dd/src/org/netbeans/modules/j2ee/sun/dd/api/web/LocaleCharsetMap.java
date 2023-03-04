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
 * LocaleCharsetMap.java
 *
 * Created on November 15, 2004, 4:26 PM
 */

package org.netbeans.modules.j2ee.sun.dd.api.web;

public interface LocaleCharsetMap extends org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean {

        public static final String LOCALE = "Locale";	// NOI18N
	public static final String AGENT = "Agent";	// NOI18N
	public static final String CHARSET = "Charset";	// NOI18N
	public static final String DESCRIPTION = "Description";	// NOI18N

        /** Setter for locale attribute.
         * @param value attribute value
         */
	public void setLocale(java.lang.String value);
        /** Getter for locale attribute.
         * @return attribute value
         */
	public java.lang.String getLocale();
        /** Setter for agent attribute.
         * @param value attribute value
         */
	public void setAgent(java.lang.String value);
        /** Getter for agent attribute.
         * @return attribute value
         */
	public java.lang.String getAgent();
        /** Setter for charset attribute.
         * @param value attribute value
         */
	public void setCharset(java.lang.String value);
        /** Getter for charset attribute.
         * @return attribute value
         */
	public java.lang.String getCharset();
        /** Setter for description property.
         * @param value property value
         */
	public void setDescription(String value);
        /** Getter for description property.
         * @return property value
         */
	public String getDescription();

}
