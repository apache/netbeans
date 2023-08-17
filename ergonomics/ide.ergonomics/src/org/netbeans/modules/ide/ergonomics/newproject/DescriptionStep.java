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

import org.netbeans.modules.ide.ergonomics.fod.FindComponentModules;
import org.netbeans.modules.ide.ergonomics.fod.ModulesInstaller;
import java.awt.Component;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.autoupdate.UpdateElement;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.modules.ide.ergonomics.ServerWizardProviderProxy;
import org.netbeans.modules.ide.ergonomics.fod.ConfigurationPanel;
import org.netbeans.modules.ide.ergonomics.fod.FoDLayersProvider;
import org.netbeans.modules.ide.ergonomics.fod.FeatureInfo;
import org.netbeans.modules.ide.ergonomics.fod.FeatureManager;
import org.netbeans.spi.server.ServerWizardProvider;
import org.openide.WizardDescriptor;
import org.openide.WizardDescriptor.InstantiatingIterator;
import org.openide.WizardDescriptor.Panel;
import org.openide.cookies.InstanceCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.loaders.TemplateWizard;
import org.openide.util.Exceptions;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.Mutex;
import org.openide.util.NbBundle;
import org.openide.util.lookup.Lookups;

// TODO rename? enables modules and there is no description!
public class DescriptionStep implements WizardDescriptor.Panel<WizardDescriptor>, Runnable {

    private ContentPanel panel;
    private Collection<UpdateElement> forEnable = null;
    private final List<ChangeListener> listeners = new ArrayList<>();
    private FeatureInfo info;
    private WizardDescriptor wd;
    private ConfigurationPanel configPanel;
    private final PresentModules findModules = new PresentModules();

    public DescriptionStep() {
    }

    @Override
    public Component getComponent() {
        if (panel == null) {
            configPanel = new ConfigurationPanel(() -> {
                FoDLayersProvider.getInstance().refreshForce();
                waitForDelegateWizard(true);
                return new JLabel(" ");
            });
            panel = new ContentPanel (getBundle ("DescriptionPanel_Name"));
        }
        return panel;
    }

    @Override
    public HelpCtx getHelp() {
        return HelpCtx.DEFAULT_HELP;
    }

    @Override
    public boolean isValid() {
        return false;
    }

    @Override
    public synchronized void addChangeListener(ChangeListener l) {
        listeners.add(l);
    }

    @Override
    public synchronized void removeChangeListener(ChangeListener l) {
        listeners.remove(l);
    }

    private void fireChange () {
        Mutex.EVENT.readAccess(this);
    }
    
    @Override
    public void run() {
        final List<ChangeListener> templist;
        synchronized (this) {
            templist = new ArrayList<> (listeners);
        }
        ChangeEvent e = new ChangeEvent (DescriptionStep.this);
        for (ChangeListener l : templist) {
            l.stateChanged (e);
        }
    }
    
    private final class PresentModules {
        
        private FindComponentModules findComponents;
        private ProgressHandle progress;

        private synchronized void reset() {
            findComponents = null;
        }

        private synchronized void schedule() {
            if (findComponents == null) {
                progress = ProgressHandle.createHandle(getBundle("DescriptionStep_Prepare", info.getClusterName()));
                configPanel.onPrepare(progress);
                progress.start();
                panel.setComponent(configPanel);
                findComponents = new FindComponentModules(info);
                findComponents.onFinished(task -> presentModulesForActivation((FindComponentModules)task));
            }
        }

        private void presentModulesForActivation(FindComponentModules f) {
            progress.finish();
            forEnable = f.getModulesForEnable ();
            final Collection<UpdateElement> toInstall = f.getModulesForInstall();
            final Collection<UpdateElement> elems = f.getModulesForEnable ();
            if (!elems.isEmpty() || f.getIncompleteFeatures().contains(info) || !f.getUpdateErrors().isEmpty()) {
                Collection<UpdateElement> visible = f.getVisibleUpdateElements (elems);
                final String name = ModulesInstaller.presentUpdateElements (visible);
                configPanel.setUpdateErrors(f.getUpdateErrors());
                configPanel.setInfo(info, name, toInstall, f.getMissingModules(info), f.getExtrasToDownload(), f.isDownloadRequired());
                forEnable = elems;
                fireChange ();
            } else {
                panel.setComponent(null);
                FoDLayersProvider.getInstance().refreshForce();
                waitForDelegateWizard (false);
                SwingUtilities.invokeLater(() -> {
                    waitForDelegateWizard(true);
                    fireChange();
                });
            }
        }
    };
    
    private static String getBundle(String key, Object... params) {
        return NbBundle.getMessage(DescriptionStep.class, key, params);
    }

    @Override
    public void readSettings(WizardDescriptor settings) {
        wd = settings;
        Object o = settings.getProperty (FeatureOnDemandWizardIterator.CHOSEN_TEMPLATE);
        assert o instanceof FileObject : o + " is not null and instanceof FileObject.";
        FileObject fileObject = (FileObject) o;
        info = FoDLayersProvider.getInstance ().whichProvides(fileObject);
        assert info != null : "No info for " + fileObject;
        if (settings.getValue() == WizardDescriptor.CANCEL_OPTION ||
            settings.getValue() == WizardDescriptor.CLOSED_OPTION) {
            return;
        }
        findModules.reset();
        findModules.schedule();
    }

    @Override
    public void storeSettings(WizardDescriptor settings) {
        if (forEnable != null && ! forEnable.isEmpty ()) {
            settings.putProperty (FeatureOnDemandWizardIterator.CHOSEN_ELEMENTS_FOR_ENABLE, forEnable);
            fireChange ();
        }
    }

    private void waitForDelegateWizard(final boolean fire) {
        Object o = wd.getProperty (FeatureOnDemandWizardIterator.CHOSEN_TEMPLATE);
        assert o instanceof FileObject :
            o + " is not null and instanceof FileObject";
        String templateResource = ((FileObject) o).getPath ();
        FileObject fo = null;
        WizardDescriptor.InstantiatingIterator<?> iterator = null;
        int i = 0;
        while (fo == null || iterator == null) {
            try {
                FoDLayersProvider.getInstance().refreshForce();
            } catch (Exception ex) {
                Exceptions.printStackTrace(ex);
            }
            // hot-fixed wizard providers - temporary
            if (templateResource.startsWith("Servers/J2eeWizardProvider")) {
                try {
                    ClassLoader loader = Lookup.getDefault().lookup(ClassLoader.class);
                    Class<?> clazz = Class.forName("org.netbeans.modules.j2ee.deployment.plugins.spi.OptionalDeploymentManagerFactory", true, loader);
                    Collection c = Lookups.forPath("J2EE/DeploymentPlugins/" +
                            templateResource.substring(templateResource.indexOf('-') + 1, templateResource.indexOf('.')) + "/").lookupAll(clazz);
                    if (!c.isEmpty()) {
                        Object optFactory = c.iterator().next();
                        Method m = optFactory.getClass().getMethod("getAddInstanceIterator");
                        iterator = (InstantiatingIterator) m.invoke(optFactory);
                        fo = (FileObject) o;
                    }
                } catch (Exception ex) {
                    Exceptions.printStackTrace(ex);
                    break;
                }
            } else if (templateResource.startsWith("Servers/WizardProvider") || templateResource.startsWith("Cloud/WizardProvider")) { // NOI18N
                String resource = templateResource.substring(0, templateResource.indexOf('/') + 1)
                        + templateResource.substring(templateResource.indexOf('-') + 1); // NOI18N
                fo = FileUtil.getConfigFile(resource);
                try {
                    if (fo != null) {
                        Object oo = DataObject.find(fo).getCookie(InstanceCookie.class).instanceCreate();
                        if (oo instanceof ServerWizardProvider && ServerWizardProviderProxy.isReal((ServerWizardProvider)oo)) {
                            iterator = ((ServerWizardProvider)oo).getInstantiatingIterator();
                        }
                    }
                } catch (Exception ex) {
                    Exceptions.printStackTrace(ex);
                    break;
                }
            } else {
                fo = FileUtil.getConfigFile(templateResource);
                iterator = readWizard(fo);
            }
            if (iterator instanceof FeatureOnDemandWizardIterator) {
                Logger LOG = Logger.getLogger(DescriptionStep.class.getName());
                LOG.warning(
                    "There is still wrong iterator " + // NOI18N
                    iterator.getClass().getName() +
                    " for file object " + fo // NOI18N
                );
                FeatureManager.dumpModules(Level.INFO, Level.INFO);
                iterator = null;
                if (++i == 10) {
                    Logger.getLogger(DescriptionStep.class.getName()).severe("Giving up to find iterator for " + fo); // NOI18N
                    Logger.getLogger(DescriptionStep.class.getName()).severe(threadDump()); // NOI18N
                    boolean npe = false;
                    assert npe = true;
                    if (npe) {
                        throw new NullPointerException("No iterator for " + fo); // NOI18N
                    }
                    return; // give up
                }
                LOG.info("Forcing refresh"); // NOI18N
                // force refresh for the filesystem
                FoDLayersProvider.getInstance().refreshForce();
                LOG.info("Done with refresh"); // NOI18N

                FileObject fake = FileUtil.getConfigFile(templateResource);
                if (fake == null) {
                    LOG.warning("no "+ templateResource + " on FoD: " + fake); // NOI18N
                    FileObject p = fo;
                    while (p != null) {
                        LOG.info("  parent: " + p + " children: " + Arrays.asList(p.getChildren())); // NOI18N
                        p = p.getParent();
                    }
                } else {
                    LOG.info("fake found " + fake); // NOI18N
                    LOG.info("its wizard is " + readWizard(fake)); // NOI18N
                }
            }
        }
        if (fire) {
            iterator.initialize (wd);
            wd.putProperty (FeatureOnDemandWizardIterator.DELEGATE_ITERATOR, iterator);
            fireChange ();
        }
    }
    
    public static WizardDescriptor.InstantiatingIterator<?> readWizard(FileObject fo) {
        if (fo == null || !fo.isValid()) {
            return null;
        }

        Object o = fo.getAttribute ("instantiatingIterator");
        if (o == null || (o instanceof FeatureOnDemandWizardIterator)) {
            Object twi = fo.getAttribute ("templateWizardIterator");
            if (twi != null) {
                o = twi;
            }
        }
        if (o instanceof WizardDescriptor.InstantiatingIterator) {
            // OK
        } else if (o instanceof TemplateWizard.Iterator) {
            final TemplateWizard.Iterator it = (TemplateWizard.Iterator) o;
            o = new WizardDescriptor.InstantiatingIterator<WizardDescriptor>() {
                private TemplateWizard tw;

                @Override
                public Set instantiate() throws IOException {
                    return it.instantiate(tw);
                }
                @Override
                public void initialize(WizardDescriptor wizard) {
                    tw = (TemplateWizard)wizard;
                    try {
                        FileObject real = tw.getTemplate().getPrimaryFile();
                        if (!real.isValid()) {
                            real = FileUtil.getConfigFile(real.getPath());
                        }
                        tw.setTemplate(DataObject.find(real));
                        it.initialize(tw);
                    } catch (DataObjectNotFoundException ex) {
                        Logger.getLogger(DescriptionStep.class.getName()).severe(ex.toString());
                    }
                }

                @Override
                public void uninitialize(WizardDescriptor wizard) {
                    it.uninitialize((TemplateWizard)wizard);
                    tw = null;
                }

                @Override
                public Panel<WizardDescriptor> current() {
                    return it.current();
                }

                @Override
                public String name() {
                    return it.name();
                }

                @Override
                public boolean hasNext() {
                    return it.hasNext();
                }

                @Override
                public boolean hasPrevious() {
                    return it.hasPrevious();
                }

                @Override
                public void nextPanel() {
                    it.nextPanel();
                }

                @Override
                public void previousPanel() {
                    it.previousPanel();
                }

                @Override
                public void addChangeListener(ChangeListener l) {
                    it.addChangeListener(l);
                }

                @Override
                public void removeChangeListener(ChangeListener l) {
                    it.removeChangeListener(l);
                }
            };
        }

        assert o instanceof InstantiatingIterator :
            o + " is not null and instanceof WizardDescriptor.InstantiatingIterator";
        return (WizardDescriptor.InstantiatingIterator<?>)o;
    }

    private static String threadDump() {
        Map<Thread, StackTraceElement[]> all = Thread.getAllStackTraces();
        StringBuilder sb = new StringBuilder();
        sb.append("Thread dump:\n"); // NOI18N
        for (Map.Entry<Thread, StackTraceElement[]> entry : all.entrySet()) {
            sb.append(entry.getKey().getName()).append('\n');
            if (entry.getValue() == null) {
                sb.append("  no information\n"); // NOI18N
                continue;
            }
            for (StackTraceElement stackTraceElement : entry.getValue()) {
                sb.append("  ");
                sb.append(stackTraceElement.getClassName()).append('.');
                sb.append(stackTraceElement.getMethodName()).append(':');
                sb.append(stackTraceElement.getLineNumber()).append('\n');
            }
        }
        return sb.toString();
    }
}

