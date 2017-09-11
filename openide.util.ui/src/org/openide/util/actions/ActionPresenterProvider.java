/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
