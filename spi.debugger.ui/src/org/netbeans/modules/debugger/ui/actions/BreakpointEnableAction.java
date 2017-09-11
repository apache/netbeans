/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
