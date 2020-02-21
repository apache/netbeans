/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.cnd.callgraph.impl;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.swing.Action;
import org.netbeans.modules.cnd.callgraph.api.Call;
import org.netbeans.modules.cnd.callgraph.api.CallModel;
import org.netbeans.modules.cnd.callgraph.api.Function;

/**
 *
 */
public class CallGraphState {
    private CallModel model;
    private CallGraphScene scene;
    private List<Action> actions;
    private Map<Function, Boolean> calleesExpanded = new ConcurrentHashMap<Function, Boolean>();
    private Map<Function, Boolean> callersExpanded = new ConcurrentHashMap<Function, Boolean>();
    
    public CallGraphState(CallModel model, CallGraphScene scene, List<Action> actions){
        this.model = model;
        this.scene = scene;
        this.actions = actions;
    }

    public List<Call> getCallers(Function declaration) {
        return model.getCallers(declaration);
    }

    public List<Call> getCallees(Function definition) {
        return model.getCallees(definition);
    }

    public void doLayout(){
        if (scene != null) {
            scene.doLayout();
        }
    }
    
    public void addCallToScene(Call element){
        if (scene != null) {
            scene.addCallToScene(element);
        }
    }
    
    public void addFunctionToScene(Function element){
        if (scene != null) {
            scene.addFunctionToScene(element);
        }
    }

    public void setCalleesExpanded(Function element, boolean expanded) {
        calleesExpanded.put(element, expanded);
    }

    public boolean isCalleesExpanded(Function element) {
        Boolean expanded = calleesExpanded.get(element);
        if (expanded == null) {
            return false;
        }
        return expanded;
    }

    public void setCallersExpanded(Function element, boolean expanded) {
        callersExpanded.put(element, expanded);
    }

    public boolean isCallersExpanded(Function element) {
        Boolean expanded = callersExpanded.get(element);
        if (expanded == null) {
            return false;
        }
        return expanded;
    }

    public Action[] getActions() {
        return actions.toArray(new Action[actions.size()]);
    }
}
