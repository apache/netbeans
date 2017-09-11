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

import java.awt.GraphicsEnvironment;
import java.util.ArrayList;
import java.util.List;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.netbeans.core.api.multiview.MultiViewHandler;
import org.netbeans.core.api.multiview.MultiViews;
import org.netbeans.core.spi.multiview.MultiViewDescription;
import org.netbeans.core.spi.multiview.MultiViewFactory;
import org.netbeans.junit.NbTestCase;
import org.openide.awt.UndoRedo;
import org.openide.windows.TopComponent;

/**
 *
 * @author Milos Kleint
 */
public class MultiViewElementTest extends NbTestCase {
    
    public static Test suite() {
        return GraphicsEnvironment.isHeadless() ? new TestSuite() : new TestSuite(MultiViewElementTest.class);
    }

    public MultiViewElementTest(String name) {
        super (name);
    }
    
    protected @Override boolean runInEQ () {
        return true;
    }

    protected @Override int timeOut() {
        return 500000;
    }
    
    public void testRequestVisible() throws Exception {
        MVElem elem1 = new MVElem();
        MVElem elem2 = new MVElem();
        MVElem elem3 = new MVElem();
        MultiViewDescription desc1 = new MVDesc("desc1", null, 0, elem1);
        MultiViewDescription desc2 = new MVDesc("desc2", null, 0, elem2);
        MultiViewDescription desc3 = new MVDesc("desc3", null, 0, elem3);
        
        MultiViewDescription[] descs = new MultiViewDescription[] { desc1, desc2, desc3 };
        TopComponent tc = MultiViewFactory.createMultiView(descs, desc1);
        
        // NOT OPENED YET.
        assertEquals("",elem1.getLog());
        assertEquals("",elem2.getLog());
        
        tc.open();
        assertEquals("componentOpened-componentShowing-", elem1.getLog());
        assertEquals("",elem2.getLog());

        // initilize the elements..
        MultiViewHandler handler = MultiViews.findMultiViewHandler(tc);
        
        // test related hack, easy establishing a  connection from Desc->perspective
        Accessor.DEFAULT.createPerspective(desc2);
        handler.requestVisible(Accessor.DEFAULT.createPerspective(desc2));
        handler.requestVisible(Accessor.DEFAULT.createPerspective(desc3));
        handler.requestVisible(Accessor.DEFAULT.createPerspective(desc1));
        elem1.resetLog();
        elem2.resetLog();
        elem3.resetLog();
        
        elem2.doRequestVisible();
        assertEquals("componentHidden-", elem1.getLog());
        assertEquals("componentShowing-", elem2.getLog());
        assertEquals("", elem3.getLog());
        
        elem3.doRequestVisible();
        assertEquals("componentHidden-", elem1.getLog());
        assertEquals("componentShowing-componentHidden-", elem2.getLog());
        assertEquals("componentShowing-", elem3.getLog());
        
        elem1.doRequestVisible();
        assertEquals("componentShowing-componentHidden-", elem3.getLog());
        assertEquals("componentHidden-componentShowing-", elem1.getLog());
        
    }

    
    public void testRequestActive() throws Exception {
        MVElem elem1 = new MVElem();
        MVElem elem2 = new MVElem();
        MVElem elem3 = new MVElem();
        MultiViewDescription desc1 = new MVDesc("desc1", null, 0, elem1);
        MultiViewDescription desc2 = new MVDesc("desc2", null, 0, elem2);
        MultiViewDescription desc3 = new MVDesc("desc3", null, 0, elem3);
        MultiViewDescription[] descs = new MultiViewDescription[] { desc1, desc2, desc3 };
        TopComponent tc = MultiViewFactory.createMultiView(descs, desc2);

        // NOT OPENED YET.
        assertEquals("",elem1.getLog());
        assertEquals("",elem2.getLog());
        
        tc.open();
        tc.requestActive();
        assertEquals("",elem1.getLog());
        assertEquals("componentOpened-componentShowing-componentActivated-", elem2.getLog());
        assertEquals("",elem3.getLog());
        
        // initilize the elements..
        // test related hack, easy establishing a  connection from Desc->perspective
        MultiViewHandler handler = MultiViews.findMultiViewHandler(tc);
        handler.requestVisible(Accessor.DEFAULT.createPerspective(desc1));
        handler.requestVisible(Accessor.DEFAULT.createPerspective(desc3));
        handler.requestActive(Accessor.DEFAULT.createPerspective(desc2));
        elem1.resetLog();
        elem2.resetLog();
        elem3.resetLog();
//        System.err.println("start Caring.........................");
        elem1.doRequestActive();
//        System.err.println("elem1=" + elem1.getLog());
//        System.err.println("elem2=" + elem2.getLog());

        assertEquals("componentShowing-componentActivated-", elem1.getLog());
        assertEquals("componentDeactivated-componentHidden-", elem2.getLog());
        assertEquals("",elem3.getLog());
        
        // do request active the same component, nothing should happen.
        elem1.doRequestActive();
        assertEquals("componentShowing-componentActivated-", elem1.getLog());
        assertEquals("componentDeactivated-componentHidden-", elem2.getLog());
        assertEquals("",elem3.getLog());
        
    }
    
    public void testUndoRedo() throws Exception {
        UndoRedoImpl redo1 = new UndoRedoImpl();
        redo1.undo = false;
        UndoRedoImpl redo2 = new UndoRedoImpl();
        redo2.redo = false;
        ChangeListenerImpl changeList = new ChangeListenerImpl();
        MVElem elem1 = new MVElem();
        elem1.setUndoRedo(redo1);
        MVElem elem2 = new MVElem();
        elem2.setUndoRedo(redo2);
        MVElem elem3 = new MVElem();
        MultiViewDescription desc1 = new MVDesc("desc1", null, 0, elem1);
        MultiViewDescription desc2 = new MVDesc("desc2", null, 0, elem2);
        MultiViewDescription desc3 = new MVDesc("desc3", null, 0, elem3);
        MultiViewDescription[] descs = new MultiViewDescription[] { desc1, desc2, desc3 };
        TopComponent tc = MultiViewFactory.createMultiView(descs, desc2);

        UndoRedo result = tc.getUndoRedo();
        assertNotNull(result);
        assertFalse(result.canRedo());
        assertTrue(result.canUndo());
        result.addChangeListener(changeList);
        assertEquals(1, redo2.listeners.size());
        tc.open();
        tc.requestActive();
        assertEquals(0, changeList.count);
        
        MultiViewHandler handler = MultiViews.findMultiViewHandler(tc);
        handler.requestVisible(Accessor.DEFAULT.createPerspective(desc1));
        
        assertTrue(result.canRedo());
        assertFalse(result.canUndo());
        assertEquals(1, redo1.listeners.size());
        assertEquals(0, redo2.listeners.size());
        assertEquals(1, changeList.count);
        
        handler.requestVisible(Accessor.DEFAULT.createPerspective(desc3));
        assertFalse(result.canRedo());
        assertFalse(result.canUndo());
        assertEquals(0, redo2.listeners.size());
        assertEquals(0, redo1.listeners.size());
        assertEquals(2, changeList.count);
        
    }    
    
    public void testUpdateTitle() throws Exception {
        MVElem elem1 = new MVElem();
        MVElem elem2 = new MVElem();
        MVElem elem3 = new MVElem();
        MultiViewDescription desc1 = new MVDesc("desc1", null, 0, elem1);
        MultiViewDescription desc2 = new MVDesc("desc2", null, 0, elem2);
        MultiViewDescription desc3 = new MVDesc("desc3", null, 0, elem3);
        MultiViewDescription[] descs = new MultiViewDescription[] { desc1, desc2, desc3 };
        TopComponent tc = MultiViewFactory.createMultiView(descs, desc2);

        tc.open();
        assertEquals(null, tc.getDisplayName());
        
        
        elem2.observer.updateTitle("test1");
        assertEquals("test1", tc.getDisplayName());
        
        // switch to desc3 to initilize the element..
        MultiViewHandler handler = MultiViews.findMultiViewHandler(tc);

        // test related hack, easy establishing a  connection from Desc->perspective
        handler.requestVisible(Accessor.DEFAULT.createPerspective(desc3));
        
        elem3.observer.updateTitle("test2");
        assertEquals("test2", tc.getDisplayName());
        
    }
    
    private class UndoRedoImpl implements UndoRedo {
        public List<ChangeListener> listeners = new ArrayList<ChangeListener>();
        public boolean undo = true;
        public boolean redo = true;
        
        public void addChangeListener(ChangeListener l) {
            listeners.add(l);
        }
        public boolean canRedo() {
            return redo;
        }
        public boolean canUndo() {
            return undo;
        }
        public String getRedoPresentationName() {
            return "String";
        }
        public String getUndoPresentationName() {
            return "String2";
        }
        public void redo() throws CannotRedoException {
        }
        public void removeChangeListener(ChangeListener l) {
            listeners.remove(l);
        }
        public void undo() throws CannotUndoException {
        }
    }
    
    private class ChangeListenerImpl implements ChangeListener {
        public int count = 0;
        public void stateChanged(ChangeEvent e) {
            count++;
        }
        
    }
    
}
