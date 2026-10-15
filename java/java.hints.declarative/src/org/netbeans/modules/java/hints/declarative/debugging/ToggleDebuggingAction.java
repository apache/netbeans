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
package org.netbeans.modules.java.hints.declarative.debugging;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JEditorPane;
import javax.swing.JToggleButton;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.editor.BaseAction;
import org.openide.util.ContextAwareAction;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.actions.Presenter;

/**
 *
 * @author lahvac
 */
public class ToggleDebuggingAction extends BaseAction implements Presenter.Toolbar, ContextAwareAction {

    public static final String toggleDebuggingAction = "toggle-debugging-action";
    static final long serialVersionUID = 0L;

    static final Set<Document> debuggingEnabled = Collections.synchronizedSet(Collections.newSetFromMap(new WeakHashMap<>()));
    static final Set<ToggleDebuggingAction> actions = Collections.synchronizedSet(Collections.newSetFromMap(new WeakHashMap<>()));
    
    private JEditorPane pane;

    private JToggleButton toggleButton;

    public ToggleDebuggingAction() {
        super(toggleDebuggingAction);
        putValue(Action.SMALL_ICON, ImageUtilities.loadImageIcon("org/netbeans/modules/java/hints/declarative/resources/toggle-debugging.png", false)); //NOI18N
        putValue("noIconInMenu", Boolean.TRUE); // NOI18N
    }
    
    public ToggleDebuggingAction(JEditorPane pane) {
        this();
        
        assert (pane != null);
        this.pane = pane;
        actions.add(this);
        updateState();
    }

    private void updateState() {
        if (pane != null && toggleButton != null) {
            boolean debugging = debuggingEnabled.contains(pane.getDocument());
            toggleButton.setSelected(debugging);
            toggleButton.setContentAreaFilled(debugging);
            toggleButton.setBorderPainted(debugging);
        }
    }

    @Override
    public void actionPerformed(ActionEvent evt, JTextComponent target) {
        if (target != null && !Boolean.TRUE.equals(target.getClientProperty("AsTextField"))) {
            Document doc = target.getDocument();
            if (debuggingEnabled.contains(doc)) debuggingEnabled.remove(doc);
            else debuggingEnabled.add(doc);
            for (ToggleDebuggingAction a : actions) {
                a.updateState();
            }
        }
    }

    @Override
    public Component getToolbarPresenter() {
        toggleButton = new JToggleButton();
        toggleButton.putClientProperty("hideActionText", Boolean.TRUE); //NOI18N
        toggleButton.setIcon((Icon) getValue(SMALL_ICON));
        toggleButton.setAction(this); // this will make hard ref to button => check GC
        return toggleButton;
    }

    @Override
    public Action createContextAwareInstance(Lookup actionContext) {
        JEditorPane pane = actionContext.lookup(JEditorPane.class);
        if (pane != null) {
            return new ToggleDebuggingAction(pane);
        }
        return this;
    }

    @Override
    protected Class getShortDescriptionBundleClass() {
        return ToggleDebuggingAction.class;
    }

}
