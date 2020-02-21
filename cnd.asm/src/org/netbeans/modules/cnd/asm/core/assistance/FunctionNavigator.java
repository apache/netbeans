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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cnd.asm.core.assistance;

import java.io.IOException;
import java.util.Collection;
import javax.swing.JEditorPane;
import javax.swing.SwingUtilities;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;

import org.netbeans.spi.navigator.NavigatorPanel;
import org.openide.util.Lookup;
import org.openide.loaders.DataObject;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.text.CloneableEditorSupport;

import org.netbeans.modules.cnd.asm.core.dataobjects.AsmDataObject;
import org.netbeans.modules.cnd.asm.core.dataobjects.AsmObjectUtilities;
import org.netbeans.modules.cnd.asm.core.ui.top.NavigatorUI;
import org.netbeans.modules.cnd.asm.model.AsmModelAccessor;
import org.netbeans.modules.cnd.asm.model.AsmModelAccessor.ParseListener;
import org.openide.cookies.EditorCookie;

public class FunctionNavigator implements NavigatorPanel {

    private NavigatorUI panelUI;
    private Lookup.Result<AsmDataObject> result;
    private final Lookup.Template<AsmDataObject> asmTemplate;
    private AsmDataObject curDataObject;
    private StateListener stateListener;
    private LookupListener lookupListener;

    public FunctionNavigator() {
        asmTemplate = new Lookup.Template<AsmDataObject>(AsmDataObject.class);
    }

    public String getDisplayName() {
        return "";
    }

    public String getDisplayHint() {
        return "";
    }

    public NavigatorUI getComponent() {
        if (panelUI == null) {
            panelUI = new NavigatorUI();
        }

        return panelUI;
    }

    public void panelActivated(Lookup context) {
        result = context.lookup(asmTemplate);
        result.addLookupListener(getLookupListener());
        setContent(result);
    }

    public void panelDeactivated() {
        result.removeLookupListener(getLookupListener());
        runInEDT(new Runnable() {
            public void run() {
                unsubscribe();
            }
        });
    }

    public Lookup getLookup() {
        return null;
    }

    private synchronized LookupListener getLookupListener() {
        if (lookupListener == null) {
            lookupListener = new ContextListener();
        }

        return lookupListener;
    }

    private synchronized StateListener getStateListener() {
        if (stateListener == null) {
            stateListener = new StateListener();
        }

        return stateListener;
    }

    private AsmModelAccessor getAccessor(DataObject dob) {
        if (dob == null) {
            return null;
        }

        return AsmObjectUtilities.getAccessor(dob);
    }

    private void unsubscribe() {
        if (curDataObject != null) {
            AsmModelAccessor acc = getAccessor(curDataObject);
            if (acc != null) {
                acc.removeParseListener(getStateListener());
            }
            JEditorPane pane = getJEditorPane(curDataObject);
            if (pane != null) {
                pane.removeCaretListener(getStateListener());
            }
            curDataObject = null;
        }
    }

    private void subscribe(AsmDataObject dob) {
        curDataObject = dob;

        if (curDataObject != null) {
            try {
                final EditorCookie ec = dob.getCookie(EditorCookie.class);
                if (ec == null) {
                    return;
                }

                ec.openDocument();

                AsmModelAccessor acc = getAccessor(curDataObject);
                if (acc != null) {
                    acc.addParseListener(getStateListener());
                }
                JEditorPane pane = getJEditorPane(curDataObject);
                if (pane != null) {
                    pane.addCaretListener(getStateListener());
                }
            } catch (IOException ex) {
                return;
            }
        }
    }

    private void setContent(Lookup.Result<AsmDataObject> result) {
        Collection<? extends AsmDataObject> dobs = result.allInstances();

        Runnable action;

        if (dobs.size() != 0) {
            final AsmDataObject dob = (dobs.iterator().next());

            if (curDataObject == dob) {
                return;
            }

            action = new Runnable() {

                public void run() {
                    unsubscribe();
                    subscribe(dob);
                    update();
                }
            };

        } else {
            action = new Runnable() {

                public void run() {
                    unsubscribe();
                    update();
                }
            };
        }

        runInEDT(action);
    }

    private void update() {
        final AsmModelAccessor acc = getAccessor(curDataObject);

        if (acc == null) {
            return;
        }

        getComponent().update(curDataObject, acc.getState());
    }

    private void updateCursor(final int pos) {
        getComponent().updateCursor(pos);
    }

    private void runInEDT(Runnable run) {
        SwingUtilities.invokeLater(run);
    }

    private JEditorPane getJEditorPane(DataObject dob) {
        JEditorPane currentJEditorPane = null;
        if (dob == null) {
            return null;
        }

        CloneableEditorSupport support = dob.getLookup().
                lookup(CloneableEditorSupport.class);
        if (support != null) {
            JEditorPane[] jEditorPanes = support.getOpenedPanes();
            if (jEditorPanes == null) {
                return null;
            }
            if (jEditorPanes.length >= 1) {
                currentJEditorPane = jEditorPanes[0];
            }
        }
        return currentJEditorPane;
    }

    private class ContextListener implements LookupListener {

        @SuppressWarnings("unchecked")
        public void resultChanged(LookupEvent ev) {
            setContent((Lookup.Result<AsmDataObject>) ev.getSource());
        }
    }

    private class StateListener implements ParseListener,
            CaretListener {

        public void caretUpdate(final CaretEvent e) {
            runInEDT(new Runnable() {

                public void run() {
                    updateCursor(e.getDot());
                }
            });
        }

        public void notifyParsed() {
            runInEDT(new Runnable() {

                public void run() {
                    update();
                }
            });
        }
    }
}
