/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.core.multiview;

import java.awt.FlowLayout;
import java.awt.GraphicsEnvironment;
import java.io.Serializable;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import java.util.prefs.Preferences;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.netbeans.core.api.multiview.MultiViewHandler;
import org.netbeans.core.api.multiview.MultiViewPerspective;
import org.netbeans.core.api.multiview.MultiViews;
import org.netbeans.core.spi.multiview.CloseOperationState;
import org.netbeans.core.spi.multiview.MultiViewElement;
import org.netbeans.core.spi.multiview.MultiViewElementCallback;
import org.netbeans.junit.NbTestCase;
import org.openide.awt.UndoRedo;
import org.openide.text.CloneableEditorSupport;
import org.openide.util.Lookup;
import org.openide.util.NbPreferences;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;
import org.openide.windows.CloneableTopComponent;
import org.openide.windows.TopComponent;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public class ToolbarVisibleTest extends NbTestCase 
implements Lookup.Provider, Serializable {

    public static Test suite() {
        return GraphicsEnvironment.isHeadless() ? new TestSuite() : new TestSuite(ToolbarVisibleTest.class);
    }

    private static final Preferences editorSettingsPreferences;
    static {
        final String n = "test.toolbar.visible";
        System.setProperty("test.multiview.toolbar.settings", n);
        editorSettingsPreferences = NbPreferences.root().node(n);
    }
    InstanceContent ic = new InstanceContent();
    Lookup lkp = new AbstractLookup(ic);

    public ToolbarVisibleTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        clearWorkDir();
        MVE.allToolbars.clear();
        editorSettingsPreferences.putBoolean("toolbarVisible", true);
    }
    
    
    @Override
    public Lookup getLookup() {
        return lkp;
    }
    
    public void testToolbarIsControlledByEditorSettings() throws Exception {
        doToolbarCheck(2);
        
    }
    public void testToolbarIsControlledByEditorSettingsWhenNonEditorSelected() throws Exception {
        doToolbarCheck(0);
    }
    
    private void doToolbarCheck(int select) throws Exception {
        CloneableTopComponent tc = MultiViews.createCloneableMultiView("text/toolbar", this);
        tc.open();
        assertVisible("MultiView visible", tc);
        
        assertEquals("One toolbar exists: " + MVE.allToolbars, 1, MVE.allToolbars.size());
        
        MultiViewHandler handle = MultiViews.findMultiViewHandler(tc);
        final MultiViewPerspective[] perspectives = handle.getPerspectives();
        assertEquals("Four perspectives", 4, perspectives.length);
        handle.requestVisible(perspectives[2]);
        
        assertEquals("two toolbars exist", 2, MVE.allToolbars.size());
        
        for (int i = 0; i < 2; i++) {
            handle.requestVisible(perspectives[i == 0 ? i : i + 1]);
            assertVisible("Toolbar is showing(" + i +")", MVE.allToolbars.get(i));
        }
        
        handle.requestVisible(perspectives[select]);
        
        W waiter = new W();
        editorSettingsPreferences.addPreferenceChangeListener(waiter);
        editorSettingsPreferences.putBoolean("toolbarVisible", false);
        waiter.waitThree();
        
        for (int i = 0; i < 2; i++) {
            handle.requestVisible(perspectives[i == 0 ? i : i + 1]);
            assertFalse("No Toolbar is showing anymore", MVE.allToolbars.get(i).isShowing());
        }
    }

    private void assertVisible(String msg, JComponent tc) throws InterruptedException {
        int cnt = 0;
        while (!tc.isShowing()) {
            Thread.sleep(100);
            if (cnt++ > 10) {
                break;
            }
        }
        assertTrue(msg + ". Showing", tc.isShowing());
    }

    
    
    static class MVE extends JPanel implements MultiViewElement {
        static CloseOperationState closeState;
        static List<JPanel> allToolbars = new CopyOnWriteArrayList<JPanel>();
        
        private JPanel toolbar = new JPanel();
        
        public MVE() {
            allToolbars.add(toolbar);
        }
        
        @Override
        public JComponent getVisualRepresentation() {
            return this;
        }

        @Override
        public JComponent getToolbarRepresentation() {
            return toolbar;
        }

        @Override
        public Action[] getActions() {
            return new Action[0];
        }

        @Override
        public Lookup getLookup() {
            return Lookup.EMPTY;
        }

        @Override
        public void componentOpened() {
        }

        @Override
        public void componentClosed() {
        }

        @Override
        public void componentShowing() {
        }

        @Override
        public void componentHidden() {
        }

        @Override
        public void componentActivated() {
        }

        @Override
        public void componentDeactivated() {
        }

        @Override
        public UndoRedo getUndoRedo() {
            return UndoRedo.NONE;
        }

        @Override
        public void setMultiViewCallback(MultiViewElementCallback callback) {
        }

        @Override
        public CloseOperationState canCloseElement() {
            if (closeState != null) {
                return closeState;
            }
            return CloseOperationState.STATE_OK;
        }
    } // end of MVE
    
    @MultiViewElement.Registration(
        displayName="Contextual",
        mimeType="text/toolbar",
        persistenceType=TopComponent.PERSISTENCE_ALWAYS,
        preferredID="context"
    )
    public static class CntxMVE extends MVE {
        private Lookup context;
        public CntxMVE(Lookup context) {
            this.context = context;
        }
        public CntxMVE() {
        }

        @Override
        public Lookup getLookup() {
            return context;
        }
    } // end of CntxMVE

    @MultiViewElement.Registration(
        displayName="Source",
        mimeType="text/toolbar",
        persistenceType=TopComponent.PERSISTENCE_NEVER,
        preferredID="source"
    )
    public static class SourceMVC extends MVE implements CloneableEditorSupport.Pane {
        private final JEditorPane pane = new JEditorPane();
        private final CloneableTopComponent tc = new CloneableTopComponent() {{
           setLayout(new FlowLayout());
           add(pane);
        }};
        
        @Override
        public JEditorPane getEditorPane() {
            return pane;
        }

        @Override
        public CloneableTopComponent getComponent() {
            return tc;
        }

        @Override
        public void updateName() {
        }

        @Override
        public void ensureVisible() {
        }
    }
    class W implements PreferenceChangeListener, Runnable {

        int stage;

        @Override
        public void preferenceChange(PreferenceChangeEvent evt) {
            SwingUtilities.invokeLater(this);
        }

        @Override
        public synchronized void run() {
            if (stage++ < 3) {
                SwingUtilities.invokeLater(this);
            } else {
                notifyAll();
            }
        }

        public synchronized void waitThree() throws InterruptedException {
            while (stage < 3) {
                wait();
            }
        }
    }
    
}
