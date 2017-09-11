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
