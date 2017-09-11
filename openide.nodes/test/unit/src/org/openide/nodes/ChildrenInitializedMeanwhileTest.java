/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.openide.nodes;

import java.util.concurrent.CountDownLatch;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import org.netbeans.junit.NbTestCase;
import org.openide.util.RequestProcessor;
import org.openide.util.RequestProcessor.Task;

public class ChildrenInitializedMeanwhileTest extends NbTestCase {
    private static final RequestProcessor RP = new RequestProcessor("Test");
    private volatile boolean wasNotified;

    public ChildrenInitializedMeanwhileTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        EntrySupportLazy.LOGGER.addHandler(new java.util.logging.Handler() {
            {
                setLevel(Level.FINER);
                EntrySupportLazy.LOGGER.setLevel(Level.FINER);
            }
            @Override
            public void publish(LogRecord record) {
                if (record.getMessage().startsWith("setEntries():")) {
                    notifyRecordIsHere();
                }
            }

            @Override
            public void flush() {
            }

            @Override
            public void close() throws SecurityException {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            
        });
    }
    
    final CountDownLatch initializing = new CountDownLatch(1);
    final void notifyRecordIsHere() {
        try {
            initializing.await();
            wasNotified = true;
        } catch (InterruptedException ex) {
            throw new IllegalStateException(ex);
        }
    }
    
    
    public void testLongInitParallelSet() throws Exception {
        
        class K extends Children.Keys<String> {
            public K() {
                super(true);
            }
            
            @Override
            protected void addNotify() {
            }

            void oneKey() {
                setKeys(new String[] { "1" });
            }

            @Override
            protected Node[] createNodes(String key) {
                AbstractNode an = new AbstractNode(Children.LEAF);
                an.setName(key);
                return new Node[] { an };
            }
        }
        
        final K k = new K();
        final Node root = new AbstractNode(k);
        
        Task task = RP.post(new Runnable() {
            @Override
            public void run() {
                k.oneKey();
            }
        });
        
        task.waitFinished(100);
        assertFalse("Not finished after waiting a bit", task.isFinished());
        
        initializing.countDown();
        
        Node[] after = root.getChildren().getNodes();
        assertEquals("One", 1, after.length);
        
        assertTrue("The message has been logged", wasNotified);
    }
}
