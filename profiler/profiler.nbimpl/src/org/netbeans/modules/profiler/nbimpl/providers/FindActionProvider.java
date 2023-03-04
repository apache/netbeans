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
package org.netbeans.modules.profiler.nbimpl.providers;

import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.KeyStroke;
import org.netbeans.lib.profiler.ui.swing.SearchUtils;
import org.netbeans.modules.profiler.api.ActionsSupport;
import org.netbeans.modules.profiler.spi.ActionsSupportProvider;
import org.openide.awt.Actions;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Jiri Sedlacek
 */
@ServiceProvider(service=ActionsSupportProvider.class, position=1)
public final class FindActionProvider extends ActionsSupportProvider {
    
    private static final String FIND_ACTION_CATEGORY = "Edit"; // NOI18N
    private static final String FIND_ACTION_KEY = "org.openide.actions.FindAction"; // NOI18N
    
    public KeyStroke registerAction(String actionKey, Action action, ActionMap actionMap, InputMap inputMap) {
        if (!SearchUtils.FIND_ACTION_KEY.equals(actionKey)) return null;
        
        Action find = Actions.forID(FIND_ACTION_CATEGORY, FIND_ACTION_KEY);
        if (find == null) return null;
        
        actionMap.put(FIND_ACTION_KEY, action);
        
        Object acc = find.getValue(Action.ACCELERATOR_KEY);
        return acc instanceof KeyStroke ? (KeyStroke)acc : ActionsSupport.NO_KEYSTROKE;
    }
}
