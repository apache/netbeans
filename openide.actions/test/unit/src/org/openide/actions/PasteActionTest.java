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

package org.openide.actions;

import java.awt.GraphicsEnvironment;
import java.awt.event.ActionEvent;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.logging.Level;
import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.netbeans.junit.Log;
import org.openide.awt.DynamicMenuContent;
import org.openide.util.actions.CallbackSystemAction;
import org.openide.util.actions.Presenter;
import org.openide.windows.TopComponent;

/** Test behaviour of PasteAction intogether with clonning.
 */
public class PasteActionTest extends AbstractCallbackActionTestHidden {
    
    public static Test suite() {
        return GraphicsEnvironment.isHeadless() ? new TestSuite() : new TestSuite(PasteActionTest.class);
    }

    public PasteActionTest(String name) {
        super(name);
    }
    
    protected Class<? extends CallbackSystemAction> actionClass() {
        return PasteAction.class;
    }
    
    protected String actionKey () {
        return javax.swing.text.DefaultEditorKit.pasteAction;
    }

    public void testListenersAreUnregisteredBug32073 () throws Exception {
        action.assertListeners ("When we created clone, we added a listener", 1);
        
        Reference<?> ref = new WeakReference<Object>(clone);
        clone = null;
        assertGC ("Clone can disappear", ref);
        action.assertListeners ("No listeners, as the clone has been GCed", 0);
    }
    
    public void testPresenterCanBeGCedIssue47314 () throws Exception {
        javax.swing.JMenuItem item = ((Presenter.Popup) clone).getPopupPresenter();
        
        Reference<?> itemref = new WeakReference<Object>(item);
        item = null;
        Reference<?> ref = new WeakReference<Object>(clone);
        clone = null;
        assertGC ("Item can disappear", itemref);
        assertGC ("Clone can disappear", ref);
    }
    
    
    public void testDelegatesAsOneAction() {
        OurAction[] arr = {
            new OurAction ()
        };

        action.setEnabled (true);
        assertTrue ("Now is the action enabled", clone.isEnabled());
        listener.assertCnt ("No change fired as it was enabled by default", 0);
        
        arr[0].setEnabled (false);
        action.putValue ("delegates", arr);
        
        assertTrue ("Clone should be disabled as the only action we delegate is", !clone.isEnabled ());
        listener.assertCnt ("That means one change should be fired", 1);
        
        action.setEnabled (false);
        assertTrue ("No influence on enabled state in delegate mode", !clone.isEnabled ());
        listener.assertCnt ("No changes fired", 0);
        
        arr[0].setEnabled (true);
        assertTrue ("State of clone changed to be enabled", clone.isEnabled ());
        listener.assertCnt ("Change fired", 1);

        arr[0].setEnabled (false);
        assertTrue ("Disabled again", !clone.isEnabled ());
        listener.assertCnt ("Changed delivered", 1);
        
        action.putValue ("delegates", null);
        assertTrue ("Still disabled, because action itself is disabled", !clone.isEnabled ());
        listener.assertCnt ("No changes due to that", 0);
        
        action.setEnabled (true);
        assertTrue ("Now we are listening to just the one action", clone.isEnabled ());
        listener.assertCnt ("And that is why we should be enabled with one event change", 1);
        
        arr[0].setEnabled (false);
        action.putValue ("delegates", arr);
        assertTrue ("Now we have delegates again, thus we are disabled", !clone.isEnabled ());
        listener.assertCnt ("One change delivered", 1);
    }
    
    public void testDelegatesAsArrayOfAction () throws Exception {
        OurAction[] arr = {
            new OurAction ()
        };
        action.putValue ("delegates", arr);
        //arr[0].setEnabled (true);
        
        TopComponent tc = new TopComponent();
        tc.getActionMap ().put(javax.swing.text.DefaultEditorKit.pasteAction, action);
        tc.requestActive();
        
        CharSequence log = Log.enable(PasteAction.class.getName(), Level.WARNING);
        global.actionPerformed (new ActionEvent (this, 0, "waitFinished"));
        assertEquals("Log is empty", "", log.toString());
        
        arr[0].assertCnt ("Performed on delegate", 1);
        action.assertCnt ("Not performed on action", 0);
    }
    
    public void testDelegatesAsArrayOfPasteType () throws Exception {
        OurPasteType [] arr = {
            new OurPasteType ()
        };
        action.putValue ("delegates", arr);
        //arr[0].setEnabled (true);
        
        TopComponent tc = new TopComponent();
        tc.getActionMap ().put(javax.swing.text.DefaultEditorKit.pasteAction, action);
        tc.requestActive();
        global.actionPerformed (new ActionEvent (this, 0, "waitFinished"));
        
        action.assertCnt ("Not performed on action", 0);
        arr[0].assertCnt ("Performed on delegate", 1);
    }
    
    public void testDelegatesAsMoreActions () throws Exception {
        action.setEnabled (false);
        listener.assertCnt ("One changed now", 1);
        
        OurAction[] arr = {
            new OurAction (),
            new OurAction ()
        };
        
        
        action.putValue ("delegates", arr);
        assertTrue ("Enabled because it has more than one action", clone.isEnabled ());
        listener.assertCnt ("One changes since that", 1);
        
        action.putValue ("delegates", new Object[0]);
        assertTrue ("Disabled as no delegates", !clone.isEnabled ());
        listener.assertCnt ("One changes since that", 1);

        action.putValue ("delegates", arr);
        assertTrue ("Enabled again", clone.isEnabled ());
        
        
        clone.actionPerformed (new java.awt.event.ActionEvent (this, 0, "waitFinished"));
        arr[0].assertCnt ("First delegate invoked", 1);
    }
    
    public void testDelegatesAsMorePasteTypes () throws Exception {
        action.setEnabled (false);
        listener.assertCnt ("One changed now", 1);
        
        OurPasteType[] arr = {
            new OurPasteType(),
            new OurPasteType()
        };
        
        
        action.putValue ("delegates", arr);
        assertTrue ("Enabled because it has more than one action", clone.isEnabled ());
        listener.assertCnt ("One changes since that", 1);
        
        action.putValue ("delegates", new Object[0]);
        assertTrue ("Disabled as no delegates", !clone.isEnabled ());
        listener.assertCnt ("One changes since that", 1);

        action.putValue ("delegates", arr);
        assertTrue ("Enabled again", clone.isEnabled ());
        
        clone.actionPerformed (new java.awt.event.ActionEvent (this, 0, "waitFinished"));
        arr[0].assertCnt ("First delegate invoked", 1);

        arr = new OurPasteType[] { new OurPasteType () };
        
        action.putValue ("delegates", arr);
        assertTrue ("Enabled still", clone.isEnabled ());
        
        clone.actionPerformed (new java.awt.event.ActionEvent (this, 0, "waitFinished"));
        arr[0].assertCnt ("First delegate invoked", 1);
    }
    
    public void testDisableIsOk() throws Exception {
        PasteAction p = PasteAction.get(PasteAction.class);
        
        class A extends AbstractAction {
            public void actionPerformed(ActionEvent e) {
            }
        }
        A a = new A();
        a.setEnabled(false);
//        action.putValue("delegates", new A[0]);
        
        TopComponent td = new TopComponent();
        td.getActionMap().put(javax.swing.text.DefaultEditorKit.pasteAction, a);
        
        td.requestActive();
        
        assertFalse("Disabled", p.isEnabled());
        JMenuItem item = p.getMenuPresenter();
        assertTrue("Dynamic one: " + item, item instanceof DynamicMenuContent);
        DynamicMenuContent d = (DynamicMenuContent)item;
        JComponent[] items = d.getMenuPresenters();
        items = d.synchMenuPresenters(items);
        assertEquals("One item", 1, items.length);
        assertTrue("One jmenu item", items[0] instanceof JMenuItem);
        JMenuItem one = (JMenuItem)items[0];
        assertFalse("And is disabled", one.getModel().isEnabled());
    }
    
    private static final class OurPasteType extends org.openide.util.datatransfer.PasteType {
        private int cnt;
        
        public java.awt.datatransfer.Transferable paste() throws java.io.IOException {
            cnt++;
            return null;
        }
        
        public void assertCnt (String msg, int count) {
            assertEquals (msg, count, this.cnt);
            this.cnt = 0;
        }
    } // end of OurPasteType
    
}
