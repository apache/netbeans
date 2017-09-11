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
package org.openide.loaders;
import javax.swing.SwingUtilities;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.*;
import org.openide.nodes.Node;
import org.openide.util.*;
import org.openide.util.actions.CallableSystemAction;

/**
 *
 * @author Jiri Rechtacek
 */
public class InstanceNodeTest extends NbTestCase {
    Node node;

    public InstanceNodeTest (String testName) {
        super (testName);
    }

    protected void setUp () throws Exception {
        FileObject root = FileUtil.getConfigRoot ();
        DataObject dobj = InstanceDataObject.create (DataFolder.findFolder (root), null, A.class);
        node = dobj.getNodeDelegate ();
        assertTrue ("Is InstanceNode", node instanceof InstanceNode);
    }

    /**
     * Test of getDisplayName method, of class org.openide.loaders.InstanceNode.
     */
    public void testGetDisplayName () throws Exception {
        Node instance = node;
        
        String expResult = "Ahoj";
        // node's name is calculated later, let's wait
        SwingUtilities.invokeAndWait (new Runnable () {
            public void run () {
                
            }
            
        });
        String result = instance.getDisplayName();
        assertEquals(expResult, result);
    }
    
    public static class A extends CallableSystemAction {
        public void performAction () {
        }

        public String getName () {
            assertTrue ("Called from AWT", SwingUtilities.isEventDispatchThread ());
            return "Ahoj";
        }

        public HelpCtx getHelpCtx () {
            assertTrue ("Called from AWT", SwingUtilities.isEventDispatchThread ());
            return HelpCtx.DEFAULT_HELP;
        }
        
    }

}
