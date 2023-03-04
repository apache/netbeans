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
 * WebserviceDescription.java
 *
 * Created on November 17, 2004, 4:52 PM
 */

package org.netbeans.modules.j2ee.sun.dd.api.common;

/**
 *
 * @author  Nitya Doraisamy
 */
public interface WebserviceDescription extends org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean {

    public static final String WEBSERVICE_DESCRIPTION_NAME = "WebserviceDescriptionName";	// NOI18N
    public static final String WSDL_PUBLISH_LOCATION = "WsdlPublishLocation";	// NOI18N

    /** Setter for webservice-description-name property
     * @param value property value
     */
    public void setWebserviceDescriptionName(java.lang.String value);
    /** Getter for webservice-description-name property.
     * @return property value
     */
    public java.lang.String getWebserviceDescriptionName();
    /** Setter for wsdl-publish-location property
     * @param value property value
     */
    public void setWsdlPublishLocation(java.lang.String value);
    /** Getter for wsdl-publish-location property.
     * @return property value
     */
    public java.lang.String getWsdlPublishLocation();
}
