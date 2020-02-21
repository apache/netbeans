/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.cnd.modelimpl.csm.core;

import java.util.ArrayList;
import java.util.List;
import org.netbeans.modules.cnd.modelimpl.csm.core.ParserQueue.Entry;
import org.netbeans.modules.cnd.modelimpl.csm.core.ParserQueue.Position;
import org.netbeans.modules.cnd.modelimpl.trace.TestModelHelper;
import org.netbeans.modules.cnd.test.CndBaseTestCase;

/**
 *
 */
public class ParserQueueTest extends CndBaseTestCase {

    private final ParserQueue queue;
    private TestModelHelper helper;
    private final List<FileImpl> projectFiles;
    
    public ParserQueueTest(String testName) {
        super(testName);
        queue = ParserQueue.testInstance();
        projectFiles = new ArrayList<>();
    }
    
    @Override
    public void setUp() throws Exception {
        super.setUp();
        System.setProperty("cnd.modelimpl.persistent", "false");
        queue.startup();
        helper = new TestModelHelper(true);
        helper.initParsedProject(getTestCaseDataDir().getAbsolutePath());
        projectFiles.addAll(helper.getProject().getAllFileImpls());
        assertEquals(4, projectFiles.size());
    }
    
    @Override
    public void tearDown() throws Exception {
        projectFiles.clear();
        helper.shutdown(true);
        queue.shutdown();
        super.tearDown();
    }

    public void testImmediateOrder() {
        execute(new OpInfo[] {
                OpInfo.add(0, Position.IMMEDIATE),
                OpInfo.add(1, Position.IMMEDIATE),
                OpInfo.add(2, Position.IMMEDIATE),
                OpInfo.add(3, Position.IMMEDIATE),
                OpInfo.poll(0),
                OpInfo.poll(1),
                OpInfo.poll(2),
                OpInfo.poll(3)
        });
    }
    
    public void testHeadOrder() {
        execute(new OpInfo[] {
                OpInfo.add(0, Position.HEAD),
                OpInfo.add(1, Position.HEAD),
                OpInfo.add(2, Position.HEAD),
                OpInfo.add(3, Position.HEAD),
                OpInfo.poll(3),
                OpInfo.poll(2),
                OpInfo.poll(1),
                OpInfo.poll(0)
        });
    }

    public void testTailOrder() {
        execute(new OpInfo[] {
                OpInfo.add(0, Position.TAIL),
                OpInfo.add(1, Position.TAIL),
                OpInfo.add(2, Position.TAIL),
                OpInfo.add(3, Position.TAIL),
                OpInfo.poll(0),
                OpInfo.poll(1),
                OpInfo.poll(2),
                OpInfo.poll(3)
        });
    }
    
    public void testSequence0001() {
        execute(new OpInfo[] {
                OpInfo.add(0, Position.IMMEDIATE),
                OpInfo.add(1, Position.IMMEDIATE),
                OpInfo.add(2, Position.HEAD),
                OpInfo.add(3, Position.TAIL),
                OpInfo.poll(0),
                OpInfo.poll(1),
                OpInfo.poll(2),
                OpInfo.poll(3)
        });
    }

    public void testSequence0002() {
        execute(new OpInfo[] {
                OpInfo.add(0, Position.TAIL),
                OpInfo.add(1, Position.HEAD),
                OpInfo.add(2, Position.IMMEDIATE),
                OpInfo.add(3, Position.IMMEDIATE),
                OpInfo.poll(2),
                OpInfo.poll(3),
                OpInfo.poll(1),
                OpInfo.poll(0)
        });
    }

    public void testSequence0003() {
        execute(new OpInfo[] {
                OpInfo.add(0, Position.TAIL),
                OpInfo.poll(0),
                OpInfo.add(1, Position.HEAD),
                OpInfo.poll(1),
                OpInfo.add(2, Position.IMMEDIATE),
                OpInfo.poll(2)
        });
    }

    protected void execute(OpInfo[] sequence) {
        for (int i = 0; i < sequence.length; ++i) {
            OpInfo op = sequence[i];
            if (op.add) {
                queue.add(projectFiles.get(op.fileIndex), FileImpl.DUMMY_STATE, op.pos);
            } else {
                Entry entry = null;
                try {
                    entry = queue.poll();
                } catch (InterruptedException e) {
                    fail("Unexpected InterruptedException during queue.poll()"); // NOI18N
                }
                assertNotNull(entry);
                FileImpl file = entry.getFile();
                assertNotNull(file);
                assertEquals(projectFiles.get(op.fileIndex), file);
            }
        }
    }

    protected static class OpInfo {
        public final boolean add;
        public final int fileIndex;
        public final Position pos;
        private OpInfo(boolean add, int fileIndex, Position pos) {
            this.add = add;
            this.fileIndex = fileIndex;
            this.pos = pos;
        }
        public static OpInfo add(int fileIndex, Position pos) {
            return new OpInfo(true, fileIndex, pos);
        }
        public static OpInfo poll(int fileIndex) {
            return new OpInfo(false, fileIndex, null);
        }
    }

}
