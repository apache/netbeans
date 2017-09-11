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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
