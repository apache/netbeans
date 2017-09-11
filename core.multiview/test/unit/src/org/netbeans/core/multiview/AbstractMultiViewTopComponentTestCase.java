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


package org.netbeans.core.multiview;

import java.awt.Component;
import org.netbeans.core.api.multiview.MultiViewHandler;
import org.netbeans.core.api.multiview.MultiViews;
import org.netbeans.core.spi.multiview.MultiViewDescription;

import java.awt.Image;
import java.io.IOException;
import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javax.swing.Action;
import org.netbeans.core.spi.multiview.CloseOperationHandler;
import org.netbeans.core.spi.multiview.MultiViewElement;
import org.netbeans.junit.*;
import org.openide.awt.UndoRedo;
import org.openide.util.HelpCtx;

import org.openide.windows.*;


/** 
 *
 * @author Milos Kleint
 */
public abstract class AbstractMultiViewTopComponentTestCase extends NbTestCase {
    
    /** Creates a new instance of SFSTest */
    public AbstractMultiViewTopComponentTestCase(String name) {
        super (name);
    }
    

    @Override
    protected boolean runInEQ () {
        return true;
    }
    
    
    protected abstract TopComponent callFactory(MultiViewDescription[] desc, MultiViewDescription def);

    protected abstract TopComponent callFactory(MultiViewDescription[] desc, MultiViewDescription def, CloseOperationHandler close);
    
    protected abstract Class getTopComponentClass();
    
    public void testTopComponentOpen () throws Exception {
        final MVElem elem1 = new MVElem();
        final MVElem elem2 = new MVElem();
        final MVElem elem3 = new MVElem();
        MultiViewDescription desc1 = new MVDesc("desc1", null, 0, elem1);
        MultiViewDescription desc2 = new MVDesc("desc2", null, 0, elem2);
        MultiViewDescription desc3 = new MVDesc("desc3", null, 0, elem3);
        MultiViewDescription[] descs = new MultiViewDescription[] { desc1, desc2, desc3 };
        TopComponent tc = callFactory(descs, desc2);
        // NOT OPENED YET.
        assertEquals("",elem1.getLog());
        assertEquals("",elem2.getLog());
        
        tc.open();
        assertEquals("",elem1.getLog());
        assertEquals("componentOpened-componentShowing-", elem2.getLog());
        assertEquals("",elem3.getLog());
        
        tc.requestActive();
        assertEquals("componentOpened-componentShowing-componentActivated-", elem2.getLog());
        
        tc.close();
        //TODO shall the winsys also call deactivate on TC? the Dummy one doens't do it..
        assertEquals("componentOpened-componentShowing-componentActivated-componentHidden-componentClosed-componentDeactivated-", elem2.getLog());
    }

    
    public void testTopComponentSwitching () throws Exception {
        final MVElem elem1 = new MVElem();
        final MVElem elem2 = new MVElem();
        final MVElem elem3 = new MVElem();
        MultiViewDescription desc1 = new MVDesc("desc1", null, 0, elem1);
        MultiViewDescription desc2 = new MVDesc("desc2", null, 0, elem2);
        MultiViewDescription desc3 = new MVDesc("desc3", null, 0, elem3);
        MultiViewDescription[] descs = new MultiViewDescription[] { desc1, desc2, desc3 };
        TopComponent tc = callFactory(descs, desc2);
        
        tc.open();
        tc.requestActive();
        assertEquals("componentOpened-componentShowing-componentActivated-", elem2.getLog());
        // reset log to make the asserts shorter..
        elem2.resetLog();
        
        TopComponent tc2 = new TopComponent();
        tc2.open();
        tc2.requestActive();
        assertEquals("componentDeactivated-", elem2.getLog());

        tc.requestActive();
        assertEquals("componentDeactivated-componentActivated-", elem2.getLog());
        
    }
    
    public void testActions() throws Exception {
        final MVElem elem1 = new MVElem(new Action[] {new Act1("act1")} );
        final MVElem elem2 = new MVElem(new Action[] {new Act1("act2")} );
        MultiViewDescription desc1 = new MVDesc("desc1", null, 0, elem1);
        MultiViewDescription desc2 = new MVDesc("desc2", null, 0, elem2);
        MultiViewDescription[] descs = new MultiViewDescription[] { desc1, desc2 };
        TopComponent tc = callFactory(descs, desc2);
        
        tc.open();
        tc.requestActive();
        
        TopComponent result = TopComponent.getRegistry().getActivated();
        Action[] acts = result.getActions();
        assertNotNull(acts);
        assertEquals("Four actions: " + Arrays.toString(acts), 4, acts.length);
        assertEquals("Second is null", null, acts[1]);
        assertTrue("Second from last one instance of tab switching",  acts[2] instanceof EditorsAction);
        assertTrue("Last one instance of spliting",  acts[3] instanceof SplitAction);
        Object name = acts[0].getValue(Action.NAME);
        assertEquals("act2", name);
        
        MultiViewHandler hand = MultiViews.findMultiViewHandler(tc);
        
        // test related hack, easy establishing a  connection from Desc->perspective
        hand.requestActive(Accessor.DEFAULT.createPerspective(desc1));
        acts = result.getActions();
        assertNotNull(acts);
        assertEquals(4, acts.length);
        name = acts[0].getValue(Action.NAME);
        assertEquals("act1", name);
        
    }
    
    public void testGetHelpCtx() throws Exception {
        final MVElem elem1 = new MVElem(new Action[] {new Act1("act1")} );
        final MVElem elem2 = new MVElem(new Action[] {new Act1("act2")} );
        MultiViewDescription desc1 = new MVDesc("desc1", null, 0, elem1);
        MultiViewDescription desc2 = new MVDesc("desc2", null, 0, elem2);
        MultiViewDescription[] descs = new MultiViewDescription[] { desc1, desc2 };
        TopComponent tc = callFactory(descs, desc2);
        
        tc.open();
        HelpCtx help = tc.getHelpCtx();
        MultiViewHandler hand = MultiViews.findMultiViewHandler(tc);
        
        assertNotNull(help);
        Object name = help.getHelpID();
        assertEquals(desc2, Accessor.DEFAULT.extractDescription(hand.getSelectedPerspective()));
        assertEquals("desc2", name);
        
        hand.requestActive(Accessor.DEFAULT.createPerspective(desc1));
        help = tc.getHelpCtx();
        assertNotNull(help);
        name = help.getHelpID();
        assertEquals("desc1", name);
        
    }

    public void testGetLookup() throws Exception {
        final MVElem elem1 = new MVElem(new Action[] {new Act1("act1")} );
        final MVElem elem2 = new MVElem(new Action[] {new Act1("act2")} );
        MultiViewDescription desc1 = new MVDesc("desc1", null, 0, elem1);
        MultiViewDescription desc2 = new MVDesc("desc2", null, 0, elem2);
        MultiViewDescription[] descs = new MultiViewDescription[] { desc1, desc2 };
        TopComponent tc = callFactory(descs, desc2);
        
        tc.open();
        Object result = tc.getLookup().lookup(MVElem.class);
        assertNotNull(result);
        assertEquals(result, elem2);
        MultiViewHandler hand = MultiViews.findMultiViewHandler(tc);
        
        hand.requestActive(Accessor.DEFAULT.createPerspective(desc1));
        result = tc.getLookup().lookup(MVElem.class);
        assertNotNull(result);
        assertEquals(result, elem1);
        
    }
    
    
    
    public void testGetUndoRedo() throws Exception {
        final MVElem elem1 = new MVElem(new Action[] {new Act1("act1")} );
        UndoRedo redo = new UndoRedo.Empty();
        elem1.setUndoRedo(redo);
        final MVElem elem2 = new MVElem(new Action[] {new Act1("act2")} );
        MultiViewDescription desc1 = new MVDesc("desc1", null, 0, elem1);
        MultiViewDescription desc2 = new MVDesc("desc2", null, 0, elem2);
        MultiViewDescription[] descs = new MultiViewDescription[] { desc1, desc2 };
        TopComponent tc = callFactory(descs, desc2);
        
        tc.open();
        UndoRedo result = tc.getUndoRedo();
        MultiViewHandler hand = MultiViews.findMultiViewHandler(tc);
        
        assertNotNull(result);
        assertTrue(redo != result);
        
        hand.requestActive(Accessor.DEFAULT.createPerspective(desc1));
        result = tc.getUndoRedo();
        assertFalse(redo.canRedo());
        assertFalse(redo.canUndo());
    }
    
    

    public void testPersistenceType() throws Exception {
        final MVElem elem1 = new MVElem(new Action[] {new Act1("act1")} );
        final MVElem elem2 = new MVElem(new Action[] {new Act1("act2")} );
        MultiViewDescription desc1 = new MVDesc("desc1", null, TopComponent.PERSISTENCE_NEVER, elem1);
        MultiViewDescription desc2 = new MVDesc("desc2", null, TopComponent.PERSISTENCE_ONLY_OPENED, elem2);
        MultiViewDescription desc3 = new MVDesc("desc3", null, TopComponent.PERSISTENCE_ALWAYS, elem2);
        
        MultiViewDescription[] descs = new MultiViewDescription[] { desc1, desc2 };
        TopComponent tc = callFactory(descs, desc2);
        // is never persisted because the Descriptions are not serializable
        assertEquals(TopComponent.PERSISTENCE_NEVER, tc.getPersistenceType());
        

        descs = new MultiViewDescription[] { desc1, desc2, desc3 };
        // is never persisted because the Descriptions are not serializable
        tc = callFactory(descs, desc2);
        assertEquals(TopComponent.PERSISTENCE_NEVER, tc.getPersistenceType());

        MultiViewDescription desc4 = new SerMVDesc("desc1", null, TopComponent.PERSISTENCE_NEVER, elem1);
        MultiViewDescription desc5 = new SerMVDesc("desc2", null, TopComponent.PERSISTENCE_ONLY_OPENED, elem2);
        MultiViewDescription desc6 = new SerMVDesc("desc3", null, TopComponent.PERSISTENCE_ALWAYS, elem2);

        descs = new MultiViewDescription[] { desc4, desc5 };
        tc = callFactory(descs, desc5);
        assertEquals(TopComponent.PERSISTENCE_ONLY_OPENED, tc.getPersistenceType());
        
        descs = new MultiViewDescription[] { desc4, desc5, desc6 };
        tc = callFactory(descs, desc6);
        assertEquals(TopComponent.PERSISTENCE_ALWAYS, tc.getPersistenceType());

    }
    
    public void testPrefferedId() throws Exception {
        //how to test? cannot really access the ID..
        
//        final MVElem elem1 = new MVElem(new Action[] {new Act1("act1")} );
//        final MVElem elem2 = new MVElem(new Action[] {new Act1("act2")} );
//        MultiViewDescription desc1 = new MVDesc("desc1", null, TopComponent.PERSISTENCE_NEVER, elem1);
//        MultiViewDescription desc2 = new MVDesc("desc2", null, TopComponent.PERSISTENCE_ONLY_OPENED, elem2);
//        MultiViewDescription desc3 = new MVDesc("desc3", null, TopComponent.PERSISTENCE_ALWAYS, elem2);
//        
//        MultiViewDescription[] descs = new MultiViewDescription[] { desc1, desc2 };
//        TopComponent tc = callFactory(descs, desc2);

    }

    /** Test for 130919 fix - tests that TabsComponent don't hold strong
     * references to visual representations of multiview tabs after close.
     */
    public void testLeakInnerCompsAfterClose_130473 () throws Exception {
        final MVElem elem1 = new MVElem(new Action[] {new Act1("act1")} );
        MultiViewDescription desc1 = new MVDesc("desc1", null, TopComponent.PERSISTENCE_NEVER, elem1);
        MultiViewDescription[] descs = new MultiViewDescription[] { desc1 };
        TopComponent tc = callFactory(descs, desc1);
        
        tc.open();
        
        Component[] comps = null;
        TabsComponent tabsC = null;
        if (tc instanceof MultiViewTopComponent) {
            MultiViewTopComponent mvtc = (MultiViewTopComponent)tc;
            tabsC = mvtc.peer.tabs;
            comps = tabsC.componentPanel.getComponents();
        } else if (tc instanceof MultiViewCloneableTopComponent) {
            MultiViewCloneableTopComponent mvctc = (MultiViewCloneableTopComponent)tc;
            tabsC = mvctc.peer.tabs;
            comps = tabsC.componentPanel.getComponents();
        }
        
        assertNotNull("Components array inside must not be null", comps);
        assertTrue("Component array must not be empty", comps.length > 0);
        
        List<WeakReference<Component>> weakComps = new ArrayList<WeakReference<Component>>();
        for (int i = 0; i < comps.length; i++) {
            weakComps.add(new WeakReference<Component>(comps[i]));
        }
        comps = null;

        tc.close();
        tc = null;
        
        for (WeakReference<Component> wComp : weakComps) {
            assertGC("Component inside TabsComponent panel not freed", wComp, Collections.singleton(tabsC));
        }
        
    }
    
// -------------------------------------------------------------------------------
// *******************************************************************************    
// subclasses
// *******************************************************************************    
// -------------------------------------------------------------------------------
    
    public static class SerMVDesc extends MVDesc implements Serializable {
        private static final long serialVersionUID =-3126744916624172415L;        
        
        public SerMVDesc() {
            super();
        }
        
        SerMVDesc(String name, Image img, int perstype, MultiViewElement elem) {
            super(name, img, perstype, elem);
        }
        
        private void writeObject(java.io.ObjectOutputStream out) throws IOException {
            out.writeUTF(name);
            out.writeInt(type);
            out.writeObject(img);
        }
        private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
            name = in.readUTF();
            type = in.readInt();
            img = (Image)in.readObject();
        }
        
    }
    
    public static class SerMVElem extends MVElem implements Serializable {
        private static final long serialVersionUID =-3126744316624172415L;        
        
        public String deserializeTest;
        
        public SerMVElem() {
            super();
        }
        
        private void writeObject(java.io.ObjectOutputStream out) throws IOException {
            out.writeUTF(deserializeTest);
        }
        private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
            deserializeTest = in.readUTF();
        }
        
    }
 
    
    public static class SerCloseHandler implements Serializable, CloseOperationHandler {
        private static final long serialVersionUID =-3126744916624172415L;        
        
        public String serValue;
        
        private SerCloseHandler() {
        }
        
        public SerCloseHandler(String value) {
            serValue = value;
        }
        
        public boolean resolveCloseOperation(org.netbeans.core.spi.multiview.CloseOperationState[] elements) {
            if (serValue != null) {
                return true;
            }
            else throw new IllegalStateException("Badly initialized or deserialized");
        }        
        
    }    
    
    protected static class Act1 implements Action {
        private String name;
        
        Act1(String name) {
            this.name = name;
        }
        
        public void actionPerformed(java.awt.event.ActionEvent e) {
        }
        
        public void addPropertyChangeListener(java.beans.PropertyChangeListener listener) {
        }
        
        public Object getValue(String key) {
            if (Action.NAME.equals(key)) {
                return name;
            }
            return null;
        }
        
        public boolean isEnabled() {
            return true;
        }
        
        public void putValue(String key, Object value) {
        }
        
        public void removePropertyChangeListener(java.beans.PropertyChangeListener listener) {
        }
        
        public void setEnabled(boolean b) {
        }
        
    }    
    
}

