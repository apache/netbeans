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
