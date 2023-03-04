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
 * LocaleCharsetInfo.java
 *
 * Created on November 15, 2004, 4:26 PM
 */

package org.netbeans.modules.j2ee.sun.dd.api.web;

public interface LocaleCharsetInfo extends org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean {

        public static final String DEFAULTLOCALE = "DefaultLocale";	// NOI18N
	public static final String LOCALE_CHARSET_MAP = "LocaleCharsetMap";	// NOI18N
	public static final String PARAMETER_ENCODING = "ParameterEncoding";	// NOI18N
	public static final String PARAMETERENCODINGFORMHINTFIELD = "ParameterEncodingFormHintField";	// NOI18N
	public static final String PARAMETERENCODINGDEFAULTCHARSET = "ParameterEncodingDefaultCharset";	// NOI18N

        
        /** Setter for default-locale attribute
         * @param value attribute value
         */
	public void setDefaultLocale(java.lang.String value);
        /** Getter for default-locale attribute.
         * @return attribute value
         */
	public java.lang.String getDefaultLocale();

	public void setLocaleCharsetMap(int index, LocaleCharsetMap value);
	public LocaleCharsetMap getLocaleCharsetMap(int index);
	public int sizeLocaleCharsetMap();
	public void setLocaleCharsetMap(LocaleCharsetMap[] value);
	public LocaleCharsetMap[] getLocaleCharsetMap();
	public int addLocaleCharsetMap(LocaleCharsetMap value);
	public int removeLocaleCharsetMap(LocaleCharsetMap value);
	public LocaleCharsetMap newLocaleCharsetMap();

        /** Setter for parameter-encoding property
         * @param value property value
         */
	public void setParameterEncoding(boolean value);
        /** Check for parameter-encoding property.
         * @return property value
         */
	public boolean isParameterEncoding();
        /** Setter for form-hint-field attribute of parameter-encoding
         * @param value attribute value
         */
	public void setParameterEncodingFormHintField(java.lang.String value);
         /** Getter for form-hint-field attribute of parameter-encoding
         * @return attribute value
         */
	public java.lang.String getParameterEncodingFormHintField();
        /** Setter for default-charset attribute of parameter-encoding
         * @param value attribute value
         */
	public void setParameterEncodingDefaultCharset(java.lang.String value);
         /** Getter for default-charset attribute of parameter-encoding
         * @return attribute value
         */
	public java.lang.String getParameterEncodingDefaultCharset();

}
