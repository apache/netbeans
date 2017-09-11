/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.java.hints.declarative.debugging;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.util.Collections;
import java.util.Set;
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
import org.openide.util.WeakSet;
import org.openide.util.actions.Presenter;

/**
 *
 * @author lahvac
 */
public class ToggleDebuggingAction extends BaseAction implements Presenter.Toolbar, ContextAwareAction {

    public static final String toggleDebuggingAction = "toggle-debugging-action";
    static final long serialVersionUID = 0L;

    static final Set<Document> debuggingEnabled = Collections.synchronizedSet(new WeakSet<Document>());
    static final Set<ToggleDebuggingAction> actions = Collections.synchronizedSet(new WeakSet<ToggleDebuggingAction>());
    
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
