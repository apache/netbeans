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

package org.netbeans.modules.debugger.ui.actions;

import java.awt.Component;
import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.BeanDescriptor;
import java.beans.BeanInfo;
import java.beans.Customizer;
import java.beans.Introspector;
import java.beans.PropertyChangeListener;
import java.util.Collection;
import javax.swing.Action;
import javax.swing.JMenuItem;

import org.netbeans.api.debugger.Breakpoint;
import org.netbeans.spi.debugger.ui.BreakpointAnnotation;

import org.netbeans.spi.debugger.ui.Controller;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.awt.Actions;
import org.openide.util.ContextAwareAction;
import org.openide.util.Exceptions;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.actions.Presenter.Popup;
import org.openide.util.actions.SystemAction;

/**
 * Customize action for line breakpoint, which is available from the gutter popup.
 *
 * @author Martin Entlicher
 */
public class BreakpointCustomizeAction extends SystemAction implements ContextAwareAction  {
    
    /** Creates a new instance of BreakpointCustomizeAction */
    public BreakpointCustomizeAction() {
        setEnabled(false);
    }

    public String getName() {
        return NbBundle.getMessage(BreakpointCustomizeAction.class, "CTL_customize");
    }

    public HelpCtx getHelpCtx() {
        return null;
    }
    
    public void actionPerformed(ActionEvent ev) {
    }
    
    public Action createContextAwareInstance(Lookup actionContext) {
        Collection<? extends BreakpointAnnotation> ann = actionContext.lookupAll(BreakpointAnnotation.class);
        if (ann.size() == 1) {
            return new BreakpointAwareAction(ann.iterator().next());
        } else {
            //Exceptions.printStackTrace(new IllegalStateException("expecting BreakpointAnnotation object in lookup "+actionContext));
            return this;
        }
    }
    
    private class BreakpointAwareAction implements Action, Popup {
        
        private BreakpointAnnotation ann;
        
        public BreakpointAwareAction(BreakpointAnnotation ann) {
            this.ann = ann;
        }

        public JMenuItem getPopupPresenter() {
            return new Actions.MenuItem (this, false);
        }
    
        public Object getValue(String key) {
            return BreakpointCustomizeAction.this.getValue(key);
        }

        public void putValue(String key, Object value) {
            //BreakpointCustomizeAction.this.putValue(key, value);
        }

        public void setEnabled(boolean b) {
            //BreakpointCustomizeAction.this.setEnabled(b);
        }

        public boolean isEnabled() {
            return getCustomizerClass(ann.getBreakpoint()) != null;
        }

        public void addPropertyChangeListener(PropertyChangeListener listener) {
            BreakpointCustomizeAction.this.addPropertyChangeListener(listener);
        }

        public void removePropertyChangeListener(PropertyChangeListener listener) {
            BreakpointCustomizeAction.this.removePropertyChangeListener(listener);
        }

        public void actionPerformed(ActionEvent e) {
            customize(ann.getBreakpoint());
        }
        
        private BeanInfo findBeanInfo(Class clazz) {
            Class biClass;
            try {
                biClass = Lookup.getDefault().lookup(ClassLoader.class).loadClass(clazz.getName()+"BeanInfo");
            } catch (ClassNotFoundException cnfex) {
                biClass = null;
            }
            if (biClass == null) {
                clazz = clazz.getSuperclass();
                if (clazz != null) {
                    return findBeanInfo(clazz);
                } else {
                    return null;
                }
            } else {
                try {
                    java.lang.reflect.Constructor c = biClass.getConstructor(new Class[0]);
                    c.setAccessible(true);
                    return (BeanInfo) c.newInstance(new Object[0]);
                } catch (Exception ex) {
                    Exceptions.printStackTrace(ex);
                    return null;
                }
            }
        }

        private Class getCustomizerClass(Breakpoint b) {
            BeanInfo bi = findBeanInfo(b.getClass());
            if (bi == null) {
                try {
                    bi = Introspector.getBeanInfo(b.getClass());
                } catch (Exception ex) {
                    Exceptions.printStackTrace(ex);
                    return null;
                }
            }
            BeanDescriptor bd = bi.getBeanDescriptor();
            if (bd == null) return null;
            Class cc = bd.getCustomizerClass();
            return cc;
        }
        
        private Customizer getCustomizer(Breakpoint b) {
            Class cc = getCustomizerClass(b);
            if (cc == null) return null;
            try {
                Customizer c = (Customizer) cc.getDeclaredConstructor().newInstance();
                c.setObject(b);
                return c;
            } catch (Exception ex) {
                Exceptions.printStackTrace(ex);
                return null;
            }
        }

        private void customize(Breakpoint b) {
            Customizer c = getCustomizer(b);
            if (c == null) {
                return;
            }
            
            HelpCtx helpCtx = HelpCtx.findHelp (c);
            if (helpCtx == null) {
                helpCtx = new HelpCtx ("debug.add.breakpoint");  // NOI18N
            }
            final Controller[] cPtr = new Controller[] { null };
            if (c instanceof Controller) {
                //Exceptions.printStackTrace(new IllegalStateException("FIXME: JComponent "+c+" must not implement Controller interface!"));
                cPtr[0] = (Controller) c;
            }
            final DialogDescriptor[] descriptorPtr = new DialogDescriptor[1];
            final Dialog[] dialogPtr = new Dialog[1];
            ActionListener buttonsActionListener = null;
            if (cPtr[0] != null) {
                buttonsActionListener = new ActionListener() {
                    public void actionPerformed(ActionEvent ev) {
                        if (descriptorPtr[0].getValue() == DialogDescriptor.OK_OPTION) {
                            boolean ok = cPtr[0].ok();
                            if (ok) {
                                dialogPtr[0].setVisible(false);
                            }
                        } else {
                            dialogPtr[0].setVisible(false);
                        }
                    }
                };
            }
            DialogDescriptor descriptor = new DialogDescriptor (
                c,
                NbBundle.getMessage (
                    BreakpointCustomizeAction.class,
                    "CTL_Breakpoint_Customizer_Title" // NOI18N
                ),
                true,
                DialogDescriptor.OK_CANCEL_OPTION,
                DialogDescriptor.OK_OPTION,
                DialogDescriptor.DEFAULT_ALIGN,
                helpCtx,
                buttonsActionListener
            );
            if (buttonsActionListener != null) {
                descriptor.setClosingOptions(new Object[] {});
            }
            Dialog d = DialogDisplayer.getDefault ().createDialog (descriptor);
            String accessibleDescription = d.getAccessibleContext().getAccessibleDescription();
            if (accessibleDescription == null) {
                if (c instanceof javax.swing.JComponent) {
                    accessibleDescription = ((javax.swing.JComponent)c).getAccessibleContext().getAccessibleDescription();
                }
                if (accessibleDescription == null) {
                    accessibleDescription = NbBundle.getMessage (
                        BreakpointCustomizeAction.class,
                        "ACSD_Breakpoint_Customizer",
                        b);
                }
                d.getAccessibleContext().setAccessibleDescription(accessibleDescription);
            }
            d.pack ();
            descriptorPtr[0] = descriptor;
            dialogPtr[0] = d;
            d.setVisible (true);
        }

    }

}
