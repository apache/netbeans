/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
 */

package org.netbeans.api.extexecution.base;

import java.io.Reader;
import java.nio.charset.Charset;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.CheckReturnValue;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.extexecution.base.input.InputProcessor;

/**
 * Descriptor for the execution service. Describes the runtime attributes
 * of the {@link BaseExecutionService}.
 * <p>
 * <i>Thread safety</i> of this class depends on type of objects passed to its
 * configuration methods. If these objects are immutable, resulting descriptor
 * is immutable as well. It these objects are thread safe, resulting descriptor
 * is thread safe as well.
 *
 * @author Petr Hejl
 * @see BaseExecutionService
 */
public final class BaseExecutionDescriptor {

    private final Charset charset;

    private final Runnable preExecution;

    private final ParametrizedRunnable<Integer> postExecution;

    private final InputProcessorFactory outProcessorFactory;

    private final InputProcessorFactory errProcessorFactory;

    private final ReaderFactory inReaderFactory;

    /**
     * Creates the new descriptor. All properties are initialized to
     * <code>null</code>.
     */
    public BaseExecutionDescriptor() {
        this(null, null, null, null, null, null);
    }

    private BaseExecutionDescriptor(Charset charset, Runnable preExecution,
            ParametrizedRunnable<Integer> postExecution,
            InputProcessorFactory outProcessorFactory,
            InputProcessorFactory errProcessorFactory,
            ReaderFactory inReaderFactory) {

        this.charset = charset;
        this.preExecution = preExecution;
        this.postExecution = postExecution;
        this.outProcessorFactory = outProcessorFactory;
        this.errProcessorFactory = errProcessorFactory;
        this.inReaderFactory = inReaderFactory;
    }

    /**
     * Returns a descriptor with configured charset. If configured
     * value is not <code>null</code> the {@link BaseExecutionService} will
     * use the given charset to decode the process streams. When
     * <code>null</code> the platform default will be used.
     * <p>
     * Note that in the most common scenario of execution of OS native
     * process you shouldn't need to set the charset. The platform default
     * (which is the default used) is just the right choice.
     * <p>
     * The default (not configured) value is <code>null</code>.
     * <p>
     * All other properties of the returned descriptor are inherited from
     * <code>this</code>.
     *
     * @param charset charset, <code>null</code> allowed
     * @return this descriptor with configured charset
     */
    @NonNull
    @CheckReturnValue
    public BaseExecutionDescriptor charset(@NullAllowed Charset charset) {
        return new BaseExecutionDescriptor(charset, preExecution, postExecution,
                outProcessorFactory, errProcessorFactory, inReaderFactory);
    }

    Charset getCharset() {
        return charset;
    }

    /**
     * Returns a descriptor with configured pre execution runnable. This
     * runnable is executed <i>before</i> the external execution itself
     * (when invoked by {@link BaseExecutionService#run()}).
     * <p>
     * The default (not configured) value is <code>null</code>.
     * <p>
     * All other properties of the returned descriptor are inherited from
     * <code>this</code>.
     *
     * @param preExecution pre execution runnable, <code>null</code> allowed
     * @return new descriptor with configured pre execution runnable
     */
    @NonNull
    @CheckReturnValue
    public BaseExecutionDescriptor preExecution(@NullAllowed Runnable preExecution) {
        return new BaseExecutionDescriptor(charset, preExecution, postExecution,
                outProcessorFactory, errProcessorFactory, inReaderFactory);
    }

    Runnable getPreExecution() {
        return preExecution;
    }

    /**
     * Returns a descriptor with configured post execution runnable. This
     * runnable is executed <i>after</i> the external execution itself
     * (when invoked by {@link BaseExecutionService#run()}).
     * <p>
     * The default (not configured) value is <code>null</code>.
     * <p>
     * All other properties of the returned descriptor are inherited from
     * <code>this</code>.
     *
     * @param postExecution post execution runnable, <code>null</code> allowed
     * @return new descriptor with configured post execution runnable
     */
    @NonNull
    @CheckReturnValue
    public BaseExecutionDescriptor postExecution(@NullAllowed ParametrizedRunnable<Integer> postExecution) {
        return new BaseExecutionDescriptor(charset, preExecution, postExecution,
                outProcessorFactory, errProcessorFactory, inReaderFactory);
    }

    ParametrizedRunnable<Integer> getPostExecution() {
        return postExecution;
    }

    /**
     * Returns a descriptor with configured factory for standard output
     * processor. The factory is used by {@link BaseExecutionService} to create
     * processor for standard output.
     * <p>
     * The default (not configured) value is <code>null</code>.
     * <p>
     * All other properties of the returned descriptor are inherited from
     * <code>this</code>.
     *
     * @param outProcessorFactory factory for standard output processor,
     *             <code>null</code> allowed
     * @return new descriptor with configured factory for additional
     *             processor to use for standard output
     */
    @NonNull
    @CheckReturnValue
    public BaseExecutionDescriptor outProcessorFactory(@NullAllowed InputProcessorFactory outProcessorFactory) {
        return new BaseExecutionDescriptor(charset, preExecution, postExecution,
                outProcessorFactory, errProcessorFactory, inReaderFactory);
    }

    InputProcessorFactory getOutProcessorFactory() {
        return outProcessorFactory;
    }

    /**
     * Returns a descriptor with configured factory for standard error output
     * processor. The factory is used by {@link BaseExecutionService} to create
     * processor for standard error output.
     * <p>
     * The default (not configured) value is <code>null</code>.
     * <p>
     * All other properties of the returned descriptor are inherited from
     * <code>this</code>.
     *
     * @param errProcessorFactory factory for standard error output processor,
     *             <code>null</code> allowed
     * @return new descriptor with configured factory for additional
     *             processor to use for standard error output
     */
    @NonNull
    @CheckReturnValue
    public BaseExecutionDescriptor errProcessorFactory(@NullAllowed InputProcessorFactory errProcessorFactory) {
        return new BaseExecutionDescriptor(charset, preExecution, postExecution,
                outProcessorFactory, errProcessorFactory, inReaderFactory);
    }

    InputProcessorFactory getErrProcessorFactory() {
        return errProcessorFactory;
    }

    /**
     * Returns a descriptor with configured factory for standard input reader.
     * The factory is used by {@link BaseExecutionService} to create
     * a reader providing input to the process.
     * <p>
     * The default (not configured) value is <code>null</code>.
     * <p>
     * All other properties of the returned descriptor are inherited from
     * <code>this</code>.
     *
     * @param inReaderFactory  factory for standard input reader,
     *             <code>null</code> allowed
     * @return new descriptor with configured factory for reader to use
     *             for standard input
     */
    @NonNull
    @CheckReturnValue
    public BaseExecutionDescriptor inReaderFactory(@NullAllowed ReaderFactory inReaderFactory) {
        return new BaseExecutionDescriptor(charset, preExecution, postExecution,
                outProcessorFactory, errProcessorFactory, inReaderFactory);
    }

    ReaderFactory getInReaderFactory() {
        return inReaderFactory;
    }

    /**
     * Factory creating the input processor.
     */
    public interface InputProcessorFactory {

        /**
         * Creates and returns new input processor.
         *
         * @return new input processor
         */
        @CheckForNull
        InputProcessor newInputProcessor();

    }

    /**
     * Factory creating the reader.
     */
    public interface ReaderFactory {

        /**
         * Creates and returns new reader.
         *
         * @return new reader
         */
        @CheckForNull
        Reader newReader();
    }

}
