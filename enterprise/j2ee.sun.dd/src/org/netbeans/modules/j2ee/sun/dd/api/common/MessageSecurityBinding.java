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
 * MessageSecurityBinding.java
 *
 * Created on November 18, 2004, 4:13 PM
 */

package org.netbeans.modules.j2ee.sun.dd.api.common;

/**
 *
 * @author Nitya Doraisamy
 */
public interface MessageSecurityBinding extends org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean {
    public static final String AUTHLAYER = "AuthLayer";	// NOI18N
    public static final String PROVIDERID = "ProviderId";	// NOI18N
    public static final String MESSAGE_SECURITY = "MessageSecurity";	// NOI18N

    public MessageSecurity [] getMessageSecurity ();
    public MessageSecurity  getMessageSecurity (int index);
    public void setMessageSecurity (MessageSecurity [] value);
    public void setMessageSecurity (int index, MessageSecurity  value);
    public int addMessageSecurity (MessageSecurity  value);
    public int removeMessageSecurity (MessageSecurity  value);
    public int sizeMessageSecurity ();
    public MessageSecurity  newMessageSecurity ();
    
    /** Setter for auth-layer attribute
     * @param value attribute value
     */
    public void setAuthLayer(java.lang.String value);
    /** Getter for auth-layer attribute.
     * @return attribute value
     */
    public java.lang.String getAuthLayer();
    /** Setter for provider-id attribute
     * @param value attribute value
     */
    public void setProviderId(java.lang.String value);
    /** Getter for provider-id attribute.
     * @return attribute value
     */
    public java.lang.String getProviderId();

}
