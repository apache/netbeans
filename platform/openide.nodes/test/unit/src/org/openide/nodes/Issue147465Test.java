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

package org.openide.nodes;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import org.netbeans.junit.Log;
import org.netbeans.junit.NbTestCase;
import org.openide.util.Exceptions;

/**
 *
 * @author Holy
 */
public class Issue147465Test extends NbTestCase {
    Logger logger = Logger.getLogger(Issue147465Test.class.getName());

    public Issue147465Test(java.lang.String testName) {
        super(testName);
    }

    @Override
    protected Level logLevel() {
        return Level.FINE;
    }

    @Override
    protected int timeOut() {
        return 30000;
    }
    
    public void testDeadlock() throws InterruptedException, InvocationTargetException {
        class Keys extends Children.Keys {

            public Keys(String... args) {
                super(true);
                if (args != null && args.length > 0) {
                    setKeys(args);
                }
            }

            public void keys(String... args) {
                super.setKeys(args);
            }

            public void keys(Collection args) {
                super.setKeys(args);
            }

            protected Node[] createNodes(Object key) {
                AbstractNode an = new AbstractNode(Children.LEAF);
                an.setName(key.toString());
                return new Node[]{an};
            }
        }
        
        class GetFromSnapshot implements Runnable {
            List<Node> snapshot;

            public GetFromSnapshot(List<Node> snapshot) {
                this.snapshot = snapshot;
            }

            public void run() {
                logger.info("getting from snapshot");
                Node n = snapshot.get(0);
                logger.info("obtained from snapshot");
            }
            
        }
        
        Keys ch = new Keys("a", "b", "c");
        Node root = new FilterNode(new AbstractNode(ch));
        Node[] nodes = root.getChildren().getNodes();
        Reference<Object> ref = new WeakReference<Object>(nodes[0]);
        
        Log.controlFlow(Logger.getLogger("org.openide.nodes"), Logger.getLogger("1.2.3.4.5"), 
              "THREAD: AWT-EventQueue-0 MSG: getting from snapshot"
            + "THREAD: Active Reference Queue Daemon MSG: register node"
            + "THREAD: AWT-EventQueue-0 MSG: obtained from snapshot"
            , 5000);
        
        GetFromSnapshot g = new GetFromSnapshot(root.getChildren().snapshot());
        SwingUtilities.invokeLater(g);
        Thread.sleep(1000);
        nodes = null;
        assertGC("should be gced", ref);
        SwingUtilities.invokeAndWait(new Runnable() {

            public void run() {
            }
        });
    }
}
