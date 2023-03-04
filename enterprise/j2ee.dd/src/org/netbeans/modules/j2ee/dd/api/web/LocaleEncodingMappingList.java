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

package org.netbeans.modules.j2ee.dd.api.web;
import org.netbeans.modules.j2ee.dd.api.common.*;
/**
 * Generated interface for LocaleEncodingMappingList element.
 *
 *<p><b><font color="red"><em>Important note: Do not provide an implementation of this interface unless you are a DD API provider!</em></font></b>
 *</p>
 */
public interface LocaleEncodingMappingList extends CommonDDBean, CreateCapability, FindCapability {
        /** Setter for locale-encoding-mapping element.
         * @param index position in the array of elements
         * @param valueInterface locale-encoding-mapping element (LocaleEncodingMapping object)
         */
	public void setLocaleEncodingMapping(int index, org.netbeans.modules.j2ee.dd.api.web.LocaleEncodingMapping valueInterface);
        /** Getter for locale-encoding-mapping element.
         * @param index position in the array of elements
         * @return locale-encoding-mapping element (LocaleEncodingMapping object)
         */
	public org.netbeans.modules.j2ee.dd.api.web.LocaleEncodingMapping getLocaleEncodingMapping(int index);
        /** Setter for locale-encoding-mapping elements.
         * @param value array of locale-encoding-mapping elements (LocaleEncodingMapping objects)
         */
	public void setLocaleEncodingMapping(org.netbeans.modules.j2ee.dd.api.web.LocaleEncodingMapping[] value);
        /** Getter for locale-encoding-mapping elements.
         * @return array of locale-encoding-mapping elements (LocaleEncodingMapping objects)
         */
	public org.netbeans.modules.j2ee.dd.api.web.LocaleEncodingMapping[] getLocaleEncodingMapping();
        /** Returns size of locale-encoding-mapping elements.
         * @return number of locale-encoding-mapping elements 
         */
	public int sizeLocaleEncodingMapping();
        /** Adds locale-encoding-mapping element.
         * @param valueInterface locale-encoding-mapping element (LocaleEncodingMapping object)
         * @return index of new locale-encoding-mapping
         */
	public int addLocaleEncodingMapping(org.netbeans.modules.j2ee.dd.api.web.LocaleEncodingMapping valueInterface);
        /** Removes locale-encoding-mapping element.
         * @param valueInterface locale-encoding-mapping element (LocaleEncodingMapping object)
         * @return index of the removed locale-encoding-mapping
         */
	public int removeLocaleEncodingMapping(org.netbeans.modules.j2ee.dd.api.web.LocaleEncodingMapping valueInterface);

}
