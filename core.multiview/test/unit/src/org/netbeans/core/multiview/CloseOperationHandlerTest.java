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
import org.netbeans.core.api.multiview.MultiViews;
import org.netbeans.core.spi.multiview.CloseOperationHandler;
import org.netbeans.core.spi.multiview.CloseOperationState;
import org.netbeans.core.spi.multiview.MultiViewDescription;
import org.netbeans.core.spi.multiview.MultiViewFactory;
import org.netbeans.junit.*;

import org.openide.windows.*;


/** 
 *
 * @author Milos Kleint
 */
public class CloseOperationHandlerTest extends NbTestCase {
    
    public static Test suite() {
        return GraphicsEnvironment.isHeadless() ? new TestSuite() : new TestSuite(CloseOperationHandlerTest.class);
    }

    public CloseOperationHandlerTest(String name) {
        super (name);
    }
    
    protected boolean runInEQ () {
        return true;
    }
    
    
    public void testCreateMultiView2 () throws Exception {
        MultiViewDescription desc1 = new MVDesc("desc1", null, 0, new NonClosableElem());
        MultiViewDescription desc2 = new MVDesc("desc2", null, 0, new NonClosableElem());
        MultiViewDescription desc3 = new MVDesc("desc3", null, 0, new MVElem());
        MultiViewDescription[] descs = new MultiViewDescription[] { desc1, desc2, desc3 };
        MyCloseHandler close = new MyCloseHandler();
        TopComponent tc = MultiViewFactory.createMultiView(descs, desc1, close);
        assertNotNull(tc);
        
         tc.open();
        // just one element as shown..
        close.supposed = 1;
        tc.close();
        assertTrue(close.wasUsed);
        
        tc.open();
        MultiViewHandler handler = MultiViews.findMultiViewHandler(tc);
        handler.requestActive(handler.getPerspectives()[2]);
        // 1 shall be checked - elem3 can be closed.
        close.supposed = 1;
        // do not allow closing..
        close.canClose = false;
        
        tc.close();  
        assertTrue(tc.isOpened());

        
        handler.requestActive(handler.getPerspectives()[1]);
        // 2 shall be checked.
        close.supposed = 2;
        // allow closing..
        close.canClose = true;
        
        tc.close();  
        assertTrue(!tc.isOpened());
        
    }

    
    private class MyCloseHandler implements CloseOperationHandler {
        
        public boolean wasUsed = false;
        public int supposed = 0;
        public boolean canClose = true;
        
        public boolean resolveCloseOperation(CloseOperationState[] elements) {
            wasUsed = true;
            if (supposed != elements.length) {
                throw new IllegalStateException("A different number of elements returned. Expected=" + supposed + " but was:" + elements.length);
            }
            return canClose;
        }
        
        
    }
    
    private class NonClosableElem extends MVElem {
        
        public CloseOperationState canCloseElement() {
            return MultiViewFactory.createUnsafeCloseState("ID", MultiViewFactory.NOOP_CLOSE_ACTION, MultiViewFactory.NOOP_CLOSE_ACTION);
        }
        
    }
    
}

