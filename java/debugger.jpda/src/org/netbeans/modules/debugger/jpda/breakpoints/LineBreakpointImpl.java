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

package org.netbeans.modules.debugger.jpda.breakpoints;

import com.sun.jdi.AbsentInformationException;
import com.sun.jdi.ClassNotPreparedException;
import com.sun.jdi.InternalException;
import com.sun.jdi.Location;
import com.sun.jdi.ObjectCollectedException;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.VMDisconnectedException;
import com.sun.jdi.event.BreakpointEvent;
import com.sun.jdi.event.Event;
import com.sun.jdi.event.LocatableEvent;
import com.sun.jdi.request.BreakpointRequest;
import com.sun.jdi.request.EventRequest;
import com.sun.jdi.request.InvalidRequestStateException;
import com.sun.source.tree.BlockTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.StatementTree;
import com.sun.source.tree.Tree;
import com.sun.source.util.SourcePositions;
import com.sun.source.util.TreePath;
import java.beans.PropertyChangeEvent;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.text.Document;
import org.netbeans.api.debugger.Breakpoint;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.Session;
import org.netbeans.api.debugger.jpda.ClassLoadUnloadBreakpoint;
import org.netbeans.api.debugger.jpda.JPDAClassType;
import org.netbeans.api.debugger.jpda.JPDAThread;
import org.netbeans.api.debugger.jpda.LineBreakpoint;
import org.netbeans.api.debugger.jpda.ObjectVariable;
import org.netbeans.api.editor.document.LineDocument;
import org.netbeans.api.editor.document.LineDocumentUtils;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.TreeUtilities;
import org.netbeans.modules.debugger.jpda.EditorContextBridge;
import org.netbeans.modules.debugger.jpda.JPDADebuggerImpl;
import org.netbeans.modules.debugger.jpda.SourcePath;
import org.netbeans.modules.debugger.jpda.expr.JDIVariable;
import org.netbeans.modules.debugger.jpda.jdi.ClassNotPreparedExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.InternalExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.InvalidRequestStateExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.LocatableWrapper;
import org.netbeans.modules.debugger.jpda.jdi.LocationWrapper;
import org.netbeans.modules.debugger.jpda.jdi.ObjectCollectedExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.ReferenceTypeWrapper;
import org.netbeans.modules.debugger.jpda.jdi.VMDisconnectedExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.event.LocatableEventWrapper;
import org.netbeans.modules.debugger.jpda.jdi.request.BreakpointRequestWrapper;
import org.netbeans.modules.debugger.jpda.jdi.request.EventRequestManagerWrapper;
import org.netbeans.modules.debugger.jpda.models.JPDAClassTypeImpl;
import org.netbeans.modules.debugger.jpda.models.JPDAThreadImpl;
import org.netbeans.spi.debugger.jpda.BreakpointsClassFilter.ClassNames;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.URLMapper;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;




/**
* Implementation of breakpoint on method.
*
* @author   Jan Jancura
*/
public class LineBreakpointImpl extends ClassBasedBreakpoint {
    
    private static final Logger logger = Logger.getLogger("org.netbeans.modules.debugger.jpda.breakpoints"); // NOI18N
    
    private int                 lineNumber;
    private int                 breakpointLineNumber;
    private int                 lineNumberForUpdate = -1;
    private final Object        lineLock = new Object();
    private BreakpointsReader   reader;
    
    
    LineBreakpointImpl (
        LineBreakpoint breakpoint, 
        BreakpointsReader reader,
        JPDADebuggerImpl debugger,
        Session session,
        SourceRootsCache sourceRootsCache
    ) {
        super (breakpoint, reader, debugger, session, sourceRootsCache);
        this.reader = reader;
        updateLineNumber();
        setSourceRoot(sourceRootsCache.getSourcePath().getSourceRoot(breakpoint.getURL()));
        set ();
    }

    private void updateLineNumber() {
        // int line = getBreakpoint().getLineNumber();
        // We need to retrieve the original line number which is associated
        // with the start of this session.
        LineBreakpoint lb = getBreakpoint();
        int theLineNumber = EditorContextBridge.getContext().getLineNumber(
                lb,
                getDebugger());
        int lbln = lb.getLineNumber();
        synchronized (lineLock) {
            breakpointLineNumber = lbln;
            lineNumber = theLineNumber;
        }
   }

    @Override
    protected LineBreakpoint getBreakpoint() {
        return (LineBreakpoint) super.getBreakpoint();
    }
    
    @Override
    void fixed () {
        logger.log(Level.FINE, "LineBreakpoint fixed: {0}", this);
        updateLineNumber();
        super.fixed ();
    }

    @Override
    protected boolean isApplicable() {
        LineBreakpoint breakpoint = getBreakpoint();
        String[] preferredSourceRoot = new String[] { null };
        String sourcePath = getDebugger().getEngineContext().getRelativePath(breakpoint.getURL(), '/', true);
        if (sourcePath == null) {
            return false;
        }
        boolean isInSources = false;
        {
            String srcRoot = getSourceRoot();
            if (srcRoot != null) {
                if (isRootInSources(srcRoot)) {
                    isInSources = true;
                }
            }
        }
        // Test if className exists in project sources:
        if (!isInSources) {
            return false;
        }
        if (isInSources && !isEnabled(sourcePath, preferredSourceRoot)) {
            return false;
        }
        return true;
    }
    
    @Override
    protected void setRequests () {
        LineBreakpoint breakpoint = getBreakpoint();
        updateLineNumber();
        String[] preferredSourceRoot = new String[] { null };
        boolean isEmptyURL = breakpoint.getURL().isEmpty();
        String sourcePath;
        if (isEmptyURL) {
            sourcePath = null;
        } else {
            sourcePath = getDebugger().getEngineContext().getRelativePath(breakpoint.getURL(), '/', true);
            if (sourcePath == null) {
                String reason = NbBundle.getMessage(LineBreakpointImpl.class,
                                                    "MSG_NoSourceRoot",
                                                    breakpoint.getURL());
                setInvalid(reason);
                return ;
            }
        }
        //JPDAClassType classType = breakpoint.getPreferredClassType();
        JPDAClassType classType = getPreferredClassType(breakpoint);
        if (classType != null) {
            classLoaded(Collections.singletonList(((JPDAClassTypeImpl) classType).getType()));
            return ;
        }
        String className = breakpoint.getPreferredClassName();
        if (className == null || className.isEmpty()) {
            className = reader.findCachedClassName(breakpoint);
            if (className == null || className.isEmpty()) {
                className = EditorContextBridge.getContext().getClassName (
                    breakpoint.getURL (), 
                    lineNumber
                );
                if (className != null && !className.isEmpty()) {
                    reader.storeCachedClassName(breakpoint, className);
                }
            }
        }
        if (className == null || className.isEmpty()) {
            logger.log(Level.WARNING, "Class name not defined for breakpoint {0}", breakpoint);
            setValidity(Breakpoint.VALIDITY.INVALID, NbBundle.getMessage(LineBreakpointImpl.class, "MSG_NoBPClass"));
            return ;
        }

        boolean isInSources = isEmptyURL;
        if (!isEmptyURL) {
            String srcRoot = getSourceRoot();
            if (srcRoot != null) {
                if (isRootInSources(srcRoot)) {
                    isInSources = true;
                }
            }
        }
        if (!isInSources) {
            String relativePath = EditorContextBridge.getRelativePath(className);
            String classURL = getDebugger().getEngineContext().getURL(relativePath, true);
            if (classURL != null && !classURL.equals(breakpoint.getURL())) {
                // Silently ignore breakpoints from other sources that resolve to a different URL.
                logger.log(Level.FINE,
                       "LineBreakpoint {0} NOT submitted, because it's URL ''{1}'' differes from class ''{2}'' URL ''{3}''.",
                       new Object[]{breakpoint, breakpoint.getURL(), className, classURL});
                return ;
            }
        }
        /*
        // Test if className exists in project sources:
        if (!isInSources && classExistsInSources(className, getDebugger().getEngineContext().getProjectSourceRoots())) {
            logger.log(Level.FINE,
                       "LineBreakpoint {0} NOT submitted, URL {1} not in sources, but class {2} exist in sources.",
                       new Object[]{breakpoint, breakpoint.getURL(), className});
            return ;
        }
        */
        if (isInSources && sourcePath != null && !isEnabled(sourcePath, preferredSourceRoot)) {
            String reason = NbBundle.getMessage(LineBreakpointImpl.class,
                                                "MSG_DifferentPrefferedSourceRoot",
                                                preferredSourceRoot[0]);
            setInvalid(reason);
            logger.log(Level.FINE,
                       "LineBreakpoint {0} NOT submitted, because of ''{1}''.",
                       new Object[]{breakpoint, reason});
            return ;
        }
        logger.log(Level.FINE,
                   "LineBreakpoint {0} - setting request for {1}",
                   new Object[]{breakpoint, className});
        ClassNames classNames = getClassFilter().filterClassNames(
                new ClassNames(
                    new String[] {
                        className // The class name is correct even for inner classes now
                    },
                    new String [0]),
                breakpoint);
        String[] names = classNames.getClassNames();
        String[] excludedNames = classNames.getExcludedClassNames();
        boolean wasSet = setClassRequests (
            names,
            excludedNames,
            ClassLoadUnloadBreakpoint.TYPE_CLASS_LOADED
        );
        if (wasSet) {
            for (String cn : names) {
                checkLoadedClasses (cn, excludedNames);
            }
        }
    }

    private void setInvalid(String reason) {
        logger.warning(
                "Unable to submit line breakpoint to "+getBreakpoint().getURL()+
                " at line "+lineNumber+", reason: "+reason);
        setValidity(Breakpoint.VALIDITY.INVALID, reason);
    }

    @Override
    protected void classLoaded (List<ReferenceType> referenceTypes) {
        LineBreakpoint breakpoint = getBreakpoint();
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("Classes "+referenceTypes+" loaded for breakpoint "+breakpoint);
        }
        boolean submitted = false;
        String failReason = null;
        ReferenceType noLocRefType = null;
        int lineNumberToSet;
        final int origBreakpointLineNumber;
        int newBreakpointLineNumber;
        synchronized (lineLock) {
            lineNumberToSet = lineNumber;
            newBreakpointLineNumber = origBreakpointLineNumber = breakpointLineNumber;
        }
        String currFailReason = null;

        // if there is no location available, find correct line candidate and run the body again
        for (int counter = 0; counter < 2; counter++) {
            for (ReferenceType referenceType : referenceTypes) {
                String[] reason = new String[] { null };
                boolean[] isNoLocReason = new boolean[1];
                List<Location> locations = getLocations (
                    referenceType,
                    breakpoint.getStratum (),
                    breakpoint.getSourceName (),
                    breakpoint.getSourcePath(),
                    lineNumberToSet,
                    reason,
                    isNoLocReason
                );
                if (logger.isLoggable(Level.FINE)) {
                    logger.fine("Locations in "+referenceType+" are: "+locations+", reason = '"+reason[0]);//+"', HAVE PARENT = "+haveParent);
                }
                if (locations.isEmpty()) {
                    failReason = reason[0];
                    if (isNoLocReason[0]) {
                        noLocRefType = referenceType;
                    }
                    continue;
                }
                // Submit the breakpoint for the lowest location on the line only:
                Location location = locations.get(0);
                com.sun.jdi.Method m0 = location.method();
                for (int li = 1; li < locations.size(); li++) {
                    Location l = locations.get(li);
                    if (l.codeIndex() < location.codeIndex()) {
                        if (l.method().equals(m0)) {
                            // Assure that we're still in the same method
                            location = l;
                        }
                    }
                }
                try {
                    BreakpointRequest br = EventRequestManagerWrapper.
                        createBreakpointRequest (getEventRequestManager (), location);
                    setFilters(br);
                    addEventRequest (br);
                    submitted = true;
                    //System.out.println("Breakpoint " + br + location + "created");
                } catch (VMDisconnectedExceptionWrapper e) {
                } catch (InternalExceptionWrapper e) {
                } catch (ObjectCollectedExceptionWrapper e) {
                } catch (InvalidRequestStateExceptionWrapper irse) {
                    Exceptions.printStackTrace(irse);
                } catch (RequestNotSupportedException rnsex) {
                    setValidity(Breakpoint.VALIDITY.INVALID, NbBundle.getMessage(ClassBasedBreakpoint.class, "MSG_RequestNotSupported"));
                    return ;
                }
            } // for
            if (counter == 0) {
                if (!submitted && noLocRefType != null && areNewOrSubmittedTypes0(referenceTypes)) {
                    int newLineNumber = findBreakableLine(breakpoint.getURL(), origBreakpointLineNumber);
                    if (newLineNumber != origBreakpointLineNumber && newLineNumber >= 0 &&
                            findBreakpoint(breakpoint.getURL(), newLineNumber) == null) {
                        newBreakpointLineNumber = newLineNumber;
                        lineNumberToSet += newLineNumber - origBreakpointLineNumber;
                        currFailReason = failReason;
                        failReason = null;
                        continue;
                    }
                }
                break;
            } else { // counter == 1
                if (!submitted) {
                    // we failed to find nearest location, roll back to values from the first run
                    failReason = currFailReason;
                }
            }
        } // for
        if (submitted) {
            if (origBreakpointLineNumber != newBreakpointLineNumber) {
                synchronized (lineLock) {
                    lineNumberForUpdate = newBreakpointLineNumber;
                }
                breakpoint.setLineNumber(newBreakpointLineNumber);
            }
            setValidity(Breakpoint.VALIDITY.VALID, failReason); // failReason is != null for partially submitted breakpoints (to some classes only)
        } else {
            String className = getBreakpoint().getPreferredClassName();
            boolean all = className != null && (className.startsWith("*") || className.endsWith("*")); // NOI18N
            // We know that the breakpoint is invalid, only when it's not submitted for an unknown set of classes.
            if (!all) {
                logger.warning(
                        "Unable to submit line breakpoint to "+referenceTypes.get(0).name()+
                        " at line "+lineNumber+", reason: "+failReason);
                setValidity(Breakpoint.VALIDITY.INVALID, failReason);
            }
        }
    }
    
    private boolean areNewOrSubmittedTypes0(List<ReferenceType> referenceTypes) {
        try {
            return areNewOrSubmittedTypes(referenceTypes);
        } catch (VMDisconnectedException vmdex) {
        } catch (InternalException e) {
        } catch (ObjectCollectedException e) {
        } catch (InvalidRequestStateException irse) {
        }
        return false;
    }
    
    /**
     * Checks whether the list of reference types are new types (the breakpoint
     * was not submitted anywhere yet), or are some of the types for which the
     * breakpoint was submitted already.
     * @param referenceTypes The types to check
     */
    private boolean areNewOrSubmittedTypes(List<ReferenceType> referenceTypes) {
        List<EventRequest> eventRequests = getEventRequests();
        List<BreakpointRequest> brs = new LinkedList<BreakpointRequest>();
        for (EventRequest er : eventRequests) {
            if (er instanceof BreakpointRequest) {
                brs.add((BreakpointRequest) er);
            }
        }
        if (brs.isEmpty()) {
            return true;
        }
        for (ReferenceType rt : referenceTypes) {
            // Check whether breakpoint requests' types contains rt:
            boolean contains = false;
            for (BreakpointRequest br : brs) {
                ReferenceType brt = br.location().declaringType();
                if (rt.equals(brt)) {
                    contains = true;
                    break;
                }
            }
            if (!contains) {
                return false;
            }
        }
        return true;
    }
    
    @Override
    protected EventRequest createEventRequest(EventRequest oldRequest) throws InternalExceptionWrapper, VMDisconnectedExceptionWrapper {
        Location location = BreakpointRequestWrapper.location((BreakpointRequest) oldRequest);
        BreakpointRequest br = EventRequestManagerWrapper.createBreakpointRequest(getEventRequestManager(), location);
        setFilters(br);
        return br;
    }
    
    private void setFilters(BreakpointRequest br) throws InternalExceptionWrapper, VMDisconnectedExceptionWrapper {
        JPDAThread[] threadFilters = getBreakpoint().getThreadFilters(getDebugger());
        if (threadFilters != null && threadFilters.length > 0) {
            for (JPDAThread t : threadFilters) {
                BreakpointRequestWrapper.addThreadFilter(br, ((JPDAThreadImpl) t).getThreadReference());
            }
        }
        ObjectVariable[] varFilters = getBreakpoint().getInstanceFilters(getDebugger());
        if (varFilters != null && varFilters.length > 0) {
            for (ObjectVariable v : varFilters) {
                BreakpointRequestWrapper.addInstanceFilter(br, (ObjectReference) ((JDIVariable) v).getJDIValue());
            }
        }
    }

    @Override
    public boolean processCondition(Event event) {
        if (event instanceof BreakpointEvent) {
            try {
                return processCondition(event, getBreakpoint().getCondition (),
                        LocatableEventWrapper.thread((BreakpointEvent) event), null);
            } catch (InternalExceptionWrapper ex) {
                return true;
            } catch (VMDisconnectedExceptionWrapper ex) {
                return true;
            }
        } else {
            return true; // Empty condition, always satisfied.
        }
    }

    @Override
    public boolean exec (Event event) {
        if (event instanceof BreakpointEvent) {
            try {
                return perform (
                    event,
                    LocatableEventWrapper.thread((BreakpointEvent) event),
                    LocationWrapper.declaringType(LocatableWrapper.location((LocatableEvent) event)),
                    null
                );
            } catch (InternalExceptionWrapper ex) {
                return false;
            } catch (VMDisconnectedExceptionWrapper ex) {
                return false;
            }
        }
        return super.exec (event);
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (LineBreakpoint.PROP_LINE_NUMBER.equals(evt.getPropertyName())) {
            synchronized (lineLock) {
                if (lineNumberForUpdate != -1) {
                    lineNumber = lineNumberForUpdate;
                    lineNumberForUpdate = -1;
                    return; // do not call super.propertyChange(evt);
                }
            }
            int old = lineNumber;
            updateLineNumber();
            //System.err.println("LineBreakpointImpl.propertyChange("+evt+")");
            //System.err.println("  old line = "+old+", new line = "+lineNumber);
            //System.err.println("  BP line = "+getBreakpoint().getLineNumber());
            if (lineNumber == old) {
                // No change, skip it
                return ;
            }
        }
        super.propertyChange(evt);
    }
    
    
    private static List<Location> getLocations (
        ReferenceType referenceType,
        String stratum,
        String sourceName,
        String bpSourcePath,
        int lineNumber,
        String[] reason,
        boolean[] noLocationReason
    ) {
        try {
            reason[0] = null;
            noLocationReason[0] = false;
            List<Location> locations = locationsOfLineInClass(referenceType, stratum,
                                                    sourceName, bpSourcePath,
                                                    lineNumber, reason);
            /* Obsolete, no special handling of inner classes, referenceType is
               the correct class now.
             if (locations.isEmpty()) {
                // add lines from innerclasses
                Iterator i = referenceType.nestedTypes ().iterator ();
                while (i.hasNext ()) {
                    ReferenceType rt = (ReferenceType) i.next ();
                    locations = locationsOfLineInClass(rt, stratum, sourceName,
                                                       bpSourcePath, lineNumber,
                                                       reason);
                    if (!locations.isEmpty()) {
                        break;
                    }
                }
            }*/
            if (locations.isEmpty() && reason[0] == null) {
                reason[0] = NbBundle.getMessage(LineBreakpointImpl.class, "MSG_NoLocation", Integer.toString(lineNumber), referenceType.name());
                noLocationReason[0] = true;
            }
            return locations;
        } catch (AbsentInformationException ex) {
            // we are not able to create breakpoint in this situation. 
            // should we write some message?!?
            // We should indicate somehow that the breakpoint is invalid...
            reason[0] = NbBundle.getMessage(LineBreakpointImpl.class, "MSG_NoLineInfo", referenceType.name());
        } catch (ObjectCollectedException ex) {
            // no problem, breakpoint will be created next time the class 
            // is loaded
            // should not occurre. see [51034]
            reason[0] = ex.getLocalizedMessage();
        } catch (ClassNotPreparedException ex) {
            // should not occurre. VirtualMachine.allClasses () returns prepared
            // classes only. But...
            Exceptions.printStackTrace(ex);
        } catch (InternalException iex) {
            // Something wrong in JDI
            iex = Exceptions.attachLocalizedMessage(iex, 
                    NbBundle.getMessage(LineBreakpointImpl.class,
                    "MSG_jdi_internal_error") );
            Exceptions.printStackTrace(iex);
            // We should indicate somehow that the breakpoint is invalid...
            reason[0] = iex.getLocalizedMessage();
        }
        return Collections.emptyList();
    }
    
    private static List<Location> locationsOfLineInClass(
        ReferenceType referenceType,
        String stratum,
        String sourceName,
        String bpSourcePath,
        int lineNumber,
        String[] reason) throws AbsentInformationException, ObjectCollectedException,
                                ClassNotPreparedException, InternalException {
        List<Location> list;
        try {
            list = ReferenceTypeWrapper.locationsOfLine0(referenceType, stratum, sourceName, lineNumber);
        } catch (ClassNotPreparedExceptionWrapper ex) {
            throw ex.getCause();
        }

        if (logger.isLoggable(Level.FINER)) {
            logger.finer("LineBreakpoint: locations for ReferenceType=" +
                    referenceType + ", stratum=" + stratum + 
                    ", source name=" + sourceName + ", bpSourcePath=" +
                    bpSourcePath+", lineNumber=" + lineNumber + 
                    " are: {" + list + "}");
        }
        if (!list.isEmpty ()) {
            if (bpSourcePath == null)
                return list;
            bpSourcePath = bpSourcePath.replace(java.io.File.separatorChar, '/');
            ArrayList<Location> locations = new ArrayList<Location>(list.size());
            for (Iterator<Location> it = list.iterator(); it.hasNext();) {
                Location l = it.next();
                String lSourcePath;
                try {
                    lSourcePath = LocationWrapper.sourcePath(l).replace(java.io.File.separatorChar, '/');
                } catch (InternalExceptionWrapper ex) {
                    return Collections.emptyList();
                } catch (VMDisconnectedExceptionWrapper ex) {
                    return Collections.emptyList();
                }
                lSourcePath = normalize(lSourcePath);
                if (lSourcePath.equals(bpSourcePath)) {
                    locations.add(l);
                } else {
                    reason[0] = "Breakpoint source path '"+bpSourcePath+"' is different from the location source path '"+lSourcePath+"'.";
                }
            }
            if (logger.isLoggable(Level.FINER)) {
                logger.finer("LineBreakpoint: relevant location(s) for path '" + bpSourcePath + "': " + locations);
            }
            if (!locations.isEmpty())
                return locations;
        }
        return Collections.emptyList();
    }
    
    /**
     * Normalizes the given path by removing unnecessary "." and ".." sequences.
     * This normalization is needed because the compiler stores source paths like "foo/../inc.jsp" into .class files. 
     * Such paths are not supported by our ClassPath API.
     * TODO: compiler bug? report to JDK?
     * 
     * @param path path to normalize
     * @return normalized path without "." and ".." elements
     */ 
    private static String normalize(String path) {
      Pattern thisDirectoryPattern = Pattern.compile("(/|\\A)\\./");
      Pattern parentDirectoryPattern = Pattern.compile("(/|\\A)([^/]+?)/\\.\\./");
      
      for (Matcher m = thisDirectoryPattern.matcher(path); m.find(); )
      {
        path = m.replaceAll("$1");
        m = thisDirectoryPattern.matcher(path);
      }
      for (Matcher m = parentDirectoryPattern.matcher(path); m.find(); )
      {
        if (!m.group(2).equals("..")) {
          path = path.substring(0, m.start()) + m.group(1) + path.substring(m.end());
          m = parentDirectoryPattern.matcher(path);        
        }
      }
      return path;
    }

    private int findBreakableLine(String url, final int lineNumber) {
        FileObject fileObj = null;
        try {
            fileObj = URLMapper.findFileObject(new URL(url));
        } catch (MalformedURLException e) {
        }
        if (fileObj == null) return lineNumber;
        JavaSource js = JavaSource.forFileObject(fileObj);
        if (js == null) return lineNumber;
        final int[] result = new int[] {lineNumber};
        try {
            js.runUserActionTask(new CancellableTask<CompilationController>() {
                @Override
                public void cancel() {
                }
                @Override
                public void run(CompilationController ci) throws Exception {
                    if (ci.toPhase(Phase.RESOLVED).compareTo(Phase.RESOLVED) < 0) {
                        logger.warning(
                                "Unable to resolve "+ci.getFileObject()+" to phase "+Phase.RESOLVED+", current phase = "+ci.getPhase()+
                                "\nDiagnostics = "+ci.getDiagnostics()+
                                "\nFree memory = "+Runtime.getRuntime().freeMemory());
                        return;
                    }
                    SourcePositions positions = ci.getTrees().getSourcePositions();
                    CompilationUnitTree compUnit = ci.getCompilationUnit();
                    TreeUtilities treeUtils = ci.getTreeUtilities();
                    Document ciDoc = ci.getDocument();
                    if (!(ciDoc instanceof LineDocument)) {
                        return ;
                    }
                    LineDocument doc = (LineDocument) ciDoc;
                    int rowStartOffset = LineDocumentUtils.getLineStartFromIndex(doc, lineNumber -1);

                    TreePath path = treeUtils.pathFor(rowStartOffset);
                    Tree tree = path.getLeaf();
                    Tree.Kind kind = tree.getKind();
                    if (kind == Tree.Kind.ERRONEOUS) {
                        return ;
                    }
                    int startOffs = (int)positions.getStartPosition(compUnit, tree);
                    int outerLineNumber = LineDocumentUtils.getLineIndex(doc, startOffs) + 1;
                    if (outerLineNumber == lineNumber) return;
                    if (kind == Tree.Kind.COMPILATION_UNIT || TreeUtilities.CLASS_TREE_KINDS.contains(kind)) return;
                    if (kind == Tree.Kind.BLOCK) {
                        BlockTree blockTree = (BlockTree)tree;
                        Tree previousTree = null;
                        int previousTreeEndOffset = -1;
                        for (StatementTree sTree : blockTree.getStatements()) {
                            int end = (int)positions.getStartPosition(compUnit, sTree);
                            if (end <= rowStartOffset && end > previousTreeEndOffset) {
                                previousTree = sTree;
                                previousTreeEndOffset = end;
                            } else if (end > rowStartOffset) {
                                break;
                            }
                        } // for
                        if (previousTree == null) {
                            tree = path.getParentPath().getLeaf();
                            kind = tree.getKind();
                            if (kind != Tree.Kind.COMPILATION_UNIT && !TreeUtilities.CLASS_TREE_KINDS.contains(kind)) {
                                previousTree = tree;
                            } else {
                                return;
                            }
                        }
                        startOffs = (int)positions.getStartPosition(compUnit, previousTree);
                        outerLineNumber = LineDocumentUtils.getLineIndex(doc, startOffs) + 1;
                    } // if
                    result[0] = outerLineNumber;
                }
            }, true);
        } catch (IOException ioex) {
            Exceptions.printStackTrace(ioex);
            return lineNumber;
        }
        return result[0];
    }

    private static LineBreakpoint findBreakpoint (String url, int lineNumber) {
        Breakpoint[] breakpoints = DebuggerManager.getDebuggerManager().getBreakpoints();
        for (int i = 0; i < breakpoints.length; i++) {
            if (!(breakpoints[i] instanceof LineBreakpoint)) {
                continue;
            }
            LineBreakpoint lb = (LineBreakpoint) breakpoints[i];
            if (!lb.getURL ().equals (url)) continue;
            if (lb.getLineNumber() == lineNumber) {
                return lb;
            }
        }
        return null;
    }

    private JPDAClassType getPreferredClassType(LineBreakpoint breakpoint) {
        try {
            Method getPreferredClassTypeMethod = breakpoint.getClass().getMethod("getPreferredClassType");
            getPreferredClassTypeMethod.setAccessible(true);
            return (JPDAClassType) getPreferredClassTypeMethod.invoke(breakpoint);
        } catch (NoSuchMethodException ex) {
            Exceptions.printStackTrace(ex);
            return null;
        } catch (SecurityException ex) {
            Exceptions.printStackTrace(ex);
            return null;
        } catch (IllegalAccessException ex) {
            Exceptions.printStackTrace(ex);
            return null;
        } catch (IllegalArgumentException ex) {
            Exceptions.printStackTrace(ex);
            return null;
        } catch (InvocationTargetException ex) {
            Exceptions.printStackTrace(ex);
            return null;
        }
        
    }

}

