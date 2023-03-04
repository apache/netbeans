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

package org.netbeans.modules.j2ee.dd.api.common;
/**
 * Generated interface for ServiceRefHandler element.
 *
 *<p><b><font color="red"><em>Important note: Do not provide an implementation of this interface unless you are a DD API provider!</em></font></b>
 *</p>
 */
public interface ServiceRefHandler extends ComponentInterface {

    public static final String HANDLER_NAME = "HandlerName";	// NOI18N
    public static final String HANDLER_CLASS = "HandlerClass";	// NOI18N
    public static final String INIT_PARAM = "InitParam";	// NOI18N
    public static final String SOAP_HEADER = "SoapHeader";	// NOI18N
    public static final String SOAP_ROLE = "SoapRole";	// NOI18N
    public static final String PORT_NAME = "PortName";	// NOI18N
    /** Setter for handler-name property.
     * @param value property value
     */
    public void setHandlerName(java.lang.String value);
    /** Getter for handler-name property.
     * @return property value 
     */
    public java.lang.String getHandlerName();
    /** Setter for handler-class property.
     * @param value property value
     */
    public void setHandlerClass(java.lang.String value);
    /** Getter for handler-class property.
     * @return property value 
     */
    public java.lang.String getHandlerClass();
    /** Setter for init-param element.
     * @param index position in the array of elements
     * @param valueInterface init-param element (InitParam object)
     */
    public void setInitParam(int index, InitParam valueInterface);
    /** Getter for init-param element.
     * @param index position in the array of elements
     * @return init-param element (InitParam object)
     */
    public InitParam getInitParam(int index);
    /** Setter for init-param elements.
     * @param value array of init-param elements (InitParam objects)
     */
    public void setInitParam(InitParam[] value);
    /** Getter for init-param elements.
     * @return array of init-param elements (InitParam objects)
     */
    public InitParam[] getInitParam();
    /** Returns number of init-param elements.
     * @return number of init-param elements 
     */
    public int sizeInitParam();
    /** Adds init-param element.
     * @param valueInterface init-param element (InitParam object)
     * @return index of new init-param
     */
    public int addInitParam(InitParam valueInterface);
    /** Removes init-param element.
     * @param valueInterface init-param element (InitParam object)
     * @return index of the removed init-param
     */
    public int removeInitParam(InitParam valueInterface);
    /** Setter for soap-header element.
     * @param index position in the array of elements
     * @param value soap-header element
     */
    public void setSoapHeader(int index, java.lang.String value);
    /** Getter for soap-header element.
     * @param index position in the array of elements
     * @return soap-header element
     */
    public java.lang.String getSoapHeader(int index);
    /** Setter for soap-header elements.
     * @param value array of soap-header elements
     */
    public void setSoapHeader(java.lang.String[] value);
    /** Getter for soap-header elements.
     * @return array of soap-header elements
     */
    public java.lang.String[] getSoapHeader();
    /** Returns number of soap-header elements.
     * @return number of soap-header elements 
     */
    public int sizeSoapHeader();
    /** Adds soap-header element.
     * @param value soap-header element
     * @return index of new soap-header
     */
    public int addSoapHeader(java.lang.String value);
    /** Removes soap-header element.
     * @param value soap-header element
     * @return index of the removed soap-header
     */
    public int removeSoapHeader(java.lang.String value);
    /** Setter for soap-role element.
     * @param index position in the array of elements
     * @param value soap-role element
     */
    public void setSoapRole(int index, java.lang.String value);
    /** Getter for soap-role element.
     * @param index position in the array of elements
     * @return soap-role element
     */
    public java.lang.String getSoapRole(int index);
    /** Setter for soap-role elements.
     * @param value array of soap-role elements
     */
    public void setSoapRole(java.lang.String[] value);
    /** Getter for soap-role elements.
     * @return array of soap-role elements
     */
    public java.lang.String[] getSoapRole();
    /** Returns number of soap-role elements.
     * @return number of soap-role elements 
     */
    public int sizeSoapRole();
    /** Adds soap-role element.
     * @param value soap-role element
     * @return index of new soap-role
     */
    public int addSoapRole(java.lang.String value);
    /** Removes soap-role element.
     * @param value soap-role element
     * @return index of the removed soap-role
     */
    public int removeSoapRole(java.lang.String value);
    /** Setter for port-name element.
     * @param index position in the array of elements
     * @param value port-name element
     */
    public void setPortName(int index, java.lang.String value);
    /** Getter for port-name element.
     * @param index position in the array of elements
     * @return port-name element
     */
    public java.lang.String getPortName(int index);
    /** Setter for port-name elements.
     * @param value array of port-name elements
     */
    public void setPortName(java.lang.String[] value);
    /** Getter for port-name elements.
     * @return array of port-name elements
     */
    public java.lang.String[] getPortName();
    /** Returns number of port-name elements.
     * @return number of port-name elements 
     */
    public int sizePortName();
    /** Adds port-name element.
     * @param value port-name element
     * @return index of new port-name
     */
    public int addPortName(java.lang.String value);
    /** Removes port-name element.
     * @param value port-name element
     * @return index of the removed port-name
     */
    public int removePortName(java.lang.String value);

}
