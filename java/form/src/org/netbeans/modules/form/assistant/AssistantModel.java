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
package org.netbeans.modules.form.assistant;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

/**
 * Assistant model.
 *
 * @author Jan Stola
 */
public class AssistantModel {
    private PropertyChangeSupport support;
    private String context;
    private String additionalContext;
    private Object[] parameters;

    public AssistantModel() {
        support = new PropertyChangeSupport(this);
    }

    public String getContext() {
        return context;
    }

    public String getAdditionalContext() {
        return additionalContext;
    }

    public Object[] getParameters() {
        return parameters;
    }

    public void setContext(String context) {
        setContext(context, (String)null);
    }

    public void setContext(String context, String additionalContext) {
        this.context = context;
        this.additionalContext = additionalContext;
        this.parameters = null;
        fireContextChange();
    }

    public void setContext(String context, Object[] parameters) {
        this.context = context;
        this.additionalContext = null;
        this.parameters = parameters;
        fireContextChange();
    }

    private void fireContextChange() {
        support.firePropertyChange("context", null, null); // NOI18N
    }

    public String[] getMessages() {
        return AssistantMessages.getDefault().getMessages(context);
    }

    public String[] getAdditionalMessages() {
        return AssistantMessages.getDefault().getMessages(additionalContext);
    }

    // Property change support
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        support.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        support.removePropertyChangeListener(listener);
    }

}
