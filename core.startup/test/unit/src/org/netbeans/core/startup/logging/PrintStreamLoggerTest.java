/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2017 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2017 Sun Microsystems, Inc.
 */
package org.netbeans.core.startup.logging;

import java.io.PrintStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import static org.netbeans.core.startup.logging.PrintStreamLogger.BUFFER_SHRINK_TIME;
import static org.netbeans.core.startup.logging.PrintStreamLogger.BUFFER_THRESHOLD;
import org.netbeans.junit.NbTestCase;

/**
 *
 * @author sdedic
 */
public class PrintStreamLoggerTest extends NbTestCase {
    
    private static class LH extends Handler {
        List<LogRecord> records = new ArrayList<>();
        
        @Override
        public void publish(LogRecord record) {
            records.add(record);
        }

        @Override
        public void flush() {
        }

        @Override
        public void close() throws SecurityException {
        }
    }
    
    private LH handler = new LH();
    
    private Logger logger = Logger.getLogger("test.pslogger");
    
    private PrintStreamLogger ps = (PrintStreamLogger)PrintStreamLogger.create("test.pslogger");
    
    private Object psLock;

    public PrintStreamLoggerTest(String name) {
        super(name);
    }
    
    public void setUp() throws Exception {
        logger.addHandler(handler);
        logger.setLevel(Level.INFO);
        logger.setUseParentHandlers(false);
        Field f = ps.getClass().getDeclaredField("lock");
        f.setAccessible(true);
        psLock = f.get(ps);
    }
    
    public void tearDown() {
        LogManager.getLogManager().getLogger("test.pslogger").removeHandler(handler);
    }
    
    public void testContinousLogingFlushes() throws Exception {
        int count = 0;
        StringBuilder data = new StringBuilder();
        for (int i = 0; i < 1000; i++) {
            data.append("12345678901234567890123456789012345678901234567890\n");
        }
        do {
            Thread.sleep(50);
            if (count < (BUFFER_THRESHOLD / data.length())) {
                assertEquals("Not buffering properly", 0, handler.records.size());
            }
            // 50k chars + newline
            ps.println(data);
        } while (++count < ((BUFFER_THRESHOLD / data.length()) * 2));
        assertFalse(handler.records.isEmpty());
   }

    public void testMemoryReclaimed() throws Exception {
        int count = 0;
        StringBuilder data = new StringBuilder();
        for (int i = 0; i < 1000; i++) {
            data.append("12345678901234567890123456789012345678901234567890");
        }
        
        do {
            Thread.sleep(50);
            if (count < (BUFFER_THRESHOLD / data.length())) {
                assertEquals("Not buffering properly", 0, handler.records.size());
            }
            // 50k chars + newline
            ps.println(data);
        } while (++count < ((BUFFER_THRESHOLD / data.length()) * 2));
        // check that the logger has started to flush already
        assertFalse(handler.records.isEmpty());

        // wait until flush, check that the buffer is still large.
        synchronized (psLock) {
            ps.println(data);
            
            // wait & release the lock:
            psLock.wait(1000);

            int[] capacity = ps.bufferSizes();
            assertTrue(capacity[1] >= BUFFER_THRESHOLD);
        }
        Thread.sleep(5 * BUFFER_SHRINK_TIME / 2);
        synchronized (psLock) {
            ps.println(data);
            
            // wait & release the lock:
            psLock.wait(1000);
        }
        int[] capacity = ps.bufferSizes();
        assertTrue(capacity[1] < BUFFER_THRESHOLD);
   }
}
