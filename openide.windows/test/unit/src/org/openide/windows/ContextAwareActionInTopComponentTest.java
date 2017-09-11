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

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.HashMap;
import java.util.Map;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.KeyStroke;
import javax.swing.text.Keymap;
import org.netbeans.junit.MockServices;
import org.netbeans.junit.NbTestCase;
import org.openide.nodes.Node;
import org.openide.util.ContextAwareAction;
import org.openide.util.Lookup;

/** Tests behaviour of GlobalContextProviderImpl
 * and its cooperation with activated and current nodes.
 *
 * @author Jaroslav Tulach
 */
public class ContextAwareActionInTopComponentTest extends NbTestCase {

    private TopComponent tc;
    private MyContextAwareAction myGlobalAction = new MyContextAwareAction(true);
    private KeyStroke KEY_STROKE = KeyStroke.getKeyStroke( KeyEvent.VK_W, KeyEvent.ALT_DOWN_MASK+KeyEvent.CTRL_DOWN_MASK+KeyEvent.SHIFT_DOWN_MASK );

    public ContextAwareActionInTopComponentTest(java.lang.String testName) {
        super(testName);
    }
    
    protected void setUp () throws Exception {
        tc = new TopComponent ();
        tc.requestActive();
        
        MockServices.setServices( MyKeymap.class );
        Keymap km = Lookup.getDefault().lookup(Keymap.class);
        km.addActionForKeyStroke( KEY_STROKE, myGlobalAction );

        MyContextAwareAction.globalActionWasPerformed = false;
        MyContextAwareAction.contextActionWasPerformed = false;
    }
    
    public void testGlobalActionDisabled () throws Exception {
        myGlobalAction.setEnabled( false );
        
        final org.openide.nodes.Node n = new org.openide.nodes.AbstractNode (org.openide.nodes.Children.LEAF);
        tc.setActivatedNodes(new Node[] { n });
        
        KeyEvent e = new KeyEvent( tc, KeyEvent.KEY_TYPED, 0, 0, 0 );
        assertTrue( tc.processKeyBinding( KEY_STROKE, e, JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT, true ) );
        assertFalse( MyContextAwareAction.globalActionWasPerformed );
        assertTrue( MyContextAwareAction.contextActionWasPerformed );
    }
    
    public void testGlobalActionSurvivedFocusChange() throws Exception {
        myGlobalAction.setEnabled( true );
        
        final org.openide.nodes.Node n = new org.openide.nodes.AbstractNode (org.openide.nodes.Children.LEAF);
        tc.setActivatedNodes(null);
        
        KeyEvent e = new KeyEvent( tc, KeyEvent.KEY_TYPED, 0, 0, 0 );
        assertTrue( tc.processKeyBinding( KEY_STROKE, e, JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT, true ) );
        assertTrue( MyContextAwareAction.globalActionWasPerformed );
        assertFalse( MyContextAwareAction.contextActionWasPerformed );
    }
    
    public void testGlobalActionDoesNotSurviveFocusChange() throws Exception {
        myGlobalAction.setEnabled( true );
        
        final org.openide.nodes.Node n = new org.openide.nodes.AbstractNode (org.openide.nodes.Children.LEAF);
        tc.setActivatedNodes(new Node[0]);
        
        KeyEvent e = new KeyEvent( tc, KeyEvent.KEY_TYPED, 0, 0, 0 );
        assertTrue( tc.processKeyBinding( KEY_STROKE, e, JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT, true ) );
        assertTrue( MyContextAwareAction.globalActionWasPerformed );
        assertFalse( MyContextAwareAction.contextActionWasPerformed );
    }
    
    /**
     * Context-aware action that is enabled only if there are any activated nodes.
     * 
     */
    private static class MyContextAwareAction extends AbstractAction implements ContextAwareAction {
        
        private static boolean globalActionWasPerformed = false;
        private static boolean contextActionWasPerformed = false;
        private final boolean isGlobal;
        
        public MyContextAwareAction(boolean isGlobal) {
            globalActionWasPerformed = false;
            this.isGlobal = isGlobal;
        }
    
        public void actionPerformed(ActionEvent arg0) {
            if( isGlobal )
                globalActionWasPerformed = true;
            else
                contextActionWasPerformed = true;
        }

        public Action createContextAwareInstance(Lookup actionContext) {
            MyContextAwareAction action = new MyContextAwareAction(false);
            action.setEnabled( null != actionContext.lookup( Node.class ) );
            return action;
        }
    }
    
    public static class MyKeymap implements Keymap {
        
        private Map<KeyStroke, Action> ks2a = new HashMap<KeyStroke, Action>();
        
        public String getName() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public Action getDefaultAction() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public void setDefaultAction(Action arg0) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public Action getAction(KeyStroke arg0) {
            return ks2a.get( arg0 );
        }

        public KeyStroke[] getBoundKeyStrokes() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public Action[] getBoundActions() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public KeyStroke[] getKeyStrokesForAction(Action arg0) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public boolean isLocallyDefined(KeyStroke arg0) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public void addActionForKeyStroke(KeyStroke arg0, Action arg1) {
            ks2a.put( arg0, arg1 );
        }

        public void removeKeyStrokeBinding(KeyStroke arg0) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public void removeBindings() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public Keymap getResolveParent() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public void setResolveParent(Keymap arg0) {
            throw new UnsupportedOperationException("Not supported yet.");
        }
}
}
