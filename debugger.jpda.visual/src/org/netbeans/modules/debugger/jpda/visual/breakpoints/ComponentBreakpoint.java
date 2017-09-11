/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.debugger.jpda.visual.breakpoints;

import com.sun.jdi.ObjectReference;
import java.util.Collection;
import java.util.Map;
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
abstract public class ComponentBreakpoint extends Breakpoint {
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
    
    abstract public int supportedTypes();
    
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
            if (printText == this.printText || (printText != null && printText.equals(this.printText))) {
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
