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
package org.netbeans.api.io;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Locale;
import java.util.Set;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.spi.io.InputOutputProvider;

/**
 * Extended {@link PrintWriter} for writing into output window or similar output
 * GUI component. It can support features like color printing, hyperlinks, or
 * folding.
 *
 * <p>
 * Methods of this class can be called in any thread.
 * </p>
 *
 * @author jhavlin
 */
public abstract class OutputWriter extends PrintWriter {

    private OutputWriter() {
        super(new DummyWriter());
    }

    /**
     * Get current position in the output stream.
     *
     * @return The position.
     */
    public abstract Position getCurrentPosition();

    /**
     * Start a new fold. If a fold already exists, a nested fold will be
     * created.
     *
     * @param expanded True if the fold should be expanded by default, false if
     * it should be collapsed.
     *
     * @return The fold handle.
     */
    public abstract Fold startFold(boolean expanded);

    /**
     * Finish a fold. If it contains some unfinished nested folds, they will be
     * finished as well.
     *
     * @param fold The fold to finish.
     */
    public abstract void endFold(Fold fold);

    public abstract void print(String s, Hyperlink link, OutputColor color);

    public abstract void print(String s, Hyperlink link);

    public abstract void print(String s, OutputColor color);

    public abstract void println(String s, Hyperlink link, OutputColor color);

    public abstract void println(String s, Hyperlink link);

    public abstract void println(String s, OutputColor color);

    static <IO, OW extends PrintWriter, P, F> OutputWriter create(
            InputOutputProvider<IO, OW, P, F> provider, IO io, OW writer) {

        return new Impl<IO, OW, P, F>(provider, io, writer);
    }

    private static class Impl<IO, OW extends PrintWriter, P, F>
            extends OutputWriter {

        private final InputOutputProvider<IO, OW, P, F> provider;
        private final IO io;
        private final OW writer;

        public Impl(InputOutputProvider<IO, OW, P, F> provider,
                IO io, OW writer) {

            this.provider = provider;
            this.io = io;
            this.writer = writer;
        }

        @Override
        public Position getCurrentPosition() {
            return Position.create(provider, io, writer,
                    provider.getCurrentPosition(io, writer));
        }

        @Override
        public void print(String s, Hyperlink link, OutputColor color) {
            provider.print(io, writer, s, link, color, false);
        }

        @Override
        public void print(String s, Hyperlink link) {
            provider.print(io, writer, s, link, null, false);
        }

        @Override
        public void print(String s, OutputColor color) {
            provider.print(io, writer, s, null, color, false);
        }

        @Override
        public void println(String s, Hyperlink link, OutputColor color) {
            provider.print(io, writer, s, link, color, true);
        }

        @Override
        public void println(String s, Hyperlink link) {
            provider.print(io, writer, s, link, null, true);
        }

        @Override
        public void println(String s, OutputColor color) {
            provider.print(io, writer, s, null, color, true);
        }

        @Override
        public void flush() {
            writer.flush();
        }

        @Override
        public void close() {
            writer.close();
        }

        @Override
        public boolean checkError() {
            return writer.checkError();
        }

        @Override
        public void write(int c) {
            writer.write(c);
        }

        @Override
        public void write(char[] buf, int off, int len) {
            writer.write(buf, off, len);
        }

        @Override
        public void write(char[] buf) {
            writer.write(buf);
        }

        @Override
        public void write(String s, int off, int len) {
            writer.write(s, off, len);
        }

        @Override
        public void write(String s) {
            writer.write(s);
        }

        @Override
        public void print(boolean b) {
            writer.print(b);
        }

        @Override
        public void print(char c) {
            writer.print(c);
        }

        @Override
        public void print(int i) {
            writer.print(i);
        }

        @Override
        public void print(long l) {
            writer.print(l);
        }

        @Override
        public void print(float f) {
            writer.print(f);
        }

        @Override
        public void print(double d) {
            writer.print(d);
        }

        @Override
        @SuppressWarnings("ImplicitArrayToString")
        public void print(char[] s) {
            writer.print(s);
        }

        @Override
        public void print(String s) {
            writer.print(s);
        }

        @Override
        public void print(Object obj) {
            writer.print(obj);
        }

        @Override
        public void println() {
            writer.println();
        }

        @Override
        public void println(boolean x) {
            writer.println(x);
        }

        @Override
        public void println(char x) {
            writer.println(x);
        }

        @Override
        public void println(int x) {
            writer.println(x);
        }

        @Override
        public void println(long x) {
            writer.println(x);
        }

        @Override
        public void println(float x) {
            writer.println(x);
        }

        @Override
        public void println(double x) {
            writer.println(x);
        }

        @Override
        @SuppressWarnings("ImplicitArrayToString")
        public void println(char[] x) {
            writer.println(x);
        }

        @Override
        public void println(String x) {
            writer.println(x);
        }

        @Override
        public void println(Object x) {
            writer.println(x);
        }

        @Override
        public PrintWriter printf(String format, Object... args) {
            return writer.printf(format, args);
        }

        @Override
        public PrintWriter printf(Locale l, String format, Object... args) {
            return writer.printf(l, format, args);
        }

        @Override
        public PrintWriter format(String format, Object... args) {
            return writer.format(format, args);
        }

        @Override
        public PrintWriter format(Locale l, String format, Object... args) {
            return writer.format(l, format, args);
        }

        @Override
        public PrintWriter append(CharSequence csq) {
            return writer.append(csq);
        }

        @Override
        public PrintWriter append(CharSequence csq, int start, int end) {
            return writer.append(csq, start, end);
        }

        @Override
        public PrintWriter append(char c) {
            return writer.append(c);
        }

        @Override
        public Fold startFold(boolean expanded) {
            F fold = provider.startFold(io, writer, expanded);
            return Fold.create(provider, io, writer, fold);
        }

        @Override
        public void endFold(Fold fold) {
            if (fold != Fold.UNSUPPORTED) {
                fold.endFold();
            }
        }
    }

    private static class DummyWriter extends Writer {

        @Override
        public void write(char[] cbuf, int off, int len) throws IOException {
        }

        @Override
        public void flush() throws IOException {
        }

        @Override
        public void close() throws IOException {
        }
    }
}
