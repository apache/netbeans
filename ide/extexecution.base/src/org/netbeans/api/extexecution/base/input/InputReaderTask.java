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
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.openide.util.Cancellable;
import org.openide.util.Parameters;

/**
 * Task consuming data from the certain reader, processing them with the given
 * processor.
 * <p>
 * When exception occurs while the task is running the task is terminated.
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
 */
public final class InputReaderTask implements Runnable, Cancellable {

    private static final Logger LOGGER = Logger.getLogger(InputReaderTask.class.getName());

    private static final int MIN_DELAY = 50;

    private static final int MAX_DELAY = 300;

    private static final int DELAY_INCREMENT = 50;

    private final InputReader inputReader;

    private final InputProcessor inputProcessor;

    private final boolean draining;

    private boolean cancelled;

    private boolean running;

    private InputReaderTask(InputReader inputReader, InputProcessor inputProcessor, boolean draining) {
        this.inputReader = inputReader;
        this.inputProcessor = inputProcessor;
        this.draining = draining;
    }

    /**
     * Creates a new task. The task will read the data from reader processing
     * them through processor (if any) until interrupted or canceled.
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

        return new InputReaderTask(reader, processor, false);
    }

    /**
     * Creates the new task. The task will read the data from reader processing
     * them through processor (if any). When interrupted or canceled task will
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

        return new InputReaderTask(reader, processor, true);
    }

    /**
     * Task repeatedly reads the data from the InputReader, passing the content
     * to InputProcessor (if any).
     * <p>
     * It is not allowed to invoke run multiple times.
     */
    @Override
    public void run() {
        synchronized (this) {
            if (running) {
                throw new IllegalStateException("Already running task");
            }
            running = true;
        }

        boolean interrupted = false;
        try {
            long delay = MIN_DELAY;
            int emptyReads = 0;

            while (true) {
                synchronized (this) {
                    if (Thread.currentThread().isInterrupted() || cancelled) {
                        interrupted = Thread.interrupted();
                        break;
                    }
                }

                int count = inputReader.readInput(inputProcessor);

                // compute the delay based on how often we really get the data
                if (count > 0) {
                    delay = MIN_DELAY;
                    emptyReads = 0;
                } else {
                    // increase the delay only slowly - once for
                    // MAX_DELAY / DELAY_INCREMENT unsuccesfull read attempts
                    if (emptyReads > (MAX_DELAY / DELAY_INCREMENT)) {
                        emptyReads = 0;
                        delay = Math.min(delay + DELAY_INCREMENT, MAX_DELAY);
                    } else {
                        emptyReads++;
                    }
                }

                if (LOGGER.isLoggable(Level.FINEST)) {
                    LOGGER.log(Level.FINEST, "Task {0} sleeping for {1} ms",
                            new Object[] {Thread.currentThread().getName(), delay});
                }
                try {
                    // give the producer some time to write the output
                    Thread.sleep(delay);
                } catch (InterruptedException e) {
                    interrupted = true;
                    break;
                }
            }

            synchronized (this) {
                if (Thread.currentThread().isInterrupted() || cancelled) {
                    interrupted = Thread.interrupted();
                }
            }
        } catch (Exception ex) {
            LOGGER.log(Level.FINE, null, ex);
        } catch (AssertionError err) {
            LOGGER.log(Level.WARNING, null, err);
            throw err;
        } finally {
            // drain the rest
            if (draining) {
                try {
                    while (inputReader.readInput(inputProcessor) > 0) {
                        LOGGER.log(Level.FINE, "Draining the rest of the reader");
                    }
                } catch (IOException ex) {
                    LOGGER.log(Level.FINE, null, ex);
                }
            }

            // perform cleanup
            try {
                if (inputProcessor != null) {
                    inputProcessor.close();
                }
                inputReader.close();
            } catch (IOException ex) {
                LOGGER.log(Level.INFO, null, ex);
            } finally {
                if (interrupted) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

    /**
     * Cancels the task. If the task is not running or task is already canceled
     * this is noop.
     *
     * @return <code>true</code> if the task was successfully canceled
     */
    @Override
    public boolean cancel() {
        synchronized (this) {
            if (cancelled) {
                return false;
            }
            cancelled = true;
            return true;
        }
    }
}
