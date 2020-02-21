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
package org.netbeans.modules.cnd.debugger.common2.debugger;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import org.netbeans.editor.EditorUI;
import org.netbeans.editor.PopupManager;
import org.netbeans.editor.ext.ToolTipSupport;
import org.netbeans.spi.debugger.ui.EditorContextDispatcher;
import org.netbeans.spi.viewmodel.UnknownTypeException;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.view.OutlineView;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.RequestProcessor;

/**
 *
 */
public final class ToolTipView extends JComponent implements ExplorerManager.Provider {
    private static final ToolTipView INSTANCE = new ToolTipView();
    private static final ExplorerManager manager = new ExplorerManager();
    
    private static RequestProcessor RP = new RequestProcessor(ToolTipView.class.getName());
    
    private volatile ActionListener listener;
    public static final int ON_DISPOSE = 0;
    
    public static ToolTipView getDefault() {
        return INSTANCE;
    }

    public ToolTipView() {
        final OutlineView ov = new OutlineView();
        ov.setPropertyColumns("value", "Value"); //NOI18N
        ov.getOutline().getColumnModel().getColumn(0).setHeaderValue("Name"); //NOI18N
        ov.getOutline().setRootVisible(true);
        ov.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(getForeground()),
                BorderFactory.createEmptyBorder(0, 0, 0, 0)));

        setLayout(new BorderLayout());
        add(ov, BorderLayout.CENTER);
    }

    public ToolTipView setRootElement(Node node) {
        getExplorerManager().setRootContext(node);
        return this;
    }
    
    public ToolTipView setOnDisposeListener(ActionListener listener) {
        this.listener = listener;
        return this;
    }

    public void showTooltip() {
        final JEditorPane ep = EditorContextDispatcher.getDefault().getMostRecentEditor();
        final EditorUI eui = org.netbeans.editor.Utilities.getEditorUI(ep);
        final ToolTipSupport toolTipSupport = eui.getToolTipSupport();
        toolTipSupport.setToolTipVisible(true);
        toolTipSupport.setToolTip(this, PopupManager.ViewPortBounds, PopupManager.AbovePreferred, 0, 0, ToolTipSupport.FLAGS_HEAVYWEIGHT_TOOLTIP);
    }

    @Override
    public ExplorerManager getExplorerManager() {
        return manager;
    }
    
    @Override
    public void removeNotify() {
        if (listener != null) {
            listener.actionPerformed(new ActionEvent(this, ON_DISPOSE, null));
            listener = null;
        }
    }    

    public static final class VariableNode extends AbstractNode {

        private Variable v;
        private static final Map<Variable, VariableNode> variables = new HashMap<Variable, VariableNode>();
        private static WatchModel watchModel = new WatchModel();
    
        public VariableNode(Variable v, Children ch) {
            super(ch);
            this.v = v;
            add(v);
        }
        
        private void add(Variable v) {
            variables.put(v, this);
        }
        
        public static void propertyChanged(Variable v) {
            final VariableNode node = variables.get(v);
            if (node == null) {
                return;
            }
            if (EventQueue.isDispatchThread()) {
                node.propertyChanged();
            } else {
                SwingUtilities.invokeLater(new Runnable() {

                    @Override
                    public void run() {
                        node.propertyChanged();
                    }
                });
            }
        }


        @Override
        public String getDisplayName() {
            return v.getVariableName();
        }

        @Override
        public Image getIcon(int type) {
            String path = "";
            
            try {
                path = watchModel.getIconBaseWithExtension(null, v);
            } catch (UnknownTypeException ex) {
                Exceptions.printStackTrace(ex);
            }
            setIconBaseWithExtension(path);
            return super.getIcon(type);
        }

        private void propertyChanged() {
            if (v.getNumChild() < 1) {
                setChildren(Children.LEAF);
            } else {
                if (getChildren() instanceof VariableNodeChildren) {
                    ((VariableNodeChildren) getChildren()).updateKeys();
                }
            }
            String path = "";
            
            try {
                path = watchModel.getIconBaseWithExtension(null, v);
            } catch (UnknownTypeException ex) {
                Exceptions.printStackTrace(ex);
            }
            setIconBaseWithExtension(path);            
            fireDisplayNameChange("old", "new"); //NOI18N
        }

        @Override
        public Node.PropertySet[] getPropertySets() {
            return new Node.PropertySet[]{new VariableNodePropertySet(v)};
        }

        private final class VariableNodePropertySet extends Node.PropertySet {

            private Variable v;

            public VariableNodePropertySet(Variable v) {
                this.v = v;
            }

            @Override
            public Node.Property<?>[] getProperties() {

                Node.Property<?>[] ps = new Node.Property<?>[]{new Node.Property<String>(String.class) {
                        @Override
                        public String getName() {
                            return "value"; //NOI18N
                        }

                        @Override
                        public boolean canRead() {
                            return true;
                        }

                        @Override
                        public String getValue() throws IllegalAccessException, InvocationTargetException {
                            return v.getAsText();
                        }

                        @Override
                        public boolean canWrite() {
                            return false;
                        }

                        @Override
                        public void setValue(String val) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
                        }
                    }};
                return ps;
            }
        }
    }
    
    public static abstract class VariableNodeChildren extends Children.Keys<Variable> {
        private final Variable var;
        public VariableNodeChildren(Variable v) {
            var = v;
        }
        
        private void updateKeys() {
            // e.g.(?) Explorer view under Children.MUTEX subsequently calls e.g.
            // SuiteProject$Info.getSimpleName() which acquires ProjectManager.mutex(). And
            // since this method might be called under ProjectManager.mutex() write access
            // and updateKeys() --> setKeys() in turn calls Children.MUTEX write access,
            // deadlock is here, so preventing it... (also got this under read access)
            RP.post(new Runnable() {
                @Override
                public void run() {
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            setKeys(var.getChildren());
                        }
                    });
                }
            });
        }

        @Override
        protected Node[] createNodes(Variable key) {
            return new Node[]{new VariableNode(key, Children.LEAF)};
        }
    }
    
//    public static ExpandableTooltip getExpTooltipForText(NativeDebugger debugger, Line.Part lp,
//            String toolTipExpr, String toolTipValue) {
//         return new ExpandableTooltip(debugger, lp, toolTipExpr, toolTipValue); // NOI18N
//    }
//    
//    public static class ExpandableTooltip extends JPanel {
//
//        private static final String UI_PREFIX = "ToolTip"; // NOI18N
//        
//        private JButton expButton;
//        private JButton pinButton;
//        private JComponent textToolTip;
//        private boolean widthCheck = true;
//        private boolean sizeSet = false;
//
//        public ExpandableTooltip(final NativeDebugger debugger, final Line.Part lp,
//                final String toolTipExpr, String toolTipValue) {
//            Font font = UIManager.getFont(UI_PREFIX + ".font"); // NOI18N
//            Color backColor = UIManager.getColor(UI_PREFIX + ".background"); // NOI18N
//            Color foreColor = UIManager.getColor(UI_PREFIX + ".foreground"); // NOI18N
//
//            if (backColor != null) {
//                setBackground(backColor);
//            }
//            setOpaque(true);
//            setBorder(BorderFactory.createCompoundBorder(
//                BorderFactory.createLineBorder(getForeground()),
//                BorderFactory.createEmptyBorder(0, 3, 0, 3)
//            ));
//
//            setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
//            Icon pinIcon = ImageUtilities.loadImageIcon("org/netbeans/editor/resources/pin.png", false);    // NOI18N
//            pinButton = new JButton(pinIcon);
//            pinButton.setBorder(new javax.swing.border.EmptyBorder(0, 0, 0, 5));
//            pinButton.setBorderPainted(false);
//            pinButton.setContentAreaFilled(false);
//            pinButton.addActionListener(new ActionListener() {
//                @Override
//                public void actionPerformed(ActionEvent e) {
//                    Point location = ExpandableTooltip.this.getLocation();
//                    JEditorPane ep = EditorContextDispatcher.getDefault().getMostRecentEditor();
//                    EditorUI eui = org.netbeans.editor.Utilities.getEditorUI(ep);
//                    location = eui.getStickyWindowSupport().convertPoint(location);
//                    DebuggerManager dbMgr = DebuggerManager.getDebuggerManager();
//                    final int mostRecentLineNumber = lp.getLine().getLineNumber();
//                    Watch.Pin pin = new EditorPin(EditorContextBridge.getMostRecentFileObject(), mostRecentLineNumber, location);
//                    final Watch w = dbMgr.createPinnedWatch(toolTipExpr, pin);
//                    SwingUtilities.invokeLater(new Runnable() {
//                        @Override
//                        public void run() {
//                            String valueProviderId = NativePinWatchValueProvider.ID;
//                            try {
//                                PinWatchUISupport.getDefault().pin(w, valueProviderId);
//                            } catch (IllegalArgumentException ex) {
//                                Exceptions.printStackTrace(ex);
//                            }
//                        }
//                    });
//                }
//            });
//            add(pinButton);
//            //check if expandable
//            Icon expIcon = UIManager.getIcon ("Tree.collapsedIcon");    // NOI18N
//            expButton = new JButton(expIcon);
//            expButton.setBorder(new javax.swing.border.EmptyBorder(0, 0, 0, 5));
//            expButton.setBorderPainted(false);
//            expButton.setContentAreaFilled(false);
//            expButton.addActionListener(new ActionListener() {
//
//                @Override
//                public void actionPerformed(ActionEvent e) {
//                    debugger.evaluateInOutline(toolTipExpr);
//                }
//            });
//            add(expButton);
//            //JLabel l = new JLabel(toolTipText);
//            // Multi-line tooltip:
//            JTextArea l = createMultiLineToolTip(toolTipExpr + "=" + toolTipValue, true);    // NOI18N
//            if (font != null) {
//                l.setFont(font);
//            }
//            if (foreColor != null) {
//                l.setForeground(foreColor);
//            }
//            if (backColor != null) {
//                l.setBackground(backColor);
//            }
//            textToolTip = l;
//            add(l);
//        }
//        
//        void setWidthCheck(boolean widthCheck) {
//            this.widthCheck = widthCheck;
//        }
//
//        @Override
//        public Dimension getPreferredSize() {
//            if (!sizeSet) {
//                // Be big enough initially.
//                return new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE);
//            }
//            return super.getPreferredSize();
//        }
//        
//        @Override
//        public void setSize(int width, int height) {
//            Dimension prefSize = getPreferredSize();
//            Dimension expButtonSize = expButton.getPreferredSize();
//            Dimension pinButtonSize = pinButton.getPreferredSize();
//            if (widthCheck) {
//                Insets insets = getInsets();
//                int textWidth = width - insets.left - expButtonSize.width - pinButtonSize.width - insets.right;
//                height = Math.max(height, Math.max(expButtonSize.height, pinButtonSize.height));
//                textToolTip.setSize(textWidth, height);
//                Dimension textPreferredSize = textToolTip.getPreferredSize();
//                super.setSize(
//                        insets.left + expButtonSize.width + pinButtonSize.width + textPreferredSize.width + insets.right,
//                        insets.top + Math.max(Math.max(expButtonSize.height, pinButtonSize.height), textPreferredSize.height) + insets.bottom);
//            } else {
//                if (height >= prefSize.height) { // enough height
//                    height = prefSize.height;
//                }
//                super.setSize(width, height);
//            }
//            sizeSet = true;
//        }
//        
//        public void showTooltip() {
//            final JEditorPane ep = EditorContextDispatcher.getDefault().getMostRecentEditor();
//            final EditorUI eui = org.netbeans.editor.Utilities.getEditorUI(ep);
//            final ToolTipSupport toolTipSupport = eui.getToolTipSupport();
//            toolTipSupport.setToolTip(this);
//        }
//        
//        private static JTextArea createMultiLineToolTip(String toolTipText, boolean wrapLines) {
//            JTextArea ta = new TextToolTip(wrapLines);
//            ta.setText(toolTipText);
//            return ta;
//        }
//
//        private static class TextToolTip extends JTextArea {
//            
//            private static final String ELIPSIS = "..."; //NOI18N
//            
//            private final boolean wrapLines;
//            
//            public TextToolTip(boolean wrapLines) {
//                this.wrapLines = wrapLines;
//                setLineWrap(false); // It's necessary to have a big width of preferred size first.
//            }
//            
//            public @Override void setSize(int width, int height) {
//                Dimension prefSize = getPreferredSize();
//                if (width >= prefSize.width) {
//                    width = prefSize.width;
//                } else { // smaller available width
//                    // Set line wrapping and do super.setSize() to determine
//                    // the real height (it will change due to line wrapping)
//                    if (wrapLines) {
//                        setLineWrap(true);
//                        setWrapStyleWord(true);
//                    }
//                    
//                    super.setSize(width, Integer.MAX_VALUE); // the height is unimportant
//                    prefSize = getPreferredSize(); // re-read new pref width
//                }
//                if (height >= prefSize.height) { // enough height
//                    height = prefSize.height;
//                } else { // smaller available height
//                    // Check how much can be displayed - cannot rely on line count
//                    // because line wrapping may display single physical line
//                    // into several visual lines
//                    // Before using viewToModel() a setSize() must be called
//                    // because otherwise the viewToModel() would return -1.
//                    super.setSize(width, Integer.MAX_VALUE);
//                    int offset = viewToModel(new Point(0, height));
//                    Document doc = getDocument();
//                    try {
//                        if (offset > ELIPSIS.length()) {
//                            offset -= ELIPSIS.length();
//                            doc.remove(offset, doc.getLength() - offset);
//                            doc.insertString(offset, ELIPSIS, null);
//                        }
//                    } catch (BadLocationException ble) {
//                        // "..." will likely not be displayed but otherwise should be ok
//                    }
//                    // Recalculate the prefSize as it may be smaller
//                    // than the present preferred height
//                    height = Math.min(height, getPreferredSize().height);
//                }
//                super.setSize(width, height);
//            }
//            
//            @Override
//            public void setKeymap(Keymap map) {
//                //#181722: keymaps are shared among components with the same UI
//                //a default action will be set to the Keymap of this component below,
//                //so it is necessary to use a Keymap that is not shared with other JTextAreas
//                super.setKeymap(addKeymap(null, map));
//            }
//        }
//    }
    
}
