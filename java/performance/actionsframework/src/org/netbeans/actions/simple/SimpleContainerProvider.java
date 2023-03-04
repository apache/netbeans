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
 * SimpleContainerProvider.java
 *
 * Created on January 25, 2004, 3:07 PM
 */

package org.netbeans.actions.simple;

import java.util.MissingResourceException;
import java.util.ResourceBundle;
import org.netbeans.actions.spi.ActionProvider;
import org.netbeans.actions.spi.ContainerProvider;

/**
 *
 * @author  tim
 */
public class SimpleContainerProvider extends ContainerProvider {
    private Interpreter interp;
    ResourceBundle bundle;
    /** Creates a new instance of SimpleContainerProvider */
    public SimpleContainerProvider(Interpreter interp, ResourceBundle bundle) {
        this.interp = interp;
        this.bundle = bundle;
    }

    public int getContainerState(Object containerType, String containerCtx, java.util.Map context) {
        return ActionProvider.STATE_ENABLED | ActionProvider.STATE_VISIBLE;
    }

    public String getDisplayName(Object containerType, String containerCtx) {
        try {
            return bundle.getString(containerCtx);
        } catch (MissingResourceException mre) {
            mre.printStackTrace();
            return containerCtx;
        }
    }
    
    public String[] getMenuContainerContexts() {
        return interp.getMenus();
    }
    
    public String[] getToolbarContainerContexts() {
        return interp.getToolbars();
    }
    
}
