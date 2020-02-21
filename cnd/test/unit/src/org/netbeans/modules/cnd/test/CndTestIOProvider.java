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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.cnd.test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import javax.swing.Action;
import org.openide.util.lookup.ServiceProvider;
import org.openide.windows.IOContainer;
import org.openide.windows.IOProvider;
import org.openide.windows.InputOutput;
import org.openide.windows.OutputListener;
import org.openide.windows.OutputWriter;

/**
 *
 */
@ServiceProvider(service=org.openide.windows.IOProvider.class, position=10)
public class CndTestIOProvider extends IOProvider {

    public interface Listener {
        public void linePrinted(String line);
    }

    private static final Reader in = new BufferedReader(new InputStreamReader(System.in));
    private static final PrintStream out;
    private static final PrintStream err;
    static {
        if ("true".equals(System.getProperty("org.netbeans.modules.cnd.test.CndTestIOProvider.traceout"))) {
            out = System.out;
        } else {
            out = new PrintStream(new OutputStream() {
                @Override
                public void write(int b) throws IOException {
                }
            });
        }
        err = System.err;
    }
    private final List<Listener> listeners = new ArrayList();

    public CndTestIOProvider() {
        //System.err.printf("CndTestIOProvider.ctor\n");
    }

    @Override
    public String getName() {
        return "CndTestIOProvider";
    }


    @Override
    public InputOutput getIO(String name, boolean newIO) {
        return new TrivialIO(name);
    }

    @Override
    public InputOutput getIO(String name, Action[] actions) {
        return new TrivialIO(name);
    }

    @Override
    public InputOutput getIO(String name, Action[] actions, IOContainer ioContainer) {
        return new TrivialIO(name);
    }

    @Override
    public OutputWriter getStdOut() {
        return new TrivialOW(out, "stdout"); // NOI18N
    }

    public void addListener(Listener listener) {
        synchronized (this) {
            listeners.add(listener);
        }
    }

    public void removeListener(Listener listener) {
        synchronized (this) {
            listeners.remove(listener);
        }
    }

    private void fireLinePrinted(String line) {
        Listener[] la = null;
        synchronized (this) {
            if (!listeners.isEmpty()) {
                la = new Listener[listeners.size()];
                listeners.toArray(la);
            }
        }
        if (la != null) {
            for (int i = 0; i < la.length; i++) {
                la[i].linePrinted(line);
            }
        }
    }

    @SuppressWarnings("deprecation")
    private final class TrivialIO implements InputOutput {

        private final String name;

        public TrivialIO(String name) {
            this.name = name;
        }

        @Override
        public Reader getIn() {
            return in;
        }

        @Override
        public OutputWriter getOut() {
            return new TrivialOW(out, name);
        }

        @Override
        public OutputWriter getErr() {
            return new TrivialOW(err, name);
        }

        @Override
        public Reader flushReader() {
            return getIn();
        }

        @Override
        public boolean isClosed() {
            return false;
        }

        @Override
        public boolean isErrSeparated() {
            return false;
        }

        @Override
        public boolean isFocusTaken() {
            return false;
        }

        @Override
        public void closeInputOutput() {}

        @Override
        public void select() {}

        @Override
        public void setErrSeparated(boolean value) {}

        @Override
        public void setErrVisible(boolean value) {}

        @Override
        public void setFocusTaken(boolean value) {}

        @Override
        public void setInputVisible(boolean value) {}

        @Override
        public void setOutputVisible(boolean value) {}

    }

    private static int count = 0;

    private final class TrivialOW extends OutputWriter {
        
        private final String name;
        private final PrintStream stream;

        public TrivialOW(PrintStream stream, String name) {
            // XXX using super(new PrintWriter(stream)) does not seem to work for some reason!
            super(new StringWriter());
            this.stream = stream;
            if (name != null) {
                this.name = name;
            } else {
                this.name = "anon-" + ++count; // NOI18N
            }
        }

        private void prefix(boolean hyperlink) {
            if (hyperlink) {
                stream.print("[" + name + "]* "); // NOI18N
            } else {
                stream.print("[" + name + "]  "); // NOI18N
            }
        }

        @Override
        public void println(String s, OutputListener l, boolean important) throws IOException {
            println(s);
        }

        @Override
        public PrintWriter append(CharSequence csq) {
            print(csq.toString());
            return this;
        }

        @Override
        public PrintWriter append(CharSequence csq, int start, int end) {
            print(csq.subSequence(start, end).toString());
            return this;
        }

        @Override
        public PrintWriter append(char c) {
            print(c);
            return this;
        }

        @Override
        public PrintWriter format(String format, Object... args) {
            return printf(format, args);
        }

        @Override
        public PrintWriter format(Locale l, String format, Object... args) {
            return printf(l, format, args);
        }

        @Override
        public void print(boolean b) {
            print("" + b);
        }

        @Override
        public void print(char c) {
            fireLinePrinted("" + c);
            stream.print(c);
        }

        @Override
        public void print(int i) {
            print("" + i);
        }

        @Override
        public void print(long l) {
            print("" + l);
        }

        @Override
        public void print(float f) {
            print("" + f);
        }

        @Override
        public void print(double d) {
            print("" + d);
        }

        @Override
        public void print(char[] s) {
            fireLinePrinted(new String(s));
            stream.print(s);
        }

        @Override
        public void print(String s) {
            fireLinePrinted(s);
            stream.print(s);
        }

        @Override
        public void print(Object obj) {
            print(obj == null ? "null" : obj.toString());
        }

        @Override
        public PrintWriter printf(String format, Object... args) {
            String text = String.format(format, args);
            print(text);
            return this;
        }

        @Override
        public PrintWriter printf(Locale l, String format, Object... args) {
            String text = String.format(l, format, args);
            print(text);
            return this;
        }

        @Override
        public void write(char[] buf) {
            print(buf);
        }

        @Override
        public void write(String s) {
            print(s);
        }

        @Override
        public void println(String s, OutputListener l) throws IOException {
            prefix(l != null);
            stream.println(s);
        }

        @Override
        public void reset() throws IOException {}

        @Override
        public void println(float x) {
            fireLinePrinted("" + x);
            prefix(false);
            stream.println(x);
        }

        @Override
        public void println(double x) {
            fireLinePrinted("" + x);
            prefix(false);
            stream.println(x);
        }

        @Override
        public void println() {
            fireLinePrinted("");
            prefix(false);
            stream.println();
        }

        @Override
        public void println(Object x) {
            fireLinePrinted("" + x);
            prefix(false);
            stream.println(x);
        }

        @Override
        public void println(int x) {
            fireLinePrinted("" + x);
            prefix(false);
            stream.println(x);
        }

        @Override
        public void println(char x) {
            fireLinePrinted("" + x);
            prefix(false);
            stream.println(x);
        }

        @Override
        public void println(long x) {
            fireLinePrinted("" + x);
            prefix(false);
            stream.println(x);
        }

        @Override
        public void println(char[] x) {
            fireLinePrinted(String.copyValueOf(x));
            prefix(false);
            stream.println(x);
        }

        @Override
        public void println(boolean x) {
            fireLinePrinted("" + x);
            prefix(false);
            stream.println(x);
        }

        @Override
        public void println(String x) {
            fireLinePrinted(x);
            prefix(false);
            stream.println(x);
        }

        @Override
        public void write(int c) {
            fireLinePrinted("" + c);
            stream.write(c);
        }

        @Override
        public void write(char[] buf, int off, int len) {
            String s = new String(buf, off, len);
            if (s.endsWith("\n")) {
                println(s.substring(0, s.length() - 1));
            } else {
                try {
                    fireLinePrinted(s); // is it worth to write something smarter?
                    stream.write(s.getBytes());
                } catch (IOException x) {}
            }
        }

        @Override
        public void write(String s, int off, int len) {
            s = s.substring(off, off + len);
            if (s.endsWith("\n")) {
                println(s.substring(0, s.length() - 1));
            } else {
                try {
                    fireLinePrinted(s); // is it worth to write something smarter?
                    stream.write(s.getBytes());
                } catch (IOException x) {}
            }
        }

    }


}
