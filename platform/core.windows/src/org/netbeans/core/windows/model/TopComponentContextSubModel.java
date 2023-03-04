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


package org.netbeans.core.windows.model;

import java.util.HashMap;
import java.util.Map;
import org.netbeans.core.windows.ModeImpl;
import org.netbeans.core.windows.SplitConstraint;
import org.openide.windows.TopComponent;

/**
 * Model which stores context of TopComponents in one mode. Context consists
 * of mode and constraints info of previous container TopComponent was part of.
 *
 * This sub model is not thread safe. It is supposed to be just part of DefaultModeModel
 * which is responsible for the synch.
 *
 * @author  Dafe Simonek
 */
final class TopComponentContextSubModel {
    
    private static final class Context {
        // XXX we should use weak reference for holding mode, to let it vanish
        ModeImpl mode;
        //tab index
        int index = -1;
        SplitConstraint[] constraints;
    } // end of Context

    /** Mapping <TopComponentID, Context> between top component and context holding
     its previous location */
    private final Map<String, Context> tcID2Contexts = new HashMap<String, Context> (10);
    
    public TopComponentContextSubModel() {
    }

    public void setTopComponentPreviousConstraints(String tcID, SplitConstraint[] constraints) {
        Context context = tcID2Contexts.get(tcID);
        if (context == null) {
            context = new Context();
            tcID2Contexts.put(tcID, context);
        }
        context.constraints = constraints;
    }
    
    public void setTopComponentPreviousMode(String tcID, ModeImpl mode, int index) {
        Context context = tcID2Contexts.get(tcID);
        if (context == null) {
            context = new Context();
            tcID2Contexts.put(tcID, context);
        }
        context.mode = mode;
        context.index = index;
    }
    
    public SplitConstraint[] getTopComponentPreviousConstraints(String tcID) {
        Context context = tcID2Contexts.get(tcID);
        return context == null ? null : context.constraints;
    }
    
    public ModeImpl getTopComponentPreviousMode(String tcID) {
        Context context = tcID2Contexts.get(tcID);
        return context == null ? null : context.mode;
    }
    
    public int getTopComponentPreviousIndex(String tcID) {
        Context context = tcID2Contexts.get(tcID);
        return context == null ? -1 : context.index;
    }
}
