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

package org.netbeans.modules.ide.ergonomics.fod;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.io.InputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Icon;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.xml.parsers.DocumentBuilder;
import org.netbeans.api.autoupdate.UpdateElement;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.spi.project.ProjectFactory;
import org.netbeans.spi.project.ProjectState;
import org.netbeans.spi.project.SubprojectProvider;
import org.netbeans.spi.project.support.LookupProviderSupport;
import org.netbeans.spi.project.ui.ProjectOpenedHook;
import org.netbeans.spi.project.ui.support.UILookupMergerSupport;
import org.openide.filesystems.FileObject;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.RequestProcessor;
import org.openide.util.WeakListeners;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;
import org.openide.util.lookup.ServiceProvider;
import org.w3c.dom.Document;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.spi.project.ui.LogicalViewProvider;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.modules.SpecificationVersion;
import org.openide.nodes.FilterNode;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor.Task;
import org.xml.sax.SAXException;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>, Jirka Rechtacek <jrechtacek@netbeans.org>
 */
@ServiceProvider(service=ProjectFactory.class, position=30000)
public class FeatureProjectFactory
implements ProjectFactory, PropertyChangeListener, Runnable {
    static final Logger LOG = Logger.getLogger("org.netbeans.modules.ide.ergonomics.projects"); // NOI18N

    public FeatureProjectFactory() {
        OpenProjects.getDefault().addPropertyChangeListener(this);
    }
    
    static Icon loadIcon() {
        return ImageUtilities.loadImageIcon(
            "org/netbeans/modules/ide/ergonomics/fod/project.png" // NOI18N
            , false
        );
    }
    
    final static class Data {
        private final boolean deepCheck;
        private final FileObject dir;
        private Map<String,String> data;
        private Map<String,Document> doms;

        public Data(FileObject dir, boolean deepCheck) {
            this.deepCheck = deepCheck;
            this.dir = dir;
        }

        Document dom(String relative) {
            Document doc = doms == null ? null : doms.get(relative);
            if (doc != null) {
                return doc;
            }
            FileObject fo = dir.getFileObject(relative);
            if (fo == null) {
                return null;
            }
            File f = FileUtil.toFile(fo);
            try {
                DocumentBuilder b = DocumentBuilderFactory.newInstance().newDocumentBuilder();
                if (f != null) {
                    doc = b.parse(f);
                } else {
                    InputStream is = fo.getInputStream();
                    doc = b.parse(is);
                }
                if (doms == null) {
                    doms = new HashMap<String,Document>();
                }
                doms.put(relative, doc);
                return doc;
            } catch (ParserConfigurationException parserConfigurationException) {
                LOG.log(Level.WARNING, "Cannot configure XML parser", parserConfigurationException); // NOI18N
            } catch (SAXException sAXException) {
                LOG.log(Level.INFO, "XML broken in " + f, sAXException); // NOI18N
            } catch (Exception any) {
                LOG.log(Level.INFO, "Cannot read " + f, any); // NOI18N
            }
            return null;
        }

        final boolean hasFile(String relative) {
            FileObject d = dir;
            int pos = 0;

            while (relative.startsWith("../", pos) && d != null) {
                d = d.getParent();
                pos += 3;
            }

            return d != null && d.getFileObject(relative) != null;
        }

        final boolean isDeepCheck() {
            return deepCheck;
        }

        @Override
        public String toString() {
            return dir.getPath();
        }

        final synchronized String is(String relative) {
            FileObject prj = dir.getFileObject(relative);
            if (prj == null) {
                return null;
            }

            String content = data == null ? null : data.get(relative);
            if (content != null) {
                return content;
            }

            byte[] arr = new byte[4000];
            int len;
            InputStream is = null;
            try {
                is = prj.getInputStream();
                len = is.read(arr);
                if (len >= 0) {
                    content = new String(arr, 0, len, "UTF-8");
                }
            } catch (IOException ex) {
                LOG.log(Level.FINEST, "exception while reading " + prj, ex); // NOI18N
                len = -1;
            } finally {
                if (is != null) {
                    try {
                        is.close();
                    } catch (IOException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
            }
            LOG.log(Level.FINEST, "    read {0} bytes", len); // NOI18N
            if (len == -1) {
                return null;
            }

            if (data == null) {
                data = new HashMap<String,String>();
            }

            data.put(relative, content);
            return content;
        }
    }


    public boolean isProject(FileObject projectDirectory) {
        Data d = new Data(projectDirectory, false);

        for (FeatureInfo info : FeatureManager.features()) {
            if (!info.isPresent()) {
                continue;
            }
            if (!info.isEnabled() && (info.isProject(d) == 1)) {
                return true;
            }
        }
        return false;
    }

    public Project loadProject(FileObject projectDirectory, ProjectState state) throws IOException {
        Data d = new Data(projectDirectory, true);
        
        FeatureInfo lead = null;
        List<FeatureInfo> additional = new ArrayList<FeatureInfo>();
        int notEnabled = 0;
        for (FeatureInfo info : FeatureManager.features()) {
            if (!info.isPresent()) {
                continue;
            }
            switch (info.isProject(d)) {
                case 0: break;
                case 1:
                    lead = info;
                    if (!info.isEnabled()) {
                        notEnabled++;
                    }
                break;
                case 2:
                    additional.add(info);
                    if (!info.isEnabled()) {
                        notEnabled++;
                    }
                    break;
                default: assert false;
            }
        }
        if (lead == null || notEnabled == 0) {
            return null;
        }

        return new FeatureNonProject(projectDirectory, lead, state, additional);
    }

    @Override
    public void saveProject(Project project) throws IOException, ClassCastException {
    }

    @Override
    public void run() {
        final Project[] toCheck = OpenProjects.getDefault().getOpenProjects();
        checkProjects(toCheck);
    }

    private void checkProjects(final Project[] toCheck) {
        final List<FeatureInfo> additional = new ArrayList<FeatureInfo>();
        FeatureInfo f = null;
        for (Project p : toCheck) {
            Data d = new Data(p.getProjectDirectory(), true);
            for (FeatureInfo info : FeatureManager.features()) {
                switch (info.isProject(d)) {
                    case 0:
                        break;
                    case 1:
                        f = info;
                        break;
                    case 2:
                        f = info;
                        additional.add(info);
                        break;
                    default:
                        assert false;
                }
            }
        }
        if (f != null && !additional.isEmpty()) {
            final FeatureInfo finalF = f;
            final FeatureInfo[] addF = additional.toArray(new FeatureInfo[0]);

            FeatureManager.logUI("ERGO_PROJECT_OPEN", finalF.clusterName);
            FindComponentModules findModules = new FindComponentModules(finalF, addF);
            Collection<UpdateElement> toEnable = findModules.getModulesForEnable();
            if (toEnable != null && !toEnable.isEmpty()) {
                ModulesActivator enabler = new ModulesActivator(toEnable, findModules);
                enabler.getEnableTask().waitFinished();
            }
        }
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if ("willOpenProjects".equals(evt.getPropertyName())) { // NOI18N
            final Object arr = evt.getNewValue();
            if (arr instanceof Project[]) {
                Task t = FeatureManager.getInstance().create(new Runnable() {
                    @Override
                    public void run() {
                        checkProjects((Project[])arr);
                    }
                });
                t.schedule(0);
                t.waitFinished();
            }
        }
        if (OpenProjects.PROPERTY_OPEN_PROJECTS.equals(evt.getPropertyName())) {
            RequestProcessor.Task t = FeatureManager.getInstance().create(this);
            t.schedule(0);
        }
    }

    private static final class FeatureNonProject 
    implements Project, ChangeListener {
        private final FeatureDelegate delegate;
        private final FeatureInfo info;
        private final FeatureInfo[] additional;
        private final Lookup lookup;
        private ProjectState state;
        private volatile String error;
        private final ChangeListener weakL;

        public FeatureNonProject(
            FileObject dir, FeatureInfo info,
            ProjectState state, List<FeatureInfo> additional
        ) {
            this.delegate = new FeatureDelegate(dir, this);
            this.info = info;
            this.additional = additional.toArray(new FeatureInfo[0]);
            this.lookup = Lookups.proxy(delegate);
            this.state = state;
            this.weakL = WeakListeners.change(this, FeatureManager.getInstance());
            FeatureManager.getInstance().addChangeListener(weakL);
        }
        
        public FileObject getProjectDirectory() {
            return delegate.dir;
        }

        public Lookup getLookup() {
            return lookup;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof Project) {
                return ((Project)obj).getProjectDirectory().equals(getProjectDirectory());
            }
            return false;
        }

        @Override
        public int hashCode() {
            return getProjectDirectory().hashCode();
        }

        public void stateChanged(ChangeEvent e) {
            if (info.isEnabled()) {
                switchToReal();
            }
        }
        final void switchToReal() {
            ProjectState s = null;
            synchronized (this) {
                s = state;
                state = null;
            }
            if (s != null) {
                try {
                    s.notifyDeleted();
                    Project p = ProjectManager.getDefault().findProject(getProjectDirectory());
                    if (p == FeatureNonProject.this) {
                        throw new IllegalStateException("New project shall be found! " + p); // NOI18N
                    }
                    delegate.associate(p);
                } catch (IOException ex) {
                    error = ex.getLocalizedMessage();
                } catch (Exception ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }
        
        private final class FeatureOpenHook extends ProjectOpenedHook
        implements Runnable, ProgressMonitor {
            /**
             * The finder instance; valid only for the duration of the task,
             * should be cleared after that.
             */
            private FindComponentModules finder;
            
            @Override
            protected void projectOpened() {
                if (state == null) {
                    return;
                }
                RequestProcessor.Task t = FeatureManager.getInstance().create(this);
                t.schedule(0);
                t.waitFinished ();
                if (error == null) {
                    switchToReal();
                    // make sure support for projects we depend on are also enabled
                    SubprojectProvider sp = getLookup().lookup(SubprojectProvider.class);
                    if (sp != null) {
                        for (Project subP : sp.getSubprojects()) {
                            FeatureNonProject toOpen;
                            toOpen = subP.getLookup().lookup(FeatureNonProject.class);
                            if (toOpen != null) {
                                toOpen.delegate.hook.projectOpened();
                            }
                        }
                    }
                } else {
                    delegate.associate(new BrokenProject(getProjectDirectory(), error));
                }
            }

            @Override
            protected void projectClosed() {
            }

            public void run() {
                FeatureManager.logUI("ERGO_PROJECT_OPEN", info.clusterName);
                error = null;
                FindComponentModules findModules = new FindComponentModules(info, additional);
                synchronized (this) {
                    this.finder = findModules;
                }
                try {
                    Collection<UpdateElement> toInstall = findModules.getModulesForInstall ();
                    Collection<UpdateElement> toEnable = findModules.getModulesForEnable ();
                    if (!findModules.getIncompleteFeatures().isEmpty() && findModules.isDownloadRequired()) {
                        // ignore
                        Collection<FeatureInfo.ExtraModuleInfo> missingModules = new LinkedHashSet<>(findModules.getMissingModules(info));
                        for (FeatureInfo i2 : additional) {
                            missingModules.addAll(findModules.getMissingModules(i2));
                        }
                        StringBuilder sb = new StringBuilder();
                        if (!missingModules.isEmpty()) {
                            for (FeatureInfo.ExtraModuleInfo s : missingModules) {
                                if (sb.length() > 0) {
                                    sb.append(", "); // NOI18N
                                }
                                sb.append(s.displayName());
                            }
                        }
                        error = NbBundle.getMessage(FeatureProjectFactory.class, 
                                "MSG_BrokenAction_FeatureIncomplete", 
                                findModules.getIncompleteFeatures().iterator().next(),
                                sb.toString());
                        return;
                    }
                    if (toInstall != null && ! toInstall.isEmpty ()) {
                        ModulesInstaller installer = new ModulesInstaller(toInstall, findModules, this);
                        installer.getInstallTask ().waitFinished ();
                    }
                    if (toEnable != null && ! toEnable.isEmpty () && error == null) {
                        ModulesActivator enabler = new ModulesActivator (toEnable, findModules, this);
                        enabler.getEnableTask ().waitFinished ();
                    }
                } finally {
                    synchronized (this) {
                        this.finder = null;
                    }
                }
            }

            public void onDownload(ProgressHandle progressHandle) {
            }

            public void onValidate(ProgressHandle progressHandle) {
            }

            public void onInstall(ProgressHandle progressHandle) {
            }

            public void onEnable(ProgressHandle progressHandle) {
            }

            public void onError(String message) {
                synchronized (this) {
                    if (finder.isDownloadRequired()) {
                        error = message;
                    }
                }
            }
        } // end of FeatureOpenHook
    } // end of FeatureNonProject
    private static final class FeatureDelegate 
    implements Lookup.Provider, ProjectInformation, LogicalViewProvider {
        private final FileObject dir;
        private final PropertyChangeSupport support;
        Lookup delegate;
        private final InstanceContent ic = new InstanceContent();
        private final Lookup hooks = new AbstractLookup(ic);
        private final FeatureNonProject.FeatureOpenHook hook;
        private List<RootNode> lvs;


        public FeatureDelegate(FileObject dir, FeatureNonProject feature) {
            this.dir = dir;
            this.hook = feature.new FeatureOpenHook();
            ic.add(UILookupMergerSupport.createProjectOpenHookMerger(hook));
            this.delegate = new ProxyLookup(
                Lookups.fixed(feature, this),
                LookupProviderSupport.createCompositeLookup(
                    hooks, "../nonsence" // NOI18N
                )
            );
            this.support = new PropertyChangeSupport(this);
        }

        public Lookup getLookup() {
            return delegate;
        }


        @Override
        public String getName() {
            ProjectInformation info = delegate.lookup(ProjectInformation.class);
            if (info != null && info != this) {
                return info.getName();
            }
            return dir.getNameExt();
        }

        @Override
        public String getDisplayName() {
            ProjectInformation info = delegate.lookup(ProjectInformation.class);
            if (info != null && info != this) {
                return info.getDisplayName();
            }
            return getName();
        }

        @Override
        public Icon getIcon() {
            ProjectInformation info = delegate.lookup(ProjectInformation.class);
            if (info != null && info != this) {
                return info.getIcon();
            }
            return loadIcon();
        }

        @Override
        public Project getProject() {
            return delegate.lookup(Project.class);
        }

        public void addPropertyChangeListener(PropertyChangeListener listener) {
            support.addPropertyChangeListener(listener);
        }

        public void removePropertyChangeListener(PropertyChangeListener listener) {
            support.removePropertyChangeListener(listener);
        }

        final void associate(Project p) {
            if (p == null) {
                delegate = Lookup.EMPTY;
                return;
            }
            assert dir.equals(p.getProjectDirectory());
            ProjectInformation info = p.getLookup().lookup(ProjectInformation.class);
            if (info != null) {
                for (PropertyChangeListener l : support.getPropertyChangeListeners()) {
                    info.addPropertyChangeListener(l);
                }
            }
            delegate = p.getLookup();
            for (ProjectOpenedHook h : p.getLookup().lookupAll(ProjectOpenedHook.class)) {
                ic.add(h);
            }
            List<RootNode> list = lvs;
            lvs = Collections.emptyList();
            if (list != null) {
                for (RootNode fn : list) {
                    fn.change(delegate);
                }
            }
            support.firePropertyChange(null, null, null);
        }

        public Node createLogicalView() {
            LogicalViewProvider lvp = delegate.lookup(LogicalViewProvider.class);
            if (lvp != null && lvp != this) {
                return lvp.createLogicalView();
            }
            if (lvs == null) {
                lvs = new ArrayList<RootNode>();
            }

            RootNode fn = new RootNode(dir);
            lvs.add(fn);
            return fn;
        }

        public Node findPath(Node root, Object target) {
            LogicalViewProvider lvp = delegate.lookup(LogicalViewProvider.class);
            if (lvp != null && lvp != this) {
                return lvp.findPath(root, target);
            }
            return null;
        }
    }

    private static final class RootNode extends FilterNode {
        public RootNode(FileObject fo) {
            super(DataFolder.findFolder(fo).getNodeDelegate());
        }

        public void change(Lookup l) {
            LogicalViewProvider lvp = l.lookup(LogicalViewProvider.class);
            if (lvp != null) {
                changeOriginal(lvp.createLogicalView(), true);
            }

        }
    }

}
