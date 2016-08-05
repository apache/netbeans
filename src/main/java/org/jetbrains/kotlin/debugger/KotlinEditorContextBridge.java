/**
 * *****************************************************************************
 * Copyright 2000-2016 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************
 */
package org.jetbrains.kotlin.debugger;

import java.beans.PropertyChangeListener;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.netbeans.api.debugger.Breakpoint.VALIDITY;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.jpda.CallStackFrame;
import org.netbeans.api.debugger.jpda.FieldBreakpoint;
import org.netbeans.api.debugger.jpda.JPDABreakpoint;
import org.netbeans.api.debugger.jpda.JPDAThread;
import org.netbeans.api.debugger.jpda.LineBreakpoint;
import org.netbeans.api.debugger.jpda.MethodBreakpoint;
import org.netbeans.spi.debugger.jpda.EditorContext;
import org.netbeans.spi.debugger.jpda.SourcePathProvider;
import org.openide.ErrorManager;

/**
 *
 * @author Alexander.Baratynski
 */
public class KotlinEditorContextBridge {
    
    public static final String FIELD = "field";
    public static final String METHOD = "method";
    public static final String CLASS = "class";
    public static final String LINE = "line";
    
    private static EditorContext context;
    
    public static EditorContext getContext() {
        if (context == null) {
            List contexts = DebuggerManager.getDebuggerManager().
                    lookup(null, EditorContext.class);
            context = (EditorContext) contexts.get(0);
            for (Object compContext : contexts) {
                context = new CompoundContextProvider(
                        (EditorContext) compContext,
                        context);
            }
        }
        
        return context;
    }
    
    public static String getCurrentMethodSignature() {
        try {
            return (String) getContext().getClass().getMethod("getCurrentMethodSignature", new Class[] {}).
                    invoke(getContext(), new Object[] {});
        } catch (java.lang.reflect.InvocationTargetException itex) {
            Throwable tex = itex.getTargetException();
            if (tex instanceof RuntimeException) {
                throw (RuntimeException) tex;
            } else {
                ErrorManager.getDefault().notify(tex);
                return "";
            }
        } catch (Exception ex) {
            ErrorManager.getDefault().notify(ex);
            return "";
        }
    }
    
    public static String getFileName(LineBreakpoint breakpoint) {
        try {
            return new File(new URL(breakpoint.getURL()).getFile()).getName();
        } catch (MalformedURLException e) {
            return null;
        }
    }
    
    public static boolean showSource(LineBreakpoint breakpoint, Object timeStamp) {
        if (breakpoint.getLineNumber() < 1) {
            return KotlinEditorContextBridge.getContext().showSource(
                    breakpoint.getURL(), 
                    1, 
                    timeStamp);
        }
        
        return KotlinEditorContextBridge.getContext().showSource(
                breakpoint.getURL(), 
                breakpoint.getLineNumber(), 
                timeStamp);
    }
    
    public static String getDefaultType() {
        String id = getContext().getSelectedIdentifier();
        if (id != null) {
            if (id.equals(getContext().getCurrentMethodName())) {
                return METHOD;
            }
            String currentClassName = getContext().getCurrentClassName();
            int lastDotIndex = currentClassName.lastIndexOf('.');
            if (lastDotIndex >= 0) {
                currentClassName = currentClassName.substring(lastDotIndex + 1);
            }
            if (id.equals(currentClassName)) {
                return CLASS;
            }
            return FIELD;
        } else {
            String currentFieldName = getContext().getCurrentFieldName();
            if (currentFieldName != null && !currentFieldName.isEmpty()) {
                return FIELD;
            }
            
            String currentMethodName = getContext().getCurrentMethodName();
            if (currentMethodName != null ) {
                if (!currentMethodName.isEmpty()){
                    return METHOD;
                }
                String currentClassName = getContext().getCurrentClassName();
                if (!currentClassName.isEmpty()) {
                    return CLASS;
                }
            }
        }
        
        return null;
    }
    
    public static String convertClassNameToRelativePath (String className) {
        int i = className.indexOf ('$');
        if (i > 0) className = className.substring (0, i);
        String sourceName = className.replace
            ('.', '/') + ".kt";
        return sourceName;
    }
    
    public static Object annotate(LineBreakpoint lineBreakpoint) {
        String url = lineBreakpoint.getURL();
        int lineNumber = lineBreakpoint.getLineNumber();
        if (lineNumber < 1) {
            return null;
        }
        String condition = lineBreakpoint.getCondition();
        boolean isConditional = (condition != null) && !"".equals(condition.trim());
        boolean isInvalid = lineBreakpoint.getValidity() == VALIDITY.INVALID;
        String annotationType = lineBreakpoint.isEnabled() ?
                (isConditional ? EditorContext.CONDITIONAL_BREAKPOINT_ANNOTATION_TYPE :
                            EditorContext.BREAKPOINT_ANNOTATION_TYPE) :
                (isConditional ? EditorContext.DISABLED_CONDITIONAL_BREAKPOINT_ANNOTATION_TYPE :
                            EditorContext.DISABLED_BREAKPOINT_ANNOTATION_TYPE);
        
        if (isInvalid && lineBreakpoint.isEnabled()) {
            annotationType += "_broken";
        }
        
        return getContext().annotate(url, lineNumber, annotationType, null);
    }
    
    public static Object[] annotate (
        JPDABreakpoint b
    ) {
        String[] URLs;
        int[] lineNumbers;
        String condition;
        if (b instanceof LineBreakpoint) {
            URLs = new String[] { ((LineBreakpoint) b).getURL () };
            lineNumbers = new int[] { ((LineBreakpoint) b).getLineNumber() };
            condition = ((LineBreakpoint) b).getCondition();
        } else if (b instanceof FieldBreakpoint) {
            String className = ((FieldBreakpoint) b).getClassName();
            URLs = getClassURLs(convertClassNameToRelativePath(className));
            lineNumbers = new int[URLs.length];
            for (int i = 0; i < URLs.length; i++) {
                lineNumbers[i] = getContext().getFieldLineNumber(URLs[i], className, ((FieldBreakpoint) b).getFieldName());
            }
            condition = ((FieldBreakpoint) b).getCondition();
        } else if (b instanceof MethodBreakpoint) {
            String[] filters = ((MethodBreakpoint) b).getClassFilters();
            String[] urls = new String[] {};
            int[] lns = new int[] {};
            for (String filter : filters) {
                // TODO: annotate also other matched classes
                if (!filter.startsWith("*") && !filter.endsWith("*")) {
                    String[] newUrls = getClassURLs(convertClassNameToRelativePath(filter));
                    int[] newlns = new int[newUrls.length];
                    for (int j = 0; j < newUrls.length; j++) {
                        newlns[j] = getContext().getMethodLineNumber(newUrls[j], filter, ((MethodBreakpoint) b).getMethodName(), ((MethodBreakpoint) b).getMethodSignature());
                    }
                    if (urls.length == 0) {
                        urls = newUrls;
                        lns = newlns;
                    } else {
                        String[] l = new String[urls.length + newUrls.length];
                        System.arraycopy(urls, 0, l, 0, urls.length);
                        System.arraycopy(newUrls, 0, l, urls.length, newUrls.length);
                        urls = l;
                        
                        int[] ln = new int[urls.length + newUrls.length];
                        System.arraycopy(lns, 0, ln, 0, lns.length);
                        System.arraycopy(newlns, 0, ln, lns.length, newlns.length);
                        lns = ln;
                    }
                }
            }
            URLs = urls;
            lineNumbers = lns;
            condition = ((MethodBreakpoint) b).getCondition();
        } else {
            return null;
        }
        
        boolean isConditional = (condition != null) &&
            !"".equals (condition.trim ()); // NOI18N
        boolean isInvalid = b.getValidity() == VALIDITY.INVALID;
        String annotationType;
        if (b instanceof LineBreakpoint) {
            annotationType = b.isEnabled () ?
            (isConditional ? EditorContext.CONDITIONAL_BREAKPOINT_ANNOTATION_TYPE :
                             EditorContext.BREAKPOINT_ANNOTATION_TYPE) :
            (isConditional ? EditorContext.DISABLED_CONDITIONAL_BREAKPOINT_ANNOTATION_TYPE :
                             EditorContext.DISABLED_BREAKPOINT_ANNOTATION_TYPE);
        } else if (b instanceof FieldBreakpoint) {
            annotationType = b.isEnabled () ?
                EditorContext.FIELD_BREAKPOINT_ANNOTATION_TYPE :
                EditorContext.DISABLED_FIELD_BREAKPOINT_ANNOTATION_TYPE;
        } else if (b instanceof MethodBreakpoint) {
            annotationType = b.isEnabled () ?
                EditorContext.METHOD_BREAKPOINT_ANNOTATION_TYPE :
                EditorContext.DISABLED_METHOD_BREAKPOINT_ANNOTATION_TYPE;
        } else {
            return null;
        }
        if (isInvalid && b.isEnabled ()) annotationType += "_broken";

        List annotations = new ArrayList(URLs.length);
        for (int i = 0; i < URLs.length; i++) {
            if (lineNumbers[i] >= 1) {
                Object annotation = getContext().annotate (URLs[i], lineNumbers[i], annotationType, null);
                if (annotation != null) {
                    annotations.add(annotation);
                }
            }
        }
        if (annotations.isEmpty()) {
            return null;
        } else {
            return annotations.toArray();
        }
    }
    
    private static SourcePathProvider getDefaultContext() {
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
    
    private static String[] getClassURLs(String className) {
        SourcePathProvider spp = getDefaultContext();
        try {
        return (String[]) spp.getClass().getMethod("getAllURLs", new Class[] { String.class, Boolean.TYPE }).
                invoke(spp, new Object[] { className, Boolean.TRUE });
        } catch (Exception ex) {
            ErrorManager.getDefault().notify(ex);
            return new String[0];
        }
    }
    
//    public static String getRelativePath(JPDAThread thread, String stratumn) {
//        return convertSlash(thread.getSourcePath(stratumn));
//        
//    }
//
//    public static String getRelativePath(CallStackFrame csf, String stratumn) {
//        return convertSlash(csf.getSourcePath(stratumn)); 
//    }

    public static String getRelativePath(String className) {
        int i = className.indexOf ('$');
        if (i > 0) className = className.substring (0, i);
        String sourceName = className.replace 
            ('.', '/') + ".kt";
        return sourceName;
    }
    
    private static String convertSlash (String original) {
        return original.replace(File.separatorChar, '/');
}
    
    private static class CompoundContextProvider extends EditorContext {

        private final EditorContext cp1, cp2;
        
        CompoundContextProvider (EditorContext cp1, EditorContext cp2) {
            this.cp1 = cp1;
            this.cp2 = cp2;
        }

        @Override
        public void createTimeStamp (Object timeStamp) {
            cp1.createTimeStamp (timeStamp);
            cp2.createTimeStamp (timeStamp);
        }

        @Override
        public void disposeTimeStamp (Object timeStamp) {
            cp1.disposeTimeStamp (timeStamp);
            cp2.disposeTimeStamp (timeStamp);
        }
        
        @Override
        public void updateTimeStamp (Object timeStamp, String url) {
            cp1.updateTimeStamp (timeStamp, url);
            cp2.updateTimeStamp (timeStamp, url);
        }

        @Override
        public String getCurrentClassName () {
            String s = cp1.getCurrentClassName ();
            if (s.trim ().length () < 1)
                return cp2.getCurrentClassName ();
            return s;
        }

        @Override
        public String getCurrentURL () {
            String s = cp1.getCurrentURL ();
            if (s.trim ().length () < 1)
                return cp2.getCurrentURL ();
            return s;
        }
        
        @Override
        public String getCurrentFieldName () {
            String s = cp1.getCurrentFieldName ();
            if ( (s == null) || (s.trim ().length () < 1))
                return cp2.getCurrentFieldName ();
            return s;
        }
        
        @Override
        public int getCurrentLineNumber () {
            int i = cp1.getCurrentLineNumber ();
            if (i < 1)
                return cp2.getCurrentLineNumber ();
            return i;
        }
        
        @Override
        public String getCurrentMethodName () {
            String s = cp1.getCurrentMethodName ();
            if ( (s == null) || (s.trim ().length () < 1))
                return cp2.getCurrentMethodName ();
            return s;
        }
        
        public String getCurrentMethodSignature() {
            String s = null;
            try {
                s = (String) cp1.getClass().getMethod("getCurrentMethodSignature", new Class[] {}). // NOI18N
                        invoke(getContext(), new Object[] {});
            } catch (java.lang.reflect.InvocationTargetException itex) {
                Throwable tex = itex.getTargetException();
                if (tex instanceof RuntimeException) {
                    throw (RuntimeException) tex;
                } else {
                    ErrorManager.getDefault().notify(tex);
                    return null;
                }
            } catch (Exception ex) {
                // Ignore, we have another attempt with cp2
                //ErrorManager.getDefault().notify(ex);
            }
            if ( (s == null) || (s.trim ().length () < 1)) {
                try {
                    s = (String) cp2.getClass().getMethod("getCurrentMethodSignature", new Class[] {}). // NOI18N
                            invoke(getContext(), new Object[] {});
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
            return s;
        }
        
        @Override
        public String getSelectedIdentifier () {
            String s = cp1.getSelectedIdentifier ();
            if ( (s == null) || (s.trim ().length () < 1))
                return cp2.getSelectedIdentifier ();
            return s;
        }
        
        @Override
        public String getSelectedMethodName () {
            String s = cp1.getSelectedMethodName ();
            if ( (s == null) || (s.trim ().length () < 1))
                return cp2.getSelectedMethodName ();
            return s;
        }
        
        @Override
        public void removeAnnotation (Object value) {
            if (value instanceof List) {
                for (Iterator iter = ((List) value).iterator(); iter.hasNext();) {
                    CompoundAnnotation ca = (CompoundAnnotation) iter.next();
                    if (ca.annotation1 != null) {
                        cp1.removeAnnotation (ca.annotation1);
                    }
                    if (ca.annotation2 != null) {
                        cp2.removeAnnotation (ca.annotation2);
                    }
                }
                return;
            }
            CompoundAnnotation ca = (CompoundAnnotation) value;
            if (ca.annotation1 != null) {
                cp1.removeAnnotation (ca.annotation1);
            }
            if (ca.annotation2 != null) {
                cp2.removeAnnotation (ca.annotation2);
            }
        }

        @Override
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
            if (ca.annotation1 != null || ca.annotation2 != null) {
                return ca;
            } else {
                return null;
            }
        }

        @Override
        public Object annotate(String url, int lineNumber, String annotationType, Object timeStamp, JPDAThread thread) {
            CompoundAnnotation ca = new CompoundAnnotation ();
            ca.annotation1 = cp1.annotate
                (url, lineNumber, annotationType, timeStamp, thread);
            ca.annotation2 = cp2.annotate
                (url, lineNumber, annotationType, timeStamp, thread);
            if (ca.annotation1 != null || ca.annotation2 != null) {
                return ca;
            } else {
                return null;
            }
        }

        @Override
        public Object annotate(String url, int startPosition, int endPosition, String annotationType, Object timeStamp) {
            CompoundAnnotation ca = new CompoundAnnotation ();
            ca.annotation1 = cp1.annotate
                (url, startPosition, endPosition, annotationType, timeStamp);
            ca.annotation2 = cp2.annotate
                (url, startPosition, endPosition, annotationType, timeStamp);
            if (ca.annotation1 != null || ca.annotation2 != null) {
                return ca;
            } else {
                return null;
            }
        }                

        @Override
        public int getLineNumber (Object annotation, Object timeStamp) {
            CompoundAnnotation ca = new CompoundAnnotation ();
            int ln;
            if (ca.annotation1 != null) {
                ln = cp1.getLineNumber (ca.annotation1, timeStamp);
            } else {
                ln = -1;
            }
            if (ln >= 0) return ln;
            if (ca.annotation2 != null) {
                return cp2.getLineNumber (ca.annotation2, timeStamp);
            } else {
                return -1;
            }
        }

        @Override
        public boolean showSource (String sourceName, int lineNumber, Object timeStamp) {
            return cp1.showSource (sourceName, lineNumber, timeStamp) |
                   cp2.showSource (sourceName, lineNumber, timeStamp);
        }
    
        @Override
        public int getFieldLineNumber (
            String url, 
            String className, 
            String fieldName
        ) {
            int ln = cp1.getFieldLineNumber (url, className, fieldName);
            if (ln != -1) return ln;
            return cp2.getFieldLineNumber (url, className, fieldName);
        }
    
        @Override
        public String getClassName (
            String url, 
            int lineNumber
        ) {
            String className = cp1.getClassName (url, lineNumber);
            if (className != null) return className;
            return cp2.getClassName (url, lineNumber);
        }
    
        @Override
        public String[] getImports (String url) {
            String[] r1 = cp1.getImports (url);
            String[] r2 = cp2.getImports (url);
            String[] r = new String [r1.length + r2.length];
            System.arraycopy (r1, 0, r, 0, r1.length);
            System.arraycopy (r2, 0, r, r1.length, r2.length);
            return r;
        }
        
        @Override
        public void addPropertyChangeListener (PropertyChangeListener l) {
            cp1.addPropertyChangeListener (l);
            cp2.addPropertyChangeListener (l);
        }
        
        @Override
        public void removePropertyChangeListener (PropertyChangeListener l) {
            cp1.removePropertyChangeListener (l);
            cp2.removePropertyChangeListener (l);
        }
        
        @Override
        public void addPropertyChangeListener (
            String propertyName, 
            PropertyChangeListener l
        ) {
            cp1.addPropertyChangeListener (propertyName, l);
            cp2.addPropertyChangeListener (propertyName, l);
        }
        
        @Override
        public void removePropertyChangeListener (
            String propertyName, 
            PropertyChangeListener l
        ) {
            cp1.removePropertyChangeListener (propertyName, l);
            cp2.removePropertyChangeListener (propertyName, l);
        }
        
    }
    
    private static class CompoundAnnotation {
        Object annotation1;
        Object annotation2;
}
    
}
