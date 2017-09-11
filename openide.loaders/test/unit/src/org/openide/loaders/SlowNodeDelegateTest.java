/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
 */
package org.openide.loaders;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;
import org.netbeans.junit.MockServices;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.nodes.Node;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public class SlowNodeDelegateTest extends NbTestCase {
    private FileObject dir;
    private FileObject a;
    private FileObject b;

    public SlowNodeDelegateTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        MockServices.setServices(SlowL.class);
        clearWorkDir();
        dir = FileUtil.toFileObject(getWorkDir());
        a = dir.createData("slow.a");
        b = dir.createData("slow.b");
    }

    public void testAIsBlockedButBCanBeCreated() throws Exception {
        final SlowND objA = (SlowND) DataObject.find(a);
        SlowND objB = (SlowND) DataObject.find(b);
        
        final RequestProcessor RP = new RequestProcessor("Node for A");
        final Node[] nodeA = { null };
        RequestProcessor.Task task = RP.post(new Runnable() {
            @Override
            public void run() {
                nodeA[0] = objA.getNodeDelegate();
            }
        });
        assertFalse("Did not finish yet", task.waitFinished(500));
        assertNull("Node not yet created", nodeA[0]);
        
        Node nodeB = objB.getNodeDelegate();
        assertNotNull("Meanwhile other nodes can be created", nodeB);
        
        
        assertNull("Node A still not created", nodeA[0]);
        synchronized (objA) {
            objA.notifyAll();
        }
        task.waitFinished();
        
        assertNotNull("Node A also created", nodeA[0]);
    }
    

    public static final class SlowL extends UniFileLoader {
        public SlowL() {
            super(SlowND.class);
            getExtensions().addExtension("a");
            getExtensions().addExtension("b");
        }
        
        @Override
        protected MultiDataObject createMultiObject(FileObject primaryFile) throws DataObjectExistsException, IOException {
            return new SlowND(primaryFile, this);
        }
    }
    
    private static final class SlowND extends MultiDataObject {
        private final AtomicInteger beingCreated = new AtomicInteger(0);
        public SlowND(FileObject fo, MultiFileLoader loader) throws DataObjectExistsException {
            super(fo, loader);
        }

        @Override
        protected Node createNodeDelegate() {
            assertEquals("Only one call to createNodeDelegate for " + getPrimaryFile(), 1, beingCreated.incrementAndGet());
            
            if (getPrimaryFile().hasExt("a")) {
                synchronized (this) {
                    try {
                        wait();
                    } catch (InterruptedException ex) {
                        throw new IllegalStateException(ex);
                    }
                }
            }
            return super.createNodeDelegate();
        }
        
        
    }
}
