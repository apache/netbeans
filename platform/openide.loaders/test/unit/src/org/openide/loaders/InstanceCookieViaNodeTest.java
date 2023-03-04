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
package org.openide.loaders;

import java.io.File;
import java.util.List;
import java.util.logging.Level;
import org.netbeans.junit.NbTestCase;
import org.openide.cookies.InstanceCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.nodes.Node;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public class InstanceCookieViaNodeTest extends NbTestCase {
    private static final int CNT = 100;
    private File wd;
    private FileObject fo;
    
    public InstanceCookieViaNodeTest(String name) {
        super(name);
    }

    @Override
    protected Level logLevel() {
        return Level.SEVERE;
    }
    
    

    @Override
    protected boolean runInEQ() {
        return true;
    }

    @Override
    protected void setUp() throws Exception {
        clearWorkDir();
        wd = getWorkDir();
        
        for (int i = 0; i < CNT; i++) {
            File f = new File(wd, "i" + i + ".instance");
            f.createNewFile();
        }
        
        fo = FileUtil.toFileObject(wd);
    }
    
    public void testAllTheNodesHaveInstanceDataObject() throws Exception {
        InstanceCookie ic;
        
        Node nd = DataFolder.findFolder(fo).getNodeDelegate();
        for (int round = 0; ; round++) {
            List<Node> s = nd.getChildren().snapshot();
            for (int i = 0; i < s.size(); i++) {
                final Node n = s.get(i);
                ic = n.getLookup().lookup(InstanceDataObject.class);
                if (ic == null) {
                    fail("No InstanceCookie for " + i + "th node in round " + round + ": " + n);
                }
            }
            if (s.size() == CNT) {
                break;
            }
            Thread.sleep(10);
        }
        clearWorkDir();
    }

    
}
