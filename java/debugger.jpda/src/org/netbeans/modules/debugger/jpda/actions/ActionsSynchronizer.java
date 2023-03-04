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
package org.netbeans.modules.debugger.jpda.actions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.debugger.jpda.JPDADebugger;
import static org.netbeans.api.debugger.ActionsManager.*;

/**
 * Some actions need to wait on each other. E.g. when we submit a breakpoint,
 * continue action needs to wait till the breakpoint submission is fully done.
 * This class takes care about synchronization of actions.
 * 
 * @author Martin Entlicher
 */
public class ActionsSynchronizer {
    
    private static final Logger logger = Logger.getLogger(ActionsSynchronizer.class.getName());
    
    private static final Map<JPDADebugger, ActionsSynchronizer> INSTANCES = new WeakHashMap<JPDADebugger, ActionsSynchronizer>();
    
    private static final Map<Object, Set<Object>> ACTIONS_WAITING_FOR_OTHERS = new HashMap<Object, Set<Object>>();
    
    static {
        Set<Object> actionsToWaitForBeforeResume = new HashSet<Object>(Arrays.asList(
                                                   ACTION_TOGGLE_BREAKPOINT, ACTION_FIX));
        ACTIONS_WAITING_FOR_OTHERS.put(ACTION_CONTINUE, actionsToWaitForBeforeResume);
        ACTIONS_WAITING_FOR_OTHERS.put(ACTION_STEP_INTO, actionsToWaitForBeforeResume);
        ACTIONS_WAITING_FOR_OTHERS.put(ACTION_STEP_OPERATION, actionsToWaitForBeforeResume);
        ACTIONS_WAITING_FOR_OTHERS.put(ACTION_STEP_OUT, actionsToWaitForBeforeResume);
        ACTIONS_WAITING_FOR_OTHERS.put(ACTION_STEP_OVER, actionsToWaitForBeforeResume);
        ACTIONS_WAITING_FOR_OTHERS.put(ACTION_RUN_TO_CURSOR, actionsToWaitForBeforeResume);
    }
    
    private final List<Object> runningActions = new ArrayList<Object>();
    private final List<Object> scheduledActions = new ArrayList<Object>();
    
    private ActionsSynchronizer() {}
    
    /**
     * Get the ActionsSynchronizer instance for the given debugger.
     */
    public static ActionsSynchronizer get(JPDADebugger debugger) {
        synchronized (INSTANCES) {
            ActionsSynchronizer as = INSTANCES.get(debugger);
            if (as == null) {
                as = new ActionsSynchronizer();
                INSTANCES.put(debugger, as);
            }
            return as;
        }
    }
    
    public void actionScheduled(Object action) {
        synchronized (runningActions) {
            logger.log(Level.FINE, "actionScheduled({0})", action);
            scheduledActions.add(action);
        }
    }
    
    /**
     * Call this when the action is about to start.
     * This method may block until the action is allowed to start.
     * 
     * @param action The action which is about to start.
     */
    public void actionStarts(Object action) {
        synchronized (runningActions) {
            logger.log(Level.FINE, "actionStarts({0})", action);
            scheduledActions.remove(action);
            Set<Object> waitingFor = ACTIONS_WAITING_FOR_OTHERS.get(action);
            if (waitingFor != null) {
                while(containsAny(scheduledActions, waitingFor) ||
                      containsAny(runningActions, waitingFor)) {
                    logger.log(Level.FINE, "action {0} is blocked.", action);
                    try {
                        runningActions.wait();
                    } catch (InterruptedException ex) {}
                }
            }
            runningActions.add(action);
            logger.log(Level.FINE, "action {0} can proceed. Running actions = {1}", new Object[] { action, runningActions});
        }
    }
    
    /**
     * Call this when the action has finished.
     * For every {@link #actionStarts(java.lang.Object)}, there must be a call
     * to this method, otherwise a deadlock can occur. Call this in a finally
     * block, when the action has finished.
     * 
     * @param action The action which has just finished.
     */
    public void actionEnds(Object action) {
        synchronized (runningActions) {
            runningActions.remove(action);
            logger.log(Level.FINE, "actionEnds({0}). Running actions = {1}", new Object[] { action, runningActions});
            runningActions.notifyAll();
        }
    }
    
    private boolean containsAny(List<Object> s1, Set<Object> s2) {
        for (Object o : s1) {
            if (s2.contains(o)) {
                return true;
            }
        }
        return false;
    }
}
