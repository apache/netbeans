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
package org.netbeans.modules.bugtracking.spi;

import javax.swing.JComponent;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.bugtracking.tasks.SchedulingPickerImpl;

/**
 * Provides a component for setting and updating Issue schedule info. The UI
 * component can be placed into an Issue editor, get the component from
 * {@link #getComponent()}.
 *
 * @author Ondrej Vrabec
 */
public final class SchedulePicker {

    private final SchedulingPickerImpl impl;

    /**
     * Creates an instance of the picker.
     */
    public SchedulePicker () {
        impl = new SchedulingPickerImpl();
    }

    /**
     * Returns a UI component that can be placed into a component hierarchy. The
     * component is able to edit and set Issue schedule info.
     *
     * @return UI component
     */
    public JComponent getComponent () {
        return impl.getComponent();
    }

    /**
     * Returns the current scheduling info either previously set by
     * {@link #setScheduleDate(IssueScheduleInfo)} or manually edited by user.
     *
     * @return scheduling info currently held by the UI component.
     * <code>null</code> can be returned and means not scheduled.
     */
    public IssueScheduleInfo getScheduleDate () {
        return impl.getScheduleDate();
    }

    /**
     * Resets the current scheduling info in the component to a given value. You
     * may pass <code>null</code> to specify no scheduling.
     *
     * @param info new scheduling info to set inside the component,
     * <code>null</code> means not scheduled.
     */
    public void setScheduleDate (IssueScheduleInfo info) {
        impl.setScheduleDate(info);
    }

    /**
     * Adds a listener that will be notified when the selected schedule changes.
     *
     * @param listener listener to add
     */
    public void addChangeListener (ChangeListener listener) {
        impl.addChangeListener(listener);
    }

    /**
     * Removes a listener, it will be no longer notified about changes.
     *
     * @param listener listener to remove
     */
    public void removeChangeListener (ChangeListener listener) {
        impl.removeChangeListener(listener);
    }
}
