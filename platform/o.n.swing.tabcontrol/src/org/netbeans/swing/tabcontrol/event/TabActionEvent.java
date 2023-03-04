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
/*
 * TabActionEvent.java
 *
 * Created on March 17, 2004, 3:48 PM
 */

package org.netbeans.swing.tabcontrol.event;

import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;

/**
 * An action event which may be consumed by a listener.  These are fired by
 * TabControl and TabbedContainer to determine if outside code wants to handle
 * an event, such as clicking the close button (which might be vetoed), or if
 * the control should handle it itself.
 *
 * @author Tim Boudreau
 */
public final class TabActionEvent extends ActionEvent {
    private MouseEvent mouseEvent = null;
    private int tabIndex;
    private String groupName = null;

    /**
     * Creates a new instance of TabActionEvent
     */
    public TabActionEvent(Object source, String command, int tabIndex) {
        super(source, ActionEvent.ACTION_PERFORMED, command);
        this.tabIndex = tabIndex;
        consumed = false;
    }

    public TabActionEvent(Object source, String command, int tabIndex,
                          MouseEvent mouseEvent) {
        this(source, command, tabIndex);
        this.mouseEvent = mouseEvent;
        consumed = false;
    }
    
    /**
     * Consume this event - any changes that should be performed as a result
     * will be done by external code by manipulating the models or other means
     */
    @Override
    public void consume() {
        consumed = true;
    }

    /**
     * Determine if the event has been consumed
     */
    @Override
    public boolean isConsumed() {
        return super.isConsumed();
    }

    /**
     * If the action event was triggered by a mouse event, get the mouse event
     * in question
     *
     * @return The mouse event, or null
     */
    public MouseEvent getMouseEvent() {
        return mouseEvent;
    }

    public int getTabIndex() {
        return tabIndex;
    }
    
    /**
     * @return Name of window group this command applies to or null.
     * @since 1.27
     */
    public String getGroupName() {
        return groupName;
    }
    
    /**
     * Set the name of window group this command applies to.
     * @param groupName 
     * @since 1.27
     */
    public void setGroupName( String groupName ) {
        this.groupName = groupName;
    }

    @Override
    public void setSource(Object source) {
        //Skip some native peer silliness in AWTEvent
        this.source = source;
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer("TabActionEvent:"); //NOI18N
        sb.append ("Tab " + tabIndex + " " + getActionCommand()); //NOI18N
        return sb.toString();
    }

}
