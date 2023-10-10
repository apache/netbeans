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

package org.netbeans.modules.ide.ergonomics.newproject;

import java.io.IOException;
import java.util.Set;
import org.netbeans.modules.ide.ergonomics.fod.FindComponentModules;
import org.netbeans.modules.ide.ergonomics.fod.ModulesActivator;
import org.netbeans.modules.ide.ergonomics.fod.ModulesInstaller;
import java.awt.Component;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.swing.JComponent;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.autoupdate.UpdateElement;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.modules.ide.ergonomics.fod.FeatureInfo;
import org.netbeans.modules.ide.ergonomics.fod.FeatureManager;
import org.netbeans.modules.ide.ergonomics.fod.FoDLayersProvider;
import org.openide.WizardDescriptor;
import org.openide.WizardDescriptor.Panel;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.TemplateWizard;
import org.openide.util.Exceptions;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

public class EnableStep implements WizardDescriptor.FinishablePanel<WizardDescriptor> {
    private InstallPanel component;
    private Collection<UpdateElement> forEnable = null;
    private final List<ChangeListener> listeners = new ArrayList<ChangeListener> ();
    private WizardDescriptor wd = null;
    private boolean doEnableRunning = false;

    public Component getComponent () {
        if (component == null) {
            doEnableRunning = false;
            component = new InstallPanel (
                    NbBundle.getMessage (EnableStep.class, "EnablePanel_Name"));
        }
        return component;
    }

    public HelpCtx getHelp () {
        return HelpCtx.DEFAULT_HELP;
    }

    public boolean isValid () {
        return false;
    }

    public synchronized void addChangeListener (ChangeListener l) {
        listeners.add(l);
    }

    public synchronized void removeChangeListener (ChangeListener l) {
        listeners.remove(l);
    }

    private Runnable doEnable (final FeatureInfo info) {
        return new Runnable () {
            public void run () {
                if (! doEnableRunning) {
                    doEnableRunning = true;
                    FindComponentModules find = new FindComponentModules(info);
                    ModulesActivator activator = new ModulesActivator(forEnable, find);
                    ProgressHandle enableHandle = ProgressHandleFactory.createHandle (
                            getBundle ("ModulesStep_Enable",
                            presentElementsForEnable (find)));
                    JComponent enableComponent = ProgressHandleFactory.createProgressComponent (enableHandle);
                    JComponent enableMainLabel = ProgressHandleFactory.createMainLabelComponent (enableHandle);
                    JComponent enableDetailLabel = ProgressHandleFactory.createDetailLabelComponent (enableHandle);
                    activator.assignEnableHandle (enableHandle);
                    component.displayEnableTask (
                            enableMainLabel,
                            enableDetailLabel,
                            enableComponent);
                    RequestProcessor.Task enable = activator.getEnableTask ();
                    enable.schedule (0);
                    enable.waitFinished ();
                    FoDLayersProvider.getInstance().refreshForce();
                    waitForDelegateWizard ();
                }
            }
        };
    }

    private void fireChange () {
        ChangeEvent e = new ChangeEvent (this);
        List<ChangeListener> templist;
        synchronized (this) {
            templist = new ArrayList<ChangeListener> (listeners);
        }
        for (ChangeListener l : templist) {
            l.stateChanged (e);
        }
    }

    @SuppressWarnings ("unchecked")
    public void readSettings (WizardDescriptor settings) {
        this.wd = settings;
        Object o = settings.getProperty (FeatureOnDemandWizardIterator.CHOSEN_ELEMENTS_FOR_ENABLE);
        assert o == null || o instanceof Collection :
            o + " is instanceof Collection<UpdateElement> or null.";
        forEnable = ((Collection<UpdateElement>) o);
        Object templateO = settings.getProperty (FeatureOnDemandWizardIterator.CHOSEN_TEMPLATE);
        assert templateO instanceof FileObject : templateO + " is not null and instanceof FileObject.";
        FileObject templateFO = (FileObject) templateO;
        FeatureInfo info = FoDLayersProvider.getInstance().whichProvides(templateFO);
        RequestProcessor.Task t = FeatureManager.getInstance().create(doEnable(info));
        t.schedule(0);
    }

    public void storeSettings (WizardDescriptor settings) {
    }

    public boolean isFinishPanel () {
        return false;
    }
    
    private String presentElementsForEnable (FindComponentModules find) {
        assert forEnable != null : "UpdateElements for enable are " + forEnable;
        Collection<UpdateElement> visible = find.getVisibleUpdateElements (forEnable);
        return ModulesInstaller.presentUpdateElements (visible);
    }
    
    private static String getBundle (String key, Object... params) {
        return NbBundle.getMessage (EnableStep.class, key, params);
    }
    
    private FileObject fo = null;

    public static WizardDescriptor.InstantiatingIterator<?> readWizard(FileObject fo) {
        Object o = fo.getAttribute ("instantiatingIterator");
        if (o == null) {
            o = fo.getAttribute ("templateWizardIterator");
        }
        if (o instanceof WizardDescriptor.InstantiatingIterator) {
            // OK
        } else if (o instanceof TemplateWizard.Iterator) {
            final TemplateWizard.Iterator it = (TemplateWizard.Iterator) o;
            o = new WizardDescriptor.InstantiatingIterator<WizardDescriptor>() {
                private TemplateWizard tw;

                public Set instantiate() throws IOException {
                    return it.instantiate(tw);
                }
                public void initialize(WizardDescriptor wizard) {
                    tw = (TemplateWizard)wizard;
                    it.initialize(tw);
                }

                public void uninitialize(WizardDescriptor wizard) {
                    it.uninitialize((TemplateWizard)wizard);
                    tw = null;
                }

                public Panel<WizardDescriptor> current() {
                    return it.current();
                }

                public String name() {
                    return it.name();
                }

                public boolean hasNext() {
                    return it.hasNext();
                }

                public boolean hasPrevious() {
                    return it.hasPrevious();
                }

                public void nextPanel() {
                    it.nextPanel();
                }

                public void previousPanel() {
                    it.previousPanel();
                }

                public void addChangeListener(ChangeListener l) {
                    it.addChangeListener(l);
                }

                public void removeChangeListener(ChangeListener l) {
                    it.removeChangeListener(l);
                }
            };
        }

        if (o == null) {
            return null;
        }

        assert o instanceof WizardDescriptor.InstantiatingIterator :
            o + " is not null and instanceof WizardDescriptor.InstantiatingIterator";
        return (WizardDescriptor.InstantiatingIterator<?>)o;
    }
    
    private void waitForDelegateWizard () {
        Object o = wd.getProperty (FeatureOnDemandWizardIterator.CHOSEN_TEMPLATE);
        assert o instanceof FileObject :
            o + " is not null and instanceof FileObject";
        final String templateResource = ((FileObject) o).getPath ();
        fo = null;
        for (;;) {
           fo = FileUtil.getConfigFile(templateResource);
           if (fo != null) {
               break;
           }
            try {
                Thread.sleep(100);
            } catch (InterruptedException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        WizardDescriptor.InstantiatingIterator iterator = readWizard(fo);
        iterator.initialize (wd);
        wd.putProperty (FeatureOnDemandWizardIterator.DELEGATE_ITERATOR, iterator);
        fireChange ();
    }
    
}
