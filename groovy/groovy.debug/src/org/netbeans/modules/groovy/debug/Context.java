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
package org.netbeans.modules.groovy.debug;

import java.beans.PropertyChangeListener;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.jpda.LineBreakpoint;
import org.netbeans.spi.debugger.jpda.EditorContext;
import org.netbeans.spi.debugger.ui.EditorContextDispatcher;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Martin Grebac
 * @author Martin Adamek
 */
public class Context {

    private static EditorContext editorContext;

    private static EditorContext getContext() {
        if (editorContext == null) {
            List<? extends EditorContext> l = DebuggerManager.getDebuggerManager().lookup(null, EditorContext.class);
            if (!l.isEmpty()) {
                editorContext = l.get(0);
            }
        }
        return editorContext;
    }

    // EditorContext methods .................................................
    /**
     * Shows source with given url on given line number.
     *
     * @param url a url of source to be shown
     * @param lineNumber a number of line to be shown
     */
    public static boolean showSource(String url, int lineNumber, Object timeStamp) {
        return getContext().showSource(url, lineNumber, timeStamp);
    }

    /**
     * Adds annotation to given url on given line.
     *
     * @param url a url of source annotation should be set into
     * @param lineNumber a number of line annotation should be set into
     * @param annotationType a type of annotation to be set
     *
     * @return annotation or <code>null</code>, when the annotation can not be
     * created at the given URL or line number.
     */
    public static Object annotate(String url, int lineNumber, String annotationType, Object timeStamp) {
        return getContext().annotate(url, lineNumber, annotationType, timeStamp);
    }

    /**
     * Removes given annotation.
     *
     * @return true if annotation has been successfully removed
     */
    public static void removeAnnotation(Object annotation) {
        getContext().removeAnnotation(annotation);
    }

    public static int getLineNumber(Object annotation, Object timeStamp) {
        return getContext().getLineNumber(annotation, timeStamp);
    }

    /**
     * Returns number of line currently selected in editor or
     * <code>null</code>.
     *
     * @return number of line currently selected in editor or <code>0</code>
     */
    public static int getCurrentLineNumber() {
        return getContext().getCurrentLineNumber();
    }

    /**
     * Returns URL of source currently selected in editor or
     * <code>null</code>.
     *
     * @return URL of source currently selected in editor or <code>null</code>
     */
    public static String getCurrentURL() {
        return getContext().getCurrentURL();
    }

    public static FileObject getCurrentFile() {
        return EditorContextDispatcher.getDefault().getCurrentFile();
    }

    public static void addPropertyChangeListener(PropertyChangeListener l) {
        //getContext ().addPropertyChangeListener (l);
        EditorContextDispatcher.getDefault().addPropertyChangeListener("text/x-groovy", l); // NOI18N
    }

    public static void removePropertyChangeListener(PropertyChangeListener l) {
        //getContext ().removePropertyChangeListener (l);
        EditorContextDispatcher.getDefault().removePropertyChangeListener(l);
    }

    /**
     * Creates a new time stamp.
     *
     * @param timeStamp a new time stamp
     */
    public static void createTimeStamp(Object timeStamp) {
        getContext().createTimeStamp(timeStamp);
    }

    /**
     * Disposes given time stamp.
     *
     * @param timeStamp a time stamp to be disposed
     */
    public static void disposeTimeStamp(Object timeStamp) {
        getContext().disposeTimeStamp(timeStamp);
    }

    // utility methods .........................................................
    public static String getFileName(LineBreakpoint b) {
        try {
            return new File(new URL(b.getURL()).getFile()).getName();
        } catch (MalformedURLException e) {
            return null;
        }
    }

    public static boolean showSource(LineBreakpoint b) {
        if (b.getLineNumber() < 1) {
            return Context.showSource(b.getURL(), 1, null);
        }
        return Context.showSource(b.getURL(), b.getLineNumber(), null);
    }

    /**
     * Adds annotation to url:line where the given breakpoint is set.
     *
     * @param b breakpoint to annotate
     *
     * @return annotation or <code>null</code>, when the annotation can not be
     * created at the url:line where the given breakpoint is set.
     */
    public static Object annotate(LineBreakpoint b) {
        String url = b.getURL();
        int lineNumber = b.getLineNumber();
        if (lineNumber < 1) {
            return null;
        }
        String condition = b.getCondition();
        boolean isConditional = (condition != null)
                && !condition.trim().equals(""); // NOI18N
        String annotationType = b.isEnabled()
                ? (isConditional ? EditorContext.CONDITIONAL_BREAKPOINT_ANNOTATION_TYPE
                : EditorContext.BREAKPOINT_ANNOTATION_TYPE)
                : (isConditional ? EditorContext.DISABLED_CONDITIONAL_BREAKPOINT_ANNOTATION_TYPE
                : EditorContext.DISABLED_BREAKPOINT_ANNOTATION_TYPE);

        return annotate(url, lineNumber, annotationType, b);
    }
}
