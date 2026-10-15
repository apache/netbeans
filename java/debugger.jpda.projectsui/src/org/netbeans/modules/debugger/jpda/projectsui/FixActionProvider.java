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

package org.netbeans.modules.debugger.jpda.projectsui;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import javax.swing.SwingUtilities;
import org.netbeans.api.debugger.ActionsManager;
import org.netbeans.api.debugger.Breakpoint;
import org.netbeans.api.debugger.DebuggerEngine;
import org.netbeans.api.debugger.DebuggerManagerListener;
import org.netbeans.api.debugger.Session;
import org.netbeans.api.debugger.Watch;
import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.debugger.jpda.projects.FixClassesSupport;
import org.netbeans.modules.debugger.jpda.projects.FixClassesSupport.ClassesToReload;
import org.netbeans.modules.debugger.jpda.projects.SourcePathProviderImpl;
import org.netbeans.spi.debugger.ActionsProvider;
import org.netbeans.spi.debugger.ActionsProviderSupport;
import org.netbeans.spi.debugger.ContextProvider;
import org.netbeans.spi.debugger.jpda.SourcePathProvider;
import org.netbeans.spi.debugger.ui.EditorContextDispatcher;
import org.netbeans.spi.project.ActionProvider;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.NotifyDescriptor;
import org.openide.awt.StatusDisplayer;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.lookup.Lookups;
import org.openide.windows.TopComponent;

/**
*
* @author   Jan Jancura
*/
@ActionsProvider.Registration(path="netbeans-JPDASession", actions="fix")
public class FixActionProvider extends ActionsProviderSupport {

    private JPDADebugger debugger;
    private SourcePathProvider sp;
    private Listener listener;
    private boolean isFixCommandSupported;
    
    
    public FixActionProvider (ContextProvider lookupProvider) {
        debugger = lookupProvider.lookupFirst(null, JPDADebugger.class);
        sp = getSourcePathProvider(lookupProvider);
        
        listener = new Listener ();
        MainProjectManager.getDefault ().addPropertyChangeListener (listener);
        debugger.addPropertyChangeListener (JPDADebugger.PROP_STATE, listener);
        //debugger.addPropertyChangeListener ("classesToReload", listener);
        ClassesToReload.getInstance().addPropertyChangeListener(listener);
        EditorContextDispatcher.getDefault().addPropertyChangeListener("text/x-java", listener);
        
        setEnabled (
            ActionsManager.ACTION_FIX,
            shouldBeEnabled ()
        );
    }
    
    private SourcePathProvider getSourcePathProvider(ContextProvider lookupProvider) {
        List<? extends SourcePathProvider> providers = lookupProvider.lookup(null, SourcePathProvider.class);
        for (SourcePathProvider p : providers) {
            if (p instanceof SourcePathProviderImpl) {
                return p;
            }
        }
        return providers.get(0);
    }

    private void destroy () {
        debugger.removePropertyChangeListener (JPDADebugger.PROP_STATE, listener);
        //debugger.removePropertyChangeListener ("classesToReload", listener);
        ClassesToReload.getInstance().removePropertyChangeListener(listener);
        MainProjectManager.getDefault ().removePropertyChangeListener (listener);
        EditorContextDispatcher.getDefault().removePropertyChangeListener (listener);
    }
    
    @Override
    public Set getActions () {
        return Collections.singleton (ActionsManager.ACTION_FIX);
    }
    
    @Override
    public void doAction (Object action) {
        if (!isFixCommandSupported) {
            Set<FileObject> sourceRootsFO = getSourceRootsFO(sp);
            Map<String, FileObject> classes = ClassesToReload.getInstance().popClassesToReload(debugger, sourceRootsFO);
            FixClassesSupport.reloadClasses(debugger, classes);
            //applyClassesToReload(getCurrentProject());
            return ;
        }
        if (!SwingUtilities.isEventDispatchThread()) {
            try {
                SwingUtilities.invokeAndWait(new Runnable() {
                    @Override
                    public void run() {
                        invokeAction();
                    }
                });
            } catch (InterruptedException iex) {
                // Procceed
            } catch (java.lang.reflect.InvocationTargetException itex) {
                ErrorManager.getDefault().notify(itex);
            }
        } else {
            invokeAction();
        }
    }

    private Set<FileObject> getSourceRootsFO(SourcePathProvider sp) {
        if (sp instanceof SourcePathProviderImpl) {
            return ((SourcePathProviderImpl) sp).getSourceRootsFO();
        } else {
            String[] sourceRoots = sp.getSourceRoots();
            Set<FileObject> fos = new HashSet<FileObject>();
            for (String root : sourceRoots) {
                FileObject fo;
                int jarIndex = root.indexOf("!/");
                if (jarIndex > 0) {
                    fo = FileUtil.toFileObject(new java.io.File(root.substring(0, jarIndex)));
                    fo = fo.getFileObject(root.substring(jarIndex + 2));
                } else {
                    fo = FileUtil.toFileObject(new java.io.File(root));
                }
                if (fo != null) {
                    fos.add(fo);
                }
            }
            return fos;
        }
    }

    private void invokeAction() {
        ((ActionProvider) getCurrentProject().getLookup ().lookup (
                ActionProvider.class
            )).invokeAction (
                JavaProjectConstants.COMMAND_DEBUG_FIX, 
                getLookup ()
            );
    }

    /**
     * Returns the project that the active node's fileobject belongs to. 
     * If this cannot be determined for some reason, returns the main project.
     *  
     * @return the project that the active node's fileobject belongs to
     */ 
    private Project getCurrentProject() {
        Node[] nodes = TopComponent.getRegistry ().getActivatedNodes ();
        if (nodes == null || nodes.length == 0) {
            return MainProjectManager.getDefault().getMainProject();
        }
        DataObject dao = (DataObject) nodes[0].getLookup().lookup(DataObject.class);
        if (dao == null || !dao.isValid()) {
            return MainProjectManager.getDefault().getMainProject();
        }
        return FileOwnerQuery.getOwner(dao.getPrimaryFile());        
    }
    
    private boolean shouldBeEnabled () {
        // check if current debugger supports this action
        if (!debugger.canFixClasses()) {
            return false;
        }
        // check if current project supports this action
        isFixCommandSupported = false;
        Project p = getCurrentProject();
        if (p != null) {
            ActionProvider actionProvider = (ActionProvider) p.getLookup ().
                lookup (ActionProvider.class);
            if (actionProvider != null) {
                String[] sa = actionProvider.getSupportedActions ();
                int i, k = sa.length;
                for (i = 0; i < k; i++) {
                    if (JavaProjectConstants.COMMAND_DEBUG_FIX.equals (sa [i])) {
                        break;
                    }
                }
                isFixCommandSupported = i < k &&
                        actionProvider.isActionEnabled(JavaProjectConstants.COMMAND_DEBUG_FIX, getLookup());
            }
        }
        if (!isFixCommandSupported) {
            // No fix command, let's see whether we have some changed classes to reload:
            return ClassesToReload.getInstance().hasClassesToReload(debugger, getSourceRootsFO(sp));
            /*Sources sources = ProjectUtils.getSources(p);
            SourceGroup[] srcGroups = sources.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
            for (SourceGroup srcGroup : srcGroups) {
                FileObject src = srcGroup.getRootFolder();
                if (hasClassesToReload(debugger, src)) {
                    return true;
                }
            }
            return false;
             */
        } else {
            return true;
        }
    }
    
    private Lookup getLookup () {
        Node[] nodes = TopComponent.getRegistry ().getActivatedNodes ();
        int i, k = nodes.length;
        ArrayList<DataObject> l = new ArrayList<DataObject>();
        for (i = 0; i < k; i++) {
            DataObject dobj = nodes [i].getLookup().lookup (DataObject.class);
            if (dobj != null && dobj.isValid()) {
                l.add (dobj);
            }
        }
        if (l.isEmpty()) {
            FileObject fo = EditorContextDispatcher.getDefault().getMostRecentFile();
            if (fo != null) {
                try {
                    DataObject dobj = DataObject.find(fo);
                    l.add(dobj);
                } catch (DataObjectNotFoundException ex) {}
            }
        }
        return Lookups.fixed ((Object[]) l.toArray (new DataObject [0]));
    }
    
    static void notifyError(String error) {
        NotifyDescriptor nd = new NotifyDescriptor.Message(error, NotifyDescriptor.Message.ERROR_MESSAGE);
        DialogDisplayer.getDefault().notifyLater(nd);
        StatusDisplayer.getDefault().setStatusText(error);
    }

    private class Listener implements PropertyChangeListener, 
    DebuggerManagerListener {
        public Listener () {}
        
        @Override
        public void propertyChange (PropertyChangeEvent e) {
            boolean en = shouldBeEnabled ();
            setEnabled (
                ActionsManager.ACTION_FIX,
                en
            );
            if (debugger.getState () == JPDADebugger.STATE_DISCONNECTED) {
                destroy ();
            }
        }
        @Override
        public void sessionRemoved (Session session) {}
        @Override
        public void breakpointAdded (Breakpoint breakpoint) {}
        @Override
        public void breakpointRemoved (Breakpoint breakpoint) {}
        @Override
        public Breakpoint[] initBreakpoints () {return new Breakpoint [0];}
        @Override
        public void initWatches () {}
        @Override
        public void sessionAdded (Session session) {}
        @Override
        public void watchAdded (Watch watch) {}
        @Override
        public void watchRemoved (Watch watch) {}
        @Override
        public void engineAdded (DebuggerEngine engine) {}
        @Override
        public void engineRemoved (DebuggerEngine engine) {}
    }

}
