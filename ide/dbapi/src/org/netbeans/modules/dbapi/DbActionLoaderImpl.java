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

package org.netbeans.modules.dbapi;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import javax.swing.Action;
import org.netbeans.modules.db.api.explorer.ActionProvider;
import org.netbeans.modules.db.explorer.DbActionLoader;
import org.openide.util.lookup.Lookups;

/**
 * Loads actions from all registered action providers and delivers them to
 * the caller of getAllActions().
 *  
 * @author David Van Couvering
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.db.explorer.DbActionLoader.class)
public class DbActionLoaderImpl implements DbActionLoader {
    
            /** 
     * Not private because used in the tests.
     */
    static final String ACTION_PROVIDER_PATH = "Databases/ActionProviders"; // NOI18N

    public List<Action> getAllActions() {
        List<Action> actions = new ArrayList<Action>();
        Collection providers = Lookups.forPath(ACTION_PROVIDER_PATH).
                lookupAll(ActionProvider.class);
        
        for (Iterator<ActionProvider> i = providers.iterator(); i.hasNext();) {
            ActionProvider provider = i.next();
            List<Action> actionList = provider.getActions();
            if (actionList != null) {
                actions.addAll(actionList);
            }
        }
        
        return actions;
    }
}
