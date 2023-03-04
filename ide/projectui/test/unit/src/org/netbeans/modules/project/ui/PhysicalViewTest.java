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
package org.netbeans.modules.project.ui;

import java.io.IOException;
import org.netbeans.junit.NbTestCase;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;


/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public class PhysicalViewTest extends NbTestCase {
    
    public PhysicalViewTest(String n) {
        super(n);
    }


    public void testCreateNodeAndChangeDataObject() throws IOException {
        AbstractNode node = new AbstractNode(Children.LEAF) {
            @Override
            public void setExpert(boolean b) {
                setChildren(new Children.Array());
            }
        };
        Node res = new PhysicalView.ProjectIconNode(node, true);
        assertEquals(Boolean.TRUE, res.getValue("VCS_PHYSICAL"));
        assertTrue("No children", res.isLeaf());

        node.setExpert(true);
        
        assertFalse("Now it has children", res.isLeaf());
    }
}
