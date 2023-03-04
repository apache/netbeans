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
public interface KeyStore extends ExtensibilityElement{

    public static final String KEYSTORE_TYPE = "JKS";      //NOI18N
    
    public static final String LOCATION = "Location";     //NOI18N
    public static final String ALIAS = "Alias";     //NOI18N
    public static final String TYPE = "Type";     //NOI18N
    public static final String PASSWORD = "StorePassword";     //NOI18N
    public static final String KEYPASSWORD = "KeyPassword";     //NOI18N
    public static final String SELECTOR = "Selector";     //NOI18N
    
    void setVisibility(String vis);
    String getVisibility();

    void setLocation(String location);
    String getLocation();

    void setAlias(String alias);
    String getAlias();

    void setType(String type);
    String getType();

    void setStorePassword(String storepassword);
    String getStorePassword();

    void setKeyPassword(String storepassword);
    String getKeyPassword();

    void setAliasSelector(String aliasSelector);
    String getAliasSelector();
}
