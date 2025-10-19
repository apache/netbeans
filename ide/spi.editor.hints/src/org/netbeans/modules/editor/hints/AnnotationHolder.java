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
package org.netbeans.modules.editor.hints;

import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Point;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JEditorPane;
import javax.swing.JLayeredPane;
import javax.swing.JViewport;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.JTextComponent;
import javax.swing.text.Position;
import javax.swing.text.StyledDocument;
import org.netbeans.api.editor.EditorRegistry;
import org.netbeans.api.editor.document.LineDocumentUtils;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.settings.AttributesUtilities;
import org.netbeans.api.editor.settings.EditorStyleConstants;
import org.netbeans.api.editor.settings.FontColorSettings;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Utilities;
import org.netbeans.lib.editor.util.swing.DocumentListenerPriority;
import org.netbeans.lib.editor.util.swing.DocumentUtilities;
import org.netbeans.spi.editor.highlighting.HighlightAttributeValue;
import org.netbeans.spi.editor.highlighting.support.OffsetsBag;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.ErrorDescriptionFactory;
import org.netbeans.spi.editor.hints.LazyFixList;
import org.netbeans.spi.editor.hints.Severity;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.text.Annotation;
import org.openide.text.NbDocument;
import org.openide.util.RequestProcessor;
import org.openide.util.WeakListeners;
import org.openide.filesystems.FileUtil;
import org.openide.text.PositionBounds;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.NbBundle;
import org.openide.util.Pair;
import org.openide.util.RequestProcessor.Task;

/**
 *
 * @author Jan Lahoda
 */
public final class AnnotationHolder implements ChangeListener, DocumentListener {

    static final Logger LOG = Logger.getLogger(AnnotationHolder.class.getName());
    
    // mimte-type --> coloring
    private static Map<String, Map<Severity, AttributeSet>> COLORINGS =
        Collections.synchronizedMap(new HashMap<String, Map<Severity, AttributeSet>>());
    // mime-type --> listener
    private static Map<String, LookupListener> COLORINGS_LISTENERS =
        Collections.synchronizedMap(new HashMap<String, LookupListener>());

    private static final AttributeSet DEFUALT_ERROR =
            AttributesUtilities.createImmutable(EditorStyleConstants.WaveUnderlineColor, new Color(0xFF, 0x00, 0x00));
    private static final AttributeSet DEFUALT_WARNING =
            AttributesUtilities.createImmutable(EditorStyleConstants.WaveUnderlineColor, new Color(0xC0, 0xC0, 0x00));
    private static final AttributeSet DEFUALT_VERIFIER =
            AttributesUtilities.createImmutable(EditorStyleConstants.WaveUnderlineColor, new Color(0xFF, 0xD5, 0x55));
    private static final AttributeSet TOOLTIP =
            AttributesUtilities.createImmutable(EditorStyleConstants.Tooltip, new TooltipResolver());

    private Map<ErrorDescription, List<Position>> errors2Lines;
    private Map<Position, List<ErrorDescription>> line2Errors;
    private Map<Position, ParseErrorAnnotation> line2Annotations;
    private Map<String, List<ErrorDescription>> layer2Errors;

    //@GuardedBy(AWT)
    private final Set<JEditorPane> openedComponents;
    private FileObject file;
    private DataObject od;
    private final BaseDocument doc;

    private static Map<DataObject, AnnotationHolder> file2Holder = new HashMap<DataObject, AnnotationHolder>();

    static {
        EditorRegistry.addPropertyChangeListener(new PropertyChangeListener() {
            @Override public void propertyChange(PropertyChangeEvent evt) {
                if (evt.getPropertyName() == null || EditorRegistry.COMPONENT_REMOVED_PROPERTY.equals(evt.getPropertyName())) {
                    resolveAllComponents();
                } else if (EditorRegistry.FOCUS_GAINED_PROPERTY.equals(evt.getPropertyName())) {
                    JTextComponent c = EditorRegistry.focusedComponent();
                    if (c == null) {
                        //#222557: unclear how this could happen
                        resolveAllComponents();
                        return;
                    }
                    Object o = c.getDocument().getProperty(Document.StreamDescriptionProperty);
                    @SuppressWarnings("element-type-mismatch")
                    AnnotationHolder holder = file2Holder.get(o);

                    if (holder != null) {
                        holder.maybeAddComponent(c);
                    }
                }
            }
        });
    }

    private static void resolveAllComponents() {
        Map<DataObject, Set<JTextComponent>> file2Components = new HashMap<DataObject, Set<JTextComponent>>();

        for (JTextComponent c : EditorRegistry.componentList()) {
            Object o = c.getDocument().getProperty(Document.StreamDescriptionProperty);

            if (!(o instanceof DataObject)) continue;

            DataObject od = (DataObject) o;
            Set<JTextComponent> components = file2Components.get(od);

            if (components == null) {
                file2Components.put(od, components = new HashSet<JTextComponent>());
            }

            components.add(c);
        }

        Map<DataObject, AnnotationHolder> file2HolderCopy = new HashMap<DataObject, AnnotationHolder>();
        synchronized (AnnotationHolder.class) {
            file2HolderCopy.putAll(file2Holder);
        }

        for (Entry<DataObject, AnnotationHolder> e : file2HolderCopy.entrySet()) {
            Set<JTextComponent> components = file2Components.get(e.getKey());

            if (components == null) components = Collections.emptySet();

            e.getValue().setComponents(components);
        }
    }

    public static synchronized AnnotationHolder getInstance(FileObject file) {
        if (file == null)
            return null;

        try {
            DataObject od = DataObject.find(file);
            AnnotationHolder result = file2Holder.get(od);

            if (result == null) {
                EditorCookie editorCookie = od.getCookie(EditorCookie.class);

                if (editorCookie == null) {
                    LOG.log(Level.WARNING,
                            "No EditorCookie.Observable for file: {0}", FileUtil.getFileDisplayName(file)); //NOI18N
                } else {
                    Document doc = editorCookie.getDocument();

                    if (doc instanceof BaseDocument) {
                        file2Holder.put(od, result = new AnnotationHolder(file, od, (BaseDocument) doc));
                    }
                }
            }

            return result;
        } catch (IOException e) {
            LOG.log(Level.FINE, null, e);
            return null;
        }
    }

    @SuppressWarnings("LeakingThisInConstructor")
    private AnnotationHolder(FileObject file, DataObject od, BaseDocument doc) {
        openedComponents = new HashSet<>();
        
        this.doc = doc;

        if (file == null)
            return ;

        init();

        this.file = file;
        this.od = od;

        getBag(doc);

        DocumentUtilities.addPriorityDocumentListener(this.doc, this, DocumentListenerPriority.AFTER_CARET_UPDATE);

        SwingUtilities.invokeLater(new Runnable() {
            @Override public void run() {
                resolveAllComponents();
            }
        });

//        LOG.log(Level.FINE, null, new Throwable("Creating AnnotationHolder for " + file.getPath()));
        Logger.getLogger("TIMER").log(Level.FINE, "Annotation Holder", //NOI18N
                    new Object[] {file, this});
    }

    private synchronized void init() {
        errors2Lines = new IdentityHashMap<ErrorDescription, List<Position>>();
        line2Errors = new HashMap<Position, List<ErrorDescription>>();
        line2Annotations = new HashMap<Position, ParseErrorAnnotation>();
        layer2Errors = new HashMap<String, List<ErrorDescription>>();
    }

    public void stateChanged(ChangeEvent evt) {
        updateVisibleRanges();
    }

    Attacher attacher = new NbDocumentAttacher();

    void attachAnnotation(Position line, ParseErrorAnnotation a, boolean synchronous) throws BadLocationException {
        attacher.attachAnnotation(line, a, synchronous);
    }

    void detachAnnotation(ParseErrorAnnotation a, boolean synchronous) {
        attacher.detachAnnotation(a, synchronous);
    }

    static interface Attacher {
        public void attachAnnotation(Position line, ParseErrorAnnotation a, boolean synchronous) throws BadLocationException;
        public void detachAnnotation(ParseErrorAnnotation a, boolean synchronous);
    }

    final class NbDocumentAttacher implements Attacher {
        public void attachAnnotation(Position lineStart, ParseErrorAnnotation a, boolean synchronous) throws BadLocationException {
            addToToDo(new ToDo(lineStart, a), synchronous);
        }
        public void detachAnnotation(ParseErrorAnnotation a, boolean synchronous) {
            addToToDo(new ToDo(null, a), synchronous);
        }
        private void addToToDo(ToDo item, boolean synchronous) {
            if (synchronous) {
                attachDetach(item);
                return ;
            }
            synchronized (todoLock) {
                if (todo == null) {
                    todo = new ArrayList<ToDo>();
                    ATTACHER.schedule(50);
                }
                todo.add(item);
            }
        }
    }

    private static class ToDo {
        private final Position lineStart;
        private final ParseErrorAnnotation a;
        public ToDo(Position lineStart, ParseErrorAnnotation a) {
            this.lineStart = lineStart;
            this.a = a;
        }
    }
    
    private void attachDetach(ToDo t) {
        if (t.lineStart != null) {
            if (LOG.isLoggable(Level.FINE)) {
                LOG.fine("addAnnotation: pos=" + t.lineStart.getOffset() + ", a="+ t.a + ", doc=" +
                        System.identityHashCode(doc) + "\n");
            }
            t.a.attachAnnotation((StyledDocument) doc, t.lineStart);
        } else {
            if (doc != null) {
                if (LOG.isLoggable(Level.FINE)) {
                    LOG.fine("removeAnnotation: a=" + t.a + ", doc=" + System.identityHashCode(doc) + "\n");
                }
                t.a.detachAnnotation((StyledDocument) doc);
            }
        }
    }
    
    private static final RequestProcessor ATTACHING_THREAD = new RequestProcessor(AnnotationHolder.class.getName(), 1, false, false);
    private List<ToDo> todo;
    private final Object todoLock = new Object();
    private final Task ATTACHER = ATTACHING_THREAD.create(new Runnable() {
        @Override public void run() {
            List<ToDo> todo = null;
            synchronized (todoLock) {
                todo = AnnotationHolder.this.todo;
                AnnotationHolder.this.todo = null;
            }
            
            if (todo == null) return;
            
            for (ToDo t : todo) {
                attachDetach(t);
            }
        }
    });
        
    private synchronized void clearAll() {
        //remove all annotations:
        for (ParseErrorAnnotation a : line2Annotations.values()) {
            detachAnnotation(a, false);
        }
        line2Annotations.clear();

        file2Holder.remove(od);
        DocumentUtilities.removePriorityDocumentListener(this.doc, this, DocumentListenerPriority.AFTER_CARET_UPDATE);

        getBag(doc).clear();
    }

    private synchronized void maybeAddComponent(JTextComponent c) {
        if (!(c instanceof JEditorPane)) return;

        JEditorPane pane = (JEditorPane) c;

        if (!openedComponents.add(pane)) return;

        addViewportListener(pane);
        updateVisibleRanges();
    }

    private void addViewportListener(JEditorPane pane) {
        Container parent = pane.getParent();
        
        if (parent instanceof JLayeredPane) {
            parent = parent.getParent();
        }

        if (parent instanceof JViewport) {
            JViewport viewport = (JViewport) parent;

            viewport.addChangeListener(WeakListeners.change(AnnotationHolder.this, viewport));
        }
    }

    private synchronized void setComponents(Set<JTextComponent> newComponents) {
        if (newComponents.isEmpty()) {
            clearAll();
            return;
        }

        Set<JEditorPane> addedPanes = new HashSet<JEditorPane>();

        for (JTextComponent c : newComponents) {
            if (!(c instanceof JEditorPane)) continue;

            addedPanes.add((JEditorPane) c);
        }

        Set<JEditorPane> removedPanes = new HashSet<JEditorPane>(openedComponents);

        removedPanes.removeAll(addedPanes);
        addedPanes.removeAll(openedComponents);

        for (JEditorPane pane : addedPanes) {
            addViewportListener(pane);
        }

        openedComponents.removeAll(removedPanes);
        openedComponents.addAll(addedPanes);

        updateVisibleRanges();
    }

    public synchronized void insertUpdate(DocumentEvent e) {
        try {
            int offset = LineDocumentUtils.getLineStartOffset(doc, e.getOffset());

            Set<Position> modifiedLines = new HashSet<Position>();

            int index = findPositionGE(offset);

            if (index == knownPositions.size())
                return ;

            Position line = knownPositions.get(index).get();

            if (line == null)
                return ;

            int endOffset = LineDocumentUtils.getLineEndOffset(doc, e.getOffset() + e.getLength());

            if (endOffset < line.getOffset())
                return;

            clearLineErrors(line, modifiedLines);

            //make sure the highlights are removed even for multi-line inserts:
            try {
                int rowStart = e.getOffset();
                int rowEnd = LineDocumentUtils.getLineEndOffset(doc, e.getOffset() + e.getLength());

                getBag(doc).removeHighlights(rowStart, rowEnd, false);
            } catch (BadLocationException ex) {
                throw new IOException(ex);
            }

            for (Position lineToken : modifiedLines) {
                updateAnnotationOnLine(lineToken, false);
                updateHighlightsOnLine(lineToken);
            }
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        } catch (BadLocationException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    public synchronized void removeUpdate(DocumentEvent e) {
        try {
            Position current = null;
            int index = -1;
            int startOffset = LineDocumentUtils.getLineStartOffset(doc, e.getOffset());

            while (current == null) {
                index = findPositionGE(startOffset);

                if (knownPositions.isEmpty()) {
                    break;
                }
                if (index == knownPositions.size()) {
                    return;
                }
                current = knownPositions.get(index).get();
            }

            if (current == null) {
                //nothing to do:
                return;
            }

            int endOffset = LineDocumentUtils.getLineEndOffset(doc, e.getOffset());

            if (endOffset < current.getOffset())
                return;

            assert index != (-1);

            //find the first:
            while (index > 0) {
                Position minusOne = knownPositions.get(index - 1).get();

                if (minusOne == null) {
                    index--;
                    continue;
                }

                if (minusOne.getOffset() != current.getOffset()) {
                    break;
                }

                index--;
            }

            Set<Position> modifiedLinesTokens = new HashSet<Position>();

            while (index < knownPositions.size()) {
                Position next = knownPositions.get(index).get();

                if (next == null) {
                    index++;
                    continue;
                }

                if (next.getOffset() != current.getOffset()) {
                    break;
                }

                modifiedLinesTokens.add(next);
                index++;
            }

            for (Position line : new LinkedList<Position>(modifiedLinesTokens)) {
                clearLineErrors(line, modifiedLinesTokens);
            }

            for (Position line : modifiedLinesTokens) {
                updateAnnotationOnLine(line, false);
                updateHighlightsOnLine(line);
            }
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        } catch (BadLocationException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    private void clearLineErrors(Position line, Set<Position> modifiedLinesTokens) {
        List<ErrorDescription> eds = getErrorsForLine(line, false);

        if (eds == null)
            return ;

        eds = new LinkedList<ErrorDescription>(eds);

        for (ErrorDescription ed : eds) {
            List<Position> lines = errors2Lines.remove(ed);

            if (lines == null) { //#	180222 
                LOG.log(Level.WARNING, "Inconsistent error2Lines for file {1}.", new Object[] {file.getPath()}); // NOI18N
                continue;
            }

            for (Position i : lines) {
                if (line2Errors.get(i) != null) {
                    line2Errors.get(i).remove(ed);
                }
                modifiedLinesTokens.add(i);
            }
            for (List<ErrorDescription> edsForLayer : layer2Errors.values()) {
                edsForLayer.remove(ed);
            }
        }

        line2Errors.remove(line);
    }

    public void changedUpdate(DocumentEvent e) {
        //ignored
    }

    private void updateVisibleRanges() {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                long startTime = System.currentTimeMillis();
                final List<int[]> visibleRanges = new ArrayList<int[]>();

                doc.render(new Runnable() {
                    public void run() {
                        for (JEditorPane pane : openedComponents) {
                            Container parent = pane.getParent();
                            
                            if (parent instanceof JLayeredPane) {
                                parent = parent.getParent();
                            }

                            if (parent instanceof JViewport) {
                                JViewport viewport = (JViewport) parent;
                                Point start = viewport.getViewPosition();
                                Dimension size = viewport.getExtentSize();
                                Point end = new Point(start.x + size.width, start.y + size.height);

                                int startPosition = pane.viewToModel(start);
                                int endPosition = pane.viewToModel(end);
                                //TODO: check differences against last:
                                visibleRanges.add(new int[]{startPosition, endPosition});
                            }
                        }
                    }
                });

                INSTANCE.post(new Runnable() {
                    public void run() {
                        for (int[] span : visibleRanges) {
                            updateAnnotations(span[0], span[1]);
                        }
                    }
                });

                long endTime = System.currentTimeMillis();

                LOG.log(Level.FINE, "updateVisibleRanges: time={0}", endTime - startTime); //NOI18N
            }
        });
    }

    private void updateAnnotations(final int startPosition, final int endPosition) {
        long startTime = System.currentTimeMillis();
        final List<ErrorDescription> errorsToUpdate = new ArrayList<ErrorDescription>();

        doc.render(new Runnable() {
            public void run() {
                synchronized (AnnotationHolder.this) {
                    try {
                        if (doc.getLength() == 0) {
                            return ;
                        }

                        int start = startPosition < doc.getLength() ? startPosition : (doc.getLength() - 1);
                        int end   = endPosition < doc.getLength() ? endPosition : (doc.getLength() - 1);

                        if (start < 0) start = 0;
                        if (end < 0) end = 0;

                        int startLine = LineDocumentUtils.getLineStartOffset(doc, start);
                        int endLine = LineDocumentUtils.getLineEndOffset(doc, end) + 1;

                        int index = findPositionGE(startLine);

                        while (index < knownPositions.size()) {
                            Reference<Position> r = knownPositions.get(index++);
                            if (r==null)
                                continue;
                            Position lineToken = r.get();

                            if (lineToken == null)
                                continue;

                            if (lineToken.getOffset() > endLine)
                                break;

                            List<ErrorDescription> errors = line2Errors.get(lineToken);

                            if (errors != null) {
                                errorsToUpdate.addAll(errors);
                            }
                        }
                    } catch (BadLocationException e) {
                        Exceptions.printStackTrace(e);
                    }
                }

            }
        });

        LOG.log(Level.FINE, "updateAnnotations: errorsToUpdate={0}", errorsToUpdate); //NOI18N

        for (ErrorDescription e : errorsToUpdate) {
            //TODO: #115340: e can be for an unknown reason null:
            if (e == null) {
                continue;
            }

            LazyFixList l = e.getFixes();

            if (l.probablyContainsFixes() && !l.isComputed()) {
                l.getFixes();
            }
        }

        long endTime = System.currentTimeMillis();

        LOG.log(Level.FINE, "updateAnnotations: time={0}", endTime - startTime); //NOI18N
    }

    private List<ErrorDescription> getErrorsForLayer(String layer) {
        List<ErrorDescription> errors = layer2Errors.get(layer);

        if (errors == null) {
            layer2Errors.put(layer, errors = new ArrayList<ErrorDescription>());
        }

        return errors;
    }

    private List<ErrorDescription> getErrorsForLine(Position line, boolean create) {
        List<ErrorDescription> errors = line2Errors.get(line);

        if (errors == null && create) {
            line2Errors.put(line, errors = new ArrayList<ErrorDescription>());
        }

        if (errors != null && errors.isEmpty() && !create) {
            //clean:
            line2Errors.remove(line);
            errors = null;
        }

        return errors;
    }

    private static List<ErrorDescription> filter(List<ErrorDescription> errors, boolean onlyErrors) {
        List<ErrorDescription> result = new ArrayList<ErrorDescription>();

        for (ErrorDescription e : errors) {
            if (e.getSeverity() == Severity.ERROR) {
                if (onlyErrors)
                    result.add(e);
            } else {
                if (!onlyErrors)
                    result.add(e);
            }
        }

        return result;
    }

    private static void concatDescription(List<ErrorDescription> errors, StringBuffer description) {
        boolean first = true;

        for (ErrorDescription e : errors) {
            String desc = e.getDescription();
            if (desc != null && desc.length() > 0) {
                if (!first) {
                    description.append("\n\n"); //NOI18N
                }
                description.append(desc);
                first = false;
            }
        }
    }

    private LazyFixList computeFixes(List<ErrorDescription> errors) {
        List<LazyFixList> result = new ArrayList<LazyFixList>();

        for (ErrorDescription e : errors) {
            result.add(e.getFixes());
        }

        return ErrorDescriptionFactory.lazyListForDelegates(result);
    }

    private void updateAnnotationOnLine(Position line, boolean synchronous) throws BadLocationException {
        List<ErrorDescription> errorDescriptions = getErrorsForLine(line, false);

        if (errorDescriptions == null) errorDescriptions = Collections.emptyList();
        else errorDescriptions = new ArrayList<>(errorDescriptions);

        Severity mostImportantSeverity = Severity.HINT;
        String customType = null;
        
        for (Iterator<ErrorDescription> it = errorDescriptions.iterator(); it.hasNext();) {
            ErrorDescription ed = it.next();
            List<Position> positions = errors2Lines.get(ed);

            if (positions == null || positions.isEmpty() || positions.get(0) != line) {
                it.remove();
            } else {
                if (mostImportantSeverity.compareTo(ed.getSeverity()) > 0) {
                    mostImportantSeverity = ed.getSeverity();
                }
                customType = ed.getCustomType();
            }
        }

        if (errorDescriptions.isEmpty()) {
            //nothing to do, remove old:
            ParseErrorAnnotation ann = line2Annotations.remove(line);
            if (ann != null) {
                detachAnnotation(ann, synchronous);
            }
            return;
        }

        Pair<FixData, String> fixData = buildUpFixDataForLine(line);

        ParseErrorAnnotation pea;
        if (customType == null) {
            pea = new ParseErrorAnnotation(
                    mostImportantSeverity,
                    fixData.first(),
                    fixData.second(),
                    line,
                    this);
        } else {
            pea = new ParseErrorAnnotation(
                    mostImportantSeverity,
                    customType,
                    fixData.first(),
                    fixData.second(),
                    line,
                    this);
        }
        ParseErrorAnnotation previous = line2Annotations.put(line, pea);

        if (previous != null) {
            detachAnnotation(previous, synchronous);
        }

        attachAnnotation(line, pea, synchronous);   
    }

    public Pair<FixData, String> buildUpFixDataForLine(int caretLine) {
        try {
            Position line = getPosition(caretLine, false);
            
            if (line == null) return null;

            return buildUpFixDataForLine(line);
        } catch (BadLocationException ex) {
            LOG.log(Level.FINE, null, ex);
            return null;
        }
    }

    private Pair<FixData, String> buildUpFixDataForLine(Position line) {
        List<ErrorDescription> errorDescriptions = getErrorsForLine(line, true);
        if (errorDescriptions.isEmpty()) {
            return null;
        }

        List<ErrorDescription> trueErrors = filter(errorDescriptions, true);
        List<ErrorDescription> others = filter(errorDescriptions, false);

        //build up the description of the annotation:
        StringBuffer description = new StringBuffer();

        concatDescription(trueErrors, description);

        if (!trueErrors.isEmpty() && !others.isEmpty()) {
            description.append("\n\n"); //NOI18N
        }

        concatDescription(others, description);

        return Pair.of(new FixData(computeFixes(trueErrors), computeFixes(others)), description.toString());
    }

    void updateHighlightsOnLine(Position line) throws IOException {
        List<ErrorDescription> errorDescriptions = getErrorsForLine(line, false);

        OffsetsBag bag = getBag(doc);

        updateHighlightsOnLine(bag, doc, line, errorDescriptions);
    }

    static void updateHighlightsOnLine(OffsetsBag bag, BaseDocument doc, Position line, List<ErrorDescription> errorDescriptions) throws IOException {
        try {
            int rowStart = line.getOffset();
            int rowEnd = LineDocumentUtils.getLineEndOffset(doc, rowStart);
            int rowHighlightStart = Utilities.getRowFirstNonWhite(doc, rowStart);
            int rowHighlightEnd = Utilities.getRowLastNonWhite(doc, rowStart) + 1;

            if (rowStart <= rowEnd) {
                bag.removeHighlights(rowStart, rowEnd, false);
            }

            if (errorDescriptions != null) {
                bag.addAllHighlights(computeHighlights(doc, errorDescriptions).getHighlights(rowHighlightStart, rowHighlightEnd));
            }
        } catch (BadLocationException ex) {
            throw new IOException(ex);
        }
    }

    static OffsetsBag computeHighlights(Document doc, List<ErrorDescription> errorDescriptions) throws IOException, BadLocationException {
        OffsetsBag bag = new OffsetsBag(doc);
        for (Severity s : Arrays.asList(Severity.VERIFIER, Severity.WARNING, Severity.ERROR)) {
            List<ErrorDescription> filteredDescriptions = new ArrayList<ErrorDescription>();

            for (ErrorDescription e : errorDescriptions) {
                if (e.getSeverity() == s) {
                    filteredDescriptions.add(e);
                }
            }

            List<int[]> currentHighlights = new ArrayList<int[]>();

            for (ErrorDescription e : filteredDescriptions) {
                addHighlights(currentHighlights, e.getRange());
                for (PositionBounds positionBounds : e.getRangeTail()) {
                    addHighlights(currentHighlights, positionBounds);
                }
            }

            for (int[] h : currentHighlights) {
                if (h[0] <= h[1]) {
                    bag.addHighlight(h[0], h[1], getColoring(s, doc));
                } else {
                    //see issue #112566
                    StringBuilder sb = new StringBuilder();

                    for (ErrorDescription e : filteredDescriptions) {
                        sb.append("["); //NOI18N
                        sb.append(e.getRange().getBegin().getOffset());
                        sb.append("-"); //NOI18N
                        sb.append(e.getRange().getEnd().getOffset());
                        sb.append("]"); //NOI18N
                    }

                    sb.append("=>"); //NOI18N

                    for (int[] h2 : currentHighlights) {
                        sb.append("["); //NOI18N
                        sb.append(h2[0]);
                        sb.append("-"); //NOI18N
                        sb.append(h2[1]);
                        sb.append("]"); //NOI18N
                    }

                    LOG.log(Level.WARNING, "Incorrect highlight computed, please reopen issue #112566 and attach the following output: {0}", sb.toString()); //NOI18N
                }
            }
        }

        return bag;
    }

    private static void addHighlights(List<int[]> currentHighlights, PositionBounds pos) throws IOException {
        int beginOffset = pos.getBegin().getPosition().getOffset();
        int endOffset = pos.getEnd().getPosition().getOffset();

        if (endOffset < beginOffset) {
            //see issue #112566
            int swap = endOffset;

            endOffset = beginOffset;
            beginOffset = swap;

            LOG.log(Level.WARNING, "Incorrect highlight in ErrorDescription, attach your messages.log to issue #112566: {0}", pos.toString()); //NOI18N
        }

        int[] h = new int[]{beginOffset, endOffset};

        OUT:
        for (Iterator<int[]> it = currentHighlights.iterator(); it.hasNext() && h != null;) {
            int[] hl = it.next();

            switch (detectCollisions(hl, h)) {
                case 0:
                    break;
                case 1:
                    it.remove();
                    break;
                case 2:
                    h = null; //nothing to add, hl is bigger:
                    break OUT;
                case 4:
                case 3:
                    int start = Math.min(hl[0], h[0]);
                    int end = Math.max(hl[1], h[1]);

                    h = new int[]{start, end};
                    it.remove();
                    break;
            }
        }

        if (h != null) {
            currentHighlights.add(h);
        }        
    }

    static AttributeSet getColoring(Severity s, Document d) {
        final String mimeType = DocumentUtilities.getMimeType(d);
        Map<Severity, AttributeSet> coloring = COLORINGS.get(mimeType);
        if (coloring == null) {
            coloring = new EnumMap<Severity, AttributeSet>(Severity.class);
            Lookup lookup = MimeLookup.getLookup(mimeType);
            Lookup.Result<FontColorSettings> result = lookup.lookupResult(FontColorSettings.class);
            
            LookupListener lookupListener = COLORINGS_LISTENERS.get(mimeType);
            if (lookupListener == null) {
                lookupListener = new LookupListener() {
                    @Override
                    public void resultChanged(LookupEvent ev) {
                        COLORINGS.remove(mimeType);
                    }
                };
                COLORINGS_LISTENERS.put(mimeType, lookupListener);
                result.addLookupListener(
                    WeakListeners.create(
                        LookupListener.class,
                        lookupListener,
                        result
                    )
                );
            }
            final Iterator<? extends FontColorSettings> it = result.allInstances().iterator();
            AttributeSet error;
            AttributeSet warning;
            AttributeSet verifier;
            if (it.hasNext()) {
                FontColorSettings fcs = it.next();
                AttributeSet attributes = fcs.getTokenFontColors("errors"); // NOI18N
                if (attributes != null) {
                    error = attributes;
                } else {
                    attributes = fcs.getTokenFontColors("error"); // NOI18N
                    if (attributes != null) {
                        error = attributes;
                    } else {
                        error = DEFUALT_ERROR;
                    }
                }
                attributes = fcs.getTokenFontColors("warning"); // NOI18N
                if (attributes != null) {
                    warning = attributes;
                    verifier = attributes;
                } else {
                    warning = DEFUALT_WARNING;
                    verifier = DEFUALT_VERIFIER;
                }
            } else {
                error = DEFUALT_ERROR;
                warning = DEFUALT_WARNING;
                verifier = DEFUALT_VERIFIER;
            }
            coloring.put(Severity.ERROR, AttributesUtilities.createComposite(error, TOOLTIP));
            coloring.put(Severity.WARNING, AttributesUtilities.createComposite(warning, TOOLTIP));
            coloring.put(Severity.VERIFIER, AttributesUtilities.createComposite(verifier, TOOLTIP));
            coloring.put(Severity.HINT, TOOLTIP);
            COLORINGS.put(mimeType, coloring);
        }
        return coloring.get(s);
    }
    
    private static int detectCollisions(int[] h1, int[] h2) {
        if (h2[1] < h1[0])
            return 0;//no collision
        if (h1[1] < h2[0])
            return 0;//no collision
        if (h2[0] < h1[0] && h2[1] > h1[1])
            return 1;//h2 encapsulates h1
        if (h1[0] < h2[0] && h1[1] > h2[1])
            return 2;//h1 encapsulates h2

        if (h1[0] < h2[0])
            return 3;//collides
        else
            return 4;
    }

    public void setErrorDescriptions(final String layer, final Collection<? extends ErrorDescription> errors) {
        setErrorDescriptions(layer, errors, false);
    }
    
    private void setErrorDescriptions(final String layer, final Collection<? extends ErrorDescription> errors, final boolean synchronous) {
        doc.render(new Runnable() {
            public void run() {
                try {
                    setErrorDescriptionsImpl(file, layer, errors, synchronous);
                } catch (IOException e) {
                    LOG.log(Level.WARNING, e.getMessage(), e);
                }
            }
        });
    }

    private synchronized void setErrorDescriptionsImpl(FileObject file, String layer, Collection<? extends ErrorDescription> errors, boolean synchronous) throws IOException {
        long start = System.currentTimeMillis();

        try {
            if (file == null)
                return ;

            List<ErrorDescription> layersErrors = getErrorsForLayer(layer);

            Set<Position> primaryLines = new HashSet<Position>();
            Set<Position> allLines = new HashSet<Position>();

            for (ErrorDescription ed : layersErrors) {
                List<Position> lines = errors2Lines.remove(ed);
                if (lines == null) { //#134282
                    LOG.log(Level.WARNING, "Inconsistent error2Lines for layer {0}, file {1}.", new Object[] {layer, file.getPath()}); // NOI18N
                    continue;
                }

                boolean first = true;

                for (Position line : lines) {
                    List<ErrorDescription> errorsForLine = getErrorsForLine(line, false);

                    if (errorsForLine != null) {
                        errorsForLine.remove(ed);
                    }

                    if (first) {
                        primaryLines.add(line);
                    }

                    allLines.add(line);
                    first = false;
                }
            }

            List<ErrorDescription> validatedErrors = new ArrayList<ErrorDescription>();

            for (ErrorDescription ed : errors) {
                if (ed == null) {
                    LOG.log(Level.WARNING, "'null' ErrorDescription in layer {0}.", layer); //NOI18N
                    continue;
                }

                if (ed.getRange() == null)
                    continue;

                validatedErrors.add(ed);

                List<Position> lines = new ArrayList<Position>();
                int startLine = ed.getRange().getBegin().getLine();
                int endLine = ed.getRange().getEnd().getLine();

                for (int cntr = startLine; cntr <= endLine; cntr++) {
                    Position p = getPosition(cntr, true);
                    lines.add(p);
                }

                errors2Lines.put(ed, lines);

                boolean first = true;

                for (Position line : lines) {
                    getErrorsForLine(line, true).add(ed);

                    if (first) {
                        primaryLines.add(line);
                    }

                    allLines.add(line);
                    first = false;
                }
            }

            layersErrors.clear();
            layersErrors.addAll(validatedErrors);

            for (Position line : primaryLines) {
                updateAnnotationOnLine(line, synchronous);
            }

            for (Position line : allLines) {
                updateHighlightsOnLine(line);
            }

            updateVisibleRanges();

            SwingUtilities.invokeLater(new Runnable() {
                @Override public void run() {
                    HintsUI.getDefault().caretUpdate(null);
                }
            });
        } catch (BadLocationException ex) {
            throw new IOException(ex);
        } finally {
            long end = System.currentTimeMillis();
            Logger.getLogger("TIMER").log(Level.FINE, "Errors update for " + layer, //NOI18N
                    new Object[] {file, end - start});
        }
    }

    private List<Reference<Position>> knownPositions = new ArrayList<Reference<Position>>();

    private static class Abort extends RuntimeException {
        @Override
        public synchronized Throwable fillInStackTrace() {
            return this;
        }
    }

    private static RuntimeException ABORT = new Abort();

    private synchronized int findPositionGE(int offset) {
        while (true) {
            try {
                int index = Collections.binarySearch(knownPositions, offset, new PositionComparator());

                if (index >= 0) {
                    return index;
                } else {
                    return - (index + 1);
                }
            } catch (Abort a) {
                LOG.log(Level.FINE, "a null Position detected - clearing"); //NOI18N
                int removedCount = 0;
                for (Iterator<Reference<Position>> it = knownPositions.iterator(); it.hasNext(); ) {
                    if (it.next().get() == null) {
                        removedCount++;
                        it.remove();
                    }
                }
                LOG.log(Level.FINE, "clearing finished, {0} positions cleared", removedCount); //NOI18N
            }
        }
    }

    private synchronized Position getPosition(int lineNumber, boolean create) throws BadLocationException {
        try {
            while (true) {
                int lineStart = Utilities.getRowStartFromLineOffset(doc, lineNumber);
                if (lineStart < 0) {
                    Element lineRoot = doc.getDefaultRootElement();
                    int lineElementCount = lineRoot.getElementCount();
                    LOG.info("AnnotationHolder: Invalid lineNumber=" + lineNumber + // NOI18N
                            ", lineStartOffset=" + lineStart + ", lineElementCount=" + lineElementCount + // NOI18N
                            ", docReadLocked=" + DocumentUtilities.isReadLocked(doc) + ", doc:\n" + doc + '\n'); // NOI18N
                    // Correct the lineStart
                    if (lineNumber < 0) {
                        lineStart = 0;
                    } else { // Otherwise use last line
                        lineStart = lineRoot.getElement(lineRoot.getElementCount() - 1).getStartOffset();
                    }
                }
                try {
                    int index = Collections.binarySearch(knownPositions, lineStart, new PositionComparator());

                    if (index >= 0) {
                        Reference<Position> r = knownPositions.get(index);
                        Position p = r.get();

                        if (p != null) {
                            return p;
                        }
                    }

                    if (!create)
                        return null;

                    Position p = NbDocument.createPosition(doc, lineStart, Position.Bias.Forward);

                    knownPositions.add(- (index + 1), new WeakReference<Position>(p));

                    Logger.getLogger("TIMER").log(Level.FINE, "Annotation Holder - Line Token", //NOI18N
                            new Object[] {file, p});

                    return p;
                } catch (Abort a) {
                    LOG.log(Level.FINE, "a null Position detected - clearing"); //NOI18N
                    int removedCount = 0;
                    for (Iterator<Reference<Position>> it = knownPositions.iterator(); it.hasNext(); ) {
                        if (it.next().get() == null) {
                            removedCount++;
                            it.remove();
                        }
                    }
                    LOG.log(Level.FINE, "clearing finished, {0} positions cleared", removedCount); //NOI18N
                }
            }
        } finally {
            LOG.log(Level.FINE, "knownPositions.size={0}", knownPositions.size()); //NOI18N
        }
    }

    public synchronized boolean hasErrors() {
        for (ErrorDescription e : errors2Lines.keySet()) {
            if (e.getSeverity() == Severity.ERROR)
                return true;
        }

        return false;
    }
    
    public Document getDocument() {
        return doc;
    }

    public synchronized List<ErrorDescription> getErrors() {
        return new ArrayList<ErrorDescription>(errors2Lines.keySet());
    }

    public synchronized List<Annotation> getAnnotations() {
        return new ArrayList<Annotation>(line2Annotations.values());
    }

    public void setErrorsForLine(final int offset, final Map<String, List<ErrorDescription>> errs) {

        doc.render(new Runnable() {

            public void run() {
                if (offset > doc.getLength()) {
                  //may happen if the document has been modified between
                  //the point caret location was read and the point where
                  //the document has been locked:
                  return;
                }
                try {
                    if (offset > doc.getLength()) {
                        //may happen if the document has been modified between
                        //the point caret location was read and the point where
                        //the document has been locked:
                        return ;
                    }

                    Position pos = getPosition(LineDocumentUtils.getLineIndex(doc, offset), true);

                    List<ErrorDescription> errsForCurrentLine = getErrorsForLine(pos, true);

                    //for each layer
                    for (Entry<String, List<ErrorDescription>> e : errs.entrySet()) {
                        //get errors for this layer, all lines
                        Set<ErrorDescription> errorsForLayer = new HashSet<ErrorDescription>(getErrorsForLayer(e.getKey()));
                        errorsForLayer.removeAll(errsForCurrentLine); //remove all for current line
                        
                        Set<ErrorDescription> toSet = new HashSet<ErrorDescription>();
                        toSet.addAll(e.getValue());
                        toSet.addAll(errorsForLayer);
                        e.getValue().clear();
                        e.getValue().addAll(toSet); //add the rest to those provided by refresher
                    }
                } catch (BadLocationException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        });

        for (Entry<String, List<ErrorDescription>> e : errs.entrySet()) {
            final List<ErrorDescription> eds = e.getValue();
            setErrorDescriptions(e.getKey(), eds, true); //set updated
        }
    }

    public synchronized List<ErrorDescription> getErrorsGE(int offset) {
        try {
            int index = findPositionGE(LineDocumentUtils.getLineStartOffset(doc, offset));
            if (index < 0) return Collections.emptyList();

            while (index < knownPositions.size()) {
                Position current = knownPositions.get(index++).get();

                if (current == null) {
                    continue;
                }

                List<ErrorDescription> errors = line2Errors.get(current);

                if (errors != null) {
                    SortedMap<Integer, List<ErrorDescription>> sortedErrors = new TreeMap<Integer, List<ErrorDescription>>();

                    for (ErrorDescription ed : errors) {
                        List<ErrorDescription> errs = sortedErrors.get(ed.getRange().getBegin().getOffset());

                        if (errs == null) {
                            sortedErrors.put(ed.getRange().getBegin().getOffset(), errs = new LinkedList<ErrorDescription>());
                        }

                        errs.add(ed);
                    }

                    SortedMap<Integer, List<ErrorDescription>> tail = sortedErrors.tailMap(offset);

                    if (!tail.isEmpty()) {
                        Integer k = tail.firstKey();

                        return new ArrayList<ErrorDescription>(sortedErrors.get(k));
                    }
                }
            }

            return Collections.emptyList();
        } catch (BadLocationException ex) {
            Exceptions.printStackTrace(ex);
            return Collections.emptyList();
        }
    }

    private static final RequestProcessor INSTANCE = new RequestProcessor("AnnotationHolder"); //NOI18N

    public static OffsetsBag getBag(Document doc) {
        OffsetsBag ob = (OffsetsBag) doc.getProperty(AnnotationHolder.class);

        if (ob == null) {
            doc.putProperty(AnnotationHolder.class, ob = new OffsetsBag(doc));
        }

        return ob;
    }

    public int lineNumber(final Position offset) {
        final int[] result = new int[] {-1};

        doc.render(new Runnable() {
            public void run() {
                try {
                    result[0] = LineDocumentUtils.getLineIndex(doc, offset.getOffset());
                } catch (BadLocationException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        });

        return result[0];
    }
    
    private static final boolean ENABLE_ASSERTS = Boolean.getBoolean(AnnotationHolder.class.getName() + ".enableAsserts200469");
    
    private static class PositionComparator implements Comparator<Object> {

        private PositionComparator() {
        }

        public int compare(Object o1, Object o2) {
            int left = -1;

            if (o1 instanceof Reference<?>) {
                Position value = (Position) ((Reference<?>) o1).get();

                if (value == null) {
                    //already collected...
                    throw ABORT;
                }

                left = value.getOffset();
                assert !ENABLE_ASSERTS || left != -1 : "o1=" + o1 + ", value=" + value; //NOI18N

            } else if (o1 instanceof Integer) {
                left = ((Integer) o1);
                assert !ENABLE_ASSERTS || left != -1 : "o1=" + o1;

            } else {
                assert !ENABLE_ASSERTS || false : "Unexpected type: o1=" + o1; //NOI18N
            }

            int right = -1;

            if (o2 instanceof Reference<?>) {
                Position value = (Position) ((Reference<?>) o2).get();

                if (value == null) {
                    //already collected...
                    throw ABORT;
                }

                right = value.getOffset();
                assert !ENABLE_ASSERTS || right != -1 : "o2=" + o2 + ", value=" + value; //NOI18N

            } else if (o2 instanceof Integer) {
                right = ((Integer) o2);
                assert !ENABLE_ASSERTS || right != -1 : "o2=" + o2;

            } else {
                assert !ENABLE_ASSERTS || false : "Unexpected type: o2=" + o2; //NOI18N
            }

            return left - right;
        }
    }

        public static String resolveWarnings(final Document document, final int startOffset, final int endOffset) {
            final Object source = document.getProperty(Document.StreamDescriptionProperty);

            if (!(source instanceof DataObject) || !(document instanceof BaseDocument)) {
                return null;
            }

            final String[] result = new String[1];

            document.render(new Runnable() {
                public void run() {
                    try {
                        if (endOffset > document.getLength()) {
                            //may happen if the document has been modified between
                            //the point caret location was read and the point where
                            //the document has been locked:
                            return ;
                        }
                        
                        int lineNumber = LineDocumentUtils.getLineIndex((BaseDocument) document, startOffset);

                        if (lineNumber < 0) {
                            return;
                        }

                        FileObject file = ((DataObject) source).getPrimaryFile();
                        AnnotationHolder h = AnnotationHolder.getInstance(file);

                        if (h == null) {
                            LOG.log(Level.INFO,
                                    "File: {0}\nStartOffset: {1}", new Object[]{file.getPath(), startOffset}); // NOI18N
                            return;
                        }

                        synchronized (h) {
                            Position p = h.getPosition(lineNumber, false);

                            if (p == null) {
                                return ;
                            }

                            List<ErrorDescription> errors = h.line2Errors.get(p);

                            if (errors == null || errors.isEmpty()) {
                                return;
                            }

                            List<ErrorDescription> trueErrors = new LinkedList<ErrorDescription>();
                            List<ErrorDescription> others = new LinkedList<ErrorDescription>();

                            for (ErrorDescription ed : errors) {
                                if (ed == null) continue;

                                PositionBounds pb = ed.getRange();

                                if (startOffset > pb.getEnd().getOffset() || pb.getBegin().getOffset() > endOffset) {
                                    continue;
                                }

                                if (pb.getBegin().getOffset() == pb.getEnd().getOffset()) {
                                    continue;
                                }

                                if (ed.getSeverity() == Severity.ERROR) {
                                    trueErrors.add(ed);
                                } else {
                                    others.add(ed);
                                }
                            }

                            //build up the description of the annotation:
                            StringBuffer description = new StringBuffer();

                            concatDescription(trueErrors, description);

                            if (!trueErrors.isEmpty() && !others.isEmpty()) {
                                description.append("\n\n"); //NOI18N
                            }

                            concatDescription(others, description);

                            result[0] = description.toString(); //NOI18N
                        }
                    } catch (BadLocationException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
            });

            return result[0];
        }
        
    private static final class TooltipResolver implements HighlightAttributeValue<String> {

        public String getValue(final JTextComponent component, final Document document, Object attributeKey, final int startOffset, final int endOffset) {
            return resolveWarnings(document, startOffset, endOffset) + NbBundle.getMessage(AnnotationHolder.class, "LBL_shortcut_promotion");
        }

    }

}
