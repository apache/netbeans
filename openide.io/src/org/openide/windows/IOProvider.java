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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.openide.windows;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.Reader;
import java.io.StringWriter;
import java.util.Collection;
import javax.swing.Action;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.spi.io.InputOutputProvider;
import org.openide.io.BridgingIOProvider;
import org.openide.util.Lookup;

/** A factory for IO tabs shown in the output window.  To create a new tab to
 * write to, call e.g. <code>IOProvider.getDefault().getIO("MyTab", false)</code>
 * (pass true if there may be an existing tab with the same name and you want
 * to write to a new tab).
 *
 * @author Jesse Glick
 * @since 3.14
 */
public abstract class IOProvider {

    /**
     * Get the default I/O provider.
     * <p>
     * Normally this is taken from {@link Lookup#getDefault} but if there is no
     * instance in lookup, a fallback instance is created which just uses the
     * standard system I/O streams. This is useful for unit tests and perhaps
     * for standalone usage of various libraries.
     * @return the default instance (never null)
     */
    public static IOProvider getDefault() {
        IOProvider iop = Lookup.getDefault().lookup(IOProvider.class);
        if (iop == null) {
            InputOutputProvider<?, ?, ?, ?> newSpiDef
                    = Lookup.getDefault().lookup(InputOutputProvider.class);
            if (newSpiDef != null) {
                iop = BridgingIOProvider.create(newSpiDef);
            } else {
                iop = new Trivial();
            }
        }
        return iop;
    }

    /**
     * Gets IOProvider of selected name or delegates to getDefault() if none was found.
     * @param name ID of provider
     * @return the instance corresponding to provided name or default instance if not found
     * @since 1.15
     */
    @SuppressWarnings("rawtypes")
    public static IOProvider get(String name) {
        Collection<? extends IOProvider> res = Lookup.getDefault().lookupAll(IOProvider.class);
        for (IOProvider iop : res) {
            if (iop.getName().equals(name)) {
                return iop;
            }
        }
        Collection<? extends InputOutputProvider> newSpiImpls
                = Lookup.getDefault().lookupAll(InputOutputProvider.class);
        for (InputOutputProvider<?,?,?,?> impl: newSpiImpls) {
            if (impl.getId().equals(name)) {
                return BridgingIOProvider.create(impl);
            }
        }
        return getDefault();
    }

    /** Subclass constructor. */
    protected IOProvider() {}

    /**
     * Get a named instance of InputOutput, which represents an output tab in
     * the output window.  Streams for reading/writing can be accessed via
     * getters on the returned instance.
     *
     * @param name A localised display name for the tab
     * @param newIO if <tt>true</tt>, a new <code>InputOutput</code> is returned, else an existing <code>InputOutput</code> of the same name may be returned
     * @return an <code>InputOutput</code> instance for accessing the new tab
     * @see InputOutput
     */
    public abstract InputOutput getIO(String name, boolean newIO);

    
    /** 
     *Gets a named instance of InputOutput with actions displayed in the
     * toolbar.
     * Streams for reading/writing can be accessed via
     * getters on the returned instance. 
     *
     * @param name A localized display name for the tab
     * @param actions array of actions that are added to the toolbar, Can be empty array, but not null.
     *   The number of actions should not exceed 5 and each should have the <code>Action.SMALL_ICON</code> property defined.
     * @return an <code>InputOutput</code> instance for accessing the new tab
     * @see InputOutput
     * @since 1.6 <br>
     * Note: The method is non-abstract for backward compatibility reasons only. If you are
     * extending <code>IOProvider</code> and implementing its abstract classes, you are encouraged to override
     * this method as well. The default implementation falls back to the <code>getIO(name, newIO)</code> method, ignoring the actions passed.
     */
    public InputOutput getIO(String name, Action[] actions) {
        return getIO(name, true);
    }

    /**
     * Gets a named instance of {@link InputOutput}. Corresponding IO tab will be placed
     * in parent container corresponding to provided {@link IOContainer}.
     * @param name A localized display name for the tab
     * @param actions array of actions that are added to the toolbar, Can be empty array, but not null.
     *   The number of actions should not exceed 5 and each should have the <code>Action.SMALL_ICON</code> property defined.
     * @param ioContainer parent container accessor
     * @return an <code>InputOutput</code> instance for accessing the new tab
     * <br>Note: Please remember that {@link InputOutput} objects need to be
     * properly closed. Ensure that {@link InputOutput#closeInputOutput()} is
     * called when returned object is no longer needed, otherwise allocated
     * memory and other resources will not be freed.
     * @see InputOutput
     * @since 1.15
     * <br>Note: The method is non-abstract for backward compatibility reasons only. If you are
     * extending <code>IOProvider</code> and implementing its abstract classes, you are encouraged to override
     * this method as well. The default implementation falls back to the <code>getIO(name, actions)</code> method, ignoring the ioContainer passed.
     */
    public InputOutput getIO(String name, Action[] actions, IOContainer ioContainer) {
        return getIO(name, actions);
    }

    /**
     * Gets a named instance of {@link InputOutput}. Corresponding IO tab will be placed
     * in parent container corresponding to provided {@link IOContainer}.
     * @param name A localized display name for the tab
     * @param newIO if <tt>true</tt>, a new <code>InputOutput</code> is returned, else an existing <code>InputOutput</code> of the same name may be returned
     * @param actions array of actions that are added to the toolbar, Can be empty array, but not null.
     *   The number of actions should not exceed 5 and each should have the <code>Action.SMALL_ICON</code> property defined.
     * @param ioContainer parent container accessor
     * @return an <code>InputOutput</code> instance for accessing the new tab
     * <br>Note: Please remember that {@link InputOutput} objects need to be
     * properly closed. Ensure that {@link InputOutput#closeInputOutput()} is
     * called when returned object is no longer needed, otherwise allocated
     * memory and other resources will not be freed.
     * @see InputOutput
     * @since 1.33
     * <br>Note: The method is non-abstract for backward compatibility reasons only. If you are
     * extending <code>IOProvider</code> and implementing its abstract classes, you are encouraged to override
     * this method as well. The default implementation falls back to the <code>getIO(name, actions)</code> method, ignoring the ioContainer and newIO passed.
     */
    public @NonNull InputOutput getIO(@NonNull String name, boolean newIO,
            @NonNull Action[] actions, @NullAllowed IOContainer ioContainer) {
        return getIO(name, actions);
    }

    /**
     * Gets name (ID) of provider
     * @return name of provider
     * @since 1.15
     * <br>Note: The method is non-abstract for backward compatibility reasons only. If you are
     * extending <code>IOProvider</code>  you should override this method. The default implementation returns ""
     */
    public String getName() {
        return "";
    }

    /** Support writing to the Output Window on the main tab or a similar output device.
     * @return a writer for the standard NetBeans output area
     */
    public abstract OutputWriter getStdOut();
    
    /** Fallback implementation. */
    private static final class Trivial extends IOProvider {
        
        private static final Reader in = new BufferedReader(new InputStreamReader(System.in));
        private static final PrintStream out = System.out;
        private static final PrintStream err = System.err;

        public Trivial() {}

        public InputOutput getIO(String name, boolean newIO) {
            return new TrivialIO(name);
        }

        public OutputWriter getStdOut() {
            return new TrivialOW(out, "stdout"); // NOI18N
        }
        
        @SuppressWarnings("deprecation")
        private final class TrivialIO implements InputOutput {
            
            private final String name;
            
            public TrivialIO(String name) {
                this.name = name;
            }

            public Reader getIn() {
                return in;
            }

            public OutputWriter getOut() {
                return new TrivialOW(out, name);
            }

            public OutputWriter getErr() {
                return new TrivialOW(err, name);
            }

            public Reader flushReader() {
                return getIn();
            }

            public boolean isClosed() {
                return false;
            }

            public boolean isErrSeparated() {
                return false;
            }

            public boolean isFocusTaken() {
                return false;
            }

            public void closeInputOutput() {}

            public void select() {}

            public void setErrSeparated(boolean value) {}

            public void setErrVisible(boolean value) {}

            public void setFocusTaken(boolean value) {}

            public void setInputVisible(boolean value) {}

            public void setOutputVisible(boolean value) {}
            
        }
        
        private static final class TrivialOW extends OutputWriter {
            
            private static int count = 0;
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

            public void println(String s, OutputListener l) throws IOException {
                prefix(l != null);
                stream.println(s);
            }

            public void reset() throws IOException {}

            @Override
            public void println(float x) {
                prefix(false);
                stream.println(x);
            }

            @Override
            public void println(double x) {
                prefix(false);
                stream.println(x);
            }

            @Override
            public void println() {
                prefix(false);
                stream.println();
            }

            @Override
            public void println(Object x) {
                prefix(false);
                stream.println(x);
            }

            @Override
            public void println(int x) {
                prefix(false);
                stream.println(x);
            }

            @Override
            public void println(char x) {
                prefix(false);
                stream.println(x);
            }

            @Override
            public void println(long x) {
                prefix(false);
                stream.println(x);
            }

            @Override
            public void println(char[] x) {
                prefix(false);
                stream.println(x);
            }

            @Override
            public void println(boolean x) {
                prefix(false);
                stream.println(x);
            }

            @Override
            public void println(String x) {
                prefix(false);
                stream.println(x);
            }

            @Override
            public void write(int c) {
                stream.write(c);
            }

            @Override
            public void write(char[] buf, int off, int len) {
                String s = new String(buf, off, len);
                if (s.endsWith("\n")) {
                    println(s.substring(0, s.length() - 1));
                } else {
                    try {
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
                        stream.write(s.getBytes());
                    } catch (IOException x) {}
                }
            }

        }
        
    }

}
