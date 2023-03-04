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
import java.io.Writer;
import java.util.logging.Logger;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.extexecution.print.LineConvertor;
import org.openide.windows.OutputWriter;

/**
 * Factory methods for {@link InputProcessor} classes.
 *
 * @author Petr Hejl
 * @deprecated use {@link org.netbeans.api.extexecution.base.input.InputProcessors}
 *             and {@link org.netbeans.api.extexecution.print.InputProcessors}
 */
@Deprecated
public final class InputProcessors {

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
     * Returned processor is <i> not thread safe</i>.
     *
     * @param lineProcessor processor consuming parsed lines
     * @return the processor converting characters to the whole lines
     */
    @NonNull
    public static InputProcessor bridge(@NonNull LineProcessor lineProcessor) {
        return new DelegatingInputProcessor(org.netbeans.api.extexecution.base.input.InputProcessors.bridge(new LineProcessors.BaseLineProcessor(lineProcessor)));
    }

    /**
     * Returns the processor acting as a proxy.
     * <p>
     * Any action taken on this processor is distributed to all processors
     * passed as arguments in the same order as they were passed to this method.
     * <p>
     * Returned processor is <i> not thread safe</i>.
     *
     * @param processors processor to which the actions will be ditributed
     * @return the processor acting as a proxy
     */
    @NonNull
    public static InputProcessor proxy(@NonNull InputProcessor... processors) {
        org.netbeans.api.extexecution.base.input.InputProcessor[] wrapped = new org.netbeans.api.extexecution.base.input.InputProcessor[processors.length];
        for (int i = 0; i < processors.length; i++) {
            if (processors[i] != null) {
                wrapped[i] = new BaseInputProcessor(processors[i]);
            } else {
                wrapped[i] = null;
            }
        }
        return new DelegatingInputProcessor(org.netbeans.api.extexecution.base.input.InputProcessors.proxy(wrapped));
    }

    /**
     * Returns the processor that writes every character passed for processing
     * to the given writer.
     * <p>
     * Reset action on the returned processor is noop. Processor closes the
     * writer on {@link InputProcessor#close()}.
     * <p>
     * Returned processor is <i> not thread safe</i>.
     *
     * @param writer processed characters will be written to this writer
     * @return the processor that writes every character passed for processing
     *             to the given writer
     */
    @NonNull
    public static InputProcessor copying(@NonNull Writer writer) {
        return new DelegatingInputProcessor(org.netbeans.api.extexecution.base.input.InputProcessors.copying(writer));
    }

    /**
     * Returns the processor printing all characters passed for processing to
     * the given output writer.
     * <p>
     * Reset action on the returned processor resets the writer if it is enabled
     * by passing <code>true</code> as <code>resetEnabled</code>. Processor
     * closes the output writer on {@link InputProcessor#close()}.
     * <p>
     * Returned processor is <i> not thread safe</i>.
     *
     * @param out where to print received characters
     * @param resetEnabled determines whether the reset operation will work
     *             (will reset the writer if so)
     * @return the processor printing all characters passed for processing to
     *             the given output writer
     */
    @NonNull
    public static InputProcessor printing(@NonNull OutputWriter out, boolean resetEnabled) {
        return new DelegatingInputProcessor(org.netbeans.api.extexecution.print.InputProcessors.printing(out, resetEnabled));
    }

    /**
     * Returns the processor converting <i>whole</i> lines with convertor and
     * printing the result including unterminated tail (if present) to the
     * given output writer. If the covertor does not handle line passed to it
     * (returning <code>null</code>) raw lines are printed.
     * <p>
     * Reset action on the returned processor resets the writer if it is enabled
     * by passing <code>true</code> as <code>resetEnabled</code>. Processor
     * closes the output writer on {@link InputProcessor#close()}.
     * <p>
     * Returned processor is <i> not thread safe</i>.
     *
     * @param out where to print converted lines and characters
     * @param convertor convertor converting the <i>whole</i> lines
     *             before printing, may be <code>null</code>
     * @param resetEnabled determines whether the reset operation will work
     *             (will reset the writer if so)
     * @return the processor converting the <i>whole</i> lines with convertor and
     *             printing the result including unterminated tail (if present)
     *             to the given output writer
     * @see LineConvertor
     */
    @NonNull
    public static InputProcessor printing(@NonNull OutputWriter out, @NullAllowed LineConvertor convertor, boolean resetEnabled) {
        return new DelegatingInputProcessor(org.netbeans.api.extexecution.print.InputProcessors.printing(out, convertor, resetEnabled));
    }

    /**
     * Returns the processor that strips any
     * <a href="http://en.wikipedia.org/wiki/ANSI_escape_code">ANSI escape sequences</a>
     * and passes the result to the delegate.
     * <p>
     * Reset and close methods on the returned processor invokes
     * the corresponding actions on delegate.
     * <p>
     * Returned processor is <i> not thread safe</i>.
     *
     * @param delegate processor that will receive characters without control
     *             sequences
     * @return the processor that strips any ansi escape sequences and passes
     *             the result to the delegate
     */
    @NonNull
    public static InputProcessor ansiStripping(@NonNull InputProcessor delegate) {
        return new DelegatingInputProcessor(org.netbeans.api.extexecution.base.input.InputProcessors.ansiStripping(new BaseInputProcessor(delegate)));
    }
    
}
