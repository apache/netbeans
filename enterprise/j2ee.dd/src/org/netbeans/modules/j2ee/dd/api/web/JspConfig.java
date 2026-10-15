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

package org.netbeans.modules.j2ee.dd.api.web;
import org.netbeans.modules.j2ee.dd.api.common.*;
/**
 * Generated interface for JspConfig element.
 *
 *<p><b><span style="color:red"><em>Important note: Do not provide an implementation of this interface unless you are a DD API provider!</em></span></b>
 *</p>
 */
public interface JspConfig extends CommonDDBean, FindCapability, CreateCapability {
        /** Setter for taglib element.
         * @param index position in the array of elements
         * @param valueInterface taglib element (Taglib object)
         */
	public void setTaglib(int index, org.netbeans.modules.j2ee.dd.api.web.Taglib valueInterface);
        /** Getter for taglib element.
         * @param index position in the array of elements
         * @return taglib element (Taglib object)
         */
	public org.netbeans.modules.j2ee.dd.api.web.Taglib getTaglib(int index);
        /** Setter for taglib elements.
         * @param value array of taglib elements (Taglib objects)
         */
	public void setTaglib(org.netbeans.modules.j2ee.dd.api.web.Taglib[] value);
        /** Getter for taglib elements.
         * @return array of taglib elements (Taglib objects)
         */
	public org.netbeans.modules.j2ee.dd.api.web.Taglib[] getTaglib();
        /** Returns number of taglib elements.
         * @return number of taglib elements 
         */
	public int sizeTaglib();
        /** Adds taglib element.
         * @param valueInterface taglib element (Taglib object)
         * @return index of new taglib
         */
	public int addTaglib(org.netbeans.modules.j2ee.dd.api.web.Taglib valueInterface);
        /** Removes taglib element.
         * @param valueInterface taglib element (Taglib object)
         * @return index of the removed taglib
         */
	public int removeTaglib(org.netbeans.modules.j2ee.dd.api.web.Taglib valueInterface);
        /** Setter for jsp-property-group element.
         * @param index position in the array of elements
         * @param valueInterface jsp-property-group element (JspPropertyGroup object)
         */
	public void setJspPropertyGroup(int index, org.netbeans.modules.j2ee.dd.api.web.JspPropertyGroup valueInterface);
        /** Getter for jsp-property-group element.
         * @param index position in the array of elements
         * @return jsp-property-group element (JspPropertyGroup object)
         */
	public org.netbeans.modules.j2ee.dd.api.web.JspPropertyGroup getJspPropertyGroup(int index);
        /** Setter for jsp-property-group elements.
         * @param value array of jsp-property-group elements (JspPropertyGroup objects)
         */
	public void setJspPropertyGroup(org.netbeans.modules.j2ee.dd.api.web.JspPropertyGroup[] value);
        /** Getter for jsp-property-group elements.
         * @return array of jsp-property-group elements (JspPropertyGroup objects)
         */
	public org.netbeans.modules.j2ee.dd.api.web.JspPropertyGroup[] getJspPropertyGroup();
        /** Returns number of jsp-property-group elements.
         * @return number of jsp-property-group elements 
         */
	public int sizeJspPropertyGroup();
        /** Adds jsp-property-group element.
         * @param valueInterface jsp-property-group element (JspPropertyGroup object)
         * @return index of new jsp-property-group
         */
	public int addJspPropertyGroup(org.netbeans.modules.j2ee.dd.api.web.JspPropertyGroup valueInterface);
        /** Removes jsp-property-group element.
         * @param valueInterface jsp-property-group element (JspPropertyGroup object)
         * @return index of the removed jsp-property-group
         */
	public int removeJspPropertyGroup(org.netbeans.modules.j2ee.dd.api.web.JspPropertyGroup valueInterface);

}
