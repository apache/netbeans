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

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.modules.extexecution.base.input.LineParsingHelper;
import org.openide.util.Parameters;

/**
 * Factory methods for {@link InputProcessor} classes.
 *
 * @author Petr Hejl
 */
public final class InputProcessors {

    private static final Logger LOGGER = Logger.getLogger(InputProcessors.class.getName());

    private InputProcessors() {
        super();
    }

    /**
     * Returns the processor converting characters to the whole lines passing
     * them to the given line processor.
     * <p>
     * Any reset or close is delegated to the corresponding method
     * of line processor.
     * <p>
     * Returned processor is <i>not thread safe</i>.
     *
     * @param lineProcessor processor consuming parsed lines
     * @return the processor converting characters to the whole lines
     */
    @NonNull
    public static InputProcessor bridge(@NonNull LineProcessor lineProcessor) {
        return new Bridge(lineProcessor);
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
    public static InputProcessor proxy(@NonNull InputProcessor... processors) {
        return new ProxyInputProcessor(processors);
    }

    /**
     * Returns the processor that writes every character passed for processing
     * to the given writer.
     * <p>
     * Reset action on the returned processor is noop. Processor closes the
     * writer on {@link InputProcessor#close()}.
     * <p>
     * Returned processor is <i>not thread safe</i>.
     *
     * @param writer processed characters will be written to this writer
     * @return the processor that writes every character passed for processing
     *             to the given writer
     */
    @NonNull
    public static InputProcessor copying(@NonNull Writer writer) {
        return new CopyingInputProcessor(writer);
    }

    /**
     * Returns the processor printing all characters passed for processing to
     * the given writer.
     * <p>
     * Reset action on the returned processor is noop. Processor closes the
     * writer on {@link InputProcessor#close()}.
     * <p>
     * Returned processor is <i>not thread safe</i>.
     *
     * @param out where to print received characters
     * @return the processor printing all characters passed for processing to
     *             the given writer
     */
    @NonNull
    public static InputProcessor printing(@NonNull PrintWriter out) {
        return new PrintingInputProcessor(out);
    }

    /**
     * Returns the processor that strips any
     * <a href="https://en.wikipedia.org/wiki/ANSI_escape_code">ANSI escape sequences</a>
     * and passes the result to the delegate.
     * <p>
     * Reset and close methods on the returned processor invokes
     * the corresponding actions on delegate.
     * <p>
     * Returned processor is <i>not thread safe</i>.
     *
     * @param delegate processor that will receive characters without control
     *             sequences
     * @return the processor that strips any ANSI escape sequences and passes
     *             the result to the delegate
     */
    @NonNull
    public static InputProcessor ansiStripping(@NonNull InputProcessor delegate) {
        return new AnsiStrippingInputProcessor(delegate);
    }

    private static class Bridge implements InputProcessor {

        private final LineProcessor lineProcessor;

        private final LineParsingHelper helper = new LineParsingHelper();

        private boolean closed;

        public Bridge(LineProcessor lineProcessor) {
            Parameters.notNull("lineProcessor", lineProcessor);

            this.lineProcessor = lineProcessor;
        }

        @Override
        public final void processInput(char[] chars) {
            if (closed) {
                throw new IllegalStateException("Already closed processor");
            }

            String[] lines = helper.parse(chars);
            for (String line : lines) {
                lineProcessor.processLine(line);
            }
        }

        @Override
        public final void reset() {
            if (closed) {
                throw new IllegalStateException("Already closed processor");
            }

            flush();
            lineProcessor.reset();
        }

        @Override
        public final void close() {
            closed = true;

            flush();
            lineProcessor.close();
        }

        private void flush() {
            String line = helper.getTrailingLine(true);
            if (line != null) {
                lineProcessor.processLine(line);
            }
        }
    }

    private static class ProxyInputProcessor implements InputProcessor {

        private final List<InputProcessor> processors = new ArrayList<InputProcessor>();

        private boolean closed;

        public ProxyInputProcessor(InputProcessor... processors) {
            for (InputProcessor processor : processors) {
                if (processor != null) {
                    this.processors.add(processor);
                }
            }
        }

        @Override
        public void processInput(char[] chars) throws IOException {
            if (closed) {
                throw new IllegalStateException("Already closed processor");
            }

            for (InputProcessor processor : processors) {
                processor.processInput(chars);
            }
        }

        @Override
        public void reset() throws IOException {
            if (closed) {
                throw new IllegalStateException("Already closed processor");
            }

            for (InputProcessor processor : processors) {
                processor.reset();
            }
        }

        @Override
        public void close() throws IOException {
            closed = true;

            for (InputProcessor processor : processors) {
                processor.close();
            }
        }
    }

    private static class PrintingInputProcessor implements InputProcessor {

        private final PrintWriter out;

        private final LineParsingHelper helper = new LineParsingHelper();

        private boolean closed;

        public PrintingInputProcessor(PrintWriter out) {
            assert out != null;

            this.out = out;
        }

        @Override
        public void processInput(char[] chars) {
            assert chars != null;

            if (closed) {
                throw new IllegalStateException("Already closed processor");
            }

            String[] lines = helper.parse(chars);
            for (String line : lines) {
                LOGGER.log(Level.FINEST, "{0}\\n", line);

                out.println(line);
                out.flush();
            }

            String line = helper.getTrailingLine(true);
            if (line != null) {
                LOGGER.log(Level.FINEST, line);

                out.print(line);
                out.flush();
            }
        }

        @Override
        public void reset() throws IOException {
            // noop
        }

        @Override
        public void close() throws IOException {
            closed = true;

            out.close();
        }
    }

    private static class CopyingInputProcessor implements InputProcessor {

        private final Writer writer;

        private boolean closed;

        public CopyingInputProcessor(Writer writer) {
            this.writer = writer;
        }

        @Override
        public void processInput(char[] chars) throws IOException {
            if (closed) {
                throw new IllegalStateException("Already closed processor");
            }

            LOGGER.log(Level.FINEST, Arrays.toString(chars));
            writer.write(chars);
            writer.flush();
        }

        @Override
        public void reset() {
            // noop
        }

        @Override
        public void close() throws IOException {
            closed = true;

            writer.close();
        }
    }

    private static class AnsiStrippingInputProcessor implements InputProcessor {

        private final InputProcessor delegate;

        private boolean closed;

        public AnsiStrippingInputProcessor(InputProcessor delegate) {
            this.delegate = delegate;
        }

        @Override
        public void processInput(char[] chars) throws IOException {
            if (closed) {
                throw new IllegalStateException("Already closed processor");
            }

            // FIXME optimize me
            String sequence = new String(chars);
            if (containsAnsiColors(sequence)) {
                sequence = stripAnsiColors(sequence);
            }
            delegate.processInput(sequence.toCharArray());
        }

        @Override
        public void reset() throws IOException {
            if (closed) {
                throw new IllegalStateException("Already closed processor");
            }

            delegate.reset();
        }

        @Override
        public void close() throws IOException {
            closed = true;

            delegate.close();
        }

        private static boolean containsAnsiColors(String sequence) {
            // RSpec will color output with ANSI color sequence terminal escapes
            return sequence.indexOf("\033[") != -1; // NOI18N
        }

        private static String stripAnsiColors(String sequence) {
            StringBuilder sb = new StringBuilder(sequence.length());
            int index = 0;
            int max = sequence.length();
            while (index < max) {
                int nextEscape = sequence.indexOf("\033[", index); // NOI18N
                if (nextEscape == -1) {
                    nextEscape = sequence.length();
                }

                for (int n = (nextEscape == -1) ? max : nextEscape; index < n; index++) {
                    sb.append(sequence.charAt(index));
                }

                if (nextEscape != -1) {
                    for (; index < max; index++) {
                        char c = sequence.charAt(index);
                        if (c == 'm') {
                            index++;
                            break;
                        }
                    }
                }
            }

            return sb.toString();
        }
    }
}
