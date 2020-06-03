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
package org.netbeans.modules.cnd.diagnostics.clank.ui.views;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.KeyboardFocusManager;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;
import org.clang.tools.services.ClankDiagnosticInfo;
import org.netbeans.api.editor.EditorRegistry;
import org.netbeans.api.editor.StickyWindowSupport;
import org.netbeans.api.editor.document.LineDocumentUtils;
import org.netbeans.editor.EditorUI;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.services.CsmFileInfoQuery;
import org.netbeans.modules.cnd.diagnostics.clank.ui.Utilities;
import org.netbeans.modules.cnd.modelutil.CsmUtilities;
import org.netbeans.modules.cnd.utils.cache.CndFileUtils;
import org.openide.awt.StatusDisplayer;
import org.openide.cookies.EditorCookie;
import org.openide.cookies.LineCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileSystem;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.text.Annotation;
import org.openide.text.Line;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 *
 */
public class DiagnosticsAnnotationProvider {
    public static final String DIAGNOSTIC_CHANGED = "diagnostic_changed";//NOI18N
    private static final AtomicReference<ClankDiagnosticInfo> currentDiagnostic = new AtomicReference<>();
    private static final Map<ClankDiagnosticInfo, DiagnosticsAnnotation> diagnosticToAnnotations = new IdentityHashMap<>();
    private static final Map<ClankDiagnosticInfo, StickyPanel> diagnosticToWindow = new IdentityHashMap<>();
    private static final Color CURRENT_PIN_BACKGROUND_COLOR = new Color(233, 239, 248);

    public static void pin(ClankDiagnosticInfo diagnosticInfo, final EditorPin pin, PropertyChangeListener l) {
        try {
            DataObject dobj = DataObject.find(pin.getFile());
            EditorCookie ec = dobj.getLookup().lookup(EditorCookie.class);
            JEditorPane[] openedPanes = ec.getOpenedPanes();
            if (openedPanes == null) {
                throw new IllegalArgumentException("No editor panes opened for file " + pin.getFile());//NOI18N
            }
            LineCookie lineCookie = dobj.getLookup().lookup(LineCookie.class);
            if (lineCookie == null) {
                throw new IllegalArgumentException("No line cookie in " + pin.getFile());//NOI18N
            }
            Line.Set ls = lineCookie.getLineSet();
            Line line;
            try {
                line = ls.getCurrent(pin.getLine());
            } catch (IndexOutOfBoundsException e) {
                throw new IllegalArgumentException("Wrong line: " + pin.getLine(), e);//NOI18N
            }
            for (JEditorPane pane : openedPanes) {
                JEditorPane ep = getMostRecentEditor();
                EditorUI editorUI = org.netbeans.editor.Utilities.getEditorUI(pane);
                pin(diagnosticInfo, pin, editorUI, line, l);
            }
        } catch (DataObjectNotFoundException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    public static void clearAll(){
        for (Annotation annotaion : diagnosticToAnnotations.values()) {
            annotaion.detach();
        }
        diagnosticToAnnotations.clear();
        Collection<StickyPanel> windows = diagnosticToWindow.values();
        for (StickyPanel stickyPanel : diagnosticToWindow.values()) {
                stickyPanel.close();
        }
        diagnosticToWindow.clear();
        currentDiagnostic.set(null);
    }
    
    private static JEditorPane getMostRecentEditor() {
        JTextComponent ctc = EditorRegistry.lastFocusedComponent();
        if (ctc instanceof JEditorPane) {
            return ((JEditorPane) ctc);
        } else {
            return null;
        }
    }
    
    public static void setCurrentDiagnostic(ClankDiagnosticInfo info) {
        currentDiagnostic.set(info);
        if (diagnosticToWindow.isEmpty()) {
            return;
        }
        StickyPanel panel = diagnosticToWindow.get(info);
        if (panel == null) {
            return;
        }
        for (StickyPanel value : diagnosticToWindow.values()) {
            value.setCurrent(panel == value);
        }
    }

    @SuppressWarnings("AssignmentToMethodParameter")
    private static void pin(ClankDiagnosticInfo diagnosticInfo, EditorPin pin, EditorUI eui, Line line, PropertyChangeListener l) throws IndexOutOfBoundsException {
//        Annotation ann = parentDiagnosticToAnnotations.remove(diagnosticInfo); // just to be sure
//        if (ann != null) {
//            ann.detach();
//        }
        //JComponent frame = parentDiagnosticToWindow.remove(diagnosticInfo);
        StickyWindowSupport stickyWindowSupport = eui.getStickyWindowSupport();
//        if (frame != null) {
//            frame.setVisible(false);
//            stickyWindowSupport.removeWindow(frame);
//        }

//        final EditorPin pin = (EditorPin) diagnosticInfo.getPin();
        if (pin == null) {
            return;
        }

        if (line == null) {
            line = getLine(pin.getFile(), pin.getLine());
        }
        //the severtry will be the same as parent if any
        String annotationType = diagnosticInfo.getSeverity() == ClankDiagnosticInfo.Severity.Error ? DiagnosticsAnnotation.DIAGNOSTIC_ERROR_ANNOTATION_TYPE: 
                DiagnosticsAnnotation.DIAGNOSTIC_WARNING_ANNOTATION_TYPE;
        if (diagnosticInfo.getParent() != null) {
            annotationType = diagnosticInfo.getParent().getSeverity() == ClankDiagnosticInfo.Severity.Error ? DiagnosticsAnnotation.DIAGNOSTIC_ERROR_ANNOTATION_TYPE: 
                DiagnosticsAnnotation.DIAGNOSTIC_WARNING_ANNOTATION_TYPE;
        }
        final DiagnosticsAnnotation annotation = new DiagnosticsAnnotation(annotationType, line);
        annotation.setDiagnostic(diagnosticInfo);
        //re-calcluate location
        ArrayList<DiagnosticsAnnotation> alreadyExistingAnnotations = new ArrayList<>();
        for (DiagnosticsAnnotation anno : diagnosticToAnnotations.values()) {
            if (anno.getLine().getLineNumber() == line.getLineNumber()){
                alreadyExistingAnnotations.add(anno);
            }
        }
        
        diagnosticToAnnotations.put(diagnosticInfo, annotation);
        annotation.attach(line);
        pin.addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if (EditorPin.PROP_LINE.equals(evt.getPropertyName())) {
                    annotation.detach();
                    Line line = getLine(pin.getFile(), pin.getLine());
                    annotation.attach(line);
                }
            }
        });

        StickyPanel window = new StickyPanel(diagnosticInfo, pin, eui, l);
        stickyWindowSupport.addWindow(window);
        //need to calculate correct location
        int newX = 0;
        int newY = 0;
        boolean recalculated = false;
        for (DiagnosticsAnnotation anno : alreadyExistingAnnotations) {
            ClankDiagnosticInfo info = anno.getLookup().lookup(ClankDiagnosticInfo.class);
            if (info == null) {
                continue;
            }
            StickyPanel wind = diagnosticToWindow.get(info);
            if (wind == null) {
                continue;
            }
            recalculated = true;
            newX += wind.getLocation().x + (int)wind.getPreferredSize().getWidth();
            newY = wind.getLocation().y ;//+ (int)wind.getPreferredSize().getHeight();
        }
        if (!recalculated) {
            window.setLocation(pin.getLocation());
        } else {
            window.setLocation(newX, newY);
        }
        
        diagnosticToWindow.put(diagnosticInfo, window);
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                Dimension size = window.getPreferredSize();
                Point loc = window.getLocation();
                window.setBounds(loc.x, loc.y, size.width, size.height);
                window.revalidate();
            }
        });
    }

    static private Line getLine(FileObject file, int lineNumber) {
        if (file == null) {
            return null;
        }
        DataObject dataObject;
        try {
            dataObject = DataObject.find(file);
        } catch (DataObjectNotFoundException ex) {
            return null;
        }
        if (dataObject == null) {
            return null;
        }
        LineCookie lineCookie = dataObject.getLookup().lookup(LineCookie.class);
        if (lineCookie == null) {
            return null;
        }
        Line.Set ls = lineCookie.getLineSet();
        if (ls == null) {
            return null;
        }
        try {
            return ls.getCurrent(lineNumber);
        } catch (IndexOutOfBoundsException | IllegalArgumentException e) {
        }
        return null;
    }
    
   
    public static void goTo(final ClankDiagnosticInfo diagnosticInfo, FileSystem fSystem, PropertyChangeListener listener) {
        final FileObject fo = CndFileUtils.toFileObject(fSystem, diagnosticInfo.getSourceFileName());
        CsmFile csmNoteFile = CsmUtilities.getCsmFile(fo, false, false);
        final int[] lineColumnByOffset = CsmFileInfoQuery.getDefault().getLineColumnByOffset(csmNoteFile, diagnosticInfo.getStartOffsets()[0]);
        StatusDisplayer.getDefault().setStatusText(NbBundle.getMessage(DiagnosticsAnnotationProvider.class, "OpeningFile"));//NOI18N
        setCurrentDiagnostic(diagnosticInfo);
        RequestProcessor.getDefault().post(new Runnable() {
            @Override
            public void run() {
                if (fo == null) {
                    StatusDisplayer.getDefault().setStatusText(NbBundle.getMessage(DiagnosticsAnnotationProvider.class, "CannotOpen", diagnosticInfo.getSourceFileName()));//NOI18N
                } else {
                    Utilities.show(fo, lineColumnByOffset[0]);
                    if (listener != null) {
                        listener.propertyChange(new PropertyChangeEvent(this, DIAGNOSTIC_CHANGED, null, diagnosticInfo));
                    }
                }
            }
        });

    }    
    
    public static void next(FileSystem fSystem, PropertyChangeListener listener) {
        if (!isNextActionEnabled()) {
            return;
        }
        final  ClankDiagnosticInfo diagnosticInfo = currentDiagnostic.get();
        final ArrayList<ClankDiagnosticInfo> notes = diagnosticInfo.getParent() == null ? diagnosticInfo.notes() : diagnosticInfo.getParent().notes();
        int currentDiagnosticIndex = diagnosticInfo.getParent() == null ? - 1 : notes.indexOf(diagnosticInfo);
        currentDiagnosticIndex++;
        goTo(notes.get(currentDiagnosticIndex), fSystem, listener);
    }
    
    public static void prev(FileSystem fSystem, PropertyChangeListener listener) {
        if (!isPrevActionEnabled()) {
            return;
        }
        final  ClankDiagnosticInfo diagnosticInfo = currentDiagnostic.get();
        final ArrayList<ClankDiagnosticInfo> notes = diagnosticInfo.getParent() == null ? diagnosticInfo.notes() : diagnosticInfo.getParent().notes();
        int currentDiagnosticIndex = diagnosticInfo.getParent() == null ? - 1 : notes.indexOf(diagnosticInfo);
        currentDiagnosticIndex--;
        if (currentDiagnosticIndex == -1) {
            goTo(diagnosticInfo.getParent(), fSystem, listener);
        } else {
            goTo(notes.get(currentDiagnosticIndex), fSystem, listener);
        }
    }   
    
    public static boolean isNextActionEnabled() {
        final  ClankDiagnosticInfo diagnosticInfo = currentDiagnostic.get();
        if (diagnosticInfo == null) {
            return false;
        }
        final ArrayList<ClankDiagnosticInfo> notes = diagnosticInfo.getParent() == null ? diagnosticInfo.notes() : diagnosticInfo.getParent().notes();
        final int currentDiagnosticIndex = diagnosticInfo.getParent() == null ? - 1 : notes.indexOf(diagnosticInfo);
        return currentDiagnosticIndex < notes.size() -1 ;
    }

    public static boolean isPrevActionEnabled() {
        final  ClankDiagnosticInfo diagnosticInfo = currentDiagnostic.get();
        if (diagnosticInfo == null) {
            return false;
        }        
        final ArrayList<ClankDiagnosticInfo> notes = diagnosticInfo.getParent() == null ? diagnosticInfo.notes() : diagnosticInfo.getParent().notes();
        final int currentDiagnosticIndex = diagnosticInfo.getParent() == null ? - 1 : notes.indexOf(diagnosticInfo);
        return currentDiagnosticIndex > -1 ;        
    }

    private static JTextComponent createNonEditableSelectableLabel(ClankDiagnosticInfo  info) {
        JTextPane tf = new JTextPane() {
            @Override
            public void setText(String text) {
                String noWrapText = "<html><div style='white-space:nowrap'>" + text + "</div></html>";  // NOI18N
                super.setText(noWrapText);
            }
        };
        tf.setContentType("text/html");                                         // NOI18N
        tf.setEditable(false);
        tf.setBorder(null);
        tf.setForeground(UIManager.getColor("Label.foreground"));               // NOI18N
        tf.setBackground(info == currentDiagnostic.get() ? CURRENT_PIN_BACKGROUND_COLOR : UIManager.getColor("Label.background"));//NOI18N
        tf.setFont(UIManager.getFont("Label.font"));                            // NOI18N
        tf.setText(info.getMessage());
        tf.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);
        return tf;
    }

    private static final class StickyPanel extends JPanel {
//        @StaticResource
//        private static final String ICON_COMMENT = "org/netbeans/modules/debugger/resources/actions/Comment.png";   // NOI18N

        private static final String UI_PREFIX = "ToolTip"; // NOI18N
        private final ClankDiagnosticInfo diagnosticInfo;
        private final EditorUI eui;
        private final JTextComponent textComponent;
        //private final JTextField valueField;
        private final JToolBar headActions;
        private final JToolBar tailActions;
        private JTextField commentField;
//        private final ValueProvider valueProvider;
        private int minPreferredHeight = 0;
        private final EditorPin pin;
        private final PropertyChangeListener listener;
        private final Action nextAction;
        private final Action prevAction;
        
        @SuppressWarnings("OverridableMethodCallInConstructor")
        public StickyPanel(final ClankDiagnosticInfo diagnosticInfo, EditorPin pin, final EditorUI eui, PropertyChangeListener l) {
            this.diagnosticInfo = diagnosticInfo;
            this.listener = l;
            this.eui = eui;
            this.pin = pin;
            //EditorPin pin = (EditorPin) watch.getPin();
            Font font = UIManager.getFont(UI_PREFIX + ".font"); // NOI18N
            setOpaque(true);

            setBorder(BorderFactory.createLineBorder(getForeground()));

            setLayout(new GridBagLayout());
            GridBagConstraints gridConstraints = new GridBagConstraints();
            gridConstraints.gridx = 0;
            gridConstraints.gridy = 0;

            //valueProvider = PIN_SUPPORT_ACCESS.getValueProvider(pin);
            headActions = createActionsToolbar();
            add(headActions, gridConstraints);
            //Action[] actions = valueProvider.getHeadActions(watch);
            addActions(headActions, new Action[0]);
//            valueField = new JTextField();
//            valueField.setVisible(false);
//            valueField.setText(value);
            Dimension size = getPreferredSize();
            Point loc = getLocation();
            setBounds(loc.x, loc.y, size.width, size.height);
            //if (!isEvaluating[0]) {
//            adjustSize();
            // }            
//            valueProvider.setChangeListener(watch, new ValueChangeListener() {
//                @Override
//                public void valueChanged(Watch w) {
//                    final boolean[] isEvaluating = new boolean[] { false };
//                    final String text = getWatchValueText(watch, valueProvider, isEvaluating);
//            javax.swing.SwingUtilities.invokeLater(new Runnable() {
//                @Override
//                public void run() {
////                    Action[] actions = valueProvider.getHeadActions(watch);
//                    addActions(headActions, actions);
//                    textComponent.setText(textComponent);
//                    actions = valueProvider.getTailActions(watch);
//                    addActions(tailActions, actions);
//                    Dimension size = getPreferredSize();
//                    Point loc = getLocation();
//                    setBounds(loc.x, loc.y, size.width, size.height);
//                    if (!isEvaluating[0]) {
//                        adjustSize();
//                    }
//                }
//            });
//                }
//            });

            textComponent = createNonEditableSelectableLabel(diagnosticInfo);

            if (font != null) {
                textComponent.setFont(font);
            }
            textComponent.setBorder(new javax.swing.border.EmptyBorder(0, 3, 0, 3));
            gridConstraints.gridx++;
            add(textComponent, gridConstraints);
            setComponentZOrder(textComponent, 1);
            tailActions = createActionsToolbar();
            gridConstraints.gridx++;
            gridConstraints.weighty = 1;
            gridConstraints.fill = GridBagConstraints.VERTICAL;
            add(tailActions, gridConstraints);
            //actions = valueProvider.getTailActions(watch);
            //the list is diagnostic itself + notes
            
            nextAction = new AbstractAction("next", ImageUtilities.loadImageIcon("org/netbeans/modules/cnd/diagnostics/clank/resources/nextmatch.png", false)) {//NOI18N
                @Override
                public void actionPerformed(ActionEvent e) {
                    try {
                        next( pin.getFile().getFileSystem(), listener);
                    } catch (FileStateInvalidException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                };                    
            };            
            nextAction.setEnabled(isNextActionEnabled());
            //nextAction.setEnabled(currentDiagnosticIndex < notes.size() -1);
            prevAction = new AbstractAction("prev", ImageUtilities.loadImageIcon("org/netbeans/modules/cnd/diagnostics/clank/resources/prevmatch.png", false)) {//NOI18N
                @Override
                public void actionPerformed(ActionEvent e) {
                    try {
                        prev(pin.getFile().getFileSystem(), listener);
                    } catch (FileStateInvalidException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
            };
            prevAction.setEnabled(isPrevActionEnabled());
            addActions(tailActions, new Action[]{nextAction, prevAction});
            JSeparator iconsSeparator = new JSeparator(JSeparator.VERTICAL);
            gridConstraints.gridx++;
            gridConstraints.weighty = 1;
            gridConstraints.fill = GridBagConstraints.VERTICAL;
            add(iconsSeparator, gridConstraints);
            gridConstraints.weighty = 0;
            gridConstraints.fill = GridBagConstraints.NONE;

//            Icon commentIcon = ImageUtilities.loadImageIcon(ICON_COMMENT, false);
//            JButton commentButton = new JButton(commentIcon);
//            commentButton.setBorder(new javax.swing.border.EmptyBorder(0, 3, 0, 3));
//            commentButton.setBorderPainted(false);
//            commentButton.setContentAreaFilled(false);
//            gridConstraints.gridx++;
//            add(commentButton, gridConstraints);

            JButton closeButton = org.openide.awt.CloseButtonFactory.createCloseButton();
            closeButton.addActionListener(new java.awt.event.ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    DiagnosticsAnnotationProvider.clearAll();
                    //StickyPanel.this.close();   
                    //and all
                    //watch.remove();
                }
            });
            gridConstraints.gridx++;
            add(closeButton, gridConstraints);

            final int gridwidth = gridConstraints.gridx + 1;
            if (pin.getComment() != null) {
                addCommentField(pin.getComment(), gridwidth);
            }
            MouseAdapter mouseAdapter = new java.awt.event.MouseAdapter() {
                private Point orig;
                private Cursor lastCursor;

                @Override
                public void mouseDragged(MouseEvent e) {
                    if (!canDrag(e)) {
                        // Do not drag with any meta key pressed - allow text selection
                        unsetMoveCursor();
                        return;
                    }
                    if (orig == null) {
                        // Needs to have orig pressed point for precise movement
                        return;
                    }
                    setMoveCursor();
                    Point p = getLocation();
                    int deltaX = e.getX() - orig.x;
                    int deltaY = e.getY() - orig.y;
                    p.translate(deltaX, deltaY);
                    setLocation(p);
                    Point linePoint = new Point(p.x, p.y + textComponent.getHeight() / 2);
                    int pos = eui.getComponent().viewToModel(linePoint);
                    int line;
                    try {
                        line = LineDocumentUtils.getLineIndex(eui.getDocument(), pos);
                    } catch (BadLocationException ex) {
                        line = pin.getLine();
                    }
                    pin.move(line, p);
                    textComponent.setCaretPosition(0);   // Assure that we do not select anything
                    e.consume();
                    adjustSize();
                }

                @Override
                public void mousePressed(MouseEvent e) {
                    orig = e.getPoint();
                    if (canDrag(e)) {
                        setMoveCursor();
                    }
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                    orig = null;
                    unsetMoveCursor();
                }

                private void setMoveCursor() {
                    if (lastCursor == null) {
                        lastCursor = getCursor();
                        setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
                    }
                }

                private void unsetMoveCursor() {
                    if (lastCursor != null) {
                        setCursor(lastCursor);
                        lastCursor = null;
                    }
                }
            };
            addMouseListener(mouseAdapter);
            addMouseMotionListener(mouseAdapter);
            textComponent.addMouseListener(mouseAdapter);
            textComponent.addMouseMotionListener(mouseAdapter);
            adjustSize();
            eui.getComponent().addComponentListener(new ComponentListener() {
                @Override
                public void componentResized(ComponentEvent e) {
                    adjustSize();
                }

                @Override
                public void componentMoved(ComponentEvent e) {
                }

                @Override
                public void componentShown(ComponentEvent e) {
                }

                @Override
                public void componentHidden(ComponentEvent e) {
                }
            });
            adjustSize();
        }
                
        
        private void close() {
            eui.getStickyWindowSupport().removeWindow(this);
        }
        
        private void setCurrent(boolean isCurrent) {
            textComponent.setBackground(isCurrent ? CURRENT_PIN_BACKGROUND_COLOR : UIManager.getColor("Label.background"));//NOI18N
            nextAction.setEnabled(isNextActionEnabled());
            prevAction.setEnabled(isPrevActionEnabled());
            doRepaint();
        }        


        private boolean canDrag(MouseEvent e) {
            return (e.getModifiersEx() & (MouseEvent.ALT_DOWN_MASK
                    | MouseEvent.ALT_GRAPH_DOWN_MASK
                    | MouseEvent.CTRL_DOWN_MASK
                    | MouseEvent.META_DOWN_MASK
                    | MouseEvent.SHIFT_DOWN_MASK)) == 0;
        }

        @Override
        public Dimension getPreferredSize() {
            Dimension preferredSize = super.getPreferredSize();
            if (minPreferredHeight == 0) {
                minPreferredHeight = Math.max(16, UIManager.getIcon("Tree.expandedIcon").getIconHeight()) + 2;//NOI18N
            }
            if (preferredSize.height < minPreferredHeight) {
                preferredSize.height = minPreferredHeight;
            }
            return preferredSize;
        }

        private void adjustSize() {
            JTextComponent component = eui.getComponent();
            if (component == null) {
                // uninstalled.
                return;
            }
            int editorSize = component.getSize().width;
            int maxSize = editorSize - getLocation().x;
            Dimension prefSize = getPreferredSize();
            if (prefSize.width > maxSize) {
                int smallerBy = prefSize.width - maxSize;
                Dimension textSize = textComponent.getPreferredSize();
                int newTextSize = textSize.width - smallerBy;
                if (newTextSize < textSize.height) { // too small
                    newTextSize = textSize.height;
                }
                textSize.width = newTextSize;
                textComponent.setSize(textSize);
                textComponent.setPreferredSize(textSize);
                Dimension size = getPreferredSize();
                Point loc = getLocation();
                if (loc.x + size.width > editorSize) {
                    loc.x = editorSize - size.width;
                    if (loc.x < 0) {
                        loc.x = 0;
                    }
                }
                setBounds(loc.x, loc.y, size.width, size.height);
                doRepaint();
            } else if (prefSize.width < maxSize) {
                if (textComponent.isPreferredSizeSet()) {
                    textComponent.setPreferredSize(null);
                    textComponent.setSize(textComponent.getPreferredSize());
                    adjustSize();
                    Dimension size = getPreferredSize();
                    Point loc = getLocation();
                    setBounds(loc.x, loc.y, size.width, size.height);
                    doRepaint();
                }
            } else {    // pref size is equal to the max, we need to check if it can be shrinked
                if (textComponent.isPreferredSizeSet()) {
                    Dimension preferredSize = textComponent.getPreferredSize();
                    Dimension uiPreferredSize = textComponent.getUI().getPreferredSize(textComponent);
                    if (uiPreferredSize.width < preferredSize.width) {
                        textComponent.setPreferredSize(null);
                        textComponent.setSize(textComponent.getPreferredSize());
                        Dimension size = getPreferredSize();
                        Point loc = getLocation();
                        setBounds(loc.x, loc.y, size.width, size.height);
                        doRepaint();
                    }
                }
            }
        }

        private void doRepaint() {
            revalidate();
            repaint();
        }

        @Override
        public void scrollRectToVisible(Rectangle aRect) {
            // No op, this component should not change scrolling.
        }

//        private void hideValueField() {
//            valueField.setVisible(false);
//            Dimension size = getPreferredSize();
//            Point loc = getLocation();
//            setBounds(loc.x, loc.y, size.width, size.height);
//            GridBagConstraints constraints = ((GridBagLayout) getLayout()).getConstraints(textComponent);
//            constraints.insets = new Insets(0, 0, 0, 0);
//            ((GridBagLayout) getLayout()).setConstraints(textComponent, constraints);
//        }

        private static JToolBar createActionsToolbar() {
            JToolBar jt = new JToolBar(JToolBar.HORIZONTAL);
            jt.setBorder(new EmptyBorder(0, 0, 0, 0));
            jt.setFloatable(false);
            jt.setRollover(false);
            return jt;
        }

        private void addActions(JToolBar tb, Action[] actions) {
            tb.removeAll();
            boolean visible = false;
            if (actions != null) {
                for (Action a : actions) {
                    if (a != null) {
                        JButton btn = tb.add(a);
                        btn.setBorder(new javax.swing.border.EmptyBorder(0, 2, 0, 2));
                        btn.setBorderPainted(false);
                        btn.setContentAreaFilled(false);
                        btn.setRolloverEnabled(true);
                        btn.setOpaque(true);
                        btn.setFocusable(true);
                        btn.setMaximumSize(new java.awt.Dimension(16, 16));
                        btn.setMinimumSize(new java.awt.Dimension(16, 16));
                        btn.setPreferredSize(new java.awt.Dimension(16, 16));
                        visible = true;
                    } else {
                        tb.add(new JSeparator(JSeparator.VERTICAL));
                    }
                }
            }
            tb.setVisible(visible);
        }

        private void addCommentField(String text, int gridwidth) {
            commentField = new JTextField(text);
            commentField.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    commentUpdated();
                }
            });
            GridBagConstraints gridConstraints = new GridBagConstraints();
            gridConstraints.gridy = 1;
            gridConstraints.gridwidth = gridwidth;
            gridConstraints.fill = GridBagConstraints.HORIZONTAL;
            add(commentField, gridConstraints);
        }

        private void addCommentListener(int gridwidth) {
            if (commentField == null) {
                addCommentField("", gridwidth);
                setSize(getPreferredSize());
                revalidate();
                repaint();
            } else {
                boolean visible = !commentField.isVisible();
                commentField.setVisible(visible);
                setSize(getPreferredSize());
                revalidate();
                repaint();
                if (visible) {
                    commentField.requestFocusInWindow();
                }
            }
        }

        @Override
        public void removeNotify() {
            //valueProvider.unsetChangeListener(watch);
        }

        private void commentUpdated() {
           // ((EditorPin) watch.getPin()).setComment(commentField.getText());
        }

//        private String getWatchValueText(Watch watch, ValueProvider vp, boolean[] isEvaluating) {
//            String value = vp.getValue(watch);
//            //System.err.println("WatchAnnotationProvider.getWatchText("+watch.getExpression()+"): value = "+value+", lastValue = "+lastValue);
//            if (value == evaluatingValue) {
//                if (isEvaluating != null) {
//                    isEvaluating[0] = true;
//                }
//                return "<html>" + "<font color=\"red\">" + value + "</font>" + "</html>";
//            }
//            boolean bold = false;
//            boolean old = false;
//            if (value != null) {
//                bold = (lastValue != null && !lastValue.equals(value));
//                lastValue = value;
//            } else {
//                old = true;
//                value = lastValue;
//            }
//            String s1, s2;
//            if (bold) {
//                s1 = "<b>";
//                s2 = "</b>";
//            } else if (old) {
//                s1 = "<font color=\"gray\">";
//                s2 = "</font>";
//            } else {
//                s1 = s2 = "";
//            }
//            //System.err.println("  return: "+"<html>" + watch.getExpression() + " = " + s1 + value + s2 + "</html>");
//            return "<html>" + s1 + value + s2 + "</html>";
//        }
        

        /*
        private static JTextArea createMultiLineToolTip(String toolTipText, boolean wrapLines) {
            JTextArea ta = new TextToolTip(wrapLines);
            ta.setText(toolTipText);
            return ta;
        }
        
        private static class TextToolTip extends JTextArea {
            
            private static final String ELIPSIS = "..."; //NOI18N
            
            private final boolean wrapLines;
            
            public TextToolTip(boolean wrapLines) {
                this.wrapLines = wrapLines;
                setLineWrap(false); // It's necessary to have a big width of preferred size first.
            }
            
            public @Override void setSize(int width, int height) {
                Dimension prefSize = getPreferredSize();
                if (width >= prefSize.width) {
                    width = prefSize.width;
                } else { // smaller available width
                    // Set line wrapping and do super.setSize() to determine
                    // the real height (it will change due to line wrapping)
                    if (wrapLines) {
                        setLineWrap(true);
                        setWrapStyleWord(true);
                    }
                    
                    super.setSize(width, Integer.MAX_VALUE); // the height is unimportant
                    prefSize = getPreferredSize(); // re-read new pref width
                }
                if (height >= prefSize.height) { // enough height
                    height = prefSize.height;
                } else { // smaller available height
                    // Check how much can be displayed - cannot rely on line count
                    // because line wrapping may display single physical line
                    // into several visual lines
                    // Before using viewToModel() a setSize() must be called
                    // because otherwise the viewToModel() would return -1.
                    super.setSize(width, Integer.MAX_VALUE);
                    int offset = viewToModel(new Point(0, height));
                    Document doc = getDocument();
                    try {
                        if (offset > ELIPSIS.length()) {
                            offset -= ELIPSIS.length();
                            doc.remove(offset, doc.getLength() - offset);
                            doc.insertString(offset, ELIPSIS, null);
                        }
                    } catch (BadLocationException ble) {
                        // "..." will likely not be displayed but otherwise should be ok
                    }
                    // Recalculate the prefSize as it may be smaller
                    // than the present preferred height
                    height = Math.min(height, getPreferredSize().height);
                }
                super.setSize(width, height);
            }
            
            @Override
            public void setKeymap(Keymap map) {
                //#181722: keymaps are shared among components with the same UI
                //a default action will be set to the Keymap of this component below,
                //so it is necessary to use a Keymap that is not shared with other JTextAreas
                super.setKeymap(addKeymap(null, map));
            }
        }
         */
    }
    
//    private static JButton createNextMatchButton() {
//        JButton nextMatch = new JButton();
//        int size = 16;
//        nextMatch.setPreferredSize(new Dimension(size, size));
//        nextMatch.setContentAreaFilled(false);
//        nextMatch.setFocusable(false);
//        nextMatch.setBorder(BorderFactory.createEmptyBorder());
//        nextMatch.setBorderPainted(false);
//        nextMatch.setRolloverEnabled(true);
//        nextMatch.setIcon(ImageUtilities.loadImageIcon("org/netbeans/modules/cnd/diagnostics/clank/resources/nextmatch.png", false));//NOI18N
////        nextMatch.setRolloverIcon(getCloseTabRolloverImage());
//       // nextMatch.setPressedIcon(getCloseTabPressedImage());
//        return nextMatch;
//    }    
}
