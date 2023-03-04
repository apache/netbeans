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
package org.netbeans.api.visual.action;

import org.netbeans.api.visual.widget.Widget;

import javax.swing.*;
import java.awt.*;

/**
 * This interface provides a popup menu.
 *
 * @author William Headrick, David Kaspar
 */
public interface PopupMenuProvider {

    /**
     * Get a JPopupMenu to display in the context of the given Widget.
     * This method may return <code>null</code>.  If that is the case,
     * no popup menu will be displayed if this PopupMenuAction gets
     * a valid popup trigger on the given Widget.
     * Note: Since version 2.6 the <code>localLocation<code> parameter could be null.
     * @param widget the widget
     * @param localLocation the local location where the popup menu was invoked; if null, then popup menu is invoked by a keyboard
     * @return The JPopupMenu to display for the given Widget.
     *         May be <code>null</code>.
     */
    public JPopupMenu getPopupMenu (Widget widget, Point localLocation);

}
