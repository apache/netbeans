/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */
package org.netbeans.modules.debugger.jpda;

import com.sun.jdi.AbsentInformationException;
import com.sun.jdi.Location;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.StackFrame;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.debugger.jpda.jdi.InternalExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.InvalidStackFrameExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.ObjectCollectedExceptionWrapper;
import org.netbeans.spi.debugger.ContextProvider;

import org.netbeans.api.debugger.jpda.CallStackFrame;
import org.netbeans.api.debugger.jpda.Field;
import org.netbeans.api.debugger.jpda.JPDAClassType;
import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.api.debugger.jpda.JPDAThread;
import org.netbeans.modules.debugger.jpda.jdi.LocationWrapper;
import org.netbeans.modules.debugger.jpda.jdi.ReferenceTypeWrapper;
import org.netbeans.modules.debugger.jpda.jdi.StackFrameWrapper;
import org.netbeans.modules.debugger.jpda.jdi.VMDisconnectedExceptionWrapper;
import org.netbeans.modules.debugger.jpda.models.CallStackFrameImpl;
import org.netbeans.spi.debugger.jpda.SourcePathProvider;
import org.netbeans.spi.debugger.jpda.EditorContext;
import org.openide.util.Exceptions;

/**
 * Utility methods for sources.
 *
 * @see Similar class in debuggerjpda/ui when modifying this.
 *
 * @author Jan Jancura
 */
public class SourcePath {

    private ContextProvider          lookupProvider;
    private SourcePathProvider   contextProvider;
    private JPDADebugger            debugger;
    

    public SourcePath (ContextProvider lookupProvider) {
        this.lookupProvider = lookupProvider;
        debugger = lookupProvider.lookupFirst(null, JPDADebugger.class);
    }

    public SourcePathProvider getContext () {
        if (contextProvider == null) {
            List l = lookupProvider.lookup (null, SourcePathProvider.class);
            contextProvider = (SourcePathProvider) l.get (0);
            int i, k = l.size ();
            for (i = 1; i < k; i++) {
                contextProvider = new CompoundContextProvider (
                    (SourcePathProvider) l.get (i), 
                    contextProvider
                );
            }
        }
        return contextProvider;
    }

    
    // ContextProvider methods .................................................
    
    /**
     * Returns relative path for given url.
     *
     * @param url a url of resource file
     * @param directorySeparator a directory separator character
     * @param includeExtension whether the file extension should be included 
     *        in the result
     *
     * @return relative path
     */
    public String getRelativePath (
        String url, 
        char directorySeparator, 
        boolean includeExtension
    ) {
        return getContext ().getRelativePath 
            (url, directorySeparator, includeExtension);
    }
    
    /**
     * Returns the source root (if any) for given url.
     *
     * @param url a url of resource file
     *
     * @return the source root or <code>null</code> when no source root was found.
     */
    public String getSourceRoot(String url) {
        return getContext().getSourceRoot(url);
    }

    /**
     * Translates a relative path ("java/lang/Thread.java") to url 
     * ("file:///C:/Sources/java/lang/Thread.java"). Uses GlobalPathRegistry
     * if global == true.
     *
     * @param relativePath a relative path (java/lang/Thread.java)
     * @param global true if global path should be used
     * @return url
     */
    public String getURL (String relativePath, boolean global) {
        String url = getContext ().getURL (relativePath, global);
        if (url != null) {
            try {
                new java.net.URL(url);
            } catch (java.net.MalformedURLException muex) {
                Logger.getLogger(SourcePath.class.getName()).log(Level.WARNING,
                        "Malformed URL '"+url+"' produced by "+getContext (), muex);
                return null;
            }
        }
        return url;
    }
    
    public String getURL(JPDAClassType clazz, String stratum) {
        SourcePathProvider context = getContext();
        try {
            String url = (String) context.getClass().
                    getMethod("getURL", JPDAClassType.class, String.class).
                    invoke(context, clazz, stratum);
            if (url != null) {
                try {
                    new java.net.URL(url);
                } catch (java.net.MalformedURLException muex) {
                    Logger.getLogger(SourcePath.class.getName()).log(Level.WARNING,
                            "Malformed URL '"+url+"' produced by "+getContext (), muex);
                    return null;
                }
                return url;
            }
        } catch (NoSuchMethodException ex) {
        } catch (SecurityException ex) {
        } catch (IllegalAccessException ex) {
        } catch (IllegalArgumentException ex) {
        } catch (InvocationTargetException ex) {
        }
        String typePath = EditorContextBridge.getRelativePath (clazz.getName());
        return getURL(typePath, true);
    }
    
    public String getURL (
        CallStackFrame csf,
        String stratumn
    ) throws InternalExceptionWrapper, VMDisconnectedExceptionWrapper, InvalidStackFrameExceptionWrapper, ObjectCollectedExceptionWrapper {
        return getURL(StackFrameWrapper.location(((CallStackFrameImpl) csf).getStackFrame()), stratumn);
    }
    
    public String getURL (
        StackFrame sf,
        String stratumn
    ) throws InternalExceptionWrapper, VMDisconnectedExceptionWrapper, InvalidStackFrameExceptionWrapper, ObjectCollectedExceptionWrapper {
        return getURL(StackFrameWrapper.location(sf), stratumn);
    }
    
    public String getURL(JPDAThread t, String stratum) throws InternalExceptionWrapper, VMDisconnectedExceptionWrapper, InvalidStackFrameExceptionWrapper, ObjectCollectedExceptionWrapper {
        String url;
        try {
            CallStackFrame[] callStacks = t.getCallStack(0, 1);
            if (callStacks.length > 0) {
                url = getURL(callStacks[0], stratum);
            } else {
                String sourcePath = convertSlash (t.getSourcePath (stratum));
                url = getURL (sourcePath, true);
            }
        } catch (AbsentInformationException e) {
            String sourcePath = convertClassNameToRelativePath (t.getClassName ());
            url = getURL (sourcePath, true);
        }
        return url;
        
    }
    
    public String getURL (
        Location loc,
        String stratumn
    ) throws InternalExceptionWrapper, VMDisconnectedExceptionWrapper, ObjectCollectedExceptionWrapper {
        ReferenceType declaringType = LocationWrapper.declaringType(loc);
        JPDAClassType classType = ((JPDADebuggerImpl) debugger).getClassType(declaringType);
        return getURL(classType, loc, stratumn);
    }
    
    private String getURL (
        JPDAClassType classType,
        Location loc,
        String stratumn
    ) throws InternalExceptionWrapper, VMDisconnectedExceptionWrapper, ObjectCollectedExceptionWrapper {
        if (classType != null) {
            SourcePathProvider context = getContext();
            try {
                String url = (String) context.getClass().
                        getMethod("getURL", JPDAClassType.class, String.class).
                        invoke(context, classType, stratumn);
                if (url != null) {
                    try {
                        new java.net.URL(url);
                        return url;
                    } catch (java.net.MalformedURLException muex) {
                        Logger.getLogger(SourcePath.class.getName()).log(Level.WARNING,
                                "Malformed URL '"+url+"' produced by "+getContext (), muex);
                    }
                }
            } catch (NoSuchMethodException ex) {
            } catch (SecurityException ex) {
            } catch (IllegalAccessException ex) {
            } catch (IllegalArgumentException ex) {
            } catch (InvocationTargetException ex) {
            }
        }
        String sourcePath;
        if (loc == null) {
            sourcePath = EditorContextBridge.getRelativePath (classType.getName());
        } else {
            try {
                sourcePath = convertSlash(LocationWrapper.sourcePath(loc, stratumn));
            } catch (AbsentInformationException e) {
                sourcePath = convertClassNameToRelativePath (
                                 ReferenceTypeWrapper.name(LocationWrapper.declaringType(loc))
                             );
            }
        }
        return getURL(sourcePath, true);
    }
    
    /**
     * Returns array of source roots.
     */
    public String[] getSourceRoots () {
        return getContext ().getSourceRoots ();
    }
    
    /**
     * Sets array of source roots.
     *
     * @param sourceRoots a new array of sourceRoots
     */
    public void setSourceRoots (String[] sourceRoots) {
        getContext ().setSourceRoots (sourceRoots);
    }
    
    /**
     * Returns set of original source roots.
     *
     * @return set of original source roots
     */
    public String[] getOriginalSourceRoots () {
        return getContext ().getOriginalSourceRoots ();
    }
    
    /**
     * Returns the project's source roots.
     * 
     * @return array of source roots belonging to the project
     */
    public String[] getProjectSourceRoots() {
        try {
            java.lang.reflect.Method getProjectSourceRootsMethod = getContext().getClass().getMethod("getProjectSourceRoots", new Class[] {}); // NOI18N
            String[] projectSourceRoots = (String[]) getProjectSourceRootsMethod.invoke(getContext(), new Object[] {});
            return projectSourceRoots;
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
            return new String[] {};
        }
    }
    
    /**
     * Adds property change listener.
     *
     * @param l new listener.
     */
    public void addPropertyChangeListener (PropertyChangeListener l) {
        getContext ().addPropertyChangeListener (l);
    }

    /**
     * Removes property change listener.
     *
     * @param l removed listener.
     */
    public void removePropertyChangeListener (
        PropertyChangeListener l
    ) {
        getContext ().removePropertyChangeListener (l);
    }
    
    
    // utility methods .........................................................

    public static String convertSlash (String original) {
        return original.replace (File.separatorChar, '/');
    }

    public static String convertClassNameToRelativePath (
        String className
    ) {
        int i = className.indexOf ('$');
        if (i > 0) className = className.substring (0, i);
        String sourceName = className.replace
            ('.', '/') + ".java";
        return sourceName;
    }

    
    // innerclasses ............................................................

    private static class CompoundContextProvider extends SourcePathProvider {

        private SourcePathProvider cp1, cp2;

        CompoundContextProvider (
            SourcePathProvider cp1,
            SourcePathProvider cp2
        ) {
            this.cp1 = cp1;
            this.cp2 = cp2;
        }

        public String getURL (String relativePath, boolean global) {
            String p1 = cp1.getURL (relativePath, global);
            if (p1 != null) {
                try {
                    new java.net.URL(p1);
                    return p1;
                } catch (java.net.MalformedURLException muex) {
                    Logger.getLogger(SourcePath.class.getName()).log(Level.WARNING,
                            "Malformed URL '"+p1+"' produced by "+cp1, muex);
                }
            }
            p1 = cp2.getURL (relativePath, global);
            if (p1 != null) {
                try {
                    new java.net.URL(p1);
                } catch (java.net.MalformedURLException muex) {
                    Logger.getLogger(SourcePath.class.getName()).log(Level.WARNING,
                            "Malformed URL '"+p1+"' produced by "+cp2, muex);
                    p1 = null;
                }
            }
            return p1;
        }
        
        public String getURL(JPDAClassType clazz, String stratum) {
            try {
                java.lang.reflect.Method getURLMethod = cp1.getClass().getMethod("getURL", JPDAClassType.class, String.class); // NOI18N
                String url = (String) getURLMethod.invoke(cp1, clazz, stratum);
                if (url != null) {
                    return url;
                }
            } catch (IllegalAccessException ex) {
            } catch (IllegalArgumentException ex) {
            } catch (NoSuchMethodException ex) {
            } catch (SecurityException ex) {
            } catch (InvocationTargetException ex) {
            }
            try {
                java.lang.reflect.Method getURLMethod = cp2.getClass().getMethod("getURL", JPDAClassType.class, String.class); // NOI18N
                String url = (String) getURLMethod.invoke(cp2, clazz, stratum);
                if (url != null) {
                    return url;
                }
            } catch (IllegalAccessException ex) {
            } catch (IllegalArgumentException ex) {
            } catch (NoSuchMethodException ex) {
            } catch (SecurityException ex) {
            } catch (InvocationTargetException ex) {
            }
            return null;
        }

        public String getRelativePath (
            String url, 
            char directorySeparator, 
            boolean includeExtension
        ) {
            String p1 = cp1.getRelativePath (
                url, 
                directorySeparator, 
                includeExtension
            );
            if (p1 != null) return p1;
            return cp2.getRelativePath (
                url, 
                directorySeparator, 
                includeExtension
            );
        }

        public String getSourceRoot(String url) {
            String sourceRoot = cp1.getSourceRoot(url);
            if (sourceRoot == null) {
                sourceRoot = cp2.getSourceRoot(url);
            }
            return sourceRoot;
        }
        
        public String[] getSourceRoots () {
            String[] fs1 = cp1.getSourceRoots ();
            String[] fs2 = cp2.getSourceRoots ();
            String[] fs = new String [fs1.length + fs2.length];
            System.arraycopy (fs1, 0, fs, 0, fs1.length);
            System.arraycopy (fs2, 0, fs, fs1.length, fs2.length);
            return fs;
        }
    
        public String[] getOriginalSourceRoots () {
            String[] fs1 = cp1.getOriginalSourceRoots ();
            String[] fs2 = cp2.getOriginalSourceRoots ();
            String[] fs = new String [fs1.length + fs2.length];
            System.arraycopy (fs1, 0, fs, 0, fs1.length);
            System.arraycopy (fs2, 0, fs, fs1.length, fs2.length);
            return fs;
        }
        
        public String[] getProjectSourceRoots() {
            String[] projectSourceRoots1;
            String[] projectSourceRoots2;
            //System.err.println("\nCompoundContextProvider["+toString()+"].getProjectSourceRoots()...\n");
            try {
                java.lang.reflect.Method getProjectSourceRootsMethod = cp1.getClass().getMethod("getProjectSourceRoots", new Class[] {}); // NOI18N
                projectSourceRoots1 = (String[]) getProjectSourceRootsMethod.invoke(cp1, new Object[] {});
            } catch (Exception ex) {
                projectSourceRoots1 = new String[0];
            }
            try {
                java.lang.reflect.Method getProjectSourceRootsMethod = cp2.getClass().getMethod("getProjectSourceRoots", new Class[] {}); // NOI18N
                projectSourceRoots2 = (String[]) getProjectSourceRootsMethod.invoke(cp2, new Object[] {});
            } catch (Exception ex) {
                projectSourceRoots2 = new String[0];
            }
            if (projectSourceRoots1.length == 0) {
                //System.err.println("\nCompoundContextProvider.getProjectSourceRoots() = "+java.util.Arrays.toString(projectSourceRoots2)+"\n");
                return projectSourceRoots2;
            }
            if (projectSourceRoots2.length == 0) {
                //System.err.println("\nCompoundContextProvider.getProjectSourceRoots() = "+java.util.Arrays.toString(projectSourceRoots1)+"\n");
                return projectSourceRoots1;
            }
            String[] projectSourceRoots = new String[projectSourceRoots1.length + projectSourceRoots2.length];
            System.arraycopy (projectSourceRoots1, 0, projectSourceRoots, 0, projectSourceRoots1.length);
            System.arraycopy (projectSourceRoots2, 0, projectSourceRoots, projectSourceRoots1.length, projectSourceRoots2.length);
            //System.err.println("\nCompoundContextProvider.getProjectSourceRoots() = "+java.util.Arrays.toString(projectSourceRoots)+"\n");
            return projectSourceRoots;
        }

        public void setSourceRoots (String[] sourceRoots) {
            cp1.setSourceRoots (sourceRoots);
            cp2.setSourceRoots (sourceRoots);
        }

        public void addPropertyChangeListener (PropertyChangeListener l) {
            cp1.addPropertyChangeListener (l);
            cp2.addPropertyChangeListener (l);
        }

        public void removePropertyChangeListener (PropertyChangeListener l) {
            cp1.removePropertyChangeListener (l);
            cp2.removePropertyChangeListener (l);
        }

        @Override
        public String toString() {
            return "CompoundContextProvider["+cp1.toString()+", "+cp2.toString()+"]";
        }
        
        
    }

}

