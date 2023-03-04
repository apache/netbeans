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
package org.netbeans.core.startup.logging;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.util.RequestProcessor;

/** a stream to delegate to logging.
 */
public final class PrintStreamLogger extends PrintStream implements Runnable {
    /**
     * Threshold of buffered data before the background thread starts to flush it to the logger.
     */
    // not private bcs of test
    static final int BUFFER_THRESHOLD = 2 * 1024 * 1024;
    
    /**
     * Minimum capacity, does not shring below this size.
     */
    private static final int BUFFER_MIN_CAPACITY = 1024 * 1024;
    
    /**
     * Time after which the buffer shrinks/is recycled, if not adequately used.
     */
    // not private bcs of test
    static final int BUFFER_SHRINK_TIME = 10 * 1000;
    
    /**
     * Ratio of required buffer usage. If the buffer is used up to less than
     * 1/n, the builder will be recycled on the next flush.
     */
    private static final int BUFFER_CAPACITY_DIV = 5;
    
    private Logger log;
    
    @SuppressWarnings("RedundantStringConstructorCall") // prevent interning
    private final Object lock = new String("lock"); // NOI18N
    
    // @GuardedBy(lock)
    private StringBuilder sb = new StringBuilder();

    // @GuardedBy(lock)
    private int bufferTop = 0;
    
    /**
     * Maximum useful buffer size at the time of flush
     * @GuardedBy(lock)
     */
    private int maxBufferSize;
    
    /**
     * Last time the buffer was checked/shrinked
     * @GuardedBy(lock)
     */
    private long lastCleanupTime;
    
    private static RequestProcessor RP = new RequestProcessor("StdErr Flush");
    private RequestProcessor.Task flush = RP.create(this, true);

    private PrintStreamLogger(Logger log) {
        super(new ByteArrayOutputStream());
        this.log = log;
    }
    
    public static boolean isLogger(PrintStream ps) {
        // should work across different classloaders
        return ps.getClass().getName().equals(PrintStreamLogger.class.getName());
    }
    
    public static PrintStream create(String loggerName) {
        return new PrintStreamLogger(Logger.getLogger(loggerName)); 
    }
    
    //
    // Impl
    //

    @Override
    public void write(byte[] buf, int off, int len) {
        if (RP.isRequestProcessorThread()) {
            return;
        }
        String s = new String(buf, off, len);
        print(s);
    }

    @Override
    public void write(byte[] b) throws IOException {
        write(b, 0, b.length);
    }

    @Override
    public void write(int b) {
        if (RP.isRequestProcessorThread()) {
            return;
        }
        synchronized (lock) {
            sb.append((char) b);
            checkFlush();
        }
    }

    @Override
    public void print(String s) {
        if (NbLogging.DEBUG != null && !NbLogging.wantsMessage(s)) {
            new Exception().printStackTrace(NbLogging.DEBUG);
        }
        synchronized (lock) {
            sb.append(s);
            checkFlush();
        }
    }

    @Override
    public void println(String x) {
        print(x);
        print(System.getProperty("line.separator"));
    }

    @Override
    public void println(Object x) {
        String s = String.valueOf(x);
        println(s);
    }

    @Override
    public void flush() {
        boolean empty;
        synchronized (lock) {
            empty = sb.length() == 0;
        }
        if (!empty) {
            try {
                flush.schedule(0);
                flush.waitFinished(500);
            } catch (InterruptedException ex) {
                // ok, flush failed, do not even print
                // as we are inside the System.err code
            }
        }
        super.flush();
    }

    private void checkFlush() {
        //if (DEBUG != null) DEBUG.println("checking flush; buffer: " + sb); // NOI18N
        boolean immediate = sb.length() - bufferTop > BUFFER_THRESHOLD;
        try {
            flush.schedule(immediate ? 0 : 100);
        } catch (IllegalStateException ex) {
            /* can happen during shutdown:
            Nested Exception is:
            java.lang.IllegalStateException: Timer already cancelled.
            at java.util.Timer.sched(Timer.java:354)
            at java.util.Timer.schedule(Timer.java:170)
            at org.openide.util.RequestProcessor$Task.schedule(RequestProcessor.java:621)
            at org.netbeans.core.startup.TopLogging$LgStream.checkFlush(TopLogging.java:679)
            at org.netbeans.core.startup.TopLogging$LgStream.write(TopLogging.java:650)
            at sun.nio.cs.StreamEncoder.writeBytes(StreamEncoder.java:202)
            at sun.nio.cs.StreamEncoder.implWrite(StreamEncoder.java:263)
            at sun.nio.cs.StreamEncoder.write(StreamEncoder.java:106)
            at java.io.OutputStreamWriter.write(OutputStreamWriter.java:190)
            at java.io.BufferedWriter.flushBuffer(BufferedWriter.java:111)
            at java.io.PrintStream.write(PrintStream.java:476)
            at java.io.PrintStream.print(PrintStream.java:619)
            at java.io.PrintStream.println(PrintStream.java:773)
            at java.lang.Throwable.printStackTrace(Throwable.java:461)
            at java.lang.Throwable.printStackTrace(Throwable.java:451)
            at org.netbeans.insane.impl.LiveEngine.trace(LiveEngine.java:180)
            at org.netbeans.insane.live.LiveReferences.fromRoots(LiveReferences.java:110)
             * just ignore it, we cannot print it at this situation anyway...
             */
        }
    }
    
    private final RequestProcessor.Task cleanupTask = RP.create(new Runnable() {
        public void run() {
            synchronized (lock) {
                maybeFreeBuffer();
            }
        }
    });
    
    /**
     * Conditionally shrinks the buffer. Called synchronously from flusher, and 
     * scheduled (by the flusher) to cover the case logging ends within BUFFER_SHRINK_TIME
     * and no more data comes.
     * <p/>
     * The intention is to recycle the buffer, if it was not filled at least to 1/BUFFER_CAPACITY_DIV
     * of its capacity during the last BUFFER_SHRINK_TIME millis.
     */
    private void maybeFreeBuffer() {
        int c = sb.capacity();
        long t = System.currentTimeMillis();
        if (t - lastCleanupTime > BUFFER_SHRINK_TIME) {
            if (c > BUFFER_MIN_CAPACITY &&
                c / BUFFER_CAPACITY_DIV > maxBufferSize) {
                // recycle the buffer, reclaim potential memory
                sb = new StringBuilder(sb);
            }
            maxBufferSize = sb.length();
            lastCleanupTime = t;
        }
    }

    public void run() {
        for (;;) {
            String toLog;
            synchronized (lock) {
                if (sb.length() == 0) {
                    break;
                }
                maxBufferSize = Math.max(maxBufferSize, sb.length());
                int last = -1;
                for (int i = sb.length() - 1; i >= bufferTop; i--) {
                    if (sb.charAt(i) == '\n') {
                        last = i;
                        break;
                    }
                }
                if (last == -1) {
                    // no \n in the buffer
                    bufferTop = sb.length();
                    break;
                }
                toLog = sb.substring(0, last + 1);
                // the remained does not contain a newline
                // PEDNING: perf: circular buffer should be used here. 
                sb.delete(0, last + 1);
                bufferTop = sb.length();
                // attempt to free the buffer
                maybeFreeBuffer();
                // schedule for the case that data stops coming in
                cleanupTask.schedule(BUFFER_SHRINK_TIME);
                lock.notifyAll();
            }
            int begLine = 0;
            while (begLine < toLog.length()) {
                int endLine = toLog.indexOf('\n', begLine);
                log.log(Level.INFO, toLog.substring(begLine, endLine + 1));
                begLine = endLine + 1;
            }
        }
    }
    
    // Testing only
    int[] bufferSizes() {
        synchronized (lock) {
            return new int[] { sb.length(), sb.capacity() };
        }
    }
} // end of LgStream
