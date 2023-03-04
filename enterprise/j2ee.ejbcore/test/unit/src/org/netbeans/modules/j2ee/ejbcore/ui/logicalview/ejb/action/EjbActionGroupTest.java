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

import java.io.IOException;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.SourceUtils;
import org.netbeans.api.java.source.Task;
import org.netbeans.modules.j2ee.ejbcore.test.TestBase;
import org.openide.filesystems.FileObject;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author Martin Adamek
 */
public class EjbActionGroupTest extends TestBase {
    
    private EJBActionGroup ejbActionGroup;
    
    public EjbActionGroupTest(String testName) {
        super(testName);
    }
    
    protected void setUp() throws IOException {
        super.setUp();
        ejbActionGroup = new EJBActionGroup();
    }
    
    public void testEnable() throws Exception {
        // XXX temporarily commented out, too asynchonous (does not wait for RepositoryUpdate (initial) scan))
        // TestUtilities.copyStringToFileObject(testFO,
        //        "package foo;" +
        //        "public class TestClass {" +
        //        "   public TestClass() { " +
        //         "   }" +
        //         "   public void method() {" +
        //         "   }" +
        //         "}");
        // Node node = new AbstractNode(Children.LEAF, Lookups.singleton(testFO));
        // assertFalse(ejbActionGroup.enable(new Node[] {node}));

        TestModule testModule = createEjb21Module();
        // XXX issue 120855: SourceUtils.waitScanFinished() is not enough, it returns when
        // RepositoryUpdate is still scanning
        FileObject beanClass = testModule.getSources()[0].getFileObject("statelesslr/StatelessLRBean.java");
        JavaSource.forFileObject(beanClass).runWhenScanFinished(new Task<CompilationController>() {
            public void run(CompilationController parameter) throws Exception {
                // this task is here just to wait for RepositoryUpdater to finish scanning
            }
        }, true).get();
        Node node = new AbstractNode(Children.LEAF, Lookups.singleton(beanClass));
        assertTrue(ejbActionGroup.enable(new Node[] {node}));
    }
}
