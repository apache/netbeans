/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2015 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2015 Sun Microsystems, Inc.
 */
package org.netbeans.modules.db.sql.history;

import java.util.Date;
import org.junit.Test;

public class SQLHistoryTest {

    public SQLHistoryTest() {
    }

    @Test
    public void testConcurrency() {
        // Enforce error condition by writing from multiple threads as fast
        // as possible -- this test is an approximation but at creation time
        // reprocuced the bug
        //
        // https://netbeans.org/bugzilla/show_bug.cgi?id=256238
        
        SQLHistory hist = new SQLHistory();
        hist.setHistoryLimit(10);

        int threadCount = 10;
        int inserts = 1000;

        Worker[] writer = new Worker[threadCount];
        
        for(int i = 0; i < writer.length; i++) {
            writer[i] = new Worker(threadCount, inserts, hist);
        }
        
        for(int i = 0; i < writer.length; i++) {
            writer[i].start();
        }
        
        for (int i = 0; i < writer.length; i++) {
            try {
                writer[i].join(10 * 1000);
                assert writer[i].isNormalEnd();
            } catch (InterruptedException ex) {
                assert false : "Was interrupted";
            }
        }
    }

    private static class Worker extends Thread {

        private final int threadId;
        private final int writes;
        private final SQLHistory history;
        private boolean normalEnd = false;

        public Worker(int threadId, int writes, SQLHistory history) {
            this.threadId = threadId;
            this.writes = writes;
            this.history = history;
        }

        @Override
        public void run() {
            for (int i = 0; i < writes; i++) {
                SQLHistoryEntry sqe = new SQLHistoryEntry("DemoURL " + threadId, "SQL "
                        + i, new Date());
                history.add(sqe);
            }
            normalEnd = true;
        }

        public boolean isNormalEnd() {
            return normalEnd;
        }
    }
}
