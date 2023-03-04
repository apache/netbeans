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
package org.netbeans.modules.xml.jaxb.api.model.events;

/**
 * For JAXBWizEventType.EVENT_BINDING_ADDED:
 * Source is Schemas, oldValue is null, new value is newly added Schema.
 *
 * For JAXBWizEventType.EVENT_BINDING_CHANGED:
 * Source is Schemas, oldValue is old Schema object, newValue is new Schema 
 * object.
 * 
 * For JAXBWizEventType.EVENT_BINDING_DELETED:
 * Source is Schemas, oldValue is deleted Schema object, newValue is null.
 * 
 * For JAXBWizEventType.EVENT_CFG_FILE_EDITED:
 * Source is new Schemas, oldValue is null, newValue is null.
 * 
 * @author gpatil
 */
public interface JAXBWizEvent {
    public enum JAXBWizEventType {
        EVENT_BINDING_ADDED, 
        EVENT_BINDING_CHANGED, 
        EVENT_BINDING_DELETED, 
        EVENT_CFG_FILE_EDITED
    } ;

    public JAXBWizEventType getEventType();
    public Object getSource();
    public Object getOldValue();
    public Object getNewValue();
}
