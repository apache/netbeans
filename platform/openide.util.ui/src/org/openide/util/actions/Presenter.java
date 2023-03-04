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
package org.openide.util.actions;

import java.awt.Component;

import javax.swing.*;


/** Provides a presentation feature for an action.
* Each {@link SystemAction action} that wants to offer a kind of presentation of itself
* to the user should implement one of the inner interfaces.
* <P>
* For example to be presented in popup menu, an action should
* implement {@link Presenter.Popup}.
* <p> Normally actions should implement both {@link Presenter.Menu} and
* {@link Presenter.Popup} together and return the same menu item for each.
* <p><em>Note:</em> implementing these interfaces yourself means that you want to
* provide some sort of unusual display format, e.g. a submenu!
* Most people will simply want to use a subclass of {@link CallableSystemAction}
* and use the default implementations of all three interfaces, according to
* {@link SystemAction#getName} and {@link SystemAction#iconResource}.
*
* @author Jaroslav Tulach
*/
public interface Presenter {
    /** The presenter interface for presenting an action in a menu.
    */
    public interface Menu extends Presenter {
        /** Get a menu item that can present this action in a {@link javax.swing.JMenu}.
         * If your menu content is dynamic in nature, consider using <a href="@org-openide-awt@/org/openide/awt/DynamicMenuContent.html">DynamicMenuContent</a>
         * @return the representation for this action
         */
        public JMenuItem getMenuPresenter();
    }

    /** The presenter interface for presenting an action in a popup menu.
    */
    public interface Popup extends Presenter {
        /** Get a menu item that can present this action in a {@link javax.swing.JPopupMenu}.
         * If your menu content is dynamic in nature, consider using <a href="@org-openide-awt@/org/openide/awt/DynamicMenuContent.html">DynamicMenuContent</a>
        * @return the representation for this action
        */
        public JMenuItem getPopupPresenter();
    }

    /** The presenter interface for presenting an action in a toolbar.
    */
    public interface Toolbar extends Presenter {
        /** Get a component that can present this action in a {@link javax.swing.JToolBar}.
        * @return the representation for this action
        */
        public Component getToolbarPresenter();
    }
}
