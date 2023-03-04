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

package org.netbeans.modules.debugger.jpda.breakpoints;

import com.sun.jdi.ClassLoaderReference;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.event.ClassPrepareEvent;
import com.sun.jdi.event.ClassUnloadEvent;
import com.sun.jdi.event.Event;
import com.sun.jdi.request.ClassPrepareRequest;
import com.sun.jdi.request.ClassUnloadRequest;
import com.sun.jdi.VirtualMachine;
import com.sun.jdi.request.EventRequest;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.lang.model.element.TypeElement;
import org.netbeans.api.debugger.Breakpoint.VALIDITY;
import org.netbeans.api.debugger.jpda.ClassLoadUnloadBreakpoint;

import org.netbeans.api.debugger.jpda.JPDABreakpoint;
import org.netbeans.api.debugger.Session;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.Task;
import org.netbeans.modules.debugger.jpda.JPDADebuggerImpl;
import org.netbeans.modules.debugger.jpda.SourcePath;
import org.netbeans.modules.debugger.jpda.jdi.InternalExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.InvalidRequestStateExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.ObjectCollectedExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.ObjectReferenceWrapper;
import org.netbeans.modules.debugger.jpda.jdi.ReferenceTypeWrapper;
import org.netbeans.modules.debugger.jpda.jdi.VMDisconnectedExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.VirtualMachineWrapper;
import org.netbeans.modules.debugger.jpda.jdi.event.ClassPrepareEventWrapper;
import org.netbeans.modules.debugger.jpda.jdi.event.ClassUnloadEventWrapper;
import org.netbeans.modules.debugger.jpda.jdi.request.ClassPrepareRequestWrapper;
import org.netbeans.modules.debugger.jpda.jdi.request.ClassUnloadRequestWrapper;
import org.netbeans.modules.debugger.jpda.jdi.request.EventRequestManagerWrapper;
import org.netbeans.modules.debugger.jpda.util.JPDAUtils;
import org.netbeans.spi.debugger.jpda.BreakpointsClassFilter;
import org.netbeans.spi.debugger.jpda.SourcePathProvider;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.WeakListeners;

/**
* Implementation of breakpoint on method.
*
* @author   Jan Jancura
*/
public abstract class ClassBasedBreakpoint extends BreakpointImpl {
    
    private String sourceRoot;
    private final Object SOURCE_ROOT_LOCK = new Object();
    private final SourceRootsCache sourceRootsCache;
    private SourceRootsChangedListener srChListener;
    private PropertyChangeListener weakSrChListener;
    private BreakpointsClassFilter classFilter;
    
    private static final Logger logger = Logger.getLogger("org.netbeans.modules.debugger.jpda.breakpoints"); // NOI18N

    ClassBasedBreakpoint (
        JPDABreakpoint breakpoint, 
        JPDADebuggerImpl debugger,
        Session session,
        SourceRootsCache sourceRootsCache
    ) {
        this (breakpoint, null, debugger, session, sourceRootsCache);
    }
    
    ClassBasedBreakpoint (
        JPDABreakpoint breakpoint, 
        BreakpointsReader reader,
        JPDADebuggerImpl debugger,
        Session session,
        SourceRootsCache sourceRootsCache
    ) {
        super (breakpoint, reader, debugger, session);
        classFilter = new CompoundClassFilter(session.lookup(null, BreakpointsClassFilter.class));
        this.sourceRootsCache = sourceRootsCache;
    }
    
    protected final BreakpointsClassFilter getClassFilter() {
        return classFilter;
    }
    
    protected final void setSourceRoot(String sourceRoot) {
        synchronized (SOURCE_ROOT_LOCK) {
            this.sourceRoot = sourceRoot;
            if (sourceRoot != null && srChListener == null) {
                srChListener = new SourceRootsChangedListener();
                getDebugger().getEngineContext().addPropertyChangeListener(
                        weakSrChListener = WeakListeners.propertyChange(srChListener,
                                                     getDebugger().getEngineContext()));
            } else if (sourceRoot == null) {
                srChListener = null; // release the listener
            }
        }
    }
    
    protected final String getSourceRoot() {
        synchronized (SOURCE_ROOT_LOCK) {
            return sourceRoot;
        }
    }
    
    protected final boolean compareSourceRoots(String sr1, String sr2) {
        if (sr1.equals(sr2)) {
            return true;
        }
        File f1 = new File(sr1);
        File f2 = new File(sr2);
        if (f1.isDirectory() && f2.isDirectory() ||
            f1.isFile() && f2.isFile()) { // An archive file
            
            if (f1.equals(f2)) {
                return true;
            }
            try {
                f1 = f1.getCanonicalFile();
                f2 = f2.getCanonicalFile();
                return f1.equals(f2);
            } catch (IOException ioex) {}
        }
        return false;
    }
    
    protected final boolean isRootInSources(String root) {
        if (sourceRootsCache.getRootPaths().contains(root)) {
            return true;
        }
        File rootFile = new File(root);
        try {
            rootFile = rootFile.getCanonicalFile();
        } catch (IOException ioex) {}
        if (sourceRootsCache.getRootCanonicalFiles().contains(rootFile)) {
            return true;
        }
        return false;
    }
    
    @Override
    protected void remove () {
        super.remove();
        synchronized (SOURCE_ROOT_LOCK) {
            if (srChListener != null) {
                getDebugger().getEngineContext().removePropertyChangeListener(weakSrChListener);
                srChListener = null;
            }
        }
    }
    
    @Override
    protected boolean isEnabled() {
        String sourceRoot = getSourceRoot();
        if (sourceRoot == null) {
            return true;
        }
        if (sourceRootsCache.getRootPaths().contains(sourceRoot)) {
            return true;
        }
        File rootFile = new File(sourceRoot);
        try {
            rootFile = rootFile.getCanonicalFile();
        } catch (IOException ioex) {}
        if (sourceRootsCache.getRootCanonicalFiles().contains(rootFile)) {
            return true;
        }
        if (sourceRootsCache.getProjectRootPaths().contains(sourceRoot) ||
            sourceRootsCache.getProjectRootCanonicalFiles().contains(rootFile)) {
                setValidity(VALIDITY.INVALID,
                            NbBundle.getMessage(ClassBasedBreakpoint.class,
                                        "MSG_DisabledSourceRoot",
                                        sourceRoot));
                return false;
        }
        // Breakpoint is not in debugger's source roots,
        // though it still might get hit if the app loads additional classes...
        return true;
        /*if (logger.isLoggable(Level.FINE)) {
            logger.fine("Breakpoint "+getBreakpoint()+
                        " NOT submitted because it's source root "+sourceRoot+
                        " is not contained in debugger's source roots: "+
                        java.util.Arrays.asList(sourceRoots));
        }
        return false;
         */
    }
    
    /** Check whether the breakpoint belongs to the first matched source root. */
    protected boolean isEnabled(String sourcePath, String[] preferredSourceRoot) {
        String sourceRoot = getSourceRoot();
        if (sourceRoot == null) {
            return true;
        }
        String url = getDebugger().getEngineContext().getURL(sourcePath, true);
        if (url == null) { // In some pathological situations, the source is not found.
            logger.warning("No URL found for source path "+sourcePath);
            return false;
        }
        String urlRoot = getDebugger().getEngineContext().getSourceRoot(url);
        if (urlRoot == null) {
            return true;
        }
        preferredSourceRoot[0] = urlRoot;
        return compareSourceRoots(sourceRoot, urlRoot);
    }
    
    /**
     * Returns list of class names that are a sub-set of provided class names,
     * which does not belong to disabled source roots.
     * @param classNames List of class names
     */
    protected final String[] checkSourcesEnabled(String[] classNames, String[] srcRootPtr) {
        if (getBreakpoint().isHidden()) {
            // Enable hidden breakpoints. Their are submitted programmatically.
            return classNames;
        }
        List<String> enabledClassNames = new ArrayList<String>(classNames.length);
        for (String className : classNames) {
            String relPath = SourcePath.convertClassNameToRelativePath(className);
            String globalURL = getDebugger().getEngineContext().getURL(relPath, true);
            if (globalURL != null) {
                if (getDebugger().getEngineContext().getURL(relPath, false) == null) {
                    // Is disabled
                    srcRootPtr[0] = getDebugger().getEngineContext().getSourceRoot(globalURL);
                    continue;
                }
            }
            enabledClassNames.add(className);
        }
        return enabledClassNames.toArray(new String[] {});
    }
    
    protected static boolean classExistsInSources(final String className, String[] projectSourceRoots) {
        /*
        ClassIndexManager cim = ClassIndexManager.getDefault();
        List<FileObject> sourcePaths = new ArrayList<FileObject>(projectSourceRoots.length);
        for (String sr : projectSourceRoots) {
            FileObject fo = getFileObject(sr);
            if (fo != null) {
                sourcePaths.add(fo);
                ClassIndexImpl ci;
                try {
                    ci = cim.getUsagesQuery(fo.getURL());
                    if (ci != null) {
                        String sourceName = ci.getSourceName(className);
                        if (sourceName != null) {
                            return true;
                        }
                    }
                } catch (FileStateInvalidException ex) {
                    continue;
                } catch (java.io.IOException ioex) {
                    continue;
                }
            }
        }
        return false;
         */
        List<FileObject> sourcePaths = new ArrayList<FileObject>(projectSourceRoots.length);
        for (String sr : projectSourceRoots) {
            FileObject fo = getFileObject(sr);
            if (fo != null) {
                sourcePaths.add(fo);
            }
        }
        ClassPath cp = ClassPathSupport.createClassPath(sourcePaths.toArray(new FileObject[0]));
        //ClassPathSupport.createClassPath(new FileObject[] {});
        ClasspathInfo cpInfo = ClasspathInfo.create(ClassPathSupport.createClassPath(new FileObject[] {}),
                                                    ClassPathSupport.createClassPath(new FileObject[] {}),
                                                    cp);
        //ClassIndex ci = cpInfo.getClassIndex();
        JavaSource js = JavaSource.create(cpInfo);
        final boolean[] found = new boolean[] { false };
        try {
            js.runUserActionTask(new Task<CompilationController>() {
                @Override
                public void run(CompilationController cc) throws Exception {
                    cc.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                    TypeElement te = cc.getElements().getTypeElement(className);
                    if (te != null) { // found
                        found[0] = true;
                    }
                }
            }, true);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        return found[0];
        /*
        SourceUtils.getFile(null, null);
        ClasspathInfo.create(null, null, cp);

        cp = org.netbeans.modules.java.source.classpath.SourcePath.create(cp, true);
        try {
            ClassLoader cl = cp.getClassLoader(true);
            FileObject fo = cp.findResource(className.replace('.', '/').concat(".class"));
            Class c = cl.loadClass(className);
            System.err.println("classExistsInSources("+className+"): fo = "+fo+", class = "+c);
            return c != null;
        } catch (ClassNotFoundException ex) {
            return false;
        }
        */
    }
    
    /**
     * Returns FileObject for given String.
     */
    private static FileObject getFileObject (String file) {
        File f = new File (file);
        FileObject fo = FileUtil.toFileObject (f);
        String path = null;
        if (fo == null && file.contains("!/")) {
            int index = file.indexOf("!/");
            f = new File(file.substring(0, index));
            fo = FileUtil.toFileObject (f);
            path = file.substring(index + "!/".length());
        }
        if (fo != null && FileUtil.isArchiveFile (fo)) {
            fo = FileUtil.getArchiveRoot (fo);
            if (path !=null) {
                fo = fo.getFileObject(path);
            }
        }
        if (fo != null && !fo.isFolder()) {
            fo = null;
        }
        return fo;
    }

    protected final boolean setClassRequests (
        String[] classFilters,
        String[] classExclusionFilters,
        int breakpointType
    ) {
        return setClassRequests(classFilters, classExclusionFilters, breakpointType, true);
    }
    
    protected final boolean setClassRequests (
        String[] classFilters,
        String[] classExclusionFilters,
        int breakpointType,
        boolean ignoreHitCountOnClassLoad
    ) {
        try {
            if ((breakpointType & ClassLoadUnloadBreakpoint.TYPE_CLASS_LOADED) != 0
            ) {
                int i, k = classFilters.length;
                for (i = 0; i < k; i++) {
                    ClassPrepareRequest cpr = EventRequestManagerWrapper.
                            createClassPrepareRequest (getEventRequestManager());
                    ClassPrepareRequestWrapper.addClassFilter (cpr, classFilters [i]);
                    if (logger.isLoggable(Level.FINE))
                        logger.fine("Set class load request: " + classFilters [i]);
                    for (String exclusionFilter : classExclusionFilters) {
                        ClassPrepareRequestWrapper.addClassExclusionFilter (cpr, exclusionFilter);
                        if (logger.isLoggable(Level.FINE))
                            logger.fine("Set class load exclusion request: " + exclusionFilter);
                    }
                    addEventRequest (cpr, ignoreHitCountOnClassLoad);
                }
            }
            if ((breakpointType & ClassLoadUnloadBreakpoint.TYPE_CLASS_UNLOADED) != 0
            ) {
                int i, k = classFilters.length;
                for (i = 0; i < k; i++) {
                    ClassUnloadRequest cur = EventRequestManagerWrapper.
                            createClassUnloadRequest (getEventRequestManager());
                    ClassUnloadRequestWrapper.addClassFilter (cur, classFilters [i]);
                    if (logger.isLoggable(Level.FINE))
                        logger.fine("Set class unload request: " + classFilters [i]);
                    for (String exclusionFilter : classExclusionFilters) {
                        ClassUnloadRequestWrapper.addClassExclusionFilter (cur, exclusionFilter);
                        if (logger.isLoggable(Level.FINE))
                            logger.fine("Set class unload exclusion request: " + exclusionFilter);
                    }
                    addEventRequest (cur, false);
                }
            }
        } catch (VMDisconnectedExceptionWrapper e) {
            return false;
        } catch (InternalExceptionWrapper e) {
            return false;
        } catch (ObjectCollectedExceptionWrapper e) {
            return false;
        } catch (InvalidRequestStateExceptionWrapper irse) {
            Exceptions.printStackTrace(irse);
            setValidity(VALIDITY.INVALID, irse.getLocalizedMessage());
            return false;
        } catch (RequestNotSupportedException rnsex) {
            setValidity(VALIDITY.INVALID, NbBundle.getMessage(ClassBasedBreakpoint.class, "MSG_RequestNotSupported"));
            return false;
        }
        return true;
    }
    
    protected boolean checkLoadedClasses (
        String className, String[] classExclusionFilters
    ) {
        VirtualMachine vm = getVirtualMachine ();
        if (vm == null) return false;
        boolean all = className.startsWith("*") || className.endsWith("*"); // NOI18N
        logger.log(Level.FINE, "Check loaded classes: {0}, will load all classes: {1}", // NOI18N
                   new Object[]{className, all});
        boolean matched = false;
        Iterator<ReferenceType> i;
        if (all) {
            i = VirtualMachineWrapper.allClasses0(vm).iterator ();
        } else {
            i = VirtualMachineWrapper.classesByName0(vm, className).iterator ();
        }
        List<ReferenceType> loadedClasses = null;
        while (i.hasNext ()) {
            ReferenceType referenceType = i.next ();
            if (!ReferenceTypeWrapper.isPrepared0(referenceType)) {
                // Ignore not prepared classes, we should receive ClassPrepareEvent later.
                continue;
            }
//                if (verbose)
//                    System.out.println("B     cls: " + referenceType);
            try {
                ClassLoaderReference clref = ReferenceTypeWrapper.classLoader(referenceType);
                if (clref != null && ObjectReferenceWrapper.isCollected(clref)) {
                    // Ignore classes whose class loaders are gone.
                    continue;
                }
                String name = ReferenceTypeWrapper.name (referenceType);
                if (match (name, className)) {
                    boolean excluded = false;
                    if (classExclusionFilters != null) {
                        for (String exFilter : classExclusionFilters) {
                            if (match(name, exFilter)) {
                                excluded = true;
                                break;
                            }
                        }
                    }
                    if (!excluded) {
                        if (logger.isLoggable(Level.FINE)) {
                            logger.fine(" Class loaded: " + referenceType);
                        }
                        if (loadedClasses == null) {
                            loadedClasses = Collections.singletonList(referenceType);
                        } else {
                            if (loadedClasses.size() == 1) {
                                loadedClasses = new ArrayList<ReferenceType>(loadedClasses);
                            }
                            loadedClasses.add(referenceType);
                        }
                        //classLoaded (referenceType);
                        matched = true;
                    }
                }
            } catch (InternalExceptionWrapper e) {
            } catch (VMDisconnectedExceptionWrapper e) {
            } catch (ObjectCollectedExceptionWrapper e) {
            }
        }
        if (loadedClasses != null && loadedClasses.size() > 0) {
            ReferenceType preferredType;
            try {
                preferredType = JPDAUtils.getPreferredReferenceType(loadedClasses, logger);
            } catch (VMDisconnectedExceptionWrapper ex) {
                return false;
            }
            if (preferredType != null) {
                loadedClasses = Collections.singletonList(preferredType);
            }
            if (canSetRequests()) { // Ignore when the breakpoint is disabled in the mean time
                classLoaded(loadedClasses);
            }
        }
        return matched;
    }

    @Override
    public boolean exec (Event event) {
        try {
            if (event instanceof ClassPrepareEvent) {
                if (logger.isLoggable(Level.FINE))
                    logger.fine(" Class loaded: " + ClassPrepareEventWrapper.referenceType((ClassPrepareEvent) event));
                if (canSetRequests()) { // Ignore when the breakpoint is disabled in the mean time
                    classLoaded(Collections.singletonList(ClassPrepareEventWrapper.referenceType((ClassPrepareEvent) event)));
                }
            } else if (event instanceof ClassUnloadEvent) {
                if (logger.isLoggable(Level.FINE))
                    logger.fine(" Class unloaded: " + ClassUnloadEventWrapper.className((ClassUnloadEvent) event));
                classUnloaded(ClassUnloadEventWrapper.className((ClassUnloadEvent) event));
            }
        } catch (InternalExceptionWrapper ex) {
            return false;
        } catch (VMDisconnectedExceptionWrapper ex) {
            return false;
        }
        return true;
    }

    @Override
    public void removed(EventRequest eventRequest) {
    }
    
    protected void classLoaded (List<ReferenceType> referenceTypes) {}
    protected void classUnloaded (String className) {}
    
    
    private class SourceRootsChangedListener implements PropertyChangeListener {
        
        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if (SourcePathProvider.PROP_SOURCE_ROOTS.equals(evt.getPropertyName())) {
                getDebugger().getRequestProcessor().post(new Runnable() {
                    @Override
                    public void run() {
                        update();
                    }
                });
            }
        }
        
    }
    
    private class CompoundClassFilter extends BreakpointsClassFilter {
        
        private List<? extends BreakpointsClassFilter> filters;
        
        public CompoundClassFilter(List<? extends BreakpointsClassFilter> filters) {
            this.filters = filters;
        }

        @Override
        public ClassNames filterClassNames(ClassNames classNames, JPDABreakpoint breakpoint) {
            for (BreakpointsClassFilter f : filters) {
                classNames = f.filterClassNames(classNames, breakpoint);
            }
            return classNames;
        }
        
    }
}

