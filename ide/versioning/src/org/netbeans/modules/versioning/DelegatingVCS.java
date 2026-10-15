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
package org.netbeans.modules.versioning;

import java.awt.Image;
import org.netbeans.modules.versioning.core.util.VCSSystemProvider;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Action;
import org.netbeans.modules.versioning.core.util.Utils;
import org.netbeans.modules.versioning.core.spi.VCSAnnotator;
import org.netbeans.modules.versioning.core.spi.VCSInterceptor;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;
import org.netbeans.modules.versioning.core.spi.VCSHistoryProvider;
import org.netbeans.modules.versioning.core.spi.*;
import org.netbeans.modules.versioning.spi.VersioningSupport;
import org.netbeans.spi.queries.CollocationQueryImplementation;
import org.netbeans.spi.queries.CollocationQueryImplementation2;
import org.openide.util.ContextAwareAction;
import org.openide.util.Utilities;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author Tomas Stupka
 */
public class DelegatingVCS extends org.netbeans.modules.versioning.core.spi.VersioningSystem implements VCSSystemProvider.VersioningSystem<org.netbeans.modules.versioning.spi.VersioningSystem> {

    private final Map<?, ?> map;
    private org.netbeans.modules.versioning.spi.VersioningSystem delegate;
    private Set<String> metadataFolderNames;
    private final Object DELEGATE_LOCK = new Object();
    
    private final String displayName;
    private final String menuLabel;
    
    private static final Logger LOG = Logger.getLogger(DelegatingVCS.class.getName());
    
    private final PropertyChangeSupport support = new PropertyChangeSupport(this);
    
    /**
     * Caches folders known as having no metadata according to {@link #getMetadataFolderNames()}. 
     * Will be flushed at delegate instantiation.
     */
    private final Set<VCSFileProxy> unversionedParents = Collections.synchronizedSet(new HashSet<VCSFileProxy>(20));
    
    public static DelegatingVCS create(Map<?, ?> map) {
        return new DelegatingVCS(map);
    }
    private VCSAnnotator annotator;
    private VCSVisibilityQuery visibilityQuery;
    private VCSInterceptor interceptor;
    private CollocationQueryImplementation2 collocationQuery;
    private VCSHistoryProvider historyProvider;
    
    private DelegatingVCS(Map<?, ?> map) {
        this.map = map;
        this.displayName = (String) map.get("displayName");
        this.menuLabel = (String) map.get("menuLabel");
        
        LOG.log(Level.FINE, "Created DelegatingVCS for : {0}", map.get("displayName")); // NOI18N
    }

    public DelegatingVCS(org.netbeans.modules.versioning.spi.VersioningSystem vs) {
        this.map = null;
        this.displayName = (String) vs.getProperty(org.netbeans.modules.versioning.spi.VersioningSystem.PROP_DISPLAY_NAME);
        this.menuLabel = (String) vs.getProperty(org.netbeans.modules.versioning.spi.VersioningSystem.PROP_MENU_LABEL);
        this.delegate = vs;
        
        LOG.log(Level.FINE, "Created DelegatingVCS for : {0}", displayName); // NOI18N
    }

    @Override
    public org.netbeans.modules.versioning.spi.VersioningSystem getDelegate() {
        synchronized(DELEGATE_LOCK) {
            if(delegate == null) {
                Utils.flushNullOwners();   
                delegate = (org.netbeans.modules.versioning.spi.VersioningSystem) map.get("delegate");                  // NOI18N
                if(delegate != null) {
                    PropertyChangeListener[] listeners = support.getPropertyChangeListeners();
                    for (PropertyChangeListener l : listeners) {
                        delegate.addPropertyChangeListener(l);
                        support.removePropertyChangeListener(l);
                    }
                } else {
                    LOG.log(Level.WARNING, "Couldn't create delegate for : {0}", map.get("displayName")); // NOI18N
                }
                unversionedParents.clear(); // flush cache. its used only if delegate not yet created.
            }
            return delegate;
        }
    }
    
    @Override
    public void getOriginalFile(VCSFileProxy workingCopy, VCSFileProxy originalFile) {
        getDelegate().getOriginalFile(workingCopy.toFile(), originalFile.toFile());
    }

    @Override
    public CollocationQueryImplementation2 getCollocationQueryImplementation() {
        if(collocationQuery == null) {
            collocationQuery = new CollocationQueryImplementation2() {
                private CollocationQueryImplementation cqi = getDelegate().getCollocationQueryImplementation();
                @Override
                public boolean areCollocated(URI uri1, URI uri2) {
                    return cqi != null && cqi.areCollocated(Utilities.toFile(uri1), Utilities.toFile(uri2));
                }
                @Override
                public URI findRoot(URI uri) {
                    File file = cqi != null ? cqi.findRoot(Utilities.toFile(uri)) : null;
                    return file != null ? Utilities.toURI(file) : null;
                }
            };
        } 
        return collocationQuery;        
    }

    @Override
    public VCSFileProxy getTopmostManagedAncestor(VCSFileProxy proxy) {
        if(!isAlive()) {
            if(getMetadataFolderNames().contains(proxy.getName()) && proxy.isDirectory()) {
                if (LOG.isLoggable(Level.FINE)) {
                    LOG.log(
                            Level.FINE, 
                            "will awake VCS {0} because of metadata folder {1}",// NOI18N 
                            new Object[]{displayName, proxy}); 
                }
                return getTopmostManagedAncestorImpl(proxy);
            } 
            if(hasMetadata(proxy)) {
                if (LOG.isLoggable(Level.FINE)) {
                    LOG.log(
                            Level.FINE, 
                            "will awake VCS {0} because {1} contains matadata",     // NOI18N
                            new Object[]{displayName, proxy});
                }
                return getTopmostManagedAncestorImpl(proxy);
            }
        } else {
            return getTopmostManagedAncestorImpl(proxy);
        }
        return null;
    }

    @Override
    public boolean isLocalHistory() {
        if(!isAlive()) {
            return false;
        }
        return getDelegate().getProperty(org.netbeans.modules.versioning.spi.VersioningSystem.PROP_LOCALHISTORY_VCS) != null;
    }

    @Override
    public String getDisplayName() {
        return displayName;
    }
    
    @Override
    public String getMenuLabel() {
        return menuLabel;
    }

    @Override
    public final void addPropertyCL(PropertyChangeListener listener) {
        synchronized(DELEGATE_LOCK) {
            if(delegate == null) {
                support.addPropertyChangeListener(listener);
            } else {
                delegate.addPropertyChangeListener(listener);
            }
        }
    }
    
    @Override
    public final void removePropertyCL(PropertyChangeListener listener) {
        synchronized(DELEGATE_LOCK) {
            if(delegate == null) {
                support.removePropertyChangeListener(listener);
            } else {
                delegate.removePropertyChangeListener(listener);
            }
        }
    }
    
    @Override
    public boolean isExcluded(VCSFileProxy file) {
        return VersioningSupport.isExcluded(file.toFile());
    }

    @Override
    public VCSAnnotator getVCSAnnotator() {
        if(annotator == null && getDelegate().getVCSAnnotator() != null) {
            annotator = new VCSAnnotator() {
                @Override
                public String annotateName(String name, org.netbeans.modules.versioning.core.spi.VCSContext context) {
                    assert accept(context);
                    return getDelegate().getVCSAnnotator().annotateName(name, Accessor.IMPL.createVCSContext(context));
                }
                @Override
                public Image annotateIcon(Image icon, org.netbeans.modules.versioning.core.spi.VCSContext context) {
                    assert accept(context);                    
                    return getDelegate().getVCSAnnotator().annotateIcon(icon, Accessor.IMPL.createVCSContext(context));
                }
                @Override
                public Action[] getActions(org.netbeans.modules.versioning.core.spi.VCSContext context, ActionDestination destination) {
                    if(!accept(context)) {
                        return new Action[0];
                    }                    
                    org.netbeans.modules.versioning.spi.VCSAnnotator.ActionDestination ad;
                    switch(destination) {
                        case MainMenu:
                            ad = org.netbeans.modules.versioning.spi.VCSAnnotator.ActionDestination.MainMenu;
                            break;
                        case PopupMenu:
                            ad = org.netbeans.modules.versioning.spi.VCSAnnotator.ActionDestination.PopupMenu;
                            break;
                        default:
                            throw new IllegalStateException();
                    }
                    return getDelegate().getVCSAnnotator().getActions(Accessor.IMPL.createVCSContext(context), ad);
                }
            };
        }
        return annotator;
    }
    
    @Override
    public VCSVisibilityQuery getVisibilityQuery() { 
        if(visibilityQuery == null && getDelegate().getVisibilityQuery() != null) {
            visibilityQuery = new VCSVisibilityQuery() {
                @Override
                public boolean isVisible(VCSFileProxy proxy) {
                    return getDelegate().getVisibilityQuery().isVisible(proxy.toFile());
                }
            };
        }
        return visibilityQuery;
    }
    
    @Override
    public VCSInterceptor getVCSInterceptor() {
        if(interceptor == null && getDelegate().getVCSInterceptor() != null) {
            interceptor = new VCSInterceptor() {

                @Override
                public boolean isMutable(VCSFileProxy file) {
                    return getDelegate().getVCSInterceptor().isMutable(file.toFile());
                }

                @Override
                public Object getAttribute(VCSFileProxy file, String attrName) {
                    return getDelegate().getVCSInterceptor().getAttribute(file.toFile(), attrName);
                }

                @Override
                public boolean beforeDelete(VCSFileProxy file) {
                    return getDelegate().getVCSInterceptor().beforeDelete(file.toFile());
                }

                @Override
                public void doDelete(VCSFileProxy file) throws IOException {
                    getDelegate().getVCSInterceptor().doDelete(file.toFile());
                }

                @Override
                public void afterDelete(VCSFileProxy file) {
                    getDelegate().getVCSInterceptor().afterDelete(file.toFile());
                }

                @Override
                public boolean beforeMove(VCSFileProxy from, VCSFileProxy to) {
                    return getDelegate().getVCSInterceptor().beforeMove(from.toFile(), to.toFile());
                }

                @Override
                public void doMove(VCSFileProxy from, VCSFileProxy to) throws IOException {
                    getDelegate().getVCSInterceptor().doMove(from.toFile(), to.toFile());
                }

                @Override
                public void afterMove(VCSFileProxy from, VCSFileProxy to) {
                    getDelegate().getVCSInterceptor().afterMove(from.toFile(), to.toFile());
                }

                @Override
                public boolean beforeCopy(VCSFileProxy from, VCSFileProxy to) {
                    return getDelegate().getVCSInterceptor().beforeCopy(from.toFile(), to.toFile());
                }

                @Override
                public void doCopy(VCSFileProxy from, VCSFileProxy to) throws IOException {
                    getDelegate().getVCSInterceptor().doCopy(from.toFile(), to.toFile());
                }

                @Override
                public void afterCopy(VCSFileProxy from, VCSFileProxy to) {
                    getDelegate().getVCSInterceptor().afterCopy(from.toFile(), to.toFile());
                }

                @Override
                public boolean beforeCreate(VCSFileProxy file, boolean isDirectory) {
                    return getDelegate().getVCSInterceptor().beforeCreate(file.toFile(), isDirectory);
                }

                @Override
                public void doCreate(VCSFileProxy file, boolean isDirectory) throws IOException {
                    getDelegate().getVCSInterceptor().doCreate(file.toFile(), isDirectory);
                }

                @Override
                public void afterCreate(VCSFileProxy file) {
                    getDelegate().getVCSInterceptor().afterCreate(file.toFile());
                }

                @Override
                public void afterChange(VCSFileProxy file) {
                    getDelegate().getVCSInterceptor().afterChange(file.toFile());
                }

                @Override
                public void beforeChange(VCSFileProxy file) {
                    getDelegate().getVCSInterceptor().beforeChange(file.toFile());
                }

                @Override
                public void beforeEdit(VCSFileProxy file) {
                    getDelegate().getVCSInterceptor().beforeEdit(file.toFile());
                }

                @Override
                public long refreshRecursively(VCSFileProxy dir, long lastTimeStamp, List<? super VCSFileProxy> children) {
                    List<File> files = new ArrayList<File>(children.size());
                    for (Iterator<? super VCSFileProxy> it = children.iterator(); it.hasNext();) {
                        VCSFileProxy fileProxy = (VCSFileProxy) it.next();
                        files.add(fileProxy.toFile());
                    }
                    long ts = getDelegate().getVCSInterceptor().refreshRecursively(dir.toFile(), lastTimeStamp, files);
                    if (ts != -1) {
                        children.clear();
                        for (File f : files) {
                            children.add(VCSFileProxy.createFileProxy(f));
                        }
                    }
                    return ts;
                }
            };
        }
        return interceptor;
    }

    @Override
    public VCSHistoryProvider getVCSHistoryProvider() {
        if(historyProvider == null && getDelegate().getVCSHistoryProvider() != null) {
            historyProvider = new DelegatingHistoryProvider();
        }
        return historyProvider;
    }
    
    @Override
    public boolean accept(VCSContext ctx) {
        Set<VCSFileProxy> roots = ctx.getRootFiles();
        for (VCSFileProxy root : roots) {
            if(!accept(root)) {
                return false;
            }
        }
        return true;
    }

    private boolean accept(VCSFileProxy root) {
        return root.toFile() != null;
    }   
    
    @Override
    public boolean isMetadataFile(VCSFileProxy file) {
        if(map == null) {
            return false;
        }
        return getMetadataFolderNames().contains(file.getName());
    }

    private Collection<String> getMetadataFolderNames() {
        if(metadataFolderNames == null) {
            metadataFolderNames = new HashSet<String>();
            int i = 0;
            while(true) {
                String name = (String) map.get("metadataFolderName" + i++);
                if(name == null) {
                    break;
                }
                name = parseName(name);
                if(name == null) {
                    continue;
                }
                metadataFolderNames.add(name);
            }
        }
        return metadataFolderNames;
    }
    
    private Action[] getActions(org.netbeans.modules.versioning.core.spi.VCSContext ctx, VCSAnnotator.ActionDestination actionDestination) {       
        if(map == null || isAlive()) {
            VCSAnnotator tmp = getVCSAnnotator();
            return tmp != null ? tmp.getActions(ctx, actionDestination) : new Action[0];
        } else {
            Action[] ia = getInitActions(ctx);
            Action[] ga = getGlobalActions(ctx);
            
            List<Action> l = new ArrayList<Action>(ia.length + ga.length + 1); // +1 if separator needed
            
            // init actions
            l.addAll(Arrays.asList(ia));
            // add separator if necessary 
            if(ga.length > 0 && ia.length > 0 && l.get(l.size() - 1) != null) {
                l.add(null); 
            }
            // global actions
            l.addAll(Arrays.asList(ga));
            
            return  l.toArray(new Action[0]);
        }        
    }
    
    // package private due unit tests
    Action[] getGlobalActions(org.netbeans.modules.versioning.core.spi.VCSContext ctx) {
        assert !isAlive();
        String category = (String) map.get("actionsCategory");              // NOI18N
        List<? extends Action> l = Utilities.actionsForPath("Versioning/" + category + "/Actions/Global"); // NOI18N
        List<Action> ret = new ArrayList<Action>(l.size());
        for (Action action : l) {
            if(action instanceof ContextAwareAction) {
                ret.add(((ContextAwareAction)action).createContextAwareInstance(Lookups.singleton(ctx)));
            } else {
                ret.add(action);
            }
        }        
        return ret != null ? ret.toArray(new Action[0]) : new Action[0];
    }
    
    // package private due unit tests
    Action[] getInitActions(org.netbeans.modules.versioning.core.spi.VCSContext ctx) {
        String category = (String) map.get("actionsCategory");              // NOI18N
        List<? extends Action> l = Utilities.actionsForPath("Versioning/" + category + "/Actions/Unversioned"); // NOI18N
        List<Action> ret = new ArrayList<Action>(l.size());
        for (Action action : l) {
            if(action instanceof ContextAwareAction) {
                ret.add(((ContextAwareAction)action).createContextAwareInstance(Lookups.singleton(ctx)));
            } else {
                ret.add(action);
            }
        }
        return ret.toArray(new Action[0]);
    }

    private boolean isAlive() {
        synchronized(DELEGATE_LOCK) {
            return delegate != null;
        }
    }
    
    private boolean hasMetadata(VCSFileProxy file) {
        if(file == null) {
            return false;
        }
        
        if (LOG.isLoggable(Level.FINE)) {
            LOG.log(Level.FINE, "looking up metadata for {0}", new Object[] { file });
        }
        if(unversionedParents.contains(file)) {
            LOG.fine(" cached as unversioned");
            return false;
        }
        
        boolean ret = false;
        Set<VCSFileProxy> done = new HashSet<VCSFileProxy>();
        for(String folderName : getMetadataFolderNames()) {
            VCSFileProxy parent;
            if(file.isDirectory()) {
                parent = file;
            } else {
                parent = file.getParentFile();
            }
            while(parent != null) {
                
                if(unversionedParents.contains(parent)) {
                    if (LOG.isLoggable(Level.FINE)) {
                        LOG.log(Level.FINE, " already known as unversioned {0}", new Object[] { file });
                    }
                    break;
                }
                
                // is the folder a special one where metadata should not be looked for?
                boolean forbiddenFolder = Utils.isForbiddenFolder(parent);
                final boolean metadataFolder = !forbiddenFolder && VCSFileProxy.createFileProxy(parent, folderName).exists();
                if(metadataFolder) {
                    if (LOG.isLoggable(Level.FINER)) {
                        LOG.log(
                                Level.FINER, 
                                "found metadata folder {0} for file {1}",           // NOI18N
                                new Object[]{metadataFolder, file});
                    }
                    
                    ret = true;
                } else {
                    done.add(parent);
                }
                parent = parent.getParentFile();
            }
        }
        
        if(!ret) {
            LOG.log(Level.FINE, " storing unversioned");
            unversionedParents.addAll(done);
        }
        return ret;
    }
    
    /**
     * Testing purposes only!
     */
    void reset() {
        if(map != null) {
            synchronized(DELEGATE_LOCK) {
                delegate = null;
            }
        }
    }

    private String parseName(String name) {
        if(name == null) {
            return null;
        }
        int idx = name.indexOf(":");
        
        if(idx < 0) {
            return name;
        }
        
        String cmd[] = name.split(":");
        
        // "_svn:getenv:SVN_ASP_DOT_NET_HACK:notnull"
        if(cmd.length != 4 || !cmd[1].contains("getenv")) {
            return name;
        } else {
            assert cmd[3].equals("notnull") || cmd[3].equals("null");
            
            boolean notnull = cmd[3].trim().equals("notnull");
            if(notnull) {
                return System.getenv(cmd[2]) != null ? cmd[0] : null;
            } else {
                return System.getenv(cmd[2]) == null ? cmd[0] : null;
            }
        }
    }

    private VCSFileProxy getTopmostManagedAncestorImpl(VCSFileProxy proxy) {
        File file = proxy.toFile();
        if(file == null) {
            return null;
        }                
        File f = getDelegate().getTopmostManagedAncestor(file);
        if(f != null) {
            return VCSFileProxy.createFileProxy(f);
        }
        return null;
    }
    
    private class DelegatingHistoryProvider implements VCSHistoryProvider {
        @Override
        public void addHistoryChangeListener(final HistoryChangeListener l) {
            getDelegate().getVCSHistoryProvider().addHistoryChangeListener(new DelegateChangeListener(l));
        }

        @Override
        public void removeHistoryChangeListener(final org.netbeans.modules.versioning.core.spi.VCSHistoryProvider.HistoryChangeListener l) {
            getDelegate().getVCSHistoryProvider().removeHistoryChangeListener(new DelegateChangeListener(l));
        }

        @Override
        public HistoryEntry[] getHistory(VCSFileProxy[] proxies, Date fromDate) {
            File[] files = toFiles(proxies);
            final org.netbeans.modules.versioning.spi.VCSHistoryProvider.HistoryEntry[] history = getDelegate().getVCSHistoryProvider().getHistory(files, fromDate);
            if(history == null) {
                return new HistoryEntry[0];
            }
            HistoryEntry[] proxyHistory = new HistoryEntry[history.length];
            for (int i = 0; i < proxyHistory.length; i++) {
                final org.netbeans.modules.versioning.spi.VCSHistoryProvider.HistoryEntry he = history[i];
                proxyHistory[i] = delegateHistoryEntry(proxies, he);
            }
            return proxyHistory;
        }

        @Override
        public Action createShowHistoryAction(VCSFileProxy[] proxies) {
            File[] files = toFiles(proxies);
            return getDelegate().getVCSHistoryProvider().createShowHistoryAction(files);
        }

        private MessageEditProvider delegateMessageEditProvider(final org.netbeans.modules.versioning.spi.VCSHistoryProvider.HistoryEntry he) {
            if(he.canEdit()) {
                return new MessageEditProvider() {
                    @Override
                    public void setMessage(String message) throws IOException {
                        org.netbeans.modules.versioning.spi.VCSHistoryProvider.MessageEditProvider provider = Accessor.IMPL.getMessageEditProvider(he);
                        if(provider != null) {
                            provider.setMessage(message);
                        }
                    }
                };
            }
            return null;
        }

        private RevisionProvider delegateRevisionProvider(final org.netbeans.modules.versioning.spi.VCSHistoryProvider.HistoryEntry he) {
            return new RevisionProvider() {
                @Override
                    public void getRevisionFile(VCSFileProxy originalFile, VCSFileProxy revisionFile) {
                        org.netbeans.modules.versioning.spi.VCSHistoryProvider.RevisionProvider provider = Accessor.IMPL.getRevisionProvider(he);
                        if(provider != null) {
                            File of = originalFile.toFile();
                            File rf = revisionFile.toFile();
                            if(of != null && rf != null) {
                                provider.getRevisionFile(of, rf);
                            }
                        }
                    }
                };
        }

        private ParentProvider delegateParentProvider(final org.netbeans.modules.versioning.spi.VCSHistoryProvider.HistoryEntry he) {
            return new ParentProvider() {
                @Override
                public HistoryEntry getParentEntry(VCSFileProxy file) {
                    org.netbeans.modules.versioning.spi.VCSHistoryProvider.ParentProvider provider = Accessor.IMPL.getParentProvider(he);
                    if(provider != null) {
                        org.netbeans.modules.versioning.spi.VCSHistoryProvider.HistoryEntry he = provider.getParentEntry(file.toFile());
                        if(he != null) {
                            return delegateHistoryEntry(toProxies(he.getFiles()), he);
                        }
                    }
                    return null;
                }
            };
        }

        private HistoryEntry delegateHistoryEntry(VCSFileProxy[] proxies, final org.netbeans.modules.versioning.spi.VCSHistoryProvider.HistoryEntry he) {
            return Utils.createHistoryEntry(
                        proxies, 
                        he.getDateTime(), 
                        he.getMessage(), 
                        he.getUsername(), 
                        he.getUsernameShort(), 
                        he.getRevision(), 
                        he.getRevisionShort(), 
                        he.getActions(), 
                        delegateRevisionProvider(he),
                        delegateMessageEditProvider(he), 
                        delegateParentProvider(he),
                        new Object[] {he});
            }

        private class DelegateChangeListener implements org.netbeans.modules.versioning.spi.VCSHistoryProvider.HistoryChangeListener {
            private final VCSHistoryProvider.HistoryChangeListener delegate;
            public DelegateChangeListener(HistoryChangeListener delegate) {
                this.delegate = delegate;
            }
            @Override
            public void fireHistoryChanged(org.netbeans.modules.versioning.spi.VCSHistoryProvider.HistoryEvent evt) {
                delegate.fireHistoryChanged(new VCSHistoryProvider.HistoryEvent(DelegatingHistoryProvider.this, toProxies(evt.getFiles())));
            }

            @Override
            public boolean equals(Object obj) {
                if(!(obj instanceof DelegateChangeListener)) {
                    return false;
                }
                DelegateChangeListener d = (DelegateChangeListener) obj;
                return d.delegate.equals(delegate);
            }

            @Override
            public int hashCode() {
                int hash = 3;
                hash = 89 * hash + (this.delegate != null ? this.delegate.hashCode() : 0);
                return hash;
            }
            
        }
    }
    private File[] toFiles(VCSFileProxy[] proxies) {
        File[] files = new File[proxies.length];
        for (int i = 0; i < files.length; i++) {
            files[i] = proxies[i].toFile();
            assert files[i] != null;
        }
        return files;
    }
    private VCSFileProxy[] toProxies(File[] files) {
        VCSFileProxy[] proxies = new VCSFileProxy[files.length];
        for (int i = 0; i < files.length; i++) {
            assert files[i] != null;
            if(files[i] != null) {
                proxies[i] = VCSFileProxy.createFileProxy(files[i]);
            }
        }
        return proxies;
    }
    
}
