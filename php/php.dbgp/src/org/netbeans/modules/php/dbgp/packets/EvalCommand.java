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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author ads
 *
 */
public class EvalCommand extends DbgpCommand {
    static final String EVAL = "eval"; // NOI18N
    private String myData;
    private List<PropertyChangeListener> myListeners;

    public EvalCommand(String transactionId) {
        this(EVAL, transactionId);
        myListeners = new CopyOnWriteArrayList<>();
    }

    protected EvalCommand(String command, String transactionId) {
        super(command, transactionId);
    }

    @Override
    public boolean wantAcknowledgment() {
        return true;
    }

    public void setData(String data) {
        myData = data;
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        myListeners.add(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        myListeners.remove(listener);
    }

    void firePropertyChangeEvent(String propName, Property property) {
        for (PropertyChangeListener listener : myListeners) {
            listener.propertyChange(new PropertyChangeEvent(this, propName, null, property));
        }
    }

    @Override
    protected String getData() {
        return myData;
    }

}
