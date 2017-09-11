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

package org.netbeans;

import java.io.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.junit.*;
import static org.netbeans.CLIHandlerTest.*;
import org.openide.util.RequestProcessor;

/**
 * Test the command-line-interface handler ability to send zero output
 * back to client.
 * 
 * @author Jaroslav Tulach
 */
public class CLIHandlerGrepTest extends NbTestCase {

    final static ByteArrayInputStream nullInput = new ByteArrayInputStream(new byte[0]);
    final static ByteArrayOutputStream nullOutput = new ByteArrayOutputStream();
    
    static Logger LOG;

    public CLIHandlerGrepTest(String name) {
        super(name);
    }
    
    protected @Override void setUp() throws Exception {
        LOG = Logger.getLogger("TEST-" + getName());
        
        super.setUp();

        // all handlers shall be executed immediatelly
        CLIHandler.finishInitialization (false);
        
        // setups a temporary file
        String p = getWorkDirPath ();
        if (p == null) {
            p = System.getProperty("java.io.tmpdir");
        }
        String tmp = p;
        assertNotNull(tmp);
        System.getProperties().put("netbeans.user", tmp);
        
        File f = new File(tmp, "lock");
        if (f.exists()) {
            assertTrue("Clean up previous mess", f.delete());
            assertTrue(!f.exists());
        }
    }

    @Override
    protected void tearDown() throws Exception {
        CLIHandler.stopServer();
    }
    
    protected @Override Level logLevel() {
        return Level.FINEST;
    }

    protected @Override int timeOut() {
        return 15000;
    }

    @RandomlyFails // ergonomics-5386
    public void testReadingOfInputWorksInHandler() throws Exception {
        final byte[] template = { 1, 2, 3, 4 };
        
        class H extends CLIHandler {
            private byte[] arr;
            
            public H() {
                super(WHEN_INIT);
            }
            
            protected int cli(Args args) {
                if (args.getArguments().length == 0) {
                    return 0;
                }
                final String searchFor = args.getArguments()[0];
                try {
                    PrintStream ps = new PrintStream(args.getOutputStream());
                    BufferedReader r = new BufferedReader(
                        new InputStreamReader(args.getInputStream(), "UTF-8")
                    );
                    for (;;) {
                        String line = r.readLine();
                        if (line == null) {
                            ps.println("End of search");
                            break;
                        }
                        if (line.contains(searchFor)) {
                            ps.println("Found " + searchFor + ": " + line);
                        }
                    }
                } catch (IOException ex) {
                    fail("There is an exception: " + ex);
                }
                return 333;
            }
            
            protected void usage(PrintWriter w) {}
        }
        H h1 = new H();
        
        // start the server
        CLIHandler.Status res = cliInitialize(new String[0], new H[] { h1 }, 
            new ByteArrayInputStream(template), System.out, System.err
        );
        assertEquals("Code OK", 0, res.getExitCode());
        
        final IS linesToParse = new IS();
        final OS result = new OS();
        
        class Communication implements Runnable {
            @Override
            public void run() {
                linesToParse.pushMsg("My first line\n");
                linesToParse.pushMsg("My 2nd line\n");
                linesToParse.pushMsg("Hello 3rd line\n");
                linesToParse.pushMsg("4th line\n");
                linesToParse.pushMsg("");
                
                result.assertResult("Found Hello: Hello 3rd line");
                try {
                    linesToParse.close();
                } catch (IOException ex) {
                    throw new AssertionError(ex);
                }
            }
        }
        Communication communication = new Communication();
        RequestProcessor.getDefault().post(communication);
        
        res = cliInitialize(new String[] { "Hello" }, new H[] { h1 }, 
            linesToParse, result, System.err
        );

        result.assertResult("End of search");
        assertEquals("Processed without problems", 333, res.getExitCode());
    }
    
    static class IS extends InputStream {
        private byte[] current;
        private int currentPos;
        private final ArrayBlockingQueue<byte[]> pending = new ArrayBlockingQueue<byte[]>(64);

        @Override
        public int read() throws IOException {
            return read(null, 0, 1);
        }

        @Override
        public int read(byte[] arr, int offset, int len) throws IOException {
            if (current == null || current.length <= currentPos) {
                if (current != null && current.length == 0) {
                    return -1;
                }
                for (;;) {
                    try {
                        current = pending.poll(100, TimeUnit.MILLISECONDS);
                        if (current == null) {
                            return 0;
                        }
                        break;
                    } catch (InterruptedException ex) {
                        throw (InterruptedIOException) new InterruptedIOException().initCause(ex);
                    }
                }
                LOG.info("Will return: " + new String(current));
                currentPos = 0;
            }
            int cnt = 0;
            while (len-- > 0 && currentPos < current.length) {
                final byte nextByte = current[currentPos++];
                if (arr == null) {
                    return nextByte;
                }
                arr[offset + cnt++] = nextByte;
            }
            LOG.log(Level.INFO, "read returns: {0}", new String(arr, offset, cnt));
            return cnt;
        }

        private void pushMsg(String p) {
            try {
                pending.offer(p.getBytes("UTF-8"));
            } catch (UnsupportedEncodingException ex) {
                throw new IllegalStateException(ex);
            }
        }
    }

    static class OS extends OutputStream {
        private final StringBuffer sb = new StringBuffer();
        transient String out;

        @Override
        public void write(int b) throws IOException {
            if (b == '\n') {
                notifyResult(sb.toString());
                sb.setLength(0);
            } else {
                sb.append((char)b);
            }
        }

        private synchronized void notifyResult(String msg) {
            LOG.log(Level.INFO, "notifyResult: {0}", msg);
            while (out != null) {
                LOG.log(Level.INFO, "some previous result {0} exists, waiting", out);
                try {
                    wait();
                } catch (InterruptedException ex) {
                    LOG.log(Level.INFO, null, ex);
                }
            }
            assertNull("No previous result yet, new: " + msg + " old: " + out, out);
            out = msg;
            notifyAll();
        }

        private synchronized void assertResult(String msg) {
            while (out == null) {
                LOG.info("assertResult for " + msg + " but waiting");
                try {
                    wait();
                } catch (InterruptedException ex) {
                    LOG.log(Level.INFO, null, ex);
                }
            }
            LOG.info("assertResult for " + msg + " and " + out);
            assertEquals("Expecting result", msg.trim(), out.trim());
            out = null;
            notifyAll();
        }

        
    }
}
