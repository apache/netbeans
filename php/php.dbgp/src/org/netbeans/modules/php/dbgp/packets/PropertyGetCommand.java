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
package org.netbeans.modules.php.dbgp.packets;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

/**
 * @author ads
 *
 */
public class PropertyGetCommand extends PropertyCommand {
    private static final String KEY_ARG = "-k "; // NOI18N
    private final PropertyChangeSupport changeSupport;
    static final String PROPERTY_GET = "property_get"; // NOI18N
    private String myKey;

    public PropertyGetCommand(String transactionId) {
        this(PROPERTY_GET, transactionId);
    }

    protected PropertyGetCommand(String command, String transactionId) {
        super(command, transactionId);
        changeSupport = new PropertyChangeSupport(this);
    }

    @Override
    public boolean wantAcknowledgment() {
        return true;
    }

    public void setKey(String key) {
        myKey = key;
    }

    @Override
    protected String getArguments() {
        StringBuilder builder = new StringBuilder(super.getArguments());
        if (myKey != null) {
            builder.append(BrkpntSetCommand.SPACE);
            builder.append(KEY_ARG);
            builder.append(myKey);
        }
        return builder.toString();
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        changeSupport.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        changeSupport.removePropertyChangeListener(listener);
    }

    void firePropertyChangeEvent(String propertyName, Object propertyValue) {
        changeSupport.firePropertyChange(propertyName, null, propertyValue);
    }

}
