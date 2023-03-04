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
package org.netbeans.modules.css.visual;

import java.util.ArrayList;
import java.util.Collection;
import javax.swing.Action;
import javax.swing.ButtonGroup;

/**
 *
 * @author marekfukala
 */
public class ViewActions {
    
    private final Collection<ViewActionSupport> actions;

    public ViewActions(RuleEditorViews views) {
        actions = new ArrayList<ViewActionSupport>();
        
        actions.add(new ViewActionSupport.UpdatedOnlyViewAction(views));
        actions.add(new ViewActionSupport.CategorizedViewAction(views));
        actions.add(new ViewActionSupport.AllViewAction(views));
        
        ButtonGroup group = new ButtonGroup();
        for(ViewActionSupport action : actions) {
            group.add(action.obtainMenuItem());
        }
        
    }

    public Action[] getActions() {
        return actions.toArray(new Action[0]);
    }
    
}
