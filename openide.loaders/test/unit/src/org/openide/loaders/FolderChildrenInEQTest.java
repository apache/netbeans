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
package org.openide.loaders;

import java.util.concurrent.CountDownLatch;
import junit.framework.Test;
import org.netbeans.junit.RandomlyFails;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Mutex;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public class FolderChildrenInEQTest extends FolderChildrenTest {

    public FolderChildrenInEQTest(String testName) {
        super(testName);
    }
    
    public static Test suite() {
        return new FolderChildrenInEQTest("testCountNumberOfNodesWhenUsingFormLikeLoader");
    }
    
    @Override
    protected boolean runInEQ() {
        return true;
    }
    
    public void testDeadlockWaitingForDelayedNode() throws Exception {
        Pool.setLoader(FormKitDataLoader.class);
        
        FileUtil.createFolder(FileUtil.getConfigRoot(), "FK/A");
        
        FileObject bb = FileUtil.getConfigFile("/FK");
        final DataFolder folder = DataFolder.findFolder(bb);
        final Node node = folder.getNodeDelegate();
        
        
        Node[] one = node.getChildren().getNodes(true);
        assertNodes(one, "A");
        
        FormKitDataLoader.waiter = new CountDownLatch(1);
        FileUtil.createData(FileUtil.getConfigRoot(), "FK/B");
        Node[] arr = Children.MUTEX.readAccess(new Mutex.ExceptionAction<Node[]>() {
            @Override
            public Node[] run() throws Exception {
                // don't deadlock
                return node.getChildren().getNodes(true);
            }
        });
        
        FormKitDataLoader.waiter.countDown();
        arr = node.getChildren().getNodes(true);
        
        assertNotNull("We have data object now", arr[1].getLookup().lookup(DataObject.class));
        
        assertFalse("No leaf", arr[0].isLeaf());
        assertTrue("File B is leaf", arr[1].isLeaf());
    }

    @RandomlyFails // NB-Core-Build #6728: Accepts only Ahoj expected:<1> but was:<2>
    @Override public void testChildrenCanGC() throws Exception {
        super.testChildrenCanGC();
    }
    
}
