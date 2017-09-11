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
