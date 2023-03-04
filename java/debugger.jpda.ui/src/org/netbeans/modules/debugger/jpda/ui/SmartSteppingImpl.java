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

package org.netbeans.modules.debugger.jpda.ui;

import com.sun.jdi.AbsentInformationException;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.netbeans.api.debugger.jpda.JPDAThread;
import org.netbeans.api.debugger.jpda.SmartSteppingFilter;
import org.netbeans.spi.debugger.jpda.SourcePathProvider;
import org.netbeans.api.debugger.DebuggerEngine;
import org.netbeans.spi.debugger.ContextProvider;
import org.netbeans.api.debugger.Session;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.classpath.GlobalPathRegistry;
import org.netbeans.spi.debugger.jpda.SmartSteppingCallback;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;


@SmartSteppingCallback.Registration(path="netbeans-JPDASession")
public class SmartSteppingImpl extends SmartSteppingCallback implements 
PropertyChangeListener {
    
    
    private Set exclusionPatterns = new HashSet (); 
    private SmartSteppingFilter smartSteppingFilter;
    
    
    /**
     * Defines default set of smart stepping filters. Method is called when 
     * a new JPDA debugger session is created.
     *
     * @param f a filter to be initialized
     */
    public void initFilter (SmartSteppingFilter f) {
        smartSteppingFilter = f;
    }
    
    /**
     * This method is called during stepping through debugged application.
     * The execution is stopped when all registered SmartSteppingListeners
     * returns true.
     *
     * @param thread contains all available information about current position
     *        in debugged application
     * @param f a filter
     * @return true if execution should be stopped on the current position
     */
    public boolean stopHere (
        ContextProvider lookupProvider, 
        JPDAThread thread, 
        SmartSteppingFilter f
    ) {
        String className = thread.getClassName ();
        if (className == null) return false;

        SourcePath ectx = getEngineContext (lookupProvider);
        boolean b = ectx.sourceAvailable (thread, null, false);
        if (b) return true;

        try {
            String sourcePath = thread.getSourcePath(null);
            sourcePath = sourcePath.replace(java.io.File.pathSeparatorChar, '/');
            FileObject sourceFO = GlobalPathRegistry.getDefault().findResource(sourcePath);
            if (sourceFO != null) {
                Set<ClassPath> cpaths = GlobalPathRegistry.getDefault().getPaths(ClassPath.SOURCE);
                FileObject rootFO = null;
                for (ClassPath cp : cpaths) {
                    FileObject fo = cp.findOwnerRoot(sourceFO);
                    if (fo != null) {
                        if (rootFO == null) {
                            rootFO = fo;
                        } else {
                            // More than one source root
                            rootFO = null;
                            break;
                        }
                    }
                }
                if (rootFO != null) {
                    java.io.File file = FileUtil.toFile(rootFO);
                    String sourceRoot = file.getAbsolutePath();

                    String[] additionalSourceRoots = ectx.getAdditionalSourceRoots();
                    String[] originalSourceRoots = ectx.getOriginalSourceRoots();
                    if (Arrays.asList(additionalSourceRoots).contains(sourceRoot)) {
                        // Source root is known, but disabled.
                        return false;
                    }
                    if (Arrays.asList(originalSourceRoots).contains(sourceRoot)) {
                        // Source root is known, but disabled.
                        return false;
                    }
                    String[] sourceRoots = ectx.getSourceRoots();
                    String[] new_additionalSourceRoots = new String[additionalSourceRoots.length + 1];
                    String[] new_sourceRoots = new String[sourceRoots.length + 1];
                    System.arraycopy(additionalSourceRoots, 0, new_additionalSourceRoots, 0, additionalSourceRoots.length);
                    System.arraycopy(sourceRoots, 0, new_sourceRoots, 0, sourceRoots.length);
                    new_additionalSourceRoots[additionalSourceRoots.length] = sourceRoot;
                    new_sourceRoots[sourceRoots.length] = sourceRoot;
                    ectx.setSourceRoots(new_sourceRoots, new_additionalSourceRoots);
                    return true;
                }
            }
        } catch (AbsentInformationException ex) {
        }
        
        
        // find pattern
        String name, n1 = className.replace ('.', '/');
        /*
        do {
            name = n1;
            int i = name.lastIndexOf ('/');
            if (i < 0) break;
            n1 = name.substring (0, i);
        } while (!ectx.sourceAvailable (n1, false));
               */
        name = n1;
        int i = name.lastIndexOf ('/');
        if (i > 0) {
            n1 = name.substring (0, i);
            if (!ectx.sourceAvailable (n1, false)) {
                name = n1;
            }
        }
            
        HashSet s = new HashSet ();
        s.add (name.replace ('/', '.') + ".*");
        addExclusionPatterns (s, f);
        return false;
    }
    
    private void addExclusionPatterns(Set ep, SmartSteppingFilter f) {
        f.addExclusionPatterns (ep);
        if (f == smartSteppingFilter) {
            exclusionPatterns.addAll (ep);
        }
    }
    
    private void removeExclusionPatterns () {
        smartSteppingFilter.removeExclusionPatterns (exclusionPatterns);
        exclusionPatterns = new HashSet ();
    }
    
    private SourcePath engineContext;
    
    private SourcePath getEngineContext (ContextProvider lookupProvider) {
        if (engineContext == null) {
            engineContext = lookupProvider.lookupFirst(null, SourcePath.class);
            engineContext.addPropertyChangeListener (this);
        }
        return engineContext;
    }
    
    public void propertyChange (PropertyChangeEvent evt) {
        if (evt.getPropertyName () == SourcePathProvider.PROP_SOURCE_ROOTS) {
            removeExclusionPatterns ();
        }
    }
}
