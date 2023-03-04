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
 * MessageSecurity.java
 *
 * Created on November 18, 2004, 4:21 PM
 */

package org.netbeans.modules.j2ee.sun.dd.api.common;

/**
 *
 * @author Nitya Doraisamy
 */
public interface MessageSecurity extends org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean {

    public static final String MESSAGE = "Message";	// NOI18N
    public static final String REQUEST_PROTECTION = "RequestProtection";	// NOI18N
    public static final String REQUESTPROTECTIONAUTHSOURCE = "RequestProtectionAuthSource";	// NOI18N
    public static final String REQUESTPROTECTIONAUTHRECIPIENT = "RequestProtectionAuthRecipient";	// NOI18N
    public static final String RESPONSE_PROTECTION = "ResponseProtection";	// NOI18N
    public static final String RESPONSEPROTECTIONAUTHSOURCE = "ResponseProtectionAuthSource";	// NOI18N
    public static final String RESPONSEPROTECTIONAUTHRECIPIENT = "ResponseProtectionAuthRecipient";	// NOI18N

        
    public Message [] getMessage();
    public Message  getMessage(int index);
    public void setMessage(Message [] value);
    public void setMessage(int index, Message  value);
    public int addMessage(Message  value);
    public int removeMessage(Message  value);
    public int sizeMessage();
    public Message  newMessage();
    
    /** Setter for request-protection property
     * @param value property value
     */
    public void setRequestProtection(boolean value);
    /** Check for request-protection property.
     * @return property value
     */
    public boolean isRequestProtection();
    /** Setter for response-protection property
     * @param value property value
     */
    public void setResponseProtection(boolean value);
    /** Getter for response-protection property.
     * @return property value
     */
    public boolean isResponseProtection();
    
    public void setRequestProtectionAuthSource(java.lang.String value);
    public java.lang.String getRequestProtectionAuthSource();
    
    public void setRequestProtectionAuthRecipient(java.lang.String value);
    public java.lang.String getRequestProtectionAuthRecipient();
    
    public void setResponseProtectionAuthSource(java.lang.String value);
    public java.lang.String getResponseProtectionAuthSource();
    
    public void setResponseProtectionAuthRecipient(java.lang.String value);
    public java.lang.String getResponseProtectionAuthRecipient();
    
    
}
