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
package org.netbeans.modules.web.browser.api;

import javax.swing.JComponent;
import javax.swing.JPopupMenu;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.web.browser.spi.ProjectBrowserProvider;
import org.netbeans.modules.web.browser.ui.picker.BrowserMenu;

/**
 * Popup menu listing all available browsers in several columns. A ChangeEvent is
 * fired when the browser selection changed.
 *
 * @author S. Aubrecht
 */
public final class BrowserPickerPopup {

    private final BrowserMenu menu;

    private BrowserPickerPopup( BrowserMenu menu ) {
        this.menu = menu;
    }

    /**
     * Creates a new browser picker menu.
     * @param provider Project provider listing the browser.
     * @return New browser menu.
     */
    public static BrowserPickerPopup create( ProjectBrowserProvider provider ) {
        return new BrowserPickerPopup( new BrowserMenu( provider ) );
    }

    /**
     * Shows the popup menu at given location.
     * @param invoker
     * @param x
     * @param y
     * @see JPopupMenu#show(java.awt.Component, int, int)
     */
    public void show( JComponent invoker, int x, int y ) {
        menu.show( invoker, x, y );
    }

    /**
     * @return Currently selected browser or null.
     */
    public WebBrowser getSelectedBrowser() {
        return menu.getSelectedBrowser();
    }

    public void addChangeListener( ChangeListener changeListener ) {
        menu.addChangeListener( changeListener );
    }

    public void removeChangeListener( ChangeListener changeListener ) {
        menu.removeChangeListener( changeListener );
    }
}
