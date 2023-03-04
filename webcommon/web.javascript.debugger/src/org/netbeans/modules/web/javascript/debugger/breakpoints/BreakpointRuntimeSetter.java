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
package org.netbeans.modules.web.javascript.debugger.breakpoints;

import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.netbeans.api.debugger.Breakpoint;
import org.netbeans.api.debugger.DebuggerEngine;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.LazyActionsManagerListener;
import org.netbeans.api.debugger.LazyDebuggerManagerListener;
import org.netbeans.api.debugger.Session;
import org.netbeans.api.debugger.Watch;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.modules.javascript2.debug.breakpoints.JSLineBreakpoint;
import org.netbeans.modules.web.clientproject.api.WebClientProjectConstants;
import org.netbeans.modules.web.common.api.RemoteFileCache;
import org.netbeans.modules.web.common.sourcemap.SourceMapsScanner;
import org.netbeans.modules.web.common.sourcemap.SourceMapsTranslator;
import org.netbeans.modules.web.javascript.debugger.MiscEditorUtil;
import org.netbeans.modules.web.javascript.debugger.browser.ProjectContext;
import org.netbeans.modules.web.webkit.debugging.api.Debugger;
import org.netbeans.modules.web.webkit.debugging.api.WebKitDebugging;
import org.netbeans.modules.web.webkit.debugging.api.debugger.Script;
import org.netbeans.spi.debugger.ContextProvider;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.RequestProcessor;


/**
 * Responsible for setting breakpoints while debugging.
 * ( Otherwise breakpoints are used that was set before debugger start ).
 * @author ads
 *
 */
@LazyActionsManagerListener.Registration(path="javascript-debuggerengine")
public class BreakpointRuntimeSetter extends LazyActionsManagerListener
                                     implements LazyDebuggerManagerListener,
                                                Debugger.ScriptsListener {

    public static final RequestProcessor RP = new RequestProcessor("Breakpoint updater");
    
    private final Debugger d;
    private final WebKitDebugging wd;
    private final ProjectContext pc;
    private final List<FileObject> projectSourceRoots;
    private final Map<Breakpoint, WebKitBreakpointManager> breakpointImpls = new HashMap<>();
    
    public BreakpointRuntimeSetter(ContextProvider lookupProvider) {
        d = lookupProvider.lookupFirst(null, Debugger.class);
        wd = lookupProvider.lookupFirst(null, WebKitDebugging.class);
        pc = lookupProvider.lookupFirst(null, ProjectContext.class);
        Project prj = pc.getProject();
        if (prj == null) {
            projectSourceRoots = null;
        } else {
            projectSourceRoots = getProjectSourceRoots(prj);
        }
        DebuggerManager.getDebuggerManager().addDebuggerListener(this);
        d.addScriptsListener(this);
        createBreakpointImpls(pc.getProject());
    }
    
    private void createBreakpointImpls(Project p) {
        Breakpoint[] breakpoints = DebuggerManager.getDebuggerManager().getBreakpoints();
        List<WebKitBreakpointManager> toAdd = new ArrayList<>();
        synchronized (breakpointImpls) {
            for (Breakpoint breakpoint : breakpoints) {
                if (breakpoint instanceof JSLineBreakpoint) {
                    JSLineBreakpoint lb = (JSLineBreakpoint) breakpoint;
                    if (acceptBreakpoint(lb) && !breakpointImpls.containsKey(lb)) {
                        WebKitBreakpointManager bm = createWebKitBreakpointManager(lb);
                        breakpointImpls.put(lb, bm);
                        toAdd.add(bm);
                    }
                }
                if (breakpoint instanceof AbstractBreakpoint) {
                    AbstractBreakpoint ab = (AbstractBreakpoint) breakpoint;
                    if (!breakpointImpls.containsKey(ab)) {
                        WebKitBreakpointManager bm = createWebKitBreakpointManager(ab);
                        breakpointImpls.put(ab, bm);
                        toAdd.add(bm);
                    }
                }
            }
        }
        if (!toAdd.isEmpty()) {
            SourceMapsTranslator scannedSMT = SourceMapsScanner.getInstance().scan(p);
            MiscEditorUtil.registerProjectsSourceMapTranslator(d, scannedSMT);
            for (WebKitBreakpointManager bm : toAdd) {
                if (bm.canAdd()) {
                    bm.add();
                }
            }
        }
    }
    
    private WebKitBreakpointManager createWebKitBreakpointManager(JSLineBreakpoint lb) {
        return WebKitBreakpointManager.create(d, pc, lb);
    }
    
    private WebKitBreakpointManager createWebKitBreakpointManager(AbstractBreakpoint ab) {
        if (ab instanceof DOMBreakpoint) {
            return WebKitBreakpointManager.create(wd, pc, (DOMBreakpoint) ab);
        }
        if (ab instanceof EventsBreakpoint) {
            return WebKitBreakpointManager.create(d, (EventsBreakpoint) ab);
        }
        if (ab instanceof XHRBreakpoint) {
            return WebKitBreakpointManager.create(d, (XHRBreakpoint) ab);
        }
        throw new IllegalArgumentException("Unknown breakpoint: "+ab);
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.api.debugger.LazyDebuggerManagerListener#getProperties()
     */
    @Override
    public String[] getProperties() {
        return new String[] { DebuggerManager.PROP_BREAKPOINTS };
    }

    /* (non-Javadoc)
     * @see org.netbeans.api.debugger.DebuggerManagerListener#breakpointAdded(org.netbeans.api.debugger.Breakpoint)
     */
    @Override
    public void breakpointAdded( Breakpoint breakpoint ) {
        JSLineBreakpoint lb = null;
        AbstractBreakpoint ab = null;
        if (breakpoint instanceof JSLineBreakpoint) {
            lb = (JSLineBreakpoint) breakpoint;
            if (!acceptBreakpoint(lb)) {
                return ;
            }
        } else if (breakpoint instanceof AbstractBreakpoint) {
            ab = (AbstractBreakpoint) breakpoint;
        } else {
            return ;
        }
        final Breakpoint b = (lb != null) ? lb : ab;
        synchronized (breakpointImpls) {
            if (breakpointImpls.containsKey(b)) {
                return ;
            }
        }
        SourceMapsTranslator scannedSMT = SourceMapsScanner.getInstance().scan(pc.getProject());
        MiscEditorUtil.registerProjectsSourceMapTranslator(d, scannedSMT);
        final WebKitBreakpointManager bm = (lb != null) ?
                                            createWebKitBreakpointManager(lb) :
                                            createWebKitBreakpointManager(ab);
        synchronized (breakpointImpls) {
            if (breakpointImpls.containsKey(b)) {
                // Added in between, destroy the one created redundantly.
                bm.destroy();
                return ;
            }
            breakpointImpls.put(b, bm);
        }
        if (b.isEnabled()) {
            RP.post(new Runnable() {
                @Override
                public void run() {
                    bm.add();
                }
            });
        }
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.api.debugger.DebuggerManagerListener#breakpointRemoved(org.netbeans.api.debugger.Breakpoint)
     */
    @Override
    public void breakpointRemoved( Breakpoint breakpoint ) {
        if (!(breakpoint instanceof AbstractBreakpoint) &&
            !(breakpoint instanceof JSLineBreakpoint)) {
            return;
        }
        //breakpoint.removePropertyChangeListener(Breakpoint.PROP_ENABLED, this);
        final WebKitBreakpointManager bm;
        synchronized (breakpointImpls) {
            bm = breakpointImpls.remove(breakpoint);
        }
        if (bm != null) {
            RP.post(new Runnable() {
                @Override
                public void run() {
                    bm.destroy();
                }
            });
        }
    }

    @Override
    protected void destroy() {
        DebuggerManager.getDebuggerManager().removeDebuggerListener(this);
        d.removeScriptsListener(this);
        List<WebKitBreakpointManager> toDestroy;
        synchronized (breakpointImpls) {
            toDestroy = new ArrayList<>(breakpointImpls.values());
            breakpointImpls.clear();
        }
        for (WebKitBreakpointManager bm : toDestroy) {
            bm.destroy();
        }
    }

    @Override
    public Breakpoint[] initBreakpoints() {
        return new Breakpoint[] {};
    }
    @Override
    public void initWatches() {}
    @Override
    public void watchAdded(Watch watch) {}
    @Override
    public void watchRemoved(Watch watch) {}
    @Override
    public void sessionAdded(Session session) {}
    @Override
    public void sessionRemoved(Session session) {}
    @Override
    public void engineAdded(DebuggerEngine engine) {}
    @Override
    public void engineRemoved(DebuggerEngine engine) {}
    @Override
    public void propertyChange(PropertyChangeEvent evt) {}
    
    private static List<FileObject> getProjectSourceRoots(Project project) {
        Set<FileObject> sources = new LinkedHashSet<>();
        sources.addAll(getProjectSourceRoots(project, WebClientProjectConstants.SOURCES_TYPE_HTML5_SITE_ROOT));
        sources.addAll(getProjectSourceRoots(project, WebClientProjectConstants.SOURCES_TYPE_HTML5_TEST));
        sources.addAll(getProjectSourceRoots(project, WebClientProjectConstants.SOURCES_TYPE_HTML5_TEST_SELENIUM));
        return new ArrayList<>(sources);
    }
    
    private static List<FileObject> getProjectSourceRoots(Project project, String type) {
        SourceGroup[] sourceGroups = ProjectUtils.getSources(project).getSourceGroups(type);
        List<FileObject> roots = new ArrayList<>(sourceGroups.length);
        for (SourceGroup sourceGroup : sourceGroups) {
            FileObject rootFolder = sourceGroup.getRootFolder();
            roots.add(rootFolder);
        }
        if (roots.isEmpty()) {
            roots.add(project.getProjectDirectory());
        }
        return roots;
    }
    
    private boolean acceptBreakpoint(JSLineBreakpoint lb) {
        FileObject fo = lb.getFileObject();
        if (fo == null) {
            return false;
        }
        if (RemoteFileCache.isRemoteFile(fo) != null) {
            return true;
        }
        if (FileUtil.toFile(fo) == null) {
            return true;
        }
        if (projectSourceRoots == null) {
            return true;
        }
        boolean isInPrjSources = false;
        for (FileObject psr : projectSourceRoots) {
            if (FileUtil.isParentOf(psr, fo)) {
                isInPrjSources = true;
                break;
            }
        }
        return isInPrjSources;
    }

    @Override
    public void scriptParsed(Script script) {
        List<FileObject> mappedSourceFiles = MiscEditorUtil.registerScriptSourceMap(pc.getProject(), d, script);
        if (mappedSourceFiles.size() > 0) {
            Set<FileObject> sourceFiles = new HashSet<>(mappedSourceFiles);
            final List<WebKitBreakpointManager> wbms = new ArrayList<>(sourceFiles.size());
            synchronized (breakpointImpls) {
                for (Breakpoint breakpoint : breakpointImpls.keySet()) {
                    if (breakpoint instanceof JSLineBreakpoint) {
                        JSLineBreakpoint jb = (JSLineBreakpoint) breakpoint;
                        if (sourceFiles.contains(jb.getFileObject())) {
                            WebKitBreakpointManager wbm = breakpointImpls.get(jb);
                            wbms.add(wbm);
                        }
                    }
                }
            }
            if (!wbms.isEmpty()) {
                for (WebKitBreakpointManager wbm : wbms) {
                    wbm.notifySourceMap();
                }
            }
        }
    }
    
    
}
