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
