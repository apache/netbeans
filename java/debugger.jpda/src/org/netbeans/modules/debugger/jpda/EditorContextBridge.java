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
package org.netbeans.modules.debugger.jpda;

import com.sun.jdi.AbsentInformationException;
import org.netbeans.api.java.source.support.ErrorAwareTreePathScanner;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.function.Function;

import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.jpda.LineBreakpoint;
import org.netbeans.api.debugger.jpda.CallStackFrame;
import org.netbeans.api.debugger.jpda.InvalidExpressionException;
import org.netbeans.api.debugger.jpda.JPDAThread;
import org.netbeans.spi.debugger.jpda.EditorContext;
import org.netbeans.spi.debugger.jpda.EditorContext.MethodArgument;
import org.netbeans.spi.debugger.jpda.EditorContext.Operation;
import org.netbeans.spi.debugger.jpda.Evaluator.Expression;
import org.netbeans.spi.debugger.jpda.SourcePathProvider;
import org.openide.util.Exceptions;
import org.openide.util.Pair;


/**
 *
 * @author Jan Jancura
 */
public class EditorContextBridge {

    public static final String FIELD = "field";
    public static final String METHOD = "method";
    public static final String CLASS = "class";
    public static final String LINE = "line";

    private static EditorContext context;

    public static EditorContext getContext () {
        if (context == null) {
            List l = DebuggerManager.getDebuggerManager ().lookup
                (null, EditorContext.class);
            context = (EditorContext) l.get (0);
            int i, k = l.size ();
            for (i = 1; i < k; i++)
                context = new CompoundContextProvider (
                    (EditorContext) l.get (i),
                    context
                );
        }
        return context;
    }

    /**
     * Returns the parsed expression tree or <code>null</code>.
     *
     * @return the parsed expression tree or <code>null</code>
     *
    public static Tree getExpressionTree(final String expression, String url, final int line) {
        // TODO: return getContext ().getExpressionTree ();
        try {
            return (Tree) getContext ().getClass().getMethod("getExpressionTree", new Class[] { String.class, String.class, Integer.TYPE }).
                invoke(getContext(), new Object[] { expression, url, line });
        } catch (java.lang.reflect.InvocationTargetException itex) {
            Throwable tex = itex.getTargetException();
            if (tex instanceof RuntimeException) {
                throw (RuntimeException) tex;
            } else {
                ErrorManager.getDefault().notify(tex);
                return null;
            }
        } catch (Exception ex) {
            ErrorManager.getDefault().notify(ex);
            return null;
        }
    }
     */

    public static <R,D> R interpretOrCompileCode(final Expression<Object> expression,
                                                 final String url, final int line,
                                                 final ErrorAwareTreePathScanner<Boolean,D> canInterpret,
                                                 final ErrorAwareTreePathScanner<R,D> interpreter,
                                                 final D context, final boolean staticContext,
                                                 final Function<Pair<String, byte[]>, Boolean> compiledClassHandler,
                                                 final SourcePathProvider sp) throws InvalidExpressionException {
        try {
            return (R) getContext ().getClass().getMethod(
                    "interpretOrCompileCode",
                    new Class[] { Expression.class, String.class, Integer.TYPE,
                                  ErrorAwareTreePathScanner.class, ErrorAwareTreePathScanner.class,
                                  Object.class, Boolean.TYPE, Function.class,
                                  SourcePathProvider.class }).
                        invoke(getContext(), new Object[] { expression, url, line,
                                                            canInterpret,
                                                            interpreter,
                                                            context, staticContext,
                                                            compiledClassHandler,
                                                            sp });
        } catch (java.lang.reflect.InvocationTargetException itex) {
            Throwable tex = itex.getTargetException();
            if (tex instanceof RuntimeException) {
                throw (RuntimeException) tex;
            } else if (tex instanceof InvalidExpressionException) {
                throw ((InvalidExpressionException) tex);
            } else {
                Exceptions.printStackTrace(tex);
                return null;
            }
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
            return null;
        }
    }

    // Utility methods .........................................................

    public static String getFileName (LineBreakpoint b) {
        try {
            return new File (new URL (b.getURL ()).getFile ()).getName ();
        } catch (MalformedURLException e) {
            return null;
        }
    }

    public static boolean showSource (LineBreakpoint b, Object timeStamp) {
        if (b.getLineNumber () < 1)
            return getContext().showSource (
                b.getURL (),
                1,
                timeStamp
            );
        return getContext().showSource (
            b.getURL (),
            b.getLineNumber (),
            timeStamp
        );
    }

    public static String getRelativePath (
        JPDAThread thread,
        String stratumn
    ) {
        try {
            return convertSlash (thread.getSourcePath (stratumn));
        } catch (AbsentInformationException e) {
            return getRelativePath (thread.getClassName ());
        }
    }

    public static String getRelativePath (
        CallStackFrame csf,
        String stratumn
    ) {
        try {
            return convertSlash (csf.getSourcePath (stratumn));
        } catch (AbsentInformationException e) {
            return getRelativePath (csf.getClassName ());
        }
    }

    public static String getRelativePath (
        String className
    ) {
        int i = className.indexOf ('$');
        if (i > 0) className = className.substring (0, i);
        String sourceName = className.replace
            ('.', '/') + ".java";
        return sourceName;
    }

    private static String convertSlash (String original) {
        return original.replace (File.separatorChar, '/');
    }

    public static int getCurrentOffset() {
        // TODO: return getContext ().getCurrentOffset();
        try {
            return (Integer) getContext ().getClass().getMethod("getCurrentOffset", new Class[] {}).
                    invoke(getContext(), new Object[] {});
        } catch (java.lang.reflect.InvocationTargetException itex) {
            Throwable tex = itex.getTargetException();
            if (tex instanceof RuntimeException) {
                throw (RuntimeException) tex;
            } else {
                Exceptions.printStackTrace(tex);
                return 0;
            }
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
            return 0;
        }
    }


    // innerclasses ............................................................

    private static class CompoundContextProvider extends EditorContext {

        private EditorContext cp1, cp2;

        CompoundContextProvider (
            EditorContext cp1,
            EditorContext cp2
        ) {
            this.cp1 = cp1;
            this.cp2 = cp2;
        }

        public void createTimeStamp (Object timeStamp) {
            cp1.createTimeStamp (timeStamp);
            cp2.createTimeStamp (timeStamp);
        }

        public void disposeTimeStamp (Object timeStamp) {
            cp1.disposeTimeStamp (timeStamp);
            cp2.disposeTimeStamp (timeStamp);
        }

        public void updateTimeStamp (Object timeStamp, String url) {
            cp1.updateTimeStamp (timeStamp, url);
            cp2.updateTimeStamp (timeStamp, url);
        }

        public String getCurrentClassName () {
            String s = cp1.getCurrentClassName ();
            if (s.trim ().length () < 1)
                return cp2.getCurrentClassName ();
            return s;
        }

        public String getCurrentURL () {
            String s = cp1.getCurrentURL ();
            if (s.trim ().length () < 1)
                return cp2.getCurrentURL ();
            return s;
        }

        public String getCurrentFieldName () {
            String s = cp1.getCurrentFieldName ();
            if ( (s == null) || (s.trim ().length () < 1))
                return cp2.getCurrentFieldName ();
            return s;
        }

        public int getCurrentLineNumber () {
            int i = cp1.getCurrentLineNumber ();
            if (i < 1)
                return cp2.getCurrentLineNumber ();
            return i;
        }

        public int getCurrentOffset() {
            Integer i = null;
            try {
                i = (Integer) cp1.getClass().getMethod("getCurrentOffset", new Class[] {}).
                        invoke(cp1, new Object[] {});
            } catch (java.lang.reflect.InvocationTargetException itex) {
                Throwable tex = itex.getTargetException();
                if (tex instanceof RuntimeException) {
                    throw (RuntimeException) tex;
                } else {
                    Exceptions.printStackTrace(tex);
                    return 0;
                }
            } catch (Exception ex) {
                // Ignore, we have another attempt with cp2
                //ErrorManager.getDefault().notify(ex);
            }
            if (i == null || i.intValue() < 1) {
                try {
                    i = (Integer) cp2.getClass().getMethod("getCurrentOffset", new Class[] {}).
                            invoke(cp2, new Object[] {});
                } catch (java.lang.reflect.InvocationTargetException itex) {
                    Throwable tex = itex.getTargetException();
                    if (tex instanceof RuntimeException) {
                        throw (RuntimeException) tex;
                    } else {
                        Exceptions.printStackTrace(tex);
                        return 0;
                    }
                } catch (Exception ex) {
                    Exceptions.printStackTrace(ex);
                    return 0;
                }
            }
            return i.intValue();
        }

        public String getCurrentMethodName () {
            String s = cp1.getCurrentMethodName ();
            if ( (s == null) || (s.trim ().length () < 1))
                return cp2.getCurrentMethodName ();
            return s;
        }

        public String getSelectedIdentifier () {
            String s = cp1.getSelectedIdentifier ();
            if ( (s == null) || (s.trim ().length () < 1))
                return cp2.getSelectedIdentifier ();
            return s;
        }

        public String getSelectedMethodName () {
            String s = cp1.getSelectedMethodName ();
            if ( (s == null) || (s.trim ().length () < 1))
                return cp2.getSelectedMethodName ();
            return s;
        }

        public void removeAnnotation (Object annotation) {
            CompoundAnnotation ca = (CompoundAnnotation) annotation;
            cp1.removeAnnotation (ca.annotation1);
            cp2.removeAnnotation (ca.annotation2);
        }

        public Object annotate (
            String sourceName,
            int lineNumber,
            String annotationType,
            Object timeStamp
        ) {
            CompoundAnnotation ca = new CompoundAnnotation ();
            ca.annotation1 = cp1.annotate
                (sourceName, lineNumber, annotationType, timeStamp);
            ca.annotation2 = cp2.annotate
                (sourceName, lineNumber, annotationType, timeStamp);
            return ca;
        }

        public <R,D> R interpretOrCompileCode(final Expression<Object> expression,
                                              final String url, final int line,
                                              final ErrorAwareTreePathScanner<Boolean,D> canInterpret,
                                              final ErrorAwareTreePathScanner<R,D> interpreter,
                                              final D context, final boolean staticContext,
                                              final Function<Pair<String, byte[]>, Boolean> compiledClassHandler,
                                              final SourcePathProvider sp) throws InvalidExpressionException {
            R ret = null;
            try {
                ret = (R) cp1.getClass().getMethod(
                    "interpretOrCompileCode",
                    new Class[] { Expression.class, String.class, Integer.TYPE, ErrorAwareTreePathScanner.class,
                                  ErrorAwareTreePathScanner.class, Object.class, Boolean.TYPE, Function.class,
                                  SourcePathProvider.class }).
                        invoke(cp1, new Object[] { expression, url, line, canInterpret, interpreter,
                                                   context, staticContext, compiledClassHandler, sp });
            } catch (java.lang.reflect.InvocationTargetException itex) {
                Throwable tex = itex.getTargetException();
                if (tex instanceof RuntimeException) {
                    throw (RuntimeException) tex;
                } else {
                    Exceptions.printStackTrace(tex);
                }
            } catch (Exception ex) {
                // Ignore, we have another attempt with cp2
            }
            if (ret == null) {
                try {
                    ret = (R) cp2.getClass().getMethod(
                    "interpretOrCompileCode",
                    new Class[] { Expression.class, String.class, Integer.TYPE, ErrorAwareTreePathScanner.class,
                                  ErrorAwareTreePathScanner.class, Object.class, Boolean.TYPE, Function.class,
                                  SourcePathProvider.class }).
                        invoke(cp2, new Object[] { expression, url, line, canInterpret, interpreter,
                                                   context, staticContext, compiledClassHandler, sp });
                } catch (java.lang.reflect.InvocationTargetException itex) {
                    Throwable tex = itex.getTargetException();
                    if (tex instanceof RuntimeException) {
                        throw (RuntimeException) tex;
                    } else {
                        Exceptions.printStackTrace(tex);
                    }
                } catch (Exception ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
            return ret;
        }

        public int getLineNumber (Object annotation, Object timeStamp) {
            int ln = cp1.getLineNumber (annotation, timeStamp);
            if (ln >= 0) return ln;
            return cp2.getLineNumber (annotation, timeStamp);
        }

        public boolean showSource (String sourceName, int lineNumber, Object timeStamp) {
            return cp1.showSource (sourceName, lineNumber, timeStamp) |
                   cp2.showSource (sourceName, lineNumber, timeStamp);
        }

        public int getFieldLineNumber (
            String url,
            String className,
            String fieldName
        ) {
            int ln = cp1.getFieldLineNumber (url, className, fieldName);
            if (ln != -1) return ln;
            return cp2.getFieldLineNumber (url, className, fieldName);
        }

        public String getClassName (
            String url,
            int lineNumber
        ) {
            String className = cp1.getClassName (url, lineNumber);
            if (className != null && className.length() > 0) return className;
            return cp2.getClassName (url, lineNumber);
        }

        public String[] getImports (String url) {
            String[] r1 = cp1.getImports (url);
            String[] r2 = cp2.getImports (url);
            String[] r = new String [r1.length + r2.length];
            System.arraycopy (r1, 0, r, 0, r1.length);
            System.arraycopy (r2, 0, r, r1.length, r2.length);
            return r;
        }

        public void addPropertyChangeListener (PropertyChangeListener l) {
            cp1.addPropertyChangeListener (l);
            cp2.addPropertyChangeListener (l);
        }

        public void removePropertyChangeListener (PropertyChangeListener l) {
            cp1.removePropertyChangeListener (l);
            cp2.removePropertyChangeListener (l);
        }

        public void addPropertyChangeListener (
            String propertyName,
            PropertyChangeListener l
        ) {
            cp1.addPropertyChangeListener (propertyName, l);
            cp2.addPropertyChangeListener (propertyName, l);
        }

        public void removePropertyChangeListener (
            String propertyName,
            PropertyChangeListener l
        ) {
            cp1.removePropertyChangeListener (propertyName, l);
            cp2.removePropertyChangeListener (propertyName, l);
        }

        @Override
        public Operation[] getOperations(String url, int lineNumber, BytecodeProvider bytecodeProvider) {
            Operation[] operations = cp1.getOperations(url, lineNumber, bytecodeProvider);
            if (operations != null) {
                return operations;
            } else {
                return cp2.getOperations(url, lineNumber, bytecodeProvider);
            }
        }

        @Override
        public MethodArgument[] getArguments(String url, Operation operation) {
            MethodArgument[] args;
            try {
                args = cp1.getArguments(url, operation);
            } catch (UnsupportedOperationException uoex) {
                args = cp2.getArguments(url, operation);
            }
            return args;
        }

        @Override
        public MethodArgument[] getArguments(String url, int methodLineNumber) {
            MethodArgument[] args;
            try {
                args = cp1.getArguments(url, methodLineNumber);
            } catch (UnsupportedOperationException uoex) {
                args = cp2.getArguments(url, methodLineNumber);
            }
            return args;
        }



    }

    private static class CompoundAnnotation {
        public CompoundAnnotation() {}

        Object annotation1;
        Object annotation2;
    }
}

