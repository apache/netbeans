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
public interface CallbackHandler extends ExtensibilityElement{
    public static final String NAME = "name";               //NOI18N
    public static final String CLASSNAME = "classname";     //NOI18N
    public static final String DEFAULT = "default";         //NOI18N
    
    public static final String USERNAME_CBHANDLER = "usernameHandler";         //NOI18N
    public static final String KERBEROS_CBHANDLER = "kerberosHandler";         //NOI18N
    public static final String SAML_CBHANDLER = "samlHandler";         //NOI18N
    public static final String PASSWORD_CBHANDLER = "passwordHandler";         //NOI18N

    void setDefault(String def);
    String getDefault();
    
    void setName(String name);
    String getName();

    void setClassname(String classname);
    String getClassname();
}
