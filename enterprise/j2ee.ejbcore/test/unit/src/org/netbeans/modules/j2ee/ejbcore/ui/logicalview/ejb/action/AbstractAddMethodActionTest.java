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

package org.netbeans.modules.j2ee.ejbcore.ui.logicalview.ejb.action;

import org.netbeans.modules.j2ee.ejbcore.test.TestBase;
import org.netbeans.modules.j2ee.ejbcore.test.TestUtilities;
import org.openide.filesystems.FileObject;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author Martin Adamek
 */
public class AbstractAddMethodActionTest extends TestBase {
    
    public AbstractAddMethodActionTest(String testName) {
        super(testName);
    }

    public void testEnable() throws Exception {
        // regular POJO
        TestUtilities.copyStringToFileObject(testFO,
                "package foo;" +
                "public class TestClass {" +
                "   public TestClass() { " +
                "   }" +
                "   public void method() {" +
                "   }" +
                "}");
        Node node = new AbstractNode(Children.LEAF, Lookups.singleton(testFO));
        assertFalse(new AddMethodAction(new AddBusinessMethodStrategy()).enable(new Node[] {node}));
        assertFalse(new AddMethodAction(new AddCreateMethodStrategy()).enable(new Node[] {node}));

        TestModule testModule = createEjb21Module();
        
        // EJB 2.1 Stateless Session Bean
        FileObject beanClass = testModule.getSources()[0].getFileObject("statelesslr/StatelessLRBean.java");
        node = new AbstractNode(Children.LEAF, Lookups.singleton(beanClass));
        assertTrue(new AddMethodAction(new AddBusinessMethodStrategy()).enable(new Node[] {node}));
        assertFalse(new AddMethodAction(new AddCreateMethodStrategy()).enable(new Node[] {node}));
        
        // EJB 2.1 Entity Bean
        beanClass = testModule.getSources()[0].getFileObject("cmplr/CmpLRBean.java");
        node = new AbstractNode(Children.LEAF, Lookups.singleton(beanClass));
        assertTrue(new AddMethodAction(new AddBusinessMethodStrategy()).enable(new Node[] {node}));
        assertTrue(new AddMethodAction(new AddCreateMethodStrategy()).enable(new Node[] {node}));
    }

    private static final class AddMethodAction extends AbstractAddMethodAction {
        public AddMethodAction(AbstractAddMethodStrategy strategy) {
            super(strategy);
        }
    }
    
}
