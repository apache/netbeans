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
package org.netbeans.modules.debugger.jpda.visual.breakpoints;

import com.sun.jdi.ObjectReference;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.WeakHashMap;
import org.netbeans.api.debugger.Breakpoint;
import org.netbeans.api.debugger.jpda.JPDABreakpoint;
import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.api.debugger.jpda.LineBreakpoint;
import org.netbeans.modules.debugger.jpda.visual.spi.ComponentInfo;

/**
 *
 * @author Martin Entlicher
 * @author Jaroslav Bachorik
 */
public abstract class ComponentBreakpoint extends Breakpoint {
    public static final int TYPE_ADD = 1;
    public static final int TYPE_REMOVE = 2;
    public static final int TYPE_SHOW = 4;
    public static final int TYPE_HIDE = 8;
    public static final int TYPE_REPAINT = 16;
    
    public static final String PROP_TYPE = "type";  // NOI18N
    
    private ComponentDescription component;
    private int type = 15;
    private boolean enabled = true;
    //private DebuggerManagerListener serviceBreakpointListener;
    //private DebuggerManagerListener serviceBreakpointListenerWeak;
    //private final Map<Session, AWTComponentBreakpointImpl> impls =
    //        new HashMap<Session, AWTComponentBreakpointImpl>();
    private String condition = "";
    private int suspend;
    private String printText;
    
    public ComponentBreakpoint(ComponentDescription component) {
        this.component = component;
        /*serviceBreakpointListener = new ServiceBreakpointListener();
        serviceBreakpointListenerWeak = WeakListeners.create(
                DebuggerManagerListener.class,
                serviceBreakpointListener,
                DebuggerManager.getDebuggerManager());
        DebuggerManager.getDebuggerManager().addDebuggerListener(serviceBreakpointListenerWeak);
        Session[] sessions = DebuggerManager.getDebuggerManager().getSessions();
        for (Session s : sessions) {
            serviceBreakpointListener.sessionAdded(s);
        }*/
        suspend = LineBreakpoint.create("", 0).getSuspend();
    }
    
    public ComponentDescription getComponent() {
        return component;
    }
    
    public int getType() {
        return type;
    }
    
    public void setType(int type) {
        int oldType = this.type;
        this.type = type;
        firePropertyChange(PROP_TYPE, oldType, type);
    }
    
    public abstract int supportedTypes();
    
    /*private void notifyRemoved() {
        DebuggerManager.getDebuggerManager().removeDebuggerListener(serviceBreakpointListenerWeak);
        Collection<AWTComponentBreakpointImpl> cbImpls;
        synchronized(impls) {
            cbImpls = new ArrayList<AWTComponentBreakpointImpl>(impls.values());
            impls.clear();
        }
        for (AWTComponentBreakpointImpl impl : cbImpls) {
            impl.notifyRemoved();
        }
    }*/

    @Override
    public boolean isEnabled () {
        return enabled;
    }
    
    @Override
    public void enable () {
        if (enabled) return;
        enabled = true;
        firePropertyChange 
            (PROP_ENABLED, Boolean.FALSE, Boolean.TRUE);
    }

    @Override
    public void disable () {
        if (!enabled) return;
        enabled = false;
        Collection<AWTComponentBreakpointImpl> cbImpls;
        firePropertyChange 
            (PROP_ENABLED, Boolean.TRUE, Boolean.FALSE);
    }
    
    public String getCondition() {
        return condition;
    }
    
    public void setCondition(String condition) {
        this.condition = condition;
    }

    void setSuspend(int suspend) {
        int oldSuspend;
        synchronized (this) {
            oldSuspend = this.suspend;
            if (suspend == oldSuspend) return ;
            this.suspend = suspend;
        }
        firePropertyChange 
            (JPDABreakpoint.PROP_SUSPEND, oldSuspend, suspend);
    }
    
    int getSuspend() {
        return suspend;
    }

    void setPrintText(String printText) {
        String old;
        synchronized (this) {
            if (Objects.equals(printText, this.printText)) {
                return;
            }
            old = this.printText;
            this.printText = printText;
        }
        firePropertyChange (JPDABreakpoint.PROP_PRINT_TEXT, old, printText);
    }
    
    //public void set
    
    public static class ComponentDescription {
        
        private Map<JPDADebugger, ObjectReference> components = new WeakHashMap<JPDADebugger, ObjectReference>();
        private ComponentInfo ci;
        
        public ComponentDescription(ComponentInfo ci, JPDADebugger debugger, ObjectReference component) {
            this.ci = ci;
            this.components.put(debugger, component);
        }
        
        public ComponentDescription(String definition) {
            // TODO
        }
        
        public ComponentInfo getComponentInfo() {
            return ci;
        }
        
        public ObjectReference getComponent(JPDADebugger debugger) {
            return components.get(debugger);
        }
    }
    
    /*
    private class ServiceBreakpointListener implements DebuggerManagerListener {

        @Override
        public Breakpoint[] initBreakpoints() {
            return new Breakpoint[] {};
        }

        @Override
        public void breakpointAdded(Breakpoint breakpoint) {
        }

        @Override
        public void breakpointRemoved(Breakpoint breakpoint) {
            if (breakpoint == AWTComponentBreakpoint.this) {
                notifyRemoved();
            }
        }

        @Override
        public void initWatches() {}

        @Override
        public void watchAdded(Watch watch) {}

        @Override
        public void watchRemoved(Watch watch) {}

        @Override
        public void sessionAdded(Session session) {
            JPDADebugger debugger = session.lookupFirst(null, JPDADebugger.class);
            if (debugger != null) {
                AWTComponentBreakpointImpl impl = new AWTComponentBreakpointImpl(AWTComponentBreakpoint.this, debugger);
                synchronized(impls) {
                    impls.put(session, impl);
                }
            }
        }

        @Override
        public void sessionRemoved(Session session) {
            AWTComponentBreakpointImpl cb;
            synchronized(impls) {
                cb = impls.remove(session);
            }
            if (cb != null) {
                cb.notifyRemoved();
            }
        }

        @Override
        public void engineAdded(DebuggerEngine engine) {}

        @Override
        public void engineRemoved(DebuggerEngine engine) {}

        @Override
        public void propertyChange(PropertyChangeEvent evt) {}
        
    }
     */

}
