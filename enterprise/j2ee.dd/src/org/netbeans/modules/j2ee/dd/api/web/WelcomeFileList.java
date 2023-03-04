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
/**
 * Generated interface for WelcomeFileList element.
 *
 *<p><b><font color="red"><em>Important note: Do not provide an implementation of this interface unless you are a DD API provider!</em></font></b>
 *</p>
 */
public interface WelcomeFileList extends org.netbeans.modules.j2ee.dd.api.common.CommonDDBean {
        /** Setter for welcome-file property.
         * @param index position in the array of welcome-files
         * @param value property value
         */
	public void setWelcomeFile(int index, java.lang.String value);
        /** Getter for welcome-file property.
         * @param index position in the array of welcome-files
         * @return property value 
         */
	public java.lang.String getWelcomeFile(int index);
        /** Setter for welcome-file property.
         * @param index position in the array of welcome-files
         * @param value array of welcome-file properties
         */
	public void setWelcomeFile(java.lang.String[] value);
        /** Getter for welcome-file property.
         * @return array of welcome-file properties
         */
	public java.lang.String[] getWelcomeFile();
        /** Returns size of welcome-file properties.
         * @return number of welcome-file properties 
         */
	public int sizeWelcomeFile();
        /** Adds welcome-file property.
         * @param value welcome-file property
         * @return index of new welcome-file
         */
	public int addWelcomeFile(java.lang.String value);
        /** Removes welcome-file property.
         * @param value welcome-file property
         * @return index of the removed welcome-file
         */
	public int removeWelcomeFile(java.lang.String value);

}
