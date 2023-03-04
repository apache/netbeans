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

package org.netbeans.modules.form;

/**
 * Listener class for notifying about changes in FormModel. There's only one
 * method to implement, with an array of FormModelEvent objects as* parameter.
 * (FormModel does batch event firing, all the events corresponding to one user
 * action are fired at once.)
 *
 * @author Tomas Pavek
 */

public interface FormModelListener extends java.util.EventListener {

    /** Notification about changes made in FormModel. Type of the changes
     * can be obtained from FormModelEvent.getChangeType() method.
     * @param events array of events fired from FormModel
     */
    public void formChanged(FormModelEvent[] events);
}
