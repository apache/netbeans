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

package org.openide.awt;

import javax.swing.JComponent;
import org.openide.util.Utilities;

/**
 * Dynamic result of a {@link org.openide.util.actions.Presenter.Menu} or {@link org.openide.util.actions.Presenter.Popup}. If the presenters return
 * an instance of <code>DynamicMenuContent</code>, then the framework code
 * will use it's methods to populate the menu and keep it uptodate.
 * @author mkleint
 * @since org.openide.awt 6.5
 */
public interface DynamicMenuContent {
    /**
     * Create main menu/popup menuitems. Null values will be later replaced by JSeparators.
     * This method is called for popups and for menus. It's called each time a popup menu is contructed and just 
     * once for the main menu. Main menu updates happen through the <code>synchMenuPresenters()</code> method.
     * If you want different behaviour for menu and popup,
     * use a different implementation returned by {@link org.openide.util.actions.Presenter.Popup} and {@link org.openide.util.actions.Presenter.Menu}.
     */
    public JComponent[] getMenuPresenters();
    
    /**
     * update main menu presenters. This method is called only by the main menu processing.
     * @param items the previously used menuitems returned by previous call to <code>getMenuPresenters()</code> or <code>synchMenuPresenters()</code>
     * @return a new set of items to show in menu. Can be either an updated old set of instances or a completely new one.
     */
    public JComponent[] synchMenuPresenters(JComponent[] items);

    /**
     * Marker for actions which should be hidden rather than merely disabled.
     * {@link Utilities#actionsToPopup(Action[],Lookup)} will skip over any disabled
     * actions which have this property set to true, unless they implement
     * {@link org.openide.util.actions.Presenter.Popup}.
     * This is a convenient way to make context menu items disappear when disabled;
     * for more complex cases you still need to have a popup presenter with dynamic
     * menu content.
     * @since org.openide.awt 7.22
     */
    String HIDE_WHEN_DISABLED = "hideWhenDisabled"; // NOI18N

}
