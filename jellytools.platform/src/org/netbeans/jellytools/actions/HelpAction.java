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
package org.netbeans.jellytools.actions;

import java.awt.event.KeyEvent;
import javax.swing.KeyStroke;
import org.netbeans.jellytools.Bundle;
import org.netbeans.jemmy.JemmyException;

/** Used to invoke help using "Help|Help Contents" menu or the F1 shortcut.
 * Can also invoke help on a property sheet from popup menu.
 *
 * @see Action
 * @author <a href="mailto:adam.sotona@sun.com">Adam Sotona</a> */
public class HelpAction extends Action {

    // String used in property sheets
    private static final String popupPath = Bundle.getString("org.openide.explorer.propertysheet.Bundle", "CTL_Help");    
    private static final KeyStroke keystroke = KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0);
    private static final String helpMenu = getHelpMenu();


    /**
     * Gets the bundle message for the "Help Contents" menu item. In case it is
     * unavailable, it falls back to a hard-coded message.
     *
     * @return String message for "Help Contents" menu item
     */
    public static String getHelpMenu()
    {
        String lsRetVal;

        try
        {
            lsRetVal = Bundle.getStringTrimmed("org.netbeans.core.ui.resources.Bundle", "Menu/Help")
                                         + "|"
                                         + Bundle.getStringTrimmed("org.netbeans.modules.usersguide.Bundle",
                                         "Menu/Help/org-netbeans-modules-usersguide-master.xml");
        }
        catch (JemmyException e)
        {
            System.err.println("Warning: " + e.getMessage() + " Falling back to hard-coded message!");
            lsRetVal = "Help &Contents";
        }

        return lsRetVal;
    }

    /** Creates new HelpAction instance for master help set (Help|Contents)
     * or for generic use e.g. in property sheets.
     */
    public HelpAction() {
        super(helpMenu, popupPath, keystroke);
    }

}
