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
 * Software is Sun Microsystems, Inc. Portions Copyright 2002 Sun
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

package org.netbeans.core.windows;

import org.netbeans.junit.*;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;

import org.openide.windows.*;


/** 
 * 
 * @author Dafe Simonek
 */
public class Bug82319Test extends NbTestCase {

    public Bug82319Test (String name) {
        super (name);
    }

    protected boolean runInEQ () {
        return true;
    }
     
    public void test82319ActivatedNodesUpdate () throws Exception {
        Node node1 = new AbstractNode(Children.LEAF);
        Node node2 = new AbstractNode(Children.LEAF);
        
        Mode mode = WindowManagerImpl.getInstance().createMode("test82319Mode",
                Constants.MODE_KIND_EDITOR, Constants.MODE_STATE_JOINED, false, new SplitConstraint[0] );
        
        TopComponent tc1 = new TopComponent();
        tc1.setActivatedNodes(new Node[] { node1 });
        mode.dockInto(tc1);
        
        TopComponent tc2 = new TopComponent();
        tc2.setActivatedNodes(null);
        mode.dockInto(tc2);
        
        tc1.open();
        tc2.open();
        
        tc1.requestActive();
        
        System.out.println("Checking bugfix 82319...");
        Node[] actNodes = WindowManager.getDefault().getRegistry().getActivatedNodes();
        assertTrue("Expected 1 activated node, but got " + actNodes.length, actNodes.length == 1);
        assertSame("Wrong activated node", actNodes[0], node1);

        tc2.requestActive();
        
        // activated nodes should stay the same, tc2 doesn't have any
        actNodes = WindowManager.getDefault().getRegistry().getActivatedNodes();
        assertTrue("Expected 1 activated node, but got " + actNodes.length, actNodes.length == 1);
        assertSame("Wrong activated node", actNodes[0], node1);
        
        tc1.setActivatedNodes(new Node[] { node2 });
        
        System.out.println("Checking update of activated nodes...");
        // activated nodes should change, as still nodes should be grabbed from tc1 
        actNodes = WindowManager.getDefault().getRegistry().getActivatedNodes();
        assertTrue("Expected 1 activated node, but got " + actNodes.length, actNodes.length == 1);
        assertSame("Wrong activated node", actNodes[0], node2);
    }
    
}
