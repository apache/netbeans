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
package org.netbeans.modules.xml.jaxb.model.events;

import org.netbeans.modules.xml.jaxb.api.model.events.JAXBWizEvent;
import java.util.EventObject;

/**
 *
 * @author gpatil
 */
public class JAXBWizEventImpl extends EventObject implements JAXBWizEvent {
    protected Object oldVal;
    protected Object newVal;
    protected JAXBWizEvent.JAXBWizEventType eventType;

    public JAXBWizEventImpl(Object source) {
        super(source);
    }

    public JAXBWizEventImpl(Object source, Object ov, Object nv, 
            JAXBWizEvent.JAXBWizEventType et) {
        super(source);
        this.oldVal = ov;
        this.newVal = nv;
        this.eventType = et;
    }

    public JAXBWizEventType getEventType() {
        return this.eventType;
    }

    public Object getOldValue() {
        return this.oldVal;
    }

    public Object getNewValue() {
        return this.newVal;
    }
}
