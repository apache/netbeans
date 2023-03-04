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

package org.netbeans.core.windows.view.ui.slides;

import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;

/**
 *
 * @author Dafe Simonek
 */
public final class SlideBarActionEvent extends ActionEvent {

    private final MouseEvent mouseEvent;

    private final int tabIndex;

    private final SlideOperation slideOperation;

    public SlideBarActionEvent(Object source, String command, MouseEvent mouseEvent, int tabIndex) {
        this(source, command, null, mouseEvent, tabIndex);
    }

    public SlideBarActionEvent(Object source, String command, SlideOperation slideOperation) {
        this(source, command, slideOperation, null, -1);
    }
    
    public SlideBarActionEvent(Object source, String command, SlideOperation operation,
                                MouseEvent mouseEvent, int tabIndex) {
        super(source, ActionEvent.ACTION_PERFORMED, command);
        this.tabIndex = tabIndex;
        this.mouseEvent = mouseEvent;
        this.slideOperation = operation;
    }
    
    
    public MouseEvent getMouseEvent() {
        return mouseEvent;
    }

    public int getTabIndex() {
        return tabIndex;
    }
    
    public SlideOperation getSlideOperation() {
        return slideOperation;
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer("SlideBarActionEvent:"); //NOI18N
        sb.append ("Tab " + tabIndex + " " + getActionCommand()); //NOI18N
        return sb.toString();
    }

}
