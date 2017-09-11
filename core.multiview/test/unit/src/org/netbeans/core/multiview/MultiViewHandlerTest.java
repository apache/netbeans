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
import junit.framework.Test;
import junit.framework.TestSuite;
import org.netbeans.core.api.multiview.MultiViewHandler;
import org.netbeans.core.api.multiview.MultiViewPerspective;
import org.netbeans.core.api.multiview.MultiViews;
import org.netbeans.core.spi.multiview.MultiViewDescription;
import org.netbeans.core.spi.multiview.MultiViewFactory;
import org.netbeans.junit.*;

import org.openide.windows.*;


/** 
 *
 * @author Milos Kleint
 */
public class MultiViewHandlerTest extends NbTestCase {
    
    public static Test suite() {
        return GraphicsEnvironment.isHeadless() ? new TestSuite() : new TestSuite(MultiViewHandlerTest.class);
    }

    public MultiViewHandlerTest(String name) {
        super (name);
    }
    
    protected boolean runInEQ () {
        return true;
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
        MultiViewHandler hand = MultiViews.findMultiViewHandler(tc);
        assertNotNull(hand);
        assertEquals(hand.getPerspectives().length, 3);
        MultiViewPerspective pers = hand.getSelectedPerspective();

        assertEquals(Accessor.DEFAULT.extractDescription(pers), desc1);
        // NOT OPENED YET.
        assertEquals("",elem1.getLog());
        assertEquals("",elem2.getLog());
        
        tc.open();
        assertEquals("componentOpened-componentShowing-", elem1.getLog());
        assertEquals("",elem2.getLog());
        
        // test related hack, easy establishing a  connection from Desc->perspective
        hand.requestVisible(Accessor.DEFAULT.createPerspective(desc2));
        
        assertEquals(Accessor.DEFAULT.extractDescription(hand.getSelectedPerspective()), desc2);
        assertEquals("componentOpened-componentShowing-componentHidden-", elem1.getLog());
        assertEquals("componentOpened-componentShowing-", elem2.getLog());
        assertEquals("", elem3.getLog());
        
        // test related hack, easy establishing a  connection from Desc->perspective
        hand.requestVisible(Accessor.DEFAULT.createPerspective(desc3));
        assertEquals("componentOpened-componentShowing-componentHidden-", elem1.getLog());
        assertEquals("componentOpened-componentShowing-componentHidden-", elem2.getLog());
        assertEquals("componentOpened-componentShowing-", elem3.getLog());
        
        // test related hack, easy establishing a  connection from Desc->perspective
        hand.requestVisible(Accessor.DEFAULT.createPerspective(desc1));
        assertEquals("componentOpened-componentShowing-componentHidden-", elem3.getLog());
        assertEquals("componentOpened-componentShowing-componentHidden-componentShowing-", elem1.getLog());
        
    }

    
    public void testRequestActive() throws Exception {
        final MVElem elem1 = new MVElem();
        final MVElem elem2 = new MVElem();
        final MVElem elem3 = new MVElem();
        MultiViewDescription desc1 = new MVDesc("desc1", null, 0, elem1);
        MultiViewDescription desc2 = new MVDesc("desc2", null, 0, elem2);
        MultiViewDescription desc3 = new MVDesc("desc3", null, 0, elem3);
        MultiViewDescription[] descs = new MultiViewDescription[] { desc1, desc2, desc3 };
        TopComponent tc = MultiViewFactory.createMultiView(descs, desc2);
        MultiViewHandler hand = MultiViews.findMultiViewHandler(tc);
        assertNotNull(hand);
        assertEquals(hand.getPerspectives().length, 3);
        
        MultiViewPerspective pers = hand.getSelectedPerspective();

        assertEquals(Accessor.DEFAULT.extractDescription(pers), desc2);
        // NOT OPENED YET.
        assertEquals("",elem1.getLog());
        assertEquals("",elem2.getLog());
        
        tc.open();
        tc.requestActive();
        assertEquals("",elem1.getLog());
        assertEquals("componentOpened-componentShowing-componentActivated-", elem2.getLog());
        assertEquals("",elem3.getLog());
        
        // test related hack, easy establishing a  connection from Desc->perspective
//        System.err.println("start caring..........................");
        hand.requestActive(Accessor.DEFAULT.createPerspective(desc1));
//        System.err.println("elem1=" + elem1.getLog());
//        System.err.println("elem2=" + elem2.getLog());
        assertEquals("componentOpened-componentShowing-componentActivated-", elem1.getLog());
        assertEquals("componentOpened-componentShowing-componentActivated-componentDeactivated-componentHidden-", elem2.getLog());
        assertEquals("",elem3.getLog());

        // do request active the same element, nothing should happen.
        // test related hack, easy establishing a  connection from Desc->perspective
        hand.requestActive(Accessor.DEFAULT.createPerspective(desc1));
        assertEquals("componentOpened-componentShowing-componentActivated-", elem1.getLog());
        assertEquals("componentOpened-componentShowing-componentActivated-componentDeactivated-componentHidden-", elem2.getLog());
        assertEquals("",elem3.getLog());
        
    }
    
    
}

