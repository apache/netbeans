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
