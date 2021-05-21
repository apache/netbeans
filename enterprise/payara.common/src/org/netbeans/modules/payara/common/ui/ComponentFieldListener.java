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
package org.netbeans.modules.payara.common.ui;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

// This is copy-paste from Payara cloud module. It should be moved
// to some common place later.
// Source: org.netbeans.modules.payara.cloud.wizards.PayaraWizardComponent
/**
 * Event listener to validate component field on the fly.
 * <p/>
 * @author Tomas Kraus
 */
abstract class ComponentFieldListener implements DocumentListener {

    ////////////////////////////////////////////////////////////////////////////
    // Abstract methods                                                       //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Process received notification from all notification types.
     */
    abstract void processEvent();

    ////////////////////////////////////////////////////////////////////////////
    // Implemented Interface Methods                                          //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Gives notification that there was an insert into component field.
     * <p/>
     * @param event Change event object.
     */
    @Override
    public void insertUpdate(DocumentEvent e) {
        processEvent();
    }

    /**
     * Gives notification that a portion of component field has been removed.
     * <p/>
     * @param event Change event object.
     */
    @Override
    public void removeUpdate(DocumentEvent e) {
        processEvent();
    }

    /**
     * Gives notification that an attribute or set of attributes changed.
     * <p/>
     * @param event Change event object.
     */
    @Override
    public void changedUpdate(DocumentEvent e) {
        processEvent();
    }

}
