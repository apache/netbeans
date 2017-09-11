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
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import org.netbeans.core.api.multiview.MultiViewHandler;
import org.netbeans.core.api.multiview.MultiViews;
import org.netbeans.core.spi.multiview.MultiViewDescription;
import org.netbeans.core.spi.multiview.MultiViewFactory;

import java.util.Collection;
import java.util.List;
import javax.swing.Action;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.netbeans.core.api.multiview.MultiViewPerspective;
import org.netbeans.core.spi.multiview.CloseOperationHandler;
import org.openide.util.io.NbMarshalledObject;

import org.openide.windows.*;


/** 
 *
 * @author Milos Kleint
 */
public class MultiViewTopComponentTest extends AbstractMultiViewTopComponentTestCase {
    
    public static Test suite() {
        return GraphicsEnvironment.isHeadless() ? new TestSuite() : new TestSuite(MultiViewTopComponentTest.class);
    }

    public MultiViewTopComponentTest(String name) {
        super (name);
    }
    
    protected TopComponent callFactory(MultiViewDescription[] desc, MultiViewDescription def) {
        return MultiViewFactory.createMultiView(desc, def);
    }    
    
    protected TopComponent callFactory(MultiViewDescription[] desc, MultiViewDescription def, CloseOperationHandler close) {
        return MultiViewFactory.createMultiView(desc, def, close);
    }
    
    protected Class getTopComponentClass() {
        return MultiViewTopComponent.class;
    }
    
    public void testPersistence() throws Exception {
        MVElem elem1 = new MVElem(new Action[] {new Act1("act1")} );
        SerMVElem elem2 = new SerMVElem();
        SerMVElem elem3 = new SerMVElem();
        elem2.deserializeTest = "testtesttest - 2";
        elem3.deserializeTest = "testtesttest - 3";
        
        MultiViewDescription desc1 = new SerMVDesc("desc1", null, TopComponent.PERSISTENCE_NEVER, elem1);
        MultiViewDescription desc2 = new SerMVDesc("desc2", null, TopComponent.PERSISTENCE_ONLY_OPENED, elem2);
        MultiViewDescription desc3 = new SerMVDesc("desc3", null, TopComponent.PERSISTENCE_ALWAYS, elem3);
        
        MultiViewDescription[] descs = new MultiViewDescription[] { desc1, desc2 };
        SerCloseHandler close = new SerCloseHandler("serializedvalue");
        
        TopComponent tc = callFactory(descs, desc2, close);
        tc.open();
        tc.requestActive();
        // testing closehandler here..
        tc.close();
        
        NbMarshalledObject mars = new NbMarshalledObject(tc);
        Object obj = mars.get();
        assertNotNull(obj);
        assertEquals(getTopComponentClass(), obj.getClass());
        tc = (MultiViewTopComponent)obj;
        
        
        MultiViewHandler handler = MultiViews.findMultiViewHandler(tc);
        MultiViewPerspective[] descsAfter = handler.getPerspectives();
        assertNotNull(descsAfter);
        assertEquals(2, descsAfter.length);
        MultiViewPerspective selDesc = handler.getSelectedPerspective();
        assertNotNull(selDesc);
        assertEquals("desc2", selDesc.getDisplayName());
        tc.open();
        tc.requestActive();
        MultiViewTopComponent mvtc = (MultiViewTopComponent)tc;
        Collection cold = mvtc.getModel().getCreatedElements();
        // expected number of elements is one, because the elem3 was not initialized at all..
        assertEquals(1, cold.size());
        
        // test if the deserialized instance is there..
        SerMVElem elSelecto = (SerMVElem)mvtc.getModel().getActiveElement();
        assertEquals("testtesttest - 2", elSelecto.deserializeTest);
        assertEquals("componentOpened-componentShowing-componentActivated-", elSelecto.getLog());
        
        //testing if closehandler was correctly deserialized..
        tc.close();
        
        
    }    

    /** Tests that multiple instances of the same class propagate well
     * into array of actions of enclosing multiview TopComponent
     */
    public void testFix_132948_MoreActionsOfSameClass () {
        Action[] acts = new Action[] {
                new MultiViewActionMapTest.TestAction("First"),
                new MultiViewActionMapTest.TestAction("Second"),
                new MultiViewActionMapTest.TestAction("Third")
        };
        Action[] actsCopy = new Action[acts.length];
        System.arraycopy(acts, 0, actsCopy, 0, acts.length);
        
        MVElem elem = new MVElem(actsCopy);
        MultiViewDescription desc = new MVDesc("desc1", null, 0, elem);
        TopComponent tc = MultiViewFactory.createMultiView(new MultiViewDescription[] { desc }, desc);

        MultiViewTopComponent mvtc = (MultiViewTopComponent)tc;
        mvtc.setSuperActions(actsCopy);
        
        List<Action> tcActs = Arrays.asList(tc.getActions());
        
        for (int i = 0; i < acts.length; i++) {
            Action action = acts[i];
            assertTrue("Action " + action.getValue(Action.NAME) + 
                    " not propagated into multiview TC actions", 
                    tcActs.contains(action));
        }
    }
    
    
    /** Tests that multiple instances of the same class propagate well
     * into array of actions of enclosing multiview TopComponent
     */
    public void testFix_204072_MissingIcon() {
        final Image img = new BufferedImage( 10, 10, BufferedImage.TYPE_INT_RGB );
        MVElem elem1 = new MVElem();
        MVElem elem2 = new MVElem();
        MultiViewDescription desc1 = new MVDesc("desc1", img, 0, elem1);
        MultiViewDescription desc2 = new MVDesc("desc2", null, 0, elem2);
        TopComponent tc = MultiViewFactory.createMultiView(new MultiViewDescription[] { desc1, desc2 }, desc1);

        tc.open();
        tc.requestActive();
        
        assertEquals( img, tc.getIcon() );
        
        MultiViewTopComponent mvtc = ( MultiViewTopComponent ) tc;
        mvtc.getModel().setActiveDescription( desc2 );
        
        assertEquals( img, tc.getIcon() );
    }
}

