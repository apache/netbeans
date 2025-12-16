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
package org.netbeans.modules.versioning.annotate;

import org.openide.util.RequestProcessor;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.ErrorManager;
import org.openide.xml.XMLUtil;
import org.openide.text.NbDocument;
import org.netbeans.modules.versioning.util.Utils;
import org.netbeans.api.editor.fold.FoldHierarchy;
import org.netbeans.api.diff.Difference;
import org.netbeans.editor.*;
import org.netbeans.editor.Utilities;
import org.netbeans.spi.diff.DiffProvider;

import javax.swing.*;
import javax.swing.Timer;
import javax.swing.text.*;
import javax.swing.event.DocumentListener;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.ChangeEvent;
import javax.accessibility.Accessible;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import java.awt.event.*;
import java.awt.*;
import java.util.*;
import java.util.List;
import java.io.File;
import java.io.Reader;
import java.io.IOException;
import java.io.CharConversionException;
import java.text.DateFormat;
import org.netbeans.api.editor.document.LineDocumentUtils;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.settings.EditorStyleConstants;
import org.netbeans.api.editor.settings.FontColorNames;
import org.netbeans.api.editor.settings.FontColorSettings;
import org.openide.util.Mutex;

/**
 * Represents annotation sidebar componnet in editor. It's
 * created by {@link AnnotationBarManager}.
 *
 * <p>It reponds to following external signals:
 * <ul>
 *   <li> {@link #annotate} message
 * </ul>
 *
 * @author Petr Kuzel
 */
final class AnnotationBar extends JComponent implements Accessible, PropertyChangeListener, DocumentListener, ChangeListener, ActionListener, Runnable, ComponentListener {

    /**
     * Target text component for which the annotation bar is aiming.
     */
    private final JTextComponent textComponent;

    /**
     * User interface related to the target text component.
     */
    private final EditorUI editorUI;

    /**
     * Fold hierarchy of the text component user interface.
     */
    private final FoldHierarchy foldHierarchy;

    /** 
     * Document related to the target text component.
     */
    private final BaseDocument doc;

    /**
     * Caret of the target text component.
     */
    private Caret caret;

    /**
     * Caret batch timer launched on receiving
     * annotation data structures (AnnotateLine).
     */
    private Timer caretTimer;

    /**
     * Controls annotation bar visibility.
     */
    private boolean annotated;

    /**
     * Maps document {@link javax.swing.text.Element}s (representing lines) to
     * {@link VcsAnnotation}. <code>null</code> means that
     * no data are available, yet. So alternative
     * {@link #elementAnnotationsSubstitute} text shoudl be used.
     *
     * @thread it is accesed from multiple threads all mutations
     * and iterations must be under elementAnnotations lock,
     */
    private Map<Element, VcsAnnotation> elementAnnotations;

    /**
     * Represents text that should be displayed in
     * visible bar with yet <code>null</code> elementAnnotations.
     */
    private String elementAnnotationsSubstitute;
    
    private Color backgroundColor = Color.WHITE;
    private Color foregroundColor = Color.BLACK;
    private Color selectedColor = Color.BLUE;

    /**
     * Most recent status message.
     */
    private String recentStatusMessage;
    
    /**
     * Revision associated with caret line.
     */
    private String recentRevision;

    /**
     * Request processor to create threads that may be cancelled.
     */
    RequestProcessor requestProcessor = null;
    
    /**
     * Latest annotation comment fetching task launched.
     */
    private RequestProcessor.Task latestAnnotationTask = null;

    /**
     * Rendering hints for annotations sidebar inherited from editor settings.
     */
    private final Map renderingHints;
    
    private VcsAnnotations vcsAnnotations;

    /**
     * Creates new instance initializing final fields.
     */
    public AnnotationBar(JTextComponent target) {
        this.textComponent = target;
        this.editorUI = Utilities.getEditorUI(target);
        this.foldHierarchy = FoldHierarchy.get(editorUI.getComponent());
        this.doc = editorUI.getDocument();
        if (textComponent instanceof JEditorPane) {
            String mimeType = org.netbeans.lib.editor.util.swing.DocumentUtilities.getMimeType(textComponent);
            FontColorSettings fcs = MimeLookup.getLookup(mimeType).lookup(FontColorSettings.class);
            renderingHints = (Map) fcs.getFontColors(FontColorNames.DEFAULT_COLORING).getAttribute(EditorStyleConstants.RenderingHints);
        } else {
            renderingHints = null;
        }
        setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
        elementAnnotationsSubstitute = "";                              //NOI18N
    }
    
    // public contract ~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    /**
     * Makes the bar visible and sensitive to
     * LogOutoutListener events that should deliver
     * actual content to be displayed.
     */
    public void annotate() {
        annotated = true;
        elementAnnotations = null;

        doc.addDocumentListener(this);
        textComponent.addComponentListener(this);
        editorUI.addPropertyChangeListener(this);

        revalidate();  // resize the component
    }

    public void setAnnotationMessage(String message) {
        elementAnnotationsSubstitute = message;
        revalidate();
    }
    
    /**
     * Result computed show it...
     * Takes AnnotateLines and shows them.
     */
    public void annotationLines(File file, VcsAnnotations vcsannotations) {
        this.vcsAnnotations = vcsannotations;
        VcsAnnotation [] annotateLines = vcsannotations.getAnnotations();
        List<VcsAnnotation> lines = new LinkedList<VcsAnnotation>(Arrays.asList(annotateLines));
        int lineCount = lines.size();
        /** 0 based line numbers => 1 based line numbers*/
        int ann2editorPermutation[] = new int[lineCount];
        for (int i = 0; i< lineCount; i++) {
            ann2editorPermutation[i] = i+1;
        }

        DiffProvider diff = (DiffProvider) Lookup.getDefault().lookup(DiffProvider.class);
        if (diff != null) {
            Reader r = new LinesReader(lines);
            Reader docReader = Utils.getDocumentReader(doc);
            try {

                Difference[] differences = diff.computeDiff(r, docReader);

                // customize annotation line numbers to match different reality
                // compule line permutation

                for (int i = 0; i < differences.length; i++) {
                    Difference d = differences[i];
                    if (d.getType() == Difference.ADD) continue;

                    int editorStart;
                    int firstShift = d.getFirstEnd() - d.getFirstStart() +1;
                    if (d.getType() == Difference.CHANGE) {
                        int firstLen = d.getFirstEnd() - d.getFirstStart();
                        int secondLen = d.getSecondEnd() - d.getSecondStart();
                        if (secondLen >= firstLen) continue; // ADD or pure CHANGE
                        editorStart = d.getSecondStart();
                        firstShift = firstLen - secondLen;
                    } else {  // DELETE
                        editorStart = d.getSecondStart() + 1;
                    }

                    for (int c = editorStart + firstShift -1; c<lineCount; c++) {
                        ann2editorPermutation[c] -= firstShift;
                    }
                }

                for (int i = differences.length -1; i >= 0; i--) {
                    Difference d = differences[i];
                    if (d.getType() == Difference.DELETE) continue;

                    int firstStart;
                    int firstShift = d.getSecondEnd() - d.getSecondStart() +1;
                    if (d.getType() == Difference.CHANGE) {
                        int firstLen = d.getFirstEnd() - d.getFirstStart();
                        int secondLen = d.getSecondEnd() - d.getSecondStart();
                        if (secondLen <= firstLen) continue; // REMOVE or pure CHANGE
                        firstShift = secondLen - firstLen;
                        firstStart = d.getFirstStart();
                    } else {
                        firstStart = d.getFirstStart() + 1;
                    }

                    for (int k = firstStart-1; k<lineCount; k++) {
                        ann2editorPermutation[k] += firstShift;
                    }
                }

            } catch (IOException e) {
                ErrorManager err = ErrorManager.getDefault();
                err.annotate(e, "Cannot compute local diff required for annotations, ignoring...");  // NOI18N
                err.notify(ErrorManager.INFORMATIONAL, e);
            }
        }

        try {
            doc.atomicLock();
            StyledDocument sd = (StyledDocument) doc;
            Iterator<VcsAnnotation> it = lines.iterator();
            elementAnnotations = Collections.synchronizedMap(new HashMap<Element, VcsAnnotation>(lines.size()));
            while (it.hasNext()) {
                VcsAnnotation line = it.next();
                int lineNum = ann2editorPermutation[line.getLineNumber() -1];
                try {
                    int lineOffset = NbDocument.findLineOffset(sd, lineNum -1);
                    Element element = sd.getParagraphElement(lineOffset);
                    elementAnnotations.put(element, line);
                } catch (IndexOutOfBoundsException ex) {
                    // TODO how could I get line behind document end?
                    // furtunately user does not spot it
                    ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
                }
            }
        } finally {
            doc.atomicUnlock();
        }

        // lazy listener registration
        caret = textComponent.getCaret();
        if (caret != null) {
            caret.addChangeListener(this);
        }
        textComponent.addPropertyChangeListener(this);
        this.caretTimer = new Timer(500, this);
        caretTimer.setRepeats(false);

        onCurrentLine();
        revalidate();
        repaint();
    }

    // implementation ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    /**
     * Registers "close" popup menu, tooltip manager
     * and repaint on documet change manager.
     */
    public void addNotify() {
        super.addNotify();


        this.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                maybeShowPopup(e);
            }

            public void mouseReleased(MouseEvent e) {
                maybeShowPopup(e);
            }

            private void maybeShowPopup(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    e.consume();
                    createPopup(getLineFromMouseEvent(e)).show(e.getComponent(),
                               e.getX(), e.getY());
                }
            }
        });

        // register with tooltip manager
        setToolTipText(""); // NOI18N

    }

    private JPopupMenu createPopup(int line) {
        final ResourceBundle loc = NbBundle.getBundle(AnnotationBar.class);
        final JPopupMenu popupMenu = new JPopupMenu();
        
        VcsAnnotation annotation = getAnnotateLine(line);
        if (annotation != null && vcsAnnotations != null) {
            Action [] actions = vcsAnnotations.getActions(annotation);
            for (Action action : actions) {
                popupMenu.add(Utils.toMenuItem(action));
            }
            popupMenu.addSeparator();
        }
        
        JMenuItem menu = new JMenuItem(loc.getString("CTL_MenuItem_CloseAnnotations"));
        menu.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                hideBar();
            }
        });
        popupMenu.add(menu);
        return popupMenu;
    }

    /**
     * Hides the annotation bar from user. 
     */
    void hideBar() {
        annotated = false;
        revalidate();
        release();
    }

    /**
     * Gets a request processor which is able to cancel tasks.
     */
    private RequestProcessor getRequestProcessor() {
        if (requestProcessor == null) {
            requestProcessor = new RequestProcessor("AnnotationBarRP", 1, true);  // NOI18N
        }
        
        return requestProcessor;
    }
    
    /**
     * Shows commit message in status bar and or revision change repaints side
     * bar (to highlight same revision). This process is started in a
     * seperate thread.
     */
    private void onCurrentLine() {
        if (latestAnnotationTask != null) {
            latestAnnotationTask.cancel();
        }
        
        if (isAnnotated()) {
            latestAnnotationTask = getRequestProcessor().post(this);
        }
    }

    // latestAnnotationTask business logic
    @Override
    public void run() {
        Caret carett = this.caret;
        if (carett == null || !isAnnotated()) {
            // closed in the meantime
            return;
        }
        // get resource bundle
        ResourceBundle loc = NbBundle.getBundle(AnnotationBar.class);
        // give status bar "wait" indication
        StatusBar statusBar = editorUI.getStatusBar();
        recentStatusMessage = loc.getString("CTL_StatusBar_WaitFetchAnnotation");
        statusBar.setText(StatusBar.CELL_MAIN, recentStatusMessage);
        
        // determine current line
        int line = -1;
        int offset = carett.getDot();
        try {
            line = LineDocumentUtils.getLineIndex(doc, offset);
        } catch (BadLocationException ex) {
            ErrorManager err = ErrorManager.getDefault();
            err.annotate(ex, "Can not get line for caret at offset " + offset); // NOI18N
            err.notify(ex);
            clearRecentFeedback();
            return;
        }

        // handle locally modified lines
        VcsAnnotation al = getAnnotateLine(line);
        if (al == null) {
            AnnotationMarkProvider amp = AnnotationMarkInstaller.getMarkProvider(textComponent);
            if (amp != null) {
                amp.setMarks(Collections.<AnnotationMark>emptyList());
            }
            clearRecentFeedback();
            if (recentRevision != null) {
                recentRevision = null;
                repaint();
            }
            return;
        }

        // handle unchanged lines
        String revision = al.getRevision();
        if (!revision.equals(recentRevision)) {
            recentRevision = revision;            
            repaint();

            AnnotationMarkProvider amp = AnnotationMarkInstaller.getMarkProvider(textComponent);
            if (amp != null) {
            
                List<AnnotationMark> marks = new ArrayList<AnnotationMark>(elementAnnotations.size());
                // I cannot affort to lock elementAnnotations for long time
                // it's accessed from editor thread too
                Iterator<Map.Entry<Element, VcsAnnotation>> it2;
                synchronized(elementAnnotations) {
                    it2 = new HashSet<Map.Entry<Element, VcsAnnotation>>(elementAnnotations.entrySet()).iterator();
                }
                while (it2.hasNext()) {
                    Map.Entry<Element, VcsAnnotation> next = it2.next();                        
                    VcsAnnotation annotateLine = next.getValue();
                    if (revision.equals(annotateLine.getRevision())) {
                        Element element = next.getKey();
                        if (!elementAnnotations.containsKey(element)) {
                            continue;
                        }
                        int elementOffset = element.getStartOffset();
                        int lineNumber = NbDocument.findLineNumber((StyledDocument)doc, elementOffset);
                        AnnotationMark mark = new AnnotationMark(lineNumber, revision);
                        marks.add(mark);
                    }

                    if (Thread.interrupted()) {
                        clearRecentFeedback();
                        return;
                    }
                }
                amp.setMarks(marks);
            }
        }

        if (al.getDescription() != null) {
            recentStatusMessage = al.getDescription();
            statusBar.setText(StatusBar.CELL_MAIN, al.getAuthor() + ": " + recentStatusMessage); // NOI18N
        } else {
            clearRecentFeedback();
        };
    }
    
    /**
     * Clears the status bar if it contains the latest status message
     * displayed by this annotation bar.
     */
    private void clearRecentFeedback() {
        StatusBar statusBar = editorUI.getStatusBar();
        if (statusBar.getText(StatusBar.CELL_MAIN) == recentStatusMessage) {
            statusBar.setText(StatusBar.CELL_MAIN, "");  // NOI18N
        }
    }

    /**
     * Components created by SibeBarFactory are positioned
     * using a Layout manager that determines componnet size
     * by retireving preferred size.
     *
     * <p>Once componnet needs resizing it simply calls
     * {@link #revalidate} that triggers new layouting
     * that consults prefered size.
     */
    public Dimension getPreferredSize() {
        Dimension dim = textComponent.getSize();
        dim.width = annotated ? getBarWidth() : 0;
        dim.height *=2;  // XXX
        return dim;
    }

    /**
     * Gets the preferred width of this component.
     *
     * @return the preferred width of this component
     */
    private int getBarWidth() {
        String longestString = "";  // NOI18N
        if (elementAnnotations == null) {
            longestString = elementAnnotationsSubstitute;
        } else {
            synchronized(elementAnnotations) {
                for (VcsAnnotation line : elementAnnotations.values()) {
                    String displayName = getDisplayName(line); // NOI18N
                    if (displayName.length() > longestString.length()) {
                        longestString = displayName;
                    }
                }
            }
        }
        char[] data = longestString.toCharArray();
        int w = getGraphics().getFontMetrics().charsWidth(data, 0,  data.length);
        return w + 4;
    }

    private String getDisplayName(VcsAnnotation line) {
        return line.getRevision() + "  " + line.getAuthor(); // NOI18N
    }

    /**
     * Pair method to {@link #annotate}. It releases
     * all resources.
     */
    private void release() {
        editorUI.removePropertyChangeListener(this);
        textComponent.removeComponentListener(this);
        textComponent.removePropertyChangeListener(this);
        doc.removeDocumentListener(this);
        if (caret != null) {
            caret.removeChangeListener(this);
        }
        if (caretTimer != null) {
            caretTimer.removeActionListener(this);
        }
        elementAnnotations = null;
        // cancel running annotation task if active
        if(latestAnnotationTask != null) {
            latestAnnotationTask.cancel();
        }
        AnnotationMarkProvider amp = AnnotationMarkInstaller.getMarkProvider(textComponent);
        if (amp != null) {
            amp.setMarks(Collections.<AnnotationMark>emptyList());
        }

        clearRecentFeedback();
        
    }

    /**
     * Paints one view that corresponds to a line (or
     * multiple lines if folding takes effect).
     */
    private void paintView(View view, Graphics g, int yBase) {
        JTextComponent component = editorUI.getComponent();
        if (component == null) return;
        BaseTextUI textUI = (BaseTextUI)component.getUI();

        Element rootElem = textUI.getRootView(component).getElement();
        int line = rootElem.getElementIndex(view.getStartOffset());

        String annotation = "";  // NOI18N
        VcsAnnotation al = null;
        if (elementAnnotations != null) {
            al = getAnnotateLine(line);
            if (al != null) {
                annotation = getDisplayName(al);  // NOI18N
            }
        } else {
            annotation = elementAnnotationsSubstitute;
        }

        if (al != null && al.getRevision().equals(recentRevision)) {
            g.setColor(selectedColor());
        } else {
            g.setColor(foregroundColor());
        }
        g.drawString(annotation, 2, yBase + editorUI.getLineAscent());
    }

    /**
     * Presents commit message as tooltips.
     */
    public String getToolTipText (MouseEvent e) {
        if (editorUI == null)
            return null;
        int line = getLineFromMouseEvent(e);

        StringBuffer annotation = new StringBuffer();
        if (elementAnnotations != null) {
            VcsAnnotation al = getAnnotateLine(line);

            if (al != null) {
                String escapedAuthor = NbBundle.getMessage(AnnotationBar.class, "TT_Annotation"); // NOI18N
                try {
                    escapedAuthor = XMLUtil.toElementContent(al.getAuthor());
                } catch (CharConversionException e1) {
                    ErrorManager err = ErrorManager.getDefault();
                    err.annotate(e1, "CVS.AB: can not HTML escape: " + al.getAuthor());  // NOI18N
                    err.notify(ErrorManager.INFORMATIONAL, e1);
                }

                // always return unique string to avoid tooltip sharing on mouse move over same revisions -->
                annotation.append("<html><!-- line=").append(line++).append(" -->").append(al.getRevision()).append(" - <b>").append(escapedAuthor).append("</b>"); // NOI18N
                if (al.getDate() != null) {
                    annotation.append(" ").append(DateFormat.getDateInstance().format(al.getDate())); // NOI18N                    
                }
                if (al.getDescription() != null) {
                    String escaped = null;
                    try {
                        escaped = XMLUtil.toElementContent(al.getDescription());
                    } catch (CharConversionException e1) {
                        ErrorManager err = ErrorManager.getDefault();
                        err.annotate(e1, "CVS.AB: can not HTML escape: " + al.getDescription()); // NOI18N
                        err.notify(ErrorManager.INFORMATIONAL, e1);
                    }
                    if (escaped != null) {
                        String lined = escaped.replaceAll(System.getProperty("line.separator"), "<br>");  // NOI18N
                        annotation.append("<p>").append(lined); // NOI18N
                    }
                }
            }
        } else {
            annotation.append(elementAnnotationsSubstitute);
        }

        return annotation.toString();
    }

    /**
     * Locates AnnotateLine associated with given line. The
     * line is translated to Element that is used as map lookup key.
     * The map is initially filled up with Elements sampled on
     * annotate() method.
     *
     * <p>Key trick is that Element's identity is maintained
     * until line removal (and is restored on undo).
     *
     * @param line
     * @return found AnnotateLine or <code>null</code>
     */
    private VcsAnnotation getAnnotateLine(int line) {
        StyledDocument sd = (StyledDocument) doc;
        int lineOffset = NbDocument.findLineOffset(sd, line);
        Element element = sd.getParagraphElement(lineOffset);
        VcsAnnotation al = elementAnnotations.get(element);

        if (al != null) {
            int startOffset = element.getStartOffset();
            int endOffset = element.getEndOffset();
            try {
                int len = endOffset - startOffset;
                String text = doc.getText(startOffset, len -1);
                String content = al.getDocumentText();
                if (text.equals(content)) {
                    return al;
                }
            } catch (BadLocationException e) {
                ErrorManager err = ErrorManager.getDefault();
                err.annotate(e, "CVS.AB: can not locate line annotation."); // NOI18N
                err.notify(ErrorManager.INFORMATIONAL, e);
            }
        }

        return null;
    }

    /**
     * GlyphGutter copy pasted bolerplate method.
     * It invokes {@link #paintView} that contains
     * actual business logic.
     */
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        Rectangle clip = g.getClipBounds();

        JTextComponent component = editorUI.getComponent();
        if (component == null) return;

        BaseTextUI textUI = (BaseTextUI)component.getUI();
        View rootView = Utilities.getDocumentView(component);
        if (rootView == null) return;

        g.setColor(backgroundColor());
        g.fillRect(clip.x, clip.y, clip.width, clip.height);

        if (renderingHints != null) {
            ((Graphics2D) g).addRenderingHints(renderingHints);
        }
        
        AbstractDocument doc = (AbstractDocument)component.getDocument();
        doc.readLock();
        try{
            foldHierarchy.lock();
            try{
                int startPos = textUI.getPosFromY(clip.y);
                int startViewIndex = rootView.getViewIndex(startPos,Position.Bias.Forward);
                int rootViewCount = rootView.getViewCount();

                if (startViewIndex >= 0 && startViewIndex < rootViewCount) {
                    int clipEndY = clip.y + clip.height;
                    for (int i = startViewIndex; i < rootViewCount; i++){
                        View view = rootView.getView(i);
                        Rectangle rec = component.modelToView(view.getStartOffset());
                        if (rec == null) {
                            break;
                        }
                        int y = rec.y;
                        paintView(view, g, y);
                        if (y >= clipEndY) {
                            break;
                        }
                    }
                }

            } finally {
                foldHierarchy.unlock();
            }
        } catch (BadLocationException ble){
            ErrorManager.getDefault().notify(ble);
        } finally {
            doc.readUnlock();
        }
    }

    private Color backgroundColor() {
        if (textComponent != null) {
            return textComponent.getBackground();
        }
        return backgroundColor;
    }

    private Color foregroundColor() {
        if (textComponent != null) {
            return textComponent.getForeground();
        }
        return foregroundColor;
    }

    private Color selectedColor() {
        if (backgroundColor == backgroundColor()) {
            return selectedColor;
        }
        if (textComponent != null) {
            return textComponent.getCaretColor();
        }
        return selectedColor;

    }


    /** GlyphGutter copy pasted utility method. */
    private int getLineFromMouseEvent(MouseEvent e){
        int line = -1;
        if (editorUI != null) {
            try {
                JTextComponent component = editorUI.getComponent();
                BaseTextUI textUI = (BaseTextUI)component.getUI();
                int clickOffset = textUI.viewToModel(component, new Point(0, e.getY()));
                line = LineDocumentUtils.getLineIndex(doc, clickOffset);
            } catch (BadLocationException ble) {
                // there's not such line, return -1
            }
        }
        return line;
    }

    /** Implementation */
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt == null) return;
        String id = evt.getPropertyName();
        if (evt.getSource() == textComponent) {
            if ("caret".equals(id)) {
                if (caret != null) {
                    caret.removeChangeListener(this);
                }
                caret = textComponent.getCaret();
                if (caret != null) {
                    caret.addChangeListener(this);
                }
            }
            return;
        }
        if (EditorUI.COMPONENT_PROPERTY.equals(id)) {  // NOI18N
            if (evt.getNewValue() == null){
                // component deinstalled, lets uninstall all isteners
                release();
            }
        }

    }

    /** Implementation */
    public void changedUpdate(DocumentEvent e) {
    }

    /** Implementation */
    public void insertUpdate(DocumentEvent e) {
        // handle new lines,  Enter hit at end of line changes
        // the line element instance
        // XXX Actually NB document implementation triggers this method two times
        //  - first time with one removed and two added lines
        //  - second time with two removed and two added lines
        if (elementAnnotations != null) {
            Element[] elements = e.getDocument().getRootElements();
            synchronized(elementAnnotations) { // atomic change
                for (int i = 0; i < elements.length; i++) {
                    Element element = elements[i];
                    DocumentEvent.ElementChange change = e.getChange(element);
                    if (change == null) continue;
                    Element[] removed = change.getChildrenRemoved();
                    Element[] added = change.getChildrenAdded();

                    if (removed.length == added.length) {
                        for (int c = 0; c<removed.length; c++) {
                            VcsAnnotation recent = elementAnnotations.get(removed[c]);
                            if (recent != null) {
                                elementAnnotations.remove(removed[c]);
                                elementAnnotations.put(added[c], recent);
                            }
                        }
                    } else if (removed.length == 1 && added.length > 0) {
                        Element key = removed[0];
                        VcsAnnotation recent = elementAnnotations.get(key);
                        if (recent != null) {
                            elementAnnotations.remove(key);
                            elementAnnotations.put(added[0], recent);
                        }
                    }
                }
            }
        }
        repaint();
    }

    /** Implementation */
    @Override
    public void removeUpdate(DocumentEvent e) {
        final int length = e.getDocument().getLength();
        Mutex.EVENT.readAccess(new Runnable() {
            @Override
            public void run () {
                if (length == 0) { // external reload
                    hideBar();
                }
                repaint();
            }
        });
    }

    /** Caret */
    public void stateChanged(ChangeEvent e) {
        assert e.getSource() == caret;
        caretTimer.restart();
    }

    /** Timer */
    public void actionPerformed(ActionEvent e) {
        assert e.getSource() == caretTimer;
        onCurrentLine();
    }

    /** on JTextPane */
    public void componentHidden(ComponentEvent e) {
    }

    /** on JTextPane */
    public void componentMoved(ComponentEvent e) {
    }

    /** on JTextPane */
    public void componentResized(ComponentEvent e) {
        revalidate();
    }

    /** on JTextPane */
    public void componentShown(ComponentEvent e) {
    }

    boolean isAnnotated() {
        return annotated;
    }
}
