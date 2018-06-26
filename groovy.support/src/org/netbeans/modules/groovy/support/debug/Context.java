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
package org.netbeans.modules.groovy.support.debug;

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
            List l = DebuggerManager.getDebuggerManager().lookup(null, EditorContext.class);
            if (!l.isEmpty()) {
                editorContext = (EditorContext) l.get(0);
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
