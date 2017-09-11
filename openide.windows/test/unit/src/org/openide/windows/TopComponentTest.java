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

package org.openide.windows;

import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.ObjectInputStream;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.logging.Level;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.text.Keymap;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.netbeans.junit.Log;
import org.netbeans.junit.MockServices;
import org.netbeans.junit.NbTestCase;
import org.openide.awt.Actions;
import org.openide.nodes.Node;
import org.openide.util.ContextAwareAction;
import org.openide.util.HelpCtx;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.actions.CookieAction;
import org.openide.util.io.NbMarshalledObject;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author mkleint
 */
public class TopComponentTest extends NbTestCase {

    public static Test suite() {
        return GraphicsEnvironment.isHeadless() ? new TestSuite() : new TestSuite(TopComponentTest.class);
    }

    static {
        System.setProperty("org.openide.windows.DummyWindowManager.VISIBLE", "false");
    }
    
    public TopComponentTest(String testName) {
        super(testName);
    }
    
    public void testPreferredIDAndIconTakenFromAnnotation() {
        @TopComponent.Description(
            preferredID="my.id", iconBase="org/openide/windows/icon.png",
            persistenceType=TopComponent.PERSISTENCE_ONLY_OPENED    
        )
        class MyTC extends TopComponent {
        }
        TopComponent tc = new MyTC();
        
        assertEquals("ID is OK", "my.id", tc.preferredID());
        Image image = ImageUtilities.loadImage("org/openide/windows/icon.png");
        assertEquals("Image is OK", image, tc.getIcon());
        
        assertEquals("Only opened", TopComponent.PERSISTENCE_ONLY_OPENED, tc.getPersistenceType());
    }

    public void testCanTCGarbageCollectWhenActionInMap() {
        TopComponent tc = new TopComponent();
        class CAA extends AbstractAction implements
                ContextAwareAction {
            public void actionPerformed(ActionEvent arg0) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            public Action createContextAwareInstance(Lookup actionContext) {
                return this;
            }

        }
        ContextAwareAction del = new CAA();
        ContextAwareAction context = Actions.context(Integer.class, true, true, del, null, "DisplayName", null, true);
        Action a = context.createContextAwareInstance(tc.getLookup());
        tc.getActionMap().put("key", a);

        WeakReference<Object> ref = new WeakReference<Object>(tc);
        tc = null;
        a = null;
        del = null;
        context = null;
        assertGC("Can the component GC?", ref);
    }
    public void testCanTCGarbageCollectWhenActionInMapAndAssignLookup() {
        TopComponent tc = new TopComponent();
        class CAA extends AbstractAction implements
                ContextAwareAction {
            public void actionPerformed(ActionEvent arg0) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            public Action createContextAwareInstance(Lookup actionContext) {
                return this;
            }

        }
        tc.associateLookup(Lookups.fixed(tc.getActionMap(), tc));
        ContextAwareAction del = new CAA();
        ContextAwareAction context = Actions.context(Integer.class, true, true, del, null, "DisplayName", null, true);
        Action a = context.createContextAwareInstance(tc.getLookup());
        tc.getActionMap().put("key", a);

        WeakReference<Object> ref = new WeakReference<Object>(tc);
        tc = null;
        a = null;
        del = null;
        context = null;
        assertGC("Can the component GC?", ref);
    }

    /**
     * Test of readExternal method, of class org.openide.windows.TopComponent.
     */
    public void testReadExternal() throws Exception {
        // first try tc with displayname
        TopComponent tc = new TopComponent();
        tc.setName("testName");
        tc.setDisplayName("testdisplayName");
        tc.setToolTipText("testTooltip");
        NbMarshalledObject obj = new NbMarshalledObject(tc);
        tc.close();
        
        tc = (TopComponent)obj.get();
        assertNotNull("One again", tc);
        assertEquals("testName", tc.getName());
        assertEquals("testTooltip", tc.getToolTipText());
        assertEquals("testdisplayName", tc.getDisplayName());
        
        // now try tc withOUT displayname
        tc = new TopComponent();
        tc.setName("testName");
        tc.setToolTipText("testTooltip");
        obj = new NbMarshalledObject(tc);
        tc.close();
        
        tc = (TopComponent)obj.get();
        assertNotNull("One again", tc);
        assertEquals("testName", tc.getName());
        assertEquals("testTooltip", tc.getToolTipText());
        assertNull(tc.getDisplayName());
        
    }
    
    /**
     * Test of readExternal method, of class org.openide.windows.TopComponent.
     */
    public void testOldReadExternal() throws Exception {
        TopComponent tc = null;
        try {
            ObjectInputStream stream = new ObjectInputStream(
                    getClass().getResourceAsStream("data/oldTcWithoutDisplayName.ser"));
            tc = (TopComponent)stream.readObject();
            stream.close();
        } catch (Exception exc) {
            exc.printStackTrace();
            fail("Cannot read tc");
        }
        
        
        assertNotNull("One again", tc);
        assertEquals("testName", tc.getName());
        assertEquals("testTooltip", tc.getToolTipText());
        assertEquals("If the old component does not have a display name, then keep it null", null, tc.getDisplayName());
    }

    TopComponent tcOpened = null;
    TopComponent tcClosed = null;
    
    public void testOpenedClosed () throws Exception {
        System.out.println("Testing property firing of TopComponent's registry");
        tcOpened = null;
        tcClosed = null;
                
        TopComponent.getRegistry().addPropertyChangeListener(
                new PropertyChangeListener() {
                    public void propertyChange(PropertyChangeEvent evt) {
                        if (TopComponent.Registry.PROP_TC_OPENED.equals(evt.getPropertyName())) {
                            tcOpened = (TopComponent) evt.getNewValue();
                        } 
                        if (TopComponent.Registry.PROP_TC_CLOSED.equals(evt.getPropertyName())) {
                            tcClosed = (TopComponent) evt.getNewValue();
                        } 
                    }
                });
                
        TopComponent tc = new TopComponent();
        
        tc.open();
        assertNotNull("Property change was not fired, tcOpened is null", tcOpened);
        assertEquals("New value in property change is wrong", tc, tcOpened);
                
        tc.close();
        assertNotNull("Property change was not fired, tcClosed is null", tcClosed);
        assertEquals("New value in property change is wrong", tc, tcClosed);
        
    }
    
    
    public void testToStringOfDelegateContainsNameOfOriginalAction() throws Exception {
        SaveAction sa = SaveAction.get(SaveAction.class);
        Action a = sa.createContextAwareInstance(Lookup.EMPTY);
        if (a.toString().indexOf("SaveAction") == -1) {
            fail("We need name of the original action:\n" + a.toString());
        }

        SaveAction.cnt = 0;
        CharSequence log = Log.enable("org.netbeans.ui", Level.FINER);
        
        final TopComponent tc = new TopComponent();
        final KeyEvent ke = new KeyEvent(tc, KeyEvent.KEY_PRESSED, System.currentTimeMillis(), KeyEvent.CTRL_MASK, 0, 'S');
        final KeyStroke ks = KeyStroke.getKeyStrokeForEvent(ke);
        MockServices.setServices(MyKM.class);
        SwingUtilities.invokeAndWait(new Runnable() {

            public void run() {
                tc.setActivatedNodes( new Node[0] );
                tc.processKeyBinding(ks, ke, JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT, true);
            }
            
        });
        
        if (log.toString().indexOf("SaveAction") == -1 
                && SaveAction.cnt == 1 ) { //make sure the action was actually invoked
            fail(log.toString());
        }
    }
    
    public void testCanGCTopComponentWhenItsActionMapIsHeld() throws Exception {
        TopComponent tc = new TopComponent();
        
        Object map = tc.getActionMap();
        Reference<Object> ref = new WeakReference<Object>(tc);
        tc = null;
        
        assertGC("TC can disappear", ref, Collections.singleton(map));
    }
    
    
    public static final class SaveAction extends CookieAction {
        static int cnt;
        
        protected void performAction(Node[] activatedNodes) {
            cnt++;
        }

        protected boolean enable(Node[] activatedNodes) {
            return true;
        }

        public String getName() {
            return "FakeName";
        }

        public HelpCtx getHelpCtx() {
            return HelpCtx.DEFAULT_HELP;
        }

        protected int mode() {
            return 0;
        }

        protected Class<?>[] cookieClasses() {
            return new Class[0];
        }
        
    }
    
    public static final class MyKM implements Keymap {

        public String getName() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public Action getDefaultAction() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public void setDefaultAction(Action a) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public Action getAction(KeyStroke key) {
            return SaveAction.get(SaveAction.class);
        }

        public KeyStroke[] getBoundKeyStrokes() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public Action[] getBoundActions() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public KeyStroke[] getKeyStrokesForAction(Action a) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public boolean isLocallyDefined(KeyStroke key) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public void addActionForKeyStroke(KeyStroke key, Action a) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public void removeKeyStrokeBinding(KeyStroke keys) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public void removeBindings() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public Keymap getResolveParent() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public void setResolveParent(Keymap parent) {
            throw new UnsupportedOperationException("Not supported yet.");
        }
        
    }
}
