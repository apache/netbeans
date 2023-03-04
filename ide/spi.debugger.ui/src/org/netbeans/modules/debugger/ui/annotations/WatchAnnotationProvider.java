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
package org.netbeans.modules.debugger.ui.annotations;

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
import java.util.IdentityHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
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
import org.netbeans.api.annotations.common.StaticResource;
import org.netbeans.api.debugger.Breakpoint;
import org.netbeans.api.debugger.DebuggerEngine;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.LazyDebuggerManagerListener;
import org.netbeans.api.debugger.Session;
import org.netbeans.api.debugger.Watch;
import org.netbeans.api.editor.document.LineDocumentUtils;
import org.netbeans.editor.EditorUI;
import org.netbeans.editor.Utilities;
import org.netbeans.spi.debugger.DebuggerServiceRegistration;
import org.netbeans.spi.debugger.ui.EditorContextDispatcher;
import org.netbeans.spi.debugger.ui.EditorPin;
import org.netbeans.spi.debugger.ui.PinWatchUISupport;
import org.netbeans.spi.debugger.ui.PinWatchUISupport.ValueProvider;
import org.netbeans.spi.debugger.ui.PinWatchUISupport.ValueProvider.ValueChangeListener;
import org.netbeans.api.editor.StickyWindowSupport;
import org.openide.cookies.EditorCookie;
import org.openide.cookies.LineCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.text.Annotation;
import org.openide.text.AnnotationProvider;
import org.openide.text.CloneableEditorSupport;
import org.openide.text.Line;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;

/**
 * Annotation of watches pinned in the editor.
 *
 * @author Ralph Benjamin Ruijs, Martin Entlicher
 */
@org.openide.util.lookup.ServiceProvider(service=org.openide.text.AnnotationProvider.class)
@DebuggerServiceRegistration(types={LazyDebuggerManagerListener.class})
public class WatchAnnotationProvider implements AnnotationProvider, LazyDebuggerManagerListener {
    
    @SuppressWarnings("PublicField")
    public static PinSupportedAccessor PIN_SUPPORT_ACCESS; // Set from PinWatchSupport.getDefault()
    private static WatchAnnotationProvider INSTANCE;
    
    private static final Map<Watch, Annotation> watchToAnnotation = new IdentityHashMap<>();
    private static final Map<Watch, JComponent> watchToWindow = new IdentityHashMap<>();
    private Set<PropertyChangeListener> dataObjectListeners;
    
    public WatchAnnotationProvider() {
        PinWatchUISupport.getDefault(); // To initialize PIN_SUPPORT_ACCESS
        INSTANCE = this;
    }

    @Override
    public void annotate(Line.Set lines, Lookup context) {
        DataObject dobj = context.lookup(DataObject.class);
        if(dobj == null) return;
        final CloneableEditorSupport ces = context.lookup(CloneableEditorSupport.class);
        if (ces == null) {
            return ;
        }
        FileObject file = context.lookup(FileObject.class);
        if (file == null) {
            file = dobj.getPrimaryFile();
        }
        List<Watch> pinnedWatches = null;
        Watch[] watches = DebuggerManager.getDebuggerManager().getWatches();
        for (Watch watch : watches) {
            Watch.Pin pin = watch.getPin();
            if (!(pin instanceof EditorPin)) {
                continue;
            }
            EditorPin epin = (EditorPin) pin;
            if (!file.equals(epin.getFile())) {
                continue;
            }
            if (pinnedWatches == null) {
                pinnedWatches = new LinkedList<>();
            }
            pinnedWatches.add(watch);
        }
        if (pinnedWatches != null) {
            final List<Watch> pws = pinnedWatches;
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    JEditorPane[] openedPanes = ces.getOpenedPanes();
                    if (openedPanes != null) {
                        for (JEditorPane pane : openedPanes) {
                            EditorUI eui = Utilities.getEditorUI(pane);
                            if (eui == null) {
                                continue;
                            }
                            synchronized (watchToAnnotation) {
                                for (Watch watch : pws) {
                                    EditorPin epin = (EditorPin) watch.getPin();
                                    Line line = lines.getOriginal(epin.getLine());
                                    pin(watch, eui, line);
                                }
                            }
                        }
                    }
                }
            });
        }
    }

    @SuppressWarnings("AssignmentToMethodParameter")
    private void pin(Watch watch, EditorUI eui, Line line) throws IndexOutOfBoundsException {
        Annotation ann = watchToAnnotation.remove(watch); // just to be sure
        if(ann != null) ann.detach();
        JComponent frame = watchToWindow.remove(watch);
        StickyWindowSupport stickyWindowSupport = eui.getStickyWindowSupport();
        if(frame != null) {
            frame.setVisible(false);
            stickyWindowSupport.removeWindow(frame);
        }
        
        final EditorPin pin = (EditorPin) watch.getPin();
        if (pin == null) return;

        if(line == null) {
            line = getLine(pin.getFile(), pin.getLine());
        }
        
        final DebuggerAnnotation annotation = new DebuggerAnnotation(DebuggerAnnotation.WATCH_ANNOTATION_TYPE, line);
        annotation.setWatch(watch);
        watchToAnnotation.put(watch, annotation);
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
        
        JComponent window = new StickyPanel(watch, eui);
        stickyWindowSupport.addWindow(window);
        window.setLocation(pin.getLocation());
        watchToWindow.put(watch, window);
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                Dimension size = window.getPreferredSize();
                Point loc = window.getLocation();
                window.setBounds(loc.x, loc.y, size.width, size.height);
            }
        });
    }
    
    private Line getLine (FileObject file, int lineNumber) {
        if (file == null) return null;
        DataObject dataObject;
        try {
            dataObject = DataObject.find (file);
        } catch (DataObjectNotFoundException ex) {
            return null;
        }
        if (dataObject == null) return null;
        LineCookie lineCookie = dataObject.getLookup().lookup(LineCookie.class);
        if (lineCookie == null) return null;
        Line.Set ls = lineCookie.getLineSet ();
        if (ls == null) return null;
        try {
            return ls.getCurrent (lineNumber);
        } catch (IndexOutOfBoundsException | IllegalArgumentException e) {
        }
        return null;
    }

    @Override
    public String[] getProperties() {
        return new String [] {
            DebuggerManager.PROP_WATCHES,
        };
    }

    @Override
    public Breakpoint[] initBreakpoints() {
        return null;
    }

    @Override
    public void breakpointAdded(Breakpoint breakpoint) {
    }

    @Override
    public void breakpointRemoved(Breakpoint breakpoint) {
    }

    @Override
    public void initWatches() {
    }

    @Override
    public void watchAdded(Watch watch) {
        /*
        Watch.Pin pin = watch.getPin();
        if (pin instanceof EditorPin) {
            // TODO:
            final JEditorPane ep = EditorContextDispatcher.getDefault().getMostRecentEditor();
            if(ep == null) return;
            EditorUI eui = Utilities.getEditorUI(ep);
            if(eui == null)  return;
            synchronized(watchToAnnotation) {
                pin(watch, eui, null);
            }
        }
        */
    }

    @Override
    public void watchRemoved(Watch watch) {
        synchronized(watchToAnnotation) {
            Annotation annotation = watchToAnnotation.remove(watch);
            if(annotation != null) {
                annotation.detach();
            }
            JComponent frame = watchToWindow.remove(watch);
            if(frame != null) {
                EditorUI eui = ((StickyPanel) frame).eui;
                eui.getStickyWindowSupport().removeWindow(frame);
            }
        }
    }

    @Override
    public void sessionAdded(Session session) {
    }

    @Override
    public void sessionRemoved(Session session) {
    }

    @Override
    public void engineAdded(DebuggerEngine engine) {
    }

    @Override
    public void engineRemoved(DebuggerEngine engine) {
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
    }

    private static final class StickyPanel extends JPanel {
        @StaticResource
        private static final String ICON_COMMENT = "org/netbeans/modules/debugger/resources/actions/Comment.png";   // NOI18N
        private static final String UI_PREFIX = "ToolTip"; // NOI18N
        private final Watch watch;
        private final EditorUI eui;
        private final JTextComponent textComponent;
        private final JTextField valueField;
        private final JToolBar headActions;
        private final JToolBar tailActions;
        private JTextField commentField;
        private final ValueProvider valueProvider;
        private final String evaluatingValue;
        private String lastValue;
        private int minPreferredHeight = 0;

        @SuppressWarnings("OverridableMethodCallInConstructor")
        public StickyPanel(final Watch watch, final EditorUI eui) {
            this.watch = watch;
            this.eui = eui;
            EditorPin pin = (EditorPin) watch.getPin();
            Font font = UIManager.getFont(UI_PREFIX + ".font"); // NOI18N
            setOpaque(true);
            
            setBorder(BorderFactory.createLineBorder(getForeground()));
            
            setLayout(new GridBagLayout());
            GridBagConstraints gridConstraints = new GridBagConstraints();
            gridConstraints.gridx = 0;
            gridConstraints.gridy = 0;
            
            valueProvider = PIN_SUPPORT_ACCESS.getValueProvider(pin);
            headActions = createActionsToolbar();
            add(headActions, gridConstraints);
            Action[] actions = valueProvider.getHeadActions(watch);
            addActions(headActions, actions);
            evaluatingValue = valueProvider.getEvaluatingText();
            final String expressionText = watch.getExpression() + " = ";
            valueField = new JTextField();
            valueField.setVisible(false);
            valueProvider.setChangeListener(watch, new ValueChangeListener() {
                @Override
                public void valueChanged(Watch w) {
                    final boolean[] isEvaluating = new boolean[] { false };
                    final String text = getWatchValueText(watch, valueProvider, isEvaluating);
                    javax.swing.SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            Action[] actions = valueProvider.getHeadActions(watch);
                            addActions(headActions, actions);
                            textComponent.setText(expressionText + text);
                            actions = valueProvider.getTailActions(watch);
                            addActions(tailActions, actions);
                            Dimension size = getPreferredSize();
                            Point loc = getLocation();
                            setBounds(loc.x, loc.y, size.width, size.height);
                            if (!isEvaluating[0]) {
                                adjustSize();
                            }
                        }
                    });
                }
            });
            
            textComponent = createNonEditableSelectableLabel(expressionText + getWatchValueText(watch, valueProvider, null));
            
            if (font != null) {
                textComponent.setFont(font);
            }
            textComponent.setBorder(new javax.swing.border.EmptyBorder(0, 3, 0, 3));
            gridConstraints.gridx++;
            add(textComponent, gridConstraints);
            final int expressionTextPositionEnd = textComponent.getFontMetrics(textComponent.getFont()).stringWidth(expressionText);
            {
                Insets lastInsets = gridConstraints.insets;
                gridConstraints.insets = new Insets(0, expressionTextPositionEnd, 0, 0);
                add(valueField, gridConstraints);
                gridConstraints.insets = lastInsets;
            }
            setComponentZOrder(textComponent, 1);
            setComponentZOrder(valueField, 0);
            TextKeysMouseListener textKeysMouseListener = new TextKeysMouseListener(expressionTextPositionEnd);
            textComponent.addMouseListener(textKeysMouseListener);
            textComponent.addKeyListener(textKeysMouseListener);
            textComponent.addFocusListener(textKeysMouseListener);
            valueField.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    String newValue = valueField.getText();
                    if (valueProvider.setValue(watch, newValue)) {
                        boolean[] isEvaluating = new boolean[] { false };
                        textComponent.setText(expressionText + getWatchValueText(watch, valueProvider, isEvaluating));
                        if (!isEvaluating[0]) {
                            adjustSize();
                        }
                    }
                    hideValueField();
                }
            });
            valueField.addFocusListener(new FocusListener() {
                @Override
                public void focusGained(FocusEvent e) {}

                @Override
                public void focusLost(FocusEvent e) {
                    hideValueField();
                }
            });
            valueField.addKeyListener(new KeyListener() {
                @Override
                public void keyTyped(KeyEvent e) {
                    if (KeyEvent.VK_ESCAPE == e.getKeyChar()) {
                        hideValueField();
                    }
                }

                @Override
                public void keyPressed(KeyEvent e) {}

                @Override
                public void keyReleased(KeyEvent e) {}
            });

            tailActions = createActionsToolbar();
            gridConstraints.gridx++;
            gridConstraints.weighty = 1;
            gridConstraints.fill = GridBagConstraints.VERTICAL;
            add(tailActions, gridConstraints);
            actions = valueProvider.getTailActions(watch);
            addActions(tailActions, actions);
            JSeparator iconsSeparator = new JSeparator(JSeparator.VERTICAL);
            gridConstraints.gridx++;
            gridConstraints.weighty = 1;
            gridConstraints.fill = GridBagConstraints.VERTICAL;
            add(iconsSeparator, gridConstraints);
            gridConstraints.weighty = 0;
            gridConstraints.fill = GridBagConstraints.NONE;

            Icon commentIcon = ImageUtilities.loadImageIcon(ICON_COMMENT, false);
            JButton commentButton = new JButton(commentIcon);
            commentButton.setBorder(new javax.swing.border.EmptyBorder(0, 3, 0, 3));
            commentButton.setBorderPainted(false);
            commentButton.setContentAreaFilled(false);
            gridConstraints.gridx++;
            add(commentButton, gridConstraints);

            JButton closeButton = org.openide.awt.CloseButtonFactory.createCloseButton();
            closeButton.addActionListener(new java.awt.event.ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    eui.getStickyWindowSupport().removeWindow(StickyPanel.this);
                    watch.remove();
                }
            });
            gridConstraints.gridx++;
            add(closeButton, gridConstraints);

            final int gridwidth = gridConstraints.gridx + 1;
            if (pin.getComment() != null) {
                addCommentField(pin.getComment(), gridwidth);
            }
            commentButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    addCommentListener(gridwidth);
                }
            });

            MouseAdapter mouseAdapter = new java.awt.event.MouseAdapter() {
                private Point orig;
                private Cursor lastCursor;
                @Override
                public void mouseDragged(MouseEvent e) {
                    if (!canDrag(e)) {
                        // Do not drag with any meta key pressed - allow text selection
                        unsetMoveCursor();
                        return ;
                    }
                    if(orig == null) {
                        // Needs to have orig pressed point for precise movement
                        return ;
                    }
                    setMoveCursor();
                    Point p = getLocation();
                    int deltaX = e.getX() - orig.x;
                    int deltaY = e.getY() - orig.y;
                    p.translate(deltaX, deltaY);
                    setLocation(p);
                    Point linePoint = new Point(p.x, p.y + textComponent.getHeight()/2);
                    int pos = eui.getComponent().viewToModel(linePoint);
                    int line;
                    try {
                        line = LineDocumentUtils.getLineIndex(eui.getDocument(), pos);
                    } catch (BadLocationException ex) {
                        line = ((EditorPin) watch.getPin()).getLine();
                    }
                    ((EditorPin) watch.getPin()).move(line, p);
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
                public void componentMoved(ComponentEvent e) {}
                @Override
                public void componentShown(ComponentEvent e) {}
                @Override
                public void componentHidden(ComponentEvent e) {}
            });
        }

        private boolean canDrag(MouseEvent e) {
            return (e.getModifiersEx() & (MouseEvent.ALT_DOWN_MASK |
                                          MouseEvent.ALT_GRAPH_DOWN_MASK |
                                          MouseEvent.CTRL_DOWN_MASK |
                                          MouseEvent.META_DOWN_MASK |
                                          MouseEvent.SHIFT_DOWN_MASK)) == 0;
        }

        @Override
        public Dimension getPreferredSize() {
            Dimension preferredSize = super.getPreferredSize();
            if (minPreferredHeight == 0) {
                minPreferredHeight = Math.max(16, UIManager.getIcon("Tree.expandedIcon").getIconHeight()) + 2;
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
                return ;
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
        
        private void hideValueField() {
            valueField.setVisible(false);
            Dimension size = getPreferredSize();
            Point loc = getLocation();
            setBounds(loc.x, loc.y, size.width, size.height);
            GridBagConstraints constraints = ((GridBagLayout) getLayout()).getConstraints(textComponent);
            constraints.insets = new Insets(0, 0, 0, 0);
            ((GridBagLayout) getLayout()).setConstraints(textComponent, constraints);
        }

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
                        btn.setRolloverEnabled(false);
                        btn.setOpaque(false);
                        btn.setFocusable(false);
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
            valueProvider.unsetChangeListener(watch);
        }

        private void commentUpdated() {
            ((EditorPin) watch.getPin()).setComment(commentField.getText());
        }

        private String getWatchValueText(Watch watch, ValueProvider vp, boolean[] isEvaluating) {
            String value = vp.getValue(watch);
            //System.err.println("WatchAnnotationProvider.getWatchText("+watch.getExpression()+"): value = "+value+", lastValue = "+lastValue);
            if (value == evaluatingValue) {
                if (isEvaluating != null) {
                    isEvaluating[0] = true;
                }
                return "<html>" + "<font color=\"red\">" + value + "</font>" + "</html>";
            }
            boolean bold = false;
            boolean old = false;
            if (value != null) {
                bold = (lastValue != null && !lastValue.equals(value));
                lastValue = value;
            } else {
                old = true;
                value = lastValue;
            }
            String s1, s2;
            if (bold) {
                s1 = "<b>";
                s2 = "</b>";
            } else if (old) {
                s1 = "<font color=\"gray\">";
                s2 = "</font>";
            } else {
                s1 = s2 = "";
            }
            //System.err.println("  return: "+"<html>" + watch.getExpression() + " = " + s1 + value + s2 + "</html>");
            return "<html>" + s1 + value + s2 + "</html>";
        }

        private class TextKeysMouseListener implements KeyListener, MouseListener, FocusListener {

            private final Cursor selectCursor = Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR);
            private Cursor lastCursor;
            private boolean lastCursorUnset;
            private final int expressionTextPositionEnd;
            private Component lastFocusOwner;

            TextKeysMouseListener(int expressionTextPositionEnd) {
                this.expressionTextPositionEnd = expressionTextPositionEnd;
            }

            @Override
            public void keyTyped(KeyEvent e) {}

            @Override
            public void keyPressed(KeyEvent e) {
                if (e.isAltDown() || e.isAltGraphDown() ||
                    e.isControlDown() || e.isMetaDown() ||
                    e.isShiftDown()) {

                    setSelectCursor();
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
                if (!(e.isAltDown() || e.isAltGraphDown() ||
                      e.isControlDown() || e.isMetaDown() ||
                      e.isShiftDown())) {

                    unsetSelectCursor();
                }
            }

            private void setSelectCursor() {
                if (lastCursor == null && !lastCursorUnset) {
                    lastCursorUnset = !textComponent.isCursorSet();
                    if (!lastCursorUnset) {
                        lastCursor = textComponent.getCursor();
                    }
                    textComponent.setCursor(selectCursor);
                }
            }

            private void unsetSelectCursor() {
                if (lastCursor != null || lastCursorUnset) {
                    textComponent.setCursor(lastCursor);
                    lastCursor = null;
                    lastCursorUnset = false;
                }
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                String editableValue = valueProvider.getEditableValue(watch);
                if (editableValue != null) {
                    if (e.getX() < expressionTextPositionEnd) {
                        return ;
                    }
                    valueField.setVisible(true);
                    valueField.setPreferredSize(null);
                    valueField.setText(editableValue);
                    Dimension fieldSize = valueField.getPreferredSize();
                    fieldSize.width = textComponent.getSize().width - expressionTextPositionEnd;
                    int minWidth = 2*fieldSize.height;  // Have some reasonable minimum width
                    if (fieldSize.width < minWidth) {
                        int extendedBy = minWidth - fieldSize.width;
                        fieldSize.width = minWidth;
                        GridBagConstraints constraints = ((GridBagLayout) getLayout()).getConstraints(textComponent);
                        constraints.insets = new Insets(0, 0, 0, extendedBy);
                        ((GridBagLayout) getLayout()).setConstraints(textComponent, constraints);
                    }
                    valueField.setPreferredSize(fieldSize);
                    valueField.requestFocusInWindow();
                    Dimension size = getPreferredSize();
                    Point loc = getLocation();
                    setBounds(loc.x, loc.y, size.width, size.height);
                    doRepaint();
                    e.consume();
                }
            }

            @Override
            public void mousePressed(MouseEvent e) {}

            @Override
            public void mouseReleased(MouseEvent e) {}

            @Override
            public void mouseEntered(MouseEvent e) {
                KeyboardFocusManager kfm = KeyboardFocusManager.getCurrentKeyboardFocusManager();
                lastFocusOwner = kfm.getFocusOwner();
                if (lastFocusOwner != null) {
                    lastFocusOwner.addKeyListener(this);
                }
                if (!canDrag(e)) {
                    setSelectCursor();
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (lastFocusOwner != null) {
                    lastFocusOwner.removeKeyListener(this);
                    lastFocusOwner = null;
                }
                unsetSelectCursor();
            }

            @Override
            public void focusGained(FocusEvent e) {}

            @Override
            public void focusLost(FocusEvent e) {
                if (lastFocusOwner != null) {
                    lastFocusOwner.removeKeyListener(this);
                    lastFocusOwner = null;
                }
                unsetSelectCursor();
            }

        }

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

    private static JTextComponent createNonEditableSelectableLabel(String text) {
        JTextPane tf = new JTextPane() {
            @Override
            public void setText(String text) {
                String noWrapText = "<html><div style='white-space:nowrap'>"+text+"</div></html>";  // NOI18N
                super.setText(noWrapText);
            }
        };
        tf.setContentType("text/html");                                         // NOI18N
        tf.setEditable(false);
        tf.setBorder(null);
        tf.setForeground(UIManager.getColor("Label.foreground"));               // NOI18N
        tf.setBackground(UIManager.getColor("Label.background"));               // NOI18N
        tf.setFont(UIManager.getFont("Label.font"));                            // NOI18N
        tf.setText(text);
        tf.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);
        return tf;
    }

    public abstract static class PinSupportedAccessor {

        public abstract ValueProvider getValueProvider(EditorPin pin);
        
        public final void pin(Watch watch) throws DataObjectNotFoundException {
            EditorPin pin = (EditorPin) watch.getPin();
            DataObject dobj = DataObject.find(pin.getFile());
            EditorCookie ec = dobj.getLookup().lookup(EditorCookie.class);
            JEditorPane[] openedPanes = ec.getOpenedPanes();
            if (openedPanes == null) {
                throw new IllegalArgumentException("No editor panes opened for file "+pin.getFile());
            }
            LineCookie lineCookie = dobj.getLookup().lookup(LineCookie.class);
            if (lineCookie == null) {
                throw new IllegalArgumentException("No line cookie in "+pin.getFile());
            }
            Line.Set ls = lineCookie.getLineSet();
            Line line;
            try {
                line = ls.getCurrent(pin.getLine());
            } catch (IndexOutOfBoundsException e) {
                throw new IllegalArgumentException("Wrong line: "+pin.getLine(), e);
            }
            for (JEditorPane pane : openedPanes) {
                JEditorPane ep = EditorContextDispatcher.getDefault().getMostRecentEditor();
                EditorUI editorUI = Utilities.getEditorUI(pane);
                WatchAnnotationProvider.INSTANCE.pin(watch, editorUI, line);
            }
        }
    }
    
}
