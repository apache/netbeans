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

package org.netbeans.api.extexecution.input;

import org.netbeans.modules.extexecution.input.BaseInputProcessor;
import org.netbeans.modules.extexecution.input.DelegatingInputProcessor;
import java.io.IOException;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.openide.util.Cancellable;
import org.openide.util.Parameters;

/**
 * Task consuming data from the certain reader, processing them with the given
 * processor.
 * <p>
 * When exception occurs while running the task it is terminated.
 * Task is responsive to interruption. InputReader is closed on finish (includes
 * both cases throwing an exception and interruption).
 * <p>
 * The {@link #run()} method can be executed just once.
 * <p>
 * Task is <i>not finished</i> implicitly by reaching the end of the reader.
 * The caller has to finish it either by interruption or explicit cancellation.
 * Cancellation is preferred in situations where the interruption could make
 * cleanup operations on {@link InputProcessor} impossible to happen.
 *
 * <div class="nonnormative">
 * <p>
 * Sample usage - reading standard output of the process (throwing the data away):
 * <pre>
 *     java.lang.Process process = ...
 *     java.util.concurrent.ExecutorService executorService = ...
 *     Runnable runnable = InputReaderTask.newTask(
 *         InputReaders.forStream(process.getInputStream(), Charset.defaultCharset()));
 *     executorService.submit(runnable);
 *
 *     ...
 *
 *     executorService.shutdownNow();
 * </pre>
 * Sample usage - forwarding data to standard input of the process:
 * <pre>
 *     java.lang.Process process = ...
 *     java.util.concurrent.ExecutorService executorService = ...
 *     Runnable runnable = InputReaderTask.newTask(
 *         InputReaders.forStream(System.in, Charset.defaultCharset()),
 *         InputProcessors.copying(new OutputStreamWriter(process.getOutputStream())));
 *     executorService.submit(runnable);
 *
 *     ...
 *
 *     executorService.shutdownNow();
 * </pre>
 * </div>
 *
 * @author Petr Hejl
 * @deprecated use {@link org.netbeans.api.extexecution.base.input.InputReaderTask}
 */
@Deprecated
public final class InputReaderTask implements Runnable, Cancellable {

    private final org.netbeans.api.extexecution.base.input.InputReaderTask delegate;

    private InputReaderTask(org.netbeans.api.extexecution.base.input.InputReaderTask delegate) {
        this.delegate = delegate;
    }

    /**
     * Creates the new task. The task will read the data from reader processing
     * them through processor (if any) until interrupted or cancelled.
     * <p>
     * <i>{@link InputReader} must be non blocking.</i>
     *
     * @param reader data producer
     * @param processor processor consuming the data, may be <code>null</code>
     * @return task handling the read process
     */
    @NonNull
    public static InputReaderTask newTask(@NonNull InputReader reader, @NullAllowed InputProcessor processor) {
        Parameters.notNull("reader", reader);

        return new InputReaderTask(org.netbeans.api.extexecution.base.input.InputReaderTask.newTask(
                new BaseInputReader(reader), processor == null ? null : new BaseInputProcessor(processor)));
    }

    /**
     * Creates the new task. The task will read the data from reader processing
     * them through processor (if any). When interrupted or cancelled task will
     * try to read all the remaining <i>available</i> data before exiting.
     * <p>
     * <i>{@link InputReader} must be non blocking.</i>
     *
     * @param reader data producer
     * @param processor processor consuming the data, may be <code>null</code>
     * @return task handling the read process
     */
    @NonNull
    public static InputReaderTask newDrainingTask(@NonNull InputReader reader, @NullAllowed InputProcessor processor) {
        Parameters.notNull("reader", reader);

        return new InputReaderTask(org.netbeans.api.extexecution.base.input.InputReaderTask.newDrainingTask(
                new BaseInputReader(reader), processor == null ? null : new BaseInputProcessor(processor)));
    }

    /**
     * Task repeatedly reads the data from the InputReader, passing the content
     * to InputProcessor (if any).
     * <p>
     * It is not allowed to invoke run multiple times.
     */
    public void run() {
        delegate.run();
    }

    /**
     * Cancels the task. If the task is not running or task is already cancelled
     * this is noop.
     *
     * @return <code>true</code> if the task was successfully cancelled
     */
    public boolean cancel() {
        return delegate.cancel();
    }
    
    private static class BaseInputReader implements org.netbeans.api.extexecution.base.input.InputReader {
        
        private final InputReader delegate;

        public BaseInputReader(InputReader delegate) {
            this.delegate = delegate;
        }

        public int readInput(org.netbeans.api.extexecution.base.input.InputProcessor processor) throws IOException {
            InputProcessor p = null;
            if (processor != null) {
                if (processor instanceof BaseInputProcessor) {
                    p = ((BaseInputProcessor) processor).getDelegate();
                } else {
                    p = new DelegatingInputProcessor(processor);
                }
            }
            return delegate.readInput(p);
        }

        public void close() throws IOException {
            delegate.close();
        }
    }
}
