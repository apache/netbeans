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
package org.openide.actions; 
 
import org.netbeans.junit.NbTestCase; 
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children; 
import org.openide.nodes.Node; 
 
public class OpenLocalExplorerActionTest extends NbTestCase { 
     
    public OpenLocalExplorerActionTest(String testName) { 
        super(testName); 
    } 
 
    public void testEnable() {
        OpenLocalExplorerAction action = new OpenLocalExplorerAction();
        Node leaf = new AbstractNode(Children.LEAF);
        Node parent = new AbstractNode(new Children.Array());
        assertFalse(action.enable(null));
        assertFalse(action.enable(new Node[0]));
        assertTrue(action.enable(new Node[] {parent}));
        assertFalse(action.enable(new Node[] {leaf}));
        assertFalse(action.enable(new Node[] {parent, parent}));
    } 
} 
