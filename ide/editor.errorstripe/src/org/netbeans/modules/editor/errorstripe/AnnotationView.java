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

package org.netbeans.modules.editor.errorstripe;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.reflect.Field;
import java.util.Map;
import javax.accessibility.Accessible;
import javax.accessibility.AccessibleContext;
import javax.accessibility.AccessibleRole;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLayeredPane;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JToolTip;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;
import javax.swing.UIManager;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.plaf.TextUI;
import javax.swing.text.AbstractDocument;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import javax.swing.text.StyledDocument;
import javax.swing.text.View;
import org.netbeans.api.editor.document.LineDocumentUtils;
import org.netbeans.api.editor.fold.FoldHierarchy;
import org.netbeans.api.editor.fold.FoldHierarchyEvent;
import org.netbeans.api.editor.fold.FoldHierarchyListener;

import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.BaseTextUI;
import org.netbeans.lib.editor.util.StringEscapeUtils;
import org.netbeans.modules.editor.errorstripe.caret.CaretMark;
import org.netbeans.modules.editor.errorstripe.privatespi.Mark;
import org.netbeans.spi.editor.errorstripe.UpToDateStatus;
import org.openide.ErrorManager;
import org.netbeans.modules.editor.errorstripe.privatespi.Status;
import org.openide.text.NbDocument;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.WeakListeners;


/**
 *
 * @author Jan Lahoda
 */
public class AnnotationView extends JComponent implements FoldHierarchyListener, MouseListener, MouseMotionListener, DocumentListener, PropertyChangeListener, Accessible {
    
    /*package private*/ static final ErrorManager ERR = ErrorManager.getDefault().getInstance("org.netbeans.modules.editor.errorstripe.AnnotationView"); // NOI18N
    
    /*package private*/ static final ErrorManager TIMING_ERR = ErrorManager.getDefault().getInstance("org.netbeans.modules.editor.errorstripe.AnnotationView.timing"); // NOI18N
    
    private static final int STATUS_BOX_SIZE = 7;
    private static final int THICKNESS = STATUS_BOX_SIZE + 6;
    /*package private*/ static final int PIXELS_FOR_LINE = 3/*height / lines*/;
    /*package private*/ static final int LINE_SEPARATOR_SIZE = 1/*2*/;
    /*package private*/ static final int HEIGHT_OFFSET = 20;
    
    /*package private*/ static final int UPPER_HANDLE = 4;
    /*package private*/ static final int LOWER_HANDLE = 4;
    
    private BaseDocument doc;
    private final JTextComponent  pane;
    
    private static final Color STATUS_UP_PART_COLOR = Color.WHITE;
    private static final Color STATUS_DOWN_PART_COLOR = new Color(0xCDCABB);
    
    private static final int QUIET_TIME = 100;

    private static final RequestProcessor WORKER = new RequestProcessor(AnnotationView.class.getName(), 1, false, false); //NOI18N
    private final RequestProcessor.Task repaintTask;
    private final RepaintTask           repaintTaskRunnable;
    private final Insets scrollBar;
    private final AnnotationViewData data;
    
    private static final Icon busyIcon;
    
    private DocumentListener weakDocL;

    static {
        busyIcon = new ImageIcon(AnnotationView.class.getResource("resources/hodiny.gif"));
    }
    
//    public AnnotationView(JTextComponent pane) {
//        this(pane, null);
//    }
    
    /** Creates a new instance of AnnotationViewBorder */
    public AnnotationView(JTextComponent pane/*, List/ *<MarkProviderCreator>* / creators*/) {
        this.pane = pane;
        // Set the name to be able to check for this component when "errorStripeOnly" property
        // is turned on for the pane in CustomizableSideBar.
        setName("errorStripe");
        
        repaintTask = WORKER.create(repaintTaskRunnable = new RepaintTask());
        this.data = new AnnotationViewDataImpl(this, pane);
        this.scrollBar = UIManager.getInsets("Nb.Editor.ErrorStripe.ScrollBar.Insets"); // NOI18N

        FoldHierarchy fh = FoldHierarchy.get(pane);
        fh.addFoldHierarchyListener(WeakListeners.create(FoldHierarchyListener.class, this, fh));
        pane.addPropertyChangeListener(WeakListeners.propertyChange(this, pane));

        updateForNewDocument();
        
        addMouseListener(this);
        addMouseMotionListener(this);
        
        setOpaque(true);
        
        setToolTipText(NbBundle.getMessage(AnnotationView.class,"TP_ErrorStripe"));
    }
    
    /*package private for tests*/AnnotationViewData getData() {
        return data;
    }
    
    private synchronized void updateForNewDocument() {
        data.unregister();
        Document newDocument = pane.getDocument();
        
        if (weakDocL != null && this.doc != null) {
            this.doc.removeDocumentListener(weakDocL);
            this.doc = null;
        }
        
        if (newDocument instanceof BaseDocument) {
            this.doc = (BaseDocument) pane.getDocument();
            weakDocL = WeakListeners.document(this, this.doc);
            this.doc.addDocumentListener(weakDocL);
        }
        
        data.register(this.doc);
    }
        
    /*package private for tests*/int[] getLinesSpan(int currentLine) {
        AbstractDocument adoc = doc;
        if (adoc != null)
            adoc.readLock();
        try {
            double componentHeight = getComponentHeight();
            double usableHeight = getUsableHeight();

            double position  = _modelToView(currentLine, componentHeight, usableHeight);

            if (position == (-1))
                return new int[] {currentLine, currentLine};

            int    startLine = currentLine;
            int    endLine   = currentLine;

            while (position == _modelToView(startLine - 1, componentHeight, usableHeight) && startLine > 0)
                startLine--;

            while ((endLine + 1) < LineDocumentUtils.getLineCount(doc) && position == _modelToView(endLine + 1, componentHeight, usableHeight))
                endLine++;

            return new int[] {startLine, endLine};
        } finally {
            if (adoc != null)
                adoc.readUnlock();
        }
    }
    
    private void drawOneColorGlobalStatus(Graphics g, Color color) {
        g.setColor(color);
        
        int x = (THICKNESS - STATUS_BOX_SIZE) / 2;
        int y = (topOffset() - STATUS_BOX_SIZE) / 2;
        
        g.fillRect(x, y, STATUS_BOX_SIZE, STATUS_BOX_SIZE);
        
        g.setColor(STATUS_DOWN_PART_COLOR);
        
        g.drawLine(x - 1, y - 1, x + STATUS_BOX_SIZE, y - 1              );
        g.drawLine(x - 1, y - 1, x - 1,               y + STATUS_BOX_SIZE);
        
        g.setColor(STATUS_UP_PART_COLOR);
        
        g.drawLine(x - 1,               y + STATUS_BOX_SIZE, x + STATUS_BOX_SIZE, y + STATUS_BOX_SIZE);
        g.drawLine(x + STATUS_BOX_SIZE, y - 1,               x + STATUS_BOX_SIZE, y + STATUS_BOX_SIZE);
    }
    
    private void drawInProgressGlobalStatus(Graphics g, Color color) {
        int x = (THICKNESS - STATUS_BOX_SIZE) / 2;
        int y = (topOffset() - STATUS_BOX_SIZE) / 2;
	
        busyIcon.paintIcon(this, g, x, y); // NOI18N
	
        g.setColor(STATUS_DOWN_PART_COLOR);
        
        g.drawLine(x - 1, y - 1, x + STATUS_BOX_SIZE, y - 1              );
        g.drawLine(x - 1, y - 1, x - 1,               y + STATUS_BOX_SIZE);
        
        g.setColor(STATUS_UP_PART_COLOR);
        
        g.drawLine(x - 1,               y + STATUS_BOX_SIZE, x + STATUS_BOX_SIZE, y + STATUS_BOX_SIZE);
        g.drawLine(x + STATUS_BOX_SIZE, y - 1,               x + STATUS_BOX_SIZE, y + STATUS_BOX_SIZE);
	
    }
    
    private static final Color GLOBAL_RED = new Color(0xFF2A1C);
    private static final Color GLOBAL_YELLOW = new Color(0xE1AA00);
    private static final Color GLOBAL_GREEN = new Color(0x65B56B);
    
    private Color getColorForGlobalStatus(Status status) {
        if (Status.STATUS_ERROR == status)
            return GLOBAL_RED;
        
        if (Status.STATUS_WARNING == status)
            return GLOBAL_YELLOW;
        
        return GLOBAL_GREEN;
    }
    
    private void drawGlobalStatus(Graphics g) {
        UpToDateStatus type = data.computeTotalStatusType();
        Color resultingColor;
        
        if (type == UpToDateStatus.UP_TO_DATE_DIRTY) {
                drawOneColorGlobalStatus(g, UIManager.getColor("Panel.background")); // NOI18N
        } else {
            if (type == UpToDateStatus.UP_TO_DATE_PROCESSING) {
//                Status totalStatus = data.computeTotalStatus();
//                
                drawInProgressGlobalStatus(g, null/*Status.getDefaultColor(totalStatus)*/);
            } else {
                if (type == UpToDateStatus.UP_TO_DATE_OK) {
                    Status totalStatus = data.computeTotalStatus();
                    
                    drawOneColorGlobalStatus(g, getColorForGlobalStatus(totalStatus));
                } else {
                    throw new IllegalStateException("Unknown up-to-date type: " + type); // NOI18N
                }
            }
        }
    }
    
    private int getCurrentLine() {
        Document doc = pane.getDocument();
        int line = -1;
        
        if (doc instanceof StyledDocument && pane.getCaret() != null) {
            int offset = pane.getCaretPosition(); //TODO: AWT?
            line = NbDocument.findLineNumber((StyledDocument) doc, offset);
        }
        
        return line;
    }
    
    private static Field currWriterField;
    private static boolean isWriteLocked(AbstractDocument doc) {
        if (currWriterField == null) {
            Field f = null;
            try {
                f = AbstractDocument.class.getDeclaredField("currWriter"); // NOI18N
            } catch (NoSuchFieldException ex) {
                throw new IllegalStateException(ex);
            }
            f.setAccessible(true);
            synchronized (doc) {
                currWriterField = f;
            }
        }
        try {
            synchronized (doc) {
                return currWriterField.get(doc) != null;
            }
        } catch (IllegalAccessException ex) {
            throw new IllegalStateException(ex);
        }
    }

    
    @Override
    public void paintComponent(Graphics g) {
        if (isWriteLocked(doc)) {
            // Try a little bit later ;-)
            repaint(100);
            return;
        }
//        Thread.dumpStack();
        long startTime = System.currentTimeMillis();
        super.paintComponent(g);
        
        Color oldColor = g.getColor();

        Color backColor = UIManager.getColor("NbEditorGlyphGutter.background"); //NOI18N
        if( null == backColor )
            backColor = UIManager.getColor("Panel.background"); // NOI18N
        g.setColor(backColor);
        
        g.fillRect(0, 0, getWidth(), getHeight());
        
//        SortedMap marks = getMarkMap();
        int currentline = getCurrentLine();
        int annotatedLine = data.findNextUsedLine(-1);

        AbstractDocument adoc = doc;
        // Try once again for the case if it was locked during the painting:
        if (isWriteLocked(doc)) {
            // Try a little bit later ;-)
            repaint(100);
            return;
        }

        if (adoc != null) {
            adoc.readLock();
        }
        try {
            while (annotatedLine != Integer.MAX_VALUE) {
//            System.err.println("annotatedLine = " + annotatedLine );
                int[] lineSpan = getLinesSpan(annotatedLine);
                int startLine = lineSpan[0];
                int endLine = lineSpan[1];

                Mark m = data.getMainMarkForBlock(startLine, endLine);

                if (m != null) {
                    Status s = m.getStatus();
                    double start = modelToView(annotatedLine);

                    if (s != null) {
//                    System.err.println("m = " + m );
                        Color color = m.getEnhancedColor();

                        if (color == null) {
                            color = Status.getDefaultColor(s);
                        }

                        assert color != null;

                        g.setColor(color);


                        //g.fillRect(1, (int) start, THICKNESS - 2, PIXELS_FOR_LINE);                            
                        //* 3D Version
                        if (m.getType() != Mark.TYPE_CARET) {
                            g.fillRect(1, (int) start, THICKNESS - 2, PIXELS_FOR_LINE);
                            //g.draw3DRect(1, (int) start, THICKNESS - 3, PIXELS_FOR_LINE - 1, true);
                        }
                        //*/                       
                        if ((startLine <= currentline && currentline <= endLine) || m.getType() == Mark.TYPE_CARET) {
                            drawCurrentLineMark(g, (int) start);
                        }
                    }
                }

                annotatedLine = data.findNextUsedLine(endLine);
            }

            drawGlobalStatus(g);
        } finally {
            if (adoc != null)
                adoc.readUnlock();
        }
        
        g.setColor(oldColor);
        
        long end = System.currentTimeMillis();
        
        if (TIMING_ERR.isLoggable(ErrorManager.INFORMATIONAL)) {
            TIMING_ERR.log("AnnotationView.paintComponent consumed: " + (end - startTime));
        }
    }

    private void drawCurrentLineMark(Graphics g, int start) {
        g.setColor( CaretMark.getCaretMarkColor());
        g.drawLine(2, start + PIXELS_FOR_LINE / 2, THICKNESS - 3, start + PIXELS_FOR_LINE / 2 );        
        g.fillRect( THICKNESS / 2 - PIXELS_FOR_LINE / 2, start, PIXELS_FOR_LINE, PIXELS_FOR_LINE );
        g.draw3DRect( THICKNESS / 2 - PIXELS_FOR_LINE / 2, start, PIXELS_FOR_LINE - 1, PIXELS_FOR_LINE - 1, true );
        
    }
    
    /*private*/ void fullRepaint() {
        fullRepaint(false);
    }
    
    /*private*/ void fullRepaint(final boolean clearMarksCache) {
        fullRepaint(clearMarksCache, false);
    }
    
    /*private*/ void fullRepaint(final boolean clearMarksCache, final boolean clearModelToViewCache) {
        synchronized (repaintTaskRunnable) {
            repaintTaskRunnable.setClearMarksCache(clearMarksCache);
            repaintTaskRunnable.setClearModelToViewCache(clearModelToViewCache);
            repaintTask.schedule(QUIET_TIME);
        }
    }
    
    private class RepaintTask implements Runnable {
        private boolean clearMarksCache;
        private boolean clearModelToViewCache;

        public void setClearMarksCache(boolean clearMarksCache) {
            this.clearMarksCache |= clearMarksCache;
        }
        
        public void setClearModelToViewCache(boolean clearModelToViewCache) {
            this.clearModelToViewCache |= clearModelToViewCache;
        }
        
        private synchronized boolean readAndDestroyClearMarksCache() {
            boolean result = clearMarksCache;
            
            clearMarksCache = false;
            
            return result;
        }

        private synchronized boolean readAndDestroyClearModelToViewCache() {
            boolean result = clearModelToViewCache;
            
            clearModelToViewCache = false;
            
            return result;
        }
        
        @Override
        public void run() {
            final boolean clearMarksCache = readAndDestroyClearMarksCache();
            final boolean clearModelToViewCache= readAndDestroyClearModelToViewCache();
            
            //Fix for #54193:
            SwingUtilities.invokeLater(() -> {
                synchronized (AnnotationView.this) {
                    if (clearMarksCache) {
                        data.clear();
                    }
                    if (clearModelToViewCache) {
                        modelToViewCache = null;
                    }
                }
                invalidate();
                repaint();
            });
        }
    }
    
    private void documentChange() {
        fullRepaint(lines != LineDocumentUtils.getLineCount(doc));
    }
    
    private double getComponentHeight() {
        final double[] ret = new double[1];
        pane.getDocument().render(() -> {
            ret[0] = pane.getUI().getRootView(pane).getPreferredSpan(View.Y_AXIS);
        });
        return ret[0];
    }
    
    double getUsableHeight() {
        //fix for issue #54080:
        //find the scrollpane which contains the pane:
        Component scrollPaneCandidade = pane.getParent();
        
        if (scrollPaneCandidade instanceof JLayeredPane) {
            scrollPaneCandidade = scrollPaneCandidade.getParent();
        }
        
        if (scrollPaneCandidade != null && !(scrollPaneCandidade instanceof JScrollPane)) {
            scrollPaneCandidade = scrollPaneCandidade.getParent();
        }
        
        if (!(scrollPaneCandidade instanceof JScrollPane) || scrollBar == null) {
            //no help for #54080:
            return getHeight() - HEIGHT_OFFSET;
        }
        
        JScrollPane scrollPane = (JScrollPane) scrollPaneCandidade;
        int visibleHeight = scrollPane.getViewport().getExtentSize().height;
        
        int topButton = topOffset();
        int bottomButton = scrollBar.bottom;
        
        return visibleHeight - topButton - bottomButton;
    }

    int topOffset() {
        if (scrollBar == null) {
            //no help for #54080:
            return HEIGHT_OFFSET;
        }
        
        return (HEIGHT_OFFSET > scrollBar.top ? HEIGHT_OFFSET : scrollBar.top) + PIXELS_FOR_LINE;
    }
    
    private int[] modelToViewCache = null;
    private int lines = -1;
    private int height = -1;
    
    private int getYFromPos(int offset) throws BadLocationException {
        TextUI ui = pane.getUI();
        int result;
        
        // For some reason the offset may become -1; uncomment following line to see that
        offset = Math.max(offset, 0);
        if (ui instanceof BaseTextUI baseTextUI) {
            result = baseTextUI.getYFromPos(offset);
        } else {
            Rectangle r = pane.modelToView(offset);
            
            result = r != null ? r.y : 0;
        }
        
        if (result == 0) {
            return -1;
        } else {
            return result;
        }
    }
    
    private synchronized int getModelToViewImpl(int line) throws BadLocationException {
        int docLines = LineDocumentUtils.getLineCount(doc);
        
        if (modelToViewCache == null || height != pane.getHeight() || lines != docLines) {
            modelToViewCache = new int[LineDocumentUtils.getLineCount(doc) + 2];
            lines = LineDocumentUtils.getLineCount(doc);
            height = pane.getHeight();
        }
        
        if (line >= docLines)
            return -1;
        
        int result = modelToViewCache[line + 1];
        
        if (result == 0) {
            int lineOffset = LineDocumentUtils.getLineStartFromIndex((BaseDocument) pane.getDocument(), line);
            
            modelToViewCache[line + 1] = result = getYFromPos(lineOffset);
        }
        
        if (result == (-1))
            result = 0;
        
        return result;
    }
    
    /*package private*/ double modelToView(int line) {
        return _modelToView(line, getComponentHeight(), getUsableHeight());
    }

    private double _modelToView(int line, double componentHeight, double usableHeight) {
        try {
            int r = getModelToViewImpl(line);

            if (r == (-1))
                return -1.0;

            if (ERR.isLoggable(ErrorManager.INFORMATIONAL)) {
                ERR.log(ErrorManager.INFORMATIONAL, "AnnotationView.modelToView: line=" + line); // NOI18N
//                ERR.log(ErrorManager.INFORMATIONAL, "AnnotationView.modelToView: lineOffset=" + lineOffset); // NOI18N
                ERR.log(ErrorManager.INFORMATIONAL, "AnnotationView.modelToView: r=" + r); // NOI18N
                ERR.log(ErrorManager.INFORMATIONAL, "AnnotationView.modelToView: getComponentHeight()=" + getComponentHeight()); // NOI18N
                ERR.log(ErrorManager.INFORMATIONAL, "AnnotationView.modelToView: getUsableHeight()=" + getUsableHeight()); // NOI18N
            }

            if (componentHeight <= usableHeight) {
                //1:1 mapping:
                return r + topOffset();
            } else {
                double position = r / componentHeight;
                int    blocksCount = (int) (usableHeight / (PIXELS_FOR_LINE + LINE_SEPARATOR_SIZE));
                int    block = (int) (position * blocksCount);

                return block * (PIXELS_FOR_LINE + LINE_SEPARATOR_SIZE) + topOffset();
            }
        } catch (BadLocationException e) {
            ErrorManager.getDefault().notify(e);
            return -1.0;
        }
    }
    
    private static final int VIEW_TO_MODEL_IMPORTANCE = ErrorManager.INFORMATIONAL;
    
    /*package private*/ int[] viewToModel(double offset) {
        try {
            if (getComponentHeight() <= getUsableHeight()) {
                //1:1 mapping:
                int positionOffset = pane.viewToModel(new Point(1, (int) (offset - topOffset())));
                if (positionOffset == -1) {
                    return null;
                }
                int line = LineDocumentUtils.getLineIndex(doc, positionOffset);
                
                if (ERR.isLoggable(VIEW_TO_MODEL_IMPORTANCE)) {
                    ERR.log(VIEW_TO_MODEL_IMPORTANCE, "AnnotationView.viewToModel: line=" + line); // NOI18N
                }
                
                double position = modelToView(line);
                
                if (offset < position || offset >= (position + PIXELS_FOR_LINE))
                    return null;
                
                return getLinesSpan(line);
            } else {
                int    blocksCount = (int) (getUsableHeight() / (PIXELS_FOR_LINE + LINE_SEPARATOR_SIZE));
                int    block = (int) ((offset - topOffset()) / (PIXELS_FOR_LINE + LINE_SEPARATOR_SIZE));
                double yPos = (getComponentHeight() * block) / blocksCount;
                
                if (yPos == (int) yPos)
                    yPos -= 1;
                
                int    positionOffset = pane.viewToModel(new Point(0, (int) yPos));
                if (positionOffset == -1) {
                    return null;
                }
                int    line = LineDocumentUtils.getLineIndex(doc, positionOffset) + 1;
                int[] span = getLinesSpan(line);
                double normalizedOffset = modelToView(span[0]);
                
                if (ERR.isLoggable(VIEW_TO_MODEL_IMPORTANCE)) {
                    ERR.log(VIEW_TO_MODEL_IMPORTANCE, "AnnotationView.viewToModel: offset=" + offset); // NOI18N
                    ERR.log(VIEW_TO_MODEL_IMPORTANCE, "AnnotationView.viewToModel: block=" + block); // NOI18N
                    ERR.log(VIEW_TO_MODEL_IMPORTANCE, "AnnotationView.viewToModel: blocksCount=" + blocksCount); // NOI18N
                    ERR.log(VIEW_TO_MODEL_IMPORTANCE, "AnnotationView.viewToModel: pane.getHeight()=" + pane.getHeight()); // NOI18N
                    ERR.log(VIEW_TO_MODEL_IMPORTANCE, "AnnotationView.viewToModel: yPos=" + yPos); // NOI18N
                    ERR.log(VIEW_TO_MODEL_IMPORTANCE, "AnnotationView.viewToModel: positionOffset=" + positionOffset); // NOI18N
                    ERR.log(VIEW_TO_MODEL_IMPORTANCE, "AnnotationView.viewToModel: line=" + line); // NOI18N
                    ERR.log(VIEW_TO_MODEL_IMPORTANCE, "AnnotationView.viewToModel: normalizedOffset=" + normalizedOffset); // NOI18N
                }
                
                if (offset < normalizedOffset || offset >= (normalizedOffset + PIXELS_FOR_LINE)) {
                    return null;
                }
                
                if (block < 0)
                    return null;
                
                return span;
            }
        } catch (BadLocationException e) {
            ErrorManager.getDefault().notify(e);
            return null;
        }
    }
    
    private Mark getMarkForPointImpl(double point) {
        int[] lineSpan   = viewToModel(point);
        
        if (lineSpan == null)
            return null;
        
        int   startLine  = lineSpan[0];
        int   endLine    = lineSpan[1];
        
        if (startLine != (-1)) {
            return data.getMainMarkForBlock(startLine, endLine);
        }
        
        return null;
    }

    /*package private*/ Mark getMarkForPoint(double point) {
        //Normalize the point:
        point = ((int) (point / (PIXELS_FOR_LINE + LINE_SEPARATOR_SIZE))) * (PIXELS_FOR_LINE + LINE_SEPARATOR_SIZE);
        
        Mark a = getMarkForPointImpl(point);
        
        if (ERR.isLoggable(VIEW_TO_MODEL_IMPORTANCE)) {
            ERR.log(VIEW_TO_MODEL_IMPORTANCE, "AnnotationView.getAnnotationForPoint: point=" + point); // NOI18N
            ERR.log(VIEW_TO_MODEL_IMPORTANCE, "AnnotationView.getAnnotationForPoint: a=" + a); // NOI18N
        }
        
        int relativeMax = Math.max(UPPER_HANDLE + 1, LOWER_HANDLE + 1);
        
        for (short relative = 1; relative < relativeMax && a == null; relative++) {
            if (relative <= UPPER_HANDLE) {
                a = getMarkForPointImpl(point + relative);
                
                if (ERR.isLoggable(VIEW_TO_MODEL_IMPORTANCE)) {
                    ERR.log(VIEW_TO_MODEL_IMPORTANCE, "AnnotationView.getAnnotationForPoint: a=" + a); // NOI18N
                    ERR.log(VIEW_TO_MODEL_IMPORTANCE, "AnnotationView.getAnnotationForPoint: relative=" + relative); // NOI18N
                }
            }
            
            if (relative <= LOWER_HANDLE && a == null) {
                a = getMarkForPointImpl(point - relative);
                
                if (ERR.isLoggable(VIEW_TO_MODEL_IMPORTANCE)) {
                    ERR.log(VIEW_TO_MODEL_IMPORTANCE, "AnnotationView.getAnnotationForPoint: a=" + a); // NOI18N
                    ERR.log(VIEW_TO_MODEL_IMPORTANCE, "AnnotationView.getAnnotationForPoint: relative=-" + relative); // NOI18N
                }
            }
        }
        
        return a;
    }
    
    @Override
    public Dimension getMaximumSize() {
        return new Dimension(THICKNESS, Integer.MAX_VALUE);
    }

    @Override
    public Dimension getMinimumSize() {
        return new Dimension(THICKNESS, Integer.MIN_VALUE);
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(THICKNESS, Integer.MIN_VALUE);
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        //NOTHING:
        resetCursor();
    }

    @Override
    public void mousePressed(MouseEvent e) {
        resetCursor();
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        checkCursor(e);
    }

    private int initialToolTipDelay;
    private int dismissToolTipDelay;

    @Override
    public void mouseExited(MouseEvent e) {
        ToolTipManager ttm = ToolTipManager.sharedInstance();
        ttm.setInitialDelay(initialToolTipDelay);
        ttm.setDismissDelay(dismissToolTipDelay);
        resetCursor();
    }

    @Override
    public void mouseEntered(MouseEvent e) {
        ToolTipManager ttm = ToolTipManager.sharedInstance();
        initialToolTipDelay = ttm.getInitialDelay();
        dismissToolTipDelay = ttm.getDismissDelay();
        ttm.setInitialDelay(200);
        ttm.setDismissDelay(60_000);
        checkCursor(e);
    }

    @Override
    public void mouseDragged(MouseEvent e) {
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        resetCursor();
        
        Mark mark = getMarkForPoint(e.getPoint().getY());
        
        if (mark!= null) {
            pane.setCaretPosition(LineDocumentUtils.getLineStartFromIndex(doc, mark.getAssignedLines()[0]));
        }
    }
    
    private void resetCursor() {
        setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
    }
    
    private void checkCursor(MouseEvent e) {
        Mark mark = getMarkForPoint(e.getPoint().getY());
        
        if (mark == null) {
            resetCursor();
            return ;
        }
        
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    @Override
    public Point getToolTipLocation(MouseEvent event) {
        String text = getToolTipText(event);
        if (text == null) {
            return null;
        }
        // position left from scrollbar
        JToolTip tt = new JToolTip();
        tt.setTipText(text);
        int offset = (int) (tt.getPreferredSize().width + new JScrollBar().getPreferredSize().getWidth());
        return new Point(-offset, event.getY());
    }
    
    private String lastToolTipText;
    private MouseEvent lastMouseEvent;

    @Override
    public String getToolTipText(MouseEvent event) {
        if (event != lastMouseEvent) {
            lastMouseEvent = event;
            lastToolTipText = getToolTipTextImpl(event);
        }
        return lastToolTipText;
    }

    private String getToolTipTextImpl(MouseEvent event) {
        if (ERR.isLoggable(ErrorManager.INFORMATIONAL)) {
            ERR.log(ErrorManager.INFORMATIONAL, "getToolTipText: event=" + event); // NOI18N
        }
        int y = event.getY();
        
        // document status indicator
        if (y <= topOffset()) {
            AnnotationViewData.Stats stats = data.computeAnnotationStatistics();
            int errors = stats.errors();
            int warnings = stats.warnings();
            
            if (errors == 0 && warnings == 0) {
                return NbBundle.getMessage(AnnotationView.class, "TP_NoErrors"); // NOI18N
            }
            
            StringBuilder text = new StringBuilder();
            text.append("<html>");
            appendHistogram(text, stats.err_histogram(), "#000000", "#ff8888");
            appendHistogram(text, stats.war_histogram(), "#000000", "#ffff88");
            
            if (errors == 0 && warnings != 0) {
                text.append(NbBundle.getMessage(AnnotationView.class, "TP_X_warning(s)", warnings)); // NOI18N
            } else if (errors != 0 && warnings == 0) {
                text.append(NbBundle.getMessage(AnnotationView.class, "TP_X_error(s)", errors)); // NOI18N
            } else {
                text.append(NbBundle.getMessage(AnnotationView.class, "TP_X_error(s)_Y_warning(s)", errors, warnings)); // NOI18N
            }
            return text.toString();
        }

        // annotation bar
        Mark mark = getMarkForPoint(y);
        if (mark != null) {
            String description = mark.getShortDescription();
            if (description != null) {
                // #122422 - some descriptions are intentionaly a valid HTML and don't want to be escaped
                if (description.startsWith(HTML_PREFIX_LOWERCASE) || description.startsWith(HTML_PREFIX_UPPERCASE)) {
                    return description;
                } else {
                    return "<html><body>" + StringEscapeUtils.escapeHtml(description); // NOI18N
                }
            }
        }
        
        return null;
    }

    private static void appendHistogram(StringBuilder sb, Map<String, Integer> histogram, String fg, String bg) {
        if (histogram.isEmpty()) {
            return;
        }
        int n = 0;
        sb.append("<div style=\"background-color:").append(bg).append(";color:").append(fg).append("\">");
        for (Map.Entry<String, Integer> annotation : histogram.entrySet()) {
            sb.append(annotation.getValue()).append(" ")
              .append(StringEscapeUtils.escapeHtml(toShortLine(annotation.getKey()))).append("<br>");
            if (n++ > 20) {
                sb.append("(...)<br>");
                break;
            }
        }
        sb.append("</div>");
    }

    @SuppressWarnings("AssignmentToMethodParameter")
    private static String toShortLine(String desc) {
        int cut = desc.indexOf("\n");
        if (cut != -1) {
            desc = desc.substring(0, cut);
        }
        int max = 200;
        if (desc.length() > max) {
            desc = desc.substring(0, max) + "...";
        }
        return desc;
    }
    
    private static final String HTML_PREFIX_LOWERCASE = "<html"; //NOI18N
    private static final String HTML_PREFIX_UPPERCASE = "<HTML"; //NOI18N

    @Override
    public void foldHierarchyChanged(FoldHierarchyEvent evt) {
        //fix for #63402: clear the modelToViewCache after folds changed:
        //#64498: do not take monitor on this here:
        fullRepaint(false, true);
    }

    @Override
    public void removeUpdate(DocumentEvent e) {
        documentChange();
    }
    
    @Override
    public void insertUpdate(DocumentEvent e) {
        documentChange();
    }
    
    @Override
    public void changedUpdate(DocumentEvent e) {
        //ignored...
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getSource() == this.pane && "document".equals(evt.getPropertyName())) {
            updateForNewDocument();
            return ;
        }
        
        fullRepaint();
    }
    
    @Override
    public AccessibleContext getAccessibleContext() {
        if (accessibleContext == null) {
            accessibleContext = new AccessibleJComponent() {
                @Override
                public AccessibleRole getAccessibleRole() {
                    return AccessibleRole.PANEL;
                }
            };
            accessibleContext.setAccessibleName(NbBundle.getMessage(AnnotationView.class, "ACSN_AnnotationView")); //NOI18N
            accessibleContext.setAccessibleDescription(NbBundle.getMessage(AnnotationView.class, "ACSD_AnnotationView")); //NOI18N
        }
        return accessibleContext;
    }
}
