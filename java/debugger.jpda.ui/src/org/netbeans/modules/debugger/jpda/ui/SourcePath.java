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
import java.beans.PropertyChangeListener;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import org.netbeans.spi.debugger.ContextProvider;

import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.jpda.CallStackFrame;
import org.netbeans.api.debugger.jpda.Field;
import org.netbeans.api.debugger.jpda.JPDAClassType;
import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.api.debugger.jpda.JPDAThread;
import org.netbeans.modules.debugger.jpda.models.CallStackFrameImpl;
import org.netbeans.spi.debugger.jpda.EditorContext;
import org.netbeans.spi.debugger.jpda.EditorContext.Operation;
import org.netbeans.spi.debugger.jpda.SourcePathProvider;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.NotifyDescriptor;
import org.openide.awt.StatusDisplayer;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 * Utility methods for sources.
 *
 * @see Similar class in debuggerjpda when modifying this.
 *
 * @author Jan Jancura
 */
public class SourcePath {

    private ContextProvider         contextProvider;
    private SourcePathProvider      sourcePathProvider;
    private JPDADebugger            debugger;
    

    public SourcePath (ContextProvider contextProvider) {
        this.contextProvider = contextProvider;
        debugger = contextProvider.lookupFirst(null, JPDADebugger.class);
        getContext();// To initialize the source path provider
    }

    private SourcePathProvider getContext () {
        if (sourcePathProvider == null) {
            List l = contextProvider.lookup (null, SourcePathProvider.class);
            sourcePathProvider = (SourcePathProvider) l.get (0);
            int i, k = l.size ();
            for (i = 1; i < k; i++) {
                sourcePathProvider = new CompoundContextProvider (
                    (SourcePathProvider) l.get (i), 
                    sourcePathProvider
                );
            }
            //initSourcePaths ();
        }
        return sourcePathProvider;
    }
    
    static SourcePathProvider getDefaultContext() {
        List providers = DebuggerManager.getDebuggerManager().
                lookup("netbeans-JPDASession", SourcePathProvider.class);
        for (Iterator it = providers.iterator(); it.hasNext(); ) {
            Object provider = it.next();
            // Hack - find our provider:
            if (provider.getClass().getName().equals("org.netbeans.modules.debugger.jpda.projects.SourcePathProviderImpl")) {
                return (SourcePathProvider) provider;
            }
        }
        return null;
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
     * Translates a relative path ("java/lang/Thread.java") to url 
     * ("file:///C:/Sources/java/lang/Thread.java"). Uses GlobalPathRegistry
     * if global == true.
     *
     * @param relativePath a relative path (java/lang/Thread.java)
     * @param global true if global path should be used
     * @return url
     */
    private String getURL (String relativePath, boolean global) {
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
     *
    public void setSourceRoots (String[] sourceRoots) {
        getContext ().setSourceRoots (sourceRoots);
    }
     */
    
    /**
     * Returns set of original source roots.
     *
     * @return set of original source roots
     */
    public String[] getOriginalSourceRoots () {
        return getContext ().getOriginalSourceRoots ();
    }

    public String[] getAdditionalSourceRoots() {
        try {
            java.lang.reflect.Method getProjectSourceRootsMethod = getContext().getClass().getMethod("getAdditionalSourceRoots", new Class[] {}); // NOI18N
            String[] projectSourceRoots = (String[]) getProjectSourceRootsMethod.invoke(getContext(), new Object[] {});
            return projectSourceRoots;
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
            return new String[] {};
        }

    }

    public void setSourceRoots (String[] sourceRoots, String[] additionalRoots) {
        try {
            java.lang.reflect.Method setSourceRootsMethod = getContext().getClass().getMethod("setSourceRoots", String[].class, String[].class); // NOI18N
            setSourceRootsMethod.invoke(getContext(), new Object[] { sourceRoots, additionalRoots });
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    public void reorderOriginalSourceRoots(int[] permutation) {
        try {
            java.lang.reflect.Method reorderOriginalSourceRootsMethod = getContext().getClass().getMethod("reorderOriginalSourceRoots", int[].class); // NOI18N
            reorderOriginalSourceRootsMethod.invoke(getContext(), new Object[] { permutation });
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
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

    public boolean sourceAvailable (
        String relativePath,
        boolean global
    ) {
        return getURL (relativePath, global) != null;
    }

    public boolean sourceAvailable (
        JPDAThread t,
        String stratumn,
        boolean global
    ) {
        try {
            CallStackFrame[] callStacks = t.getCallStack(0, 1);
            if (callStacks.length > 0) {
                String url = getClassURL(((CallStackFrameImpl) callStacks[0]).getClassType(), stratumn);
                if (url != null) {
                    return true;
                }
            }
        } catch (AbsentInformationException e) {}
        try {
            return sourceAvailable (
                convertSlash (t.getSourcePath (stratumn)), global
            );
        } catch (AbsentInformationException e) {
            return sourceAvailable (
                convertClassNameToRelativePath (t.getClassName ()), global
            );
        }
    }

    public boolean sourceAvailable (
        Field f
    ) {
        JPDAClassType declaringClass = f.getDeclaringClass();
        if (declaringClass != null) {
            String url = getClassURL(declaringClass, null);
            if (url != null) {
                return true;
            }
        }
        String className = f.getClassName ();
        return sourceAvailable (className, true);
    }

    public boolean sourceAvailable (
        CallStackFrame csf,
        String stratumn
    ) {
        String url = getClassURL(((CallStackFrameImpl) csf).getClassType(), stratumn);
        if (url != null) {
            return true;
        }
        try {
            return sourceAvailable (
                convertSlash (csf.getSourcePath (stratumn)), true
            );
        } catch (AbsentInformationException e) {
            return sourceAvailable (
                convertClassNameToRelativePath (csf.getClassName ()), true
            );
        }
    }

    public String getURL (
        CallStackFrame csf,
        String stratumn
    ) {
        return getURL(csf, stratumn, null);
    }
    
    public String getURL (
        CallStackFrame csf,
        String stratumn,
        String[] sourcePathPtr
    ) {
        JPDAClassType classType = ((CallStackFrameImpl) csf).getClassType();
        String url = null;
        if (classType != null) {
            url = getClassURL(classType, stratumn);
        }
        if (url == null) {
            String sourcePath;
            try {
                sourcePath = convertSlash (csf.getSourcePath (stratumn));
                url = getURL(sourcePath, true);
                if (url == null) {
                    String ds = csf.getDefaultStratum();
                    sourcePath = convertSlash (csf.getSourcePath (ds));
                    url = getURL(sourcePath, true);
                }
            } catch (AbsentInformationException e) {
                sourcePath = convertClassNameToRelativePath (csf.getClassName ());
                url = getURL(sourcePath, true);
            }
            
            if (sourcePathPtr != null) {
                sourcePathPtr[0] = sourcePath;
            }
        }
        return url;
    }
    
    public String getURL(JPDAThread t, String stratum) {
        return getURL(t, stratum, null);
    }
    
    private String getURL(JPDAThread t, String stratum, String[] sourcePathPtr) {
        String url;
        try {
            CallStackFrame[] callStacks = t.getCallStack(0, 1);
            if (callStacks.length > 0) {
                url = getURL(callStacks[0], stratum, sourcePathPtr);
            } else {
                String sourcePath = convertSlash (t.getSourcePath (stratum));
                url = getURL (sourcePath, true);
                if (sourcePathPtr != null) {
                    sourcePathPtr[0] = sourcePath;
                }
            }
        } catch (AbsentInformationException e) {
            String sourcePath = convertClassNameToRelativePath (t.getClassName ());
            url = getURL (sourcePath, true);
            if (sourcePathPtr != null) {
                sourcePathPtr[0] = sourcePath;
            }
        }
        return url;
        
    }
    
    private String getClassURL(JPDAClassType clazz, String stratum) {
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
        return null;
    }

    /** Do not call in AWT */
    public void showSource (
        JPDAThread t,
        String stratumn
    ) {
        int lineNumber = t.getLineNumber (stratumn);
        if (lineNumber < 1) lineNumber = 1;
        String[] sourcePathPtr = new String[1];
        String url = getURL(t, stratumn, sourcePathPtr);
        if (url == null) {
            String message = NbBundle.getMessage(SourcePath.class, "No_URL_Warning", sourcePathPtr[0]);
            ErrorManager.getDefault().log(ErrorManager.WARNING, message);
            StatusDisplayer.getDefault().setStatusText(message);
            return ;
        }
        final int ln = lineNumber;
        final String u = url;
        SwingUtilities.invokeLater (new Runnable () {
            public void run () {
                EditorContextBridge.getContext().showSource (
                    u,
                    ln,
                    debugger
                );
            }
        });
    }

    /** Do not call in AWT */
    public void showSource (CallStackFrame csf, String stratumn) {
        String url = null;
        int lineNumber;
        JPDAClassType classType = ((CallStackFrameImpl) csf).getClassType();
        if (classType != null) {
            url = getClassURL(classType, stratumn);
        }
        if (url == null) {
            try {
                url = getURL (
                    convertSlash (csf.getSourcePath (stratumn)), true
                );
                if (url == null) {
                    stratumn = csf.getDefaultStratum ();
                    url = getURL (
                        convertSlash (csf.getSourcePath (stratumn)), true
                    );
                }
                if (url == null) {
                    String message = NbBundle.getMessage(SourcePath.class,
                                                         "No_URL_Warning",
                                                         csf.getSourcePath (stratumn));
                    ErrorManager.getDefault().log(ErrorManager.WARNING, message);
                    StatusDisplayer.getDefault().setStatusText(message);
                    return ;
                }
            } catch (AbsentInformationException e) {
                url = getURL (
                    convertClassNameToRelativePath (csf.getClassName ()), true
                );
                if (url == null) {
                    String message = NbBundle.getMessage(SourcePath.class,
                                                         "No_URL_Warning",
                                                         csf.getClassName());
                    ErrorManager.getDefault().log(ErrorManager.WARNING, message);
                    StatusDisplayer.getDefault().setStatusText(message);
                    return ;
                }
            }
        }
        lineNumber = csf.getLineNumber (stratumn);
        if (lineNumber < 1) lineNumber = 1;
        final int ln = lineNumber;
        final String u = url;
        SwingUtilities.invokeLater (new Runnable () {
            public void run () {
                EditorContextBridge.getContext().showSource (
                    u,
                    ln,
                    debugger
                );
            }
        });
    }

    public void showSource (Field v) {
        showSource(v, true);
    }
    
    public void showSource (Field v, final boolean reportUnknownSource) {
        String fieldName = ((Field) v).getName ();
        String url = null;
        JPDAClassType declaringClass = v.getDeclaringClass();
        if (declaringClass != null) {
            url = getClassURL(declaringClass, null);
        }
        final String className = ((Field) v).getClassName ();
        final String sourcePath = EditorContextBridge.getRelativePath (className);
        if (url == null) {
            url = getURL(sourcePath, true);
        }
        if (url == null) return ;
        int lineNumber = lineNumber = EditorContextBridge.getContext().getFieldLineNumber (
            url,
            className,
            fieldName
        );
        if (lineNumber < 1) lineNumber = 1;
        
        final int ln = lineNumber;
        final String u = url;
        SwingUtilities.invokeLater (new Runnable () {
            public void run () {
                boolean success = EditorContextBridge.getContext().showSource (
                    u,
                    ln,
                    debugger
                );
                if (reportUnknownSource && !success) {
                    String message = NbBundle.getMessage(SourcePath.class, "No_URL_Warning", sourcePath);
                    NotifyDescriptor d = new NotifyDescriptor.Message(message, NotifyDescriptor.WARNING_MESSAGE);
                    DialogDisplayer.getDefault().notifyLater(d);
                }
            }
        });
    }

    static String convertSlash (String original) {
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

    public Object annotate (
        JPDAThread t,
        String stratumn,
        String url,
        int lineNumber
    ) {
        return annotate(t, stratumn, url, lineNumber, true);
    }
    
    public Object annotate (
        JPDAThread t,
        String stratumn,
        String url,
        int lineNumber,
        boolean isCurrent
    ) {
        Operation operation;
        List operationsAnn;
        if (isCurrent) {
            operation = t.getCurrentOperation();
            operationsAnn = annotateOperations(debugger, url, operation, t.getLastOperations(), lineNumber);
        } else {
            operation = null;
            operationsAnn = Collections.EMPTY_LIST;
        }
        if (operation == null) {
            if (operationsAnn.size() == 0) {
                return EditorContextBridge.getContext().annotate (
                    url,
                    lineNumber,
                    isCurrent ?
                        EditorContext.CURRENT_LINE_ANNOTATION_TYPE :
                        EditorContext.OTHER_THREAD_ANNOTATION_TYPE,
                    debugger,
                    t
                );
            }
        }
        return operationsAnn;
    }

    public Object annotate (
        CallStackFrame csf,
        String stratumn
    ) {
        int lineNumber = csf.getLineNumber (stratumn);
        if (lineNumber < 1) return null;
        Operation operation = csf.getCurrentOperation(stratumn);
//        try {
            if (operation != null) {
                int startOffset;
                int endOffset;
                if (operation.getMethodName() != null) {
                    startOffset = operation.getMethodStartPosition().getOffset();
                    endOffset = operation.getMethodEndPosition().getOffset();
                } else {
                    startOffset = operation.getStartPosition().getOffset();
                    endOffset = operation.getEndPosition().getOffset();
                }
                String url = getURL(csf, stratumn);
                return EditorContextBridge.getContext().annotate (
                    url,
                    startOffset,
                    endOffset,
                    EditorContext.CALL_STACK_FRAME_ANNOTATION_TYPE,
                    debugger
                );
            } else {
                String url = getURL(csf, stratumn);
                return EditorContextBridge.getContext().annotate (
                    url,
                    lineNumber,
                    EditorContext.CALL_STACK_FRAME_ANNOTATION_TYPE,
                    debugger
                );
            }
//        } catch (AbsentInformationException e) {
//            return EditorContextBridge.getContext().annotate (
//                getURL (
//                    convertClassNameToRelativePath (csf.getClassName ()), true
//                ),
//                lineNumber,
//                EditorContext.CALL_STACK_FRAME_ANNOTATION_TYPE,
//                debugger
//            );
//        }
    }
    
    private static List annotateOperations(JPDADebugger debugger, String url,
                                           Operation currentOperation, List lastOperations,
                                           int locLineNumber) {
        List<Object> annotations = null;
        int currentOperationLine = -1;
        if (currentOperation != null) {
            annotations = new ArrayList<>();
            Object ann = createAnnotation(debugger, url, currentOperation,
                                          EditorContext.CURRENT_LINE_ANNOTATION_TYPE,
                                          true);
            if (ann != null) annotations.add(ann);
            int lineNumber;
            if (currentOperation.getMethodName() != null) {
                lineNumber = currentOperation.getMethodStartPosition().getLine();
            } else {
                lineNumber = currentOperation.getStartPosition().getLine();
            }
            ann = EditorContextBridge.getContext().annotate (
                url,
                lineNumber,
                EditorContext.CURRENT_EXPRESSION_CURRENT_LINE_ANNOTATION_TYPE,
                debugger
            );
            currentOperationLine = lineNumber;
            if (ann != null) annotations.add(ann);
        }
        boolean isNewLineExp = false;
        if (lastOperations != null && lastOperations.size() > 0) {
            if (annotations == null) {
                annotations = new ArrayList<>();
            }
            isNewLineExp = currentOperation == null;
            for (int i = 0; i < lastOperations.size(); i++) {
                Operation lastOperation = (Operation) lastOperations.get(i);
                if (currentOperation == lastOperation && i == lastOperations.size() - 1) {
                    Object ann = createAnnotation(debugger, url,
                                                  lastOperation,
                                                  EditorContext.CURRENT_OUT_OPERATION_ANNOTATION_TYPE,
                                                  false);
                    if (ann != null) annotations.add(ann);
                    int lineNumber = lastOperation.getEndPosition().getLine();
                    if (currentOperationLine != lineNumber) {
                        ann = EditorContextBridge.getContext().annotate (
                            url,
                            lineNumber,
                            EditorContext.CURRENT_EXPRESSION_CURRENT_LINE_ANNOTATION_TYPE,
                            debugger
                        );
                        if (ann != null) annotations.add(ann);
                    }
                    isNewLineExp = false;
                } else {
                    Object ann = createAnnotation(debugger, url,
                                                  lastOperation,
                                                  EditorContext.CURRENT_LAST_OPERATION_ANNOTATION_TYPE,
                                                  true);
                    if (ann != null) annotations.add(ann);
                }
            }
        }
        if (isNewLineExp) {
            Object ann = EditorContextBridge.getContext().annotate (
                url,
                locLineNumber,
                EditorContext.CURRENT_LINE_ANNOTATION_TYPE,
                debugger
            );
            if (ann != null) annotations.add(ann);
        }
        if (annotations != null) {
            return annotations;
        } else {
            return Collections.EMPTY_LIST;
        }
    }
    
    private static Object createAnnotation(JPDADebugger debugger, String url,
                                           Operation operation, String type,
                                           boolean method) {
        int startOffset;
        int endOffset;
        if (method && operation.getMethodName() != null) {
            startOffset = operation.getMethodStartPosition().getOffset();
            endOffset = operation.getMethodEndPosition().getOffset();
        } else {
            startOffset = operation.getStartPosition().getOffset();
            endOffset = operation.getEndPosition().getOffset();
        }
        return EditorContextBridge.getContext().annotate (
            url,
            startOffset,
            endOffset,
            type,
            debugger
        );
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

        public void setSourceRoots (String[] sourceRoots) {
            cp1.setSourceRoots (sourceRoots);
            cp2.setSourceRoots (sourceRoots);
        }

        public String[] getAdditionalSourceRoots() {
            String[] additionalSourceRoots1;
            String[] additionalSourceRoots2;
            //System.err.println("\nCompoundContextProvider["+toString()+"].getadditionalSourceRoots()...\n");
            try {
                java.lang.reflect.Method getAdditionalSourceRootsMethod = cp1.getClass().getMethod("getAdditionalSourceRoots", new Class[] {}); // NOI18N
                additionalSourceRoots1 = (String[]) getAdditionalSourceRootsMethod.invoke(cp1, new Object[] {});
            } catch (Exception ex) {
                additionalSourceRoots1 = new String[0];
            }
            try {
                java.lang.reflect.Method getAdditionalSourceRootsMethod = cp2.getClass().getMethod("getAdditionalSourceRoots", new Class[] {}); // NOI18N
                additionalSourceRoots2 = (String[]) getAdditionalSourceRootsMethod.invoke(cp2, new Object[] {});
            } catch (Exception ex) {
                additionalSourceRoots2 = new String[0];
            }
            if (additionalSourceRoots1.length == 0) {
                //System.err.println("\nCompoundContextProvider.getAdditionalSourceRoots() = "+java.util.Arrays.toString(additionalSourceRoots2)+"\n");
                return additionalSourceRoots2;
            }
            if (additionalSourceRoots2.length == 0) {
                //System.err.println("\nCompoundContextProvider.getAdditionalSourceRoots() = "+java.util.Arrays.toString(additionalSourceRoots1)+"\n");
                return additionalSourceRoots1;
            }
            String[] additionalSourceRoots = new String[additionalSourceRoots1.length + additionalSourceRoots2.length];
            System.arraycopy (additionalSourceRoots1, 0, additionalSourceRoots, 0, additionalSourceRoots1.length);
            System.arraycopy (additionalSourceRoots2, 0, additionalSourceRoots, additionalSourceRoots1.length, additionalSourceRoots2.length);
            //System.err.println("\nCompoundContextProvider.getAdditionalSourceRoots() = "+java.util.Arrays.toString(additionalSourceRoots)+"\n");
            return additionalSourceRoots;
        }

        public void setSourceRoots (String[] sourceRoots, String[] additionalRoots) {
            try {
                java.lang.reflect.Method setSourceRootsMethod = cp1.getClass().getMethod("setSourceRoots", String[].class, String[].class); // NOI18N
                setSourceRootsMethod.invoke(cp1, new Object[] { sourceRoots, additionalRoots });
            } catch (Exception ex) {
                cp1.setSourceRoots(sourceRoots);
            }
            try {
                java.lang.reflect.Method setSourceRootsMethod = cp2.getClass().getMethod("setSourceRoots", String[].class, String[].class); // NOI18N
                setSourceRootsMethod.invoke(cp2, new Object[] { sourceRoots, additionalRoots });
            } catch (Exception ex) {
                cp2.setSourceRoots(sourceRoots);
            }
        }

        public void reorderOriginalSourceRoots(int[] permutation) {
            try {
                java.lang.reflect.Method reorderOriginalSourceRootsMethod = cp1.getClass().getMethod("reorderOriginalSourceRoots", int[].class); // NOI18N
                reorderOriginalSourceRootsMethod.invoke(cp1, new Object[] { permutation });
            } catch (Exception ex) {

            }
            try {
                java.lang.reflect.Method reorderOriginalSourceRootsMethod = cp2.getClass().getMethod("reorderOriginalSourceRoots", int[].class); // NOI18N
                reorderOriginalSourceRootsMethod.invoke(cp2, new Object[] { permutation });
            } catch (Exception ex) {

            }
        }

        public void addPropertyChangeListener (PropertyChangeListener l) {
            cp1.addPropertyChangeListener (l);
            cp2.addPropertyChangeListener (l);
        }

        public void removePropertyChangeListener (PropertyChangeListener l) {
            cp1.removePropertyChangeListener (l);
            cp2.removePropertyChangeListener (l);
        }
    }

    /*
    private void initSourcePaths () {
        Properties properties = Properties.getDefault ().
            getProperties ("debugger").getProperties ("sources");
        Set originalSourceRoots = new HashSet (Arrays.asList (
            sourcePathProvider.getOriginalSourceRoots ()
        ));
        Set sourceRoots = new HashSet (Arrays.asList (
            sourcePathProvider.getSourceRoots ()
        ));

        Iterator enabledSourceRoots = properties.getProperties ("source_roots").
            getCollection ("enabled", Collections.EMPTY_SET).iterator ();
        while (enabledSourceRoots.hasNext ()) {
            String root = (String) enabledSourceRoots.next ();
            if (originalSourceRoots.contains (root)) 
                sourceRoots.add (root);
        }
        Iterator additionalSourceRoots = properties.getProperties("additional_source_roots").
                getCollection("src_roots", Collections.EMPTY_LIST).iterator();
        while (additionalSourceRoots.hasNext()) {
            String root = (String) additionalSourceRoots.next ();
            sourceRoots.add (root);
        }
        Iterator disabledSourceRoots = properties.getProperties ("source_roots").
            getCollection ("disabled", Collections.EMPTY_SET).iterator ();
        while (disabledSourceRoots.hasNext ()) {
            String root = (String) disabledSourceRoots.next ();
            sourceRoots.remove (root);
        }
        String[] ss = new String [sourceRoots.size ()];
        sourcePathProvider.setSourceRoots ((String[]) sourceRoots.toArray (ss));
    }
     */

    private static class CompoundAnnotation {
        Object annotation1;
        Object annotation2;
    }
}

