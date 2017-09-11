/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.api.extexecution.input;

import java.util.concurrent.CountDownLatch;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.extexecution.print.LineConvertor;
import org.openide.windows.OutputWriter;

/**
 * Factory methods for {@link LineProcessor} classes.
 * <p>
 * Note that main difference between {@link InputProcessor} and
 * {@link LineProcessor} is that LineProcessor always process whole lines.
 *
 * @author Petr Hejl
 * @see InputProcessors#bridge(org.netbeans.api.extexecution.input.LineProcessor)
 * @deprecated use {@link org.netbeans.api.extexecution.base.input.LineProcessors}
 *             and {@link org.netbeans.api.extexecution.print.LineProcessors}
 */
public final class LineProcessors {

    private LineProcessors() {
        super();
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
    public static LineProcessor proxy(@NonNull LineProcessor... processors) {
        org.netbeans.api.extexecution.base.input.LineProcessor[] wrapped = new org.netbeans.api.extexecution.base.input.LineProcessor[processors.length];
        for (int i = 0; i < processors.length; i++) {
            if (processors[i] != null) {
                wrapped[i] = new BaseLineProcessor(processors[i]);
            } else {
                wrapped[i] = null;
            }
        }
        return new DelegatingLineProcessor(org.netbeans.api.extexecution.base.input.LineProcessors.proxy(wrapped));
    }

    /**
     * Returns the processor printing all lines passed for processing to
     * the given output writer.
     * <p>
     * Reset action on the returned processor resets the writer if it is enabled
     * by passing <code>true</code> as <code>resetEnabled</code>. Processor
     * closes the output writer on {@link InputProcessor#close()}.
     * <p>
     * Returned processor is <i> not thread safe</i>.
     *
     * @param out where to print received lines
     * @param resetEnabled determines whether the reset operation will work
     *             (will reset the writer if so)
     * @return the processor printing all lines passed for processing to
     *             the given output writer
     */
    @NonNull
    public static LineProcessor printing(@NonNull OutputWriter out, boolean resetEnabled) {
        return new DelegatingLineProcessor(org.netbeans.api.extexecution.print.LineProcessors.printing(out, resetEnabled));
    }

    /**
     * Returns the processor converting lines with convertor and
     * printing the result to the given output writer. If the covertor does
     * not handle line passed to it (returning <code>null</code>) raw
     * lines are printed.
     * <p>
     * Reset action on the returned processor resets the writer if it is enabled
     * by passing <code>true</code> as <code>resetEnabled</code>. Processor
     * closes the output writer on {@link InputProcessor#close()}.
     * <p>
     * Returned processor is <i> not thread safe</i>.
     *
     * @param out where to print converted lines and characters
     * @param convertor convertor converting the lines before printing,
     *             may be <code>null</code>
     * @param resetEnabled determines whether the reset operation will work
     *             (will reset the writer if so)
     * @return the processor converting the lines with convertor and
     *             printing the result to the given output writer
     * @see LineConvertor
     */
    @NonNull
    public static LineProcessor printing(@NonNull OutputWriter out, @NullAllowed LineConvertor convertor, boolean resetEnabled) {
        return new DelegatingLineProcessor(org.netbeans.api.extexecution.print.LineProcessors.printing(out, convertor, resetEnabled));
    }

    /**
     * Returns the processor that will wait for the line matching the pattern,
     * decreasing the latch when such line appears for the first time.
     * <p>
     * Reset action on the returned processor is noop.
     * <p>
     * Returned processor is <i> thread safe</i>.
     *
     * @param pattern pattern that line must match in order decrease the latch
     * @param latch latch to decrease when the line matching the pattern appears
     *             for the first time
     * @return the processor that will wait for the line matching the pattern,
     *             decreasing the latch when such line appears for the first time
     */
    @NonNull
    public static LineProcessor patternWaiting(@NonNull Pattern pattern, @NonNull CountDownLatch latch) {
        return new DelegatingLineProcessor(org.netbeans.api.extexecution.base.input.LineProcessors.patternWaiting(pattern, latch));
    }
    
    static class DelegatingLineProcessor implements LineProcessor {
        
        private final org.netbeans.api.extexecution.base.input.LineProcessor delegate;

        public DelegatingLineProcessor(org.netbeans.api.extexecution.base.input.LineProcessor delegate) {
            this.delegate = delegate;
        }

        @Override
        public void processLine(String line) {
            delegate.processLine(line);
        }

        @Override
        public void reset() {
            delegate.reset();
        }

        @Override
        public void close() {
            delegate.close();
        }
    }
    
    static class BaseLineProcessor implements org.netbeans.api.extexecution.base.input.LineProcessor {
        
        private final LineProcessor delegate;

        public BaseLineProcessor(LineProcessor delegate) {
            this.delegate = delegate;
        }

        @Override
        public void processLine(String line) {
            delegate.processLine(line);
        }

        @Override
        public void reset() {
            delegate.reset();
        }

        @Override
        public void close() {
            delegate.close();
        }
    }
}
