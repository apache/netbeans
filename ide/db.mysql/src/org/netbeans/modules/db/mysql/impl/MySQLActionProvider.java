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

package org.netbeans.modules.db.mysql.impl;

import org.netbeans.modules.db.mysql.actions.RegisterServerAction;
import java.util.ArrayList;
import java.util.List;
import javax.swing.Action;
import org.netbeans.modules.db.api.explorer.ActionProvider;
import org.openide.util.actions.SystemAction;

/**
 * Provides the actions for this module to be registered under the
 * Databases node in the DB Explorer
 * 
 * @author David Van Couvering
 */
public class MySQLActionProvider implements ActionProvider {

    private static final MySQLActionProvider DEFAULT = new MySQLActionProvider();
    private final ArrayList<Action> actions = new ArrayList<Action>();
    private static final ArrayList<Action> emptyActions = new ArrayList<Action>();
    
    static MySQLActionProvider getDefault() {
        return DEFAULT;
    }
    
    private MySQLActionProvider() {
        // Right now only one server, although this may (likely) change
        actions.add(SystemAction.get(RegisterServerAction.class));
    }

    public List<Action> getActions() {
        // If we're registered, then don't provide the action
        // to register the server...
        
        if ( ServerNodeProvider.getDefault().isRegistered()){
            return emptyActions;
        } else {
            return actions;
        }
    } 
    
}
