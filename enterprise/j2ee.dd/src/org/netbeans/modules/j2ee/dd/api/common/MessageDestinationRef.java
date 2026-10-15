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

package org.netbeans.modules.j2ee.dd.api.common;

/**
 * Generated interface for MessageDestinationRef element.
 *
 *<p><b><span style="color:red"><em>Important note: Do not provide an implementation of this interface unless you are a DD API provider!</em></span></b>
 *</p>
 */
public interface MessageDestinationRef extends CommonDDBean, DescriptionInterface {

    public static final String MESSAGE_DESTINATION_REF_NAME = "MessageDestinationRefName";	// NOI18N
    public static final String MESSAGE_DESTINATION_TYPE = "MessageDestinationType";	// NOI18N
    public static final String MESSAGE_DESTINATION_USAGE = "MessageDestinationUsage";	// NOI18N
    public static final String MESSAGE_DESTINATION_LINK = "MessageDestinationLink";	// NOI18N
    public static final String MESSAGE_DESTINATION_USAGE_CONSUMES = "Consumes";	// NOI18N
    public static final String MESSAGE_DESTINATION_USAGE_PRODUCES = "Produces";	// NOI18N
    public static final String MESSAGE_DESTINATION_USAGE_CONSUMESPRODUCES = "ConsumesProduces";	// NOI18N
    
    /** Setter for message-destination-ref-name property.
     * @param value property value
     */
    public void setMessageDestinationRefName(String value);
    /** Getter for message-destination-ref-name property.
     * @return property value 
     */
    public String getMessageDestinationRefName();
    /** Setter for message-destination-type property.
     * @param value property value
     */
    public void setMessageDestinationType(String value);
    /** Getter for message-destination-type property.
     * @return property value 
     */
    public String getMessageDestinationType();
    /** Setter for message-destination-usage property.
     * @param value property value
     */
    public void setMessageDestinationUsage(String value);
    /** Getter for message-destination-usage property.
     * @return property value 
     */
    public String getMessageDestinationUsage();
    /** Setter for message-destination-link property.
     * @param value property value
     */
    public void setMessageDestinationLink(String value);
    /** Getter for message-destination-link property.
     * @return property value 
     */
    public String getMessageDestinationLink();

    // Java EE 5
    
    void setMappedName(String value) throws VersionNotSupportedException;
    String getMappedName() throws VersionNotSupportedException;
    void setInjectionTarget(int index, InjectionTarget valueInterface) throws VersionNotSupportedException;
    InjectionTarget getInjectionTarget(int index) throws VersionNotSupportedException;
    int sizeInjectionTarget() throws VersionNotSupportedException;
    void setInjectionTarget(InjectionTarget[] value) throws VersionNotSupportedException;
    InjectionTarget[] getInjectionTarget() throws VersionNotSupportedException;
    int addInjectionTarget(InjectionTarget valueInterface) throws VersionNotSupportedException;
    int removeInjectionTarget(InjectionTarget valueInterface) throws VersionNotSupportedException;
    InjectionTarget newInjectionTarget() throws VersionNotSupportedException;

}
