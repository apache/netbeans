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
package org.netbeans.spi.debugger.ui;

import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BorderFactory;
import javax.swing.JEditorPane;
import javax.swing.SwingUtilities;
import javax.swing.text.Document;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.Watch;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.EditorUI;
import org.netbeans.editor.PopupManager;
import org.netbeans.editor.Utilities;
import org.netbeans.editor.ext.ToolTipSupport;
import org.netbeans.modules.debugger.ui.views.ToolTipView;
import org.netbeans.modules.debugger.ui.views.ToolTipView.ExpandableTooltip;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.util.Exceptions;

/**
 * A representation of a tooltip with actions.
 * The actions are described by {@link Expandable} and {@link Pinnable} classes.
 * 
 * @since 2.54
 * @author Martin Entlicher
 */
public final class ToolTipUI {

    private final ExpandableTooltip et;
    private JEditorPane editorPane; // Editor pane the tooltip is showing on

    ToolTipUI(String toolTipText, Expandable expandable, Pinnable pinnable) {
        et = new ExpandableTooltip(toolTipText, expandable != null, pinnable != null);
        if (expandable != null) {
            et.addExpansionListener(new ExpansionListener(expandable));
        }
        if (pinnable != null) {
            et.addPinListener(new PinListener(pinnable));
        }
    }

    /**
     * Show the tooltip on the given editor pane.
     * @param editorPane The editor pane to show the tooltip on.
     * @return An instance of tooltip support that allows to control the display
     * of the tooltip, or <code>null</code> when it's not possible to show the
     * tooltip on the editor pane.
     * It can be used e.g. to close the tooltip when it's no longer applicable,
     * when the debugger resumes, etc.
     */
    public ToolTipSupport show(JEditorPane editorPane) {
        EditorUI eui = Utilities.getEditorUI(editorPane);
        if (eui != null) {
            ToolTipSupport toolTipSupport = eui.getToolTipSupport();
            toolTipSupports.setToolTip(
                    et,
                    PopupManager.ViewPortBounds,
                    PopupManager.AbovePreferred,
                    0, 0,
                    ToolTipSupport.FLAGS_HEAVYWEIGHT_TOOLTIP
            );

            this.editorPane = editorPane;
            return toolTipSupport;
        } else {
            return null;
        }
    }

    /**
     * Description of an expandable tooltip.
     * The expanded tooltip is a view created from models registered under
     * <code>ToolTipView</code> name. Default model filters are provided, which define
     * the expanded first node with <code>expression</code> name and <code>variable</code>
     * being the node's value.
     */
    public static final class Expandable {
        
        private final String expression;
        private final Object variable;
        
        /**
         * Create a description of expandable tooltip.
         * @param expression the tooltip's expression
         * @param variable the evaluated variable
         */
        public Expandable(String expression, Object variable) {
            this.expression = expression;
            this.variable = variable;
        }
    }

    /**
     * Description of a pinnable watch.
     * The pin watch is created from the provided parameters.
     */
    public static final class Pinnable {
        
        private final String expression;
        private final int line;
        private final String valueProviderId;
        
        /**
         * Create a description of pin watch
         * @param expression The pin watch expression
         * @param line Line to create the pin watch at
         * @param valueProviderId ID of the registered {@link PinWatchUISupport.ValueProvider value provider}
         */
        public Pinnable(String expression, int line, String valueProviderId) {
            this.expression = expression;
            this.line = line;
            this.valueProviderId = valueProviderId;
        }
    }

    private class ExpansionListener implements ActionListener {

        private final Expandable expandable;
        
        ExpansionListener(Expandable expandable) {
            this.expandable = expandable;
        }
        
        @Override
        public void actionPerformed(ActionEvent e) {
            et.setBorder(BorderFactory.createLineBorder(et.getForeground()));
            et.removeAll();
            et.setWidthCheck(false);
            final ToolTipView ttView = ToolTipView.createToolTipView(
                    expandable.expression,
                    expandable.variable);
            et.add(ttView);
            et.revalidate();
            et.repaint();
            SwingUtilities.invokeLater(() -> {
                EditorUI eui = Utilities.getEditorUI(editorPane);
                if (eui != null) {
                    eui.getToolTipSupport().setToolTip(et, PopupManager.ViewPortBounds,
                                                       PopupManager.AbovePreferred, 0, 0,
                                                       ToolTipSupport.FLAGS_HEAVYWEIGHT_TOOLTIP);
                } else {
                    throw new IllegalStateException("Have no EditorUI for "+editorPane);
                }
            });
        }
    }

    private class PinListener implements ActionListener {
        
        private final Pinnable pinnable;

        public PinListener(Pinnable pinnable) {
            this.pinnable = pinnable;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            EditorUI eui = Utilities.getEditorUI(editorPane);
            Point location = et.getLocation();
            location = eui.getStickyWindowSupport().convertPoint(location);
            eui.getToolTipSupport().setToolTipVisible(false);
            DebuggerManager dbMgr = DebuggerManager.getDebuggerManager();
            BaseDocument document = Utilities.getDocument(editorPane);
            DataObject dobj = (DataObject) document.getProperty(Document.StreamDescriptionProperty);
            FileObject fo = dobj.getPrimaryFile();
            Watch.Pin pin = new EditorPin(fo, pinnable.line, location);
            final Watch w = dbMgr.createPinnedWatch(pinnable.expression, pin);
            SwingUtilities.invokeLater(() -> {
                try {
                    PinWatchUISupport.getDefault().pin(w, pinnable.valueProviderId);
                } catch (IllegalArgumentException ex) {
                    Exceptions.printStackTrace(ex);
                }
            });
        }
    }

}
