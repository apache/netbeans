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
package org.netbeans.spi.debugger.ui;

import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JEditorPane;
import javax.swing.UIManager;
import org.netbeans.editor.EditorUI;
import org.netbeans.editor.Utilities;
import org.netbeans.editor.ext.ToolTipSupport;
import org.netbeans.modules.debugger.ui.views.ToolTipView;

/**
 * A base class for an action that shows an expanded tooltip.
 * It can be used as a
 * {@link PinWatchUISupport.ValueProvider#getHeadActions(org.netbeans.api.debugger.Watch) head action}
 * for a pin watch to display the structured value of a watch.
 * 
 * @since 2.54
 * @author Martin Entlicher
 */
public abstract class AbstractExpandToolTipAction extends AbstractAction {

    private final Icon toExpandIcon = UIManager.getIcon ("Tree.collapsedIcon"); // NOI18N
    private final Icon toCollapsIcon = UIManager.getIcon ("Tree.expandedIcon"); // NOI18N
    private boolean expanded;

    /**
     * Create a new expand tooltip action.
     */
    protected AbstractExpandToolTipAction() {
        putValue(Action.SMALL_ICON, toExpandIcon);
        putValue(Action.LARGE_ICON_KEY, toExpandIcon);
    }

    /**
     * Open a tooltip view.
     * Call {@link #openTooltipView(java.lang.String, java.lang.Object)} method
     * to open the tooltip view.
     */
    protected abstract void openTooltipView();

    /**
     * Open a tooltip view for the expression and variable the expression evaluates to.
     * @param expression the tooltip's expression
     * @param var the evaluated variable
     * @return An instance of tooltip support that allows to control the display
     * of the tooltip, or <code>null</code> when it's not possible to show it.
     * It can be used e.g. to close the tooltip when it's no longer applicable,
     * when the debugger resumes, etc.
     */
    protected final ToolTipSupport openTooltipView(String expression, Object var) {
        ToolTipView toolTipView = ToolTipView.createToolTipView(expression, var);
        JEditorPane currentEditor = EditorContextDispatcher.getDefault().getMostRecentEditor();
        EditorUI eui = Utilities.getEditorUI(currentEditor);
        if (eui != null) {
            final ToolTipSupport toolTipSupport = eui.getToolTipSupport();
            toolTipSupport.setToolTipVisible(true, false);
            toolTipSupport.setToolTip(toolTipView);
            toolTipSupport.addPropertyChangeListener(new PropertyChangeListener() {
                @Override
                public void propertyChange(PropertyChangeEvent evt) {
                    if (ToolTipSupport.PROP_STATUS.equals(evt.getPropertyName())) {
                        if (!toolTipSupport.isToolTipVisible()) {
                            expanded = false;
                            putValue(Action.SMALL_ICON, toExpandIcon);
                            putValue(Action.LARGE_ICON_KEY, toExpandIcon);
                            toolTipSupport.removePropertyChangeListener(this);
                        }
                    }
                }
            });
            return toolTipSupport;
        } else {
            return null;
        }
    }

    @Override
    public final void actionPerformed(ActionEvent e) {
        expanded = !expanded;
        if (expanded) {
            openTooltipView();
            putValue(Action.SMALL_ICON, toCollapsIcon);
            putValue(Action.LARGE_ICON_KEY, toCollapsIcon);
        } else {
            hideTooltipView();
            putValue(Action.SMALL_ICON, toExpandIcon);
            putValue(Action.LARGE_ICON_KEY, toExpandIcon);
        }
    }

    private void hideTooltipView() {
        JEditorPane currentEditor = EditorContextDispatcher.getDefault().getMostRecentEditor();
        EditorUI eui = Utilities.getEditorUI(currentEditor);
        if (eui != null) {
            eui.getToolTipSupport().setToolTipVisible(false, false);
        }
    }
}
