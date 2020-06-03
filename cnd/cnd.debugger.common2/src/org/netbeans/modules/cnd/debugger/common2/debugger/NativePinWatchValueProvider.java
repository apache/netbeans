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
package org.netbeans.modules.cnd.debugger.common2.debugger;

import java.util.HashMap;
import java.util.Map;
import javax.swing.Action;
import org.netbeans.api.debugger.Watch;
import org.netbeans.modules.cnd.debugger.common2.values.VariableValue;
import org.netbeans.spi.debugger.ContextProvider;
import org.netbeans.spi.debugger.ui.AbstractExpandToolTipAction;
import org.netbeans.spi.debugger.ui.PinWatchUISupport;

/**
 *
 */

public class NativePinWatchValueProvider implements PinWatchUISupport.ValueProvider, PinWatchUISupport.ValueProvider.ValueChangeListener {
    public static final String ID = "NativePinWatchValueProvider";    // NOI18N
    
    private final Map<Watch, ValueChangeListener> valueListeners = new HashMap();
    private final NativeDebugger debugger;

    public NativePinWatchValueProvider(ContextProvider lookupProvider) {
        debugger = lookupProvider.lookupFirst(null, NativeDebugger.class);
        NativeDebuggerManager.get().registerPinnedWatchesUpdater(debugger, this);
    }
    
    protected final NativeDebugger getDebugger() {
        return debugger;
    }
    
    @Override
    public String getId() {
        return ID;
    }

    @Override
    public String getValue(Watch watch) {
        for (WatchVariable watchVar : debugger.getWatches()) {
            //if (watchVar.getNativeWatch().watch().getExpression().equals(watch.getExpression())) {
            //see bz#270230, the watches should be equal (otherwise update will not come anyway)
            if (watchVar.getNativeWatch().watch().equals(watch)) {
                Variable v = ((Variable) watchVar);
                VariableValue value = new VariableValue(v.getAsText(), v.getDelta());
                return value.toString();
            }
        }
        
        return "";    // NOI18N
    }

    @Override
    public String getEditableValue(Watch watch) {
        return null;
    }
    
    private boolean isExpandable(Watch watch) {
        for (WatchVariable watchVar : debugger.getWatches()) {
            if (watchVar.getNativeWatch().watch().getExpression().equals(watch.getExpression())) {
                Variable v = ((Variable) watchVar);                
                return !v.isLeaf;
            }
        }
        return false;
    }

    @Override
    public Action[] getHeadActions(Watch watch) {
        //return head action IF it is expandabe
        if (!isExpandable(watch)) {
            return new Action[0];
        }
        return new Action[]{new ExpandAction(debugger, watch)};
    }
    
    @Override
    public void setChangeListener(Watch watch, ValueChangeListener chl) {
        synchronized (valueListeners) {
            valueListeners.put(watch, chl);
        }
    }

    @Override
    public void unsetChangeListener(Watch watch) {
        synchronized (valueListeners) {
            valueListeners.remove(watch);
        }
    }
    
    @Override
    public void valueChanged(Watch watch) {
        ValueChangeListener listener = valueListeners.get(watch);
        if (listener != null) {
            listener.valueChanged(watch);
        }
    }
    
    protected static class ExpandAction extends AbstractExpandToolTipAction {

        private final NativeDebugger debugger;
        private final Watch watch;

        public ExpandAction(NativeDebugger debugger, Watch watch) {
            this.debugger = debugger;
            this.watch = watch;
        }

        @Override
        protected void openTooltipView() {
            debugger.evaluateInOutline(watch.getExpression());
        }
    }

}
