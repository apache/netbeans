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

package org.netbeans.api.extexecution.base.input;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import org.netbeans.api.annotations.common.NonNull;

/**
 * Factory methods for {@link LineProcessor} classes.
 * <p>
 * Note that main difference between {@link InputProcessor} and
 * {@link LineProcessor} is that LineProcessor always process whole lines.
 *
 * @author Petr Hejl
 * @see InputProcessors#bridge(org.netbeans.api.extexecution.base.input.LineProcessor)
 */
public final class LineProcessors {

    private static final Logger LOGGER = Logger.getLogger(LineProcessors.class.getName());

    private LineProcessors() {
        super();
    }

    /**
     * Returns the processor acting as a proxy.
     * <p>
     * Any action taken on this processor is distributed to all processors
     * passed as arguments in the same order as they were passed to this method.
     * <p>
     * Returned processor is <i>not thread safe</i>.
     *
     * @param processors processor to which the actions will be distributed
     * @return the processor acting as a proxy
     */
    @NonNull
    public static LineProcessor proxy(@NonNull LineProcessor... processors) {
        return new ProxyLineProcessor(processors);
    }

    /**
     * Returns the processor printing all lines passed for processing to
     * the given output writer.
     * <p>
     * Processor closes the output writer on {@link InputProcessor#close()}.
     * <p>
     * Returned processor is <i>not thread safe</i>.
     *
     * @param out where to print received lines
     * @return the processor printing all lines passed for processing to
     *             the given output writer
     */
    @NonNull
    public static LineProcessor printing(@NonNull PrintWriter out) {
        return new PrintingLineProcessor(out);
    }

    /**
     * Returns the processor that will wait for the line matching the pattern,
     * decreasing the latch when such line appears for the first time.
     * <p>
     * Reset action on the returned processor is noop.
     * <p>
     * Returned processor is <i>thread safe</i>.
     *
     * @param pattern pattern that line must match in order decrease the latch
     * @param latch latch to decrease when the line matching the pattern appears
     *             for the first time
     * @return the processor that will wait for the line matching the pattern,
     *             decreasing the latch when such line appears for the first time
     */
    @NonNull
    public static LineProcessor patternWaiting(@NonNull Pattern pattern, @NonNull CountDownLatch latch) {
        return new WaitingLineProcessor(pattern, latch);
    }

    private static class ProxyLineProcessor implements LineProcessor {

        private final List<LineProcessor> processors = new ArrayList<LineProcessor>();

        private boolean closed;

        public ProxyLineProcessor(LineProcessor... processors) {
            for (LineProcessor processor : processors) {
                if (processor != null) {
                    this.processors.add(processor);
                }
            }
        }

        @Override
        public void processLine(String line) {
            if (closed) {
                throw new IllegalStateException("Already closed processor");
            }

            for (LineProcessor processor : processors) {
                processor.processLine(line);
            }
        }

        @Override
        public void reset() {
            if (closed) {
                throw new IllegalStateException("Already closed processor");
            }

            for (LineProcessor processor : processors) {
                processor.reset();
            }
        }

        @Override
        public void close() {
            closed = true;

            for (LineProcessor processor : processors) {
                processor.close();
            }
        }
    }

    private static class PrintingLineProcessor implements LineProcessor {

        private final PrintWriter out;

        private boolean closed;

        public PrintingLineProcessor(PrintWriter out) {
            assert out != null;

            this.out = out;
        }

        @Override
        public void processLine(String line) {
            assert line != null;

            if (closed) {
                throw new IllegalStateException("Already closed processor");
            }

            LOGGER.log(Level.FINEST, line);

            out.println(line);
            out.flush();
        }

        @Override
        public void reset() {
            if (closed) {
                throw new IllegalStateException("Already closed processor");
            }
        }

        @Override
        public void close() {
            closed = true;

            out.flush();
            out.close();
        }
    }

    private static class WaitingLineProcessor implements LineProcessor {

        private final Pattern pattern;

        private final CountDownLatch latch;

        /**<i>GuardedBy("this")</i>*/
        private boolean processed;

        /**<i>GuardedBy("this")</i>*/
        private boolean closed;

        public WaitingLineProcessor(Pattern pattern, CountDownLatch latch) {
            assert pattern != null;
            assert latch != null;

            this.pattern = pattern;
            this.latch = latch;
        }

        @Override
        public synchronized void processLine(String line) {
            assert line != null;

            if (closed) {
                throw new IllegalStateException("Already closed processor");
            }

            if (!processed && pattern.matcher(line).matches()) {
                latch.countDown();
                processed = true;
            }
        }

        @Override
        public synchronized void reset() {
            if (closed) {
                throw new IllegalStateException("Already closed processor");
            }
        }

        @Override
        public synchronized void close() {
            closed = true;
        }
    }
}
