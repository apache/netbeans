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
package org.netbeans.core.startup;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.netbeans.junit.RandomlyFails;
import org.openide.util.RequestProcessor;


/**
 * Checks that it is possible to log to console.
 */
public class TopLoggingNbLoggerConsoleTest extends TopLoggingTest {
    private static ByteArrayOutputStream w;
    private static PrintStream ps;
    static {
        final PrintStream OLD = System.err;
        System.setProperty("netbeans.logger.console", "true");
        w = new ByteArrayOutputStream() {
            @Override
            public void write(byte[] b, int off, int len) {
                super.write(b, off, len);
            }

            @Override
            public void write(byte[] b) throws IOException {
                super.write(b);
            }

            @Override
            public void write(int b) {
                super.write(b);
            }

            @Override
            public String toString() {
                TopLogging.flush(false);
                OLD.flush();

                String retValue;                
                retValue = super.toString();
                return retValue;
            }
        };

        ps = new PrintStream(w);
        System.setErr(ps);
    }


    public TopLoggingNbLoggerConsoleTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        clearWorkDir();

        System.setProperty("netbeans.user", getWorkDirPath());

        // initialize logging
        TopLogging.initialize();

        ps.flush();
        w.reset();
    }

    @Override
    protected ByteArrayOutputStream getStream() {
        return w;
    }

    @RandomlyFails
    public void testFlushHappensQuickly() throws Exception {
        Logger.getLogger(TopLoggingTest.class.getName()).log(Level.INFO, "First visible message");

        Pattern p = Pattern.compile("INFO.*First visible message");

        Matcher d = null;
        String disk = null;
        // console gets flushed at 500ms
        for (int i = 0; i < 4; i++) {
            disk = w.toString("utf-8"); // this one is not flushing
            d = p.matcher(disk);
            if (!d.find()) {
                Thread.sleep(300);
            } else {
                return;
            }
        }

        fail("msg shall be logged to file: " + disk);
    }

    @RandomlyFails // NB-Core-Build #8225: "msg shall be logged to file: "
    public void testCycleWithConsoleLogger() throws Exception {
        ConsoleHandler h = new ConsoleHandler();

        try {
            Logger.getLogger("").addHandler(h);


            w.reset();
            Logger.getLogger(TopLoggingTest.class.getName()).log(Level.INFO, "First visible message");

            Pattern p = Pattern.compile("INFO.*First visible message");
            Matcher m = p.matcher(getStream().toString("utf-8"));

            Matcher d = null;
            String disk = null;
            // console gets flushed at 500ms
            for (int i = 0; i < 4; i++) {
                disk = w.toString("utf-8"); // this one is not flushing
                d = p.matcher(disk);
                if (!d.find()) {
                    Thread.sleep(300);
                } else {
                    if (w.size() > d.end() + 300) {
                        fail("File is too big\n" + w + "\nsize: " + w.size() + " end: " + d.end());
                    }

                    return;
                }
            }

            fail("msg shall be logged to file: " + disk);
        } finally {
            Logger.getLogger("").removeHandler(h);
            
        }
    }


    public void testDeadlockConsoleAndStdErr() throws Exception {
        ConsoleHandler ch = new ConsoleHandler();
        
        Logger root = Logger.getLogger("");
        root.addHandler(ch);
        try {
            doDeadlockConsoleAndStdErr(ch);
        } finally {
            root.removeHandler(ch);
        }
    }
    
    private void doDeadlockConsoleAndStdErr(final ConsoleHandler ch) {
        class H extends Handler implements Runnable {
            public void publish(LogRecord record) {
                try {
                    RequestProcessor.getDefault().post(this).waitFinished(100);
                } catch (InterruptedException ex) {
                    // ex.printStackTrace();
                }
            }

            public void flush() {
            }

            public void close() throws SecurityException {
            }
            
            public void run() {
                ch.publish(new LogRecord(Level.WARNING, "run"));
            }
        }
        H handler = new H();
        Logger.getLogger("stderr").addHandler(handler);
        
        System.err.println("Ahoj");
    }
    
}
