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

package org.netbeans.modules.websvc.wsitmodelext.security.proprietary;

import org.netbeans.modules.xml.wsdl.model.ExtensibilityElement;

/**
 *
 * @author Martin Grebac
 */
public interface PreconfiguredSTS extends ExtensibilityElement{

    public static final String ENDPOINT = "endpoint";  //NOI18N
    public static final String METADATA = "metadata";  //NOI18N
    public static final String WSDLLOCATION = "wsdlLocation";  //NOI18N
    public static final String SERVICENAME = "serviceName";  //NOI18N
    public static final String PORTNAME = "portName";  //NOI18N
    public static final String NAMESPACE = "namespace";  //NOI18N
    public static final String WSTVERSION = "wstversion";  //NOI18N
    public static final String SHARE_TOKEN = "shareToken";  //NOI18N

    void setVisibility(String vis);
    String getVisibility();

    void setEndpoint(String url);
    String getEndpoint();
    
    void setMetadata(String url);
    String getMetadata();

    void setWsdlLocation(String url);
    String getWsdlLocation();

    void setServiceName(String sname);
    String getServiceName();

    void setPortName(String pname);
    String getPortName();

    void setNamespace(String ns);
    String getNamespace();

    void setTrustVersion(String trustVersion);
    String getTrustVersion();

    void setShareToken(boolean shareToken);
    boolean isShareToken();
}
