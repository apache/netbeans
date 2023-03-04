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

package org.netbeans;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.junit.*;
import org.openide.util.RequestProcessor;

import static org.netbeans.CLIHandlerTest.*;

/**
 * Test the command-line-interface handler ability to send zero output
 * back to client.
 * 
 * @author Jaroslav Tulach
 */
public class CLIHandlerGrepTest extends NbTestCase {

    static final ByteArrayInputStream nullInput = new ByteArrayInputStream(new byte[0]);
    static final ByteArrayOutputStream nullOutput = new ByteArrayOutputStream();
    
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
                        new InputStreamReader(args.getInputStream(), StandardCharsets.UTF_8)
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
            pending.offer(p.getBytes(StandardCharsets.UTF_8));
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
