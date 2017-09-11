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


package org.netbeans.spi.palette;

import javax.swing.Action;
import org.openide.util.Lookup;

/**
 * <p>An interface implemented by palette clients to provide custom actions
 * for popup menus and actions for import of new items.</p>
 *
 * @author S. Aubrecht.
 */
public abstract class PaletteActions {

    /**
     * @return An array of action that will be used to construct buttons for import
     * of new palette item in palette manager window.
     *
     */
    public abstract Action[] getImportActions();
    
    /**
     * @return Custom actions to be added to the top of palette's default popup menu.
     */
    public abstract Action[] getCustomPaletteActions();
    
    /**
     * @param category Lookup representing palette's category.
     *
     * @return Custom actions to be added to the top of default popup menu for the given category.
     */
    public abstract Action[] getCustomCategoryActions( Lookup category );
    
    /**
     * @param item Lookup representing palette's item.
     *
     * @return Custom actions to be added to the top of the default popup menu for the given palette item.
     */
    public abstract Action[] getCustomItemActions( Lookup item );
    
    
    /**
     * @param item Lookup representing palette's item.
     *
     * @return An action to be invoked when user double-clicks the item in the
     * palette (e.g. insert item at editor's default location).
     * Return null to disable preferred action for this item.
     */
    public abstract Action getPreferredAction( Lookup item );
    
    /**
     * An action that will be invoked as part of the palette refresh logic,
     * for example when user chooses "Refresh" in palette's popup menu. Can be null.
     * The action properties (label, icon) are not displayed to the user, the Palette module
     * will provide its own.
     * @return Custom refresh action or null.
     * @since 1.9
     */
    public Action getRefreshAction() {
        return null;
    }
    
    /**
     * An action that resets the palette content to its default state. The action can be 
     * invoked by the user from palette's popup menu for from the Palette Manager window.
     * The action properties (label, icon) are not displayed to the user, the Palette module
     * provides its own.
     * @return Custom reset action or null to use the default one that removes all user's 
     * modifications to the XML layer files.
     * @since 1.11
     */
    public Action getResetAction() {
        return null;
    }
}
