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
package org.netbeans.core.startup.logging;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.StreamHandler;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.RandomlyFails;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public class DispatchingHandlerTest extends NbTestCase {
    
    public DispatchingHandlerTest(String s) {
        super(s);
    }
    
    @RandomlyFails // NB-Core-Build #9138, #9370: Unstable
    public void testContinuousMessagesShouldNotPreventOutput() throws InterruptedException {
        class MyHandler extends Handler {
            final List<LogRecord> records = new CopyOnWriteArrayList<LogRecord>();

            @Override
            public void publish(LogRecord record) {
                records.add(record);
            }

            @Override
            public void flush() {
            }

            @Override
            public void close() throws SecurityException {
                records.clear();
            }
            
        }
        MyHandler mh = new MyHandler();
        DispatchingHandler dh = new DispatchingHandler(mh, 100);
        
        for (int i = 0; i < 100; i++) {
            dh.publish(new LogRecord(Level.INFO, "" + i));
            Thread.sleep(10);
            if (i > 50 && mh.records.isEmpty()) {
                fail("There should be some records when we are at round " + i);
            }
        }
        dh.flush();
        
        assertEquals("One hundered records now", 100, mh.records.size());
    }
    
    public void testOwnFormatter() throws UnsupportedEncodingException {
        class MyFrmtr extends Formatter {
            private int cnt;
            @Override
            public String format(LogRecord record) {
                cnt++;
                return record.getMessage();
            }
        }
        MyFrmtr my = new MyFrmtr();
        
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        StreamHandler sh = new StreamHandler(os, NbFormatter.FORMATTER);
        DispatchingHandler dh = new DispatchingHandler(sh, 10);
        dh.setFormatter(my);
        dh.publish(new LogRecord(Level.WARNING, "Ahoj"));
        dh.flush();
        String res = new String(os.toByteArray(), "UTF-8");
        assertEquals("Only the message is written", "Ahoj", res);
        assertEquals("Called once", 1, my.cnt);
    }
}
