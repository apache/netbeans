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

package org.netbeans.api.extexecution.print;

import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.extexecution.base.input.InputProcessor;
import org.netbeans.api.extexecution.base.input.LineProcessor;
import org.openide.windows.OutputWriter;

/**
 * Factory methods for {@link LineProcessor} classes.
 * <p>
 * Note that main difference between {@link InputProcessor} and
 * {@link LineProcessor} is that LineProcessor always process whole lines.
 *
 * @author Petr Hejl
 * @see org.netbeans.api.extexecution.base.input.InputProcessors#bridge(org.netbeans.api.extexecution.base.input.LineProcessor)
 * @since 1.43
 */
public final class LineProcessors {

    private static final Logger LOGGER = Logger.getLogger(LineProcessors.class.getName());

    private LineProcessors() {
        super();
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
        return printing(out, null, resetEnabled);
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
        return new PrintingLineProcessor(out, convertor, resetEnabled);
    }

    private static class PrintingLineProcessor implements LineProcessor {

        private final OutputWriter out;

        private final LineConvertor convertor;

        private final boolean resetEnabled;

        private boolean closed;

        public PrintingLineProcessor(OutputWriter out, LineConvertor convertor, boolean resetEnabled) {
            assert out != null;

            this.out = out;
            this.convertor = convertor;
            this.resetEnabled = resetEnabled;
        }

        public void processLine(String line) {
            assert line != null;

            if (closed) {
                throw new IllegalStateException("Already closed processor");
            }

            LOGGER.log(Level.FINEST, line);

            if (convertor != null) {
                List<ConvertedLine> convertedLines = convertor.convert(line);
                if (convertedLines != null) {
                    for (ConvertedLine converted : convertedLines) {
                        if (converted.getListener() == null) {
                            out.println(converted.getText());
                        } else {
                            try {
                                out.println(converted.getText(), converted.getListener());
                            } catch (IOException ex) {
                                LOGGER.log(Level.INFO, null, ex);
                                out.println(converted.getText());
                            }
                        }
                    }
                } else {
                    out.println(line);
                }
            } else {
                out.println(line);
            }
            out.flush();
        }

        public void reset() {
            if (closed) {
                throw new IllegalStateException("Already closed processor");
            }

            if (!resetEnabled) {
                return;
            }

            try {
                out.reset();
            } catch (IOException ex) {
                LOGGER.log(Level.INFO, null, ex);
            }
        }

        public void close() {
            closed = true;

            out.flush();
            out.close();
        }
    }
}
