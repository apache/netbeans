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

package org.netbeans.modules.ide.ergonomics.newproject;

import java.awt.Component;
import java.awt.EventQueue;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import javax.swing.JComponent;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.modules.ide.ergonomics.fod.FoDLayersProvider;
import org.openide.WizardDescriptor;
import org.openide.WizardDescriptor.InstantiatingIterator;
import org.openide.WizardDescriptor.ProgressInstantiatingIterator;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;
import org.openide.util.WeakListeners;

public final class FeatureOnDemandWizardIterator
implements WizardDescriptor.ProgressInstantiatingIterator<WizardDescriptor>, ChangeListener {
    public static final String CHOSEN_ELEMENTS_FOR_INSTALL = "chosen-elements-for-install"; // NOI18N
    public static final String CHOSEN_ELEMENTS_FOR_ENABLE = "chosen-elements-for-enable"; // NOI18N
    public static final String APPROVED_ELEMENTS = "approved-elements"; // NOI18N
    public static final String CHOSEN_TEMPLATE = "chosen-template"; // NOI18N
    public static final String DELEGATE_ITERATOR = "delegate-iterator"; // NOI18N
    
    private WizardDescriptor.InstantiatingIterator delegateIterator;
    private Boolean doEnable = null;
    private FileObject template;
    private boolean autoEnable = true;
    private ChangeListener weakL;
    private ChangeListener listener;
    
    public FeatureOnDemandWizardIterator (FileObject template) {
        this.template = template;
    }
    
    public static WizardDescriptor.InstantiatingIterator newProject (FileObject fo) {
        try {
            WizardDescriptor.InstantiatingIterator it = getRealNewMakeProjectWizardIterator (fo);
            if (it != null) {
                return it;
            }
        } catch (Exception x) {
            // x.printStackTrace ();
        }
        Object obj = fo.getAttribute("templateWizardIterator"); // NOI18N
        if (obj != null) {
            return null;
        }
        return new FeatureOnDemandWizardIterator (fo);
    }
    
    private static WizardDescriptor.InstantiatingIterator getRealNewMakeProjectWizardIterator (FileObject template) {
        WizardDescriptor.InstantiatingIterator res = null;
        if (FoDLayersProvider.getInstance().getDelegateFileSystem (template) != null) {
            return null;
        }
        FileObject fo = FileUtil.getConfigFile(template.getPath ());
        if (fo != null) {
            Object o = fo.getAttribute ("instantiatingIterator");
            if (o == null) {
                o = fo.getAttribute ("templateWizardIterator");
            }
            assert o instanceof InstantiatingIterator :
                o + " is not null and instanceof WizardDescriptor.InstantiatingIterator";
            WizardDescriptor.InstantiatingIterator iterator = (WizardDescriptor.InstantiatingIterator) o;
            if (! FeatureOnDemandWizardIterator.class.equals (o.getClass ())) {
                return iterator;
            }
        }
        return res;
    }

    private List<WizardDescriptor.Panel<WizardDescriptor>> getPanels () {
        assert EventQueue.isDispatchThread();
        if (panels == null) {
            panels = new ArrayList<WizardDescriptor.Panel<WizardDescriptor>> ();
            panels.add (new DescriptionStep ());
            names = new String [] {
                NbBundle.getMessage (FeatureOnDemandWizardIterator.class, "DescriptionStep_Name"),
            
            };
            String[] steps = new String [panels.size ()];
            assert steps.length == names.length : "As same names as steps must be";
            int i = 0;
            for (WizardDescriptor.Panel p : panels) {
                Component c = p.getComponent ();
                // Default step name to component name of panel. Mainly useful
                // for getting the name of the target chooser to appear in the
                // list of steps.
                steps [i] = c.getName ();
                if (c instanceof JComponent) { // assume Swing components
                    JComponent jc = (JComponent) c;
                    // Sets step number of a component
                    jc.putClientProperty ("WizardPanel_contentSelectedIndex", new Integer (i));
                    // Sets steps names for a panel
                    jc.putClientProperty ("WizardPanel_contentData", steps);
                }
                i ++;
            }
        }
        return panels;
    }
    
    private void createPanelsForEnable () {
        if (getPanels() == null) {
            panels = new ArrayList<WizardDescriptor.Panel<WizardDescriptor>> ();
            getPanels().add (new DescriptionStep ());
            getPanels().add (new EnableStep ());
            names = new String [] {
                NbBundle.getMessage (FeatureOnDemandWizardIterator.class, "DescriptionStep_Name"),
                NbBundle.getMessage (FeatureOnDemandWizardIterator.class, "EnableStep_Name"),
            
            };
            String[] steps = new String [getPanels().size ()];
            assert steps.length == names.length : "As same names as steps must be";
            int i = 0;
            for (WizardDescriptor.Panel p : getPanels()) {
                Component c = p.getComponent ();
                // Default step name to component name of panel. Mainly useful
                // for getting the name of the target chooser to appear in the
                // list of steps.
                steps [i] = c.getName ();
                if (c instanceof JComponent) { // assume Swing components
                    JComponent jc = (JComponent) c;
                    // Sets step number of a component
                    jc.putClientProperty ("WizardPanel_contentSelectedIndex", new Integer (i));
                    // Sets steps names for a panel
                    jc.putClientProperty ("WizardPanel_contentData", steps);
                }
                i ++;
            }
        }
    }
    
    public Set/*<FileObject>*/ instantiate() throws IOException {
        if (getDelegateIterator () != null) {
            return getDelegateIterator ().instantiate ();
        }
        return null;
    }
    
    
    public Set instantiate (ProgressHandle handle) throws IOException {
        InstantiatingIterator it = getDelegateIterator ();
        if (it != null) {
            if (it instanceof ProgressInstantiatingIterator) {
                return ((ProgressInstantiatingIterator) getDelegateIterator ()).instantiate (handle);
            } else {
                return getDelegateIterator ().instantiate ();
            }
        }
        return null;
    }
    
    private int index;
    private List<WizardDescriptor.Panel<WizardDescriptor>> panels = null;
    private String [] names;

    private WizardDescriptor wiz;
    
    public void initialize(WizardDescriptor wiz) {
        if (WizardDescriptor.CLOSED_OPTION.equals(wiz.getValue())) {
            autoEnable = false;
        }
        this.wiz = wiz;
        wiz.putProperty (CHOSEN_TEMPLATE, template);
        wiz.putProperty(DELEGATE_ITERATOR, null);
        index = 0;
    }
    public void uninitialize(WizardDescriptor wiz) {
        if (getDelegateIterator () != null) {
            getDelegateIterator ().uninitialize (wiz);
        }
        this.wiz = null;
        panels = null;
    }
    
    @SuppressWarnings ("unchecked")
    public WizardDescriptor.Panel<WizardDescriptor> current () {
        if (getDelegateIterator () != null && getDelegateIterator() != this) {
            return getDelegateIterator ().current ();
        }
        assert getPanels() != null;
        return getPanels().get (index);
    }

    public String name () {
        if (getDelegateIterator () != null) {
            return getDelegateIterator ().name ();
        }
        getPanels();
        return names [index];
    }

    public boolean hasNext () {
        if (getDelegateIterator () != null) {
            return getDelegateIterator ().hasNext ();
        }
        return index < getPanels().size () - 1;
    }

    public boolean hasPrevious () {
        if (getDelegateIterator () != null) {
            return getDelegateIterator ().hasPrevious ();
        }
        return index > 0;
    }

    public void nextPanel () {
        if (getDelegateIterator () != null) {
            if (getDelegateIterator ().hasNext ()) {
                getDelegateIterator ().nextPanel ();
            }
            return ;
        }
        if (!hasNext ()) {
            return;
        }
        index++;
    }

    public void previousPanel () {
        if (getDelegateIterator () != null) {
            if (getDelegateIterator ().hasPrevious ()) {
                getDelegateIterator ().previousPanel ();
            }
            return ;
        }
        if (!hasPrevious ()) {
            return;
        }
        index--;
    }

    // If nothing unusual changes in the middle of the wizard, simply:
    public synchronized void addChangeListener (ChangeListener l) {
        assert listener == null;
        listener= l;
    }

    public synchronized void removeChangeListener (ChangeListener l) {
        if (l == listener) {
            listener = null;
        }
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        ChangeListener l;
        synchronized (this) {
            l = listener;
        }
        if (l != null) {
            l.stateChanged(new ChangeEvent(this));
        }
    }
    
    private WizardDescriptor.InstantiatingIterator getDelegateIterator () {
        if (wiz != null) {
            if (delegateIterator == null) {
                Object o = wiz.getProperty (DELEGATE_ITERATOR);
                assert o == null || o instanceof WizardDescriptor.InstantiatingIterator :
                    o + " is instanceof WizardDescriptor.InstantiatingIterator or null";
                delegateIterator = (WizardDescriptor.InstantiatingIterator) o;
                if (delegateIterator == null) {
                    if (doEnable == null) {
                        o = wiz.getProperty (CHOSEN_ELEMENTS_FOR_ENABLE);
                        if (o != null && ! ((Collection) o).isEmpty ()) {
                            doEnable = Boolean.TRUE;
                            panels = null;
                            createPanelsForEnable ();
                        }
                    }
                } else {
                    this.weakL = WeakListeners.change(this, delegateIterator);
                    delegateIterator.addChangeListener(weakL);
                }
            }
        }
        return delegateIterator;
    }

}
