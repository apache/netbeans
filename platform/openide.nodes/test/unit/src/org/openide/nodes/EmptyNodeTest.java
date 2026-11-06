/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.openide.nodes;

import java.beans.BeanInfo;
import java.util.Arrays;
import org.netbeans.junit.NbTestCase;

public class EmptyNodeTest extends NbTestCase {

    public EmptyNodeTest(String name) {
        super(name);
    }

    public void testProperties() throws Exception {
        Node legacyEmpty = new AbstractNode(Children.LEAF);
        Node newEmpty = Node.EMPTY;
        int[] iconTypes = new int[] {
            BeanInfo.ICON_COLOR_16x16,
            BeanInfo.ICON_COLOR_32x32,
            BeanInfo.ICON_MONO_16x16,
            BeanInfo.ICON_MONO_32x32
        };
        for (int i = 0; i < iconTypes.length; i++) {
            assertEquals(legacyEmpty.getIcon(iconTypes[i]), newEmpty.getIcon(iconTypes[i]));
        }
        assertTrue(Arrays.equals(legacyEmpty.getPropertySets(), newEmpty.getPropertySets()));
    }

    public void testSetName() throws Exception {
        try {
            Node.EMPTY.setName("");
            fail("Expected exception didn't occur.");
        } catch (UnsupportedOperationException ex) {
            //ok
        }
        try {
            Node.EMPTY.setDisplayName("");
            fail("Expected exception didn't occur.");
        } catch (UnsupportedOperationException ex) {
            //ok
        }
        try {
            Node.EMPTY.setShortDescription("");
            fail("Expected exception didn't occur.");
        } catch (UnsupportedOperationException ex) {
            //ok
        }
        try {
            Node.EMPTY.cloneNode().setName("");
            fail("Expected exception didn't occur.");
        } catch (UnsupportedOperationException ex) {
            //ok
        }
        try {
            Node.EMPTY.cloneNode().setDisplayName("");
            fail("Expected exception didn't occur.");
        } catch (UnsupportedOperationException ex) {
            //ok
        }
        try {
            Node.EMPTY.cloneNode().setShortDescription("");
            fail("Expected exception didn't occur.");
        } catch (UnsupportedOperationException ex) {
            //ok
        }
    }
}
