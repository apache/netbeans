/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2004, 2016 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 */
/*
 * SimpleActionProvider.java
 *
 * Created on January 25, 2004, 3:06 PM
 */

package org.netbeans.actions.simple;

import java.util.MissingResourceException;
import java.util.ResourceBundle;
import javax.swing.Icon;
import org.netbeans.actions.spi.ActionProvider;

/**
 *
 * @author  tim
 */
public class SimpleActionProvider extends ActionProvider {
    private Interpreter interp;
    private ResourceBundle bundle;
    /** Creates a new instance of SimpleActionProvider */
    public SimpleActionProvider(Interpreter interp, ResourceBundle bundle) {
        this.interp = interp;
        this.bundle = bundle;
    }

    public String[] getActionNames(String containerCtx) {
        return interp.getActionNames(containerCtx);
    }

    public int getActionType(String actionName, String containerCtx) {
        return ACTION_TYPE_ITEM; //XXX
    }

    public String getDescription(String actionName, String containerCtx) {
        return actionName + "description"; //XXX
    }

    public String getDisplayName(String actionName, String containerCtx) {
        try {
            return bundle.getString(actionName);
        } catch (MissingResourceException mre) {
            mre.printStackTrace();
            return actionName;
        }
    }
    
    public Icon getIcon(String actionName, String containerCtx, int type) {
        Icon result = interp.getIconForAction(actionName);
        if (result == null) {
            result = dummyIcon;
        }
        return result;
    }
    
    public int getMnemonic(String actionName, String containerCtx) {
        return 0;
    }
    
    public int getMnemonicIndex(String actionName, String containerCtx) {
        return 0;
    }
    
    public int getState(String actionName, String containerCtx, java.util.Map context) {
        if (interp.contextContainsAction(containerCtx, actionName)) {
            return interp.getState(actionName, context);
        } else {
            throw new IllegalStateException ("Container " + containerCtx + " does not contain an action " + actionName);
        }
    }
    
    private Icon dummyIcon = new DummyIcon();
    private class DummyIcon implements Icon {
        
        public int getIconHeight() {
            return 16;
        }
        
        public int getIconWidth() {
            return 16;
        }
        
        public void paintIcon(java.awt.Component c, java.awt.Graphics g, int x, int y) {
            g.setColor(java.awt.Color.BLUE);
            g.drawRect (x+4, y+4, 6, 6);
        }
        
    }
    
}
