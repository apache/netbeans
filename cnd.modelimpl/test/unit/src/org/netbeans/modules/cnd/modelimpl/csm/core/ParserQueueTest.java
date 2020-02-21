/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
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
