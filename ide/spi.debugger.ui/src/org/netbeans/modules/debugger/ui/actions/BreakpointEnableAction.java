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

package org.netbeans.modules.debugger.ui.actions;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeListener;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import javax.swing.Action;
import javax.swing.JMenuItem;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.BooleanStateAction;

import org.netbeans.api.debugger.Breakpoint;
import org.netbeans.spi.debugger.ui.BreakpointAnnotation;
import org.openide.text.Annotation;
import org.openide.util.ContextAwareAction;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.actions.Presenter;
import org.openide.util.actions.SystemAction;

/**
 * Enables or disables breakpoints.
 *
 * @author Martin Entlicher
 */
public class BreakpointEnableAction extends BooleanStateAction implements ContextAwareAction {
        
    @Override
    public boolean isEnabled() {
        return false;
    }

    public String getName() {
        return NbBundle.getMessage(BreakpointEnableAction.class, "CTL_enabled");
    }
    
    public HelpCtx getHelpCtx() {
        return null;
    }
    
    public Action createContextAwareInstance(Lookup actionContext) {
        Collection<? extends BreakpointAnnotation> ann = actionContext.lookupAll(BreakpointAnnotation.class);
        if (!ann.isEmpty()) {
            BreakpointAwareAction a = new BreakpointAwareAction(ann);
            return a;
        } else {
            return this;
        }
    }
    
    private class BreakpointAwareAction implements Action, Presenter.Menu, Presenter.Popup {
        
        private Collection<? extends BreakpointAnnotation> ann;
        private HiddenBooleanStateAction hba;
        
        public BreakpointAwareAction(Collection<? extends BreakpointAnnotation> ann) {
            this.ann = ann;
            hba = SystemAction.get(HiddenBooleanStateAction.class);
        }

        public Object getValue(String key) {
            return hba.getValue(key);
        }

        public void putValue(String key, Object value) {
            hba.putValue(key, value);
        }

        public void setEnabled(boolean b) {
            //BreakpointEnableAction.this.setEnabled(b);
        }

        private void setBreakpoints() {
            Collection<Breakpoint> breakpoints = new ArrayList<Breakpoint>(ann.size());
            for (BreakpointAnnotation an : ann) {
                breakpoints.add(an.getBreakpoint());
            }
            hba.setFor(breakpoints);
        }

        public boolean isEnabled() {
            setBreakpoints();
            return true;
        }

        public void addPropertyChangeListener(PropertyChangeListener listener) {
            hba.addPropertyChangeListener(listener);
        }

        public void removePropertyChangeListener(PropertyChangeListener listener) {
            hba.removePropertyChangeListener(listener);
        }

        public void actionPerformed(ActionEvent e) {
            //perform();
        }

        public JMenuItem getMenuPresenter() {
            setBreakpoints();
            return hba.getMenuPresenter();
        }

        public JMenuItem getPopupPresenter() {
            setBreakpoints();
            return hba.getPopupPresenter();
        }
        
    }
    
    private static class HiddenBooleanStateAction extends BooleanStateAction {
        
        private Collection<Reference<Breakpoint>> bRefs;// = new WeakReference<Breakpoint>(null);
        
        public HiddenBooleanStateAction() {
            setEnabled(true);
        }
        
        public void setFor(Collection<Breakpoint> breakpoints) {
            bRefs = new ArrayList<Reference<Breakpoint>>(breakpoints.size());
            for (Breakpoint b : breakpoints) {
                bRefs.add(new WeakReference<Breakpoint>(b));
            }
            setBooleanState(areBreakpointsEnabled());
        }

        private boolean areBreakpointsEnabled() {
            boolean isEnabled = true;
            for (Reference<Breakpoint> br : bRefs) {
                Breakpoint b = br.get();
                if (b != null && !b.isEnabled()) {
                    isEnabled = false;
                }
            }
            return isEnabled;
        }

        @Override
        public String getName() {
            return NbBundle.getMessage(BreakpointEnableAction.class, "CTL_enabled");
        }

        @Override
        public HelpCtx getHelpCtx() {
            return null;
        }
        
        @Override
        public void actionPerformed(ActionEvent ev) {
            super.actionPerformed(ev);
            boolean enabled = !areBreakpointsEnabled();
            for (Reference<Breakpoint> br : bRefs) {
                Breakpoint b = br.get();
                if (b != null) {
                    if (enabled) {
                        b.enable();
                    } else {
                        b.disable();
                    }
                }
            }
        }
        
    }
    
}
