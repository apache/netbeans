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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
package org.netbeans.jellytools;

import java.awt.Component;
import org.netbeans.jellytools.actions.Action;
import org.netbeans.jemmy.ComponentChooser;
import org.netbeans.jemmy.operators.JTreeOperator;

/** Keeps methods to access navigator component.
 *
 * @author Jindrich Sedek
 */
public class NavigatorOperator extends TopComponentOperator {

    private static final String NAVIGATOR_TITLE =
            Bundle.getString("org.netbeans.modules.navigator.Bundle", "LBL_Navigator");

    /** NavigatorOperator is created for navigator window. 
     *  Navigator window must be displayed.
     */
    public NavigatorOperator() {
        super(waitTopComponent(null, NAVIGATOR_TITLE, 0, new NavigatorComponentChooser()));
    }

    /** This function displays navigator window and returns operator for it
     * 
     *@return navigator operator
     * 
     */
    public static NavigatorOperator invokeNavigator() {
        new NavigatorAction().perform();
        return new NavigatorOperator();
    }

    /** Using navigation Tree you can access root node and then it's children 
     * recursively.
     * 
     * @return Operator of the navigation tree
     */
    public JTreeOperator getTree() {
        return new JTreeOperator(this);
    }

    private static final class NavigatorAction extends Action {

        private static final String navigatorActionName = Bundle.getStringTrimmed("org.netbeans.core.windows.resources.Bundle", "Menu/Window")
                + "|Navigator";

        public NavigatorAction() {
            super(navigatorActionName, null, "org.netbeans.modules.navigator.ShowNavigatorAction");
        }
    }

    private static final class NavigatorComponentChooser implements ComponentChooser {

        @Override
        public boolean checkComponent(Component comp) {
            return (comp.getClass().getName().equals("org.netbeans.modules.navigator.NavigatorTC"));
        }

        @Override
        public String getDescription() {
            return "Navigator Window";
        }
    }
}
