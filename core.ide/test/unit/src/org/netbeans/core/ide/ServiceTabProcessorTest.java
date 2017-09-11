/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.core.ide;

import java.awt.Image;
import java.beans.BeanInfo;
import java.util.Collection;
import org.junit.Test;
import org.netbeans.api.core.ide.ServicesTabNodeRegistration;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.ImageUtilities;
import org.openide.util.lookup.Lookups;
import static org.junit.Assert.*;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public class ServiceTabProcessorTest {

    @Test
    public void testNodesGeneratedCorrectly() throws Exception {
        Collection<? extends Node> arr = Lookups.forPath("UI/Runtime").lookupAll(Node.class);
        assertEquals("One node is there: " + arr, 1, arr.size());
        Node n = arr.iterator().next();

        assertEquals("my1", n.getName());
        assertEquals("disp1", n.getDisplayName());
        assertEquals("By default short description delegates to displayName", n.getDisplayName(), n.getShortDescription());

        Image img1 = ImageUtilities.loadImage("org/netbeans/core/ide/TestIcon1.png");
        Image img2 = ImageUtilities.loadImage("org/netbeans/core/ide/TestIcon2.png");

        assertSame("icon1 is in use", img1, n.getIcon(BeanInfo.ICON_COLOR_16x16));

        Node[] subNodes = n.getChildren().getNodes(true);
        assertEquals("Two subnodes", 2, subNodes.length);

        // now everything is initialized

        assertEquals("my2", n.getName());
        assertEquals("disp2", n.getDisplayName());
        assertEquals("short2", n.getShortDescription());
        assertSame("icon2 is in use", img2, n.getIcon(BeanInfo.ICON_COLOR_16x16));

    }
    
    @Test
    public void testDoubleNode() {
        Node n1 = new ServicesTab.ServicesNode();
        Node n2 = new ServicesTab.ServicesNode();
        
        assertEquals("One subnode there", 1, n1.getChildren().getNodes(true).length);
        assertEquals("One subnode there", 1, n2.getChildren().getNodes(true).length);
        
    }

    @ServicesTabNodeRegistration(
        name="my1", displayName="org.netbeans.core.ide.TestBundle#NAME", position=10,
        iconResource="org/netbeans/core/ide/TestIcon1.png"
    )
    public static class MyNode extends AbstractNode {
        public MyNode() {
            this(true);
        }
        private MyNode(boolean addChildren) {
            super(new Children.Array());
            setName("my2");
            setDisplayName("disp2");
            setShortDescription("short2");
            setIconBaseWithExtension("org/netbeans/core/ide/TestIcon2.png");

            if (addChildren) {
                getChildren().add(new Node[] { new MyNode(false), new MyNode(false) });
            }
        }
    }
}
