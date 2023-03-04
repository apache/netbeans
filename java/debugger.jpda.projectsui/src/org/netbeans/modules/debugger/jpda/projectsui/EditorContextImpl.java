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

package org.netbeans.modules.debugger.jpda.projectsui;

import org.netbeans.api.java.source.support.ErrorAwareTreePathScanner;
import java.awt.Color;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.function.Function;
import java.util.logging.Logger;
import javax.lang.model.element.ElementKind;
import javax.swing.JEditorPane;
import javax.swing.SwingUtilities;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Caret;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import org.netbeans.api.debugger.Breakpoint;
import org.netbeans.api.debugger.Properties;
import org.netbeans.api.debugger.jpda.InvalidExpressionException;
import org.netbeans.api.debugger.jpda.JPDAThread;
import org.netbeans.api.debugger.jpda.LineBreakpoint;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.editor.settings.AttributesUtilities;
import org.netbeans.api.editor.settings.EditorStyleConstants;
import org.netbeans.api.editor.settings.FontColorSettings;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.editor.JumpList;
import org.netbeans.modules.debugger.jpda.projects.ASTOperationCreationDelegate;
import org.netbeans.modules.debugger.jpda.projects.EditorContextSupport;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.modules.parsing.spi.Parser.Result;
import org.netbeans.spi.debugger.jpda.EditorContext;
import org.netbeans.spi.debugger.jpda.Evaluator.Expression;
import org.netbeans.spi.debugger.jpda.SourcePathProvider;
import org.netbeans.spi.debugger.ui.EditorContextDispatcher;
import org.openide.ErrorManager;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.URLMapper;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.text.Annotation;
import org.openide.text.Line;
import org.openide.text.Line.ShowOpenType;
import org.openide.text.Line.ShowVisibilityType;
import org.openide.text.NbDocument;
import org.openide.util.Pair;
import org.openide.util.Utilities;
import org.openide.util.WeakListeners;

/**
 *
 * @author Jan Jancura
 */
@EditorContext.Registration()
public class EditorContextImpl extends EditorContext {

    private static String fronting =
        System.getProperty ("netbeans.debugger.fronting", "true");
    
    private static final Logger logger = Logger.getLogger(EditorContextImpl.class.getName());

    private PropertyChangeSupport   pcs;
    private Map<Annotation, String> annotationToURL = new HashMap<Annotation, String>();
    private final Object annotationToURLLock = new Object();
    private PropertyChangeListener  dispatchListener;
    private EditorContextDispatcher contextDispatcher;

    {
        pcs = new PropertyChangeSupport (this);
        dispatchListener = new EditorContextDispatchListener();
        contextDispatcher = EditorContextDispatcher.getDefault();
        contextDispatcher.addPropertyChangeListener("text/x-java",
                WeakListeners.propertyChange(dispatchListener, contextDispatcher));
        
    }


    /**
     * Shows source with given url on given line number.
     *
     * @param url a url of source to be shown
     * @param lineNumber a number of line to be shown
     * @param timeStamp a time stamp to be used
     */
    @Override
    public boolean showSource (String url, int lineNumber, Object timeStamp) {
        Line l = showSourceLine(url, lineNumber, timeStamp);
        if (l != null) {
            addPositionToJumpList(url, l, 0);
        }
        return l != null;
    }

    static Line showSourceLine (String url, int lineNumber, Object timeStamp) {
        Line l = LineTranslations.getTranslations().getLine (url, lineNumber, timeStamp); // false = use original ln
        if (l == null) {
            ErrorManager.getDefault().log(ErrorManager.WARNING,
                    "Show Source: Have no line for URL = "+url+", line number = "+lineNumber);
            return null;
        }
        Properties p = Properties.getDefault().getProperties("debugger.options.JPDA");
        boolean reuseEditorTabs = p.getBoolean("ReuseEditorTabs", true);
        if ("true".equalsIgnoreCase(fronting)) {
            if (reuseEditorTabs) {
                l.show (ShowOpenType.REUSE, ShowVisibilityType.FOCUS);
            } else {
                l.show (ShowOpenType.OPEN, ShowVisibilityType.FOCUS);
            }
            l.show (ShowOpenType.OPEN, ShowVisibilityType.FRONT); //FIX 47825
        } else {
            if (reuseEditorTabs) {
                l.show (ShowOpenType.REUSE, ShowVisibilityType.FOCUS);
            } else {
                l.show (ShowOpenType.OPEN, ShowVisibilityType.FOCUS);
            }
        }
        return l;
    }

    /**
     * Shows source with given url on given line number.
     *
     * @param url a url of source to be shown
     * @param lineNumber a number of line to be shown
     * @param timeStamp a time stamp to be used
     */
    public boolean showSource (String url, int lineNumber, int column, int length, Object timeStamp) {
        Line l = LineTranslations.getTranslations().getLine (url, lineNumber, timeStamp); // false = use original ln
        if (l == null) {
            ErrorManager.getDefault().log(ErrorManager.WARNING,
                    "Show Source: Have no line for URL = "+url+", line number = "+lineNumber);
            return false;
        }
        if ("true".equalsIgnoreCase(fronting)) {
            l.show (ShowOpenType.OPEN, ShowVisibilityType.FRONT, column); //FIX 47825
        } else {
            l.show (ShowOpenType.OPEN, ShowVisibilityType.FOCUS, column);
        }
        addPositionToJumpList(url, l, column);
        return true;
    }

    /** Add the line offset into the jump history */
    private void addPositionToJumpList(String url, Line l, int column) {
        DataObject dataObject = getDataObject (url);
        if (dataObject != null) {
            EditorCookie ec = dataObject.getLookup().lookup(EditorCookie.class);
            if (ec != null) {
                try {
                    StyledDocument doc = ec.openDocument();
                    JEditorPane[] eps = ec.getOpenedPanes();
                    if (eps != null && eps.length > 0) {
                        JumpList.addEntry(eps[0], NbDocument.findLineOffset(doc, l.getLineNumber()) + column);
                    }
                } catch (java.io.IOException ioex) {
                    ErrorManager.getDefault().notify(ioex);
                }
            }
        }
    }


    /**
     * Creates a new time stamp.
     *
     * @param timeStamp a new time stamp
     */
    @Override
    public void createTimeStamp (Object timeStamp) {
        LineTranslations.getTranslations().createTimeStamp(timeStamp);
    }

    /**
     * Disposes given time stamp.
     *
     * @param timeStamp a time stamp to be disposed
     */
    @Override
    public void disposeTimeStamp (Object timeStamp) {
        LineTranslations.getTranslations().disposeTimeStamp(timeStamp);
    }

    @Override
    public Object annotate (
        String url,
        int lineNumber,
        String annotationType,
        Object timeStamp
    ) {
        return annotate(url, lineNumber, annotationType, timeStamp, null);
    }
    @Override
    public Object annotate (
        String url,
        int lineNumber,
        String annotationType,
        Object timeStamp,
        JPDAThread thread
    ) {
        Line l =  LineTranslations.getTranslations().getLine (
            url,
            lineNumber,
            (timeStamp instanceof Breakpoint) ? null : timeStamp
        );
        if (l == null) {
            return null;
        }
        Annotation annotation;
        if (timeStamp instanceof Breakpoint) {
            annotation = new DebuggerBreakpointAnnotation(annotationType, l, (Breakpoint) timeStamp);
        } else {
            annotation = new DebuggerAnnotation (annotationType, l, thread);
        }
        synchronized (annotationToURLLock) {
            assert url != null;
            annotationToURL.put (annotation, url);
        }
        return annotation;
    }

    @Override
    public Object annotate (
        String url,
        int startPosition,
        int endPosition,
        String annotationType,
        Object timeStamp
    ) {
        AttributeSet attrs;
        if (EditorContext.CURRENT_LAST_OPERATION_ANNOTATION_TYPE.equals(annotationType)) {
            attrs = AttributesUtilities.createImmutable(EditorStyleConstants.WaveUnderlineColor, getColor(annotationType));
        } else {
            attrs = AttributesUtilities.createImmutable(StyleConstants.Background, getColor(annotationType));
        }
        DebuggerAnnotation annotation;
        try {
            annotation = new DebuggerAnnotation(annotationType, attrs, startPosition, endPosition,
                    URLMapper.findFileObject(new URL(url)));
        } catch (MalformedURLException ex) {
            throw new RuntimeException("Bad URL: "+url, ex);
        }
        synchronized (annotationToURLLock) {
            assert url != null;
            annotationToURL.put (annotation, url);
        }
        return annotation;
    }

    private static Color getColor(String annotationType) {
        if (annotationType.endsWith("_broken")) {
            annotationType = annotationType.substring(0, annotationType.length() - "_broken".length());
        }
        if (EditorContext.BREAKPOINT_ANNOTATION_TYPE.equals(annotationType)) {
            return getHighlight(annotationType, 0xFC9D9F);
        } else if (EditorContext.CURRENT_LINE_ANNOTATION_TYPE.equals(annotationType) ||
                   EditorContext.CURRENT_OUT_OPERATION_ANNOTATION_TYPE.equals(annotationType)) {
            return getHighlight(annotationType, 0xBDE6AA);
        //} else if (EditorContext.CURRENT_EXPRESSION_CURRENT_LINE_ANNOTATION_TYPE.equals(annotationType)) {
        //    return getHighlight(annotationType, 0xE9FFE6); // 0xE3FFD2// 0xD1FFBC
        } else if (EditorContext.CURRENT_LAST_OPERATION_ANNOTATION_TYPE.equals(annotationType)) {
            return getHighlight(annotationType, 0x99BB8A);
        } else {
            return new Color(0x0000FF);
        }
    }

    private static Color getHighlight(String name, int defaultRGB) {
        FontColorSettings fcs = MimeLookup.getLookup(MimePath.EMPTY).lookup(FontColorSettings.class);
        AttributeSet as = (fcs != null) ? fcs.getFontColors(name) : null;
        if (as != null) {
            return (Color) as.getAttribute(StyleConstants.Background);
        } else {
            return new Color(defaultRGB);
        }
    }

    /**
     * Removes given annotation.
     *
     * @return true if annotation has been successfully removed
     */
    @Override
    public void removeAnnotation (
        Object a
    ) {
        if (a instanceof Collection) {
            Collection annotations = ((Collection) a);
            for (Iterator it = annotations.iterator(); it.hasNext(); ) {
                removeAnnotation((Annotation) it.next());
            }
        } else {
            removeAnnotation((Annotation) a);
        }
    }

    private void removeAnnotation(Annotation annotation) {
        synchronized (annotationToURLLock) {
            String url = annotationToURL.remove (annotation);
            //logger.severe("Removing "+annotation+", URL = "+url+", thread = "+Thread.currentThread().getId());
            //Thread.dumpStack();
            assert url != null;
        }
        annotation.detach ();
        
    }

    /**
     * Returns line number given annotation is associated with.
     *
     * @param annotation an annotation, or an array of "url" and new Integer(line number)
     * @param timeStamp a time stamp to be used
     *
     * @return line number given annotation is associated with
     */
    @Override
    public int getLineNumber (
        Object annotation,
        Object timeStamp
    ) {
        if (annotation instanceof LineBreakpoint) {
            // A sort of hack to be able to retrieve the original line.
            LineBreakpoint lb = (LineBreakpoint) annotation;
            return LineTranslations.getTranslations().getOriginalLineNumber(lb, timeStamp);
        }
        /*if (annotation instanceof Object[]) {
            // A sort of hack to be able to retrieve the original line.
            Object[] urlLine = (Object[]) annotation;
            String url = (String) urlLine[0];
            int line = ((Integer) urlLine[1]).intValue();
            return LineTranslations.getTranslations().getOriginalLineNumber(url, line, timeStamp);
        }*/
        Line line;
        if (annotation instanceof DebuggerBreakpointAnnotation) {
            line = ((DebuggerBreakpointAnnotation) annotation).getLine();
        } else {
            line = ((DebuggerAnnotation) annotation).getLine();
        }
        if (timeStamp == null) {
            return line.getLineNumber () + 1;
        }
        String url;
        synchronized (annotationToURLLock) {
            url = annotationToURL.get ((Annotation) annotation);
            assert url != null;
        }
        Line.Set lineSet = LineTranslations.getTranslations().getLineSet (url, timeStamp);
        return lineSet.getOriginalLineNumber (line) + 1;
    }

    /**
     * Updates timeStamp for gived url.
     *
     * @param timeStamp time stamp to be updated
     * @param url an url
     */
    @Override
    public void updateTimeStamp (Object timeStamp, String url) {
        LineTranslations.getTranslations().updateTimeStamp(timeStamp, url);
    }

    /**
     * Returns number of line currently selected in editor or <code>-1</code>.
     *
     * @return number of line currently selected in editor or <code>-1</code>
     */
    @Override
    public int getCurrentLineNumber () {
        return contextDispatcher.getCurrentLineNumber();
    }

    /**
     * Returns number of line currently selected in editor or <code>-1</code>.
     *
     * @return number of line currently selected in editor or <code>-1</code>
     */
    public int getCurrentOffset () {
        JEditorPane ep = contextDispatcher.getCurrentEditor();
        if (ep == null) {
            return -1;
        }
        Caret caret = ep.getCaret ();
        if (caret == null) {
            return -1;
        }
        return caret.getDot();
    }

    /**
     * Returns name of class currently selected in editor or empty string.
     *
     * @return name of class currently selected in editor or empty string
     */
    @Override
    public String getCurrentClassName () {
        String currentClass = getCurrentElement(ElementKind.CLASS);
        if (currentClass == null) {
            return "";
        } else {
            return currentClass;
        }
    }

    /**
     * Returns name of class recently selected in editor or empty string.
     *
     * @return name of class recently selected in editor or empty string
     */
    public String getMostRecentClassName () {
        String clazz = getMostRecentElement(ElementKind.CLASS);
        if (clazz == null) {
            return "";
        } else {
            return clazz;
        }
    }

    /**
     * Returns URL of source currently selected in editor or empty string.
     *
     * @return URL of source currently selected in editor or empty string
     */
    @Override
    public String getCurrentURL () {
        return contextDispatcher.getCurrentURLAsString();
    }

    /**
     * Returns name of method currently selected in editor or empty string.
     *
     * @return name of method currently selected in editor or empty string
     */
    @Override
    public String getCurrentMethodName () {
        String currentMethod = getCurrentElement(ElementKind.METHOD);
        if (currentMethod == null) {
            return "";
        } else {
            return currentMethod;
        }
    }

    /**
     * Returns name of method recently selected in editor or empty string.
     *
     * @return name of method recently selected in editor or empty string
     */
    public String getMostRecentMethodName () {
        String method = getMostRecentElement(ElementKind.METHOD);
        if (method == null) {
            return "";
        } else {
            return method;
        }
    }

    /**
     * Returns signature of method currently selected in editor or null.
     *
     * @return signature of method currently selected in editor or null
     */
    public String getCurrentMethodSignature () {
        final String[] elementSignaturePtr = new String[] { null };
        try {
            getCurrentElement(ElementKind.METHOD, elementSignaturePtr);
        } catch (final java.awt.IllegalComponentStateException icse) {
            throw new java.awt.IllegalComponentStateException() {
                @Override
                public String getMessage() {
                    icse.getMessage();
                    return elementSignaturePtr[0];
                }
            };
        }
        return elementSignaturePtr[0];
    }

    public String getMostRecentMethodSignature () {
        final String[] elementSignaturePtr = new String[] { null };
        try {
            getMostRecentElement(ElementKind.METHOD, elementSignaturePtr);
        } catch (final java.awt.IllegalComponentStateException icse) {
            throw new java.awt.IllegalComponentStateException() {
                @Override
                public String getMessage() {
                    icse.getMessage();
                    return elementSignaturePtr[0];
                }
            };
        }
        return elementSignaturePtr[0];
    }

    /**
     * Returns name of field currently selected in editor or <code>null</code>.
     *
     * @return name of field currently selected in editor or <code>null</code>
     */
    @Override
    public String getCurrentFieldName () {
        String currentField = getCurrentElement(ElementKind.FIELD);
        if (currentField == null) {
            return "";
        } else {
            return currentField;
        }
        //return getSelectedIdentifier ();
    }

    /**
     * Returns name of field recently selected in editor or <code>null</code>.
     *
     * @return name of field recently selected in editor or <code>null</code>
     */
    public String getMostRecentFieldName () {
        String field = getMostRecentElement(ElementKind.FIELD);
        if (field == null) {
            return "";
        } else {
            return field;
        }
    }


    /**
     * Returns identifier currently selected in editor or <code>null</code>.
     *
     * @return identifier currently selected in editor or <code>null</code>
     */
    @Override
    public String getSelectedIdentifier () {
        JEditorPane ep = contextDispatcher.getCurrentEditor ();
        if (ep == null) {
            return null;
        }
        Caret caret = ep.getCaret();
        if (caret == null) {
            // No caret => no selected text
            return null;
        }
        String s = ep.getSelectedText ();
        if (s == null) {
            return null;
        }
        if (Utilities.isJavaIdentifier (s)) {
            return s;
        }
        return null;
    }

    /**
     * Returns method name currently selected in editor or empty string.
     *
     * @return method name currently selected in editor or empty string
     */
    @Override
    public String getSelectedMethodName () {
        if (SwingUtilities.isEventDispatchThread()) {
            return getSelectedMethodName_();
        } else {
            final String[] mn = new String[1];
            try {
                SwingUtilities.invokeAndWait(new Runnable() {
                    @Override
                    public void run() {
                        mn[0] = getSelectedMethodName_();
                    }
                });
            } catch (InvocationTargetException ex) {
                ErrorManager.getDefault().notify(ex.getTargetException());
            } catch (InterruptedException ex) {
                // interrupted, ignored.
            }
            return mn[0];
        }
    }

    private String getSelectedMethodName_() {
        JEditorPane ep = contextDispatcher.getCurrentEditor ();
        if (ep == null) {
            return "";
        }
        StyledDocument doc = (StyledDocument) ep.getDocument ();
        if (doc == null) {
            return "";
        }
        int offset = ep.getCaret ().getDot ();
        String t;
//        if ( (ep.getSelectionStart () <= offset) &&
//             (offset <= ep.getSelectionEnd ())
//        )   t = ep.getSelectedText ();
//        if (t != null) return t;

        int line = NbDocument.findLineNumber (
            doc,
            offset
        );
        int col = NbDocument.findLineColumn (
            doc,
            offset
        );
        try {
            javax.swing.text.Element lineElem =
                org.openide.text.NbDocument.findLineRootElement (doc).
                getElement (line);

            if (lineElem == null) {
                return "";
            }
            int lineStartOffset = lineElem.getStartOffset ();
            int lineLen = lineElem.getEndOffset () - lineStartOffset;
            // t contains current line in editor
            t = doc.getText (lineStartOffset, lineLen);

            int identStart = col;
            while ( identStart > 0 &&
                    Character.isJavaIdentifierPart (
                        t.charAt (identStart - 1)
                    )
            ) {
                identStart--;
            }

            int identEnd = col;
            while (identEnd < lineLen &&
                   Character.isJavaIdentifierPart (t.charAt (identEnd))
            ) {
                identEnd++;
            }
            int i = t.indexOf ('(', identEnd);
            if (i < 0) {
                return "";
            }
            if (t.substring (identEnd, i).trim ().length () > 0) {
                return "";
            }

            if (identStart == identEnd) {
                return "";
            }
            return t.substring (identStart, identEnd);
        } catch (javax.swing.text.BadLocationException ex) {
            return "";
        }
    }

    /**
     * Returns line number of given field in given class.
     *
     * @param url the url of file the class is deined in
     * @param className the name of class (or innerclass) the field is
     *                  defined in
     * @param fieldName the name of field
     *
     * @return line number or -1
     */
    @Override
    public int getFieldLineNumber (
        String url,
        final String className,
        final String fieldName
    ) {
        return EditorContextSupport.getFieldLineNumber(url, className, fieldName);
    }

    /**
     * Returns line number of given method in given class.
     *
     * @param url the url of file the class is deined in
     * @param className the name of class (or innerclass) the method is
     *                  defined in
     * @param methodName the name of method
     * @param methodSignature the JNI-style signature of the method.
     *        If <code>null</code>, then the first method found is returned.
     *
     * @return line number or -1
     */
    @Override
    public int getMethodLineNumber (
        String url,
        final String className,
        final String methodName,
        final String methodSignature
    ) {
        return EditorContextSupport.getMethodLineNumber(url, className, methodName, methodSignature);
    }

    /** @return declared class name
     */
    public String getCurrentClassDeclaration() {
        FileObject fo = contextDispatcher.getCurrentFile();
        if (fo == null) {
            return null;
        }
        JEditorPane ep = contextDispatcher.getCurrentEditor();
        final int currentOffset = (ep == null) ? 0 : ep.getCaretPosition();
        //final int currentOffset = org.netbeans.editor.Registry.getMostActiveComponent().getCaretPosition();
        
        return EditorContextSupport.getClassDeclaredAt(fo, currentOffset);
    }

    /** @return { "method name", "method signature", "enclosing class name" }
     */
    @Override
    public String[] getCurrentMethodDeclaration() {
        FileObject fo = contextDispatcher.getCurrentFile();
        if (fo == null) {
            return null;
        }
        JEditorPane ep = contextDispatcher.getCurrentEditor();
        final int currentOffset = (ep == null) ? 0 : ep.getCaretPosition();
        return EditorContextSupport.getMethodDeclaredAt(fo, currentOffset);
    }


    /**
     * Returns binary class name for given url and line number or null.
     *
     * @param url a url
     * @param lineNumber a line number
     *
     * @return binary class name for given url and line number or null
     */
    @Override
    public String getClassName (
        String url,
        int lineNumber
    ) {
        return EditorContextSupport.getClassName(url, lineNumber);
    }

    @Override
    public Operation[] getOperations(String url, final int lineNumber,
                                     final BytecodeProvider bytecodeProvider) {
        return EditorContextSupport.getOperations(url, lineNumber,
                                                  bytecodeProvider,
                                                  new OperationCreationDelegateImpl());
    }

    /** return the offset of the first non-whitespace character on the line,
               or -1 when the line does not exist
     */
    private static int findLineOffset(StyledDocument doc, int lineNumber) {
        int offset;
        try {
            offset = NbDocument.findLineOffset (doc, lineNumber - 1);
            int offset2 = NbDocument.findLineOffset (doc, lineNumber);
            try {
                String lineStr = doc.getText(offset, offset2 - offset);
                for (int i = 0; i < lineStr.length(); i++) {
                    if (!Character.isWhitespace(lineStr.charAt(i))) {
                        offset += i;
                        break;
                    }
                }
            } catch (BadLocationException ex) {
                // ignore
            }
        } catch (IndexOutOfBoundsException ioobex) {
            return -1;
        }
        return offset;
    }

    @Override
    public MethodArgument[] getArguments(String url, final Operation operation) {
        return EditorContextSupport.getArguments(url, operation, new OperationCreationDelegateImpl());
    }
    
    @Override
    public MethodArgument[] getArguments(String url, final int methodLineNumber) {
        return EditorContextSupport.getArguments(url, methodLineNumber, new OperationCreationDelegateImpl());
    }
    
    /**
     * Returns list of imports for given source url.
     *
     * @param url the url of source file
     *
     * @return list of imports for given source url
     */
    @Override
    public String[] getImports (
        String url
    ) {
        return EditorContextSupport.getImports(url);
    }
    
    public <R,D> R interpretOrCompileCode(final Expression<Object> expression, String url, final int line,
                                          final ErrorAwareTreePathScanner<Boolean,D> canInterpret,
                                          final ErrorAwareTreePathScanner<R,D> interpreter,
                                          final D context, final boolean staticContext,
                                          final Function<Pair<String, byte[]>, Boolean> compiledClassHandler,
                                          final SourcePathProvider sp) throws InvalidExpressionException {
        return EditorContextSupport.interpretOrCompileCode(expression, url, line,
                                                           canInterpret,
                                                           interpreter,
                                                           context, staticContext,
                                                           compiledClassHandler,
                                                           sp);
    }

    /**
     * Adds a property change listener.
     *
     * @param l the listener to add
     */
    @Override
    public void addPropertyChangeListener (PropertyChangeListener l) {
        pcs.addPropertyChangeListener (l);
    }

    /**
     * Removes a property change listener.
     *
     * @param l the listener to remove
     */
    @Override
    public void removePropertyChangeListener (PropertyChangeListener l) {
        pcs.removePropertyChangeListener (l);
    }

    /**
     * Adds a property change listener.
     *
     * @param propertyName the name of property
     * @param l the listener to add
     */
    @Override
    public void addPropertyChangeListener (
        String propertyName,
        PropertyChangeListener l
    ) {
        pcs.addPropertyChangeListener (propertyName, l);
    }

    /**
     * Removes a property change listener.
     *
     * @param propertyName the name of property
     * @param l the listener to remove
     */
    @Override
    public void removePropertyChangeListener (
        String propertyName,
        PropertyChangeListener l
    ) {
        pcs.removePropertyChangeListener (propertyName, l);
    }


    // private helper methods ..................................................

//    public void fileChanged (FileEvent fe) {
//	pcs.firePropertyChange (PROP_LINE_NUMBER, null, null);
//    }
//
//    public void fileDeleted (FileEvent fe) {}
//    public void fileAttributeChanged (org.openide.filesystems.FileAttributeEvent fe) {}
//    public void fileDataCreated (FileEvent fe) {}
//    public void fileFolderCreated (FileEvent fe) {}
//    public void fileRenamed (org.openide.filesystems.FileRenameEvent fe) {}


    private String getCurrentElement(ElementKind kind) {
        return getCurrentElement(kind, null);
    }

    private String getMostRecentElement(ElementKind kind) {
        return getMostRecentElement(kind, null);
    }

    /** throws IllegalComponentStateException when can not return the data in AWT. */
    private String getCurrentElement(final ElementKind kind, final String[] elementSignaturePtr)
            throws java.awt.IllegalComponentStateException {
        return getCurrentElement(contextDispatcher.getCurrentFile(),
                                 contextDispatcher.getCurrentEditor(),
                                 kind, elementSignaturePtr);
    }

    /** throws IllegalComponentStateException when can not return the data in AWT. */
    private String getMostRecentElement(final ElementKind kind, final String[] elementSignaturePtr)
            throws java.awt.IllegalComponentStateException {
        return getCurrentElement(contextDispatcher.getMostRecentFile(),
                                 contextDispatcher.getMostRecentEditor(),
                                 kind, elementSignaturePtr);
    }

    /** throws IllegalComponentStateException when can not return the data in AWT. */
    private String getCurrentElement(FileObject fo, JEditorPane ep,
                                     final ElementKind kind, final String[] elementSignaturePtr)
            throws java.awt.IllegalComponentStateException {

        if (fo == null) {
            return null;
        }
        final int currentOffset;
        final String selectedIdentifier;
        if (ep != null) {
            String s;
            Caret caret = ep.getCaret();
            if (caret == null) {
                s = null;
                currentOffset = 0;
            } else {
                s = ep.getSelectedText ();
                currentOffset = ep.getCaretPosition();
                if (ep.getSelectionStart() > currentOffset || ep.getSelectionEnd() < currentOffset) {
                    s = null; // caret outside of the selection
                }
            }
            if (s != null && Utilities.isJavaIdentifier (s)) {
                selectedIdentifier = s;
            } else {
                selectedIdentifier = null;
            }
        } else {
            selectedIdentifier = null;
            currentOffset = 0;
        }
        return EditorContextSupport.getCurrentElement(fo, currentOffset, selectedIdentifier, kind, elementSignaturePtr);
    }

    private static DataObject getDataObject (String url) {
        FileObject file;
        try {
            file = URLMapper.findFileObject (new URL (url));
        } catch (MalformedURLException e) {
            return null;
        }

        if (file == null) {
            return null;
        }
        try {
            return DataObject.find (file);
        } catch (DataObjectNotFoundException ex) {
            return null;
        }
    }

    private static StyledDocument findDocument(FileObject fo) {
        DataObject dataObject;
        try {
            dataObject = DataObject.find (fo);
        } catch (DataObjectNotFoundException ex) {
            return null;
        }
        EditorCookie ec = (EditorCookie) dataObject.getLookup().lookup(EditorCookie.class);
        if (ec == null) {
            return null;
        }
        StyledDocument doc;
        try {
            doc = ec.openDocument();
        } catch (IOException ex) {
            return null;
        }
        return doc;
    }

    private static StyledDocument findDocument(DataObject dataObject) {
        EditorCookie ec = (EditorCookie) dataObject.getLookup().lookup(EditorCookie.class);
        if (ec == null) {
            return null;
        }
        StyledDocument doc;
        try {
            doc = ec.openDocument();
        } catch (IOException ex) {
            return null;
        }
        return doc;
    }

    private static CompilationController retrieveController(ResultIterator resIt, StyledDocument doc) throws ParseException {
        Result res = resIt.getParserResult();
        CompilationController ci = res != null ? CompilationController.get(res) : null;
        if (ci == null) {
            ErrorManager.getDefault().log(ErrorManager.WARNING,
                    "Unable to get compilation controller " + doc);
        }
        return ci;
    }

    // Support classes:

    private class EditorContextDispatchListener extends Object implements PropertyChangeListener {

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            pcs.firePropertyChange (org.openide.windows.TopComponent.Registry.PROP_CURRENT_NODES, null, null);
        }

    }

    private class OperationCreationDelegateImpl implements ASTOperationCreationDelegate {
        /*
         public Operation createOperation(
                 Position startPosition,
                 Position endPosition,
                 int bytecodeIndex) {
             return EditorContextImpl.this.createOperation(
                     startPosition,
                     endPosition,
                     bytecodeIndex);
         }
         */
        @Override
         public Operation createMethodOperation(
                 Position startPosition,
                 Position endPosition,
                 Position methodStartPosition,
                 Position methodEndPosition,
                 String methodName,
                 String methodClassType,
                 int bytecodeIndex,
                 boolean isNative) {
             return EditorContextImpl.this.createMethodOperation(
                     startPosition,
                     endPosition,
                     methodStartPosition,
                     methodEndPosition,
                     methodName,
                     methodClassType,
                     bytecodeIndex, isNative);
         }
        @Override
         public Position createPosition(
                 int offset,
                 int line,
                 int column) {
             return EditorContextImpl.this.createPosition(
                     offset,
                     line,
                     column);
         }
        @Override
         public void addNextOperationTo(Operation operation, Operation next) {
             EditorContextImpl.this.addNextOperationTo(operation, next);
         }
    }

}
