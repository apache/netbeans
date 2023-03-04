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
 *//*
 * ComplexListDataListener.java
 *
 * Created on May 26, 2003, 6:01 PM
 */

package org.netbeans.swing.tabcontrol.event;

import javax.swing.event.ListDataListener;

/**
 * An extension to javax.swing.ListDataListener to allow handling of events on
 * non-contiguous elements
 *
 * @author Tim
 */
public interface ComplexListDataListener extends ListDataListener {
    /**
     * Elements have been added at the indices specified by the event's
     * getIndices() value
     *
     * @param e The event
     */
    void indicesAdded(ComplexListDataEvent e);

    /**
     * Elements have been removed at the indices specified by the event's
     * getIndices() value
     *
     * @param e The event
     */
    void indicesRemoved(ComplexListDataEvent e);

    /**
     * Elements have been changed at the indices specified by the event's
     * getIndices() value.  If the changed data can affect display width (such
     * as a text change or a change in icon size), the event's
     * <code>isTextChanged()</code> method will return true.
     *
     * @param e The event
     */
    void indicesChanged(ComplexListDataEvent e);
}
