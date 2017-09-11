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
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.RandomlyFails;


/**
 * Checks the behaviour of NetBeans logging support.
 */
public class TopLoggingStartLogTest extends NbTestCase {
    private ByteArrayOutputStream w;
    private Handler handler;
    private Logger logger;
    
    static {
        System.setProperty("org.netbeans.log.startup", "print"); // NOI18N
    }
    
    public TopLoggingStartLogTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        clearWorkDir();

        System.setProperty("netbeans.user", getWorkDirPath());

        // initialize logging
        TopLogging.initialize();

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
                handler.flush();

                String retValue;                
                retValue = super.toString();
                return retValue;
            }

        };

        handler = TopLogging.createStreamHandler(new PrintStream(getStream()));
        logger = Logger.getLogger("");
        Handler[] old = logger.getHandlers();
// do not remove default handlers from CLIOptions.initialize():
//        for (int i = 0; i < old.length; i++) {
//            logger.removeHandler(old[i]);
//        }
        logger.addHandler(handler);

        w.reset();

    }


    protected ByteArrayOutputStream getStream() {
        return w;
    }

    @RandomlyFails // NB-Core-Build #1659
    public void testProgress() throws Exception {
        StartLog.logProgress("First visible message");

        Pattern p = Pattern.compile("@[0-9]+.*First visible message");
        Matcher m = p.matcher(getStream().toString());

        if (!m.find()) {
            fail("msg shall be logged: " + getStream().toString());
        }

        String disk = readLog(true);
        Matcher d = p.matcher(disk);

        if (!d.find()) {
            fail("msg shall be logged to file: " + disk);
        }

    }
    
    public void testStartEnd() throws Exception {
        StartLog.logStart("run");
        StartLog.logEnd("run");

        {
            Pattern p = Pattern.compile("@[0-9]+.*run.*started");
            Matcher m = p.matcher(getStream().toString());

            if (!m.find()) {
                fail("msg shall be logged: " + getStream().toString());
            }

            String disk = readLog(true);
            Matcher d = p.matcher(disk);

            if (!d.find()) {
                fail("msg shall be logged to file: " + disk);
            }
        }
        
        {
            Pattern p = Pattern.compile("@[0-9]+.*run.*finished");
            Matcher m = p.matcher(getStream().toString());

            if (!m.find()) {
                fail("msg shall be logged: " + getStream().toString());
            }

            String disk = readLog(true);
            Matcher d = p.matcher(disk);

            if (!d.find()) {
                fail("msg shall be logged to file: " + disk);
            }
        }

    }
    
    public void testStartEndToLogger() throws Exception {
        Logger LOG = Logger.getLogger("org.netbeans.log.startup");
        LOG.log(Level.FINE, "start", "run");
        LOG.log(Level.FINE, "end", "run");

        {
            Pattern p = Pattern.compile("@[0-9]+.*run.*started");
            Matcher m = p.matcher(getStream().toString());

            if (!m.find()) {
                fail("msg shall be logged: " + getStream().toString());
            }

            String disk = readLog(true);
            Matcher d = p.matcher(disk);

            if (!d.find()) {
                fail("msg shall be logged to file: " + disk);
            }
        }
        
        {
            Pattern p = Pattern.compile("@[0-9]+.*run.*finished");
            Matcher m = p.matcher(getStream().toString());

            if (!m.find()) {
                fail("msg shall be logged: " + getStream().toString());
            }

            String disk = readLog(true);
            Matcher d = p.matcher(disk);

            if (!d.find()) {
                fail("msg shall be logged to file: " + disk);
            }
        }

    }

    private String readLog(boolean doFlush) throws IOException {
        if (doFlush) {
            TopLogging.flush(false);
        }

        File log = new File(new File(new File(getWorkDir(), "var"), "log"), "messages.log");
        assertTrue("Log file exists: " + log, log.canRead());

        FileInputStream is = new FileInputStream(log);

        byte[] arr = new byte[(int)log.length()];
        int r = is.read(arr);
        assertEquals("all read", arr.length, r);
        is.close();

        return new String(arr);
    }

}
