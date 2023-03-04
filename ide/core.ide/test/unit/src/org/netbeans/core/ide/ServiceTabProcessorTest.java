/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
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
