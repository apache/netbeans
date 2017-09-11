/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.openide.explorer.view;

import java.awt.EventQueue;
import org.netbeans.junit.NbTestCase;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;

public class ViewUtilTest extends NbTestCase {
    
    public ViewUtilTest(String n) {
        super(n);
    }

    @Override
    protected boolean runInEQ() {
        return true;
    }

    @Override
    protected int timeOut() {
        return 20000;
    }
    
    public void testRenameNormalNode() {
        Node n = new AbstractNode(Children.LEAF) {
            @Override
            public void setName(String s) {
                assertTrue("In AWT", EventQueue.isDispatchThread());
                super.setName(s);
            }
        };
        
        n.setName("newName");
        assertEquals("newName", n.getName());
    }

    public void testRenameForSlowNode() {
        Node n = new AbstractNode(Children.LEAF) {
            boolean renamed;
            
            @Override
            public synchronized void setName(String s) {
                renamed = true;
                notifyAll();
                assertFalse("Not in AWT", EventQueue.isDispatchThread());
                super.setName(s);
            }

            @Override
            public synchronized String toString() {
                while (!renamed) {
                    try {
                        wait();
                    } catch (InterruptedException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
                return super.getName();
            }
        };

        n.setValue("slowRename", true);
        ViewUtil.nodeRename(n, "newName");
        assertEquals("newName", n.toString());
    }
}
