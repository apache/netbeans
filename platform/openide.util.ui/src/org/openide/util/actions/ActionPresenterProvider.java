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
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import org.openide.util.Lookup;

/** Provider of action presentations. Based on type of the action
 * should be able to derive its menu, popup menu and toolbar
 * presenter.
 * <P>
 * In order to provide greater flexibility this is made as a pluggable component
 * to allow other parts of the system to provide more enhanced
 * visualizations.
 * @since 8.1
 */
public abstract class ActionPresenterProvider extends Object {
    /** Gets the default implementation from lookup.
     * @return the presenter
     */
    public static ActionPresenterProvider getDefault () {
        ActionPresenterProvider ap = Lookup.getDefault().lookup(ActionPresenterProvider.class);
        return ap == null ? new Default () : ap;
    }
    
    /** Subclass constructor. */
    protected ActionPresenterProvider() {}

    /** Creates a default empty implementation of popup menu.
     * @return popup menu
     */
    public abstract JPopupMenu createEmptyPopup();
    
    /** Creates a menu item that can present this action in a {@link javax.swing.JMenu}.
     * @param action the action to represent
     * @return the representation for this action
     */
    public abstract JMenuItem createMenuPresenter (Action action);
    
    /** Get a menu item that can present this action in a {@link javax.swing.JPopupMenu}.
     * @param action the action to represent
    * @return the representation for this action
    */
    public abstract JMenuItem createPopupPresenter (Action action);
    
    /** Get a component that can present this action in a {@link javax.swing.JToolBar}.
     * @param action the action to represent
    * @return the representation for this action
    */
    public abstract Component createToolbarPresenter (Action action);
    
    /**
     * Used for implementation of <a href="@org-openide-awt@/org/openide/awt/DynamicMenuContent.html"><code>DynamicMenuContent</code></a>.
     * @param comp a component
     * @return zero or more components to display in its place
     */
    public abstract Component[] convertComponents(Component comp);
    
    //
    // Default implementation of the the presenter
    // 
    
    private static final class Default extends ActionPresenterProvider {
        
        public JMenuItem createMenuPresenter(Action action) {
            return new JMenuItem(action);
        }
        
        public JMenuItem createPopupPresenter(Action action) {
            return new JMenuItem(action);
        }
        
        public Component createToolbarPresenter(Action action) {
            return new JButton(action);
        }
        
        public JPopupMenu createEmptyPopup() {
            return new JPopupMenu();
        }
        
        public Component[] convertComponents(Component comp) {
            return new Component[] {comp};
        }
    }
}
