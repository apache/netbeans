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

package org.openide.loaders;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.logging.Logger;
import org.netbeans.junit.Log;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.RandomlyFails;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.nodes.Node;
import org.openide.util.RequestProcessor;

public class FolderChildrenGCRaceConditionTest extends NbTestCase {
    private Logger LOG;
    
    public FolderChildrenGCRaceConditionTest() {
        super("");
    }
    
    public FolderChildrenGCRaceConditionTest(java.lang.String testName) {
        super(testName);
    }
    
    @Override
    protected void setUp() throws Exception {
        clearWorkDir();
        LOG = Logger.getLogger(FolderChildrenGCRaceConditionTest.class.getName());

        FileObject[] arr = FileUtil.getConfigRoot().getChildren();
        for (int i = 0; i < arr.length; i++) {
            arr[i].delete();
        }
    }

    @RandomlyFails // NB-Core-Build #1087
    public void testChildrenCanBeSetToNullIfGCKicksIn () throws Exception {
        FileObject f = FileUtil.createData(FileUtil.getConfigRoot(), "folder/node.txt");
        
        DataFolder df = DataFolder.findFolder(f.getParent());
        Node n = df.getNodeDelegate();
        
        Node[] arr = n.getChildren().getNodes(true);
        assertEquals("Ok, one", 1, arr.length);
        final Reference<?> ref = new WeakReference<Node>(arr[0]);
        arr = null;
        
        class R implements Runnable {
            @Override
            public void run() {
                LOG.info("Ready to GC");
                assertGC("Node can go away in the worst possible moment", ref);
                LOG.info("Gone");
            }
        }
        R r = new R();
        RequestProcessor.Task t = new RequestProcessor("Inter", 1, true).post(r);
        
        Log.controlFlow(Logger.getLogger("org.openide.loaders"), null,
            "THREAD:FolderChildren_Refresh MSG:Children computed" +
            "THREAD:FolderChildren_Refresh MSG:notifyFinished.*" +
            "THREAD:Inter MSG:Gone.*" +
            "THREAD:Finalizer MSG:RMV.*" +
            "THREAD:FolderChildren_Refresh MSG:Clearing the ref.*" +
            "", 200);
        
        LOG.info("Before getNodes(true");
        int cnt = n.getChildren().getNodes(true).length;
        LOG.info("Children are here: " + cnt);
        t.cancel();
        LOG.info("Cancel done");
        assertEquals("Count is really one", 1, cnt);
    }
   
}
