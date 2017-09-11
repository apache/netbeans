/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package org.netbeans.modules.debugger.jpda.ui.models;

import org.netbeans.api.debugger.DebuggerEngine;
import org.netbeans.spi.viewmodel.NodeActionsProviderFilter;
import org.netbeans.spi.viewmodel.NodeActionsProvider;
import org.netbeans.spi.viewmodel.UnknownTypeException;
import org.netbeans.spi.viewmodel.ModelListener;
import org.netbeans.api.debugger.Session;
import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.spi.debugger.ContextProvider;

import org.openide.util.actions.Presenter;
import org.openide.util.NbBundle;

import javax.swing.*;
import java.util.*;
import java.awt.event.ActionEvent;
import org.netbeans.spi.debugger.DebuggerServiceRegistration;
import org.openide.awt.Mnemonics;

/**
 * Provides popup menu for JPDA session nodes: suspend options and language selection. 
 *
 * @author Maros Sandor
 */
@DebuggerServiceRegistration(path="SessionsView", types=NodeActionsProviderFilter.class)
public class JPDASessionActionsProvider implements NodeActionsProviderFilter {

    private HashSet         listeners;
    

    public JPDASessionActionsProvider () {
    }

    public void performDefaultAction(NodeActionsProvider original, Object node) throws UnknownTypeException {
        original.performDefaultAction(node);
    }

    public Action [] getActions(NodeActionsProvider original, Object node) throws UnknownTypeException {

        if (!(node instanceof Session) || !SessionsTableModelFilter.isJPDASession((Session) node)) {
            return original.getActions(node);
        }
        Session session = (Session) node;
        Action [] actions;
        try {
            actions = original.getActions(node);
        } catch (UnknownTypeException e) {
            actions = new Action[0];
        }
        List myActions = new ArrayList();
        if (actions.length > 0) {
            myActions.add(actions[0]); // Keep the first action as the first one.
        }
        DebuggerEngine e = session.getCurrentEngine ();
        if (e != null) {
            JPDADebugger d = e.lookupFirst(null, JPDADebugger.class);
            myActions.add(new CustomizeSession(d));
        }
        myActions.add(new LanguageSelection(session));
        for (int i = 1; i < actions.length; i++) {
            myActions.add(actions[i]);
        }
        return (Action[]) myActions.toArray(new Action[myActions.size()]);
    }

    private String localize(String s) {
        s = NbBundle.getBundle(JPDASessionActionsProvider.class).getString(s);
        int ampIndex = Mnemonics.findMnemonicAmpersand(s);
        if (ampIndex >= 0) {
            s = s.substring(0, ampIndex) + s.substring(ampIndex+1);
        }
        return s;
    }

    private class LanguageSelection extends AbstractAction implements Presenter.Popup {

        private Session session;

        public LanguageSelection(Session session) {
            this.session = session;
        }

        public void actionPerformed(ActionEvent e) {
        }

        public JMenuItem getPopupPresenter() {
            JMenu displayAsPopup = new JMenu(localize("CTL_Session_Popup_Language"));

            String [] languages = session.getSupportedLanguages();
            String currentLanguage = session.getCurrentLanguage();
            for (int i = 0; i < languages.length; i++) {
                final String language = languages[i];
                JRadioButtonMenuItem langItem = new JRadioButtonMenuItem(new AbstractAction(language) {
                    public void actionPerformed(ActionEvent e) {
                        session.setCurrentLanguage(language);
                    }
                });
                if (currentLanguage.equals(language)) langItem.setSelected(true);
                displayAsPopup.add(langItem);
            }
            return displayAsPopup;
        }
    }

    private class CustomizeSession extends AbstractAction implements Presenter.Popup {

        private JPDADebugger dbg;

        public CustomizeSession(JPDADebugger dbg) {
            this.dbg = dbg;
        }

        public void actionPerformed(ActionEvent e) {
        }

        public JMenuItem getPopupPresenter() {
            JMenu displayAsPopup = new JMenu 
                (localize ("CTL_Session_Resume_Threads"));

            JRadioButtonMenuItem resumeAllItem = new JRadioButtonMenuItem (
                new AbstractAction (localize ("CTL_Session_Resume_All_Threads")
            ) {
                public void actionPerformed (ActionEvent e) {
                    dbg.setSuspend (JPDADebugger.SUSPEND_ALL);
                }
            });
            JRadioButtonMenuItem resumeCurrentItem = new JRadioButtonMenuItem (
                new AbstractAction (localize ("CTL_Session_Resume_Current_Thread")
            ) {
                public void actionPerformed(ActionEvent e) {
                    dbg.setSuspend (JPDADebugger.SUSPEND_EVENT_THREAD);
                }
            });

            if (dbg.getSuspend() == JPDADebugger.SUSPEND_ALL) 
                resumeAllItem.setSelected(true);
            else resumeCurrentItem.setSelected(true);

            displayAsPopup.add(resumeAllItem);
            displayAsPopup.add(resumeCurrentItem);
            return displayAsPopup;
        }
    }

    public void addModelListener(ModelListener l) {
        HashSet newListeners = (listeners == null) ? new HashSet() : (HashSet) listeners.clone();
        newListeners.add(l);
        listeners = newListeners;
    }

    public void removeModelListener(ModelListener l) {
        if (listeners == null) return;
        HashSet newListeners = (HashSet) listeners.clone();
        newListeners.remove(l);
        listeners = newListeners;
    }

}
